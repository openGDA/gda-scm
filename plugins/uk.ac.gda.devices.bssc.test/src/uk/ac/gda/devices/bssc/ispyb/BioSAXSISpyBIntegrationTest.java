package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import gda.rcp.util.OSGIServiceRegister;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import uk.ac.gda.devices.bssc.ui.BioSAXSProgressView;

public class BioSAXSISpyBIntegrationTest extends BioSAXSScriptTest {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxsprogressperspective";
	private static BioSAXSProgressView view;
	private static IObservableList model;
	private static BioSAXSISPyB bioSAXSISPyB;
	private static BioSAXSProgressController controller;
	private static List<ISAXSDataCollection> iSpyBSAXSDataCollections;
	private static List<ISAXSProgress> progressList;
	private static long blsessionId;
	private static long experimentId;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		model = new WritableList();
		bioSAXSISPyB = new MockBioSAXSISPyB();

		controller = new BioSAXSProgressController();
		controller.setModel(model);
		controller.setISpyBAPI(bioSAXSISPyB);
		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(BioSAXSProgressController.class);
		modelReg.setService(controller);
		modelReg.afterPropertiesSet();

		String visit = "nt20-12";
		blsessionId = bioSAXSISPyB.getSessionForVisit(visit);
		// create an EXPERIMENT in ISpyB
		experimentId = bioSAXSISPyB.createExperiment(blsessionId, "test",
				"TEMPLATE", "test");

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

	@Test
	public void testUpdateProgressFromISpyB() throws SQLException {
		iSpyBSAXSDataCollections = controller.getDataCollectionsFromISPyB();
		progressList = controller.loadModel(iSpyBSAXSDataCollections);
		model.addAll(progressList);

		Job updateJob = new Job("") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (ISAXSDataCollection saxsDataCollection : bioSAXSISPyB
							.getSAXSDataCollections(0)) {
						long saxsDataCollectionId = saxsDataCollection.getId();

						long bufferBeforeId = bioSAXSISPyB.createBufferRun(
								saxsDataCollectionId, 1.0, 20.0f, 20.0f, 10.0,
								10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
								getFilename(1), defaultDataPath);

						Thread.sleep(3000);

						long dataCollectionId1 = bioSAXSISPyB
								.createSaxsDataCollection(experimentId,
										(short) 0, (short) 1, (short) 1,
										"Sample1", (short) 0, (short) 1,
										(short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
										10.0, "viscosity");

						Thread.sleep(3000);

						// create a buffer after entry in ISpyB
						// long bufferAfterId =
						// bioSAXSISPyB.createBufferRun(dataCollectionId1,
						// 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0,
						// 1.0, 1.0,
						// 1.0, getFilename(3),
						// defaultDataPath);
						ISpyBStatusInfo statusInfo = new ISpyBStatusInfo();
						statusInfo.setStatus(ISpyBStatus.COMPLETE);
						statusInfo.setProgress(100);
						bioSAXSISPyB.setDataCollectionStatus(
								saxsDataCollectionId, statusInfo);

						Thread.sleep(3000);

						bioSAXSISPyB.createDataReduction(saxsDataCollectionId);

						Thread.sleep(3000);

						bioSAXSISPyB.createDataAnalysis(saxsDataCollectionId);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return Status.OK_STATUS;
			}
		};
		updateJob.schedule();
	}

	@Test
	public void testProgressSelection() {
		// Add test here to assert that when a measurement is selected then the
		// correct editor part is opened
		fail("Not yet implemented");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		waitForJobs();
		delay(60000);
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
