/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.gef.dnd.SimpleObjectTransfer;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueEvent;

public class MeasurementsFieldComposite extends FieldComposite {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementsFieldComposite.class);

	Object value = null;
	private Table table;
	private Label sampleCount;
	private final TableViewer tableViewer;
	private Composite composite_1;
	private final RichBeanEditorPart rbeditor;

	Color okay, warning;

	private static final SimpleObjectTransfer TRANSFER = new SimpleObjectTransfer() {
		private final String TYPE_NAME = "uk.ac.gda.devices.bssc.ui.TitrationBeanTransfer" + System.currentTimeMillis(); //$NON-NLS-1$
		private final int TYPE_ID = registerType(TYPE_NAME);

		@Override
		protected int[] getTypeIds() {
			return new int[] { TYPE_ID };
		}

		@Override
		protected String[] getTypeNames() {
			return new String[] { TYPE_NAME };
		}
	};

	public abstract class OurEditingSupport extends EditingSupport {

		protected TableViewer viewer = tableViewer;
		protected RichBeanEditorPart editor = rbeditor;
		protected CellEditor cachedCellEditor = null;

		public OurEditingSupport() {
			super(tableViewer);
		}

		@Override
		final protected CellEditor getCellEditor(Object element) {
			if (cachedCellEditor == null) {
				cachedCellEditor = getOurCellEditor(element);
			}
			return cachedCellEditor;
		}

		abstract protected CellEditor getOurCellEditor(Object element);

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected void setValue(Object element, Object value) {
			editor.valueChangePerformed(new ValueEvent(this, null));
			viewer.refresh();
		}
	}

	public final class DoubleCellEditor extends TextCellEditor {
		public DoubleCellEditor(final Composite parent) {
			super(parent);
		}

		@Override
		protected Object doGetValue() {
			Object value = super.doGetValue();
			return Double.parseDouble(value.toString());
		}

		@Override
		protected void doSetValue(final Object value) {
			if (value == null) {
				super.doSetValue(String.valueOf(new Double(0)));
			} else {
				super.doSetValue(String.valueOf(value.toString()));
			}
		}
	}

	public final class IntegerCellEditor extends TextCellEditor {
		public IntegerCellEditor(final Composite parent) {
			super(parent);
		}

		@Override
		protected Object doGetValue() {
			Object value = super.doGetValue();
			Integer result = 0;
			try {
				result = Integer.parseInt(value.toString());
			} catch (NumberFormatException nfe) {
				result = 0;
			}

			return result;
		}

		@Override
		protected void doSetValue(final Object value) {
			if (value == null) {
				super.doSetValue(String.valueOf(new Integer(0)));
			} else {
				super.doSetValue(String.valueOf(value.toString()));
			}
		}
	}

	public MeasurementsFieldComposite(Composite parent, int style, RichBeanEditorPart editor) {
		super(parent, style);
		this.rbeditor = editor;

		final Display display = Display.getCurrent();
		okay = null;
		warning = new Color(display, 255, 160, 30);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		final Composite comp = new Composite(this, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(layoutData);
		TableColumnLayout layout = new TableColumnLayout();
		comp.setLayout(layout);

		tableViewer = new TableViewer(comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);

		table = tableViewer.getTable();
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.EraseItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.detail &= ~SWT.HOT;
				if ((event.detail & SWT.SELECTED) == 0)
					return; // / item not selected

				// Table table =(Table)event.widget;
				// TableItem item =(TableItem)event.item;
				// TitrationBean element = (TitrationBean) item.getData();
				// int clientWidth = table.getClientArea().width;

				GC gc = event.gc;
				Rectangle rect = event.getBounds();
				gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
				gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
				gc.fillRectangle(rect);
				event.detail &= ~SWT.SELECTED;
			}
		});

		Object[][] columns = { { "Plate", 50, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getLocation().getPlate();
				switch (plate) {
				case 1:
					return "I";
				case 2:
					return "II";
				}
				return "III";
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "I", "II", "III" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getLocation().getPlate();
				switch (plate) {
				case 1:
					return "I";
				case 2:
					return "II";
				}
				return "III";
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).getLocation().setPlate((short) String.valueOf(value).length());
				super.setValue(element, value);
			}
		} }, { "Row", 50, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%c", tb.getLocation().getRow());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "A", "B", "C", "D", "E", "F", "G", "H" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getLocation().getRow();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).getLocation().setRow(String.valueOf(value).charAt(0));
				super.setValue(element, value);
			}
		} }, { "Column", 65, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%d", tb.getLocation().getColumn());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getLocation().getColumn();
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (value == null) {
					return;
				}
				((TitrationBean) element).getLocation().setColumn(Integer.valueOf((String) value).shortValue());
				super.setValue(element, value);
			}
		} }, { "Sample Name", 120, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return tb.getSampleName();
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getSampleName();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setSampleName(String.valueOf(value));
				super.setValue(element, value);
			}
		} }, { "Concentration", 100, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%5.5f", tb.getConcentration());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new DoubleCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getConcentration();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setConcentration((Double) value);
				super.setValue(element, value);
			}
		} }, { "Viscosity", 75, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return tb.getViscosity();
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "low", "medium", "high" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getViscosity();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setViscosity(String.valueOf(value));
				super.setValue(element, value);
			}
		} }, { "Molecular\nWeight", 90, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.valueOf(tb.getMolecularWeight());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new DoubleCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getMolecularWeight();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setMolecularWeight(((Number) value).doubleValue());
				super.setValue(element, value);
			}
		} }, { "Buffer\nPlate", 50, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getBufferLocation().getPlate();
				switch (plate) {
				case 1:
					return "I";
				case 2:
					return "II";
				}
				return "III";
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "I", "II", "III" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getBufferLocation().getPlate();
				switch (plate) {
				case 1:
					return "I";
				case 2:
					return "II";
				}
				return "III";
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).getBufferLocation().setPlate((short) String.valueOf(value).length());
				super.setValue(element, value);
			}
		} }, { "Buffer\nRow", 50, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%c", tb.getBufferLocation().getRow());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "A", "B", "C", "D", "E", "F", "G", "H" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getBufferLocation().getRow();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).getBufferLocation().setRow(String.valueOf(value).charAt(0));
				super.setValue(element, value);
			}
		} }, { "Buffer\nColumn", 65, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%d", tb.getBufferLocation().getColumn());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getBufferLocation().getColumn();
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (value == null) {
					return;
				}
				((TitrationBean) element).getBufferLocation().setColumn(Integer.valueOf((String) value).shortValue());
				super.setValue(element, value);
			}
		} }, { "Recoup", 60, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.valueOf(tb.getRecouperateLocation() != null);
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new CheckboxCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getRecouperateLocation() != null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if ((Boolean) value) {
					((TitrationBean) element).setRecouperateLocation(new LocationBean());
				} else {
					((TitrationBean) element).setRecouperateLocation(null);
				}
				super.setValue(element, value);
			}
		} }, { "Recoup\nPlate", 60, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					return "--";
				short plate = tb.getRecouperateLocation().getPlate();
				switch (plate) {
				case 1:
					return "I";
				case 2:
					return "II";
				}
				return "III";
			}

			@Override
			public Color getBackground(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getLocation().equals(tb.getRecouperateLocation()))
					return warning;
				return okay;
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "I", "II", "III" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					return "--";
				short plate = tb.getRecouperateLocation().getPlate();
				switch (plate) {
				case 1:
					return "I";
				case 2:
					return "II";
				}
				return "III";
			}

			@Override
			protected void setValue(Object element, Object value) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					tb.setRecouperateLocation(new LocationBean());
				tb.getRecouperateLocation().setPlate((short) String.valueOf(value).length());
				super.setValue(element, value);
			}
		} }, { "Recoup\nRow", 60, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					return "--";
				return String.format("%c", tb.getRecouperateLocation().getRow());
			}

			@Override
			public Color getBackground(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getLocation().equals(tb.getRecouperateLocation()))
					return warning;
				return okay;
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "A", "B", "C", "D", "E", "F", "G", "H" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					return "--";
				return tb.getRecouperateLocation().getRow();
			}

			@Override
			protected void setValue(Object element, Object value) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					tb.setRecouperateLocation(new LocationBean());
				tb.getRecouperateLocation().setRow(String.valueOf(value).charAt(0));
				super.setValue(element, value);
			}
		} }, { "Recoup\nColumn", 65, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					return "--";
				return String.format("%d", tb.getRecouperateLocation().getColumn());
			}

			@Override
			public Color getBackground(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getLocation().equals(tb.getRecouperateLocation()))
					return warning;
				return okay;
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" });
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					return "--";
				return tb.getRecouperateLocation().getColumn();
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (value == null) {
					return;
				}
				TitrationBean tb = (TitrationBean) element;
				if (tb.getRecouperateLocation() == null)
					tb.setRecouperateLocation(new LocationBean());
				tb.getRecouperateLocation().setColumn(Integer.valueOf((String) value).shortValue());
				super.setValue(element, value);
			}
		} }, { "Time per\nFrame", 80, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%5.3f", tb.getTimePerFrame());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new DoubleCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getTimePerFrame();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setTimePerFrame((Double) value);
				super.setValue(element, value);
			}
		} }, { "Frames", 60, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.valueOf(tb.getFrames());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new IntegerCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getFrames();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setFrames((Integer) value);
				super.setValue(element, value);
			}
		} }, { "Exposure\nTemperature", 90, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%4.1f \u00B0C", tb.getExposureTemperature());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new DoubleCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getExposureTemperature();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setExposureTemperature(((Number) value).floatValue());
				super.setValue(element, value);
			}
		} } };

		for (Object[] column : columns) {
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.CENTER);
			int width = Integer.valueOf(column[1].toString());
			col.getColumn().setWidth(width);
			col.getColumn().setText(column[0].toString());
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(true);
			layout.setColumnData(col.getColumn(), new ColumnWeightData(width, width));
			col.setLabelProvider((CellLabelProvider) column[2]);
			col.setEditingSupport((EditingSupport) column[3]);
		}

		DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		source.setTransfer(new Transfer[] { TRANSFER, TextTransfer.getInstance() });

		source.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem[] selection = table.getSelection();

				if (TRANSFER.isSupportedType(event.dataType)) {
					List<TitrationBean> data = new ArrayList<TitrationBean>();
					try {
						for (TableItem element : selection) {
							TitrationBean oldBean = (TitrationBean) element.getData();
							TitrationBean copiedBean = (TitrationBean) BeanUtils.cloneBean(oldBean);
							copiedBean.setLocation((LocationBean) BeanUtils.cloneBean(oldBean.getLocation()));
							copiedBean.setBufferLocation((LocationBean) BeanUtils.cloneBean(oldBean.getBufferLocation()));
							data.add(copiedBean);
						}
					} catch (Exception e) {
						logger.error("error cloning titrationbean for outgoing drag and drop", e);
					}
					event.data = data;
				} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					StringBuffer buff = new StringBuffer();
					for (TableItem element : selection) {
						buff.append(((TitrationBean) element.getData()).getSampleName());
					}

					event.data = buff.toString();
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				super.dragFinished(event);
				if ((event.detail & DND.DROP_MOVE) != 0) {
					logger.debug(event.toString());
					DragSource ds = (DragSource) event.widget;
					Table table = (Table) ds.getControl();
					TableItem[] selection = table.getSelection();
					if (selection.length == 0)
						logger.debug("selection empty");
					for (TableItem element : selection) {
						if (!getList().remove(element.getData()))
							logger.debug("data not there or not removed");
					}

					sampleCount.setText(String.valueOf(getList().size()));
					tableViewer.refresh();
				} else {
					logger.debug("not move");
				}
			}
		});

		// Create the drop target
		DropTarget target = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[] { TRANSFER });
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_MOVE) != 0 ? DND.DROP_MOVE : DND.DROP_NONE;
				}

				for (TransferData dataType : event.dataTypes) {
					if (TRANSFER.isSupportedType(dataType)) {
						event.currentDataType = dataType;
					}
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (TRANSFER.isSupportedType(event.currentDataType)) {
					@SuppressWarnings("unchecked")
					List<TitrationBean> data = (List<TitrationBean>) event.data;
					List<TitrationBean> list = getList();

					int before = 0;

					Widget intoitem = event.item;
					if (intoitem == null) {
						before = list.size();
					} else {
						before = list.indexOf(intoitem.getData());
					}

					list.addAll(before, data);

					sampleCount.setText(String.valueOf(getList().size()));
					tableViewer.refresh();
				}
			}
		});

		tableViewer.setContentProvider(new ArrayContentProvider());

		composite_1 = new Composite(this, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginWidth = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginRight = 0;
		rowLayout.spacing = 5;
		composite_1.setLayout(rowLayout);

		Label label = new Label(composite_1, SWT.NONE);
		label.setText("Number of Samples:");

		sampleCount = new Label(composite_1, SWT.NONE);
		sampleCount.setText("NaN");
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
		sampleCount.setText(String.valueOf(getList().size()));
		tableViewer.setInput(value);
	}

	@SuppressWarnings("unchecked")
	private List<TitrationBean> getList() {
		return ((List<TitrationBean>) value);
	}

	public void deleteSelection() {
		if (table.getSelectionIndices().length == 0) {
			return;
		}
		int[] selectionIndices = table.getSelectionIndices();
		Arrays.sort(selectionIndices);
		for (int i = selectionIndices.length - 1; i >= 0; i--) {
			getList().remove(selectionIndices[i]);
		}
		sampleCount.setText(String.valueOf(getList().size()));
		tableViewer.refresh();
	}

	public void addSample() {
		if (table.getSelectionIndices().length == 0) {
			getList().add(new TitrationBean());
		} else {
			int[] selectionIndices = table.getSelectionIndices();
			List<TitrationBean> toadd = new ArrayList<TitrationBean>(table.getSelectionIndices().length);
			for (int i : selectionIndices) {
				try {
					TitrationBean oldBean = getList().get(i);
					TitrationBean copiedBean = (TitrationBean) BeanUtils.cloneBean(oldBean);
					copiedBean.setLocation((LocationBean) BeanUtils.cloneBean(oldBean.getLocation()));
					copiedBean.setBufferLocation((LocationBean) BeanUtils.cloneBean(oldBean.getBufferLocation()));
					toadd.add(copiedBean);
				} catch (Exception e) {
				}
			}
			getList().addAll(selectionIndices[selectionIndices.length - 1] + 1, toadd);
		}
		sampleCount.setText(String.valueOf(getList().size()));
		tableViewer.refresh();
	}
}