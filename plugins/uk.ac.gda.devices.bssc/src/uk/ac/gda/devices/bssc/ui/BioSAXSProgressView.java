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

import gda.rcp.GDAClientActivator;
import gda.rcp.util.OSGIServiceRegister;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BioSAXSSampleProgressCollection;
import uk.ac.gda.devices.bssc.beans.ISampleProgressCollection;

public class BioSAXSProgressView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressComposite.class);
	public static final String ID = "uk.ac.gda.devices.bssc.biosaxsprogressview";
	private BioSAXSProgressComposite bioSAXSComposite;
	private IObservableList input;

	public BioSAXSProgressView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		ISampleProgressCollection model = new BioSAXSSampleProgressCollection();

		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(ISampleProgressCollection.class);
		modelReg.setService(model);
		try {
			modelReg.afterPropertiesSet();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		model = (ISampleProgressCollection) GDAClientActivator.getNamedService(ISampleProgressCollection.class, null);
		input = model.getItems();
		bioSAXSComposite = new BioSAXSProgressComposite(parent, input, SWT.NONE);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public Viewer getViewer() {
		return bioSAXSComposite.getViewer();
	}
}
