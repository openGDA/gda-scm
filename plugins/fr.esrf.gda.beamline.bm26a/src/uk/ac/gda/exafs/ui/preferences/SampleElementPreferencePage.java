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

package uk.ac.gda.exafs.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.api.reflection.IBeanService;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.ElementPosition;
import uk.ac.gda.beans.exafs.SampleElements;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.ElementPositionComposite;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import fr.esrf.gda.beamline.bm26a.BM26aBeamlineActivator;

/**
 *
 */
public class SampleElementPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{

	private static final Logger logger = LoggerFactory.getLogger(SampleElementPreferencePage.class);

	/**
	 *
	 */
	public static final String ID = "uk.ac.gda.exafs.preferences.sampleElementPreferencePage";

	/**
	 *
	 */
	public SampleElementPreferencePage() {
		setDescription("Enter the element configuration of the sample wheel.");
		setPreferenceStore(ExafsActivator.getDefault().getPreferenceStore()); // Does nothing we store elements as XML.
	}

	private VerticalListEditor elementPositions;

	private IBeanController controller;

	@Override
	protected Control createContents(Composite parent) {

        final Composite main = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        main.setLayout(layout);
        main.setFont(parent.getFont());

        this.elementPositions = new VerticalListEditor(main, SWT.NONE);
        elementPositions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		elementPositions.setNameField("name");
		elementPositions.setEditorClass(ElementPosition.class);
		elementPositions.setEditorUI(new ElementPositionComposite(elementPositions, SWT.NONE));
		elementPositions.setTemplateName("Position");
		elementPositions.setAdditionalFields(new String[]{"wheelPosition"});
		elementPositions.setColumnWidths(new int[]{120,90});
		elementPositions.setListWidth(400);


		try {
			IBeanService service = BM26aBeamlineActivator.getService(IBeanService.class);
			this.controller = service.createController(this, new SampleElements());
			controller.addValueListener(new ValueAdapter("Validate Elements Listener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					performValidate();
				}
			});
		} catch (Exception e) {
			logger.error("Cannot listen to value changes.",e);
		}

		performDefaults();

		return main;
	}

	@Override
	public Point doComputeSize() {
		return new Point(600,300);
	}

    protected void performValidate() {
       	final SampleElements elements = new SampleElements();
    	try {
    		controller.uiToBean();

 			final List<Integer> positions  = new ArrayList<Integer>(Math.max(1,elements.getElementPositions().size()));
		    for (ElementPosition pos : elements.getElementPositions()) {
				if (pos.getName()==null) {
					setErrorMessage("There is a reference sample without a name specified.");
					setValid(false);
					return;
				}
				if (pos.getPrincipleElement()==null) {
					setErrorMessage("The reference sample '"+pos.getName()+"' does not have an element supplied.");
					setValid(false);
					return;
				}
				if (pos.getWheelPosition()==null||pos.getWheelPosition()<1) {
					setErrorMessage("The wheel position for '"+pos.getName()+"' is zero.");
					setValid(false);
					return;
				}
				if (positions.contains(pos.getWheelPosition())) {
					setErrorMessage("The wheel position for '"+pos.getName()+"' is not unique.");
					setValid(false);
					return;
				}
				positions.add(pos.getWheelPosition());
			}

			setErrorMessage(null);
			setValid(true);

		} catch (Exception e) {
			logger.error("Cannot validate.", e);
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    @Override
	public boolean performOk() {

    	if (!isValid()) return false;
    	final SampleElements elements = new SampleElements();
    	try {
    		controller.uiToBean();

			XMLCommandHandler xmlCommandHandler = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(SampleElements.class);
			final SampleElements templateParameters = (SampleElements)xmlCommandHandler.getTemplateParameters();

			templateParameters.setElementPositions(elements.getElementPositions());

			XMLHelpers.saveBean(xmlCommandHandler.getTemplatePath(), templateParameters);

			ExafsActivator.getDefault().getPreferenceStore().firePropertyChangeEvent(ExafsPreferenceConstants.SAMPLE_ELEMENTS, null, null);

    	} catch (Exception e) {
			logger.error("Cannot get bean.", e);
		}
        return true;
    }

    @Override
    protected void performDefaults()  {
    	try {
			XMLCommandHandler xmlCommandHandler = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(SampleElements.class);
    		final SampleElements templateParameters = (SampleElements)xmlCommandHandler.getTemplateParameters();

			IBeanService service = BM26aBeamlineActivator.getService(IBeanService.class);
			this.controller = service.createController(this, BeansFactory.deepClone(templateParameters));

			controller.beanToUI();

    	} catch (Exception e) {
    		logger.error("Cannot get defaults.", e);
    	}
    	super.performDefaults();
    }

	/**
	 * @return Returns the elementPositions.
	 */
	public VerticalListEditor getElementPositions() {
		return elementPositions;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
