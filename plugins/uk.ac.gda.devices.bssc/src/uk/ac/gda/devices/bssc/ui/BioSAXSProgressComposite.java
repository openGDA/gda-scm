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

import java.sql.SQLException;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.jface.viewers.ObservableMapOwnerDrawProvider;
import uk.ac.gda.devices.bssc.BioSAXSSampleProgressCollection;
import uk.ac.gda.devices.bssc.BioSaxsSampleProgress;
import uk.ac.gda.devices.bssc.ISampleProgress;
import uk.ac.gda.devices.bssc.ISampleProgressCollection;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSDBFactory;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;
import uk.ac.gda.richbeans.components.FieldComposite;

public class BioSAXSProgressComposite extends FieldComposite {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressComposite.class);
	private TableViewer bioSaxsProgressViewer;
	private Table bioSaxsTable;
	private ISampleProgressCollection model;
	private BioSAXSISPyB bioSAXSISPyB;

	public BioSAXSProgressComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new FillLayout());

		bioSaxsProgressViewer = new TableViewer(this, SWT.NONE);
		bioSaxsTable = bioSaxsProgressViewer.getTable();

		bioSaxsTable.setHeaderVisible(true);
		bioSaxsTable.setLinesVisible(true);

		final TableViewerColumn viewerColumn1 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column1 = viewerColumn1.getColumn();
		column1.setWidth(120);
		column1.setResizable(true);
		column1.setText("Experiment ID");

		final TableViewerColumn viewerColumn2 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column2 = viewerColumn2.getColumn();
		column2.setWidth(120);
		column2.setResizable(true);
		column2.setText("Measurement");

		final TableViewerColumn viewerColumn3 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column3 = viewerColumn3.getColumn();
		column3.setWidth(80);
		column3.setResizable(true);
		column3.setText("Collection\nStatus");

		final TableViewerColumn viewerColumn4 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column4 = viewerColumn4.getColumn();
		column4.setWidth(80);
		column4.setResizable(true);
		column4.setText("Reduction\nStatus");

		final TableViewerColumn viewerColumn5 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column5 = viewerColumn5.getColumn();
		column5.setWidth(80);
		column5.setResizable(true);
		column5.setText("Analysis\nStatus");

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		bioSaxsProgressViewer.setContentProvider(contentProvider);

		// Create the label provider including monitoring of the changes of the
		// labels
		IObservableSet knownElements = contentProvider.getKnownElements();

		// final IObservableMap collectionProgress = BeanProperties.value(ISampleProgress.class,
		// ISampleProgress.COLLECTION_PROGRESS).observeDetail(knownElements);

		viewerColumn1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				BioSaxsSampleProgress progress = (BioSaxsSampleProgress) element;
				return progress.getExperimentId();
			}
		});

		viewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				BioSaxsSampleProgress progress = (BioSaxsSampleProgress) element;
				return progress.getSampleName();
			}
		});

		viewerColumn3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				BioSaxsSampleProgress progress = (BioSaxsSampleProgress) element;
				return progress.getCollectionStatus();
			}
		});

		// viewerColumn4.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// BioSaxsSampleProgress progress = (BioSaxsSampleProgress) element;
		// return progress.getReductionStatus();
		// }
		// });
		//
		// viewerColumn5.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// BioSaxsSampleProgress progress = (BioSaxsSampleProgress) element;
		// return progress.getAnalysisStatus();
		// }
		// });
		//
		// ControlFactoryAndUpdater factory = new ObservableMapCellControlProvider.ControlFactoryAndUpdater() {
		//
		// @Override
		// public Control createControl(Composite parent) {
		// ProgressBar progressBar = new ProgressBar(parent, SWT.NONE);
		// progressBar.setMaximum(100);
		// return progressBar;
		// }
		//
		// @Override
		// public void updateControl(Control control, Object value) {
		// ((ProgressBar) control).setSelection(((Double) value).intValue());
		//
		// }
		// };
		// viewerColumn3.setLabelProvider(new ObservableMapCellControlProvider(collectionProgress, factory, "Column3"));

		final IObservableMap reductionProgressValues = BeanProperties.value(ISampleProgress.class,
				ISampleProgress.REDUCTION_PROGRESS).observeDetail(knownElements);

		viewerColumn4.setLabelProvider(new ObservableMapOwnerDrawProvider(reductionProgressValues) {
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color original = null;

			@Override
			protected void measure(Event event, Object element) {
				event.setBounds(new Rectangle(event.x, event.y, 20, 10));
			}

			@Override
			protected void erase(Event event, Object element) {
				if (original != null) {
					event.gc.setBackground(original);
					event.gc.fillRectangle(event.getBounds());
				}
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				green = event.display.getSystemColor(SWT.COLOR_GREEN);
				event.gc.setBackground(green);

				Object value = attributeMaps[0].get(element);

				int columnWidth = viewerColumn3.getColumn().getWidth();
				int percentage = ((Double) value).intValue();
				int columnPercentage = (int) ((columnWidth * 0.01) * percentage);
				event.setBounds(new Rectangle(event.x, event.y, columnPercentage, (event.height - 1)));

				event.gc.fillRectangle(event.getBounds());
			}
		});

		final IObservableMap analysisProgressValues = BeanProperties.value(ISampleProgress.class,
				ISampleProgress.ANALYSIS_PROGRESS).observeDetail(knownElements);

		viewerColumn5.setLabelProvider(new ObservableMapOwnerDrawProvider(analysisProgressValues) {
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color original = null;

			@Override
			protected void measure(Event event, Object element) {
				event.setBounds(new Rectangle(event.x, event.y, 20, 10));
			}

			@Override
			protected void erase(Event event, Object element) {
				if (original != null) {
					event.gc.setBackground(original);
					event.gc.fillRectangle(event.getBounds());
				}
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				green = event.display.getSystemColor(SWT.COLOR_GREEN);
				event.gc.setBackground(green);

				Object value = attributeMaps[0].get(element);

				int columnWidth = viewerColumn4.getColumn().getWidth();
				int percentage = ((Double) value).intValue();
				int columnPercentage = (int) ((columnWidth * 0.01) * percentage);
				event.setBounds(new Rectangle(event.x, event.y, columnPercentage, (event.height - 1)));

				event.gc.fillRectangle(event.getBounds());
			}
		});

		BioSAXSSampleProgressCollection bioSAXSSampleProgressCollection = new BioSAXSSampleProgressCollection();
		model = bioSAXSSampleProgressCollection;

		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(ISampleProgressCollection.class);
		modelReg.setService(model);
		try {
			modelReg.afterPropertiesSet();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		model = (ISampleProgressCollection) GDAClientActivator.getNamedService(ISampleProgressCollection.class, null);

		loadModelFromISPyB();

		if (model != null) {
			IObservableList input = model.getItems();
			bioSaxsProgressViewer.setInput(input);
		}

		final Job pollingJob = new Job("Polling ISpyB") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					clearModel();
					loadModelFromISPyB();

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							bioSaxsProgressViewer.refresh();
						}
					});

					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					return Status.OK_STATUS;
				} finally {
					// start job again after specified time has elapsed
					schedule(90000);
				}
			}
		};
		// job.addJobChangeListener(new JobChangeAdapter() {
		// @Override
		// public void done(IJobChangeEvent event) {
		// if (event.getResult().isOK())
		// System.out.println("Job completed successfully");
		// else
		// System.out.println("Job did not complete successfully");
		// }
		// });
		pollingJob.schedule(); // start as soon as possible
	}

	private void clearModel() {
		model.clearItems();
	}

	private void loadModelFromISPyB() {
		new BioSAXSDBFactory().setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();

		try {
			model.addItems(bioSAXSISPyB.getBioSAXSSamples());
		} catch (SQLException e) {
			logger.error("SQL EXception getting samples from ISPyB", e.getMessage());
		}
	}

	public Viewer getViewer() {
		return bioSaxsProgressViewer;
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(Object value) {
		System.out.println();
	}
}
