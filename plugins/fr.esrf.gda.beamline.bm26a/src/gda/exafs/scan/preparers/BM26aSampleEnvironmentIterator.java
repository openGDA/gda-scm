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

import gda.device.DeviceException;
import gda.device.scannable.scannablegroup.ScannableGroup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.beans.exafs.bm26a.XYZStageParameters;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class BM26aSampleEnvironmentIterator implements SampleEnvironmentIterator {

	private static final Logger logger = LoggerFactory.getLogger(BM26aSampleEnvironmentIterator.class);
	
	private SampleParameters parameters;
	private ScannableGroup xyzStage;
	private ScannableGroup cryoStage;

	public BM26aSampleEnvironmentIterator(SampleParameters parameters, ScannableGroup xyzStage, ScannableGroup cryoStage) {
		this.parameters = parameters;
		this.xyzStage = xyzStage;
		this.cryoStage = cryoStage;
	}

	@Override
	public int getNumberOfRepeats() {
		return 1;
	}

	@Override
	public void next() throws DeviceException, InterruptedException {
		
		logger.debug("Preparing sample parameters");
		if (parameters.getStage().equals("xyzStage")) {
			XYZStageParameters bean = parameters.getXyzStageParameters();
			Double[] targetPosition = { bean.getX(), bean.getY(), bean.getZ() };
			logger.info("moving xyzStage (" + xyzStage.getName() + ") to " + targetPosition);
			xyzStage.asynchronousMoveTo(targetPosition);
			logger.info("xyzStage move complete.");
		} else if (parameters.getStage().equals("cryoStage")) {
			XYZStageParameters bean = parameters.getCryoStageParameters();
			Double[] targetPosition = { bean.getX(), bean.getY(), bean.getZ() };
			logger.info("moving cryoStage (" + cryoStage.getName() + ") to " + targetPosition);
			cryoStage.asynchronousMoveTo(targetPosition);
			logger.info("cryoStage move complete.");
		}
	}

	@Override
	public void resetIterator() {
		// not applicable
	}

	@Override
	public String getNextSampleName() {
		return parameters.getName();
	}

	@Override
	public List<String> getNextSampleDescriptions() {
		return parameters.getDescriptions();
	}
}
