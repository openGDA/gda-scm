/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury & Rutherford Laboratory
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
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

public class LpdButtonView extends ViewPart implements IObserver {
	public static final String ID = "views.LpdButtonView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(LpdButtonView.class);
    private Button startButton;
    private Button stopButton;
	private Button outputButton;
	private Detector lpdDetector;
	private PlotView plotView;
	private String plotPanelName;
	
    public LpdButtonView() {
        super();
    }

    @Override
	public void createPartControl(Composite parent) {
    	
		lpdDetector = (Detector) Finder.getInstance().find("lpdDetector");
		lpdDetector.addIObserver(this);
		// check if Plot View is open
		try {
			plotView = (PlotView) getSite().getPage().showView("uk.ac.rl.gda.lpd.imageview");
			plotPanelName = plotView.getPlotViewName();
			
		} catch (PartInitException e) {
			logger.error("All over now! Cannot find plotview: " + plotPanelName, e);
		}

		FormLayout parentLayout = new FormLayout();
		parent.setLayout(parentLayout);

		startButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(0, 15);
			formData.left = new FormAttachment(0, 20);
			startButton.setLayoutData(formData);
		}
		startButton.setText("Start");
		startButton.setToolTipText("Start data collection");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
					start();
			}
		});

		stopButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(startButton, 0, SWT.TOP);
			formData.left = new FormAttachment(startButton, 15);
			stopButton.setLayoutData(formData);
		}
		stopButton.setText("stop");
		stopButton.setToolTipText("stop acquisition");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				stop();
			}
		});

		outputButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(stopButton, 0, SWT.TOP);
			formData.left = new FormAttachment(stopButton, 15);
			outputButton.setLayoutData(formData);
		}
		outputButton.setText("Output");
		outputButton.setToolTipText("Output data to file");
		outputButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				output();
			}
		});

    }

    @Override
	public void setFocus() {
    }
    
    @Override
	public void dispose() {
        super.dispose();
    }
    
	private void start() {
		try {
			lpdDetector.collectData();
		} catch (DeviceException de) {
			logger.error("DeviceException starting data collection ", de); //$NON-NLS-1$
		} catch (Exception e) {
			logger.error("Error creating byte dataset", e); //$NON-NLS-1$
		}
	}
	private void stop() {
		try {
			lpdDetector.stop();
		} catch (DeviceException de) {
			logger.error("DeviceException stopping data collection ", de); //$NON-NLS-1$
		}
	}
	
	private void output() {
		JythonServerFacade.getInstance().runCommand("gda.scan.StaticScanNoCollection([lpdDetector]).runScan()");		
	}

	@Override
	public void update(Object source, Object arg) {
		IntegerDataset plotDataset = (IntegerDataset) arg;
		try {
			SDAPlotter.imagePlot(plotPanelName, plotDataset);
		} catch (Exception e) {
			logger.error("FemButton View update exception", e);
		}
	}

}
