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

import uk.ac.gda.devices.bssc.views.BioSAXSAnalysisResultPlotView;
import uk.ac.gda.devices.bssc.views.BioSAXSCollectionResultPlotView;
import uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView;

public class BioSAXSResultPerspective implements IPerspectiveFactory {
	public static final String ID = "uk.ac.gda.devices.bssc.biosaxsresultperspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.LEFT, 0.73f,
				IPageLayout.ID_EDITOR_AREA);
		
		layout.addView("uk.ac.gda.devices.bssc.biosaxsprogressview", IPageLayout.RIGHT, 0.60f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.CommandQueueViewFactory", IPageLayout.BOTTOM, 0.75f, "uk.ac.gda.devices.bssc.biosaxsprogressview");
		
		folderLayout.addView(BioSAXSCollectionResultPlotView.ID);
		folderLayout.addView(BioSAXSReductionResultPlotView.ID);
		folderLayout.addView(BioSAXSAnalysisResultPlotView.ID);
		
		layout.setEditorAreaVisible(false);
	}

}
