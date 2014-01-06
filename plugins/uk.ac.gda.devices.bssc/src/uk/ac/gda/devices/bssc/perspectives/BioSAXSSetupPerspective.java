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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.devices.bssc.ui.BioSAXSProgressView;
import uk.ac.gda.richbeans.xml.string.StringInput;
import uk.ac.gda.richbeans.xml.string.StringStorage;

public class BioSAXSSetupPerspective implements IPerspectiveFactory {

	private IWorkbenchPage page;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.73f,
				IPageLayout.ID_EDITOR_AREA);

		folderLayout.addView("uk.ac.gda.client.CommandQueueViewFactory");
	}
}
