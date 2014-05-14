/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ispyb;

public class BioSAXSDBFactory {
	
	private static String jdbcURL;
	private static NotifyISpyBObserversObject notifyObject;
	
	public static BioSAXSISPyB makeAPI() {
		if (jdbcURL == null) {
			throw new IllegalArgumentException("jdbcURL unspecified");
		}
		return new BioSAXSISPyBviaOracle(jdbcURL, notifyObject);
	}

	public String getJdbcURL() {
		return jdbcURL;
	}

	public void setJdbcURL(String jdbcURL) {
		BioSAXSDBFactory.jdbcURL = jdbcURL;
	}

	public static NotifyISpyBObserversObject getNotifyObject() {
		return notifyObject;
	}

	public static void setNotifyObject(NotifyISpyBObserversObject notifyObject) {
		BioSAXSDBFactory.notifyObject = notifyObject;
	}
}