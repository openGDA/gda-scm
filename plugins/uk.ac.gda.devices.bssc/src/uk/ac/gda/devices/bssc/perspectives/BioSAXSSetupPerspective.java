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

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanEditor;

public class BioSAXSSetupPerspective implements IPerspectiveFactory {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxssetupperspective";
	private IWorkbenchWindow window;
	private IWorkbenchPage page;
	protected String activePerspectiveID;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.73f,
				IPageLayout.ID_EDITOR_AREA);

		folderLayout.addView("uk.ac.gda.client.CommandQueueViewFactory");

		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		page = window.getActivePage();

		// Need add listener to workbench to always have at least one editor available
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
				if (activePerspectiveID.equals(ID)) {
					if (page.getEditorReferences().length == 0) {
						openDefaultEditor();
					}
				}
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}

			@Override
			public void partOpened(IWorkbenchPart part) {

			}
		});

		window.addPerspectiveListener(new PerspectiveAdapter() {
			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				activePerspectiveID = perspective.getId();
				
				if (perspective.getId().equals(ID)) {
					if (page.getEditorReferences().length == 0) {
						openDefaultEditor();
					}
				}
			}

			@Override
			public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
					IPerspectiveDescriptor newPerspective) {

			}

			@Override
			public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

			}
		});
	}

	private void openDefaultEditor() {
		BSSCSessionBeanEditor editor = new BSSCSessionBeanEditor();
		editor.openDefaultEditor();
	}
}
