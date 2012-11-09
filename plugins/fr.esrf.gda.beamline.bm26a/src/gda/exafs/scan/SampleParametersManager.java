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

import gda.data.scan.datawriter.NexusExtraMetadataDataWriter;
import gda.data.scan.datawriter.NexusFileMetadata;
import gda.device.DeviceException;
import gda.device.Temperature;
import gda.factory.Finder;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.bm26a.CryostatParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleStageParameters;

/**
 * Sets up Sample Parameters for BM26a. Talks to hardware with hard coded parameter names.
 * <p>
 * Every piece fo hardware should also add its parameters to the nexus file header.
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

		NexusExtraMetadataDataWriter.removeAllMetadataEntries();

		final String sampleEnv = sampleParameters.getSampleEnvironment();

		if (SampleParameters.SAMPLE_ENV[1].equals(sampleEnv)) { // Room
			log("Moving Sample Table");
			final SampleStageParameters roomTemp = sampleParameters.getRoomTemperatureParameters();
			setScannable("sample_x", roomTemp.getX());
			setScannable("sample_y", roomTemp.getY());
			setScannable("sample_z", roomTemp.getZ());
			setScannable("sample_rot", roomTemp.getRotation());
			setScannable("sample_pitch", roomTemp.getRoll());
			setScannable("sample_roll", roomTemp.getYaw());

			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("x", roomTemp.getX(),
					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
					"sampletable"));
			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("y", roomTemp.getY(),
					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
					"sampletable"));
			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("z", roomTemp.getZ(),
					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
					"sampletable"));
			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("rot", roomTemp.getRotation(),
					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
					"sampletable"));
			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("yaw", roomTemp.getYaw(),
					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
					"sampletable"));
			NexusExtraMetadataDataWriter.addMetadataEntry(new NexusFileMetadata("roll", roomTemp.getRoll(),
					NexusFileMetadata.EntryTypes.NXinstrument, NexusFileMetadata.NXinstrumentSubTypes.NXpositioner,
					"sampletable"));
		} else if (SampleParameters.SAMPLE_ENV[2].equals(sampleEnv)) {
			log("Preparing cryostat for experiment");

			final CryostatParameters cryo = sampleParameters.getCryostatParameters();
			final Temperature temp = (Temperature) Finder.getInstance().find("Clake");
			if (temp != null) {
				temp.setTargetTemperature(Double.parseDouble(cryo.getTemperature()));
				scannables.add(temp);
			} else {
				log("Clake could not be found in Jython namespace- cryostat not configured");
			}
		}
	}

	/**
	 * @return - The temperature of the sample in K
	 * @throws DeviceException
	 */
	public double getTemperature() throws DeviceException {

		final String sampleEnv = sampleParameters.getSampleEnvironment();
		if (SampleParameters.SAMPLE_ENV[2].equals(sampleEnv)) { // Cryo
			final Temperature temp = Finder.getInstance().find("Clake");
			return temp.getCurrentTemperature();

		} else if (SampleParameters.SAMPLE_ENV[3].equals(sampleEnv)) { // Furnace
			// TODO
			return 300d;

		}

		return -1d;
	}
}
