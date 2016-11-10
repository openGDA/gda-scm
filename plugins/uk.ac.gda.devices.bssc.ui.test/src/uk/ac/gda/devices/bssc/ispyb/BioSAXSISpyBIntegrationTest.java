package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.factory.FactoryException;
import gda.rcp.util.OSGIServiceRegister;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;
import uk.ac.gda.devices.bssc.ispyb.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatus;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;
import uk.ac.gda.devices.bssc.ui.BioSAXSProgressView;

public class BioSAXSISpyBIntegrationTest {
//	public static String ID = "uk.ac.gda.devices.bssc.biosaxsprogressperspective";
//	private static String defaultDataPath = "/entry1/detector/data";
//	private static BioSAXSProgressView view;
//	private static List<ISAXSProgress> model;
//	private static BioSAXSISPyB bioSAXSISPyB;
//	private static BioSAXSProgressController controller;
//	private static List<ISAXSDataCollection> iSpyBSAXSDataCollections;
//	private static DefaultRealm realm;
//	private static long blSessionId;
//	private static long experimentId;
//	private static String visit;
//
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {

//		realm = new DefaultRealm();
//		realm.exec(new Runnable() {
//			@Override
//			public void run() {
//				visit = "nt20-12";
//				try {
//					bioSAXSISPyB = new MockBioSAXSISPyB();
//					blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
//					experimentId = bioSAXSISPyB.createExperiment(blSessionId,
//							"test", "TEMPLATE", "test");
//					controller = new BioSAXSProgressController();
//					model = new ArrayList<ISAXSProgress>();
//					controller.setModel(model);
//					controller.setISpyBAPI(bioSAXSISPyB);
//					OSGIServiceRegister modelReg = new OSGIServiceRegister();
//					modelReg.setClass(BioSAXSProgressController.class);
//					modelReg.setService(controller);
//					modelReg.afterPropertiesSet();
//
//					Display.getDefault().asyncExec(new Runnable() {
//
//						@Override
//						public void run() {
//							final IWorkbenchWindow window = PlatformUI
//									.getWorkbench().getActiveWorkbenchWindow();
//							try {
//								view = (BioSAXSProgressView) window
//										.getActivePage().showView(
//												BioSAXSProgressView.ID);
//							} catch (PartInitException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							window.getActivePage().activate(view);
//							ActionFactory.IWorkbenchAction maximizeAction = ActionFactory.MAXIMIZE
//									.create(window);
//							// Will maximize the active part
//							maximizeAction.run();
//
//							window.getActivePage().activate(view);
//						}
//					});
//
//				} catch (SQLException e) {
//					e.printStackTrace();
//				} catch (FactoryException e) {
//					e.printStackTrace();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		});
//
//	}
//
//	@Test
//	public void testUpdateProgressViewFromISpyB() throws SQLException {
//		Job updateJob = new Job("") {
//
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//					List<ISAXSDataCollection> saxsDataCollections = bioSAXSISPyB
//							.getSAXSDataCollections(0);
//
//					for (ISAXSDataCollection saxsDataCollection : saxsDataCollections) {
//						long saxsDataCollectionId = saxsDataCollection.getId();
//
//						Thread.sleep(1000);
//
//						ISpyBStatusInfo expectedCollectionStatusInfo = new ISpyBStatusInfo();
//						expectedCollectionStatusInfo
//								.setStatus(ISpyBStatus.NOT_STARTED);
//						expectedCollectionStatusInfo.setProgress(0);
//						expectedCollectionStatusInfo.setMessage("");
//
//						ISpyBStatusInfo ispyBStatusInfo = bioSAXSISPyB
//								.getDataCollectionStatus(saxsDataCollectionId);
//						assertEquals(expectedCollectionStatusInfo.getStatus(),
//								ispyBStatusInfo.getStatus());
//						assertEquals(
//								expectedCollectionStatusInfo.getProgress(),
//								ispyBStatusInfo.getProgress(), 0.0);
//						assertEquals(expectedCollectionStatusInfo.getMessage(),
//								ispyBStatusInfo.getMessage());
//
//						long bufferBeforeId = bioSAXSISPyB.createBufferRun(
//								saxsDataCollectionId, 1.0, 20.0f, 20.0f, 10.0,
//								10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
//								getFilename(0), defaultDataPath);
//
//						Thread.sleep(1000);
//
//						if ((saxsDataCollectionId % 2) == 0) {
//							ISpyBStatusInfo expectedFailedStatusInfo = new ISpyBStatusInfo();
//							expectedFailedStatusInfo
//									.setStatus(ISpyBStatus.FAILED);
//							expectedFailedStatusInfo.setProgress(-1);
//
//							bioSAXSISPyB.setDataCollectionStatus(
//									saxsDataCollectionId,
//									expectedFailedStatusInfo);
//
//							Thread.sleep(1000);
//
//							ispyBStatusInfo = bioSAXSISPyB
//									.getDataCollectionStatus(saxsDataCollectionId);
//
//							assertEquals(expectedFailedStatusInfo.getStatus(),
//									ispyBStatusInfo.getStatus());
//							assertEquals(
//									expectedFailedStatusInfo.getProgress(),
//									ispyBStatusInfo.getProgress(), 0.0);
//							assertEquals(expectedFailedStatusInfo.getMessage(),
//									ispyBStatusInfo.getMessage());
//						}
//
//						// if collection has failed don't attempt to go any
//						// further
//						if (bioSAXSISPyB.getDataCollectionStatus(
//								saxsDataCollectionId).getStatus() != ISpyBStatus.FAILED) {
//							Thread.sleep(1000);
//
//							expectedCollectionStatusInfo
//									.setStatus(ISpyBStatus.RUNNING);
//							expectedCollectionStatusInfo.setProgress(33);
//							expectedCollectionStatusInfo
//									.addFileName(getFilename(0));
//							expectedCollectionStatusInfo.setMessage("");
//
//							ispyBStatusInfo = bioSAXSISPyB
//									.getDataCollectionStatus(saxsDataCollectionId);
//							assertEquals(
//									expectedCollectionStatusInfo.getStatus(),
//									ispyBStatusInfo.getStatus());
//							assertEquals(
//									expectedCollectionStatusInfo.getProgress(),
//									ispyBStatusInfo.getProgress(), 0.0);
//							assertEquals(expectedCollectionStatusInfo
//									.getFileNames().size(), ispyBStatusInfo
//									.getFileNames().size());
//							assertEquals(expectedCollectionStatusInfo
//									.getFileNames().get(0), ispyBStatusInfo
//									.getFileNames().get(0));
//							assertEquals(
//									expectedCollectionStatusInfo.getMessage(),
//									ispyBStatusInfo.getMessage());
//
//							long sampleId = bioSAXSISPyB.createSampleRun(
//									saxsDataCollectionId, 1.0, 20.0f, 20.0f,
//									10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
//									1.0, 1.0, getFilename(1), defaultDataPath);
//
//							// Assert status values are as expected
//							expectedCollectionStatusInfo
//									.setStatus(ISpyBStatus.RUNNING);
//							expectedCollectionStatusInfo.setProgress(66);
//							expectedCollectionStatusInfo
//									.addFileName(getFilename(1));
//							expectedCollectionStatusInfo.setMessage("");
//
//							Thread.sleep(1000);
//
//							ispyBStatusInfo = bioSAXSISPyB
//									.getDataCollectionStatus(saxsDataCollectionId);
//							assertEquals(
//									expectedCollectionStatusInfo.getStatus(),
//									ispyBStatusInfo.getStatus());
//							assertEquals(
//									expectedCollectionStatusInfo.getProgress(),
//									ispyBStatusInfo.getProgress(), 0.0);
////							assertEquals(expectedCollectionStatusInfo
////									.getFileNames().size(), ispyBStatusInfo
////									.getFileNames().size());
////							assertEquals(expectedCollectionStatusInfo
////									.getFileNames().get(1), ispyBStatusInfo
////									.getFileNames().get(1));
//							assertEquals(
//									expectedCollectionStatusInfo.getMessage(),
//									ispyBStatusInfo.getMessage());
//
//							Thread.sleep(1000);
//
//							// create a buffer after entry in ISpyB
//							// long bufferAfterId =
//							// bioSAXSISPyB.createBufferRun(dataCollectionId1,
//							// 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0,
//							// 1.0,
//							// 1.0, 1.0,
//							// 1.0, getFilename(3),
//							// defaultDataPath);
//							ISpyBStatusInfo statusInfo = new ISpyBStatusInfo();
//							statusInfo.setStatus(ISpyBStatus.COMPLETE);
//							statusInfo.setProgress(100);
//							bioSAXSISPyB.setDataCollectionStatus(
//									saxsDataCollectionId, statusInfo);
//
//							Thread.sleep(1000);
//
//							// Reduction
//							ISpyBStatusInfo expectedReductionStatusInfo = new ISpyBStatusInfo();
//							expectedReductionStatusInfo
//									.setStatus(ISpyBStatus.NOT_STARTED);
//							expectedReductionStatusInfo.setProgress(0);
//							expectedReductionStatusInfo.setMessage("");
//							ispyBStatusInfo = bioSAXSISPyB
//									.getDataReductionStatus(saxsDataCollectionId);
//							assertEquals(
//									expectedReductionStatusInfo.getStatus(),
//									ispyBStatusInfo.getStatus());
//							assertEquals(
//									expectedReductionStatusInfo.getProgress(),
//									ispyBStatusInfo.getProgress(), 0.0);
//							assertEquals(
//									expectedReductionStatusInfo.getMessage(),
//									ispyBStatusInfo.getMessage());
//							bioSAXSISPyB
//									.createDataReduction(saxsDataCollectionId);
//
//							Thread.sleep(1000);
//
//							// Set the reduction status upon successful
//							// completion
//							ISpyBStatusInfo reductionStatus = new ISpyBStatusInfo();
//							reductionStatus.setStatus(ISpyBStatus.COMPLETE);
//							reductionStatus.setProgress(100);
//							reductionStatus.addFileName(getFilename(4));
//							reductionStatus.setMessage("");
//							bioSAXSISPyB.setDataReductionStatus(
//									saxsDataCollectionId, reductionStatus);
//							expectedReductionStatusInfo = new ISpyBStatusInfo();
//							expectedReductionStatusInfo
//									.setStatus(ISpyBStatus.COMPLETE);
//							expectedReductionStatusInfo.setProgress(100);
////							expectedReductionStatusInfo
////									.addFileName(getFilename(4));
//							expectedReductionStatusInfo.setMessage("");
//							ispyBStatusInfo = bioSAXSISPyB
//									.getDataReductionStatus(saxsDataCollectionId);
//							assertEquals(
//									expectedReductionStatusInfo.getStatus(),
//									ispyBStatusInfo.getStatus());
//							assertEquals(
//									expectedReductionStatusInfo.getProgress(),
//									ispyBStatusInfo.getProgress(), 0.0);
////							assertEquals(expectedReductionStatusInfo
////									.getFileNames().get(0), ispyBStatusInfo
////									.getFileNames().get(0));
//							assertEquals(
//									expectedReductionStatusInfo.getMessage(),
//									ispyBStatusInfo.getMessage());
//
//						}
//					}
//
//					// Test adding a new data collection
//					// long dataCollectionId =
//					long newDataCollectionId = bioSAXSISPyB
//							.createSaxsDataCollection(experimentId, (short) 0,
//									(short) 1, (short) 1, "Sample1", 5.0, 10., (short) 0,
//									(short) 1, (short) 1, 20.0f, 10, 1.0, 2.0,
//									5.0, 10.0, "viscosity");
//				} catch (SQLException e1) {
//					e1.printStackTrace();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				return Status.OK_STATUS;
//			}
//		};
//		updateJob.schedule();
//	}
//
//	@Test
//	public void testProgressSelection() {
//		// Add test here to assert that when a measurement is selected then the
//		// correct editor part is opened
//		fail("Not yet implemented");
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//		waitForJobs();
//		delay(60000);
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//				.hideView(view);
//	}
//
//	@Before
//	public void setUp() throws Exception {
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//
//	/**
//	 * Process UI input but do not return for the specified time interval.
//	 * 
//	 * @param waitTimeMillis
//	 *            the number of milliseconds
//	 */
//	private static void delay(long waitTimeMillis) {
//		Display display = Display.getCurrent();
//
//		// If this is the UI thread,
//		// then process input.
//
//		if (display != null) {
//			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
//			while (System.currentTimeMillis() < endTimeMillis) {
//				if (!display.readAndDispatch())
//					display.sleep();
//			}
//			display.update();
//		}
//
//		// Otherwise, perform a simple sleep.
//
//		else {
//			try {
//				Thread.sleep(waitTimeMillis);
//			} catch (InterruptedException e) {
//				// Ignored.
//			}
//		}
//	}
//
//	/**
//	 * Wait until all background tasks are complete.
//	 */
//	public static void waitForJobs() {
//		while (!Job.getJobManager().isIdle())
//			delay(1000);
//	}
//
//	protected static String getFilename(int fileNumber) {
//		return "/dls/b21/data/2013/sm999-9/b21-" + fileNumber + ".nxs";
//	}
//	
//	
}
