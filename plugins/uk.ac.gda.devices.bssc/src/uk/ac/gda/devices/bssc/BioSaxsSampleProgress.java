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

import uk.ac.gda.beans.ObservableModel;

public class BioSaxsSampleProgress extends ObservableModel implements ISampleProgress {
	private double collectionProgress;
	private double reductionProgress;
	private double analysisProgress;
	private String experimentId;
	private String sampleName;
	private String collectionStatus;
	private String reductionStatus;
	private String analysisStatus;

	public BioSaxsSampleProgress() {
		
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

	public void setCollectionProgress(double newVal) {
		firePropertyChange(ISampleProgress.COLLECTION_PROGRESS, this.collectionProgress,
				this.collectionProgress = newVal);
	}

	public void setReductionProgress(double newVal) {
		firePropertyChange(ISampleProgress.REDUCTION_PROGRESS, this.reductionProgress, this.reductionProgress = newVal);
	}

	public void setAnalysisProgress(double newVal) {
		firePropertyChange(ISampleProgress.ANALYSIS_PROGRESS, this.analysisProgress, this.analysisProgress = newVal);
	}

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getCollectionStatus() {
		return collectionStatus;
	}

	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}

	public String getReductionStatus() {
		return reductionStatus;
	}

	public void setReductionStatus(String reductionStatus) {
		this.reductionStatus = reductionStatus;
	}

	public String getAnalysisStatus() {
		return analysisStatus;
	}

	public void setAnalysisStatus(String analysisStatus) {
		this.analysisStatus = analysisStatus;
	}

}
