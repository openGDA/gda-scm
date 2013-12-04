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
	private int wellRow;
	private int wellColumn;
	private String name;
	private int collectionProgress;
	private int reductionProgress;
	private int analysisProgress;

	public BioSaxsMeasurement(BioSaxsSession session, int wellRow, int wellColumn, String name) {
		this.session = session;
		this.wellRow = wellRow;
		this.wellColumn = wellColumn;
		this.name = name;
	}

	public int getWellRow() {
		return wellRow;
	}

	public void setWellRow(int wellRow) {
		this.wellRow = wellRow;
	}

	public int getWellColumn() {
		return wellColumn;
	}

	public void setWellColumn(int wellColumn) {
		this.wellColumn = wellColumn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BioSaxsSession getSession() {
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