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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.ac.gda.devices.bssc.Activator;

public class BioSAXSAnalysisResultPlotView extends ViewPart {

	public static final String ID = "uk.ac.gda.devices.bssc.views.BioSAXSAnalysisResultPlotView";

	public BioSAXSAnalysisResultPlotView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		AbstractUIPlugin plugin = Activator.getDefault();
		ImageRegistry imageRegistry = plugin.getImageRegistry();
		Image image = imageRegistry.get(Activator.IMAGE_ID);
		Label label = new Label(composite, SWT.BORDER);
		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		label.setImage(image);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
