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

import gda.commandqueue.Queue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;

public class BioSAXSSessionBeanComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(BSSCSessionBeanComposite.class);

	private NumberBox sampleStorageTemperature;
	private BioSAXSMeasurementsFieldComposite measurements;
	private GridData layoutData;
	
	public BioSAXSSessionBeanComposite(Composite parent, int style, IViewPart bioSAXASessionBeanUIView) {
		super(parent, style);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth=5;
		setLayout(layout);
		
		Composite leftComposite = new Composite(this, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginWidth=0;
		rowLayout.marginTop=0;
		rowLayout.marginBottom=0;
		rowLayout.marginLeft=0;
		rowLayout.marginRight=0;
		rowLayout.spacing=5;
		leftComposite.setLayout(rowLayout);
		
		Composite rightComposite = new Composite(this, SWT.NONE);
		GridData rightCompositeGD = new GridData();
		rightCompositeGD.horizontalAlignment = SWT.FILL;
		rightCompositeGD.grabExcessHorizontalSpace = true;
		rightComposite.setLayoutData(rightCompositeGD);

		GridLayout rightCompositeGL = new GridLayout();
		rightCompositeGL.numColumns = 4;
		rightComposite.setLayout(rightCompositeGL);
		
		Button btnNewMeasurement = new Button(leftComposite, SWT.NONE);
		btnNewMeasurement.setText("Add Measurement");
		btnNewMeasurement.setToolTipText("Add a measurement to the table");
		btnNewMeasurement.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				// TODO
				if (measurements != null)
					measurements.addSample();		
			}
		});
		
		Button btnDeleteMeasurement = new Button(leftComposite, SWT.NONE);
		btnDeleteMeasurement.setText("Delete Measurement");
		btnDeleteMeasurement.setToolTipText("Delete the selected measurement from the table");
		btnDeleteMeasurement.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				if (measurements != null)
					measurements.deleteSelection();			
			}
		});
		
		Button btnloadExp = new Button(rightComposite, SWT.NONE);
		btnloadExp.setText("Load Experiment");
		GridData btnLoadExpGD = new GridData();
		btnLoadExpGD.horizontalAlignment = SWT.RIGHT;
		btnLoadExpGD.grabExcessHorizontalSpace = true;
		btnloadExp.setLayoutData(btnLoadExpGD);
		btnloadExp.setToolTipText("Load an experiment from the file system");
		btnloadExp.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
			}
		});
		
		Button btnSaveExp = new Button(rightComposite, SWT.NONE);
		btnSaveExp.setText("Save Experiment");
		GridData btnSaveExpGD = new GridData();
		btnSaveExpGD.horizontalAlignment = SWT.RIGHT;
		btnSaveExpGD.grabExcessHorizontalSpace = true;
		btnSaveExp.setToolTipText("Save the experiment to the file system");
		btnSaveExp.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
			}
		});
		
		Button btnQueueExp = new Button(rightComposite, SWT.NONE);
		btnQueueExp.setText("Queue Experiment");
		GridData btnQueueExpGD = new GridData();
		btnQueueExpGD.horizontalAlignment = SWT.RIGHT;
		btnQueueExpGD.grabExcessHorizontalSpace = true;
		btnQueueExp.setToolTipText("Save file and queue for execution (will start immediately if queue running");
		btnQueueExp.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {
					IProgressMonitor monitor = new NullProgressMonitor();
//					editor.doSave(monitor);
					if (monitor.isCanceled())
						return;
					Queue queue = CommandQueueViewFactory.getQueue();
					if (queue != null) {
//						queue.addToTail(new JythonCommandCommandProvider(String.format("import BSSC; BSSC.BSSCRun(\"%s\").run()", editor.getPath()), editor.getTitle(), editor.getPath()));
					} else {
						logger.warn("No queue received from CommandQueueViewFactory");
					}
				} catch (Exception e1) {
					logger.error("Error adding command to the queue", e1);
				}
			}
		});
		
		Button btnEditQueuedExp = new Button(rightComposite, SWT.NONE);
		btnEditQueuedExp.setText("Edit Queued Experiment");
		GridData btnEditQueuedExpGD = new GridData();
		btnEditQueuedExpGD.horizontalAlignment = SWT.RIGHT;
		btnEditQueuedExpGD.grabExcessHorizontalSpace = true;
		btnEditQueuedExp.setToolTipText("Change the experiment that is currently in the queue");
		btnEditQueuedExp.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
			}
		});
		
		measurements = new BioSAXSMeasurementsFieldComposite(this, SWT.NONE, bioSAXASessionBeanUIView);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan=2;
		measurements.setLayoutData(layoutData);
		
		sampleStorageTemperature = new ScaleBox(this, SWT.NONE);
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
		layoutData.horizontalSpan=5;
		sampleStorageTemperature.setLayoutData(layoutData);
		sampleStorageTemperature.setUnit("\u00B0C");
		sampleStorageTemperature.setLabel("Sample Storage Temperature");
		sampleStorageTemperature.setLabelWidth(230);
		sampleStorageTemperature.setMinimum(-100);
		sampleStorageTemperature.setMaximum(100);
		sampleStorageTemperature.setDecimalPlaces(1);
		new Label(sampleStorageTemperature, SWT.NONE);
	}

}
