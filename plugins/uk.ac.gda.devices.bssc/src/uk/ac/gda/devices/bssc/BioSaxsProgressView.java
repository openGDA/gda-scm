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

package uk.ac.gda.devices.bssc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

public class BioSaxsProgressView extends ViewPart {

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
		
//		bioSaxsProgressViewer = new TreeViewer(bioSaxsProgressComposite, SWT.NONE);
//		Tree bioSaxsTree = bioSaxsProgressViewer.getTree();
//		bioSaxsTree.setLayout(new FillLayout());
		
		bioSaxsProgressViewer = new TableViewer(bioSaxsProgressComposite, SWT.NONE);
		Table bioSaxsTable = bioSaxsProgressViewer.getTable();
		bioSaxsTable.setLayout(new FillLayout());

		TableViewerColumn viewerColumn1 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column1 = viewerColumn1.getColumn();
		column1.setWidth(100);
		column1.setResizable(true);
		column1.setText("Session");

		TableViewerColumn viewerColumn2 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column2 = viewerColumn2.getColumn();
		column2.setWidth(140);
		column2.setResizable(true);
		column2.setText("Measurement Name");

		TableViewerColumn viewerColumn3 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column3 = viewerColumn3.getColumn();
		column3.setWidth(100);
		column3.setResizable(true);
		column3.setText("Well Position");

		TableViewerColumn viewerColumn4 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column4 = viewerColumn4.getColumn();
		column4.setWidth(100);
		column4.setResizable(true);
		column4.setText("Data Collection");

		TableViewerColumn viewerColumn5 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column5 = viewerColumn5.getColumn();
		column5.setWidth(100);
		column5.setResizable(true);
		column5.setText("Data Reduction");

		TableViewerColumn viewerColumn6 = new TableViewerColumn(bioSaxsProgressViewer, SWT.NONE);
		TableColumn column6 = viewerColumn6.getColumn();
		column6.setWidth(100);
		column6.setResizable(true);
		column6.setText("Data Analysis");

		bioSaxsProgressViewer.setContentProvider(new BioSaxsProgressContentProvider());
		bioSaxsProgressViewer.setLabelProvider(new BioSaxsProgressLabelProvider());
		bioSaxsTable.setHeaderVisible(true);

		createDummyModel();
		bioSaxsProgressViewer.setInput(bioSaxsProgressModel);
		populateDummyModel();
		bioSaxsProgressViewer.refresh();

		for (int i = 1; i < bioSaxsTable.getItemCount(); i++) {
			TableItem item = bioSaxsTable.getItem(i);
			ProgressBar bar = new ProgressBar(bioSaxsTable, SWT.NONE);
			bar.setSelection(i);
			TableEditor editor = new TableEditor(bioSaxsTable);
			editor.grabHorizontal = editor.grabVertical = true;
			editor.setEditor(bar, item, 3);
			editor.setEditor(bar, item, 4);
			editor.setEditor(bar, item, 5);
		}
	}

	private void createDummyModel() {
		bioSaxsProgressModel = new BioSaxsProgressModel();
	}

	private void populateDummyModel() {
		BioSaxsSession session = new BioSaxsSession("Session 1");
		for (int i = 0; i < 7; i++) {
			BioSaxsMeasurement bioSaxsMeasurement = new BioSaxsMeasurement(session, i, "Measurement # " + (i + 1));
			session.addMeasurement(bioSaxsMeasurement);
		}
		bioSaxsProgressModel.addSession(session);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
