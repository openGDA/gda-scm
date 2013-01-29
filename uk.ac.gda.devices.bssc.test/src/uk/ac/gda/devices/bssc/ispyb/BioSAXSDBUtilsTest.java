package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.apache.commons.lang.NotImplementedException;
import org.junit.BeforeClass;
import org.junit.Test;

public class BioSAXSDBUtilsTest {

	static BioSAXSISPyB bioSAXSISPyB = null;
	
	@BeforeClass
	public static void setup() {
		
		try {
			bioSAXSISPyB = BioSAXSDBFactory.makeAPI(BioSAXSISPyB.RDBMSTYPE.Oracle, BioSAXSISPyB.MODE.testing);
		}
		catch (NotImplementedException nie) {
			String msg = nie.getMessage();
			if (msg == null)
				msg = "Not implemented.";
			
			System.err.println(msg);
		}
	}
	
	@Test
	public void testcreateMeasurementsAndRegisterBufferForSample() throws SQLException {
		if (bioSAXSISPyB != null) {
			assertEquals(bioSAXSISPyB.getSessionForVisit("nt20-12"), 434);

			long bufferId = bioSAXSISPyB.createBufferMeasurement(434L, (short)0, (short)1, (short)1, 20.0f, 21.0f, 10, 1.0, 2.0, 5.0, 10.0, 
					"viscosity", "/dls/i22/data/2013/sm999-9/i22-9999.nxs", "/entry1/detector/data");
			assertTrue(bufferId >= 0);

			long sampleId = bioSAXSISPyB.createSampleMeasurement(434L, (short)1, (short)1, (short)2, "The blue one",
					19.0, 20.0f, 21.0f, 10, 1.0, 2.0, 3.0, 11.0, 
					"viscosity", "/dls/i22/data/2013/sm999-9/i22-9999.nxs", "/entry1/detector/data");
			assertTrue(sampleId >= 0);

			bioSAXSISPyB.registerBufferForSample(434L, sampleId, bufferId);

			bioSAXSISPyB.disconnect();
		}
	}
}
