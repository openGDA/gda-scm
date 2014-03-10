package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class BioSAXSScriptTest {

	protected static BioSAXSISPyB bioSAXSISPyB;
	protected static String defaultDataPath = "/entry1/detector/data";
	private static long blSessionId;
	private static long experimentId;
	private static String visit;
	private List<ISAXSDataCollection> iSpyBSAXSDataCollections;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new BioSAXSDBFactory()
				.setJdbcURL("jdbc:oracle:thin:@ws096.diamond.ac.uk:1521:xe");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
	}

	@Test
	public void testBBSCScript() throws SQLException {
		String visit = "nt20-13";
		int experimentCount = 0;
		int dataCollectionCount = 0;

		long blsessionId;

		blsessionId = bioSAXSISPyB.getSessionForVisit(visit);

		// create an EXPERIMENT in ISpyB
		long experimentId = bioSAXSISPyB.createExperiment(blsessionId, "test",
				"TEMPLATE", "test");
		experimentCount++;

		// create 5 data collections
		long dataCollectionId1 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Test Sample 1",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;

		long dataCollectionId2 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Test Sample 2",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;
		
		long dataCollectionId3 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Test Sample 3",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;

		long dataCollectionId4 = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Test Sample 4",
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");
		dataCollectionCount++;
		
		long dataCollectionId5 = bioSAXSISPyB
				.createSaxsDataCollectionUsingPreviousBuffer(experimentId,
						(short) 0, (short) 1, (short) 1, "Test Sample 5", (short) 0,
						(short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0, 10.0,
						"viscosity", dataCollectionId4);
		dataCollectionCount++;
		
		//************Run data collection 1 **********************
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
				1.0, getFilename(1), defaultDataPath);

		
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo.addFileName(getFilename(1));
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
				getFilename(2), defaultDataPath);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo.addFileName(getFilename(2));
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
				1.0, getFilename(3), defaultDataPath);
		bioSAXSISPyB.setExperimentFinished(experimentId);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo.addFileName(getFilename(3));
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
		reductionStatus.addFileName(getFilename(4));
		reductionStatus.setMessage("");
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId1, reductionStatus);
		expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedReductionStatusInfo.setProgress(100);
		expectedReductionStatusInfo.addFileName(getFilename(4));
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
		analysisStatus.addFileName(getFilename(5));
		analysisStatus.setMessage("");
		bioSAXSISPyB.setDataAnalysisStatus(dataCollectionId1, analysisStatus);

		expectedAnalysisStatusInfo = new ISpyBStatusInfo();
		expectedAnalysisStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedAnalysisStatusInfo.setProgress(100);
		expectedAnalysisStatusInfo.addFileName(getFilename(5));
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

		
		
		
		//************Run data collection 2 **********************
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
		bufferBeforeId = bioSAXSISPyB.createBufferRun(dataCollectionId2, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				getFilename(6), defaultDataPath);

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo.addFileName(getFilename(6));
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

		sampleId = bioSAXSISPyB.createSampleRun(dataCollectionId2, 1.0, 20.0f,
				20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				getFilename(7), defaultDataPath);

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
		
		
		
		
		//************Run data collection 3 **********************
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
		bufferBeforeId = bioSAXSISPyB.createBufferRun(dataCollectionId3, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				getFilename(8), defaultDataPath);

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo.addFileName(getFilename(8));
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
		sampleId = bioSAXSISPyB.createSampleRun(dataCollectionId3, 1.0, 20.0f,
				20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				getFilename(10), defaultDataPath);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo.addFileName(getFilename(10));
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
				getFilename(10), defaultDataPath);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo.addFileName(getFilename(10));
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
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId3, reductionStatus);
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

		// Check status values are correct on data collection creation
		expectedCollectionStatusInfo = new ISpyBStatusInfo();
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedCollectionStatusInfo.setProgress(0);
		expectedCollectionStatusInfo.setMessage("");

		
		
		
		//************Run data collection 4 **********************
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
				getFilename(12), defaultDataPath);

		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo.addFileName(getFilename(12));
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
				getFilename(13), defaultDataPath);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo.addFileName(getFilename(13));
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
				getFilename(14), defaultDataPath);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo.addFileName(getFilename(14));
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
		reductionStatus.addFileName(getFilename(14));
		reductionStatus.setMessage("");
		bioSAXSISPyB.setDataReductionStatus(dataCollectionId4, reductionStatus);
		expectedReductionStatusInfo = new ISpyBStatusInfo();
		expectedReductionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedReductionStatusInfo.setProgress(100);
		expectedReductionStatusInfo.addFileName(getFilename(14));
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
		bioSAXSISPyB.setDataAnalysisStatus(dataCollectionId4, analysisStatus);
		expectedAnalysisStatusInfo = new ISpyBStatusInfo();
		expectedAnalysisStatusInfo.setStatus(ISpyBStatus.FAILED);
		expectedAnalysisStatusInfo.setProgress(0);
		expectedAnalysisStatusInfo
				.setMessage("Data Analysis Failed for data collection "
						+ dataCollectionId4);
		ispyBStatusInfo = bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId4);
		assertEquals(expectedAnalysisStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedAnalysisStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedAnalysisStatusInfo.getFileNames().size(), 0);
		assertEquals(expectedAnalysisStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		
		
		
		//************ Run data collection 5 **********************
		// Check status values are correct on data collection creation
		expectedCollectionStatusInfo = new ISpyBStatusInfo();
		// Because we are using the previous buffer then set STATUS to NOT_STARTED
		// and progress to 33 (because the buffer before has already been run in
		// the previous collection)
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		expectedCollectionStatusInfo.setProgress(33);
		expectedCollectionStatusInfo.setMessage("");

		ispyBStatusInfo = bioSAXSISPyB
				.getDataCollectionStatus(dataCollectionId5);
		assertEquals(expectedCollectionStatusInfo.getStatus(),
				ispyBStatusInfo.getStatus());
		assertEquals(expectedCollectionStatusInfo.getProgress(),
				ispyBStatusInfo.getProgress(), 0.0);
		assertEquals(expectedCollectionStatusInfo.getMessage(),
				ispyBStatusInfo.getMessage());

		// Create sample run
		sampleId = bioSAXSISPyB.createSampleRun(dataCollectionId5, 1.0, 20.0f,
				20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				getFilename(15), defaultDataPath);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.RUNNING);
		expectedCollectionStatusInfo.setProgress(66);
		expectedCollectionStatusInfo.addFileName(getFilename(14));
		expectedCollectionStatusInfo.addFileName(getFilename(15));
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
				getFilename(16), defaultDataPath);
		// Assert status values are as expected
		expectedCollectionStatusInfo.setStatus(ISpyBStatus.COMPLETE);
		expectedCollectionStatusInfo.setProgress(100);
		expectedCollectionStatusInfo.addFileName(getFilename(16));
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

		Iterator<ISAXSDataCollection> iSDCIterator = iSAXSDataCollections
				.iterator();
		findFirstDesiredId(iSDCIterator, dataCollectionId1);
		assertEquals(dataCollectionId2, iSDCIterator.next().getId());
		assertEquals(dataCollectionId3, iSDCIterator.next().getId());
		assertEquals(dataCollectionId4, iSDCIterator.next().getId());

		// Test correct experiment ids are returned for a session
		List<Long> experimentIds = bioSAXSISPyB
				.getExperimentsForSession(blsessionId);
		assert (experimentIds.contains(experimentId));

		// Test correct data collectionIds are returned for an experiment
		// TODO check that Jun can preserve the ordering
		List<Long> dataCollectionIds = bioSAXSISPyB
				.getDataCollectionsForExperiments(experimentId);
		assertEquals(dataCollectionCount, dataCollectionIds.size());
		assertEquals(dataCollectionId1, dataCollectionIds.get(0).longValue());
		assertEquals(dataCollectionId2, dataCollectionIds.get(1).longValue());
		assertEquals(dataCollectionId3, dataCollectionIds.get(2).longValue());
		assertEquals(dataCollectionId4, dataCollectionIds.get(3).longValue());

		// Test the correct number of measurement files are returned for a
		// SAXSDATACOLLECTION
		List<SampleInfo> sampleInfoList = bioSAXSISPyB
				.getSaxsDataCollectionInfo(dataCollectionId1);
		// data collection 1 ran successfully so the first sampleInfoList should be
		// populated with buffer before, sample, and buffer after filenames
		assertEquals(1, sampleInfoList.size());
		SampleInfo info = sampleInfoList.get(0);
		assert(info.getBufferBeforeFileName().equals(getFilename(1)));
		assert(info.getSampleFileName().equals(getFilename(2)));
		assert(info.getBufferAfterFileName().equals(getFilename(3)));
	}

	protected static String getFilename(int fileNumber) {
		return "/dls/b21/data/2013/sm999-9/b21-" + fileNumber + ".nxs";
	}

	private void findFirstDesiredId(Iterator<ISAXSDataCollection> iSDCIterator,
			long dataCollectionId1) {
		long iteratorCollectionId = 0;
		while (iSDCIterator.hasNext()
				&& iteratorCollectionId != dataCollectionId1) {
			ISAXSDataCollection collection = iSDCIterator.next();
			if (collection == null) {
				continue;
			}
			iteratorCollectionId = collection.getId();
		}
	}
}
