package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BioSAXSISPyBviaOracleTest {

	static BioSAXSISPyB bioSAXSISPyB = null;
	static BioSAXSISPyBviaOracle bioSAXSViaOracle = null;

	@BeforeClass
	public static void setup() {
		new BioSAXSDBFactory()
				.setJdbcURL("jdbc:oracle:thin:@ws096.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();

	}

	@AfterClass
	public static void testCleanup() {
	    try {
			bioSAXSISPyB.disconnect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetBioSAXSSamples() {
		String visit = null;
		long blSessionId;
		
		try {
			visit = "mx9426-2";
			blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
			
			List<ISAXSDataCollection> progressList = bioSAXSISPyB
					.getSAXSDataCollections(blSessionId);

			// check blSessionId for each ISampleProgress object is correct
			for (ISAXSDataCollection progress : progressList) {
				assertEquals(blSessionId, progress.getBlSessionId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
