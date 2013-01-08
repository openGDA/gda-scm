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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;

import uk.ac.gda.ClientManager;
import uk.ac.gda.beans.exafs.ElementPosition;
import uk.ac.gda.beans.exafs.SampleElements;
import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.beans.exafs.bm26a.CryostatParameters;
import uk.ac.gda.beans.exafs.bm26a.CustomParameter;
import uk.ac.gda.beans.exafs.bm26a.CustomXYZParameter;
import uk.ac.gda.beans.exafs.bm26a.FurnaceParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleStageParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.CryostatComposite;
import uk.ac.gda.exafs.ui.composites.CustomParameterComposite;
import uk.ac.gda.exafs.ui.composites.CustomXYZParameterComposite;
import uk.ac.gda.exafs.ui.composites.FurnaceComposite;
import uk.ac.gda.exafs.ui.composites.RoomTemperatureComposite;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.microreactor.MicroreactorParametersComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.exafs.ui.preferences.SampleElementPreferencePage;
import uk.ac.gda.richbeans.components.FieldComposite.NOTIFY_TYPE;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

import com.swtdesigner.SWTResourceManager;

public class SampleParametersUIEditor extends RichBeanEditorPart implements IPropertyChangeListener {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SampleParametersUIEditor.class);

	private Composite mainComp;
	private ComboWrapper temperatureControl;
	private ScrolledComposite scrolledComposite;
	private SpinnerWrapper sampleWheelPosition;
	private TextWrapper descriptions;
	private TextWrapper name;
	private RoomTemperatureComposite sampleStageParameters;
	private CryostatComposite cryostatParameters;
	private FurnaceComposite furnaceParameters;
	private MicroreactorParametersComposite microreactorParameters;
	private VerticalListEditor customParameters;
	private VerticalListEditor customXYZParameters;

	private CLabel elementName;

	private SampleElements sampleElements;

	private SelectionAdapter selectionListener;

	private Link elementLabel;

	private Composite complexTypesTemp;

	private StackLayout stackLayoutTemp;

	private Composite blankTempComposite;

	public SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);

		ExafsActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	protected String getRichEditorTabText() {
		return "Sample";
	}

	@Override
	public void createPartControl(final Composite parent) {

		parent.setLayout(new FillLayout());

		this.scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.mainComp = new Composite(scrolledComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		mainComp.setLayout(gridLayout);

		final Composite composite = new Composite(mainComp, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);

		final Group experimentSample = new Group(composite, SWT.NONE);
		experimentSample.setText("Sample Details");
		GridData experimentSampleGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		experimentSampleGridData.minimumWidth = 350;
		experimentSample.setLayoutData(experimentSampleGridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		experimentSample.setLayout(gridLayout);

		final Label sampleNameLabel = new Label(experimentSample, SWT.NONE);
		sampleNameLabel.setText("Sample name");

		name = new TextWrapper(experimentSample, SWT.BORDER);
		name.setTextLimit(5000);
		final GridData gd_name = new GridData(SWT.FILL, SWT.CENTER, true, false);
		name.setLayoutData(gd_name);	

		final Label descriptionLabel = new Label(experimentSample, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		descriptionLabel.setText("Description");

		descriptions = new TextWrapper(experimentSample, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		final GridData gd_descriptions = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_descriptions.heightHint = 73;
		descriptions.setLayoutData(gd_descriptions);

		final Group temperatureChoice = new Group(composite, SWT.NONE);
		temperatureChoice.setText("Sample Environment");
		final GridData gd_tempControl = new GridData(SWT.FILL, SWT.CENTER, true, false);
		temperatureChoice.setLayoutData(gd_tempControl);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		temperatureChoice.setLayout(gridLayout);

		temperatureControl = new ComboWrapper(temperatureChoice, SWT.READ_ONLY);
		temperatureControl.select(0);
		if (ScanObjectManager.isXESOnlyMode()) {
			temperatureControl.setItems(SampleParameters.SAMPLE_ENV_XES);
		} else {
			temperatureControl.setItems(SampleParameters.SAMPLE_ENV);
		}
		final GridData gd_tempType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		temperatureControl.setLayoutData(gd_tempType);

		this.complexTypesTemp = new Composite(temperatureChoice, SWT.NONE);
		this.stackLayoutTemp = new StackLayout();
		complexTypesTemp.setLayout(stackLayoutTemp);
		complexTypesTemp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		blankTempComposite = new Composite(complexTypesTemp, SWT.NONE);

		if (!ScanObjectManager.isXESOnlyMode()) {
			this.sampleStageParameters = new RoomTemperatureComposite(complexTypesTemp, SWT.NONE);
			sampleStageParameters.setEditorClass(SampleStageParameters.class);

			this.cryostatParameters = new CryostatComposite(complexTypesTemp, SWT.NONE);
			cryostatParameters.setEditorClass(CryostatParameters.class);

			this.furnaceParameters = new FurnaceComposite(complexTypesTemp, SWT.NONE);
			furnaceParameters.setEditorClass(FurnaceParameters.class);
			
//			this.microreactorParameters = new MicroreactorParametersComposite(complexTypesTemp, SWT.NONE);
//			microreactorParameters.setEditorClass(MicroreactorParameters.class);
		}

		this.customXYZParameters = new VerticalListEditor(complexTypesTemp, SWT.NONE);
		customXYZParameters.setNameField("deviceName");
		customXYZParameters.setEditorClass(CustomXYZParameter.class);
		customXYZParameters.setEditorUI(new CustomXYZParameterComposite(customXYZParameters, SWT.NONE));
		customXYZParameters.setTemplateName("Custom XYZ Parameter");

		this.customParameters = new VerticalListEditor(complexTypesTemp, SWT.NONE);
		customParameters.setNameField("deviceName");
		customParameters.setEditorClass(CustomParameter.class);
		customParameters.setEditorUI(new CustomParameterComposite(customParameters, SWT.NONE));
		customParameters.setTemplateName("Custom Parameter");

		if (!ScanObjectManager.isXESOnlyMode()) {
			final Group wheelPos = new Group(composite, SWT.NONE);
			wheelPos.setText("Reference Sample");
			wheelPos.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			final GridLayout wheelPosLay = new GridLayout();
			wheelPosLay.numColumns = 3;
			wheelPos.setLayout(wheelPosLay);

			final Composite left = new Composite(wheelPos, SWT.NONE);
			left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			final GridLayout leftLay = new GridLayout();
			leftLay.numColumns = 4;
			left.setLayout(leftLay);

			final Label wheelPositionLabel = new Label(left, SWT.NONE);
			wheelPositionLabel.setText("Position");

			sampleWheelPosition = new SpinnerWrapper(left, SWT.BORDER);
			sampleWheelPosition.setMinimum(1);
			sampleWheelPosition.setMaximum(24);
			sampleWheelPosition.setNotifyType(NOTIFY_TYPE.ALWAYS);

			this.elementLabel = new Link(left, SWT.NONE);
			elementLabel.setText("  <a>Name</a> ");
			elementLabel.setToolTipText("Open the preferences to edit the sample elements.");
			this.selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openPreferences();
				}
			};
			elementLabel.addSelectionListener(selectionListener);

			this.elementName = new CLabel(left, SWT.NONE);
			elementName.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD | SWT.ITALIC));
			elementName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			elementName.setText("                            ");

			sampleWheelPosition.addValueListener(new ValueAdapter("sampleWheelPositionListener") {

				@Override
				public void valueChangePerformed(final ValueEvent e) {

					if (ClientManager.isTestingMode()) {
						updateElementLabel();
					} else {
						// Having some issues with executing updaing the element label straight away so shunted to
						// invoke later.
						getSite().getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								updateElementLabel();
							}
						});
					}
				}

			});
		}

		scrolledComposite.setContent(mainComp);
		mainComp.layout();
		scrolledComposite.setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	protected void openPreferences() {
		PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(getSite().getShell(),
				SampleElementPreferencePage.ID, null, null);
		if (pref != null)
			pref.open();
	}

	private SampleElements getDefaultWheelPositions() {
		// We take the element positions from the default sample params.
		try {
			XMLCommandHandler xmlCommandHandler = ExperimentBeanManager.INSTANCE
					.getXmlCommandHandler(SampleElements.class);
			return (SampleElements) xmlCommandHandler.getTemplateParameters();
		} catch (Throwable ne) {
			logger.error("Cannot read sample elements", ne);
			return null;
		}
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		temperatureControl.addValueListener(new ValueAdapter("sampleEnvironmentListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateTemperatureType(temperatureControl.getSelectionIndex());
			}
		});

		super.linkUI(isPageChange);

		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateElementLabel();
			}
		});

		// Now the data will have one of the complex types so we can init the envType
		int index = initTempType();
		temperatureControl.select(index);
	}

	public void updateElementLabel() {

		if (sampleElements == null)
			this.sampleElements = getDefaultWheelPositions();
		if (sampleElements == null)
			return;
		final int index = (Integer) sampleWheelPosition.getValue();
		final List<ElementPosition> pos = sampleElements.getElementPositions();
		for (ElementPosition elementPosition : pos) {
			if (elementPosition.getWheelPosition() == index) {
				elementName.setText(elementPosition.getName());
				return;
			}
		}
		elementName.setText("<No element>");
	}

	private int initTempType() {
		final SampleParameters params = (SampleParameters) editingBean;
		final List<String> items = Arrays.asList(temperatureControl.getItems());
		final int index = items.indexOf(params.getSampleEnvironment());
		updateTemperatureType(index);
		return index;
	}

	private void updateTemperatureType(final int index) {

		final SampleParameters params = (SampleParameters) editingBean;
		Control control = null;

		if (ScanObjectManager.isXESOnlyMode()) {
			switch (index) {
			case 0:
				control = blankTempComposite;
				break;
			case 1:
				control = customXYZParameters;
				break;
			case 2:
				control = customParameters;
				break;
			default:
				break;
			}
		} else {
			Object val = null;

			switch (index) {
			case 0:
				control = blankTempComposite;
				val = "none";
				break;
			case 1:
				control = sampleStageParameters;
				val = getRoomTemperatureParameters().getValue();
				if (val == null) {
					params.getRoomTemperatureParameters();
				}
				if (val == null) {
					val = new SampleStageParameters();
				}
				if (params.getRoomTemperatureParameters() == null) {
					params.setRoomTemperatureParameters((SampleStageParameters) val);
				}
				if (getRoomTemperatureParameters().getValue() == null) {
					getRoomTemperatureParameters().setEditingBean(val);
				}
				break;
			case 2:
				control = cryostatParameters;
				val = getCryostatParameters().getValue();
				if (val == null)
					params.getCryostatParameters();
				if (val == null)
					val = new CryostatParameters();
				if (params.getCryostatParameters() == null)
					params.setCryostatParameters((CryostatParameters) val);
				if (getCryostatParameters().getValue() == null)
					getCryostatParameters().setEditingBean(val);

				break;
			case 3:
				control = furnaceParameters;
				val = getFurnaceParameters().getValue();
				if (val == null)
					params.getFurnaceParameters();
				if (val == null)
					val = new FurnaceParameters();
				if (params.getFurnaceParameters() == null)
					params.setFurnaceParameters((FurnaceParameters) val);
				if (getFurnaceParameters().getValue() == null)
					getFurnaceParameters().setEditingBean(val);
				break;
//			case 4:
//				control = microreactorParameters;
//				val = getMicroreactorParameters().getValue();
//				if (val == null)
//					params.getMicroreactorParameters();
//				if (val == null)
//					val = new MicroreactorParameters();
//				if (params.getMicroreactorParameters() == null)
//					params.setMicroreactorParameters((MicroreactorParameters) val);
//				if (getMicroreactorParameters().getValue() == null)
//					getMicroreactorParameters().setEditingBean(val);
//				break;
				
			case 5:
				control = customXYZParameters;
				break;
			case 6:
				control = customParameters;
				break;
			default:
				break;
			}
		}

		stackLayoutTemp.topControl = control;
		complexTypesTemp.layout();
	}

	@Override
	public void setFocus() {
	}

	public TextWrapper getDescriptions() {
		return descriptions;
	}

	public TextWrapper getName() {
		return name;
	}

	public SpinnerWrapper getSampleWheelPosition() {
		return sampleWheelPosition;
	}

	public RoomTemperatureComposite getRoomTemperatureParameters() {
		return sampleStageParameters;
	}

	public CryostatComposite getCryostatParameters() {
		return cryostatParameters;
	}

	public FurnaceComposite getFurnaceParameters() {
		return furnaceParameters;
	}
	
	public MicroreactorParametersComposite getMicroreactorParameters(){
		return microreactorParameters;
	}

	public VerticalListEditor getCustomParameters() {
		return customParameters;
	}

	public VerticalListEditor getCustomXYZParameters() {
		return customXYZParameters;
	}

	public ComboWrapper getSampleEnvironment() {
		return temperatureControl;
	}

	public String _testGetElementName() {
		return this.elementName.getText();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (ExafsPreferenceConstants.SAMPLE_ELEMENTS.equals(event.getProperty())) {
			sampleElements = getDefaultWheelPositions();
		}
	}

	@Override
	public void dispose() {
		ExafsActivator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		if (elementLabel != null && !elementLabel.isDisposed())
			this.elementLabel.removeSelectionListener(selectionListener);
		super.dispose();
	}

}
