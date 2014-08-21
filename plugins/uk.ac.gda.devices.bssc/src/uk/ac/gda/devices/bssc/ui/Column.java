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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueEvent;

public abstract class Column<T,V> {
	public static enum ColumnType{
		DOUBLE {
			@Override
			public <E> CellEditor getCellEditor(Composite parent, @SuppressWarnings("unchecked") E... options) {
				return new DoubleCellEditor(parent);
			}
		},
		INTEGER {
			@Override
			public <E> CellEditor getCellEditor(Composite parent, @SuppressWarnings("unchecked") E... options) {
				return new IntegerCellEditor(parent);
			}
		},
		TEXT {
			@Override
			public <E> CellEditor getCellEditor(Composite parent, @SuppressWarnings("unchecked") E... options) {
				return new TextCellEditor(parent);
			}
		},
		BOOL {
			@Override
			public <E> CellEditor getCellEditor(Composite parent, @SuppressWarnings("unchecked") E... options) {
				return new CheckboxCellEditor(parent);
			}
		},
		CHOICE {
			@Override
			public <E> CellEditor getCellEditor(Composite parent, @SuppressWarnings("unchecked") E... options) {
				return new EditableComboBox<E>(parent, options);
			}
		};
		public abstract <E> CellEditor getCellEditor(Composite parent, @SuppressWarnings("unchecked") E... options);
	}
	public static abstract class ColumnHelper<T,V> {
		abstract V getValue(T target);
		abstract void setValue(T target, V value);
		Color bGColor(@SuppressWarnings("unused") T element) {
			return null;
		}
		String toolTip(@SuppressWarnings("unused") T element) {
			return null;
		}
	}
	
	private static abstract class OurEditingSupport extends EditingSupport {
		protected TableViewer viewer;
		protected RichBeanEditorPart editor;
		protected CellEditor cachedCellEditor = null;

		public OurEditingSupport(TableViewer viewer, RichBeanEditorPart editor) {
			super(viewer);
			this.viewer = viewer;
			this.editor = editor;
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
	
	private static final class EditableComboBox<T> extends ComboBoxViewerCellEditor {

		public EditableComboBox(Composite parent, final T[] choices) {
			super(parent);
			setContentProvider(new ArrayContentProvider());
			setLabelProvider(new LabelProvider());
			setInput(choices);
		}

		@Override
		protected Object doGetValue() {
			Object value = super.doGetValue();
			if (value == null) {
				value = ((CCombo) getViewer().getControl()).getText();
			}
			return value;
		}
	}

	private static final class DoubleCellEditor extends TextCellEditor {
		private double currentValue = 0;
		public DoubleCellEditor(final Composite parent) {
			super(parent);
		}

		@Override
		protected Object doGetValue() {
			Object value = super.doGetValue();
			try {
				this.currentValue = Double.valueOf(value.toString());
			} catch (NumberFormatException nfe) { //default to previous
			}
			return this.currentValue;
		}

		@Override
		protected void doSetValue(final Object value) {
			if (value == null) {
				super.doSetValue(String.valueOf(new Double(0)));
			} else {
				super.doSetValue(String.valueOf(value));
			}
		}
	}

	private static final class IntegerCellEditor extends TextCellEditor {
		private int currentValue = 0;
		public IntegerCellEditor(final Composite parent) {
			super(parent);
		}

		@Override
		protected Object doGetValue() {
			Object value = super.doGetValue();
			try {
				currentValue = Integer.parseInt(value.toString());
			} catch (NumberFormatException nfe) { //default to previous
			}
			return currentValue;
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
	
	private int width;
	private EditingSupport support;
	private CellEditor cellEditor;
	private CellLabelProvider labelProvider;
	private String outputFormat = "%s";
//	private TableViewer table;

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}
	public Column(int width, TableViewer table, RichBeanEditorPart rbEditor, @SuppressWarnings("unchecked") V... choices) {
		this(width, table,rbEditor,ColumnType.CHOICE.getCellEditor(table.getTable(), choices));
	}
	public Column(int width, TableViewer table, RichBeanEditorPart rbEditor, ColumnType type) {
		this(width, table,rbEditor,type.getCellEditor(table.getTable()));
	}
	private Column(int width, TableViewer table, RichBeanEditorPart rbEditor, CellEditor editor) {
//		this.table = table;
		this.width = width;
		this.cellEditor = editor;
		setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getStringValue(element);
			}
			@SuppressWarnings("unchecked")
			@Override
			public Color getBackground(Object element) {
				return getColour((T)element);
			}
			@SuppressWarnings("unchecked")
			@Override
			public String getToolTipText(Object element) {
				return getToolTip((T)element);
			}
		});
		setEditor(new OurEditingSupport(table, rbEditor) {
			@SuppressWarnings("unchecked")
			@Override
			protected Object getValue(Object element) {
				return getRealValue((T)element);
			}
			
			@Override
			protected CellEditor getOurCellEditor(Object element) {
				return cellEditor;
			}
			
			@SuppressWarnings("unchecked")
			@Override
			protected void setValue(Object element, Object value) {
				try {
					setNewValue((T)element, String.valueOf(value));
					super.setValue(element, value);
				} catch (IllegalArgumentException iae) {
					//ignore invalid entries - revert to previous value
				}
			}
		});
	}
	@SuppressWarnings("unchecked")
	protected String getStringValue(Object element) {
		try {
			return String.format(outputFormat, getRealValue((T)element));
		} catch (Exception e) {
			return String.format(outputFormat, String.valueOf(getRealValue((T)element)));
		}
	}
	abstract V getRealValue(T element);
	abstract void setNewValue(T element, String value);
	public int getWidth() {
		return this.width;
	}
	public EditingSupport getEditor() {
		return support;
	}
	public CellLabelProvider getLabelProvider() {
		return labelProvider;
	}
	public CellEditor getCellEditor() {
		return cellEditor;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setEditor(EditingSupport editor) {
		this.support = editor;
	}
	public void setLabelProvider(CellLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}
	protected Color getColour(@SuppressWarnings("unused") T element) {
		return null;
	}
	protected String getToolTip(@SuppressWarnings("unused") T element) {
		return null;
	}
	protected <E> void setInput(E[] options) {
		if (cellEditor instanceof EditableComboBox<?>) {
			((EditableComboBox<?>) cellEditor).setInput(options);
		}
//		cellEditor = ColumnType.CHOICE.getCellEditor(table.getTable(), options);
	}
//	protected <E> void setInput(E[] options) {
//		
//	}
}
