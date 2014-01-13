package uk.ac.gda.devices.bssc.wizards;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;

public class BSSCImportWizardPage extends WizardNewFileCreationPage {

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
	private static final int RECOUP_COL_NO = 10;
	private static final int RECOUP_PLATE_COL_NO = 11;
	private static final int RECOUP_ROW_COL_NO = 12;
	private static final int RECOUP_COLUMN_COL_NO = 13;
	private static final int TIME_PER_FRAME_COL_NO = 14;
	private static final int FRAMES_COL_NO = 15;
	private static final int EXPOSURE_TEMP_COL_NO = 16;

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
				String newFileName = path.removeFileExtension().addFileExtension("xml").lastSegment();
				setFileName(newFileName);
			}
		});
		String[] extensions = new String[] { "*.xls;*.xlsx" }; // NON-NLS-1
		editor.setFileExtensions(extensions);
		fileSelectionArea.moveAbove(null);
	}

	@Override
	protected void createLinkTarget() {
	}
	
	private short parsePlateCell(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return (short) cell.getNumericCellValue();
		} 
		short result = 0;
		String str = cell.getStringCellValue();
		for (int i = 0; i < str.length(); i++) {
			if ("I".equalsIgnoreCase(str.substring(i, i+1))) 
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
	protected InputStream getInitialContents() {
		try {
			Workbook wb = WorkbookFactory.create(new File(editor.getStringValue()));
			Sheet sheet = wb.getSheetAt(0);

			BSSCSessionBean sessionBean = new BSSCSessionBean();
			sessionBean.setSampleStorageTemperature(sampleStorageTemperature);
			List<TitrationBean> measurements = new ArrayList<TitrationBean>();

			for (Row row : sheet) {

				try {
					TitrationBean tibi = new TitrationBean();
	
					LocationBean location = locationFromCells(row.getCell(PLATE_COL_NO), row.getCell(PLATE_ROW_COL_NO), row.getCell(PLATE_COLUMN_COL_NO));
					if (!location.isValid())
						throw new Exception("invalid sample location");
					tibi.setLocation(location);
				
					tibi.setSampleName(row.getCell(SAMPLE_NAME_COL_NO).getStringCellValue());
	
					location = locationFromCells(row.getCell(BUFFER_PLATE_COL_NO), row.getCell(BUFFER_ROW_COL_NO), row.getCell(BUFFER_COLUMN_COL_NO));
					if (!location.isValid())
						throw new Exception("invalid buffer location");
					tibi.setBufferLocation(location);
	
					try {
						location = locationFromCells(row.getCell(RECOUP_PLATE_COL_NO), row.getCell(RECOUP_ROW_COL_NO), row.getCell(RECOUP_COLUMN_COL_NO));
						if (!location.isValid())
							location = null;
					} catch (Exception e) {
						location = null;
					}
					tibi.setRecouperateLocation(location);
					tibi.setConcentration(row.getCell(CONCENTRATION_COL_NO).getNumericCellValue()); 
					tibi.setViscosity(row.getCell(VISCOSITY_COL_NO).getStringCellValue());
					tibi.setMolecularWeight(row.getCell(MOLECULAR_WEIGHT_COL_NO).getNumericCellValue());
					tibi.setTimePerFrame(row.getCell(TIME_PER_FRAME_COL_NO).getNumericCellValue());
					tibi.setFrames((int) row.getCell(FRAMES_COL_NO).getNumericCellValue()); 
					tibi.setExposureTemperature((float) row.getCell(EXPOSURE_TEMP_COL_NO).getNumericCellValue()); 
	
					measurements.add(tibi);
				} catch (Exception e) {
					logger.debug("row rejected"+row.toString());
				}
			}

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