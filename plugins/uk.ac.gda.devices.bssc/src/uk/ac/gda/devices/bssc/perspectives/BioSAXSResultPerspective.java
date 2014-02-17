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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public class BioSAXSResultPerspective implements IPerspectiveFactory {
	public static final String ID = "uk.ac.gda.devices.bssc.biosaxsresultperspective";
	private HashMap<String, ArrayList<IEditorReference>> perspectiveEditors = new HashMap<String, ArrayList<IEditorReference>>();
	private HashMap<String, IEditorReference> lastActiveEditors = new HashMap<String, IEditorReference>();
	private ArrayList<IEditorReference> editorRefs;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView("uk.ac.gda.devices.bssc.biosaxsprogressview", IPageLayout.RIGHT, 0.60f,
				IPageLayout.ID_EDITOR_AREA);
		layout.setEditorAreaVisible(false);
	}

}
