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

package gda.rcp.ncd.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.observable.IObserver;
import gda.rcp.ncd.Activator;

public class ShutterGroup implements IObserver, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ShutterGroup.class);

	private static final Map<String, String> status2action = new HashMap<String, String>() {
		{
			put("Open", "Close");
			put("Closed", "Open");
			put("Close", "Open"); // this one is for testing with the demented dummy
			put("Fault", "Reset");
			put("Opening", "Wait");
			put("Closing", "Wait");
		}
	};

	private final EnumPositioner shutter;
	private final Button button;
	private final Label label;
	final Color red;
	final Color defaultColor;
	final Color green;

	private Group group;

	public ShutterGroup(Composite parent, int style, final EnumPositioner shutter) {

		group = new Group(parent, style);
		group.setText(shutter.getName());

		this.shutter = shutter;

		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.center = true;
		group.setLayout(layout);
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		label = new Label(group, SWT.NONE);
		label.setText("  Fault  ");
		label.setSize(100, 20);

		button = new Button(group, SWT.NONE);
		button.setText("  Wait  ");
		button.setToolTipText(String.format("Operate %s", shutter.getName()));
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// do what is says on the tin
				final String action = button.getText();

				if (action.equals("Wait")) {
					return;
				}

				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						try {
							shutter.asynchronousMoveTo(action);
						} catch (DeviceException de) {
							// Create the required Status object
							final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error Operating "
									+ shutter.getName(), de);

							Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getCurrent().getActiveShell(), "DeviceException", "Error Operating "
											+ shutter.getName(), status));
						}
					}
				};
				new Thread(runnable).start();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		green = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
		defaultColor = label.getBackground();
		shutter.addIObserver(this);
		update(null, null);
		group.addDisposeListener(e -> group.dispose());
	}
	@Override
	public void update(Object theObserved, Object changeCode) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// TODO I agree it is not too good requesting more information in the update
					// we should improve the changeCode
					final String status = shutter.getPosition().toString();
					final String nextaction = status2action.get(status);

					Display dis = Display.getDefault();
					dis.asyncExec(new Runnable() {
						@Override
						public void run() {
							if ("Close".equals(status)) {
								label.setText("Closed");
								group.setBackground(red);
							} else if ("FAULT".equals(status)) {
								label.setText("FAULT");
								group.setBackground(red);
							} else {
								//label.setBackground(green);
								label.setText(status);
								group.setBackground(defaultColor);
							}

							if (nextaction != null) {
								button.setText(nextaction);
							} else {
								// something fishy
								button.setText("Reset");
							}
						}
					});
				} catch (DeviceException e) {
					logger.warn("could not get status for " + shutter.getName() + ": ", e);
				}
			}
		}).start();
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			// update every so long, in case an EPICS update is lost
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for shutter update", e);
				Thread.currentThread().interrupt();
				break;
			}
			update(null, null);
		}
	}
}