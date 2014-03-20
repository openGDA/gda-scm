package uk.ac.gda.devices.bssc.ui;

import static org.junit.Assert.fail;
import gda.rcp.util.OSGIServiceRegister;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.beans.BioSAXSProgress;
import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatus;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;

public class BioSaxsProgressViewTest {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxsprogressperspective";
	private static BioSAXSProgressView view;
	private static IObservableList model;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		model = new WritableList();

		BioSAXSProgressController controller = new BioSAXSProgressController();
		controller.setModel(model);
		
		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(BioSAXSProgressController.class);
		modelReg.setService(controller);
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
		// Will maximize the active part
		maximizeAction.run();

		window.getActivePage().activate(view);
	}

	private static void populateModel() {
		for (int i = 0; i < 7; i++) {
			ISpyBStatusInfo collectionStatusDetails = new ISpyBStatusInfo();
			collectionStatusDetails.setStatus(ISpyBStatus.NOT_STARTED);
			collectionStatusDetails.setProgress(0);

			ISpyBStatusInfo reductionStatusDetails = new ISpyBStatusInfo();
			reductionStatusDetails.setStatus(ISpyBStatus.NOT_STARTED);
			reductionStatusDetails.setProgress(0);

			ISpyBStatusInfo analysisStatusDetails = new ISpyBStatusInfo();
			analysisStatusDetails.setStatus(ISpyBStatus.NOT_STARTED);
			analysisStatusDetails.setProgress(0);

			ISAXSProgress progress = new BioSAXSProgress(i, i, "Sample " + i,
					collectionStatusDetails, reductionStatusDetails,
					analysisStatusDetails);;

			model.add(progress);
		}
	}

	@Test
	public void testProgress() throws Exception {
		ObservableList items = (ObservableList) model;

		for (int i = 0; i < items.size(); i++) {

			ISAXSProgress progress = (ISAXSProgress) items.get(i);

			ISpyBStatusInfo collectionStatusDetails = new ISpyBStatusInfo();
			collectionStatusDetails.setStatus(ISpyBStatus.RUNNING);
			collectionStatusDetails.setProgress(33);
			progress.setCollectionStatusInfo(collectionStatusDetails);
			delay(1000);

			collectionStatusDetails.setStatus(ISpyBStatus.RUNNING);
			collectionStatusDetails.setProgress(66);
			progress.setCollectionStatusInfo(collectionStatusDetails);
			delay(1000);

			collectionStatusDetails.setStatus(ISpyBStatus.RUNNING);
			collectionStatusDetails.setProgress(100);
			progress.setCollectionStatusInfo(collectionStatusDetails);
			delay(1000);

			ISpyBStatusInfo reductionStatusDetails = new ISpyBStatusInfo();
			reductionStatusDetails.setStatus(ISpyBStatus.RUNNING);
			reductionStatusDetails.setProgress(100);
			progress.setReductionStatusInfo(reductionStatusDetails);

			ISpyBStatusInfo analysisStatusDetails = new ISpyBStatusInfo();
			analysisStatusDetails.setStatus(ISpyBStatus.COMPLETE);
			analysisStatusDetails.setProgress(100);
			progress.setAnalysisStatusInfo(analysisStatusDetails);
		}
		delay(1000);
	}

	@Test
	public void testProgressSelection() {
		// Add test here to assert that when a measurement is selected then the
		// correct editor part is opened
		fail("Not yet implemented");
	}

	@Test
	public void testAddProgressModel() {
		ObservableList items = (ObservableList) model;

		ISpyBStatusInfo collectionStatusInfo = new ISpyBStatusInfo();
		collectionStatusInfo.setProgress(0);
		ISpyBStatusInfo reductionStatusInfo = new ISpyBStatusInfo();
		collectionStatusInfo.setProgress(0);
		ISpyBStatusInfo analysisStatusInfo = new ISpyBStatusInfo();
		collectionStatusInfo.setProgress(0);

		ISAXSProgress newProgress = new BioSAXSProgress(model.size() + 1, 
				model.size() + 1, "New Sample "
						+ model.size() + 1, collectionStatusInfo,
				reductionStatusInfo, analysisStatusInfo);
		;

		newProgress.setCollectionStatusInfo(collectionStatusInfo);
		newProgress.setAnalysisStatusInfo(reductionStatusInfo);
		newProgress.setAnalysisStatusInfo(analysisStatusInfo);

		items.add(newProgress);
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


