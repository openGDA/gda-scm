package uk.ac.gda.devices.bssc.ui;

import static org.junit.Assert.fail;
import gda.rcp.util.OSGIServiceRegister;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BioSaxsSampleProgress;
import uk.ac.gda.devices.bssc.beans.ISampleProgress;
import uk.ac.gda.devices.bssc.beans.IProgressModel;

public class BioSaxsProgressViewTest {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxsprogressperspective";
	private static final Logger logger = LoggerFactory
			.getLogger(BioSaxsProgressViewTest.class);
	private static BioSAXSProgressView view;
	private static IProgressModel model;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		model = new MyProgressModel();

		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(IProgressModel.class);
		modelReg.setService(model);
		modelReg.afterPropertiesSet();

		// populate model with sample values
		populateModel();
		
		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		view = (BioSAXSProgressView) window.getActivePage().showView(
				BioSAXSProgressView.ID);
		window.getActivePage().activate(view);
		ActionFactory.IWorkbenchAction maximizeAction = ActionFactory.MAXIMIZE
				.create(window);
		maximizeAction.run(); // Will maximize the active part

		window.getActivePage().activate(view);
	}

	private static void populateModel() {
		for (int i = 0; i < 20; i++) {
			ISampleProgress progress = new BioSaxsSampleProgress();
			progress.setSampleName("Sample : " + String.valueOf(i));
			progress.setCollectionStartTime(i);
			model.add(progress);
		}
	}

	@Test
	public void testMeasurementProgress() throws Exception {
		ObservableList items = (ObservableList) model.getItems();
		for (int i = 0; i < model.size(); i++) {
			items.add(model.get(i));
		}

		for (int i = 0; i < items.size(); i++) {
			for (int j = 0; j < 100; j++) {
				((BioSaxsSampleProgress) items.get(i)).setCollectionProgress(i
						+ j);
				((BioSaxsSampleProgress) items.get(i)).setReductionProgress(i
						+ j);
				((BioSaxsSampleProgress) items.get(i)).setAnalysisProgress(i
						+ j);
			}
			delay(50);
		}
	}

	@Test
	public void testMeasurementSelection() {
		// Add test here to assert that when a measurement is selected then the
		// correct editor part is opened
		fail("Not yet implemented");
	}

	@Test
	public void testAddMeasurementToModel() {
		ObservableList items = (ObservableList) model.getItems();
		
		BioSaxsSampleProgress newProgress = new BioSaxsSampleProgress();
		newProgress.setExperimentId(String.valueOf(21));
		items.add(new BioSaxsSampleProgress());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		waitForJobs();
		delay(20000);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.hideView(view);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	private static void delay(long waitTimeMillis) {
		Display display = Display.getCurrent();

		// If this is the UI thread,
		// then process input.

		if (display != null) {
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.update();
		}
		
		// Otherwise, perform a simple sleep.

		else {
			try {
				Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}

	/**
	 * Wait until all background tasks are complete.
	 */
	public static void waitForJobs() {
		while (!Job.getJobManager().isIdle())
			delay(1000);
	}

}

class MyProgressModel extends ArrayList<ISampleProgress> implements
		IProgressModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	WritableList items = new WritableList(new ArrayList<ISampleProgress>(),
			ISampleProgress.class);

	@Override
	public WritableList getItems() {
		return items;
	}

	@Override
	public void clearItems() {
		items.clear();
	}

	@Override
	public void addItems(List<ISampleProgress> bioSAXSSamples) {
		items.add(bioSAXSSamples);
	}
}
