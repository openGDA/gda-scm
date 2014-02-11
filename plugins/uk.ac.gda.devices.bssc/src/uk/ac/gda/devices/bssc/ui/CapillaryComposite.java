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

package uk.ac.gda.devices.bssc.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import uk.ac.gda.client.viewer.ImageViewer;
import uk.ac.gda.video.views.ICameraConfig;

public class CapillaryComposite extends Composite {
	private ImageViewer viewer;
	
	public CapillaryComposite(Composite parent, int style, ICameraConfig cameraConfig, Display display, CapillaryView capillaryView) {
		super(parent, style);
		setLayout(new FillLayout());
		viewer = new uk.ac.gda.client.viewer.ImageViewer(this, SWT.DOUBLE_BUFFERED);
		viewer.zoomFit();
	}
}
