/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.bm26a.SampleParameters;

/**
 * Sets up Sample Parameters for BM26a. Talks to hardware with hard coded parameter names.
 * <p>
 * Every piece of hardware should also add its parameters to the nexus file header.
 */
public class SampleParametersManager extends ParametersManager {

	private static final Logger logger = LoggerFactory.getLogger(SampleParametersManager.class);

	private final SampleParameters sampleParameters;
	private final ObservableComponent controller;

	public SampleParametersManager(SampleParameters sampleParameters, ObservableComponent controller) {
		super();
		this.sampleParameters = sampleParameters;
		this.controller = controller;
	}

	/**
	 * Call to configure the sample.
	 * 
	 * @throws Exception
	 */
	@Override
	public void init() throws Exception {
		createSampleEnvironment();
	}

	private void log(String message) {
		if (this.controller != null) {
			controller.notifyIObservers(this, new ScriptProgressEvent(message));
		}
		logger.info(message);
	}

	private void createSampleEnvironment() throws Exception {

//		NexusExtraMetadataDataWriter.removeAllMetadataEntries();
//
//		final String stage = sampleParameters.getStage();
//
//		if (SampleParameters.SAMPLE_ENV[1].equals(stage)) { // Room
//			log("Moving Sample Table");
//			final XYZStageParameters roomTemp = sampleParameters.getRoomTemperatureParameters();
//			setScannable("sample_x", roomTemp.getX());
//			setScannable("sample_y", roomTemp.getY());
//			setScannable("sample_z", roomTemp.getZ());
//
//			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("x", roomTemp.getX(),
//					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
//					"sampletable"));
//			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("y", roomTemp.getY(),
//					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
//					"sampletable"));
//			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("z", roomTemp.getZ(),
//					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
//					"sampletable"));
//		} else if (SampleParameters.SAMPLE_ENV[2].equals(stage)) {
//			log("Preparing cryostat for experiment");
//
//			final CryostatParameters cryo = sampleParameters.getCryostatParameters();
//			final Temperature temp = (Temperature) Finder.getInstance().find("Clake");
//			if (temp != null) {
//				temp.setTargetTemperature(Double.parseDouble(cryo.getTemperature()));
//				scannables.add(temp);
//			} else {
//				log("Clake could not be found in Jython namespace- cryostat not configured");
//			}
//		}
	}

}
