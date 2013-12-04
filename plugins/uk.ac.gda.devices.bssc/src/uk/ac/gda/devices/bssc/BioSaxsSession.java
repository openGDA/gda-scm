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

package uk.ac.gda.devices.bssc;

import java.util.ArrayList;
import java.util.List;

public class BioSaxsSession {
	private String name;
	private List<BioSaxsMeasurement> measurements;

	public List<BioSaxsMeasurement> getMeasurements() {
		return measurements;
	}

	public BioSaxsSession(String name) {
		this.name = name;
		measurements = new ArrayList<BioSaxsMeasurement>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addMeasurement(BioSaxsMeasurement bioSaxsMeasurement) {
		measurements.add(bioSaxsMeasurement);
	}

}
