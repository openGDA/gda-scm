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

		long saxsDataCollectionId = bioSAXSISPyB.createSaxsDataCollection(
				experimentId, (short) 0, (short) 1, (short) 1, "Sample", (short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0,
				2.0, 5.0, 10.0, "viscosity");

		assertEquals(
				bioSAXSISPyB.getDataCollectionStatus(saxsDataCollectionId),
				ISpyBStatus.NOT_STARTED);

		// These steps will be done when the experiment is being run (in the run
		// module of the BSSC.py)
		long bufferBeforeRunId = bioSAXSISPyB.createBufferRun(prefCollectionId, saxsDataCollectionId,
				1.0, 20.0f, 20.0f, 10.0, 10, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9990.nxs",
				"/entry1/detector/data");
		
		assertTrue(bufferBeforeRunId >= 0);
		assertEquals(
				bioSAXSISPyB.getDataCollectionStatus(saxsDataCollectionId),
				ISpyBStatus.RUNNING); //TODO get progress as well

		long sampleRunId = bioSAXSISPyB.createSampleRun(saxsDataCollectionId, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9991.nxs",
				"/entry1/detector/data");
		assertTrue(sampleRunId >= 0);
		assertEquals(
				bioSAXSISPyB.getDataCollectionStatus(saxsDataCollectionId),
				ISpyBStatus.RUNNING);

		long bufferAfterRunId = bioSAXSISPyB.createBufferRun(
				saxsDataCollectionId, nextCollectionId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				"/dls/b21/data/2013/sm999-9/b21-9992.nxs",
				"/entry1/detector/data");
		assertTrue(bufferAfterRunId >= 0);
		assertEquals(
				bioSAXSISPyB.getDataCollectionStatus(saxsDataCollectionId),
				ISpyBStatus.COMPLETE);

		// create a data collection gets updated with a FAILED status if any of
		// the measurements for that data collection fail
		saxsDataCollectionId = bioSAXSISPyB.createSaxsDataCollection(experimentId,
				(short) 0, (short) 1, (short) 1, "Sample", (short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity");

		assertEquals(
				bioSAXSISPyB.getDataCollectionStatus(saxsDataCollectionId),
				ISpyBStatus.NOT_STARTED);

		bioSAXSISPyB.setMeasurementStatus(saxsDataCollectionId,
				bufferAfterId, ISpyBStatus.FAILED);
		assertEquals(
				bioSAXSISPyB.getDataCollectionStatus(saxsDataCollectionId),
				ISpyBStatus.FAILED);
		
		// start data reduction and assert it is in the running state
		long subtractionId = bioSAXSISPyB
				.createDataReduction(saxsDataCollectionId);

		assertEquals(bioSAXSISPyB.getDataReductionStatus(saxsDataCollectionId),
				ISpyBStatus.RUNNING);

		// test data reduction completed
		bioSAXSISPyB
				.setDataReductionStatus(saxsDataCollectionId,
						ISpyBStatus.COMPLETE,
						"/dls/b21/data/2013/sm999-9/b21-9993.nxs");
		assertEquals(bioSAXSISPyB.getDataReductionStatus(saxsDataCollectionId), ISpyBStatus.COMPLETE);
		
		// test data reduction failed
		bioSAXSISPyB.setDataReductionStatus(saxsDataCollectionId,
				ISpyBStatus.FAILED, "");
		assertEquals(bioSAXSISPyB.getDataReductionStatus(saxsDataCollectionId),
				ISpyBStatus.FAILED);

		// start data analysis and assert it is in the running state
		long analysisId = bioSAXSISPyB.createDataAnalysis(saxsDataCollectionId);

		assertEquals(bioSAXSISPyB.getDataAnalysisStatus(saxsDataCollectionId),
				ISpyBStatus.RUNNING);

		// test data analysis completed
		bioSAXSISPyB.setDataAnalysisStatus(saxsDataCollectionId, ISpyBStatus.COMPLETE, "/dls/b21/data/2013/sm999-9/b21-9994.nxs");
		assertEquals(bioSAXSISPyB.getDataAnalysisStatus(saxsDataCollectionId), ISpyBStatus.COMPLETE);
		
		// test data analysis failed
		bioSAXSISPyB.setDataAnalysisStatus(saxsDataCollectionId,
				ISpyBStatus.FAILED, "");

		assertEquals(bioSAXSISPyB.getDataAnalysisStatus(saxsDataCollectionId),
				ISpyBStatus.FAILED);

		bioSAXSISPyB.disconnect();
	}
}