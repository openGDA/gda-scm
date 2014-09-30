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

import gda.device.scannable.scannablegroup.ScannableGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.beans.exafs.bm26a.XYZStageParameters;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class BM26aSamplePreparer implements SampleEnvironmentPreparer {
	private static final Logger logger = LoggerFactory.getLogger(BM26aSamplePreparer.class);
	private ScannableGroup xyzStage;
	private ScannableGroup cryoStage;

	public BM26aSamplePreparer(ScannableGroup xyzStage, ScannableGroup cryoStage) {
		this.xyzStage = xyzStage;
		this.cryoStage = cryoStage;
	}

	@Override
	public void prepare(ISampleParameters parameters) throws Exception {
		SampleParameters sampleParameters = (SampleParameters)parameters;
		logger.debug("Preparing sample parameters");
		if (sampleParameters.getStage().equals("xyzStage")) {
			XYZStageParameters bean = sampleParameters.getXyzStageParameters();
			Double[] targetPosition = { bean.getX(), bean.getY(), bean.getZ() };
			logger.info("moving xyzStage (" + xyzStage.getName() + ") to " + targetPosition);
			xyzStage.moveTo(targetPosition);
			logger.info("xyzStage move complete.");
		} else if (sampleParameters.getStage().equals("cryoStage")) {
			XYZStageParameters bean = sampleParameters.getCryoStageParameters();
			Double[] targetPosition = { bean.getX(), bean.getY(), bean.getZ() };
			logger.info("moving cryoStage (" + cryoStage.getName() + ") to " + targetPosition);
			cryoStage.moveTo(targetPosition);
			logger.info("cryoStage move complete.");
		}
	}

	@Override
	public SampleEnvironmentIterator createIterator(String experimentType) {
		return null;
	}
}
