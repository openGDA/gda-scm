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

package uk.ac.gda.devices.bssc.views;

import java.util.List;

import gda.analysis.DataSet;
import gda.analysis.io.ScanFileHolderException;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.LabelledSlider;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSPlotView extends ViewPart {
	public static String ID = "uk.ac.gda.devices.bssc.views.BioSAXSPlotView";
	private IPlottingSystem plotting;
	private Logger logger = LoggerFactory.getLogger(BioSAXSPlotView.class);
	private String plotName;
	private Composite plotComposite;
	private Composite plotComposite2;
	private ISAXSProgress sampleProgress;
	private LabelledSlider slider;

	public BioSAXSPlotView() {
		try {
			this.plotting = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Cannot create a plotting system!", e);
		}
	}

	public void setName(String plotName) {
		this.plotName = plotName;
	}

	@Override
	public void createPartControl(Composite parent) {
		plotComposite = new Composite(parent, SWT.NONE);
		GridLayout gl_plotComposite = new GridLayout();
		plotComposite.setLayout(gl_plotComposite);

		Composite sliderComposite = new Composite(plotComposite, SWT.NONE);
		sliderComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		GridLayout sliderCompositeGL = new GridLayout();
		sliderCompositeGL.verticalSpacing = 10;
		sliderCompositeGL.marginWidth = 10;
		sliderCompositeGL.marginHeight = 10;
		sliderCompositeGL.horizontalSpacing = 10;
		sliderCompositeGL.numColumns = 2;

		Label lblFrames = new Label(sliderComposite, SWT.NONE);
		sliderComposite.setLayout(sliderCompositeGL);
		lblFrames.setText("Frames ");
		lblFrames.setLayoutData(new GridData(SWT.NONE));

		slider = new LabelledSlider(sliderComposite, SWT.HORIZONTAL);
		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if (slice == null)
				// return;
				// final int start = slider.getValue();
				// final Slice s = slice.getValue();
				// if (s.setPosition(start)) {
				// if (size != null)
				// size.setSelection(s.getNumSteps());
				// }
				// slice.setStart(start);
				// if (value != null)
				// value.setText(adata.getString(start));
				// reset.setEnabled(true);
			}
		});
		slider.setIncrements(1, 5);
		slider.setToolTipText("Starting position");

		GridData gd_slider = new GridData(SWT.NONE);
		gd_slider.widthHint = 222;
		slider.setLayoutData(gd_slider);

		plotting.createPlotPart(plotComposite, plotName, getViewSite().getActionBars(), PlotType.IMAGE, this);
		GridData plotGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		plotGD.horizontalSpan = 2;
		plotting.getPlotComposite().setLayoutData(plotGD);
	}

	@Override
	public void setFocus() {
		plotting.setFocus();
	}

	@Override
	public Object getAdapter(final Class clazz) {
		if (IPlottingSystem.class == clazz)
			return plotting;
		if (IToolPageSystem.class == clazz)
			return plotting;
		return super.getAdapter(clazz);
	}

	public void setPlot(ISAXSProgress sampleProgress) {
		this.sampleProgress = sampleProgress;

		// get the frames from the nexus file and set the slider
		String filePath = this.sampleProgress.getCollectionFileNames().get(0);

		HDF5Loader hdf5Loader = new HDF5Loader(filePath);
		System.out.println(hdf5Loader);
		try {
			DataHolder dataHolder = hdf5Loader.loadFile();
			List<ILazyDataset> dataSetList = dataHolder.getList();
			// for (ILazyDataset dataSet : dataSetList) {
			// System.out.println("dataSet.getName() : " + dataSet.getName());
			// System.out.println("dataSet size is : " + dataSet.getSize());
			// }
			ILazyDataset dataSet = (ILazyDataset) dataSetList.get(0);
			slider.setMinMax(0, dataSet.getSize(), "0", String.valueOf(dataSet.getSize()));
		} catch (ScanFileHolderException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
	}
}
