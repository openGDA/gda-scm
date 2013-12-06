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
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.IWorkbenchPage;

public class BioSaxsSetupPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView("uk.ac.gda.devices.bssc.biosaxssetupview", false, IPageLayout.TOP, 0.60f,
				IPageLayout.ID_EDITOR_AREA);
		IViewLayout vLayout = layout.getViewLayout("uk.ac.gda.devices.bssc.biosaxsprogressview");
		vLayout.setCloseable(false);
		vLayout.setMoveable(false);

		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.73f,
				IPageLayout.ID_EDITOR_AREA);

		folderLayout.addView("uk.ac.gda.client.CommandQueueViewFactory");
		folderLayout.addView("gda.rcp.jythonterminalview");

		layout.setEditorAreaVisible(false);
	}

}
