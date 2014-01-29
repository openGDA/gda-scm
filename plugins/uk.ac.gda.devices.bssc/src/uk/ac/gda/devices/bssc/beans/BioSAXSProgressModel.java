/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.beans;

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSDBFactory;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;

public class BioSAXSProgressModel extends ArrayList<ISampleProgress> implements IProgressModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressModel.class);
	WritableList items = new WritableList(new ArrayList<ISampleProgress>(), ISampleProgress.class);

	public BioSAXSProgressModel() {
		//Set up connection to ISpyB
		BioSAXSProgressController controller = new BioSAXSProgressController(this);
		controller.pollISpyB();
	}

	@Override
	public WritableList getItems() {
		return items;
	}

	@Override
	public void clearItems() {
		items.getRealm().asyncExec(new Runnable() {
			@Override
			public void run() {
				items.clear();
			}
		});
	}

	@Override
	public void addItems(final List<ISampleProgress> bioSAXSSamples) {
		items.getRealm().asyncExec(new Runnable() {

			@Override
			public void run() {
				items.addAll(bioSAXSSamples);
			}
		});

	}
}
