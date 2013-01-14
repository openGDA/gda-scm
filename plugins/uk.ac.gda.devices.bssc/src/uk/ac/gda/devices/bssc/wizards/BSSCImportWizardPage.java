package uk.ac.gda.devices.bssc.wizards;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;

public class BSSCImportWizardPage extends WizardNewFileCreationPage {

	protected FileFieldEditor editor;
	private float sampleStorageTemperature = 15;

	public BSSCImportWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
		setTitle(pageName); // NON-NLS-1
		setDescription("Import a spreadsheet from the local file system into the workspace"); // NON-NLS-1
	}

	@Override
	protected void createAdvancedControls(Composite parent) {
		Composite fileSelectionArea = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		fileSelectionArea.setLayoutData(fileSelectionData);

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		fileSelectionArea.setLayout(fileSelectionLayout);

		editor = new FileFieldEditor("fileSelect", "Select File: ", fileSelectionArea); // NON-NLS-1 //NON-NLS-2
		editor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				IPath path = new Path(BSSCImportWizardPage.this.editor.getStringValue());
				setFileName(path.lastSegment());
			}
		});
		String[] extensions = new String[] { "*.xls;*.xlsx" }; // NON-NLS-1
		editor.setFileExtensions(extensions);
		fileSelectionArea.moveAbove(null);
	}

	@Override
	protected void createLinkTarget() {
	}
	
	private LocationBean locationFromCells(Cell platec, Cell rowc, Cell columnc) {
		LocationBean location = new LocationBean();
		location.setPlate(Short.parseShort(platec.getStringCellValue()));
		location.setRow(rowc.getStringCellValue().charAt(0));
		location.setColumn((short) columnc.getNumericCellValue());
		return location;
	}

	@Override
	protected InputStream getInitialContents() {
		try {
			InputStream excelDocumentStream = new FileInputStream(new File(editor.getStringValue()));
			POIFSFileSystem fsPOI = new POIFSFileSystem(new BufferedInputStream(excelDocumentStream));
			HSSFWorkbook workBook = new HSSFWorkbook(fsPOI);
			HSSFSheet sheet = workBook.getSheetAt(0);

			BSSCSessionBean sessionBean = new BSSCSessionBean();
			sessionBean.setSampleStorageTemperature(sampleStorageTemperature);
			List<TitrationBean> measurements = new ArrayList<TitrationBean>();

			for (Row row : sheet) {

				TitrationBean tibi = new TitrationBean();

				LocationBean location = locationFromCells(row.getCell(0), row.getCell(1), row.getCell(2));
				tibi.setLocation(location);
			
				tibi.setSampleName(row.getCell(3).getStringCellValue());

				location = locationFromCells(row.getCell(4), row.getCell(5), row.getCell(6));
				tibi.setBufferLocation(location);

//				tibi.setConcentration(concentration); 
//				tibi.setExposureTemperature(exposureTemperature); 
//				tibi.setFrames(frames); 
//				tibi.setRecouperate(recouperate);
//				tibi.setTimePerFrame(timePerFrame);
//				tibi.setViscosity(viscosity);

				measurements.add(tibi);
			}
			excelDocumentStream.close();

			sessionBean.setMeasurements(measurements);

			return BSSCWizardUtils.sessionBeanToStream(sessionBean);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected String getNewFileLabel() {
		return "New File Name:"; // NON-NLS-1
	}

	@Override
	protected IStatus validateLinkedResource() {
		return new Status(IStatus.OK, "uk.ac.gda.devices.bssc", IStatus.OK, "", null); // NON-NLS-1 //NON-NLS-2
	}
}