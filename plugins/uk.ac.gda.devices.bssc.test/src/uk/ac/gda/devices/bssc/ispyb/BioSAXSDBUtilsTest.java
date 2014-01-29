package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.ui.MeasurementsFieldComposite;

public class BioSAXSDBUtilsTest {

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
		// These steps are done prior to the experiment being run
		String visit = "nt20-12";

		long proposalId = bioSAXSISPyB.getProposalForVisit(visit);
		assertEquals(proposalId, 13L);

		long blsessionId = bioSAXSISPyB.getSessionForVisit(visit);
		assertEquals(blsessionId, 434L);

		long experimentId = bioSAXSISPyB.createExperiment(proposalId, "test",
				"TEMPLATE", "test");

		long saxsDataCollectionId = bioSAXSISPyB.createSaxsDataCollection(
				blsessionId, experimentId);

		long specimenId = bioSAXSISPyB.createSpecimenForMeasurement();

		long bufferBeforeMeasurementId = bioSAXSISPyB.createBufferMeasurement(
				blsessionId, saxsDataCollectionId, specimenId, true, (short) 0,
				(short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0, 10.0,
				"viscosity", "/dls/i22/data/2013/sm999-9/i22-9990.nxs",
				"/entry1/detector/data");
		assertTrue(bufferBeforeMeasurementId >= 0);

		long someid = bioSAXSISPyB.createMeasurementToDataCollection(
				saxsDataCollectionId, bufferBeforeMeasurementId);
		assertTrue(someid >= 0);

		long sampleMeasurementId = bioSAXSISPyB.createSampleMeasurement(
				blsessionId, saxsDataCollectionId, specimenId, (short) 1,
				(short) 1, (short) 2, 20.0f, 10, 1.0, 2.0, 3.0, 11.0,
				"viscosity", "/dls/i22/data/2013/sm999-9/i22-9991.nxs",
				"/entry1/detector/data");
		assertTrue(sampleMeasurementId >= 0);

		someid = bioSAXSISPyB.createMeasurementToDataCollection(
				saxsDataCollectionId, sampleMeasurementId);
		assertTrue(someid >= 0);

		long bufferAfterMeasurementId = bioSAXSISPyB.createBufferMeasurement(
				blsessionId, saxsDataCollectionId, specimenId, false,
				(short) 0, (short) 1, (short) 1, 20.0f, 10, 1.0, 2.0, 5.0,
				10.0, "viscosity", "/dls/i22/data/2013/sm999-9/i22-9992.nxs",
				"/entry1/detector/data");
		assertTrue(bufferAfterMeasurementId >= 0);

		someid = bioSAXSISPyB.createMeasurementToDataCollection(
				saxsDataCollectionId, bufferAfterMeasurementId);
		assertTrue(someid >= 0);

		// These steps would be run when the experiment is run
		long bufferBeforeRunId = bioSAXSISPyB.measurementStarted(
				bufferBeforeMeasurementId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
		assertTrue(bufferBeforeRunId >= 0);
		
		long sampleRunId = bioSAXSISPyB.measurementStarted(sampleMeasurementId,
				1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0);
		assertTrue(sampleRunId >= 0);
		
		long bufferAfterRunId = bioSAXSISPyB.measurementStarted(
				bufferAfterMeasurementId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
		assertTrue(bufferAfterRunId >= 0);
		
		bioSAXSISPyB.measurementDone(bufferBeforeMeasurementId);

		bioSAXSISPyB.measurementDone(sampleMeasurementId);

		bioSAXSISPyB.measurementDone(bufferAfterMeasurementId);

		bioSAXSISPyB.measurementFailed(bufferBeforeMeasurementId);

		bioSAXSISPyB.measurementFailed(sampleMeasurementId);

		bioSAXSISPyB.measurementFailed(bufferAfterMeasurementId);

		bioSAXSISPyB.isMeasurementDone(bufferBeforeMeasurementId);

		bioSAXSISPyB.isMeasurementDone(sampleMeasurementId);

		bioSAXSISPyB.isMeasurementDone(bufferAfterMeasurementId);

		bioSAXSISPyB.isMeasurementSuccessful(bufferBeforeMeasurementId);

		bioSAXSISPyB.isMeasurementSuccessful(sampleMeasurementId);

		bioSAXSISPyB.isMeasurementSuccessful(bufferAfterMeasurementId);

		bioSAXSISPyB.isMeasurementFailed(bufferBeforeMeasurementId);

		bioSAXSISPyB.isMeasurementFailed(sampleMeasurementId);

		bioSAXSISPyB.isMeasurementFailed(bufferAfterMeasurementId);

		bioSAXSISPyB.disconnect();
	}
}