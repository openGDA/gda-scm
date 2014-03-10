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
	}

}
