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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.BioSaxsMeasurement;
import uk.ac.gda.devices.bssc.BioSaxsProgressContentProvider;
import uk.ac.gda.devices.bssc.BioSaxsProgressLabelProvider;
import uk.ac.gda.devices.bssc.BioSaxsProgressModel;
import uk.ac.gda.devices.bssc.BioSaxsSession;
import uk.ac.gda.devices.bssc.DummyEditorInput;

public class BioSaxsProgressView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(BioSaxsProgressView.class);
	private static final int DEFAULT_COLUMN_WIDTH = 140;
	private Composite bioSaxsProgressComposite;
	private TableViewer bioSaxsProgressViewer;
	private BioSaxsProgressModel bioSaxsProgressModel;

	public BioSaxsProgressView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		bioSaxsProgressComposite = new Composite(parent, SWT.NONE);
		bioSaxsProgressComposite.setLayout(new FillLayout());

		// bioSaxsProgressViewer = new TreeViewer(bioSaxsProgressComposite, SWT.NONE);
		// Tree bioSaxsTree = bioSaxsProgressViewer.getTree();
		// bioSaxsTree.setLayout(new FillLayout());

		bioSaxsProgressViewer = new TableViewer(bioSaxsProgressComposite, SWT.NONE);
		bioSaxsProgressViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				BioSaxsMeasurement selectedMeasurement = (BioSaxsMeasurement) selection.getFirstElement();

				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				try {
					page.openEditor(
							new DummyEditorInput(selectedMeasurement.getName() + " ("
									+ selectedMeasurement.getWellColumn() + ", " + selectedMeasurement.getWellRow()
									+ ")"), "org.eclipse.ui.DefaultTextEditor");
				} catch (PartInitException e) {
					logger.error("TODO put description of error here", e);
				}
			}
		});

		final Table bioSaxsTable = bioSaxsProgressViewer.getTable();
		bioSaxsTable.setLayout(new FillLayout());

		TableColumn column1 = new TableColumn(bioSaxsTable, SWT.NONE);
		column1.setWidth(100);
		column1.setResizable(true);
		column1.setText("Session");

		final TableColumn column2 = new TableColumn(bioSaxsTable, SWT.NONE);
		column2.setWidth(100);
		column2.setResizable(true);
		column2.setText("Measurement");

		TableColumn column3 = new TableColumn(bioSaxsTable, SWT.NONE);
		column3.setWidth(100);
		column3.setResizable(true);
		column3.setText("WellPosition (C,R)");

		TableColumn column4 = new TableColumn(bioSaxsTable, SWT.NONE);
		column4.setWidth(100);
		column4.setResizable(true);
		column4.setText("Acquisition");

		TableColumn column5 = new TableColumn(bioSaxsTable, SWT.NONE);
		column5.setWidth(100);
		column5.setResizable(true);
		column5.setText("Reduction");

		TableColumn column6 = new TableColumn(bioSaxsTable, SWT.NONE);
		column6.setWidth(100);
		column6.setResizable(true);
		column6.setText("Analysis");

		bioSaxsProgressViewer.setContentProvider(new BioSaxsProgressContentProvider());
		bioSaxsProgressViewer.setLabelProvider(new BioSaxsProgressLabelProvider());
		bioSaxsTable.setHeaderVisible(true);

		createDummyModel();
		bioSaxsProgressViewer.setInput(bioSaxsProgressModel);
		populateDummyModel();
		bioSaxsProgressViewer.refresh();

		int measurementCount = bioSaxsProgressModel.getMeasurements().size();

		final int[] percents = new int[bioSaxsProgressModel.getMeasurements().size()];
		for (int i = 0; i < measurementCount; i++) {
			percents[i] = 100;
		}

		bioSaxsTable.addListener(SWT.PaintItem, new Listener() {
			public void handleEvent(Event event) {
				if ((event.index == 3) || (event.index == 4) || (event.index == 5)) {
					Display display = bioSaxsTable.getDisplay();
					GC gc = event.gc;
					TableItem item = (TableItem) event.item;
					int index = bioSaxsTable.indexOf(item);
					int percent = percents[index];
					Color foreground = gc.getForeground();
					Color background = gc.getBackground();
					gc.setForeground(display.getSystemColor(SWT.COLOR_GREEN));
					// gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
					int width = (column2.getWidth() - 1) * percent / 100;
					gc.fillGradientRectangle(event.x, event.y, width, event.height, true);
					Rectangle rect2 = new Rectangle(event.x, event.y, width - 1, event.height - 1);
					gc.drawRectangle(rect2);
					gc.setForeground(bioSaxsTable.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
					String text = percent + "%";
					Point size = event.gc.textExtent(text);
					int offset = Math.max(0, (event.height - size.y) / 2);
					gc.drawText(text, event.x + 2, event.y + offset, true);
					gc.setForeground(background);
					gc.setBackground(foreground);
				}
			}
		});
	}

	private void createDummyModel() {
		bioSaxsProgressModel = new BioSaxsProgressModel();
	}

	private void populateDummyModel() {
		BioSaxsSession session = new BioSaxsSession("Session 1");
		for (int columnIndex = 1; columnIndex < 8; columnIndex++) {
			for (int rowIndex = 1; rowIndex < 8; rowIndex++) {
				BioSaxsMeasurement bioSaxsMeasurement = new BioSaxsMeasurement(session, columnIndex, rowIndex,
						"Measurement");
				bioSaxsProgressModel.addMeasurement(bioSaxsMeasurement);
			}
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
