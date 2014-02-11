/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import gda.factory.FactoryException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.video.views.BasicCameraComposite;

public class CapillaryView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(CapillaryView.class);

	public static final String ID = "uk.ac.gda.devices.bssc.views.CapillaryView"; //$NON-NLS-1$

	private BasicCameraComposite bcc;

	public CapillaryView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite com = new Composite(parent, SWT.FILL);
		com.setLayout(new GridLayout());

		bcc = new BasicCameraComposite(com, SWT.DOUBLE_BUFFERED);
		GridData gd_bcc = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_bcc.exclude = true;
		bcc.setLayoutData(gd_bcc);
		try {
			bcc.playURL("http://bl21b-di-serv-01.diamond.ac.uk:8082/BSAXS.CAM.MJPG.mjpg");
		} catch (FactoryException e) {
			logger.error("cannot configure mjpeg stream", e);
		}

		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				bcc.zoomFit();
			}
		});
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
}