package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

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

		ISpyBStatusInfo collectionStatus;
		long collection1 = bioSAXSISPyB.createSaxsDataCollection(experimentId,
				(short) 0, (short) 1, (short) 1, "Sample1", (short) 0,
				(short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0, 10.0,
				"viscosity");
		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection1);
		assertEquals(collectionStatus, ISpyBStatus.NOT_STARTED);

		long bufferBefore1 = bioSAXSISPyB.createBufferRun(collection1, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9990.nxs",
				"/entry1/detector/data");
		assertTrue(bufferBefore1 >= 0);
		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection1);
		assertEquals(collectionStatus.getProgress(), 33);
		assertEquals(collectionStatus, ISpyBStatus.RUNNING);
		// TODO get progress as well

		long sample1 = bioSAXSISPyB.createSampleRun(collection1, 1.0, 20.0f,
				20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9991.nxs",
				"/entry1/detector/data");
		assertTrue(sample1 >= 0);
		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection1);
		assertEquals(collectionStatus.getProgress(), 66);
		assertEquals(collectionStatus, ISpyBStatus.RUNNING);

		// Create a buffer after run (pass in -1 for the previous collection id
		// which indicates not to share the buffer of the previous collection
		long bufferAfter1 = bioSAXSISPyB.createBufferRun(collection1, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9992.nxs",
				"/entry1/detector/data");
		assertTrue(bufferAfter1 >= 0);
		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection1);
		assertEquals(collectionStatus.getProgress(), 100);
		assertEquals(collectionStatus, ISpyBStatus.COMPLETE);

		// create a 2nd data collection and assign it share the buffer from the
		// previous data collection
		long collection2 = bioSAXSISPyB
				.createSaxsDataCollectionUsingPreviousBuffer(experimentId,
						(short) 0, (short) 1, (short) 1, "Sample2", (short) 0,
						(short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0, 10.0,
						"viscosity", collection1); // or could replace this last
													// argument with
													// bufferAfter1 - use the
													// measurementId explicitly
													// instead of searching
													// through the data
													// collection

		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection2);
		assertEquals(collectionStatus.getProgress(), 33);
		assertEquals(collectionStatus, ISpyBStatus.RUNNING);
		// TODO get progress as well

		long sample2 = bioSAXSISPyB.createSampleRun(collection2, 1.0, 20.0f,
				20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9991.nxs",
				"/entry1/detector/data");
		assertTrue(sample2 >= 0);
		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection2);
		assertEquals(collectionStatus.getProgress(), 66);
		assertEquals(collectionStatus, ISpyBStatus.RUNNING);

		// Create a buffer after run (pass in -1 for the previous collection id
		// which indicates not to share the buffer of the previous collection
		long bufferAfter2 = bioSAXSISPyB.createBufferRun(collection2, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9992.nxs",
				"/entry1/detector/data");
		assertTrue(bufferAfter2 >= 0);
		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection2);
		assertEquals(collectionStatus.getProgress(), 100);
		assertEquals(collectionStatus, ISpyBStatus.COMPLETE);

		// create a data collection gets updated with a FAILED status if any of
		// the measurements for that data collection fail
		long collection3 = bioSAXSISPyB.createSaxsDataCollection(experimentId,
				(short) 0, (short) 1, (short) 1, "Sample3", (short) 0,
				(short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0, 10.0,
				"viscosity");

		assertEquals(bioSAXSISPyB.getDataCollectionStatus(collection3),
				ISpyBStatus.NOT_STARTED);

		ISpyBStatusInfo collectionStatusFailed = new ISpyBStatusInfo();
		collectionStatusFailed.setStatus(ISpyBStatus.FAILED);
		collectionStatusFailed.setProgress(33);
		String bufferBeforeFailureMessage = "Data Collection failed at after running buffer before meaurement";
		bioSAXSISPyB.setDataCollectionStatus(collection3,
				collectionStatusFailed);
		collectionStatus = bioSAXSISPyB.getDataCollectionStatus(collection3);
		assertEquals(collectionStatus, ISpyBStatus.FAILED);
		assertEquals(collectionStatus.getMessage(), bufferBeforeFailureMessage);
		assertEquals(collectionStatus.getProgress(), 33);

		// start data reduction and assert it is in the running state
		ISpyBStatusInfo reductionStatusComplete = new ISpyBStatusInfo();
		reductionStatusComplete.setStatus(ISpyBStatus.COMPLETE);
		reductionStatusComplete.setProgress(100);
		long subtractionId = bioSAXSISPyB.createDataReduction(collection1);

		assertEquals(bioSAXSISPyB.getDataReductionStatus(collection1),
				ISpyBStatus.RUNNING);

		// test data reduction completed
		bioSAXSISPyB.setDataReductionStatus(collection3,
				reductionStatusComplete,
				"/dls/b21/data/2013/sm999-9/b21-9993.nxs");

		ISpyBStatusInfo iSpyBReductionDetails = bioSAXSISPyB
				.getDataReductionStatus(collection1);
		assertEquals(reductionStatusComplete.getProgress(),
				iSpyBReductionDetails.getProgress(), 0.0);
		assertEquals(reductionStatusComplete, iSpyBReductionDetails.getStatus());

		// test data reduction failed
		// create new data collection here
		ISpyBStatusInfo reductionStatusFailed = new ISpyBStatusInfo();
		reductionStatusFailed.setStatus(ISpyBStatus.FAILED);
		reductionStatusFailed.setProgress(0);
		bioSAXSISPyB.setDataReductionStatus(collection1, reductionStatusFailed,
				"");

		ISpyBStatusInfo iSpyBReductionDetails2 = bioSAXSISPyB
				.getDataReductionStatus(collection1);
		assertEquals(reductionStatusFailed.getStatus(),
				iSpyBReductionDetails2.getStatus());

		// start data analysis and assert it is in the running state
		ISpyBStatusInfo analysisStatusInfo = new ISpyBStatusInfo();
		analysisStatusInfo.setStatus(ISpyBStatus.RUNNING);
		long analysisId = bioSAXSISPyB.createDataAnalysis(collection1);
		ISpyBStatusInfo iSpyBAnalysisStatusInfo = bioSAXSISPyB
				.getDataAnalysisStatus(collection1);
		assertEquals(analysisStatusInfo.getStatus(),
				iSpyBAnalysisStatusInfo.getStatus());

		// test data analysis completed
		ISpyBStatusInfo analysisStatusInfo1 = new ISpyBStatusInfo();
		analysisStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		bioSAXSISPyB.setDataAnalysisStatus(collection1, analysisStatusInfo1,
				"/dls/b21/data/2013/sm999-9/b21-9994.nxs");

		ISpyBStatusInfo iSpyBAnalysisInfo1 = bioSAXSISPyB
				.getDataAnalysisStatus(collection1);
		assertEquals(analysisStatusInfo1.getStatus(),
				iSpyBAnalysisInfo1.getStatus());
		assertEquals(analysisStatusInfo1.getProgress(),
				iSpyBAnalysisInfo1.getProgress(), 0.0);

		// test data analysis failed
		// create a new data collection here
		ISpyBStatusInfo analysisStatusInfo2 = new ISpyBStatusInfo();
		analysisStatusInfo2.setStatus(ISpyBStatus.FAILED);
		bioSAXSISPyB
				.setDataAnalysisStatus(collection1, analysisStatusInfo2, "");

		ISpyBStatusInfo iSpyBAnalysisInfo2 = bioSAXSISPyB
				.getDataAnalysisStatus(collection1);
		assertEquals(analysisStatusInfo2.getStatus(),
				iSpyBAnalysisInfo2.getStatus());

		bioSAXSISPyB.disconnect();
	}
}
