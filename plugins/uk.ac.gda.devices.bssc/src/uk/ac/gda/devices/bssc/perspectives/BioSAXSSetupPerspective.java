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

package uk.ac.gda.devices.bssc.perspectives;

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
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanEditor;
import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanUIEditor;
import uk.ac.gda.devices.bssc.wizards.BSSCImportWizardPage;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class BioSAXSSetupPerspective implements IPerspectiveFactory {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxssetupperspective";
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSSetupPerspective.class);
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
	// private static final int RECOUP_COL_NO = 10;
	private static final int RECOUP_PLATE_COL_NO = 10;
	private static final int RECOUP_ROW_COL_NO = 11;
	private static final int RECOUP_COLUMN_COL_NO = 12;
	private static final int TIME_PER_FRAME_COL_NO = 13;
	private static final int FRAMES_COL_NO = 14;
	private static final int EXPOSURE_TEMP_COL_NO = 15;
	private BSSCSessionBean sessionBean;
	private ArrayList<TitrationBean> measurements;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.73f,
				IPageLayout.ID_EDITOR_AREA);

		folderLayout.addView("uk.ac.gda.client.CommandQueueViewFactory");

		// File fileToOpen = new File("externalfile.xml");
		//
		// if (fileToOpen.exists() && fileToOpen.isFile()) {
		// IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
		// IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		//
		// try {
		// page.openEditor(new NullEditorInput(), "org.eclipse.ui.DefaultTextEdtior");
		// } catch (PartInitException e) {
		// e.printStackTrace();
		// }
		//
		// try {
		// IDE.openEditorOnFileStore( page, fileStore );
		// } catch ( PartInitException e ) {
		// //Put your exception handler here if you wish to
		// }
		// } else {
		// //Do something if the file does not exist
		// }
		// Need add listener to workbench to always have at least one editor available
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IPartService service = (IPartService) window.getService(IPartService.class);
		service.addPartListener(new IPartListener() {

			@Override
			public void partActivated(IWorkbenchPart part) {

			}

			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}

			@Override
			public void partClosed(IWorkbenchPart part) {
				if (part instanceof RichBeanMultiPageEditorPart) {
					RichBeanMultiPageEditorPart editor = (RichBeanMultiPageEditorPart) part;
					IWorkbenchPage page = part.getSite().getPage();
					IEditorInput editorInput = editor.getEditorInput();
				}
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}

			@Override
			public void partOpened(IWorkbenchPart part) {
				if (part instanceof RichBeanMultiPageEditorPart) {
					RichBeanMultiPageEditorPart editor = (RichBeanMultiPageEditorPart) part;
					IWorkbenchPage page = part.getSite().getPage();
					IEditorInput editorInput = editor.getEditorInput();
				}
			}
		});
	}

	public void openEditor()
	{
		Workbook wb;
		String bioSAXSFilePath = "Sample" + ".biosaxs";
		sessionBean = new BSSCSessionBean();
		measurements = new ArrayList<TitrationBean>();

		try {
			wb = WorkbookFactory.create(new File(bioSAXSFilePath));

			Sheet sheet = wb.getSheetAt(0);
			
			for (Row row : sheet) {
				TitrationBean tibi = new TitrationBean();

				LocationBean location = locationFromCells(row.getCell(PLATE_COL_NO), row.getCell(PLATE_ROW_COL_NO),
						row.getCell(PLATE_COLUMN_COL_NO));
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
			}
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}

		sessionBean.setMeasurements(measurements);

		File bioSAXSfile = new File(bioSAXSFilePath);

		try {
			XMLHelpers.writeToXML(BSSCSessionBean.mappingURL, sessionBean, bioSAXSfile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}

		new BSSCSessionBeanUIEditor(bioSAXSFilePath, BSSCSessionBean.mappingURL, new BSSCSessionBeanEditor(),
				sessionBean);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFileStore biosaxsFileStore = EFS.getLocalFileSystem().getStore(bioSAXSfile.toURI());
		try {
			IDE.openEditorOnFileStore(page, biosaxsFileStore);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
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
}
