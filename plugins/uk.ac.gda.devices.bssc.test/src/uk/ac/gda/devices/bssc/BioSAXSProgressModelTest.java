package uk.ac.gda.devices.bssc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.beans.BioSaxsSampleProgress;
import uk.ac.gda.devices.bssc.beans.ISampleProgress;
import uk.ac.gda.devices.bssc.beans.ISampleProgressCollection;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSDBFactory;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;

public class BioSAXSSampleProgressCollectionTest {

	private static BioSAXSISPyB bioSAXSISPyB;
	private static ISampleProgressCollection model;

	@BeforeClass
	public static void testSetup() {
		new BioSAXSDBFactory()
				.setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
		// populate model with sample values
		model = new MyISampleProgressCollection();
		populateModel();
	}

	@AfterClass
	public static void testCleanup() {
		try {
			bioSAXSISPyB.disconnect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void populateModel() {
		for (int i = 0; i < 20; i++) {
			BioSaxsSampleProgress progress = new BioSaxsSampleProgress();
			progress.setExperimentId(String.valueOf(i));
			model.add(new BioSaxsSampleProgress());
		}
	}

	@Test
	public void testGetItems() {
		List<ISampleProgress> progressItems = new ArrayList<ISampleProgress>();

		for (int i = 0; i < 7; i++) {
			progressItems.add(new BioSaxsSampleProgress());
		}
		model.addItems(progressItems);

		assertEquals(7, model.getItems().size());
	}

	@Test
	public void testClearItems() {
		model.clearItems();
		assertEquals(0, model.getItems().size());
	}

	@Test
	public void testAddItems() {
		List<ISampleProgress> progressItems = new ArrayList<ISampleProgress>();

		for (int i = 0; i < 7; i++) {
			progressItems.add(new BioSaxsSampleProgress());
		}
		model.addItems(progressItems);

		assertEquals(7, model.getItems().size());
	}

}

class MyISampleProgressCollection extends ArrayList<ISampleProgress> implements
		ISampleProgressCollection {

	/**
* 
*/
	private static final long serialVersionUID = 1L;

	WritableList items = new WritableList(new ArrayList<ISampleProgress>(),
			ISampleProgress.class);

	@Override
	public WritableList getItems() {
		return items;
	}

	@Override
	public void clearItems() {
		items.clear();
	}

	@Override
	public void addItems(List<ISampleProgress> bioSAXSSamples) {
		items.add(bioSAXSSamples);
	}

	@Override
	public void pollISpyB() {
		// TODO Auto-generated method stub

	}
}
