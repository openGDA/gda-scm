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

import gda.device.detector.xspress.XspressBeanUtils;
import gda.device.detector.xspress.XspressDetectorConfiguration;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
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
	
	public BM26aDetectorPreparer(XspressDetectorConfiguration xspressConfig) {
		this.xspressConfig = xspressConfig;
//		this.vortexConfig = vortexConfig;
	}
	
	@Override
	public void configure(IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean, String experimentFullPath) throws Exception {
		logger.debug("Preparing detector parameters");
		if (detectorBean.getExperimentType().equals("Fluorescence")) {
			FluorescenceParameters fluoresenceParameters = detectorBean.getFluorescenceParameters();
			if (fluoresenceParameters.isCollectDiffractionImages()) {
//				_control_mythen(fluoresenceParameters, outputBean, experimentFullPath);
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

	@Override
	public void beforeEachRepetition() throws Exception {
		// 
	}
}
