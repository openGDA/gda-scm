/*-
 * Copyright © 2014 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.rcp.ncd.actions;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;

import java.io.File;
import java.util.Collection;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.UIHelper;
import uk.ac.gda.server.ncd.beans.StoredDetectorInfo;

public class MaskFileUpdater extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(MaskFileUpdater.class);
	private static IPersistenceService service;
	private StoredDetectorInfo fileLocation;
	private IPersistentFile file;

	public static void setPersistenceService(IPersistenceService s) { // Injected
		service = s;
	}

	public MaskFileUpdater() {
		fileLocation = Finder.getInstance().find("detectorInfoPath");
	}
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String newFile = LocalProperties.getVarDir() + "temporaryMaskFileForMaskFileUpdater.hdf5";
		try {
			saveMaskFile(newFile);
			fileLocation.setSaxsDetectorInfoPath(newFile);
			MessageBox dialog = new MessageBox(new Shell(), SWT.ICON_INFORMATION | SWT.OK);
			dialog.setText("Mask and Profiles Saved");
			dialog.setMessage("The mask and any profiles selected have been saved to " + fileLocation.getSaxsDetectorInfoPath());
			dialog.open();
		} catch (Exception e) {
			logger.error("Could not save mask file", e);
			fileLocation.clearSaxsDetectorInfoPath();
			UIHelper.showError("Error Saving File", "The mask/profile file could not be saved (existing file will continue to be used). If problem continues please contact support");
		} finally {
			if (file != null) {
				file.close();
			}
			File newF = new File(newFile);
			if (newF.exists()) {
				newF.delete();
			}
		}
		return true;
	}
	
	private void saveMaskFile(String newFile) throws Exception {
		logger.debug("Saving mask file to {}", newFile);
		createMaskFile(newFile);
		addMaskToFile();
		addRegionsToFile();
		addDiffractionMetadataToFile();
	}
	
	private void createMaskFile(String newFile) throws Exception {
		file = service.createPersistentFile(newFile);
	}
	
	private void addMaskToFile() throws Exception {
		IImageTrace image = getImage();
		IDataset mask = image.getMask();
		if (mask != null) {
			file.addMask("mask", image.getMask(), null);
		} else {
			logger.debug("No mask to save");
		}
	}

	private IImageTrace getImage() throws Exception {
		final ITrace trace = getPlottingSystem().getTraces().iterator().next();
		if (trace instanceof IImageTrace) {
			IImageTrace image = (IImageTrace)trace;
			return image;
		}
		logger.error("No mask found");
		throw new Exception("Could not find mask");
	}
	
	private void addRegionsToFile() throws Exception {
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions!=null && !regions.isEmpty()) {
			for (IRegion iRegion : regions) {
				addRegionToFile(iRegion);
			}
		} else {
			logger.debug("No regions to add to file");
		}
	}
	
	private void addDiffractionMetadataToFile() {
		IImageTrace trace;
		try {
			trace = getImage();
			if (trace!=null && trace.getData() != null) {
				IMetadata meta = trace.getData().getMetadata();
				if (meta == null || meta instanceof IDiffractionMetadata) {
					file.setDiffractionMetadata((IDiffractionMetadata) meta);
				}
			} else {
				logger.debug("No diffractionMetadata to save to file");
			}
		} catch (Exception e) {
			logger.error("Could not include metadata in mask file", e);
		}
	}
	
	private void addRegionToFile(IRegion r) throws Exception {
		if (!file.isRegionSupported(r.getROI())) {
			return;
		}
		file.addROI(r.getName(), r.getROI());
		file.setRegionAttribute(r.getName(), "Region Type", r.getRegionType().getName());
		if (r.getUserObject()!=null) {
			file.setRegionAttribute(r.getName(), "User Object", r.getUserObject().toString()); 
		}
	}

	private IPlottingSystem getPlottingSystem() {
		final IWorkbenchPart part = EclipseUtils.getPage().getActivePart();
		if (part!=null) {
			return (IPlottingSystem)part.getAdapter(IPlottingSystem.class);
		}
		return null;
	}
}
