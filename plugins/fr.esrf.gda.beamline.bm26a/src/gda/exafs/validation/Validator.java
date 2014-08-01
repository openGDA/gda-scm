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

import gda.exafs.scan.ExafsValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
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
			errors.addAll(validateISampleParameters(bean.getSampleParameters()));
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

	protected List<InvalidBeanMessage> validateISampleParameters(ISampleParameters sampleParameters) {

		if (sampleParameters instanceof SampleParameters) {
			return validateSampleParameters((SampleParameters)sampleParameters);
		}
		InvalidBeanMessage invalidBeanMessage;
		if (sampleParameters == null) {
			try {
				if (bean != null && bean.isMicroFocus()) {
					// do not have a sample file for microfocus scans
					return Collections.emptyList();
				}
				// else its missing
				invalidBeanMessage = new InvalidBeanMessage("Missing or Invalid Sample Parameters");
			} catch (Exception e) {
				invalidBeanMessage = new InvalidBeanMessage(
						"Error testing if bean is a microfocus scan when testing Scan parameters from bean");
			}
		} else {
			invalidBeanMessage = new InvalidBeanMessage("Unknown Sample Type " + sampleParameters.getClass().getName());
		}
		if (bean != null) {
			invalidBeanMessage.setFileName(bean.getSampleFileName());
		}
		ArrayList<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>();
		errors.add(invalidBeanMessage);
		return errors;
	}

	public List<InvalidBeanMessage> validateSampleParameters(uk.ac.gda.beans.exafs.bm26a.SampleParameters s) {

		if (s == null) {
			return Collections.emptyList();
		}
		
		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (!s.isShouldValidate()) {
			return errors;
		}

		if (s.getName() == null) {
			errors.add(new InvalidBeanMessage("Please set a sample name."));
		}
		if (s.getDescription1() == null) {
			errors.add(new InvalidBeanMessage("Please set a sample description."));
		}

		if (s.getName().compareTo("default") == 0) {
			errors.add(new InvalidBeanMessage("Sample Name has not been set in " + bean.getSampleFileName()));
		} else if (!stringCouldBeConvertedToValidUnixFilename(s.getName())) {
			errors.add(new InvalidBeanMessage("The given Sample Name in " + bean.getSampleFileName()
					+ " cannot be converted into a valid file prefix.\nPlease remove invalid characters."));
		}

		if (bean != null) {
			setFileName(errors, bean.getSampleFileName());
		}
		
		return errors;
	}
}
