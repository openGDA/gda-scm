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

import org.dawnsci.common.richbeans.components.FieldComposite;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.jface.viewers.ObservableMapColumnLabelProvider;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapOwnerDrawProvider;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatus;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;
import uk.ac.gda.devices.bssc.views.BioSAXSCollectionResultPlotView;
import uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView;

public class BioSAXSProgressComposite extends FieldComposite {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressComposite.class);

	private static final Color white = ColorConstants.white;
	private static final Color green = ColorConstants.green;
	private static final Color lightgreen = new Color(null, 170, 255, 170);
	private static final Color red = ColorConstants.red;
	private static final Color listBackGround = white;
	private static final Color lightGrey = ColorConstants.lightGray;
	
	private long lastExperimentId;
	private TableViewer bioSaxsProgressViewer;
	private Table bioSaxsTable;
	private BioSAXSViewerComparator comparator;
	private Color lastBackground;

	public BioSAXSProgressComposite(Composite parent, final IObservableList input, int style) {
		super(parent, style);

		setLayout(new FillLayout());
		
		bioSaxsProgressViewer = new TableViewer(this, SWT.NONE);
		bioSaxsTable = bioSaxsProgressViewer.getTable();

		bioSaxsTable.setHeaderVisible(true);
		bioSaxsTable.setLinesVisible(false);
		lastBackground = white;

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

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		bioSaxsProgressViewer.setContentProvider(contentProvider);

		comparator = new BioSAXSViewerComparator();
		bioSaxsProgressViewer.setComparator(comparator);

		// Create the label provider including monitoring of the changes of the
		// labels
		IObservableSet knownElements = contentProvider.getKnownElements();

		final Table table = bioSaxsProgressViewer.getTable();

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});

		table.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point pt = new Point(event.x, event.y);
				TableItem tableItem = table.getItem(pt);
				if (tableItem == null) {
					logger.trace("No item selected on table");
					return;
				}
				ISAXSProgress sampleProgress = (ISAXSProgress) tableItem.getData();

				IWorkbench workBench = PlatformUI.getWorkbench();
				final IWorkbenchWindow window = workBench.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();

				try {
					workBench.showPerspective("uk.ac.gda.devices.bssc.biosaxsresultperspective", window);
				} catch (WorkbenchException e1) {
					logger.error("Exception showing the perpective uk.ac.gda.devices.bssc.biosaxsresultperspective", e1);
				}

				BioSAXSCollectionResultPlotView collectionResultPlotView;
				BioSAXSReductionResultPlotView reductionResultPlotView;

				for (int col = 0; col < table.getColumnCount(); col++) {
					Rectangle rect = tableItem.getBounds(col);
					if (rect.contains(pt)) {
						switch (col) {
						case 0:
							try {
								collectionResultPlotView = (BioSAXSCollectionResultPlotView) page
										.showView("uk.ac.gda.devices.bssc.views.BioSAXSCollectionResultPlotView");
								page.activate(collectionResultPlotView);
								collectionResultPlotView.setPlot(sampleProgress);
							} catch (PartInitException e) {
								logger.error("Error activating the data analysis results view", e);
							}
							break;
						case 1:
							try {
								collectionResultPlotView = (BioSAXSCollectionResultPlotView) page
										.showView("uk.ac.gda.devices.bssc.views.BioSAXSCollectionResultPlotView");
								page.activate(collectionResultPlotView);
								collectionResultPlotView.setPlot(sampleProgress);
							} catch (PartInitException e) {
								logger.error("Error activating the data analysis results view", e);
							}
							break;
						case 2:
							try {
								reductionResultPlotView = (BioSAXSReductionResultPlotView) page
										.showView("uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView");
								page.activate(reductionResultPlotView);

								if (sampleProgress.getReductionStatusInfo().getStatus() == ISpyBStatus.COMPLETE) {
									reductionResultPlotView.setPlot(sampleProgress);
								}
							} catch (PartInitException e) {
								logger.error("Error activating the data reduction results view", e);
							}
							break;
						}
					}
				}
			}
		});

		final IObservableMap sampleName = BeanProperties.value(ISAXSProgress.class, ISAXSProgress.SAMPLE_NAME)
				.observeDetail(knownElements);

		viewerColumn1.setLabelProvider(new ObservableMapColumnLabelProvider(sampleName) {
			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				ISAXSProgress progress = (ISAXSProgress) cell.getElement();
				long experimentId = progress.getExperimentId();
				if (lastExperimentId != experimentId) {
					lastExperimentId = experimentId;
					lastBackground = lastBackground.equals(lightGrey) ? white : lightGrey;
					cell.setBackground(lastBackground);
				} else {
					cell.setBackground(lastBackground);
				}
			}
		});

		final IObservableMap collectionProgressValues = BeanProperties.value(ISAXSProgress.class,
				ISAXSProgress.COLLECTION_STATUS_INFO).observeDetail(knownElements);

		viewerColumn2.setLabelProvider(new ObservableMapOwnerDrawProvider(collectionProgressValues) {
			@Override
			protected void measure(Event event, Object element) {
				event.setBounds(new Rectangle(event.x, event.y, 20, 10));
			}

			@Override
			protected void erase(Event event, Object element) {
				eraseTableCell(event);
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				ISAXSProgress progress = (ISAXSProgress) element;
				ISpyBStatusInfo statusInfo = progress.getCollectionStatusInfo();
				paintTableCell(event, viewerColumn2, statusInfo);
			}
		});

		final IObservableMap reductionProgressValues = BeanProperties.value(ISAXSProgress.class,
				ISAXSProgress.REDUCTION_STATUS_INFO).observeDetail(knownElements);

		viewerColumn3.setLabelProvider(new ObservableMapOwnerDrawProvider(reductionProgressValues) {
			@Override
			protected void measure(Event event, Object element) {
				event.setBounds(new Rectangle(event.x, event.y, 20, 10));
			}

			@Override
			protected void erase(Event event, Object element) {
				eraseTableCell(event);
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				ISAXSProgress progress = (ISAXSProgress) element;
				ISpyBStatusInfo statusInfo = progress.getReductionStatusInfo();
				paintTableCell(event, viewerColumn3, statusInfo);
			}
		});

		bioSaxsProgressViewer.setInput(input);
	}

	private void paintTableCell(Event event, TableViewerColumn column, ISpyBStatusInfo statusInfo) {
		GC gc = event.gc;

		ISpyBStatus status = statusInfo.getStatus();
		double progressValue = statusInfo.getProgress();
		
		int percentage = ((Double) progressValue).intValue();
		int columnWidth = column.getColumn().getWidth();

		if (status == ISpyBStatus.NOT_STARTED) {
			gc.setBackground(listBackGround);
			int width = (int) ((columnWidth * 0.01) * 100);
			event.setBounds(new Rectangle(event.x, event.y, width, (event.height - 1)));
			gc.fillRectangle(event.getBounds());
		} else if (status == ISpyBStatus.RUNNING) {
			gc.setBackground(lightgreen);
			int width = (int) ((columnWidth * 0.01) * 100);
			gc.fillRectangle(event.x, event.y, width, event.height);

			if (percentage > 0) {
				width = (int) ((columnWidth * 0.01) * percentage);
				gc.setBackground(green);
				event.setBounds(new Rectangle(event.x, event.y, width, (event.height - 1)));
				gc.fillRectangle(event.getBounds());
			}
		} else if (status == ISpyBStatus.COMPLETE) {
			gc.setBackground(green);
			int width = (int) ((columnWidth * 0.01) * 100);
			event.setBounds(new Rectangle(event.x, event.y, width, (event.height - 1)));
			gc.fillRectangle(event.getBounds());
		} else if (status == ISpyBStatus.FAILED) {
			gc.setBackground(red);
			int width = (int) ((columnWidth * 0.01) * 100);
			event.setBounds(new Rectangle(event.x, event.y, width, (event.height - 1)));
			gc.fillRectangle(event.getBounds());
		}
	}

	private void eraseTableCell(Event event) {
		if (white != null) {
			event.gc.setBackground(white);
			event.gc.fillRectangle(event.getBounds());
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

	}
}

class BioSAXSViewerComparator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		ISAXSProgress p1 = (ISAXSProgress) e1;
		ISAXSProgress p2 = (ISAXSProgress) e2;

		if (p1.getDataCollectionId() > p2.getDataCollectionId())
			return 1;
		return 0;
	}

}
