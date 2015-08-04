/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import java.net.URL;

import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.ACTIVE_MODE;
import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.beans.exafs.bm26a.XYZStageParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ui.composites.XYZStageComposite;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class SampleParametersUIEditor extends RichBeanEditorPart {

	private ScrolledComposite topComposite;
	private FieldComposite description1;
	private FieldComposite description2;
	private FieldComposite name;
	private XYZStageComposite xyzStageComposite;
	private XYZStageComposite cryoStageComposite;
	private Composite blankStageComposite;
	private Composite composite;
	private StackLayout stageLayout;
	private ComboWrapper stage;
	private SampleParameters bean;
	private ExpandableComposite sampleStageExpandableComposite;
	private Composite stageComp;
	private Group grpStage;
	private Group grpStageParameters;

	public SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		bean = (SampleParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "SampleParametersEditor";
	}

	@Override
	public void createPartControl(final Composite parent) {

		parent.setLayout(new FillLayout());
		topComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		topComposite.setExpandHorizontal(true);
		topComposite.setExpandVertical(true);

		Composite container = new Composite(topComposite, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));

		topComposite.setContent(container);
		topComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Label label = new Label(composite, SWT.NONE);
		label.setSize(37, 17);
		label.setText("Filename");

		name = new TextWrapper(composite, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		name.setSize(234, 21);

		new Label(composite, SWT.NONE);

		label = new Label(composite, SWT.NONE);
		label.setSize(72, 17);
		label.setText("Sample description");
		description1 = new TextWrapper(composite, SWT.NONE);
		description1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		description1.setSize(234, 21);

		new Label(composite, SWT.NONE);

		label = new Label(composite, SWT.NONE);
		label.setSize(72, 17);
		label.setText("Additional comments");

		description2 = new TextWrapper(composite, SWT.NONE);
		description2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		description2.setSize(234, 21);

		new Label(composite, SWT.NONE);

		sampleStageExpandableComposite = new ExpandableComposite(composite, SWT.NONE);
		sampleStageExpandableComposite.setText("Sample Stage");
		sampleStageExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		createStage();
		// stage
		ExpansionAdapter stageExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if (!stage.getValue().toString().equals("none"))
					sampleStageExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(sampleStageExpandableComposite);
				linkuiForDynamicLoading(false);
			}
		};
		sampleStageExpandableComposite.addExpansionListener(stageExpansionListener);

		if (bean != null && !bean.getStage().toString().equals("none")) {
			sampleStageExpandableComposite.setExpanded(true);
			linkuiForDynamicLoading(false);
			updateStageType();
		}

	}

	public void linkuiForDynamicLoading(@SuppressWarnings("unused") final boolean isPageChange) {
		try {
			switchState(false);
			beanToUI();
			switchState(true);
		} catch (Exception e) {
			logger.warn("Unexpected error merging bean!", e);
		}
	}

	public void createStage() {
		if (stageComp == null) {

			stageComp = new Composite(sampleStageExpandableComposite, SWT.NONE);
			GridLayout gridLayout_2 = new GridLayout();
			gridLayout_2.numColumns = 2;
			stageComp.setLayout(gridLayout_2);

			grpStage = new Group(stageComp, SWT.NONE);
			grpStage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 2;
			grpStage.setLayout(gridLayout);

			Label label = new Label(grpStage, SWT.NONE);
			label.setSize(37, 17);
			label.setText("Stage");

			stage = new ComboWrapper(grpStage, SWT.READ_ONLY);
			stage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			stage.setSize(234, 27);
			stage.setItems(new String[] { "none", "xyzStage", "cryoStage"});

			grpStageParameters = new Group(grpStage, SWT.NONE);
			stageLayout = new StackLayout();
			grpStageParameters.setLayout(stageLayout);
			grpStageParameters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			blankStageComposite = new Composite(grpStageParameters, SWT.NONE);

			xyzStageComposite = new XYZStageComposite(grpStageParameters, SWT.NONE, "samplex", "sampley", "samplez");
			xyzStageComposite.setVisible(true);
			xyzStageComposite.setEditorClass(XYZStageParameters.class);
			xyzStageComposite.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);

			cryoStageComposite = new XYZStageComposite(grpStageParameters, SWT.NONE, "cryox", "cryoy", "cryoz");
			cryoStageComposite.setVisible(true);
			cryoStageComposite.setEditorClass(XYZStageParameters.class);
			cryoStageComposite.setActiveMode(ACTIVE_MODE.ACTIVE_ONLY);


			Control[] children = grpStageParameters.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control control = children[i];
				if (control instanceof FieldBeanComposite) {
					((FieldBeanComposite) control).addValueListener(new ValueListener() {
						@Override
						public void valueChangePerformed(ValueEvent e) {
							Object source = e.getSource();
							if (!(source instanceof ScaleBox)) {
								bean.setStage(stage.getItem(stage.getSelectionIndex()));
								updateStageType();
								linkUI(false);
							}
						}

						@Override
						public String getValueListenerName() {
							return null;
						}
					});
				}
			}

			stage.addValueListener(new ValueListener() {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					bean.setStage(stage.getItem(stage.getSelectionIndex()));
					updateStageType();
					linkUI(false);
				}

				@Override
				public String getValueListenerName() {
					return "stage";
				}
			});

			sampleStageExpandableComposite.setClient(stageComp);
		}
	}
	
	@Override
	public void linkUI(final boolean isPageChange) {
		try {
			GridUtils.startMultiLayout(topComposite);
			super.linkUI(isPageChange);
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	private void updateStageType() {
		switch (stage.getSelectionIndex()) {
		case 0:
			stageLayout.topControl = blankStageComposite;
			break;
		case 1:
			stageLayout.topControl = xyzStageComposite;
			break;
		case 2:
			stageLayout.topControl = cryoStageComposite;
			break;
		}
		GridUtils.layoutFull(grpStageParameters);
	}

	@Override
	public void setFocus() {
	}

	public FieldComposite getName() {
		return name;
	}

	public FieldComposite getDescription1() {
		return description1;
	}

	public FieldComposite getDescription2() {
		return description2;
	}

	public FieldComposite getStage() {
		return stage;
	}

	public XYZStageComposite getXyzStageParameters() {
		return xyzStageComposite;
	}

	public XYZStageComposite getCryoStageParameters() {
		return cryoStageComposite;
	}

//	public XYZStageComposite getXYZStageComposite() {
//		return xyzStageComposite;
//	}
//
//	public XYZStageComposite getCryoStageComposite() {
//		return cryoStageComposite;
//	}
}
