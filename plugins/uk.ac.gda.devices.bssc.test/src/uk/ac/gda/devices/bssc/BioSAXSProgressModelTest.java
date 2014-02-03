package uk.ac.gda.devices.bssc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.beans.BioSAXSDataCollection;
import uk.ac.gda.devices.bssc.beans.IProgressModel;
import uk.ac.gda.devices.bssc.beans.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSDBFactory;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;

public class BioSAXSProgressModelTest {

	private static BioSAXSISPyB bioSAXSISPyB;
	private static IProgressModel model;

	@BeforeClass
	public static void testSetup() {
		new BioSAXSDBFactory()
				.setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
		// populate model with sample values
		model = new MyProgressModel();
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
			ISAXSDataCollection progress = new BioSAXSDataCollection();
			progress.setExperimentId(String.valueOf(i));
			model.add(new BioSAXSDataCollection());
		}
	}

	@Test
	public void testGetItems() {
		List<ISAXSDataCollection> progressItems = new ArrayList<ISAXSDataCollection>();

		for (int i = 0; i < 7; i++) {
			progressItems.add(new BioSAXSDataCollection());
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
		List<ISAXSDataCollection> progressItems = new ArrayList<ISAXSDataCollection>();

		for (int i = 0; i < 7; i++) {
			progressItems.add(new BioSAXSDataCollection());
		}
		model.addItems(progressItems);

		assertEquals(7, model.getItems().size());
	}

}

class MyProgressModel extends ArrayList<ISAXSDataCollection> implements
		IProgressModel {

	/**
* 
*/
	private static final long serialVersionUID = 1L;

	WritableList items = new WritableList(new ArrayList<ISAXSDataCollection>(),
			ISAXSDataCollection.class);

	@Override
	public WritableList getItems() {
		return items;
	}

	@Override
	public void clearItems() {
		items.clear();
	}

	@Override
	public void addItems(List<ISAXSDataCollection> bioSAXSSamples) {
		items.add(bioSAXSSamples);
	}
}
