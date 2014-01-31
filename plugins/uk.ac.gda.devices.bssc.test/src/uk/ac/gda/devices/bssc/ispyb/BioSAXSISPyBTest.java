package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gda.rcp.GDAClientActivator;
import gda.rcp.util.OSGIServiceRegister;

import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.beans.BioSAXSProgressModel;
import uk.ac.gda.devices.bssc.beans.BioSaxsDataCollection;
import uk.ac.gda.devices.bssc.beans.IProgressModel;
import uk.ac.gda.devices.bssc.beans.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.beans.ISpyBStatus;

public class BioSAXSISPyBTest {

	static BioSAXSISPyB bioSAXSISPyB = null;

	@BeforeClass
	public static void setup() {
		new BioSAXSDBFactory()
				.setJdbcURL("jdbc:oracle:thin:@sci-serv2.diamond.ac.uk:1521:xe");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
	}

	@Test
	public void testcreateMeasurementsAndRegisterBufferForSample()
			throws SQLException {
		// These steps will be done prior to the experiment being run (in the
		// init module of the BSSC.py)
		String visit = "nt20-12";

		long blsessionId = bioSAXSISPyB.getSessionForVisit(visit);
		assertEquals(blsessionId, 434L);

		long experimentId = bioSAXSISPyB.createExperiment(blsessionId, "test",
				"TEMPLATE", "test");

		ISpyBStatus collectionStatus;
		long saxsDataCollectionId = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Sample",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		collectionStatus = bioSAXSISPyB
				.getDataCollectionStatus(saxsDataCollectionId);
		assertEquals(collectionStatus, ISpyBStatus.NOT_STARTED);
		
		// pass in collection id of previous and current collection so that
		// current data collection and previous data collection share the same
		// buffer (previous buffer after == current buffer before)
		long bufferBeforeRunId = bioSAXSISPyB.createBufferRun(
				-1, saxsDataCollectionId, 1.0, 20.0f, 20.0f,
				10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9990.nxs",
				"/entry1/detector/data");
		assertTrue(bufferBeforeRunId >= 0);
		collectionStatus = bioSAXSISPyB
				.getDataCollectionStatus(saxsDataCollectionId);
		assertEquals(collectionStatus.getProgress(), 0.33);
		assertEquals(collectionStatus, ISpyBStatus.RUNNING);
		// TODO get progress as well

		long sampleRunId = bioSAXSISPyB.createSampleRun(saxsDataCollectionId,
				1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, "/dls/b21/data/2013/sm999-9/b21-9991.nxs",
				"/entry1/detector/data");
		assertTrue(sampleRunId >= 0);
		collectionStatus = bioSAXSISPyB
				.getDataCollectionStatus(saxsDataCollectionId);
		assertEquals(collectionStatus.getProgress(), 0.66);
		assertEquals(collectionStatus, ISpyBStatus.RUNNING);

		// Create a buffer after run (pass in -1 for the previous collection id
		// which indicates not to share the buffer of the previous collection
		long bufferAfterRunId = bioSAXSISPyB.createBufferRun(-1,
				saxsDataCollectionId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9992.nxs",
				"/entry1/detector/data");
		assertTrue(bufferAfterRunId >= 0);
		collectionStatus = bioSAXSISPyB
				.getDataCollectionStatus(saxsDataCollectionId);
		assertEquals(collectionStatus.getProgress(), 1);
		assertEquals(collectionStatus, ISpyBStatus.COMPLETE);

		// create a data collection gets updated with a FAILED status if any of
		// the measurements for that data collection fail
		saxsDataCollectionId = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Sample",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");

		assertEquals(
				bioSAXSISPyB.getDataCollectionStatus(saxsDataCollectionId),
				ISpyBStatus.NOT_STARTED);

		ISpyBStatus status = ISpyBStatus.FAILED;
		status.setProgress(0.33);
		String bufferBeforeFailureMessage = "Data Collection failed at after running buffer before meaurement";
		bioSAXSISPyB.setCollectionStatus(saxsDataCollectionId,
				status);
		collectionStatus = bioSAXSISPyB
				.getDataCollectionStatus(saxsDataCollectionId);
		assertEquals(collectionStatus, ISpyBStatus.FAILED);
		assertEquals(collectionStatus.getMessage(), bufferBeforeFailureMessage);
		assertEquals(collectionStatus.getProgress(), 0.33);

		// start data reduction and assert it is in the running state
		ISpyBStatus reductionStatus;
		long subtractionId = bioSAXSISPyB
				.createDataReduction(saxsDataCollectionId);

		assertEquals(bioSAXSISPyB.getDataReductionStatus(saxsDataCollectionId),
				ISpyBStatus.RUNNING);

		// test data reduction completed
		bioSAXSISPyB
				.setDataReductionStatus(saxsDataCollectionId,
						ISpyBStatus.COMPLETE,
						"/dls/b21/data/2013/sm999-9/b21-9993.nxs");
		reductionStatus = bioSAXSISPyB
				.getDataReductionStatus(saxsDataCollectionId);
		assertEquals(reductionStatus.getProgress(), 1);
		assertEquals(reductionStatus, ISpyBStatus.COMPLETE);

		// test data reduction failed
		// create new data collection here
		bioSAXSISPyB.setDataReductionStatus(saxsDataCollectionId,
				ISpyBStatus.FAILED, "");
		reductionStatus = bioSAXSISPyB
				.getDataReductionStatus(saxsDataCollectionId);
		assertEquals(reductionStatus, ISpyBStatus.FAILED);

		// start data analysis and assert it is in the running state
		ISpyBStatus analysisStatus;
		long analysisId = bioSAXSISPyB.createDataAnalysis(saxsDataCollectionId);
		analysisStatus = bioSAXSISPyB
				.getDataAnalysisStatus(saxsDataCollectionId);
		assertEquals(analysisStatus, ISpyBStatus.RUNNING);

		// if reduction has failed then analysis has not being started
		// test data analysis completed

		bioSAXSISPyB
				.setDataAnalysisStatus(saxsDataCollectionId,
						ISpyBStatus.COMPLETE,
						"/dls/b21/data/2013/sm999-9/b21-9994.nxs");
		analysisStatus = bioSAXSISPyB
				.getDataAnalysisStatus(saxsDataCollectionId);
		assertEquals(analysisStatus.getProgress(), 1);
		assertEquals(analysisStatus, ISpyBStatus.COMPLETE);

		// test data analysis failed
		// create a new data collection here
		bioSAXSISPyB.setDataAnalysisStatus(saxsDataCollectionId,
				ISpyBStatus.FAILED, "");
		analysisStatus = bioSAXSISPyB
				.getDataAnalysisStatus(saxsDataCollectionId);
		assertEquals(analysisStatus, ISpyBStatus.FAILED);

		bioSAXSISPyB.disconnect();
	}
}