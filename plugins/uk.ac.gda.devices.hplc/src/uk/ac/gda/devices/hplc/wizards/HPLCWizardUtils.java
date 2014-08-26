package uk.ac.gda.devices.hplc.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import uk.ac.gda.devices.hplc.beans.HPLCSessionBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class HPLCWizardUtils {

	public static InputStream sessionBeanToStream(HPLCSessionBean sessionBean) {
		try {
			File tempFile = File.createTempFile("hplc-", ".xml");
			tempFile.deleteOnExit();
			XMLHelpers.writeToXML(HPLCSessionBean.mappingURL, sessionBean, tempFile);
			return new FileInputStream(tempFile);
		} catch (Exception e) {
		}
		return null;
	}
}
