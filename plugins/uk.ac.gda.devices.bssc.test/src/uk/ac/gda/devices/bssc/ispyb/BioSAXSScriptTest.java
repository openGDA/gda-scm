package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class BioSAXSScriptTest {

	private static BioSAXSISPyB bioSAXSISPyB;
	private static long blSessionId;
	private static long experimentId;
	private static String visit;
	private List<ISAXSDataCollection> iSpyBSAXSDataCollections;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bioSAXSISPyB = new MockBioSAXSISPyB();
	}

	@Test
	public void testBBSCScript() throws SQLException {
		String visit = "nt20-12";
		int experimentCount = 0;
		int dataCollectionCount = 0;

		long blsessionId;

		blsessionId = bioSAXSISPyB.getSessionForVisit(visit);

		// create an EXPERIMENT in ISpyB
		long experimentId = bioSAXSISPyB.createExperiment(blsessionId, "test",
				"TEMPLATE", "test");
		experimentCount++;

		// ********* Test data collection successful ************************
		// create a SAXSDATACOLLECTION in ISpyB
		long dataCollectionId1 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Sample1",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;

		// Check status values are correct on data collection creation
		ISpyBStatusInfo expectedCollectionStatusInfo = new ISpyBStatusInfo();
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedCollectionStatusInfo.setProgress(0);
		expectedCollectionStatusInfo.setMessage("");

		ISpyBStatusInfo ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId1);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create buffer before run
		long bufferBeforeId = bioSAXSISPyB.createBufferRun(dataCollectionId1,
				1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, "/dls/b21/data/2013/sm999-9/b21-1.nxs",
				"/entry1/detector/data");

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-1.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId1);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create sample run
		long sampleId = bioSAXSISPyB.createSampleRun(dataCollectionId1, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9991.nxs",
				"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-2.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId1);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(1),
				ispyBStatusInfo.getFileNames().get(1));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// create a buffer after entry in ISpyB
		long bufferAfterId = bioSAXSISPyB.createBufferRun(dataCollectionId1,
				1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, "/dls/b21/data/2013/sm999-9/b21-3.nxs",
				"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-3.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId1);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(2),
				ispyBStatusInfo.getFileNames().get(2));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Reduction
		ISpyBStatusInfo expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedReductionStatusInfo.setProgress(0);
		expectedReductionStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB
				.getDataReductionStatus(dataCollectionId1);
		assertEquals(expectedReductionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedReductionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedReductionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());
		// create a data reduction entry in ISpyB
		long reductionId = bioSAXSISPyB.createDataReduction(dataCollectionId1);

		// Set the reduction status upon successful completion
		ISpyBStatusInfo reductionStatus = new ISpyBStatusInfo();
		reductionStatus.setStatus(ISpyBStatus.COMPLETE);
		reductionStatus.setProgress(100);
		reductionStatus.addFileName("/dls/b21/data/2013/sm999-9/b21-4.nxs");
		reductionStatus.setMessage("");
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId1, reductionStatus);
		expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedReductionStatusInfo.setProgress(100);
		expectedReductionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-4.nxs");
		expectedReductionStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB
				.getDataReductionStatus(dataCollectionId1);
		assertEquals(expectedReductionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedReductionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedReductionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedReductionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Analysis
		ISpyBStatusInfo expectedAnalysisStatusInfo = new ISpyBStatusInfo();
		expectedAnalysisStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedAnalysisStatusInfo.setProgress(0);
		expectedAnalysisStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId1);
		assertEquals(expectedAnalysisStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedAnalysisStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedAnalysisStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());
		// create an analysis entry in ISpyB
		long analysisId = bioSAXSISPyB.createDataAnalysis(dataCollectionId1);
		// Set the analysis status upon successful completion
		ISpyBStatusInfo analysisStatus = new ISpyBStatusInfo();
		analysisStatus.setStatus(ISpyBStatus.COMPLETE);
		analysisStatus.setProgress(100);
		analysisStatus.addFileName("/dls/b21/data/2013/sm999-9/b21-5.nxs");
		analysisStatus.setMessage("");
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId1, analysisStatus);

		expectedAnalysisStatusInfo = new ISpyBStatusInfo();
		expectedAnalysisStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedAnalysisStatusInfo.setProgress(100);
		expectedAnalysisStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-5.nxs");
		expectedAnalysisStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId1);
		assertEquals(expectedAnalysisStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedAnalysisStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedAnalysisStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedAnalysisStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// **********************Test data collection failed
		// create SAXSDATACOLLECTION in ISpyB
		long dataCollectionId2 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Sample1",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;

		expectedCollectionStatusInfo = new ISpyBStatusInfo();
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedCollectionStatusInfo.setProgress(0);
		expectedCollectionStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId2);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create buffer before run
		bufferBeforeId = bioSAXSISPyB
				.createBufferRun(dataCollectionId2, 1.0, 20.0f, 20.0f, 10.0,
						10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
						"/dls/b21/data/2013/sm999-9/b21-6.nxs",
						"/entry1/detector/data");

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-6.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId2);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		sampleId = bioSAXSISPyB
				.createSampleRun(dataCollectionId2, 1.0, 20.0f, 20.0f, 10.0,
						10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
						"/dls/b21/data/2013/sm999-9/b21-7.nxs",
						"/entry1/detector/data");

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.FAILED);
		expectedCollectionStatusInfo.setProgress(0);
		expectedCollectionStatusInfo.addFileName("");
		expectedCollectionStatusInfo.setMessage("");
		bioSAXSISPyB.setDataCollectionStatus(dataCollectionId2,
				expectedCollectionStatusInfo);
		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId2);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// ******************Test data reduction failed
		// create SAXSDATACOLLECTION in ISpyB
		long dataCollectionId3 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Sample1",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;

		// Check status values are correct on data collection creation
		expectedCollectionStatusInfo = new ISpyBStatusInfo();
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedCollectionStatusInfo.setProgress(0);
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId3);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create buffer before run
		bufferBeforeId = bioSAXSISPyB
				.createBufferRun(dataCollectionId3, 1.0, 20.0f, 20.0f, 10.0,
						10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
						"/dls/b21/data/2013/sm999-9/b21-8.nxs",
						"/entry1/detector/data");

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-8.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId3);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create sample run
		sampleId = bioSAXSISPyB
				.createSampleRun(dataCollectionId3, 1.0, 20.0f, 20.0f, 10.0,
						10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
						"/dls/b21/data/2013/sm999-9/b21-9.nxs",
						"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-9.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId3);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(1),
				ispyBStatusInfo.getFileNames().get(1));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// create a buffer after entry in ISpyB
		bufferAfterId = bioSAXSISPyB.createBufferRun(dataCollectionId3, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-10.nxs",
				"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-10.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId3);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(2),
				ispyBStatusInfo.getFileNames().get(2));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Reduction
		expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedReductionStatusInfo.setProgress(0);
		expectedReductionStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB
				.getDataReductionStatus(dataCollectionId3);
		assertEquals(expectedReductionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedReductionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedReductionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// create a data reduction entry in ISpyB
		reductionId = bioSAXSISPyB.createDataReduction(dataCollectionId3);
		// if reduction fails set the status in ISpyB
		reductionStatus = new ISpyBStatusInfo();
		reductionStatus.setStatus(ISpyBStatus.FAILED);
		reductionStatus.setProgress(0);
		reductionStatus.setMessage("Data Reduction Failed for data collection "
				+ dataCollectionId3);
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId1, reductionStatus);
		expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.FAILED);
		expectedReductionStatusInfo.setProgress(0);
		expectedReductionStatusInfo
				.setMessage("Data Reduction Failed for data collection "
						+ dataCollectionId3);
		ispyBStatusInfo = bioSAXSISPyB
				.getDataReductionStatus(dataCollectionId3);
		assertEquals(expectedReductionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedReductionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedReductionStatusInfo.getFileNames().size(), 0);
		assertEquals(expectedReductionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// ********** Test data analysis failed
		// create a SAXSDATACOLLECTION in ISpyB
		long dataCollectionId4 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Sample1",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;

		// Check status values are correct on data collection creation
		expectedCollectionStatusInfo = new ISpyBStatusInfo();
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedCollectionStatusInfo.setProgress(0);
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId4);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create buffer before run
		bufferBeforeId = bioSAXSISPyB.createBufferRun(dataCollectionId4, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-12.nxs",
				"/entry1/detector/data");

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-12.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId4);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create sample run
		sampleId = bioSAXSISPyB.createSampleRun(dataCollectionId4, 1.0, 20.0f,
				20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-13.nxs",
				"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-13.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId4);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(1),
				ispyBStatusInfo.getFileNames().get(1));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// create a buffer after entry in ISpyB
		bufferAfterId = bioSAXSISPyB.createBufferRun(dataCollectionId4, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-14.nxs",
				"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-14.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId4);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(2),
				ispyBStatusInfo.getFileNames().get(2));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Reduction
		expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedReductionStatusInfo.setProgress(0);
		expectedReductionStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB
				.getDataReductionStatus(dataCollectionId4);
		assertEquals(expectedReductionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedReductionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedReductionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());
		// create a data reduction entry in ISpyB
		reductionId = bioSAXSISPyB.createDataReduction(dataCollectionId4);
		// Set the reduction status upon successful completion
		reductionStatus = new ISpyBStatusInfo();
		reductionStatus.setStatus(ISpyBStatus.COMPLETE);
		reductionStatus.setProgress(100);
		reductionStatus.addFileName("/dls/b21/data/2013/sm999-9/b21-14.nxs");
		reductionStatus.setMessage("");
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId4, reductionStatus);
		expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedReductionStatusInfo.setProgress(100);
		expectedReductionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-14.nxs");
		expectedReductionStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB
				.getDataReductionStatus(dataCollectionId4);
		assertEquals(expectedReductionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedReductionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedReductionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedReductionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Analysis
		expectedAnalysisStatusInfo = new ISpyBStatusInfo();
		expectedAnalysisStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedAnalysisStatusInfo.setProgress(0);
		expectedAnalysisStatusInfo.setMessage("");
		ispyBStatusInfo = bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId4);
		assertEquals(expectedAnalysisStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedAnalysisStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedAnalysisStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());
		// create an analysis entry in ISpyB
		analysisId = bioSAXSISPyB.createDataAnalysis(dataCollectionId4);
		// Set the analysis status FAILED
		analysisStatus = new ISpyBStatusInfo();
		analysisStatus.setStatus(ISpyBStatus.FAILED);
		analysisStatus.setProgress(0);
		analysisStatus.setMessage("Data Analysis Failed for data collection "
				+ dataCollectionId4);
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId1, analysisStatus);
		expectedAnalysisStatusInfo = new ISpyBStatusInfo();
		expectedAnalysisStatusInfo.setStatus(ISpyBStatus.FAILED);
		expectedAnalysisStatusInfo.setProgress(0);
		expectedAnalysisStatusInfo
				.setMessage("Data Reduction Failed for data collection "
						+ dataCollectionId4);
		ispyBStatusInfo = bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId4);
		assertEquals(expectedAnalysisStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedAnalysisStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedAnalysisStatusInfo.getFileNames().size(), 0);
		assertEquals(expectedAnalysisStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// ***************** Test a data collection which uses the same buffer
		// as the previous collection
		// create a data collection that uses the same buffer before as the
		// buffer after from the previous collection, not sure how we can assert
		long dataCollectionId5 = bioSAXSISPyB
				.createSaxsDataCollectionUsingPreviousBuffer(experimentId,
						(short) 0, (short) 1, (short) 1, "Sample1", (short) 0,
						(short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0, 10.0,
						"viscosity", dataCollectionId4);

		// Check status values are correct on data collection creation
		expectedCollectionStatusInfo = new ISpyBStatusInfo();
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedCollectionStatusInfo.setProgress(0);
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId3);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create buffer before run
		bufferBeforeId = bioSAXSISPyB
				.createBufferRun(dataCollectionId4, 1.0, 20.0f, 20.0f, 10.0,
						10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
						"/dls/b21/data/2013/sm999-14/b21-8.nxs",
						"/entry1/detector/data");

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-15/b21-14.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId5);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(0),
				ispyBStatusInfo.getFileNames().get(0));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create sample run
		sampleId = bioSAXSISPyB
				.createSampleRun(dataCollectionId5, 1.0, 20.0f, 20.0f, 10.0,
						10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
						"/dls/b21/data/2013/sm999-9/b21-15.nxs",
						"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-15.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId5);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().size(),
				ispyBStatusInfo.getFileNames().size());
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(1),
				ispyBStatusInfo.getFileNames().get(1));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// create a buffer after entry in ISpyB
		bufferAfterId = bioSAXSISPyB.createBufferRun(dataCollectionId5, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-16.nxs",
				"/entry1/detector/data");
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo
				.addFileName("/dls/b21/data/2013/sm999-9/b21-16.nxs");
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId5);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getFileNames().get(2),
				ispyBStatusInfo.getFileNames().get(2));
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Test data collections have been added to the database
		List<ISAXSDataCollection> iSAXSDataCollections = bioSAXSISPyB
				.getSAXSDataCollections(blsessionId);
		assertEquals(dataCollectionId1, iSAXSDataCollections.get(0).getId());
		assertEquals(dataCollectionId2, iSAXSDataCollections.get(1).getId());
		assertEquals(dataCollectionId3, iSAXSDataCollections.get(2).getId());
		assertEquals(dataCollectionId4, iSAXSDataCollections.get(3).getId());

		// Test correct experiment ids are returned for a session
		List<Long> experimentIds = bioSAXSISPyB
				.getExperimentsForSession(blsessionId);
		assertEquals(experimentId, experimentIds.get(0), 0.0);

		// Test correct data collectionIds are returned for an experiment
		// TODO check that Jun can preserve the ordering
		List<Long> dataCollectionIds = bioSAXSISPyB
				.getDataCollectionsForExperiments(experimentId);
		assertEquals(dataCollectionCount, dataCollectionIds.size());
		assertEquals(dataCollectionId1, dataCollectionIds.get(0).longValue());
		assertEquals(dataCollectionId2, dataCollectionIds.get(1).longValue());
		assertEquals(dataCollectionId3, dataCollectionIds.get(2).longValue());
		assertEquals(dataCollectionId4, dataCollectionIds.get(3).longValue());

		// Test getPreviousId
		// TODO why do we need that call? What does "previous" mean?
		// Previous added to the database or the last one that had data
		// collected to it?
		long previousCollectionId = bioSAXSISPyB
				.getPreviousCollectionId(dataCollectionId2);
		assertEquals(dataCollectionId1, previousCollectionId);

		// Test the correct number of measurement files are returned for a
		// SAXSDATACOLLECTION
		List<SampleInfo> sampleInfoList = bioSAXSISPyB
				.getSaxsDataCollectionInfo(dataCollectionId1);
		// data collection 1 ran successfully so it should contain 3 nexus files
		// in the sampleInfo list
		assertEquals(3, sampleInfoList.size());
	}
}
