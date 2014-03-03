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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.devices.bssc.ui.BSSCSessionBeanEditor;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public class BSSCPerspective implements IPerspectiveFactory {
	public static String ID = "uk.ac.gda.devices.bssc.perspective";
	private HashMap<String, ArrayList<IEditorReference>> perspectiveEditors = new HashMap<String, ArrayList<IEditorReference>>();
	private HashMap<String, IEditorReference> lastActiveEditors = new HashMap<String, IEditorReference>();
	private ArrayList<IEditorReference> editorRefs;
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView("gda.rcp.ncd.views.NCDStatus", IPageLayout.RIGHT, 0.7f, IPageLayout.ID_EDITOR_AREA);
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.73f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("gda.rcp.jythonterminalview");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
		}
		// TODO Auto-generated method stub
		
		layout.addView("org.eclipse.ui.navigator.ProjectExplorer", IPageLayout.LEFT, 0.31f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.devices.bssc.views.CapillaryView", IPageLayout.TOP, 0.45f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.CommandQueueViewFactory", IPageLayout.TOP, 0.22f, "gda.rcp.ncd.views.NCDStatus");
		layout.addView("uk.ac.gda.devices.bssc.ui.BSSCStatus", IPageLayout.BOTTOM, 0.46f, "gda.rcp.ncd.views.NCDStatus");
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
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
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}

			@Override
			public void partOpened(IWorkbenchPart part) {
				if (part instanceof RichBeanMultiPageEditorPart) {
					RichBeanMultiPageEditorPart editor = (RichBeanMultiPageEditorPart)part;
					IWorkbenchPage page = part.getSite().getPage();
					IEditorInput editorInput = editor.getEditorInput();
					IPerspectiveDescriptor activePerspective = page.getPerspective();
					
					ArrayList<IEditorReference> editors = perspectiveEditors.get(activePerspective.getId());
					if (editors == null)
						editors = new ArrayList<IEditorReference>();

					// Find the editor reference that relates to this editor input
					IEditorReference[] editorRefs = page.findEditors(editorInput, null, IWorkbenchPage.MATCH_INPUT);
					
					if (editorRefs.length > 0) {
						editors.add(editorRefs[0]);
						perspectiveEditors.put(activePerspective.getId(), editors);
					}
				}
			}
		});

		window.addPerspectiveListener(new PerspectiveAdapter() {
			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				if (perspective.getId().equals(ID)) {
					// Hide all the editors
					IEditorReference[] editors = page.getEditorReferences();
					for (int i = 0; i < editors.length; i++) {
						page.hideEditor(editors[i]);
					}

					// Show the editors associated with this perspective
					editorRefs = perspectiveEditors.get(ID);
					if (editorRefs != null) {
						for (Iterator<IEditorReference> it = editorRefs.iterator(); it.hasNext();) {
							IEditorReference editorInput = it.next();
							page.showEditor(editorInput);
						}

						// Send the last active editor to the top
						IEditorReference lastActiveRef = lastActiveEditors.get(ID);
						page.bringToTop(lastActiveRef.getPart(true));
					}
				}
			}

			@Override
			public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
					IPerspectiveDescriptor newPerspective) {

			}

			@Override
			public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				IEditorPart activeEditor = page.getActiveEditor();
				if (activeEditor != null) {

					// Find the editor reference that relates to this editor input
					IEditorReference[] editorRefs = page.findEditors(activeEditor.getEditorInput(), null,
							IWorkbenchPage.MATCH_INPUT);
					if (editorRefs.length > 0) {
						lastActiveEditors.put(ID, editorRefs[0]);
					}
				}
			}
		});
	}

}
