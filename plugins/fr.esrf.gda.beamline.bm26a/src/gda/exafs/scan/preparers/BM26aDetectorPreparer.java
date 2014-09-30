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

import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.xspress.XspressBeanUtils;
import gda.device.detector.xspress.XspressDetectorConfiguration;
import gda.scan.StaticScan;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IExperimentDetectorParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BM26aDetectorPreparer implements DetectorPreparer {
	private static final Logger logger = LoggerFactory.getLogger(BM26aDetectorPreparer.class);
	private XspressDetectorConfiguration xspressConfig;
	private Scannable energy_scannable;
	private Detector mythen_scannable;
	
	public BM26aDetectorPreparer(Scannable energy_scannable, XspressDetectorConfiguration xspressConfig) {
//	public BM26aDetectorPreparer(Scannable energy_scannable, Detector mythen, XspressDetectorConfiguration xspressConfig) {
		this.xspressConfig = xspressConfig;
		this.energy_scannable = energy_scannable;
//		this.vortexConfig = vortexConfig;
	}
	
	@Override
	public void prepare(IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean, String experimentFullPath) throws Exception {
		logger.debug("Preparing detector parameters");
		if (detectorBean.getExperimentType().equals("Fluorescence")) {
			FluorescenceParameters fluoresenceParameters = detectorBean.getFluorescenceParameters();
			if (fluoresenceParameters.isCollectDiffractionImages()) {
//				control_mythen(fluoresenceParameters, outputBean, experimentFullPath);
				String detType = fluoresenceParameters.getDetectorType();
				String xmlFileName = experimentFullPath + fluoresenceParameters.getConfigFileName();
				if (detType.equals("Germanium")) {
					XspressParameters xspressBean = XspressBeanUtils.createBeanFromXML(xmlFileName);
					xspressConfig.setOnlyShowFF(xspressBean.isOnlyShowFF());
					xspressConfig.setShowDTRawValues(xspressBean.isShowDTRawValues());
					xspressConfig.setSaveRawSpectrum(xspressBean.isSaveRawSpectrum());
					xspressConfig.configure(xmlFileName);
				} else if (detType.equals("Silicon")) {
//					vortexConfig.initialize()
//					vortexBean = self.vortexConfig.createBeanFromXML(xmlFileName);
//					saveRawSpectrum = vortexBean.isSaveRawSpectrum();
//					vortexConfig.configure(xmlFileName, saveRawSpectrum);
				}
//				_control_all_ionc(fluoresenceParameters.getIonChamberParameters());
			} else  if (detectorBean.getExperimentType().equals("Transmission")) {
				TransmissionParameters transmissionParameters = detectorBean.getTransmissionParameters();
				if (transmissionParameters.isCollectDiffractionImages()) {
//					_control_mythen(transmissionParameters, outputBean, experimentFullPath);
				}
//				_control_all_ionc(transmissionParameters.getIonChamberParameters())
			}
		}
	}
	
	// this will be called at the end of a loop of scans or after an abort
	@Override
	public void completeCollection() {
		return;
	}

	private void control_mythen(IExperimentDetectorParameters fluoresenceParameters, IOutputParameters outputBean,
			String experimentFullPath) throws Exception {

		String experimentFolderName = experimentFullPath.substring(experimentFullPath.indexOf("xml") + 4,
				experimentFullPath.length());
		String nexusSubFolder = experimentFolderName + "/" + outputBean.getNexusDirectory();
		String asciiSubFolder = experimentFolderName + "/" + outputBean.getAsciiDirectory();

		// print "Moving DCM for Mythen image..."
		energy_scannable.moveTo(fluoresenceParameters.getMythenEnergy());

		mythen_scannable.setCollectionTime(fluoresenceParameters.getMythenTime());

//		mythen_scannable.setSubDirectory(experimentFolderName);
		XasAsciiNexusDataWriter dataWriter = new XasAsciiNexusDataWriter();
		dataWriter.setRunFromExperimentDefinition(false);
		dataWriter.setNexusFileNameTemplate(nexusSubFolder + "/%d-mythen.nxs");
		dataWriter.setAsciiFileNameTemplate(asciiSubFolder + "/%d-mythen.dat");

		StaticScan staticscan = new StaticScan(new Scannable[] { mythen_scannable });
		staticscan.setDataWriter(dataWriter);
		// print "Collecting a diffraction image...";
		staticscan.run();
		// print "Diffraction scan complete.";
	}

	@Override
	public void beforeEachRepetition() throws Exception {
		// do nothing
	}
}
