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
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.dawnsci.plotting.api.filter.IFilterDecorator;
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

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.diamond.scisoft.ncd.core.data.SaxsAnalysisPlotType;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSReductionResultPlotView extends ViewPart {
	public static final String ID = "uk.ac.gda.devices.bssc.views.BioSAXSReductionResultPlotView";

	private Logger logger = LoggerFactory.getLogger(BioSAXSProgressPlotView.class);
	
	// data related 
	private ILazyDataset lz;
	private ILazyDataset xAxisLazyDataSet;
	private IDataset xAxisDataSet;
	private List<IDataset> dataSetList;                                      // retain state of which data is currently displayed
	private IDataHolder dh;
	private SliceObject sliceObject;
	
	// plotting system related 
	private IPlottingSystem saxsPlottingSystem;
	private SaxsAnalysisPlotType plotType;
	private IFilterDecorator plotTypeFilterDecorator;
	
	// paths, mainly nexus
	private String filePath;
	private String xAxisPath;
	private String dataSetPath;
	private final String resultDataSetPath = "/entry1/detector_result/data";
	private final String qDataSetPath = "/entry1/detector_result/q";
	private final String bGroundDataSetPath = "/entry1/detector_processing/BackgroundSubtraction/background";
	private final String sampleDataSetPath = "/entry1/detector_processing/Normalisation/data";
	private final String rgPath = "/entry1/detector_processing/guinierTestData/Rg";
	private final String invPath = "/entry1/detector_processing/Invariant/data";
	
	private int frame;
	private ISAXSProgress sampleProgress;
	private Composite plotComposite;
	private LabelledSlider slider;


	final Job loadReducedPlotJob = new Job("Load Plot Data") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ILoaderService loaderService = (ILoaderService) ServiceManager.getService(ILoaderService.class);
				logger.trace("loadReducedPlotJob.schedule->run");
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
				dataSetList = new ArrayList<IDataset>();
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

				dataSetList = new ArrayList<IDataset>();
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
				dataSetList = new ArrayList<IDataset>();
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

				dataSetList = new ArrayList<IDataset>();
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
	private Button guinier;
	private Button porod;
	private Button kratky;
	private Button zimm;
	private Button debyeBueche;

	public BioSAXSReductionResultPlotView() {
	    try {
	    	this.saxsPlottingSystem = PlottingFactory.createPlottingSystem();
	    	plotTypeFilterDecorator = PlottingFactory.createFilterDecorator(this.saxsPlottingSystem); // Filter decorator used to provide Guinier, Kratky etc transforms
	    	sliceObject             = new SliceObject();

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

	final AbstractPlottingFilter saxsPlotTransformLN = new AbstractPlottingFilter() {
	    @Override
	    public int getRank() { return 1; }
	    @Override
	    protected IDataset[] filter(IDataset x, IDataset y) {
	    	return SaxsAnalysisPlotType.LOGNORM_PLOT.process((AbstractDataset) x, (AbstractDataset) y);
	    }
	};		

	final AbstractPlottingFilter saxsPlotTransformLL = new AbstractPlottingFilter() {
	    @Override
	    public int getRank() { return 1; }
	    @Override
	    protected IDataset[] filter(IDataset x, IDataset y) {
	    	return SaxsAnalysisPlotType.LOGLOG_PLOT.process((AbstractDataset) x, (AbstractDataset) y);
	    }
	};		

	final AbstractPlottingFilter saxsPlotTransformGN = new AbstractPlottingFilter() {
	    @Override
	    public int getRank() { return 1; }
	    @Override
	    protected IDataset[] filter(IDataset x, IDataset y) {
	    	return SaxsAnalysisPlotType.GUINIER_PLOT.process((AbstractDataset) x, (AbstractDataset) y);
	    }
	};		
	
	final AbstractPlottingFilter saxsPlotTransformPD = new AbstractPlottingFilter() {
	    @Override
	    public int getRank() { return 1; }
	    @Override
	    protected IDataset[] filter(IDataset x, IDataset y) {
	    	return SaxsAnalysisPlotType.POROD_PLOT.process((AbstractDataset) x, (AbstractDataset) y);
	    }
	};		
	
	final AbstractPlottingFilter saxsPlotTransformKY = new AbstractPlottingFilter() {
	    @Override
	    public int getRank() { return 1; }
	    @Override
	    protected IDataset[] filter(IDataset x, IDataset y) {
	    	return SaxsAnalysisPlotType.KRATKY_PLOT.process((AbstractDataset) x, (AbstractDataset) y);
	    }
	};		
	
	final AbstractPlottingFilter saxsPlotTransformZM = new AbstractPlottingFilter() {
	    @Override
	    public int getRank() { return 1; }
	    @Override
	    protected IDataset[] filter(IDataset x, IDataset y) {
	    	return SaxsAnalysisPlotType.ZIMM_PLOT.process((AbstractDataset) x, (AbstractDataset) y);
	    }
	};	
	
	final AbstractPlottingFilter saxsPlotTransformDB = new AbstractPlottingFilter() {
	    @Override
	    public int getRank() { return 1; }
	    @Override
	    protected IDataset[] filter(IDataset x, IDataset y) {
	    	return SaxsAnalysisPlotType.DEBYE_BUECHE_PLOT.process((AbstractDataset) x, (AbstractDataset) y);
	    }
	};		
	
	// new Label(grpPlot, SWT.NONE);                                    // include for marginally nicer alignment of radio buttons
	logNorm = new Button(grpPlot, SWT.RADIO);
	logNorm.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (logNorm.getSelection()) {	
				    logger.trace("BioSAXSReductionResultPlotView.widgetSelected:LN");
			    	plotType = SaxsAnalysisPlotType.LOGNORM_PLOT;  // a bit redundant since current filter is already record of current transform state 
				    plotTypeFilterDecorator.clear();
				    plotTypeFilterDecorator.addFilter(saxsPlotTransformLN);
				    setPlot(sampleProgress); 
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
			    	logger.trace("BioSAXSReductionResultPlotView.widgetSelected:LL");
			    	plotType = SaxsAnalysisPlotType.LOGLOG_PLOT;  // a bit redundant since current filter is already record 			    	
			    	plotTypeFilterDecorator.clear();
			    	plotTypeFilterDecorator.addFilter(saxsPlotTransformLL);
				    setPlot(sampleProgress);  			    	
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		logLog.setText("Log/Log");

		guinier = new Button(grpPlot, SWT.RADIO);
		guinier.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (guinier.getSelection()) {
					logger.trace("BioSAXSReductionResultPlotView.widgetSelected:GN");
			    	plotType = SaxsAnalysisPlotType.GUINIER_PLOT;  // a bit redundant since current filter is already record of current transform state 			    
				    plotTypeFilterDecorator.clear();
				    plotTypeFilterDecorator.addFilter(saxsPlotTransformGN);
				    setPlot(sampleProgress); 	
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		guinier.setText("Guinier");

		porod = new Button(grpPlot, SWT.RADIO);
		porod.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (porod.getSelection()) {
					logger.trace("BioSAXSReductionResultPlotView.widgetSelected:PD");
			    	plotType = SaxsAnalysisPlotType.POROD_PLOT;  // a bit redundant since current filter is already record of current transform state 				    
				    plotTypeFilterDecorator.clear();
				    plotTypeFilterDecorator.addFilter(saxsPlotTransformPD);
				    setPlot(sampleProgress); 

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
					logger.trace("BioSAXSReductionResultPlotView.widgetSelected:KY");
			    	plotType = SaxsAnalysisPlotType.KRATKY_PLOT;  // a bit redundant since current filter is already record of current transform state 						    
				    plotTypeFilterDecorator.clear();
				    plotTypeFilterDecorator.addFilter(saxsPlotTransformKY);
				    setPlot(sampleProgress); 
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
					logger.trace("BioSAXSReductionResultPlotView.widgetSelected:ZM");
			    	plotType = SaxsAnalysisPlotType.ZIMM_PLOT;  // a bit redundant since current filter is already record of current transform state				    
				    plotTypeFilterDecorator.clear();
				    plotTypeFilterDecorator.addFilter(saxsPlotTransformZM);
				    setPlot(sampleProgress); 
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		zimm.setText("Zimm");

		debyeBueche = new Button(grpPlot, SWT.RADIO);
		debyeBueche.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (debyeBueche.getSelection()) {
					logger.trace("BioSAXSReductionResultPlotView.widgetSelected:DB");
			    	plotType = SaxsAnalysisPlotType.DEBYE_BUECHE_PLOT;  // a bit redundant since current filter is already record of current transform state 				    
				    plotTypeFilterDecorator.clear();
				    plotTypeFilterDecorator.addFilter(saxsPlotTransformDB);
				    setPlot(sampleProgress); 
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		debyeBueche.setText("Debye-Bueche");
				
		
		saxsPlottingSystem.createPlotPart(plotComposite, "My Plot Name", getViewSite().getActionBars(), PlotType.XY, this); // move this up so default selection code below can be put in widgetDefaultSelected?
		plotType = SaxsAnalysisPlotType.LOGNORM_PLOT;                       // remove recording which plotType is active? FilterDecorator remembers state instead
        plotTypeFilterDecorator.clear();                                    // set initial transform after createPlotPart
        plotTypeFilterDecorator.addFilter(saxsPlotTransformLN);

		GridData plotGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		plotGD.horizontalSpan = 2;
		saxsPlottingSystem.getPlotComposite().setLayoutData(plotGD);
	}

	private void enablePlotGroup(boolean enabled) {
		grpPlot.setEnabled(enabled);
		logNorm.setEnabled(enabled);
		logLog.setEnabled(enabled);
		guinier.setEnabled(enabled);
		porod.setEnabled(enabled);
		kratky.setEnabled(enabled);
		zimm.setEnabled(enabled);
		debyeBueche.setEnabled(enabled);
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
		logger.trace("plot: clear, createPlot1D, set axes titles");
		saxsPlottingSystem.clear();
		if (list == null || list.isEmpty())
			return false;

		saxsPlottingSystem.createPlot1D(x, list, null);
		for (IAxis axis : saxsPlottingSystem.getAxes()) {
			if (axis.isYAxis()) {
				axis.setTitle(plotType.getAxisNames().getSecond() );
			} else {
				axis.setTitle(plotType.getAxisNames().getFirst() );
			}			
		}

		return true;
	}


}
