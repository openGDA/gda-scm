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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.LabelledSlider;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSPlotView extends ViewPart {
	public static String ID = "uk.ac.gda.devices.bssc.views.BioSAXSPlotView";
	private IPlottingSystem plotting;
	private Logger logger = LoggerFactory.getLogger(BioSAXSPlotView.class);
	private String plotName;
	private Composite plotComposite;
	private ISAXSProgress sampleProgress;
	private LabelledSlider slider;
	private SliceObject sliceObject;
	protected ILazyDataset lz;
	protected String dataSetPath;
	protected IDataHolder dh;
	private String filePath;
	private int startValue;

	public BioSAXSPlotView() {
		try {
			this.plotting = PlottingFactory.createPlottingSystem();
			
			sliceObject = new SliceObject();
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
				final Job loadJob = new Job("Load plot Data") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							ILoaderService loaderService = (ILoaderService) ServiceManager.getService(ILoaderService.class);
							dataSetPath = "/entry1/instrument/detector/data";
							dh = loaderService.getData(filePath, new ProgressMonitorWrapper(monitor));
							lz = dh.getLazyDataset(dataSetPath);
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									startValue = slider.getValue();
								}
							});
							
							sliceObject.setName(sampleProgress.getSampleName());
							sliceObject.setFullShape(lz.getShape());
							sliceObject.setShapeMessage("");
							sliceObject.setPath(filePath);
							sliceObject.setSliceStart(new int[] { 0, startValue, 0, 0 });
							sliceObject.setSliceStop(new int[] { 1, 59, 1679, 1475 });
							sliceObject.setSliceStep(new int[] { 1, 49, 1 ,1 });

							final IDataset dataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

							List<IDataset> dataSetList = new ArrayList<IDataset>();
							dataSetList.add(dataSet);
							plot(dataSetList);


						} catch (Exception e) {
							logger.error("Exception creating 2D plot", e);
						} catch (Throwable e) {
							logger.error("Throwing exception creating 2D plot", e);
						}

						return Status.OK_STATUS;
					}
				};
				loadJob.schedule();
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

	public void setPlot(final ISAXSProgress sampleProgress) {
		this.sampleProgress = sampleProgress;

		// get the frames from the nexus file and set the slider
		filePath = this.sampleProgress.getCollectionFileNames().get(0);

		final Job loadJob = new Job("Load plot Data") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ILoaderService loaderService = (ILoaderService) ServiceManager.getService(ILoaderService.class);
					dataSetPath = "/entry1/instrument/detector/data";
					dh = loaderService.getData(filePath, new ProgressMonitorWrapper(monitor));
					lz = dh.getLazyDataset(dataSetPath);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							slider.setMinMax(0, lz.getShape()[1], "0", String.valueOf(lz.getShape()[1]));
						}
					});
					
					sliceObject.setName(sampleProgress.getSampleName());
					sliceObject.setFullShape(lz.getShape());
					sliceObject.setShapeMessage("");
					sliceObject.setPath(filePath);
					sliceObject.setSliceStart(new int[] { 0, 10, 0, 0 });
					sliceObject.setSliceStop(new int[] { 1, 59, 1679, 1475 });
					sliceObject.setSliceStep(new int[] { 1, 49, 1 ,1 });

					final IDataset dataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

					List<IDataset> dataSetList = new ArrayList<IDataset>();
					dataSetList.add(dataSet);
					plot(dataSetList);


				} catch (Exception e) {
					logger.error("Exception creating 2D plot", e);
				} catch (Throwable e) {
					logger.error("Throwing exception creating 2D plot", e);
				}

				return Status.OK_STATUS;
			}
		};
		loadJob.schedule();
	}

	private boolean plot(List<IDataset> list) {

		// setPlotting(true);

		plotting.clear();
		if (list == null || list.isEmpty())
			return false;

		if (list.get(0).getShape().length == 1) {
			plotting.createPlot1D(null, list, null);
		} else if (list.get(0).getShape().length == 2) {
			// Average the images, then plot
			AbstractDataset added = Maths.add(list, list.size() > 1);
			plotting.createPlot2D(added, null, null);
		}

		return true;

	}

	// private void update(DatacollectionData dc) throws Throwable {
	//
	// final String corPath =
	// DataCollectionGalleryInfo.getCorrectedPath(dc.getIspybDatacollection().getXtalsnapshotfullpath1());
	// final File dlsPath = new File(corPath);
	// if (dlsPath.exists()) {
	// try {
	// if (label.getImage()!=null) label.getImage().dispose();
	// Image image = new Image(null, corPath);
	// label.setImage(image);
	// currentImageData = image.getImageData();
	// setPlotting(false);
	// scaleImage();
	//
	// } catch (Throwable ne) {
	//
	// final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
	// final IDataHolder holder = service.getData(corPath, new IMonitor.Stub());
	// if (holder!=null && holder.getDataset(0)!=null) {
	// plotting.updatePlot2D(holder.getDataset(0), null, dlsPath.getName(), new NullProgressMonitor());
	// }
	// setPlotting(true);
	// }
	//
	// }
	// }

	// private void setPlotting(boolean isPlotting) {
	//
	// GridUtils.setVisible(label, !isPlotting);
	// GridUtils.setVisible(plotting.getPlotComposite(), isPlotting);
	//
	// if (isPlotting) {
	// if (getViewSite().getActionBars().getToolBarManager().isEmpty()) {
	// for (IContributionItem item : this.plottingActions) {
	// getViewSite().getActionBars().getToolBarManager().add(item);
	// }
	// }
	// } else {
	// getViewSite().getActionBars().getToolBarManager().removeAll();
	// }
	// getViewSite().getActionBars().getToolBarManager().update(true);
	// label.getParent().layout();
	// }
}
