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

package views;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.Finder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LpdConfigureView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(LpdConfigureView.class);

    private Button setButton;
	private Text collectTime;
    private Button voltageButton;
	private Text voltageText;
	private Detector lpdDetector;

	@Override
	public void createPartControl(Composite parent) {
		
		lpdDetector = (Detector) Finder.getInstance().find("lpdDetector");

		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		parent.setLayoutData(gridData);
		parent.setLayout(gridLayout);
		{
			Group group = new Group(parent, SWT.NONE);
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
			group.setLayoutData(gd);
			group.setLayout(new FillLayout(SWT.HORIZONTAL));
			{
				setButton = new Button(parent, SWT.PUSH | SWT.CENTER);
				setButton.setText("Set Collection Time");
				setButton.setToolTipText("Set");
				setButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent evt) {
						setCollectionTime();
					}
				});

				collectTime = new Text(group, SWT.NONE);
				double time = 0;
				try {
					time = lpdDetector.getCollectionTime();
				} catch (DeviceException e) {
					logger.error("Error getting collection time", e);
				}
				collectTime.setText("" + time);
				collectTime.setEditable(true);
			}

		}
		{
			Group group = new Group(parent, SWT.NONE);
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
			group.setLayoutData(gd);
			group.setLayout(new FillLayout(SWT.HORIZONTAL));
			{
				setButton = new Button(parent, SWT.PUSH | SWT.CENTER);
				setButton.setText("set voltage");
				setButton.setToolTipText("set DAC voltage");
				setButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent evt) {
						try {
							lpdDetector.setAttribute("DACvoltage", Double.parseDouble(voltageText.getText()));
						} catch (NumberFormatException e) {
							logger.error("TODO put description of error here", e);
						} catch (DeviceException e) {
							logger.error("TODO put description of error here", e);
						}
						// set voltage's attribute here
					}
				});

				voltageText = new Text(group, SWT.NONE);
				double voltage = 0;
				try {
					voltage = (Double) lpdDetector.getAttribute("DACVoltage");
				} catch (DeviceException e) {
					logger.error("Error getting voltage", e);
				}
				voltageText.setText("" + voltage);
				voltageText.setEditable(true);
			}
		}
	}

	@Override
	public void setFocus() {
	}

	private void setCollectionTime() {
		try {
			lpdDetector.setCollectionTime(Double.parseDouble(collectTime.getText()));
		} catch (NumberFormatException e) {
			logger.error("Error setting collection time number format", e);
		} catch (DeviceException e) {
			logger.error("Error setting collection time", e);
		}
	}
}
