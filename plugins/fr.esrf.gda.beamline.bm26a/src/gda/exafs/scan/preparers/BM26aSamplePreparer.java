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

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class BM26aSamplePreparer implements SampleEnvironmentPreparer, InitializingBean {

	private ScannableGroup xyzStage;
	private ScannableGroup cryoStage;
	private SampleParameters parameters;

	public BM26aSamplePreparer() {
	}

	public BM26aSamplePreparer(ScannableGroup xyzStage, ScannableGroup cryoStage) {
		this.xyzStage = xyzStage;
		this.cryoStage = cryoStage;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (xyzStage == null || cryoStage == null) {
			throw new IllegalArgumentException("Missing scannable group xyzStage or cryoStage");
		}
	}
	
	public ScannableGroup getXyzStage() {
		return xyzStage;
	}

	public void setXyzStage(ScannableGroup xyzStage) {
		this.xyzStage = xyzStage;
	}

	public ScannableGroup getCryoStage() {
		return cryoStage;
	}

	public void setCryoStage(ScannableGroup cryoStage) {
		this.cryoStage = cryoStage;
	}

	@Override
	public SampleEnvironmentIterator createIterator(String experimentType) {
		return new BM26aSampleEnvironmentIterator(parameters, xyzStage, cryoStage);
	}

	@Override
	public void configure(IScanParameters scanParameters, ISampleParameters sampleParameters) throws Exception {
		this.parameters = (SampleParameters) sampleParameters;
	}
}
