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
import gda.rcp.util.OSGIServiceRegister;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;
import uk.ac.gda.devices.bssc.beans.BioSAXSProgressModel;
import uk.ac.gda.devices.bssc.beans.IProgressModel;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSDBFactory;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;

public class BioSAXSProgressView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressComposite.class);
	public static final String ID = "uk.ac.gda.devices.bssc.biosaxsprogressview";
	private BioSAXSProgressComposite bioSAXSComposite;
	private IObserver modelObserver;
	private IProgressModel model;
	private BioSAXSProgressController controller;

	@Override
	public void createPartControl(Composite parent) {
/*		IProgressModel model = new BioSAXSProgressModel();
		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(IProgressModel.class);
		modelReg.setService(model);
		
		try {
			modelReg.afterPropertiesSet();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		{BioSAXSProgressController controller = new BioSAXSProgressController(model);
		new BioSAXSDBFactory().setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		BioSAXSISPyB bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
		controller.setISpyBAPI(bioSAXSISPyB);
		controller.pollISpyB();
		}
*/
		
		model = (IProgressModel) GDAClientActivator.getNamedService(IProgressModel.class, null);
		
		controller = (BioSAXSProgressController) GDAClientActivator.getNamedService(BioSAXSProgressController.class, null);
		
		IObservableList input = model.getItems();
		bioSAXSComposite = new BioSAXSProgressComposite(parent, input, SWT.NONE);
		modelObserver = new IObserver() {
			
			@Override
			public void update(Object source, Object arg) {
				// TODO Auto-generated method stub
				
			}
		};
		model.addIObserver(modelObserver);
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
		if( model != null && modelObserver != null){
			model.deleteIObserver(modelObserver);
			modelObserver=null;
			model = null;
			controller.disconnectFromISpyB();
		}
		super.dispose();
	}
	
	
}
