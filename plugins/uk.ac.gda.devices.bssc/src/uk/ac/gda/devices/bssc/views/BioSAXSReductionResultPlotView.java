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
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.dawb.common.services.ServiceManager;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
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
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IErrorDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.diamond.scisoft.ncd.core.data.SaxsAnalysisPlotType;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSReductionResultPlotView extends ViewPart {
	public static final String ID = "uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView";
	private IPlottingSystem saxsPlottingSystem;
	private Logger logger = LoggerFactory.getLogger(BioSAXSProgressPlotView.class);
	private ILazyDataset lz;
	private ILazyDataset xAxisLazyDataSet;
	private IDataset xAxisDataSet;
	private String filePath;
	private String xAxisPath;
	private String dataSetPath;
	private final String resultDataSetPath = "/entry1/detector_result/data";
	private final String qDataSetPath = "/entry1/detector_result/q";
	private final String bGroundDataSetPath = "/entry1/detector_processing/BackgroundSubtraction/background";
	private final String sampleDataSetPath = "/entry1/detector_processing/Normalisation/data";
	private final String rgPath = "/entry1/detector_processing/guinierTestData/Rg";
	private final String invPath = "/entry1/detector_processing/Invariant/data";
	private IDataHolder dh;
	private int frame;
	private SliceObject sliceObject;
	private ISAXSProgress sampleProgress;
	private Composite plotComposite;
	private LabelledSlider slider;
	private SaxsAnalysisPlotType plotType;
	private SaxsJob saxsUpdateJob;
	public List<ITrace> cachedTraces;

	final Job loadReducedPlotJob = new Job("Load Plot Data") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ILoaderService loaderService = (ILoaderService) ServiceManager.getService(ILoaderService.class);

				dh = loaderService.getData(filePath, new ProgressMonitorWrapper(monitor));
				lz = dh.getLazyDataset(resultDataSetPath);

				String name = sampleProgress.getSampleName();
				sliceObject.setName(name);

				int[] shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, frame, 0 });
				sliceObject.setSliceStop(new int[] { 1, frame + 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);

				final IDataset dataSet = SliceUtils.getSlice(lz, sliceObject, monitor);
				List<IDataset> dataSetList = new ArrayList<IDataset>();
				dataSetList.add(dataSet.squeeze());

				// Get the x axis
				if (xAxisPath != null) {
					xAxisLazyDataSet = dh.getLazyDataset(xAxisPath);
					xAxisDataSet = SliceUtils.getSlice(xAxisLazyDataSet, new SliceObject(), monitor);
					xAxisDataSet.setName(xAxisLazyDataSet.getName());
				}
				// final IDataset qDataset = SliceUtils.getAxis(sliceObject, varMan, data, monitor);

				plot(xAxisDataSet, dataSetList);
			} catch (Exception e) {
				logger.error("Exception creating plot", e);
			} catch (Throwable e) {
				logger.error("Throwing exception creating plot", e);
			}
			return Status.OK_STATUS;
		}
	};

	final Job loadReducedWithSampleAndBgroundPlotJob = new Job("Load Plot Data") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				String name = sampleProgress.getSampleName();
				sliceObject.setName(name);

				ILoaderService loaderService = (ILoaderService) ServiceManager.getService(ILoaderService.class);
				dh = loaderService.getData(filePath, new ProgressMonitorWrapper(monitor));
				lz = dh.getLazyDataset(resultDataSetPath);
				int[] shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, frame, 0 });
				sliceObject.setSliceStop(new int[] { 1, frame + 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);
				final IDataset reducedDataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

				lz = dh.getLazyDataset(sampleDataSetPath);
				shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, frame, 0 });
				sliceObject.setSliceStop(new int[] { 1, frame + 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);
				final IDataset sampleDataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

				lz = dh.getLazyDataset(bGroundDataSetPath);
				shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, frame, 0 });
				sliceObject.setSliceStop(new int[] { 1, frame + 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);
				final IDataset backGroundDataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

				List<IDataset> dataSetList = new ArrayList<IDataset>();
				dataSetList.add(reducedDataSet.squeeze());
				dataSetList.add(sampleDataSet.squeeze());
				dataSetList.add(backGroundDataSet.squeeze());

				// Get the x axis
				if (xAxisPath != null) {
					xAxisLazyDataSet = dh.getLazyDataset(xAxisPath);
					xAxisDataSet = SliceUtils.getSlice(xAxisLazyDataSet, new SliceObject(), monitor);
					xAxisDataSet.setName(xAxisLazyDataSet.getName());
				}
				// final IDataset qDataset = SliceUtils.getAxis(sliceObject, varMan, data, monitor);

				plot(xAxisDataSet, dataSetList);
			} catch (Exception e) {
				logger.error("Exception creating plot", e);
			} catch (Throwable e) {
				logger.error("Throwing exception creating plot", e);
			}
			return Status.OK_STATUS;
		}
	};

	final Job loadRgPlotJob = new Job("Load Rg plot") {
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
				sliceObject.setSliceStart(new int[] { frame, 0 });
				sliceObject.setSliceStop(new int[] { frame + 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);

				final IDataset dataSet = SliceUtils.getSlice(lz, sliceObject, monitor);
				List<IDataset> dataSetList = new ArrayList<IDataset>();
				dataSetList.add(dataSet.squeeze());

				// Get the x axis
				if (xAxisPath != null) {
					xAxisLazyDataSet = dh.getLazyDataset(xAxisPath);
					xAxisDataSet = SliceUtils.getSlice(xAxisLazyDataSet, new SliceObject(), monitor);
					xAxisDataSet.setName(xAxisLazyDataSet.getName());
				} else {
					xAxisDataSet = null;
				}
				// final IDataset qDataset = SliceUtils.getAxis(sliceObject, varMan, data, monitor);

				plot(xAxisDataSet, dataSetList);
			} catch (Exception e) {
				logger.error("Exception creating plot", e);
			} catch (Throwable e) {
				logger.error("Throwing exception creating plot", e);
			}

			return Status.OK_STATUS;
		}
	};

	final Job sliceJob = new Job("Slice Plot Data") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ILoaderService loaderService = (ILoaderService) ServiceManager.getService(ILoaderService.class);
				dh = loaderService.getData(filePath, new ProgressMonitorWrapper(monitor));
				lz = dh.getLazyDataset(resultDataSetPath);
				int[] shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, 0, 0 });
				sliceObject.setSliceStop(new int[] { 1, 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);
				final IDataset reducedDataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

				lz = dh.getLazyDataset(sampleDataSetPath);
				shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, frame, 0 });
				sliceObject.setSliceStop(new int[] { 1, frame + 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);
				final IDataset sampleDataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

				lz = dh.getLazyDataset(bGroundDataSetPath);
				shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");
				sliceObject.setSliceStart(new int[] { 0, 0, 0 });
				sliceObject.setSliceStop(new int[] { 1, 1, shape[shape.length - 1] });
				sliceObject.setSliceStep(null);
				final IDataset backGroundDataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

				List<IDataset> dataSetList = new ArrayList<IDataset>();
				dataSetList.add(reducedDataSet.squeeze());
				dataSetList.add(sampleDataSet.squeeze());
				dataSetList.add(backGroundDataSet.squeeze());

				// Get the x axis
				if (xAxisPath != null) {
					xAxisLazyDataSet = dh.getLazyDataset(xAxisPath);
					xAxisDataSet = SliceUtils.getSlice(xAxisLazyDataSet, new SliceObject(), monitor);
					xAxisDataSet.setName(xAxisLazyDataSet.getName());
				}

				plot(xAxisDataSet, dataSetList);

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						slider.slider.setToolTipText(String.valueOf(frame));
					}
				});
			} catch (Exception e) {
				logger.error("Exception slicing plot", e);
			} catch (Throwable e) {
				logger.error("Exception slicing plot", e);
			}

			return Status.OK_STATUS;
		}
	};
	private Group grpData;
	private Group grpPlot;
	private Button logNorm;
	private Button logLog;
	private Button guinear;
	private Button porod;
	private Button kratky;
	private Button zimm;
	private Button debeyeBueche;

	public BioSAXSReductionResultPlotView() {
		try {
			this.saxsPlottingSystem = PlottingFactory.createPlottingSystem();
			sliceObject = new SliceObject();

			saxsUpdateJob = new SaxsJob();
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
				frame = slider.getValue();
				sliceJob.schedule();
			}
		});
		slider.setIncrements(1, 1);
		slider.setToolTipText("Starting position");

		GridData gd_slider = new GridData(SWT.NONE);
		gd_slider.widthHint = 178;
		slider.setLayoutData(gd_slider);

		grpData = new Group(sliderComposite, SWT.NONE);
		grpData.setLayout(new GridLayout(2, false));
		grpData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpData.setText("Data");

		final Button reduced = new Button(grpData, SWT.RADIO);
		reduced.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (reduced.getSelection()) {
					enablePlotGroup(true);
					slider.setEnabled(false);
					dataSetPath = resultDataSetPath;
					xAxisPath = qDataSetPath;
					loadReducedPlotJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		reduced.setText("Reduced");
		reduced.setSelection(true);

		final Button reducedWithSampleAndBackground = new Button(grpData, SWT.RADIO);
		reducedWithSampleAndBackground.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (reducedWithSampleAndBackground.getSelection()) {
					enablePlotGroup(true);
					slider.setEnabled(true);
					xAxisPath = null;
					loadReducedWithSampleAndBgroundPlotJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		reducedWithSampleAndBackground.setText("Reduced (With Sample and Background)");

		final Button rg = new Button(grpData, SWT.RADIO);
		rg.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (rg.getSelection()) {
					enablePlotGroup(false);
					dataSetPath = rgPath;
					xAxisPath = null;
					loadRgPlotJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		rg.setText("Rg");

		final Button invariant = new Button(grpData, SWT.RADIO);
		invariant.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (invariant.getSelection()) {
					enablePlotGroup(false);
					dataSetPath = invPath;
					xAxisPath = null;
					loadRgPlotJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		invariant.setText("Invariant");

		grpPlot = new Group(sliderComposite, SWT.NONE);
		grpPlot.setText("Plot");
		GridData gd_grpPlot = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_grpPlot.widthHint = 351;
		grpPlot.setLayoutData(gd_grpPlot);
		grpPlot.setLayout(new GridLayout(4, false));

		logNorm = new Button(grpPlot, SWT.RADIO);
		logNorm.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (logNorm.getSelection()) {
					xAxisPath = qDataSetPath;
					loadReducedPlotJob.schedule();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		logNorm.setText("Log/Norm");
		logNorm.setSelection(true);

		logLog = new Button(grpPlot, SWT.RADIO);
		logLog.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (logLog.getSelection()) {
					process(SaxsAnalysisPlotType.LOGLOG_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		logLog.setText("Log/Log");

		guinear = new Button(grpPlot, SWT.RADIO);
		guinear.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (guinear.getSelection()) {
					;
					process(SaxsAnalysisPlotType.GUINIER_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		guinear.setText("Guinear");

		porod = new Button(grpPlot, SWT.RADIO);
		porod.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (porod.getSelection()) {
					process(SaxsAnalysisPlotType.POROD_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		porod.setText("Porod");

		kratky = new Button(grpPlot, SWT.RADIO);
		kratky.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (kratky.getSelection()) {
					process(SaxsAnalysisPlotType.KRATKY_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		kratky.setText("Kratky");

		zimm = new Button(grpPlot, SWT.RADIO);
		zimm.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (zimm.getSelection()) {
					process(SaxsAnalysisPlotType.ZIMM_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		zimm.setText("Zimm");

		debeyeBueche = new Button(grpPlot, SWT.RADIO);
		debeyeBueche.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (debeyeBueche.getSelection()) {
					process(SaxsAnalysisPlotType.DEBYE_BUECHE_PLOT);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		debeyeBueche.setText("Debeye-Bueche");
		new Label(grpPlot, SWT.NONE);

		saxsPlottingSystem.createPlotPart(plotComposite, "My Plot Name", getViewSite().getActionBars(), PlotType.XY,
				this);

		GridData plotGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		plotGD.horizontalSpan = 2;
		saxsPlottingSystem.getPlotComposite().setLayoutData(plotGD);
	}

	private void cacheTraces(Collection<ITrace> traces) {
		cachedTraces = new ArrayList<ITrace>();

		for (ITrace trace : traces) {
			ILineTrace lineTrace = (ILineTrace) trace;
			AbstractDataset xTraceData = (AbstractDataset) lineTrace.getXData().clone();
			AbstractDataset yTraceData = (AbstractDataset) lineTrace.getYData().clone();
			ILineTrace cachedLineTrace = saxsPlottingSystem.createLineTrace(lineTrace.getName());
			cachedLineTrace.setData(xTraceData, yTraceData);
			cachedLineTrace.setTraceColor(lineTrace.getTraceColor());
			cachedTraces.add(cachedLineTrace);
		}
	}

	private void enablePlotGroup(boolean enabled) {
		grpPlot.setEnabled(enabled);
		logNorm.setEnabled(enabled);
		logLog.setEnabled(enabled);
		guinear.setEnabled(enabled);
		porod.setEnabled(enabled);
		kratky.setEnabled(enabled);
		zimm.setEnabled(enabled);
		debeyeBueche.setEnabled(enabled);
	}

	private void process(SaxsAnalysisPlotType pt) {
		if (saxsPlottingSystem == null || saxsPlottingSystem.getPlotComposite() == null)
			return;

		plotType = pt;

		final Collection<ITrace> traces = saxsPlottingSystem.getTraces(ILineTrace.class);
		if (traces != null && !traces.isEmpty()) {
			saxsPlottingSystem.setTitle(plotType.getName());
			Pair<String, String> axesTitles = plotType.getAxisNames();
			saxsPlottingSystem.getSelectedXAxis().setTitle(axesTitles.getFirst());
			saxsPlottingSystem.getSelectedYAxis().setTitle(axesTitles.getSecond());
			saxsUpdateJob.schedule(traces, plotType);
		} else {
			saxsPlottingSystem.clear();
		}
	}

	@Override
	public void setFocus() {
		saxsPlottingSystem.setFocus();
	}

	@Override
	public Object getAdapter(final Class clazz) {
		if (IPlottingSystem.class == clazz)
			return saxsPlottingSystem;
		if (IToolPageSystem.class == clazz)
			return saxsPlottingSystem;
		return super.getAdapter(clazz);
	}

	public void setPlot(final ISAXSProgress sampleProgress) {
		this.sampleProgress = sampleProgress;

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				filePath = sampleProgress.getReductionStatusInfo().getFileNames().get(0);
				xAxisPath = qDataSetPath;
				dataSetPath = resultDataSetPath;
				loadReducedPlotJob.schedule();
			}
		});
	}

	private boolean plot(IDataset x, List<IDataset> list) {
		saxsPlottingSystem.clear();
		if (list == null || list.isEmpty())
			return false;

		saxsPlottingSystem.createPlot1D(x, list, null);
		for (IAxis axis : saxsPlottingSystem.getAxes()) {
			if (axis.isYAxis()) {
				axis.setTitle("Y-Axis");
			}
		}

		cacheTraces(saxsPlottingSystem.getTraces());
		return true;
	}

	private class SaxsJob extends UIJob {

		private Collection<ITrace> traces;
		private SaxsAnalysisPlotType pt;

		public SaxsJob() {
			super("Process ");
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			saxsPlottingSystem.clear();

			ILineTrace lineTrace = (ILineTrace) cachedTraces.toArray()[0];
			if (!lineTrace.isUserTrace()) {
				return Status.CANCEL_STATUS;
			}

			IDataset xData = lineTrace.getXData();
			IDataset yData = lineTrace.getYData();
			if (xData == null || yData == null) {
				return Status.CANCEL_STATUS;
			}

			IDataset xErrors = null;
			IDataset yErrors = null;
			if (xData instanceof IErrorDataset && ((IErrorDataset) xData).hasErrors()) {
				xErrors = ((IErrorDataset) xData).getError().clone();
			}
			if (yData instanceof IErrorDataset && ((IErrorDataset) yData).hasErrors()) {
				yErrors = ((IErrorDataset) yData).getError().clone();
			}

			AbstractDataset xTraceData = (AbstractDataset) xData.clone();
			if (xErrors != null) {
				xTraceData.setError(xErrors);
			}
			AbstractDataset yTraceData = (AbstractDataset) yData.clone();
			if (yErrors != null) {
				yTraceData.setError(yErrors);
			}

			try {
				this.pt.process(xTraceData, yTraceData.squeeze());
			} catch (Throwable ne) {
				logger.error("Cannot process " + yTraceData.getName(), ne);
			}
			ILineTrace tr = saxsPlottingSystem.createLineTrace(lineTrace.getName());
			tr.setData(xTraceData, yTraceData);
			tr.setTraceColor(lineTrace.getTraceColor());
			tr.setErrorBarEnabled(true);
			tr.setErrorBarColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));

			saxsPlottingSystem.addTrace(tr);
			saxsPlottingSystem.repaint();

			return Status.OK_STATUS;
		}

		public void schedule(Collection<ITrace> traces, final SaxsAnalysisPlotType pt) {
			this.traces = traces;
			this.pt = pt;
			SaxsJob.this.setName("Process " + plotType.getName());
			schedule();
		}
	}
}
