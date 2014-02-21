package uk.ac.gda.devices.bssc.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.devices.bssc.perspectives.BioSAXSSetupPerspective;
import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanEditor;
import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanUIEditor;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class SwitchToBioSAXSSetupPerspectiveAction implements IIntroAction {
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

	public SwitchToBioSAXSSetupPerspectiveAction() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

		// close all other perspectives that might be open
		window.getActivePage().closeAllPerspectives(true, true);

		// open the BioSAXS setup perspective
		try {
			workbench.showPerspective("uk.ac.gda.devices.bssc.biosaxsresultperspective", window);
			workbench.showPerspective("uk.ac.gda.devices.bssc.biosaxsprogressperspective", window);
			workbench.showPerspective("uk.ac.gda.devices.bssc.biosaxssetupperspective", window);

			IPerspectiveRegistry iPerspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
			iPerspectiveRegistry.setDefaultPerspective("uk.ac.gda.devices.bssc.biosaxssetupperspective");

			//open sample template in setup perspective when perspective is opened first time from the welcome page
			openEditor();
		} catch (WorkbenchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run(IIntroSite site, Properties params) {
		final IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
	}

	private void openEditor() {
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
			logger.error("InvalidFormatException reading Workbook ", e);
		} catch (IOException e) {
			logger.error("IOException reading Workbook ", e);
		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		sessionBean.setMeasurements(measurements);

		File bioSAXSfile = new File(bioSAXSFilePath);

		try {
			XMLHelpers.writeToXML(BSSCSessionBean.mappingURL, sessionBean, bioSAXSfile);
		} catch (Exception e) {
			logger.error("Exception writing bean to XML", e);
		}

		new BSSCSessionBeanUIEditor(bioSAXSFilePath, BSSCSessionBean.mappingURL, new BSSCSessionBeanEditor(),
				sessionBean);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFileStore biosaxsFileStore = EFS.getLocalFileSystem().getStore(bioSAXSfile.toURI());
		try {
			IDE.openEditorOnFileStore(page, biosaxsFileStore);
		} catch (PartInitException e) {
			logger.error("PartyInitException opening editor", e);
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