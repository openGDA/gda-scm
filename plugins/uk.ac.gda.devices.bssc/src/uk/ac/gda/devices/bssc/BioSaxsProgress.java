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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class BioSaxsMeasurement implements PropertyChangeListener {
	private BioSaxsSession session;
	private int wellRow;
	private int wellColumn;
	private String name;
	private String position;
	private int collectionProgress;
	private int reductionProgress;
	private int analysisProgress;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public BioSaxsMeasurement(BioSaxsSession session, int wellRow, int wellColumn, String name) {
		this.session = session;
		this.wellRow = wellRow;
		this.wellColumn = wellColumn;
		this.name = name;
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public int getWellRow() {
		return wellRow;
	}

	public void setWellRow(int wellRow) {
		propertyChangeSupport.firePropertyChange("wellRow", this.wellRow, this.wellRow = wellRow);
	}

	public int getWellColumn() {
		return wellColumn;
	}

	public void setWellColumn(int wellColumn) {
		propertyChangeSupport.firePropertyChange("wellColumn", this.wellColumn, this.wellColumn = wellColumn);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public String getSessionName() {
		return session.getName();
	}

	public int getCollectionProgress() {
		return collectionProgress;
	}

	public void setCollectionProgress(int collectionProgress) {
		propertyChangeSupport.firePropertyChange("collectionProgress", this.collectionProgress,
				this.collectionProgress = collectionProgress);
	}

	public int getReductionProgress() {
		return reductionProgress;
	}

	public void setReductionProgress(int reductionProgress) {
		propertyChangeSupport.firePropertyChange("reductionProgress", this.reductionProgress,
				this.reductionProgress = reductionProgress);
	}

	public int getAnalysisProgress() {
		return analysisProgress;
	}

	public void setAnalysisProgress(int analysisProgress) {
		propertyChangeSupport.firePropertyChange("analysisProgress", this.analysisProgress,
				this.analysisProgress = analysisProgress);
	}

	public String getPosition() {
		position = "(" + wellRow + ", " + wellColumn + ")";
		return position;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange("session", null, session);
	}
}
