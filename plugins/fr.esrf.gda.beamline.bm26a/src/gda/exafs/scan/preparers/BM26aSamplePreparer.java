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
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class BM26aSamplePreparer implements SampleEnvironmentPreparer {

	private ScannableGroup xyzStage;
	private ScannableGroup cryoStage;
	private SampleParameters parameters;

	public BM26aSamplePreparer(ScannableGroup xyzStage, ScannableGroup cryoStage) {
		this.xyzStage = xyzStage;
		this.cryoStage = cryoStage;
	}

	@Override
	public void configure(ISampleParameters parameters) throws Exception {
		this.parameters = (SampleParameters) parameters;
	}

	@Override
	public SampleEnvironmentIterator createIterator(String experimentType) {
		return new BM26aSampleEnvironmentIterator(parameters, xyzStage, cryoStage);
	}
}
