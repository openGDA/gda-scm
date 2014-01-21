package uk.ac.gda.devices.bssc.ui;

import static org.junit.Assert.fail;
import gda.rcp.util.OSGIServiceRegister;

import java.util.ArrayList;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

import uk.ac.gda.devices.bssc.BioSaxsProgress;
import uk.ac.gda.devices.bssc.ISampleProgress;
import uk.ac.gda.devices.bssc.ISampleProgressCollection;
import uk.ac.gda.devices.bssc.ispyb.SampleInfo;

public class BioSaxsProgressViewTest {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxsprogressperspective";
	private static final Logger logger = LoggerFactory
			.getLogger(BioSaxsProgressViewTest.class);
	private static BioSAXSProgressView view;
	private static ISampleProgressCollection model;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		model = new MyISampleProgressCollection();
		populateModel();

		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(ISampleProgressCollection.class);
		modelReg.setService(model);
		modelReg.afterPropertiesSet();

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
			model.add(new SampleInfo());
		}
	}

	// @Test
	// public void testMeasurementProgress() throws Exception {
	// ObservableList items = (ObservableList) model.getItems();
	// for (int i = 0; i < model.size(); i++) {
	// items.add(model.get(i));
	// }
	//
	// for (int i = 0; i < model.size(); i++) {
	// for (int j = 0; j < 100; j++) {
	// ((BioSaxsProgress) items.get(i)).setCollectionProgress(i + j);
	// ((BioSaxsProgress) items.get(i)).setReductionProgress(i + j);
	// ((BioSaxsProgress) items.get(i)).setAnalysisProgress(i + j);
	// }
	// delay(50);
	// }
	// //
	// Assert.assertEquals("149.0",view.viewer.getTable().getItem(50).getText(0));
	// }

	@Test
	public void testMeasurementSelection() {
		// Add test here to assert that when a measurement is selectecd then the
		// correct editor part is opened
		fail("Not yet implemented");
	}

	@Test
	public void testAddMeasurementToModel() {
		ObservableList items = (ObservableList) model.getItems();
		items.add(new SampleInfo());
		view.getViewer().refresh();
		Assert.assertEquals(21, ((TableViewer) view.getViewer()).getTable()
				.getItemCount());
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

class MyISampleProgressCollection extends ArrayList<SampleInfo> implements
		ISampleProgressCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	WritableList items = new WritableList(new ArrayList<SampleInfo>(),
			ISampleProgress.class);

	@Override
	public WritableList getItems() {
		return items;
	}

}
