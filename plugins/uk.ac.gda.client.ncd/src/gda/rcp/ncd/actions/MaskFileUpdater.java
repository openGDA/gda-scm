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

import gda.data.PathConstructor;
import gda.factory.Finder;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.beans.StoredDetectorInfo;

public class MaskFileUpdater extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(MaskFileUpdater.class);
	private StoredDetectorInfo fileLocation;
	private IPersistentFile file;
	
	public MaskFileUpdater() {
		fileLocation = Finder.getInstance().find("detectorInfoPath");
	}
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String newFile = PathConstructor.createFromDefaultProperty() + "/" + newFileName();
		saveMaskFile(newFile);
		fileLocation.setSaxsDetectorInfoPath(newFile);
		return true;
	}
	
	private void saveMaskFile(String newFile) {
		try {
			logger.debug("Saving mask file to {}", newFile);
			 createMaskFile(newFile);
			 addMaskToFile();
			 addRegionsToFile();
		} catch (Exception e) {
			logger.error("Could not save mask file", e);
		} finally {
			if (file != null) {
				file.close();
			}
		}
		
	}

	private static String newFileName() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String timeStamp = sdf.format(now);
		return String.format("detectorMask-%s.h5", timeStamp);
	}
	
	private void createMaskFile(String newFile) throws Exception {
		IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
		file = service.createPersistentFile(newFile);
	}
	
	private void addMaskToFile() throws Exception {
		IImageTrace image = getImage();
		file.addMask("mask", image.getMask(), null);
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
		}
	}
	
	private void addRegionToFile(IRegion roi) throws Exception {
		if (!file.isRegionSupported(roi.getROI())) {
			return;
		}
		file.addROI(roi.getName(), roi.getROI());
		file.setRegionAttribute(roi.getName(), "Region Type", roi.getRegionType().getName());
		if (roi.getUserObject()!=null) {
			file.setRegionAttribute(roi.getName(), "User Object", roi.getUserObject().toString()); 
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
