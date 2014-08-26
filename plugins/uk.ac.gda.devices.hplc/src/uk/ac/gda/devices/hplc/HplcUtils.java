/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hplc;

import java.io.File;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;

public class HplcUtils {

	private static final String VISIT_DIRECTORY_PROPERTY = "gda.data.visitdirectory";
	
	private static final String DEFAULT_FILE_NAME = "default";
	private static final String HPLC_EXTENSION = "hplc";
//	public static final PlateConfig PLATE_SETUP = Finder.getInstance().find("bsscPlates");
	
	public static String getXmlDirectory() {
		return PathConstructor.createFromTemplate(LocalProperties.get(VISIT_DIRECTORY_PROPERTY) + "/xml/");
	}
	
	public static File getNewFileFromName(String name) {
		return new File(String.format("%s/%s.%s", getXmlDirectory(), name, HPLC_EXTENSION));
	}
	
	public static File getDefaultFile() {
		return getNewFileFromName(DEFAULT_FILE_NAME);
	}
	
}
