package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

public class BioSAXSISPyBviaOracleTest {

	static BioSAXSISPyB bioSAXSISPyB = null;
	static BioSAXSISPyBviaOracle bioSAXSViaOracle = null;
	
	@BeforeClass
	public static void setup() {
		new BioSAXSDBFactory().setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
		
	}
	
	@Test
	public void testGetSaxsSamples() {
		String visit = "cm9877-1";
		
		try {
			bioSAXSISPyB.getSaxsSamples();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
