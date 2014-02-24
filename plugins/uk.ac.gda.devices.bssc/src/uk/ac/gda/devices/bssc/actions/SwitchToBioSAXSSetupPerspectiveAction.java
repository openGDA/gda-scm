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
	private static final int SAMPLE_COLLECTIONS_SIZE = 1;
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

			// open sample template in setup perspective when perspective is opened first time from the welcome page
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
		String bioSAXSFilePath = "Samples" + ".biosaxs";
		sessionBean = new BSSCSessionBean();
		measurements = new ArrayList<TitrationBean>();

		try {
			for (int i = 0; i < SAMPLE_COLLECTIONS_SIZE; i++) {
				TitrationBean tibi = new TitrationBean();

				LocationBean location = new LocationBean();
				if (!location.isValid())
					throw new Exception("invalid sample location");
				tibi.setLocation(location);

				tibi.setSampleName("Sample " + i);

				location = new LocationBean();
				if (!location.isValid())
					throw new Exception("invalid buffer location");
				tibi.setBufferLocation(location);

				location = new LocationBean();
				if (!location.isValid())
					location = null;

				tibi.setRecouperateLocation(null);
				tibi.setConcentration(1);
				tibi.setViscosity("high");
				tibi.setMolecularWeight(1);
				tibi.setTimePerFrame(1);
				tibi.setFrames(1);
				tibi.setExposureTemperature(22);

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
			logger.error("PartInitException opening editor", e);
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