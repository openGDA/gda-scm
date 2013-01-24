package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.*;

import java.sql.SQLException;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSOracleUtils;

import org.junit.BeforeClass;
import org.junit.Test;

public class BioSAXSDBUtilsTest {

	static BioSAXSOracleUtils bioSAXSDBUtils = null;
	
	@BeforeClass
	public static void setup() {
		bioSAXSDBUtils = new BioSAXSOracleUtils(BioSAXSISPyB.MODE.testing);
	}
	
	@Test
	public void testGetSessionForVisit() throws SQLException {
		assertEquals(bioSAXSDBUtils.getSessionForVisit("nt20-12"), 434);
	}
	
	@Test
	public void testcreateMeasurementsAndRegisterBufferForSample() throws SQLException {
		long bufferId = bioSAXSDBUtils.createBufferMeasurement(434L, (short)0, (short)1, (short)1, 20.0f, 21.0f, 10, 1.0, 2.0, 5.0, 10.0, 
				"viscosity", "/dls/i22/data/2013/sm999-9/i22-9999.nxs", "/entry1/detector/data");
		assertTrue(bufferId >= 0);

		long sampleId = bioSAXSDBUtils.createSampleMeasurement(434L, (short)1, (short)1, (short)2, "The blue one",
				19.0, 20.0f, 21.0f, 10, 1.0, 2.0, 3.0, 11.0, 
				"viscosity", "/dls/i22/data/2013/sm999-9/i22-9999.nxs", "/entry1/detector/data");
		assertTrue(sampleId >= 0);
		
		bioSAXSDBUtils.registerBufferForSample(434L, sampleId, bufferId);
	}
}
