package uk.ac.gda.devices.bssc.ispyb;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class BioSAXSISPyBviaOracleTest extends BioSAXSScriptTest {

	@BeforeClass
	public static void setUpBeforeClass() {
		new BioSAXSDBFactory()
				.setJdbcURL("jdbc:oracle:thin:@ws096.diamond.ac.uk:1521:xe");
		BioSAXSDBFactory.setNotifyObject(new NotifyISpyBObserversObject("localhost", "9877"));
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
}
