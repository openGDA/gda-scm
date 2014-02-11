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
		bioSAXSISPyB = new MyBioSAXSISPyB();
	}

	@Test
	public void testBBSCScript() throws SQLException {
		String visit = "nt20-12";

		long blsessionId;
		
			blsessionId = bioSAXSISPyB.getSessionForVisit(visit);

			// create an EXPERIMENT in ISpyB
			long experimentId = bioSAXSISPyB.createExperiment(blsessionId,
					"test", "TEMPLATE", "test");

			// create a SAXSDATACOLLECTION in ISpyB
			long dataCollectionId1 = bioSAXSISPyB.createSaxsDataCollection(
					experimentId, (short) 0, (short) 1, (short) 1, "Sample1",
					(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
					10.0, "viscosity");

			// Test status values are correct on data collection creation
			ISpyBStatusInfo expectedStatusInfo = new ISpyBStatusInfo();
			expectedStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
			expectedStatusInfo.setProgress(0);
			expectedStatusInfo.setFileName("");
			expectedStatusInfo.setMessage("");
			
			ISpyBStatusInfo ispyBStatusInfo = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId1);
			assertEquals(expectedStatusInfo.getStatus(),
					ispyBStatusInfo.getStatus());
			assertEquals(expectedStatusInfo.getProgress(),
					ispyBStatusInfo.getProgress(), 0.0);
			assertEquals(expectedStatusInfo.getFileName(),
					ispyBStatusInfo.getFileName());
			assertEquals(expectedStatusInfo.getMessage(),
					ispyBStatusInfo.getMessage());

			// Test create buffer before run
			long bufferBeforeId = bioSAXSISPyB.createBufferRun(
					dataCollectionId1, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0,
					1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					"/dls/b21/data/2013/sm999-9/b21-9990.nxs",
					"/entry1/detector/data");
			
			// Assert status values are as expected
			ISpyBStatusInfo expectedBufferBeforeRunStatusInfo = new ISpyBStatusInfo();
			expectedBufferBeforeRunStatusInfo.setStatus(ISpyBStatus.RUNNING);
			expectedBufferBeforeRunStatusInfo.setProgress(33);
			expectedBufferBeforeRunStatusInfo.setFileName("/dls/b21/data/2013/sm999-9/b21-9990.nxs");
			expectedBufferBeforeRunStatusInfo.setMessage("");
			
			ispyBStatusInfo = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId1);
			assertEquals(expectedBufferBeforeRunStatusInfo.getStatus(),
					ispyBStatusInfo.getStatus());
			assertEquals(expectedBufferBeforeRunStatusInfo.getProgress(),
					ispyBStatusInfo.getProgress(), 0.0);
			assertEquals(expectedBufferBeforeRunStatusInfo.getFileName(),
					ispyBStatusInfo.getFileName());
			assertEquals(expectedBufferBeforeRunStatusInfo.getMessage(),
					ispyBStatusInfo.getMessage());

			// Test create sample run
			long sample1 = bioSAXSISPyB.createSampleRun(dataCollectionId1, 1.0,
					20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					1.0, "/dls/b21/data/2013/sm999-9/b21-9991.nxs",
					"/entry1/detector/data");
			// Assert status values are as expected
			ISpyBStatusInfo expectedSampleRunStatusInfo = new ISpyBStatusInfo();
			expectedSampleRunStatusInfo.setStatus(ISpyBStatus.RUNNING);
			expectedSampleRunStatusInfo.setProgress(66);
			expectedSampleRunStatusInfo.setFileName("/dls/b21/data/2013/sm999-9/b21-9991.nxs");
			expectedSampleRunStatusInfo.setMessage("");
			
			ispyBStatusInfo = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId1);
			assertEquals(expectedSampleRunStatusInfo.getStatus(),
					ispyBStatusInfo.getStatus());
			assertEquals(expectedSampleRunStatusInfo.getProgress(),
					ispyBStatusInfo.getProgress(), 0.0);
			assertEquals(expectedSampleRunStatusInfo.getFileName(),
					ispyBStatusInfo.getFileName());
			assertEquals(expectedSampleRunStatusInfo.getMessage(),
					ispyBStatusInfo.getMessage());

			// Test if sample run failed
			ISpyBStatusInfo failedSampleRunStatusInfo = new ISpyBStatusInfo();
			failedSampleRunStatusInfo.setStatus(ISpyBStatus.FAILED);
			failedSampleRunStatusInfo.setProgress(0);
			failedSampleRunStatusInfo.setMessage("Sample " + dataCollectionId1
					+ "run failed");
			failedSampleRunStatusInfo.setFileName("");
			bioSAXSISPyB.setDataCollectionStatus(dataCollectionId1,
					failedSampleRunStatusInfo);
			ispyBStatusInfo = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId1);
			assertEquals(failedSampleRunStatusInfo.getStatus(),
					ispyBStatusInfo.getStatus());
			assertEquals(failedSampleRunStatusInfo.getProgress(),
					ispyBStatusInfo.getProgress(), 0.0);
			assertEquals(failedSampleRunStatusInfo.getFileName(),
					ispyBStatusInfo.getFileName());
			assertEquals(failedSampleRunStatusInfo.getMessage(),
					ispyBStatusInfo.getMessage());

			// create a buffer after entry in ISpyB
			long bufferAfter1 = bioSAXSISPyB.createBufferRun(dataCollectionId1,
					1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					1.0, 1.0, "/dls/b21/data/2013/sm999-9/b21-9992.nxs",
					"/entry1/detector/data");
			// Assert status values are as expected
			ISpyBStatusInfo expectedBufferAfterRunStatusInfo = new ISpyBStatusInfo();
			expectedBufferAfterRunStatusInfo.setStatus(ISpyBStatus.COMPLETE);
			expectedBufferAfterRunStatusInfo.setProgress(100);
			expectedBufferAfterRunStatusInfo.setFileName("/dls/b21/data/2013/sm999-9/b21-9992.nxs");
			expectedBufferAfterRunStatusInfo.setMessage("");
			
			ispyBStatusInfo = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId1);
			assertEquals(expectedBufferAfterRunStatusInfo.getStatus(),
					ispyBStatusInfo.getStatus());
			assertEquals(expectedBufferAfterRunStatusInfo.getProgress(),
					ispyBStatusInfo.getProgress(), 0.0);
			assertEquals(expectedBufferAfterRunStatusInfo.getFileName(),
					ispyBStatusInfo.getFileName());
			assertEquals(expectedBufferAfterRunStatusInfo.getMessage(),
					ispyBStatusInfo.getMessage());

			// create a data reduction entry in ISpyB
			long reductionId = bioSAXSISPyB
					.createDataReduction(dataCollectionId1);
			// FIXME: check reduction status before as well
			// FIXME: how does ISPyB know about the reduction file name?
			ISpyBStatusInfo expectedReductionStatusInfo = new ISpyBStatusInfo();
			expectedReductionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
			expectedReductionStatusInfo.setProgress(100);
			expectedReductionStatusInfo.setFileName("/dls/b21/data/2013/sm999-9/b21-9993.nxs");
			expectedReductionStatusInfo.setMessage("");
			ispyBStatusInfo = bioSAXSISPyB
					.getDataReductionStatus(dataCollectionId1);
			assertEquals(expectedReductionStatusInfo.getStatus(),
					ispyBStatusInfo.getStatus());
			assertEquals(expectedReductionStatusInfo.getProgress(),
					ispyBStatusInfo.getProgress(), 0.0);
			assertEquals(expectedReductionStatusInfo.getFileName(),
					ispyBStatusInfo.getFileName());
			assertEquals(expectedReductionStatusInfo.getMessage(),
					ispyBStatusInfo.getMessage());

			// create an analysis entry in ISpyB
			long analysisId = bioSAXSISPyB
					.createDataAnalysis(dataCollectionId1);
			// FIXME: how is the file set?
			ispyBStatusInfo = bioSAXSISPyB
					.getDataAnalysisStatus(dataCollectionId1);
			ISpyBStatusInfo expectedAnalysisStatusInfo = new ISpyBStatusInfo();
			expectedAnalysisStatusInfo.setStatus(ISpyBStatus.COMPLETE);
			expectedAnalysisStatusInfo.setProgress(100);
			expectedAnalysisStatusInfo.setFileName("/dls/b21/data/2013/sm999-9/b21-9994.nxs");
			expectedAnalysisStatusInfo.setMessage("");
			assertEquals(expectedAnalysisStatusInfo.getStatus(),
					ispyBStatusInfo.getStatus());
			assertEquals(expectedAnalysisStatusInfo.getProgress(),
					ispyBStatusInfo.getProgress(), 0.0);
			assertEquals(expectedAnalysisStatusInfo.getFileName(),
					ispyBStatusInfo.getFileName());
			assertEquals(expectedAnalysisStatusInfo.getMessage(),
					ispyBStatusInfo.getMessage());

			// create a data collection that uses the same buffer before as the
			// buffer after from the previous collection, not sure how we can assert here
			// perhaps this needs to be tested within BioSAXSISpyBviaOracleTest 
			// to assert that the buffer after measurement used in the previous data collection 
			// matches the buffer before measurement of the current data collection
			bioSAXSISPyB.createSaxsDataCollectionUsingPreviousBuffer(
					experimentId, (short) 0, (short) 1, (short) 1, "Sample1",
					(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
					10.0, "viscosity", dataCollectionId1);
	}
}
