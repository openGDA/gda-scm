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

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.diamond.scisoft.ncd.rcp.plotting.tools.SaxsAnalysisDelegate;
import uk.ac.diamond.scisoft.ncd.utils.SaxsAnalysisPlotType;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSReductionResultPlotView extends ViewPart {
	public static final String ID = "uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView";
	private IPlottingSystem plotting;
	private Logger logger = LoggerFactory.getLogger(BioSAXSProgressPlotView.class);
	private ILazyDataset lz;
	private ILazyDataset xAxisLazyDataSet;
	private String dataSetPath = "/entry1/detector_result/data";
	private String xAxisPath = "/entry1/detector_result/q";
	private String backGroundPath = "/entry1/detector_processing/BackgroundSubtraction/data";
	private String samplePath = "/entry1/detector_processing/SectorIntegration/data";
	private String rgPath = "/entry1/detector_processing/GuinierPlot/Rg";
	private IDataHolder dh;
	private int frame;
	private SliceObject sliceObject;
	private ISAXSProgress sampleProgress;
	private Composite plotComposite;
	private LabelledSlider slider;
	private BioSAXSReductionResultPlotView ref;

	public BioSAXSReductionResultPlotView() {
		try {
			ref = this;
			this.plotting = PlottingFactory.createPlottingSystem();
			sliceObject = new SliceObject();
		} catch (Exception e) {
			logger.error("Cannot create a plotting system!", e);
		}
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
		sliderCompositeGL.numColumns = 4;

		Label lblFrames = new Label(sliderComposite, SWT.NONE);
		sliderComposite.setLayout(sliderCompositeGL);
		lblFrames.setText("Frame ");
		lblFrames.setLayoutData(new GridData(SWT.NONE));

		slider = new LabelledSlider(sliderComposite, SWT.HORIZONTAL);
		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// frame = slider.getValue();
				// sliceJob.schedule();
			}
		});
		slider.setIncrements(1, 1);
		slider.setToolTipText("Starting position");

		GridData gd_slider = new GridData(SWT.NONE);
		gd_slider.widthHint = 178;
		slider.setLayoutData(gd_slider);

		Group grpPlot = new Group(sliderComposite, SWT.NONE);
		grpPlot.setText("Plot");
		GridData gd_grpPlot = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_grpPlot.widthHint = 351;
		grpPlot.setLayoutData(gd_grpPlot);
		grpPlot.setLayout(new GridLayout(4, false));

		final Button q = new Button(grpPlot, SWT.RADIO);
		q.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (q.getSelection()) {
//					loadJob.schedule();
					Job loadJob = new LoadPlotJob("/dls/b21/data/2014/cm4976-1/processing/results_b21-5790_detector_280214_180858.nxs", dataSetPath, xAxisPath);
					loadJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		q.setText("q");
		q.setSelection(true);

		final Button logLog = new Button(grpPlot, SWT.RADIO);
		logLog.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (logLog.getSelection()) {
					SaxsAnalysisDelegate delegate = new SaxsAnalysisDelegate(/*
																			 * SaxsAnalysisPlotType.LOGLOG_PLOT,
																			 * plotting
																			 */);
					delegate.setLinkedPlottingSystem(plotting);
					delegate.process(SaxsAnalysisPlotType.LOGLOG_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		logLog.setText("Log/Log");

		final Button guinear = new Button(grpPlot, SWT.RADIO);
		guinear.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (guinear.getSelection()) {
					SaxsAnalysisDelegate delegate = new SaxsAnalysisDelegate(SaxsAnalysisPlotType.GUINIER_PLOT,
							plotting);
					delegate.setLinkedPlottingSystem(plotting);
					delegate.process(SaxsAnalysisPlotType.GUINIER_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		guinear.setText("Guinear");

		final Button porod = new Button(grpPlot, SWT.RADIO);
		porod.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (porod.getSelection()) {
					SaxsAnalysisDelegate delegate = new SaxsAnalysisDelegate(SaxsAnalysisPlotType.POROD_PLOT, plotting);
					delegate.setLinkedPlottingSystem(plotting);
					delegate.process(SaxsAnalysisPlotType.POROD_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		porod.setText("Porod");

		final Button kratky = new Button(grpPlot, SWT.RADIO);
		kratky.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (kratky.getSelection()) {
					SaxsAnalysisDelegate delegate = new SaxsAnalysisDelegate(SaxsAnalysisPlotType.KRATKY_PLOT, plotting);
					delegate.setLinkedPlottingSystem(plotting);
					delegate.process(SaxsAnalysisPlotType.KRATKY_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		kratky.setText("Kratky");

		final Button zimm = new Button(grpPlot, SWT.RADIO);
		zimm.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (zimm.getSelection()) {
					SaxsAnalysisDelegate delegate = new SaxsAnalysisDelegate(SaxsAnalysisPlotType.ZIMM_PLOT, plotting);
					delegate.setLinkedPlottingSystem(plotting);
					delegate.process(SaxsAnalysisPlotType.ZIMM_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		zimm.setText("Zimm");

		final Button debeyeBueche = new Button(grpPlot, SWT.RADIO);
		debeyeBueche.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (debeyeBueche.getSelection()) {
					SaxsAnalysisDelegate delegate = new SaxsAnalysisDelegate(SaxsAnalysisPlotType.DEBYE_BUECHE_PLOT,
							plotting);
					delegate.setLinkedPlottingSystem(plotting);
					delegate.process(SaxsAnalysisPlotType.DEBYE_BUECHE_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		debeyeBueche.setText("Debeye-Bueche");
		new Label(grpPlot, SWT.NONE);

		Group grpData = new Group(sliderComposite, SWT.NONE);
		grpData.setLayout(new GridLayout(2, false));
		grpData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpData.setText("Data");

		final Button reduced = new Button(grpData, SWT.RADIO);
		reduced.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (reduced.getSelection()) {
					Job loadJob = new LoadPlotJob("/dls/b21/data/2014/cm4976-1/processing/results_b21-5790_detector_280214_180858.nxs", dataSetPath, xAxisPath);
					loadJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		reduced.setText("Reduced");
		reduced.setSelection(true);

		final Button backGround = new Button(grpData, SWT.RADIO);
		backGround.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (backGround.getSelection()) {
					Job loadJob = new LoadPlotJob("/dls/b21/data/2014/cm4976-1/processing/results_b21-5790_detector_280214_180858.nxs", backGroundPath, null);
					loadJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		backGround.setText("Background");

		final Button sample = new Button(grpData, SWT.RADIO);
		sample.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (sample.getSelection()) {
					Job loadJob = new LoadPlotJob("/dls/b21/data/2014/cm4976-1/processing/results_b21-5790_detector_280214_180858.nxs", samplePath, null);
					loadJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		sample.setText("Sample");

		final Button rg = new Button(grpData, SWT.RADIO);
		rg.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (rg.getSelection()) {
					Job loadJob = new LoadPlotJob("/dls/b21/data/2014/cm4976-1/processing/results_b21-5790_detector_280214_180858.nxs", rgPath, null);
					loadJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		rg.setText("Rg");

		plotting.createPlotPart(plotComposite, "My Plot Name", getViewSite().getActionBars(), PlotType.XY, this);

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

	public void setPlot(final ISAXSProgress sampleProgress) {
		this.sampleProgress = sampleProgress;
		// filePath = this.sampleProgress.getCollectionFileNames();
		//
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
//				loadJob.schedule();
				Job loadJob = new LoadPlotJob("/dls/b21/data/2014/cm4976-1/processing/results_b21-5790_detector_280214_180858.nxs", dataSetPath, xAxisPath);
				loadJob.schedule();
			}
		});
	}

//	final Job loadJob = new Job("Load Plot Data") {
//		@Override
//		protected IStatus run(IProgressMonitor monitor) {
//			return null;
//		}
//	};

	private boolean plot(IDataset x, List<IDataset> list) {
		plotting.clear();
		if (list == null || list.isEmpty())
			return false;

		plotting.createPlot1D(x, list, null);

		return true;
	}

	private class LoadPlotJob extends Job {
		private String filePath;
		private String dataSetLocation;
		private String xAxisLocation;

		public LoadPlotJob(String filePath, String dataSetLocation, String xAxisLocation) {
			super("Load Plot");
			this.filePath = filePath;
			this.dataSetLocation = dataSetLocation;
			this.xAxisLocation = xAxisLocation;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ILoaderService loaderService = (ILoaderService) ServiceManager.getService(ILoaderService.class);

				dh = loaderService.getData(filePath, new ProgressMonitorWrapper(monitor));
				lz = dh.getLazyDataset(dataSetPath);

				String name = sampleProgress.getSampleName();
				sliceObject.setName(name);

				int[] shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, frame, 0 });
				sliceObject.setSliceStop(new int[] { 1, frame + 1, shape[2] });
				sliceObject.setSliceStep(null);

				final IDataset dataSet = SliceUtils.getSlice(lz, sliceObject, monitor);
				List<IDataset> dataSetList = new ArrayList<IDataset>();
				dataSetList.add(dataSet.squeeze());

				// Get the q axis
				xAxisLazyDataSet = dh.getLazyDataset(xAxisLocation);
				final IDataset xAxisDataSet = SliceUtils.getSlice(xAxisLazyDataSet, new SliceObject(), monitor);
				xAxisDataSet.setName(xAxisLazyDataSet.getName());
				// final IDataset qDataset = SliceUtils.getAxis(sliceObject, varMan, data, monitor);

				plot(xAxisDataSet, dataSetList);
			} catch (Exception e) {
				logger.error("Exception creating plot", e);
			} catch (Throwable e) {
				logger.error("Throwing exception creating plot", e);
			}

			return Status.OK_STATUS;
		}

	}
}
