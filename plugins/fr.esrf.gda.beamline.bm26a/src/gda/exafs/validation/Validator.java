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

package gda.exafs.validation;

import gda.device.Scannable;
import gda.exafs.scan.ExafsValidator;

import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.bm26a.CustomParameter;
import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleStageParameters;
import uk.ac.gda.beans.validation.InvalidBeanException;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.exafs.ui.data.ScanObject;

/**
 * A class to check that the XML parameters are sensible. This is an additional check which is beyond that which the
 * schema can test the xml file.
 */
public class Validator extends ExafsValidator {

	private static final double MINENERGY = 2000;
	private static final double MAXENERGY = 35000;

	@Override
	public void validate(final IExperimentObject b) throws InvalidBeanException {

		this.bean = (ScanObject) b;

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);

		try {
			errors.addAll(validateIScanParameters(bean.getScanParameters()));
			errors.addAll(validateSampleParameters((SampleParameters) bean.getSampleParameters()));
			errors.addAll(validateIDetectorParameters(bean.getDetectorParameters()));
			errors.addAll(validateIOutputParameters(bean.getOutputParameters()));
		} catch (Exception e) {
			throw new InvalidBeanException("Exception retrieving parameters objects: " + e.getMessage());
		}

		if (!errors.isEmpty()) {
			for (InvalidBeanMessage invalidBeanMessage : errors) {
				invalidBeanMessage.setFolderName(bean.getFolder().getName());
			}
			throw new InvalidBeanException(errors);
		}
	}

	private List<InvalidBeanMessage> validateIScanParameters(IScanParameters scanParams) {
		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (scanParams instanceof XasScanParameters) {
			errors.addAll(validateXasScanParameters((XasScanParameters) scanParams, MINENERGY, MAXENERGY));
		} else if (scanParams instanceof XanesScanParameters) {
			errors.addAll(validateXanesScanParameters((XanesScanParameters) scanParams));
		} else if (scanParams == null) {
			errors.add(new InvalidBeanMessage("Missing or Invalid Scan Parameters"));
		} else {
			errors.add(new InvalidBeanMessage("Unknown Scan Type " + scanParams.getClass().getName()));
		}
		if (bean != null) {
			setFileName(errors, bean.getScanFileName());
		}
		return errors;
	}

	public List<InvalidBeanMessage> validateSampleParameters(SampleParameters s) {

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (!s.isShouldValidate()) {
			return errors;
		}

		final String environment = s.getSampleEnvironment();
		if (environment.equalsIgnoreCase(SampleParameters.SAMPLE_ENV[1])) {

			final SampleStageParameters p = s.getRoomTemperatureParameters();
			final String message = "The sample stage parameters are out of bounds.";
			checkRangeBounds("x", p.getX(), -15d, 15d, errors, message);
			checkRangeBounds("y", p.getY(), -20.1d, 20.1d, errors, message);
			checkRangeBounds("z", p.getZ(), -15d, 15d, errors, message);

			checkRangeBounds("Rotation", p.getRotation(), 0d, 360d, errors, message);
			checkRangeBounds("Roll", p.getRoll(), -5d, 5d, errors, message);
			checkRangeBounds("Yaw", p.getYaw(), -5d, 5d, errors, message);

		}
		
		else if (environment.equalsIgnoreCase(SampleParameters.SAMPLE_ENV[5])) {
			final List<CustomParameter> c = s.getCustomParameters();
			for (CustomParameter cp : c) {
				checkFindable("Device Name", cp.getDeviceName(), Scannable.class, errors);
			}
		}

		if (bean != null) {
			setFileName(errors, bean.getSampleFileName());
		}
		return errors;
	}
}
