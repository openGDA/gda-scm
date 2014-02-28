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

package uk.ac.gda.devices.bssc.views;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BioSAXSReductionResultPlotView extends ViewPart {
	public static final String ID = "uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView";
	private IPlottingSystem plotting;
	private Logger logger = LoggerFactory.getLogger(BioSAXSProgressPlotView.class);

	public BioSAXSReductionResultPlotView() {
		try {
			this.plotting = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Cannot create a plotting system!", e);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		plotting.createPlotPart(parent, "My Plot Name", getViewSite().getActionBars(), PlotType.IMAGE, this);
	}

	@Override
	public void setFocus() {
		plotting.setFocus();
	}

	@Override
	public Object getAdapter(final Class clazz) {
		if (IPlottingSystem.class == clazz)
			return plotting;
		if (IToolPageSystem.class == clazz)
			return plotting;
		return super.getAdapter(clazz);
	}
}