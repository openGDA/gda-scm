/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public final class BSSCSessionBeanEditor extends RichBeanMultiPageEditorPart {
	private static final Logger logger = LoggerFactory.getLogger(BSSCSessionBeanEditor.class);
	private static final int SAMPLE_COLLECTIONS_SIZE = 7;
	private BSSCSessionBean sessionBean;
	private ArrayList<TitrationBean> measurements;

	public BSSCSessionBeanEditor() {
		super();
		setPartProperty("RichBeanEditorPart", null);
	}

	@Override
	public Class<?> getBeanClass() {
		return BSSCSessionBean.class;
	}

	@Override
	public URL getMappingUrl() {
		return BSSCSessionBean.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new BSSCSessionBeanUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return BSSCSessionBean.schemaURL; // Please make sure this field is present and the schema
	}

	@Override
	public void setInput(final IEditorInput input) {
		try {
			assignInput(input);
			createBean();
			linkUI();
		} catch (Throwable th) {
			logger.error("Error setting input for editor from input " + input.getName(), th);
		}
	}

	/**
	 * NOTE Can save both to this project, in which case add as IFile or to any other location, in which case add as
	 * external resource.
	 */
	@Override
	public void doSaveAs() {

		final IFile currentiFile = EclipseUtils.getIFile(getEditorInput());
		final IFolder folder = (currentiFile != null) ? (IFolder) currentiFile.getParent() : null;

		final FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		dialog.setText("Save as BIOSAXS Experiment");
		dialog.setFilterExtensions(new String[] { "*.biosaxs", "*.xml" });
		final File currentFile = new File(this.path);
		dialog.setFilterPath(currentFile.getParentFile().getAbsolutePath());

		String newFile = dialog.open();
		if (newFile != null) {
			if (!newFile.endsWith(".biosaxs") && (!newFile.endsWith(".xml")))
				newFile = newFile + ".biosaxs";
			newFile = validateFileName(newFile);
			if (newFile == null)
				return;

			final File file = new File(newFile);
			if (file.exists()) {
				final boolean ovr = MessageDialog.openQuestion(getSite().getShell(), "Confirm File Overwrite",
						"The file '" + file.getName() + "' exists in '" + file.getParentFile().getName() + "'.\n\n"
								+ "Would you like to overwrite it?");
				if (!ovr)
					return;
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				MessageDialog.openError(getSite().getShell(), "Cannot save file.", "The file '" + file.getName()
						+ "' cannot be created in '" + file.getParentFile().getName() + "'.\n\n" + e.getMessage());
				return;
			}
			try {
				if (!confirmFileNameChange(currentFile, file)) {
					file.delete();
					return;
				}
			} catch (Exception ne) {
				logger.error("Cannot confirm name change", ne);
				return;
			}

			IEditorInput input;
			if (folder != null && folder.getLocation().toFile().equals(file.getParentFile())) {
				final IFile ifile = folder.getFile(file.getName());
				try {
					ifile.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					logger.error("Cannot refresh " + ifile, e);
				}
				input = new FileEditorInput(ifile);
			} else {
				input = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(file));
			}

			assignInput(input);
			doSave(new NullProgressMonitor());
			setDirty(false);
		}
	}

	public void openDefaultEditor() {
		String bioSAXSFilePath = "default" + ".biosaxs";
		sessionBean = new BSSCSessionBean();
		measurements = new ArrayList<TitrationBean>();
		short plateIndex = 3;
		char[] rows = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
		String[] viscosities = { "low", "medium", "high" };
		int viscosityIndex = 0;

		File bioSAXSfile = new File(bioSAXSFilePath);

		// if the file dosen't exist then create a new one with default values
		if (!bioSAXSfile.exists()) {
			try {

				for (int i = 1; i <= SAMPLE_COLLECTIONS_SIZE; i++) {
					TitrationBean tibi = new TitrationBean();

					LocationBean bufferLocation = new LocationBean();
					LocationBean location = new LocationBean();
					tibi.setSampleName("Sample " + i);
					tibi.setFrames(i);

					if (viscosityIndex > 2)
						viscosityIndex = 0;
					tibi.setViscosity(viscosities[viscosityIndex]);

					short columnIndex = Integer.valueOf(i).shortValue();
					bufferLocation.setColumn(columnIndex);
					location.setColumn(columnIndex);
					bufferLocation.setRow(rows[i]);
					location.setRow(rows[i]);

					if (plateIndex < 1)
						plateIndex = 3;

					location.setPlate(plateIndex);

					if (!location.isValid())
						throw new Exception("invalid sample location");
					tibi.setLocation(location);
					tibi.setBufferLocation(location);

					if (!location.isValid())
						throw new Exception("invalid buffer location");

					if (!location.isValid())
						location = null;

					tibi.setRecouperateLocation(null);
					tibi.setConcentration(1);
					tibi.setMolecularWeight(1);
					tibi.setTimePerFrame(1);
					tibi.setFrames(1);
					tibi.setExposureTemperature(22);

					plateIndex--;
					viscosityIndex++;
					measurements.add(tibi);
				}
			} catch (InvalidFormatException e) {
				logger.error("InvalidFormatException reading Workbook ", e);
			} catch (IOException e) {
				logger.error("IOException reading Workbook ", e);
			} catch (Exception e) {
				logger.error("Exception ", e);
			}

			sessionBean.setMeasurements(measurements);
			try {
				XMLHelpers.writeToXML(BSSCSessionBean.mappingURL, sessionBean, bioSAXSfile);
			} catch (Exception e) {
				logger.error("Exception writing bean to XML", e);
			}

			new BSSCSessionBeanUIEditor(bioSAXSFilePath, BSSCSessionBean.mappingURL, new BSSCSessionBeanEditor(),
					sessionBean);
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFileStore biosaxsFileStore = EFS.getLocalFileSystem().getStore(bioSAXSfile.toURI());
		try {
			if (page != null) {
				IDE.openEditorOnFileStore(page, biosaxsFileStore);
			}
		} catch (PartInitException e) {
			logger.error("PartInitException opening editor", e);
		}
	}
}