/*-
 * Copyright © 2012 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.devices.bssc.beans;

import java.net.URL;
import java.util.List;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class BSSCSessionBean implements IRichBean{

	static public final URL mappingURL = BSSCSessionBean.class.getResource("BSSCMapping.xml");
	static public final URL schemaURL  = BSSCSessionBean.class.getResource("BSSCMapping.xsd");

	List<TitrationBean> measurements;
	
	public static BSSCSessionBean createFromXML(String filename) throws Exception {
		return (BSSCSessionBean) XMLHelpers.createFromXML(mappingURL, BSSCSessionBean.class, schemaURL, filename);
	}
	
	public static void writeToXML(BSSCSessionBean bean, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, bean, filename);
	}
	
	public List<TitrationBean> getMeasurements() {
		return measurements;
	}
	public void setMeasurements(List<TitrationBean> measurements) {
		this.measurements = measurements;
	}
	@Override
	public void clear() {
		measurements = null;
	}
}