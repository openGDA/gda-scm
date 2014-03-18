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

package uk.ac.gda.devices.bssc.ui.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanEditor;
import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanUIEditor;
import uk.ac.gda.devices.bssc.wizards.BSSCImportWizardPage;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class LoadExperimentHandler implements IHandler {
	private static final Logger logger = LoggerFactory.getLogger(BSSCImportWizardPage.class);
	private static final int PLATE_COL_NO = 0;
	private static final int PLATE_ROW_COL_NO = 1;
	private static final int PLATE_COLUMN_COL_NO = 2;
	private static final int SAMPLE_NAME_COL_NO = 3;
	private static final int CONCENTRATION_COL_NO = 4;
	private static final int VISCOSITY_COL_NO = 5;
	private static final int MOLECULAR_WEIGHT_COL_NO = 6;
	private static final int BUFFER_PLATE_COL_NO = 7;
	private static final int BUFFER_ROW_COL_NO = 8;
	private static final int BUFFER_COLUMN_COL_NO = 9;
	private static final int RECOUP_PLATE_COL_NO = 10;
	private static final int RECOUP_ROW_COL_NO = 11;
	private static final int RECOUP_COLUMN_COL_NO = 12;
	private static final int TIME_PER_FRAME_COL_NO = 13;
	private static final int FRAMES_COL_NO = 14;
	private static final int EXPOSURE_TEMP_COL_NO = 15;

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath(System.getProperty("user.home") + "//experiments");
		String[] filterExt = { "*.xls" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();

		File fileToOpen = new File(selected);

		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			// IDE.openEditorOnFileStore(page, fileStore);
			Workbook wb;
			try {
				wb = WorkbookFactory.create(fileToOpen);
				Sheet sheet = wb.getSheetAt(0);

				BSSCSessionBean sessionBean = new BSSCSessionBean();
				List<TitrationBean> measurements = new ArrayList<TitrationBean>();

				for (Row row : sheet) {

					try {
						TitrationBean tibi = new TitrationBean();

						LocationBean location = locationFromCells(row.getCell(PLATE_COL_NO),
								row.getCell(PLATE_ROW_COL_NO), row.getCell(PLATE_COLUMN_COL_NO));
						if (!location.isValid())
							throw new Exception("invalid sample location");
						tibi.setLocation(location);

						tibi.setSampleName(row.getCell(SAMPLE_NAME_COL_NO).getStringCellValue());

						location = locationFromCells(row.getCell(BUFFER_PLATE_COL_NO), row.getCell(BUFFER_ROW_COL_NO),
								row.getCell(BUFFER_COLUMN_COL_NO));
						if (!location.isValid())
							throw new Exception("invalid buffer location");
						tibi.setBufferLocation(location);

						location = locationFromCells(row.getCell(RECOUP_PLATE_COL_NO), row.getCell(RECOUP_ROW_COL_NO),
								row.getCell(RECOUP_COLUMN_COL_NO));
						if (!location.isValid())
							location = null;

						tibi.setRecouperateLocation(location);
						tibi.setConcentration(row.getCell(CONCENTRATION_COL_NO).getNumericCellValue());
						tibi.setViscosity(row.getCell(VISCOSITY_COL_NO).getStringCellValue());
						tibi.setMolecularWeight(row.getCell(MOLECULAR_WEIGHT_COL_NO).getNumericCellValue());
						tibi.setTimePerFrame(row.getCell(TIME_PER_FRAME_COL_NO).getNumericCellValue());
						tibi.setFrames((int) row.getCell(FRAMES_COL_NO).getNumericCellValue());
						tibi.setExposureTemperature((float) row.getCell(EXPOSURE_TEMP_COL_NO).getNumericCellValue());

						measurements.add(tibi);
					} catch (Exception e) {
						logger.debug("row rejected" + row.toString());
					}
				}

				sessionBean.setMeasurements(measurements);

				// Need to convert xls to biosaxs
				String expXlsFile = fileToOpen.getAbsolutePath();
				String xmlFilePath = expXlsFile.substring(0, expXlsFile.lastIndexOf("."));
				String bioSAXSFilePath = xmlFilePath + ".biosaxs";

				File bioSAXSfile = new File(bioSAXSFilePath);
				XMLHelpers.writeToXML(BSSCSessionBean.mappingURL, sessionBean, bioSAXSfile);

//				BSSCSessionBeanEditor editor = new BSSCSessionBeanEditor();
//				editor.getRichBeanEditorPart(bioSAXSFilePath, sessionBean);

				new BSSCSessionBeanUIEditor(bioSAXSFilePath, BSSCSessionBean.mappingURL, new BSSCSessionBeanEditor(),
						sessionBean);

				IFileStore biosaxsFileStore = EFS.getLocalFileSystem().getStore(bioSAXSfile.toURI());
				IDE.openEditorOnFileStore(page, biosaxsFileStore);

				bioSAXSfile.delete();
			} catch (PartInitException e) {
				logger.error("PartInitException opening editor", e);
			} catch (InvalidFormatException e1) {
				logger.error("InvalidFormatException creating Workbook", e1);
			} catch (IOException e1) {
				logger.error("IOException creating Workbook", e1);
			} catch (Exception e) {
				logger.error("Exception writing to xml", e);
			}
		}
		return null;
	}

	private short parsePlateCell(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return (short) cell.getNumericCellValue();
		}
		short result = 0;
		String str = cell.getStringCellValue();
		for (int i = 0; i < str.length(); i++) {
			if ("I".equalsIgnoreCase(str.substring(i, i + 1)))
				result++;
		}
		return result;
	}

	private LocationBean locationFromCells(Cell platec, Cell rowc, Cell columnc) {
		LocationBean location = new LocationBean();
		location.setPlate(parsePlateCell(platec));
		location.setRow(rowc.getStringCellValue().charAt(0));
		location.setColumn((short) columnc.getNumericCellValue());
		return location;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
