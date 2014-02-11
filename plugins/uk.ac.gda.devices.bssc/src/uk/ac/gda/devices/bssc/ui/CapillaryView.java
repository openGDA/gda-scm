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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.client.viewer.ImageViewer;
import uk.ac.gda.devices.bssc.Activator;
import uk.ac.gda.video.views.ICameraConfig;
import uk.ac.gda.video.views.NewImageListener;

public class CapillaryView extends ViewPart implements NewImageListener {

	private Composite capillaryComposite;
	private ImageViewer viewer;
	private ICameraConfig cameraConfig;

	public CapillaryView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		capillaryComposite = new CapillaryComposite(parent, SWT.NONE, cameraConfig, parent.getDisplay(), this);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handlerNewImageNotification() {
		// TODO Auto-generated method stub
		
	}

}
