package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

public class BioSAXSDBUtilsTest {

	static BioSAXSISPyB bioSAXSISPyB = null;
	
	@BeforeClass
	public static void setup() {
		new BioSAXSDBFactory().setJdbcURL("jdbc:oracle:thin:@sci-serv2.diamond.ac.uk:1521:xe");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
	}
	
	@Test
	public void testcreateMeasurementsAndRegisterBufferForSample() throws SQLException {
		long proposalId = bioSAXSISPyB.getProposalForVisit("nt20-12");
		assertEquals(proposalId, 13);

		long experimentId = bioSAXSISPyB.createExperiment(proposalId, "test", "TEMPLATE", "test");

		long sdc = bioSAXSISPyB.createSaxsDataCollection(experimentId);
		
		long bufferId1 = bioSAXSISPyB.createBufferMeasurement(proposalId, (short)0, (short)1, (short)1, 20.0f, 21.0f, 10, 1.0, 2.0, 5.0, 10.0, 
				"viscosity", "/dls/i22/data/2013/sm999-9/i22-9990.nxs", "/entry1/detector/data");
		assertTrue(bufferId1 >= 0);

		long someid = bioSAXSISPyB.createMeasurementToDataCollection(sdc, bufferId1);
		assertTrue(someid >= 0);

		long sampleId = bioSAXSISPyB.createSampleMeasurement(experimentId, (short)1, (short)1, (short)2, "The blue one",
				19.0, 20.0f, 21.0f, 10, 1.0, 2.0, 3.0, 11.0, 
				"viscosity", "/dls/i22/data/2013/sm999-9/i22-9991.nxs", "/entry1/detector/data");
		assertTrue(sampleId >= 0);
		
		someid = bioSAXSISPyB.createMeasurementToDataCollection(sdc, sampleId);
		assertTrue(someid >= 0);
		
		long bufferId2 = bioSAXSISPyB.createBufferMeasurement(experimentId, (short)0, (short)1, (short)1, 20.0f, 21.0f, 10, 1.0, 2.0, 5.0, 10.0, 
				"viscosity", "/dls/i22/data/2013/sm999-9/i22-9992.nxs", "/entry1/detector/data");
		assertTrue(bufferId2 >= 0);
		
		someid = bioSAXSISPyB.createMeasurementToDataCollection(sdc, bufferId2);
		assertTrue(someid >= 0);

		bioSAXSISPyB.disconnect();
	}
}