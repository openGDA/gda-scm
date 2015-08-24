/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.exafs.scan.preparers;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.mythen.TangoMythenDetector;
import gda.device.detector.xspress.Xspress1System;
import gda.device.scannable.TangoMythenDetectorTrigger;
import gda.exafs.scan.ExafsScanPointCreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IExperimentDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;

public class BM26aDetectorPreparer implements DetectorPreparer, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(BM26aDetectorPreparer.class);
	private Scannable energyScannable;
	private Xspress1System xspressSystem;
//	private Xmap vortexDetector;
//	private Xspress3Detector xspress3Detector;
	private TangoMythenDetectorTrigger mythenDetectorTrigger;
	private IScanParameters scanBean;
	private IDetectorParameters detectorBean;

	public BM26aDetectorPreparer() {
	}

	public Scannable getEnergyScannable() {
		return energyScannable;
	}

	public void setEnergyScannable(Scannable energyScannable) {
		this.energyScannable = energyScannable;
	}

	public Xspress1System getXspressSystem() {
		return xspressSystem;
	}

	public void setXspressSystem(Xspress1System xspressSystem) {
		this.xspressSystem = xspressSystem;
	}

	public TangoMythenDetectorTrigger getTangoMythenDetectorTrigger() {
		return mythenDetectorTrigger;
	}

	public void setTangoMythenDetectorTrigger(TangoMythenDetectorTrigger mythenDetectorTrigger) {
		this.mythenDetectorTrigger = mythenDetectorTrigger;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (energyScannable == null) {
			logger.error("BM26aDetectorPreparer(): enery scannable is not set");
		}
	}

	@Override
	public void configure(IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean, String experimentFullPath) throws Exception {
		logger.debug("Preparing bm26a detector parameters");
		this.scanBean = scanBean;
		this.detectorBean = detectorBean;

		if (detectorBean.getExperimentType().equalsIgnoreCase("Fluorescence")) {
			FluorescenceParameters fluoresenceParameters = detectorBean.getFluorescenceParameters();
			if (fluoresenceParameters.isCollectDiffractionImages()) {
				control_mythen(fluoresenceParameters, outputBean, experimentFullPath);
			}
			String detType = fluoresenceParameters.getDetectorType();
			String xmlFileName = experimentFullPath + fluoresenceParameters.getConfigFileName();
			if (detType == "Germanium") {
				xspressSystem.setConfigFileName(xmlFileName);
				xspressSystem.configure();
			} else if (detType == "Silicon") {
//				vortexDetector.setConfigFileName(xmlFileName);
//				vortexDetector.configure();
			} else if (detType == "Xspress3") {
//				xspress3Detector.setConfigFileName(xmlFileName);
//				xspress3Detector.configure();
			}
//			control_all_ionc(fluoresenceParameters.getIonChamberParameters());
		} else if (detectorBean.getExperimentType().equalsIgnoreCase("Transmission")) {
			TransmissionParameters transmissionParameters = detectorBean.getTransmissionParameters();
			if (transmissionParameters.isCollectDiffractionImages()) {
				control_mythen(transmissionParameters, outputBean, experimentFullPath);
			}
//			control_all_ionc(transmissionParameters.getIonChamberParameters());
		}
	}

	// this will be called at the end of a loop of scans or after an abort
	@Override
	public void completeCollection() {
		return;
	}

	private void control_mythen(IExperimentDetectorParameters detectorParameters, IOutputParameters outputBean,
			String experimentFullPath) throws Exception {

		String experimentFolderName = experimentFullPath.substring(experimentFullPath.indexOf("xml") + 4,
				experimentFullPath.length());
		String nexusSubFolder = experimentFolderName + outputBean.getNexusDirectory();
		// String asciiSubFolder = experimentFolderName + outputBean.getAsciiDirectory();

		mythenDetectorTrigger.setStartAtScanPoint(calculateStartPoint(detectorParameters.getMythenEnergy()));
		TangoMythenDetector mythenDetector = mythenDetectorTrigger.getMythenDetector();
		mythenDetector.writeSavingDirectory(nexusSubFolder);
		mythenDetector.setCollectionTime(detectorParameters.getMythenTime());
		mythenDetector.writeNbFrames(detectorParameters.getMythenFrames());
		mythenDetector.writeSavingFramePerFile(detectorParameters.getMythenFrames());
		// XasAsciiNexusDataWriter dataWriter = new XasAsciiNexusDataWriter();
		// dataWriter.setRunFromExperimentDefinition(false);
		// dataWriter.setNexusFileNameTemplate(nexusSubFolder + "/%d-mythen.nxs");
		// dataWriter.setAsciiFileNameTemplate(asciiSubFolder + "/%d-mythen.dat");
	}

	@Override
	public void beforeEachRepetition() throws Exception {
		// do nothing
	}

	@Override
	public Detector[] getExtraDetectors() {
		// not required for this beamline
		return null;
	}

	private int calculateStartPoint(double energy) throws Exception {
		double scanEnergies[][] = ExafsScanPointCreator.calculateScanEnergies((XasScanParameters) scanBean);
		int scanPoint = 1;
		for (int i = 0; i < scanEnergies.length; i++) {
			if (scanEnergies[i][0] > energy) {
				scanPoint = i - 1;
				break;
			}
		}
		return scanPoint;
	}
}
