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

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class BSSCPerspective implements IPerspectiveFactory {

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
		layout.addView("uk.ac.gda.video.views.cameraview", IPageLayout.TOP, 0.45f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.CommandQueueViewFactory", IPageLayout.TOP, 0.27f, "gda.rcp.ncd.views.NCDStatus");
		layout.addView("uk.ac.gda.devices.bssc.ui.BSSCStatus", IPageLayout.BOTTOM, 0.51f, "gda.rcp.ncd.views.NCDStatus");
	}

}
