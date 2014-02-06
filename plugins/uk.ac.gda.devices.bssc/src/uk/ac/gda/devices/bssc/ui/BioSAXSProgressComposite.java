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

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.jface.viewers.ObservableMapCellControlProvider;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapCellControlProvider.ControlFactoryAndUpdater;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapColumnLabelProvider;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapOwnerDrawProvider;
import uk.ac.gda.devices.bssc.ispyb.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatus;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.xml.string.StringInput;
import uk.ac.gda.richbeans.xml.string.StringStorage;

public class BioSAXSProgressComposite extends FieldComposite {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressComposite.class);

	private TableViewer bioSaxsProgressViewer;
	private Table bioSaxsTable;

	public BioSAXSProgressComposite(Composite parent, IObservableList input, int style) {
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
		column1.setText("Sample");

		final TableViewerColumn viewerColumn2 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column2 = viewerColumn2.getColumn();
		column2.setWidth(80);
		column2.setResizable(true);
		column2.setText("Collection\nStatus");

		final TableViewerColumn viewerColumn3 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column3 = viewerColumn3.getColumn();
		column3.setWidth(80);
		column3.setResizable(true);
		column3.setText("Reduction\nStatus");

		final TableViewerColumn viewerColumn4 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column4 = viewerColumn4.getColumn();
		column4.setWidth(80);
		column4.setResizable(true);
		column4.setText("Analysis\nStatus");

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		bioSaxsProgressViewer.setContentProvider(contentProvider);

		// Create the label provider including monitoring of the changes of the
		// labels
		IObservableSet knownElements = contentProvider.getKnownElements();

		Table table = bioSaxsProgressViewer.getTable();

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem tableItem = (TableItem) e.item;
				ISAXSDataCollection sampleProgress = (ISAXSDataCollection) tableItem.getData();

				final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
			}
		});

		final IObservableMap sampleName = BeanProperties.value(ISAXSDataCollection.class,
				ISAXSDataCollection.SAMPLE_NAME).observeDetail(knownElements);

		viewerColumn1.setLabelProvider(new ObservableMapColumnLabelProvider(sampleName));

		// final IObservableMap collectionStatus = BeanProperties.value(ISAXSDataCollection.class,
		// ISAXSDataCollection.COLLECTION_STATUS).observeDetail(knownElements);
		//
		// viewerColumn3.setLabelProvider(new ObservableMapColumnLabelProvider(collectionStatus));
		//
		// final IObservableMap reductionStatus = BeanProperties.value(ISAXSDataCollection.class,
		// ISAXSDataCollection.REDUCTION_STATUS).observeDetail(knownElements);
		//
		// viewerColumn4.setLabelProvider(new ObservableMapColumnLabelProvider(reductionStatus));
		//
		// final IObservableMap analysisStatus = BeanProperties.value(ISAXSDataCollection.class,
		// ISAXSDataCollection.ANALYSIS_STATUS).observeDetail(knownElements);
		//
		// viewerColumn5.setLabelProvider(new ObservableMapColumnLabelProvider(analysisStatus));

		// final IObservableMap collectionProgress = BeanProperties.value(ISAXSDataCollection.class,
		// ISAXSDataCollection.COLLECTION_PROGRESS).observeDetail(knownElements);

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
		// int progress = ((Double) value).intValue();
		// ProgressBar progressBar = ((ProgressBar) control);
		//
		// progressBar.setSelection(progress);
		// }
		// };
		// viewerColumn3.setLabelProvider(new ObservableMapCellControlProvider(collectionProgress, factory, "Column3"));

		final IObservableMap collectionProgressValues = BeanProperties.value(ISAXSDataCollection.class,
				ISAXSDataCollection.COLLECTION_PROGRESS).observeDetail(knownElements);

		viewerColumn2.setLabelProvider(new ObservableMapOwnerDrawProvider(collectionProgressValues) {
			org.eclipse.swt.graphics.Color original = null;
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color red = null;

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
				original = event.display.getSystemColor(SWT.COLOR_WHITE);
				green = event.display.getSystemColor(SWT.COLOR_GREEN);
				red = event.display.getSystemColor(SWT.COLOR_RED);

				event.gc.setBackground(green);

				Object value = attributeMaps[0].get(element);

				int columnWidth = viewerColumn2.getColumn().getWidth();
				int percentage = ((Double) value).intValue();
				int columnPercentage = (int) ((columnWidth * 0.01) * percentage);
				event.setBounds(new Rectangle(event.x, event.y, columnPercentage, (event.height - 1)));

				event.gc.fillRectangle(event.getBounds());
			}
		});

		final IObservableMap reductionProgressValues = BeanProperties.value(ISAXSDataCollection.class,
				ISAXSDataCollection.REDUCTION_PROGRESS).observeDetail(knownElements);

		viewerColumn3.setLabelProvider(new ObservableMapOwnerDrawProvider(reductionProgressValues) {
			org.eclipse.swt.graphics.Color original = null;
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color red = null;

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
				original = event.display.getSystemColor(SWT.COLOR_WHITE);
				green = event.display.getSystemColor(SWT.COLOR_GREEN);
				red = event.display.getSystemColor(SWT.COLOR_RED);

				event.gc.setBackground(green);

				Object value = attributeMaps[0].get(element);

				int columnWidth = viewerColumn3.getColumn().getWidth();
				int percentage = ((Double) value).intValue();
				int columnPercentage = (int) ((columnWidth * 0.01) * percentage);
				event.setBounds(new Rectangle(event.x, event.y, columnPercentage, (event.height - 1)));

				event.gc.fillRectangle(event.getBounds());
			}
		});

		final IObservableMap analysisProgressValues = BeanProperties.value(ISAXSDataCollection.class,
				ISAXSDataCollection.ANALYSIS_PROGRESS).observeDetail(knownElements);

		viewerColumn4.setLabelProvider(new ObservableMapOwnerDrawProvider(analysisProgressValues) {
			org.eclipse.swt.graphics.Color original = null;
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color red = null;

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
				original = event.display.getSystemColor(SWT.COLOR_WHITE);
				green = event.display.getSystemColor(SWT.COLOR_GREEN);
				red = event.display.getSystemColor(SWT.COLOR_RED);

				event.gc.setBackground(green);

				Object value = attributeMaps[0].get(element);

				int columnWidth = viewerColumn4.getColumn().getWidth();
				int percentage = ((Double) value).intValue();
				int columnPercentage = (int) ((columnWidth * 0.01) * percentage);
				event.setBounds(new Rectangle(event.x, event.y, columnPercentage, (event.height - 1)));

				event.gc.fillRectangle(event.getBounds());
			}
		});

		bioSaxsProgressViewer.setInput(input);
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

	}
}
