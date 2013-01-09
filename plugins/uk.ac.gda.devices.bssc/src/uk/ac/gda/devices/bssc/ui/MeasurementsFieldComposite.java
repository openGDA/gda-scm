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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueEvent;

public class MeasurementsFieldComposite extends FieldComposite {

	Object value = null;
	private Table table;
	private Label sampleCount;
	private final TableViewer tableViewer;
	private Composite composite_1;
	private final RichBeanEditorPart rbeditor;

	public abstract class OurEditingSupport extends EditingSupport {

		protected TableViewer viewer = tableViewer;
		protected RichBeanEditorPart editor;
		protected CellEditor cachedCellEditor = null;

		public OurEditingSupport() {
			super(tableViewer);
			editor = rbeditor;
		}

		@Override
		final protected CellEditor getCellEditor(Object element) {
			if (cachedCellEditor == null)
				cachedCellEditor = getOurCellEditor(element);
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
			if (value==null) {
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
			if (value==null) {
				super.doSetValue(String.valueOf(new Integer(0)));
			} else {
				super.doSetValue(String.valueOf(value.toString()));
			}
		}
	}

	public MeasurementsFieldComposite(Composite parent, int style, RichBeanEditorPart editor) {
		super(parent, style);
		this.rbeditor = editor;

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		final Composite comp = new Composite(this, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(layoutData);
		TableColumnLayout layout = new TableColumnLayout();
		comp.setLayout(layout);

		tableViewer = new TableViewer(comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		table = tableViewer.getTable();
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		Object[][] columns = { 
		{ "Plate", 50, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getLocation().getPlate();
				switch (plate) {
				case 1: return "I";
				case 2: return "II";
				}
				return "III";
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] {"I", "II", "III"});
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getLocation().getPlate();
				switch (plate) {
				case 1: return "I";
				case 2: return "II";
				}
				return "III";
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).getLocation().setPlate((short) String.valueOf(value).length());
				super.setValue(element, value);
			} 
		} }, 
		{ "Row", 40, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%c",tb.getLocation().getRow());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] {"A", "B", "C", "D", "E", "F", "G", "H"});
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
		}
		}, 
		{ "Column", 65, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%d",tb.getLocation().getColumn());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"});
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getLocation().getColumn();
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (value == null) return;
				((TitrationBean) element).getLocation().setColumn(Integer.valueOf((String) value).shortValue());
				super.setValue(element, value);
			} 
		}
		}, 
		{ "Sample Name", 110, new ColumnLabelProvider() {
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
		} }, 
		{ "Bu Plate", 50, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getBufferLocation().getPlate();
				switch (plate) {
				case 1: return "I";
				case 2: return "II";
				}
				return "III";
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] {"I", "II", "III"});
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				TitrationBean tb = (TitrationBean) element;
				short plate = tb.getBufferLocation().getPlate();
				switch (plate) {
				case 1: return "I";
				case 2: return "II";
				}
				return "III";
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).getBufferLocation().setPlate((short) String.valueOf(value).length());
				super.setValue(element, value);
			} 
		} }, 
		{ "Bu Row", 40, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%c",tb.getBufferLocation().getRow());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] {"A", "B", "C", "D", "E", "F", "G", "H"});
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
		}
		}, 
		{ "Bu Column", 65, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.format("%d",tb.getBufferLocation().getColumn());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor((Composite) viewer.getControl());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setLabelProvider(new LabelProvider());
				ce.setInput(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"});
				return ce;
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).getBufferLocation().getColumn();
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (value == null) return;
				((TitrationBean) element).getBufferLocation().setColumn(Integer.valueOf((String) value).shortValue());
				super.setValue(element, value);
			} 
		}
		}, 
		{ "Yellow Sample", 112, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.valueOf(tb.isYellowSample());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new CheckboxCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).isYellowSample();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setYellowSample((Boolean) value);
				super.setValue(element, value);
			} 
		} }, 
		{ "Recouperate", 112, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TitrationBean tb = (TitrationBean) element;
				return String.valueOf(tb.isRecouperate());
			}
		}, new OurEditingSupport() {
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return new CheckboxCellEditor(viewer.getTable());
			}

			@Override
			protected Object getValue(Object element) {
				return ((TitrationBean) element).isRecouperate();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((TitrationBean) element).setRecouperate((Boolean) value);
				super.setValue(element, value);
			} 
		} }, 
		{ "Concentration", 110, new ColumnLabelProvider() {
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
				ce.setInput(new String[] {"low", "medium", "high"});
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
		}
		}, { "Time per Frame", 122, new ColumnLabelProvider() {
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
		} }, { "Exposure Temperature", 170, new ColumnLabelProvider() {
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
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE);
			int width = Integer.valueOf(column[1].toString());
			col.getColumn().setWidth(width);
			col.getColumn().setText(column[0].toString());
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(true);
			layout.setColumnData(col.getColumn(), new ColumnWeightData(width, width));
			col.setLabelProvider((CellLabelProvider) column[2]);
			col.setEditingSupport((EditingSupport) column[3]);
		}

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
		if (table.getSelectionIndices().length == 0) return;
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
			for(int i : selectionIndices)
				try {
					toadd.add((TitrationBean) BeanUtils.cloneBean(getList().get(i)));
				} catch (Exception e) {
				} 
			getList().addAll(selectionIndices[selectionIndices.length-1]+1, toadd);
		}
		sampleCount.setText(String.valueOf(getList().size()));
		tableViewer.refresh();
	}
}