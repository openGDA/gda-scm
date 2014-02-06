package uk.ac.gda.devices.bssc.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.observable.IObserver;
import gda.observable.Observer;
import gda.observable.Predicate;
import gda.rcp.GDAClientActivator;
import gda.rcp.util.OSGIServiceRegister;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

import uk.ac.gda.devices.bssc.beans.BioSAXSDataCollection;
import uk.ac.gda.devices.bssc.beans.IProgressModel;
import uk.ac.gda.devices.bssc.beans.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.beans.ISpyBStatus;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;
import uk.ac.gda.devices.bssc.ispyb.SampleInfo;

public class BioSaxsProgressViewTest {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxsprogressperspective";
	private static BioSAXSProgressView view;
	private static IProgressModel model;
	private static MyBioSAXSISPy bioSAXSISPyB;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bioSAXSISPyB = new MyBioSAXSISPy();
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
		// Will maximize the active part
		maximizeAction.run();

		window.getActivePage().activate(view);
	}

	private static void populateModel() {
		String visit = "nt20-12";
		long blsessionId;
		long experimentId = 0;

		try {
			blsessionId = bioSAXSISPyB.getSessionForVisit(visit);
			experimentId = bioSAXSISPyB.createExperiment(blsessionId, "test",
					"TEMPLATE", "test");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < 20; i++) {
			try {
				long collection1 = bioSAXSISPyB.createSaxsDataCollection(
						experimentId, (short) 0, (short) 1, (short) 1,
						"Sample1", (short) 0, (short) 1, (short) 1, 20.0f, 10,
						1.0, 2.0, 5.0, 10.0, "viscosity");
				ISAXSDataCollection dataCollection = new BioSAXSDataCollection();
				dataCollection.setId(collection1);
				dataCollection.setSampleName("Sample : " + String.valueOf(i));
				dataCollection.setCollectionProgress(0);
				model.add(dataCollection);

				dataCollection.setCollectionStatus(ISpyBStatus.NOT_STARTED);
				dataCollection.getCollectionStatus().setProgress(0);

				dataCollection.setReductionStatus(ISpyBStatus.NOT_STARTED);
				dataCollection.getReductionStatus().setProgress(0);

				dataCollection.setAnalysisStatus(ISpyBStatus.NOT_STARTED);
				dataCollection.getAnalysisStatus().setProgress(0);

				bioSAXSISPyB.setDataCollectionStatus(collection1,
						dataCollection.getCollectionStatus());
				bioSAXSISPyB.setDataReductionStatus(collection1,
						dataCollection.getReductionStatus(), "");
				bioSAXSISPyB.setDataAnalysisStatus(collection1,
						dataCollection.getAnalysisStatus(), "");

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testMeasurementProgress() throws Exception {
		ObservableList items = (ObservableList) model.getItems();
		for (int i = 0; i < model.size(); i++) {
			items.add(model.get(i));
		}

		for (int i = 0; i < items.size(); i++) {

			ISAXSDataCollection dataCollection = (ISAXSDataCollection) items
					.get(i);

			bioSAXSISPyB.setDataCollectionStatus(i, ISpyBStatus.RUNNING);
			long bufferBeforeRun = bioSAXSISPyB.createBufferRun(i, 1.0,
					20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					1.0, "/dls/b21/data/2013/sm999-9/b21-9990.nxs",
					"/entry1/detector/data");
			dataCollection.setCollectionProgress(33);
			delay(1000);

			long sampleRun = bioSAXSISPyB.createSampleRun(i, 1.0, 20.0f, 20.0f,
					10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					"/dls/b21/data/2013/sm999-9/b21-9991.nxs",
					"/entry1/detector/data");
			dataCollection.setCollectionProgress(66);
			delay(1000);

			long bufferAfter1 = bioSAXSISPyB.createBufferRun(i, 1.0, 20.0f,
					20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					"/dls/b21/data/2013/sm999-9/b21-9992.nxs",
					"/entry1/detector/data");
			dataCollection.setCollectionProgress(100);
			delay(1000);

			bioSAXSISPyB.setDataReductionStatus(i, ISpyBStatus.RUNNING, "");
			delay(1000);

			bioSAXSISPyB.setDataAnalysisStatus(i, ISpyBStatus.RUNNING, "");
			delay(1000);

			for (int j = 0; j < 100; j++) {
				dataCollection.setReductionProgress(i + j);
				dataCollection.setAnalysisProgress(i + j);
			}
			delay(1000);
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

		ISAXSDataCollection newProgress = new BioSAXSDataCollection();
		newProgress.setExperimentId(String.valueOf(21));
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

class MyProgressModel extends ArrayList<ISAXSDataCollection> implements
		IProgressModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	WritableList items = new WritableList(new ArrayList<ISAXSDataCollection>(),
			ISAXSDataCollection.class);

	@Override
	public WritableList getItems() {
		return items;
	}

	@Override
	public void clearItems() {
		items.clear();
	}

	@Override
	public void addItems(List<ISAXSDataCollection> bioSAXSSamples) {
		items.add(bioSAXSSamples);
	}

	@Override
	public void addItem(ISAXSDataCollection dataCollection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addIObserver(IObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIObserver(IObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIObservers() {
		// TODO Auto-generated method stub

	}
}

class MyBioSAXSISPy implements BioSAXSISPyB {

	private int dataCollectionId;

	@Override
	public long getSessionForVisit(String visitname) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long createSaxsDataCollection(long experimentID, short plate,
			short row, short column, String sampleName, short bufferPlate,
			short bufferRow, short bufferColumn, float exposureTemperature,
			int numFrames, double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity) throws SQLException {
		return dataCollectionId++;
	}

	@Override
	public long createBufferRun(long currentDataCollectionId,
			double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount,
			double transmission, double beamCenterX, double beamCenterY,
			double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename,
			String internalPath) {
		return currentDataCollectionId;
	}

	@Override
	public long createSampleRun(long dataCollectionId, double timePerFrame,
			float storageTemperature, float exposureTemperature, double energy,
			int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY,
			double radiationRelative, double radiationAbsolute,
			double normalization, String filename, String internalPath) {
		return dataCollectionId;
	}

	@Override
	public void disconnect() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<SampleInfo> getSaxsDataCollectionInfo(long saxsDataCollectionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SampleInfo> getExperimentInfo(long experimentId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getExperimentsForSession(long blsessionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getDataCollectionsForExperiments(long experiment)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long createExperiment(long sessionId, String name,
			String experimentType, String comments) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDataCollectionStatus(long saxsDataCollectionId,
			ISpyBStatus status) {
		IProgressModel model = (IProgressModel) GDAClientActivator
				.getNamedService(IProgressModel.class, null);
		model.get(((Long) saxsDataCollectionId).intValue())
				.setCollectionStatus(status);
	}

	@Override
	public ISpyBStatus getDataCollectionStatus(long dataCollectionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long createDataReduction(long dataCollectionId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDataReductionStatus(long saxsDataCollectionId,
			ISpyBStatus status, String resultsFilename) throws SQLException {
		IProgressModel model = (IProgressModel) GDAClientActivator
				.getNamedService(IProgressModel.class, null);
		model.get(((Long) saxsDataCollectionId).intValue()).setReductionStatus(
				status);
	}

	@Override
	public ISpyBStatus getDataReductionStatus(long dataCollectionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ISAXSDataCollection> getSAXSDataCollections(long blSessionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long createDataAnalysis(long dataCollectionId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDataAnalysisStatus(long saxsDataCollectionId,
			ISpyBStatus status, String resultsFilename) throws SQLException {
		IProgressModel model = (IProgressModel) GDAClientActivator
				.getNamedService(IProgressModel.class, null);
		model.get(((Long) saxsDataCollectionId).intValue()).setAnalysisStatus(
				status);
	}

	@Override
	public ISpyBStatus getDataAnalysisStatus(long dataCollectionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long createSaxsDataCollectionUsingPreviousBuffer(long experimentID,
			short plate, short row, short column, String sampleName,
			short bufferPlate, short bufferRow, short bufferColumn,
			float exposureTemperature, int numFrames, double timePerFrame,
			double flow, double volume, double energyInkeV, String viscosity,
			long previousDataCollectionId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAnalysisStatus(long dataCollectionId,
			ISpyBStatus analysisStatus) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPreviousCollectionId(long dataCollectionId) {
		// TODO Auto-generated method stub
		return 0;
	}

}
