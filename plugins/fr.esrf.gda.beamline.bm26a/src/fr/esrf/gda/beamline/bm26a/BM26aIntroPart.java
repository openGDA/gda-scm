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

package fr.esrf.gda.beamline.bm26a;

import gda.configuration.properties.LocalProperties;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.IntroPart;

import uk.ac.gda.client.experimentdefinition.components.ExperimentPerspective;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;

public class BM26aIntroPart extends IntroPart {

	@Override
	public void standbyStateChanged(boolean standby) {
		// TODO Auto-generated method stub
	}

	@Override
	public void createPartControl(Composite parent) {

		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		applyBackground(parent);
		createFullWidthMessage(parent, "Welcome to BM26a.", 24);
		createFullWidthMessage(parent,
				"You will collect data under experiment " + LocalProperties.get(LocalProperties.RCP_APP_VISIT), 20);
		createFullWidthMessage(parent, "Choose the type of experiment you wish to perform:", 20);
		createExperimentTypeButton(parent, "EXAFS\\XANES experiment", createImage("icons/link_obj.gif"), false);
		createExperimentTypeButton(parent, "XES experiment", createImage("icons/link_obj.gif"), true);
	}

	private void applyBackground(Composite parent) {
		Color color = getColor(parent);
		parent.setBackground(color);
		Image newImage = new Image(parent.getDisplay(), 1920, 1600);
		GC gc = new GC(newImage);
		gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.setForeground(color);
		gc.fillGradientRectangle(0, 0, 1920, 1400, true);
		gc.dispose();
		parent.setBackgroundImage(newImage);
	}

	protected Color getColor(Composite parent) {
		RGB rgbColor = new RGB(new Float(248), new Float(0.27), new Float(0.94));
		Color color = new Color(parent.getDisplay(), rgbColor);
		return color;
	}

	public static Image createImage(String imagePath) {
		try {
			URL url = Platform.getBundle(BM26aBeamlineActivator.PLUGIN_ID).getEntry(imagePath);

			// URL imageFileUrl = FileLocator.find(new URL("plugin:"+imagePath));
			return new Image(null, url.openStream());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected void createFullWidthMessage(Composite parent, String message, int fontSize) {
		Label theLabel = new Label(parent, SWT.LEFT);
		theLabel.setText(message);
		theLabel.setBackground(getColor(parent));

		GridData labelGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		labelGD.horizontalSpan = 2;
		labelGD.widthHint = 500;
		theLabel.setLayoutData(labelGD);

		FontData fd = theLabel.getFont().getFontData()[0];
		fd.setHeight(fontSize);
		Font newFont = new Font(parent.getDisplay(), fd);
		theLabel.setFont(newFont);
	}

	protected void createExperimentTypeButton(Composite parent, String experimentType, Image displayImage,
			final boolean isXES) {
		Button theLabel = new Button(parent, SWT.LEFT);
		theLabel.setText(experimentType);
		theLabel.setImage(displayImage);
		GridData labelGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		labelGD.widthHint = 100;
		labelGD.heightHint = 100;
		theLabel.setLayoutData(labelGD);

		theLabel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				ScanObjectManager.setXESOnlyMode(isXES);
				PlatformUI.getWorkbench().getIntroManager().closeIntro(BM26aIntroPart.this);
				try {
					for (IWorkbenchPage page : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()) {
						page.close();
					}
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().openPage(ExperimentPerspective.ID, null);
				} catch (WorkbenchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

}
