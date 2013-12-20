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

import java.awt.Color;

import gda.rcp.GDAClientActivator;

import org.apache.poi.sl.usermodel.Fill;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapCellControlProvider;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapCellControlProvider.ControlFactoryAndUpdater;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapOwnerDrawProvider;
import uk.ac.gda.devices.bssc.ISampleProgress;
import uk.ac.gda.devices.bssc.ISampleProgressCollection;

public class BioSAXSProgressComposite extends Composite {
	private TableViewer bioSaxsProgressViewer;
	private Table bioSaxsTable;
	private ISampleProgressCollection model;
	
	public BioSAXSProgressComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new FillLayout());
		
		bioSaxsProgressViewer = new TableViewer(this, SWT.NONE);
		bioSaxsTable = bioSaxsProgressViewer.getTable();

		bioSaxsTable.setHeaderVisible(true);
		bioSaxsTable.setLinesVisible(true);

		TableViewerColumn viewerColumn1 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column1 = viewerColumn1.getColumn();
		column1.setWidth(100);
		column1.setResizable(true);
		column1.setText("Measurement");

		TableViewerColumn viewerColumn2 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column2 = viewerColumn2.getColumn();
		column2.setWidth(100);
		column2.setResizable(true);
		column2.setText("Collection Progress");

		final TableViewerColumn viewerColumn3 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column3 = viewerColumn3.getColumn();
		column3.setWidth(100);
		column3.setResizable(true);
		column3.setText("Reduction Progress");

		final TableViewerColumn viewerColumn4 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column4 = viewerColumn4.getColumn();
		column4.setWidth(100);
		column4.setResizable(true);
		column4.setText("Analysis Progress");

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		bioSaxsProgressViewer.setContentProvider(contentProvider);

		// Create the label provider including monitoring of the changes of the
		// labels
		IObservableSet knownElements = contentProvider.getKnownElements();

		final IObservableMap collectionProgress = BeanProperties.value(ISampleProgress.class,
				ISampleProgress.COLLECTION_PROGRESS).observeDetail(knownElements);

		viewerColumn1.setLabelProvider(new ColumnLabelProvider());

		ControlFactoryAndUpdater factory = new ObservableMapCellControlProvider.ControlFactoryAndUpdater() {

			@Override
			public Control createControl(Composite parent) {
				ProgressBar progressBar = new ProgressBar(parent, SWT.NONE);
				progressBar.setMaximum(100);
				return progressBar;
			}

			@Override
			public void updateControl(Control control, Object value) {
				((ProgressBar) control).setSelection(((Double) value).intValue());

			}
		};
		viewerColumn2.setLabelProvider(new ObservableMapCellControlProvider(collectionProgress, factory, "Column2"));

		final IObservableMap reductionProgressValues = BeanProperties.value(ISampleProgress.class,
				ISampleProgress.REDUCTION_PROGRESS).observeDetail(knownElements);
		// viewerColumn3.setLabelProvider(new ObservableMapColumnLabelProvider(reductionProgressValues));
		viewerColumn3.setLabelProvider(new ObservableMapOwnerDrawProvider(reductionProgressValues) {
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color yellow = null;
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
				if (green == null) {
					original = event.gc.getBackground();
					green = event.display.getSystemColor(SWT.COLOR_GREEN);
					yellow = event.display.getSystemColor(SWT.COLOR_YELLOW);
				}

				Object value = attributeMaps[0].get(element);
				boolean complete = ((Double) value).intValue() > 10;
				event.gc.setBackground(complete ? green : yellow);

				int columnWidth = viewerColumn3.getColumn().getWidth();
				int percentage = ((Double) value).intValue();
				int percentageToFill = (int) ((columnWidth * 0.01) * percentage);
				event.setBounds(new Rectangle(event.x, event.y, percentageToFill, (event.height - 1)));

				event.gc.fillRectangle(event.getBounds());
			}
		});

		final IObservableMap analysisProgressValues = BeanProperties.value(ISampleProgress.class,
				ISampleProgress.ANALYSIS_PROGRESS).observeDetail(knownElements);
		viewerColumn4.setLabelProvider(new ObservableMapOwnerDrawProvider(analysisProgressValues) {
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color yellow = null;
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
				if (green == null) {
					original = event.gc.getBackground();
					green = event.display.getSystemColor(SWT.COLOR_GREEN);
					yellow = event.display.getSystemColor(SWT.COLOR_YELLOW);
				}

				Object value = attributeMaps[0].get(element);
				boolean complete = ((Double) value).intValue() > 10;
				event.gc.setBackground(complete ? green : yellow);

				int columnWidth = viewerColumn4.getColumn().getWidth();
				int percentage = ((Double) value).intValue();
				int percentageToFill = (int) ((columnWidth * 0.01) * percentage);
				event.setBounds(new Rectangle(event.x, event.y, percentageToFill, (event.height - 1)));

				event.gc.fillRectangle(event.getBounds());
			}
		});
		
		model = (ISampleProgressCollection) GDAClientActivator.getNamedService(ISampleProgressCollection.class, null);

		IObservableList input = model.getItems();
		bioSaxsProgressViewer.setInput(input);
	}
}
