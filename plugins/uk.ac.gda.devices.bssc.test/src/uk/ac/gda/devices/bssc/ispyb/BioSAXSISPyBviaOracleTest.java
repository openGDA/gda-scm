package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.*;

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;

import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.BioSaxsSampleProgress;
import uk.ac.gda.devices.bssc.ISampleProgress;

public class BioSAXSISPyBviaOracleTest {

	static BioSAXSISPyB bioSAXSISPyB = null;
	static BioSAXSISPyBviaOracle bioSAXSViaOracle = null;

	@BeforeClass
	public static void setup() {
		new BioSAXSDBFactory()
				.setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();

	}

	@Test
	public void testGetBioSAXSSamples() {
		String visit = null;
		long blSessionId;
		
		try {
			visit = "cm4977-1";
			blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
			
			List<ISampleProgress> progressList = bioSAXSISPyB
					.getBioSAXSSamples(blSessionId);

			// check blSessionId for each ISampleProgress object is correct
			for (ISampleProgress progress : progressList) {
				assertEquals(blSessionId, progress.getBlSessionId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetBioSAXSSamplesForExperiment() {
		String experimentID = "34";
		String visit = null;
		long blSessionId;
		
		try {
			visit = "cm4977-1";
			blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
			
			List<ISampleProgress> progressList = bioSAXSISPyB
					.getBioSAXSSamples(experimentID, blSessionId);

			for (ISampleProgress progress : progressList) {
				assertEquals(experimentID, progress.getExperimentId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
