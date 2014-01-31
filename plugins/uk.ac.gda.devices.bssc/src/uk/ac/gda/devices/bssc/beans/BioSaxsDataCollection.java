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

package uk.ac.gda.devices.bssc.beans;

import uk.ac.gda.beans.ObservableModel;

public class BioSaxsDataCollection extends ObservableModel implements ISAXSDataCollection {
	private double collectionProgress;
	private double reductionProgress;
	private double analysisProgress;
	private String experimentId;
	private String sampleName;
	private String collectionStatus;
	private String reductionStatus;
	private String analysisStatus;
	private String visit;
	private long blSessionId;
	private long collectionStartTime;
	private long id;
	private long bufferBeforeMeasurementId;
	private long bufferAfterMeasurementId;

	public BioSaxsDataCollection() {

	}

	@Override
	public double getCollectionProgress() {
		return collectionProgress;
	}

	@Override
	public double getReductionProgress() {
		return reductionProgress;
	}

	@Override
	public double getAnalysisProgress() {
		return analysisProgress;
	}

	@Override
	public void setCollectionProgress(double newVal) {
		firePropertyChange(ISAXSDataCollection.COLLECTION_PROGRESS, this.collectionProgress,
				this.collectionProgress = newVal);
	}

	@Override
	public void setReductionProgress(double newVal) {
		firePropertyChange(ISAXSDataCollection.REDUCTION_PROGRESS, this.reductionProgress, this.reductionProgress = newVal);
	}

	@Override
	public void setAnalysisProgress(double newVal) {
		firePropertyChange(ISAXSDataCollection.ANALYSIS_PROGRESS, this.analysisProgress, this.analysisProgress = newVal);
	}

	@Override
	public String getExperimentId() {
		return experimentId;
	}

	@Override
	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	@Override
	public String getSampleName() {
		return sampleName;
	}

	@Override
	public void setSampleName(String sampleName) {
		firePropertyChange(ISAXSDataCollection.SAMPLE_NAME, this.sampleName, this.sampleName = sampleName);
	}

	@Override
	public long getCollectionStartTime() {
		return collectionStartTime;
	}

	@Override
	public void setCollectionStartTime(long collectionStartTime) {
		firePropertyChange(ISAXSDataCollection.COLLECTION_START_TIME, this.collectionStartTime,
				this.collectionProgress = collectionStartTime);
	}
	
	@Override
	public String getCollectionStatus() {
		return collectionStatus;
	}

	@Override
	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}

	@Override
	public String getReductionStatus() {
		return reductionStatus;
	}

	@Override
	public void setReductionStatus(String reductionStatus) {
		this.reductionStatus = reductionStatus;
	}

	@Override
	public String getAnalysisStatus() {
		return analysisStatus;
	}

	@Override
	public void setAnalysisStatus(String analysisStatus) {
		this.analysisStatus = analysisStatus;
	}

	public String getVisit() {
		return visit;
	}

	public void setVisit(String visit) {
		this.visit = visit;
	}

	@Override
	public long getBlSessionId() {
		return blSessionId;
	}

	@Override
	public void setBlSessionId(long blSessionId) {
		this.blSessionId = blSessionId;
	}

	@Override
	public void setId(long saxsDataCollectionId) {
		this.id = saxsDataCollectionId;
	}

	@Override
	public void setBufferBeforeMeasurementId(long bufferBeforeMeasurementId) {
		this.bufferBeforeMeasurementId = bufferBeforeMeasurementId;
	}

	@Override
	public void setBufferAfterMeasurementId(long bufferAfterMeasurementId) {
		this.bufferAfterMeasurementId = bufferAfterMeasurementId;
	}
}
