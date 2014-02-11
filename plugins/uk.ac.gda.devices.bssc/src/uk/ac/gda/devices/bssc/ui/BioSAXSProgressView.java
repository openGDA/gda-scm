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

import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;

public class BioSAXSProgressView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressComposite.class);
	public static final String ID = "uk.ac.gda.devices.bssc.biosaxsprogressview";
	private BioSAXSProgressComposite bioSAXSComposite;
	private IListChangeListener listChangedListener;
	private IObservableList model;
	private BioSAXSProgressController controller;
	private IObserver controllerObserver;

	@Override
	public void createPartControl(Composite parent) {

		controller = (BioSAXSProgressController) GDAClientActivator.getNamedService(BioSAXSProgressController.class,
				null);
		model = new WritableList();
		controller.setModel(model);

		bioSAXSComposite = new BioSAXSProgressComposite(parent, model, SWT.NONE);
		listChangedListener = new IListChangeListener() {

			@Override
			public void handleListChange(ListChangeEvent event) {
				// TODO Auto-generated method stub

			}
		};
		controllerObserver = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				// TODO Auto-generated method stub

			}
		};
		model.addListChangeListener(listChangedListener);
		controller.addIObserver(controllerObserver);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public Viewer getViewer() {
		return bioSAXSComposite.getViewer();
	}

	@Override
	public void dispose() {
		if (model != null && controllerObserver != null) {
			model.removeListChangeListener(listChangedListener);
			controller.deleteIObserver(controllerObserver);
			controllerObserver = null;
			model = null;
		}

		if (controller != null) {
			controller.disconnectFromISpyB();
		}

		super.dispose();
	}

}
