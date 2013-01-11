package uk.ac.gda.devices.bssc.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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

public class BSSCImportWizardPage extends WizardNewFileCreationPage {

	protected FileFieldEditor editor;

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

	@Override
	protected InputStream getInitialContents() {
		try {
			return new FileInputStream(new File(editor.getStringValue()));
		} catch (FileNotFoundException e) {
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