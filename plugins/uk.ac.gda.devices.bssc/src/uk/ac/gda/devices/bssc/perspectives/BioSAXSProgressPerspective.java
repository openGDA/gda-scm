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

import org.eclipse.core.resources.IStorage;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.richbeans.xml.string.StringInput;
import uk.ac.gda.richbeans.xml.string.StringStorage;

public class BioSAXSProgressPerspective implements IPerspectiveFactory {

	private IWorkbenchPage page;

	@Override
	public void createInitialLayout(IPageLayout layout) {

		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.73f,
				IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("uk.ac.gda.client.CommandQueueViewFactory");
		// TODO Auto-generated method stub

		layout.addView("uk.ac.gda.video.views.cameraview", IPageLayout.LEFT, 0.25f, IPageLayout.ID_EDITOR_AREA);
		// layout.addView("uk.ac.gda.devices.bssc.3DPlotView", IPageLayout.TOP, 0.45f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.ncd.saxsview", IPageLayout.BOTTOM, 0.25f, "uk.ac.gda.video.views.cameraview");
		layout.addView("uk.ac.gda.devices.bssc.biosaxsprogressview", IPageLayout.RIGHT, 0.60f,
				IPageLayout.ID_EDITOR_AREA);


		// Need add listener to workbench to always have at least one editor available
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		page = window.getActivePage();
		// adding a listener
		IPartListener2 pl = new IPartListener2() {
			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef instanceof IEditorReference) {
					if (page.getEditorReferences().length == 0) {
						String string = "Test";
						IStorage storage = new StringStorage(string);
						IStorageEditorInput input = new StringInput(storage);
						IWorkbenchPage page = window.getActivePage();
						if (page != null)
							try {
								page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
							} catch (PartInitException e) {
								// TODO Auto-generated catch block
								// logger.error("TODO put description of error here", e);
							}
					}
				}
			}

			@Override
			public void partActivated(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {
				// TODO Auto-generated method stub

			}

		};
		page.addPartListener(pl);
	}

}
