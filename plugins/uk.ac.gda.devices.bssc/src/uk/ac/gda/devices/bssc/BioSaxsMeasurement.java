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

public class BioSaxsMeasurement {
	private BioSaxsSession session;
	private int wellPosition;
	private String name;
	private int collectionProgress;
	private int reductionProgress;
	private int analysisProgress;

	public BioSaxsMeasurement(BioSaxsSession session, int wellposition, String name) {
		this.session = session;
		this.wellPosition = wellposition;
		this.name = name;
	}

	public int getWellPosition() {
		return wellPosition;
	}

	public void setWellPosition(int wellPosition) {
		this.wellPosition = wellPosition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getSession() {
		return session;
	}

	public int getCollectionProgress() {
		return collectionProgress;
	}

	public void setCollectionProgress(int collectionProgress) {
		this.collectionProgress = collectionProgress;
	}

	public int getReductionProgress() {
		return reductionProgress;
	}

	public void setReductionProgress(int reductionProgress) {
		this.reductionProgress = reductionProgress;
	}

	public int getAnalysisProgress() {
		return analysisProgress;
	}

	public void setAnalysisProgress(int analysisProgress) {
		this.analysisProgress = analysisProgress;
	}

}
