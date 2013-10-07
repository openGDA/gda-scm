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

import org.csstudio.swt.widgets.figures.TankFigure;
import org.csstudio.swt.widgets.figures.ThermometerFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class BSSCStatus extends ViewPart {

	public static final String ID = "uk.ac.gda.devices.bssc.ui.BSSCStatus"; //$NON-NLS-1$
	private Label label_1;
	private Label label_2;
	private Label label_3;
	private Label label_4;
	private Label label_0;

	public BSSCStatus() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		GridLayout gl_container = new GridLayout(5, true);
		gl_container.marginWidth = 15;
		gl_container.verticalSpacing = 15;
		gl_container.horizontalSpacing = 35;
		container.setLayout(gl_container);
		{
			//use LightweightSystem to create the bridge between SWT and draw2D
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);		
			
			final ThermometerFigure thermo = new ThermometerFigure();
			
			thermo.setRange(-20, 60);
			thermo.setLoLevel(-50);
			thermo.setLoloLevel(-80);
			thermo.setHiLevel(60);
			thermo.setHihiLevel(80);
			thermo.setShowMarkers(false);
			thermo.setMajorTickMarkStepHint(20);
			
			lws.setContents(thermo);		}
		{
			//use LightweightSystem to create the bridge between SWT and draw2D
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);		
			
			final ThermometerFigure thermo = new ThermometerFigure();
			
			thermo.setRange(-20, 60);
			thermo.setLoLevel(-50);
			thermo.setLoloLevel(-80);
			thermo.setHiLevel(60);
			thermo.setHihiLevel(80);
			thermo.setShowMarkers(false);
			thermo.setMajorTickMarkStepHint(20);
			
			lws.setContents(thermo);
		}
		
		{
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);
			
			final TankFigure tank = new TankFigure();
			
			tank.setRange(0, 100);
			tank.setLoLevel(-50);
			tank.setLoloLevel(-80);
			tank.setHiLevel(60);
			tank.setHihiLevel(80);
			tank.setShowMarkers(false);
			tank.setMajorTickMarkStepHint(20);
			
			lws.setContents(tank);
		}
		{
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);
			
			final TankFigure tank = new TankFigure();
			
			tank.setRange(0, 100);
			tank.setLoLevel(-50);
			tank.setLoloLevel(-80);
			tank.setHiLevel(60);
			tank.setHihiLevel(80);
			tank.setShowMarkers(false);
			tank.setMajorTickMarkStepHint(20);
			
			lws.setContents(tank);
		}
		{
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);
			
			final TankFigure tank = new TankFigure();
			
			tank.setRange(0, 100);
			tank.setLoLevel(-50);
			tank.setLoloLevel(-80);
			tank.setHiLevel(60);
			tank.setHihiLevel(80);
			tank.setShowMarkers(false);
			tank.setMajorTickMarkStepHint(20);
			
			lws.setContents(tank);
		}
		
		label_0 = new Label(container, SWT.CENTER);
		label_0.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		label_0.setText("Exposure");
		label_1 = new Label(container, SWT.CENTER);
		label_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		label_1.setText("Storage");
		label_2 = new Label(container, SWT.CENTER);
		label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_2.setText("Detergent");
		label_3 = new Label(container, SWT.CENTER);
		label_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_3.setText("Water");
		label_4 = new Label(container, SWT.CENTER);
		label_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_4.setText("Waste");
		
		{
			Button btnScanPark = new Button(container, SWT.NONE);
			btnScanPark.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
			btnScanPark.setText("Scan + Park");
		}
		new Label(container, SWT.NONE);
		{
			Button btnLoad = new Button(container, SWT.NONE);
			btnLoad.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
			btnLoad.setText("Load");
		}


		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
}