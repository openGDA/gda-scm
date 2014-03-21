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
import org.eclipse.swt.widgets.Display;
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
import uk.ac.gda.devices.bssc.views.BioSAXSCollectionResultPlotView;
import uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView;
import uk.ac.gda.richbeans.components.FieldComposite;

public class BioSAXSProgressComposite extends FieldComposite {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressComposite.class);
	private static final Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	private static final Color green = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private static final Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private static final Color listBackGround = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	private static final Color lightGrey = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
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
		lastBackground = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

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
						case 3:
							IViewPart analysisResultPlotView;
							try {
								analysisResultPlotView = page
										.showView("uk.ac.gda.devices.bssc.views.BioSAXSAnalysisResultPlotView");
								page.activate(analysisResultPlotView);
							} catch (PartInitException e) {
								logger.error("Error activating the data analysis results view", e);
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
				if (white != null) {
					event.gc.setBackground(white);
					event.gc.fillRectangle(event.getBounds());
				}
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				GC gc = event.gc;

				ISAXSProgress progress = (ISAXSProgress) element;
				ISpyBStatus status = progress.getCollectionStatusInfo().getStatus();
				double progressValue = progress.getCollectionStatusInfo().getProgress();
				int percentage = ((Double) progressValue).intValue();
				int columnWidth = viewerColumn2.getColumn().getWidth();

				if (status == ISpyBStatus.NOT_STARTED) {
					gc.setBackground(listBackGround);
					int width = (int) ((columnWidth * 0.01) * 100);
					event.setBounds(new Rectangle(event.x, event.y, width, (event.height - 1)));
					gc.fillRectangle(event.getBounds());
				} else if (status == ISpyBStatus.RUNNING) {
					gc.setForeground(green);
					gc.setBackground(listBackGround);
					int gradientWidth = (int) ((columnWidth * 0.01) * 100);
					gc.fillGradientRectangle(event.x, event.y, gradientWidth, event.height, true);

					if (percentage > 0) {
						int width = (int) ((columnWidth * 0.01) * percentage);
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
				if (white != null) {
					event.gc.setBackground(white);
					event.gc.fillRectangle(event.getBounds());
				}
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				GC gc = event.gc;

				ISAXSProgress progress = (ISAXSProgress) element;
				ISpyBStatus status = progress.getReductionStatusInfo().getStatus();
				double progressValue = progress.getReductionStatusInfo().getProgress();
				int percentage = ((Double) progressValue).intValue();
				int columnWidth = viewerColumn2.getColumn().getWidth();
				int columnPercentage;

				if (status == ISpyBStatus.RUNNING) {
					gc.setForeground(green);
					gc.setBackground(listBackGround);
					columnPercentage = (int) ((columnWidth * 0.01) * 100);
					gc.fillGradientRectangle(event.x, event.y, columnPercentage, (event.height - 1), true);
				} else if (status == ISpyBStatus.FAILED) {
					gc.setBackground(red);
					columnPercentage = (int) ((columnWidth * 0.01) * 100);
					event.setBounds(new Rectangle(event.x, event.y, columnPercentage, (event.height - 1)));
					gc.fillRectangle(event.getBounds());
				} else if (status == ISpyBStatus.COMPLETE) {
					gc.setBackground(green);
					columnPercentage = (int) ((columnWidth * 0.01) * percentage);
					event.setBounds(new Rectangle(event.x, event.y, columnPercentage, (event.height - 1)));
					gc.fillRectangle(event.getBounds());
				}

			}
		});

		final IObservableMap analysisProgressValues = BeanProperties.value(ISAXSProgress.class,
				ISAXSProgress.ANALYSIS_STATUS_INFO).observeDetail(knownElements);

		viewerColumn4.setLabelProvider(new ObservableMapOwnerDrawProvider(analysisProgressValues) {
			@Override
			protected void measure(Event event, Object element) {
				event.setBounds(new Rectangle(event.x, event.y, 20, 10));
			}

			@Override
			protected void erase(Event event, Object element) {
				if (white != null) {
					event.gc.setBackground(white);
					event.gc.fillRectangle(event.getBounds());
				}
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				GC gc = event.gc;
				ISAXSProgress progress = (ISAXSProgress) element;
				ISpyBStatus status = progress.getAnalysisStatusInfo().getStatus();
				double progressValue = progress.getAnalysisStatusInfo().getProgress();
				int percentage = ((Double) progressValue).intValue();
				int columnWidth = viewerColumn2.getColumn().getWidth();
				int columnPercentage;

				if (status == ISpyBStatus.FAILED) {
					event.gc.setBackground(red);
					columnPercentage = (int) ((columnWidth * 0.01) * 100);
				} else if (status == ISpyBStatus.RUNNING) {
					gc.setForeground(green);
					gc.setBackground(listBackGround);
					columnPercentage = (int) ((columnWidth * 0.01) * 100);
					gc.fillGradientRectangle(event.x, event.y, columnPercentage, (event.height - 1), false);
				} else {
					event.gc.setBackground(green);
					columnPercentage = (int) ((columnWidth * 0.01) * percentage);
				}

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
