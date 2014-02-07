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

public class BioSaxsSampleProgress extends ObservableModel implements ISampleProgress {
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
		this.sampleName = sampleName;
	}

	@Override
	public String getCollectionStatus() {
		return collectionStatus;
	}

	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}

	@Override
	public String getReductionStatus() {
		return reductionStatus;
	}

	public void setReductionStatus(String reductionStatus) {
		this.reductionStatus = reductionStatus;
	}

	@Override
	public String getAnalysisStatus() {
		return analysisStatus;
	}

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
	public long getCollectionStartTime() {
		// TODO Auto-generated method stub
		return collectionStartTime;
	}

}