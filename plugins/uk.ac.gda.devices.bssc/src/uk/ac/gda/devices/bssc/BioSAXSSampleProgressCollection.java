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

package uk.ac.gda.devices.bssc;

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

public class BioSAXSSampleProgressCollection extends ArrayList<ISampleProgress> implements ISampleProgressCollection {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSSampleProgressCollection.class);
	WritableList items = new WritableList(new ArrayList<ISampleProgress>(), ISampleProgress.class);
	private BioSAXSISPyB bioSAXSISPyB;

	public BioSAXSSampleProgressCollection() {
		//Set up connection to ISpyB
		new BioSAXSDBFactory().setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
		pollISpyB();
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

	@Override
	public void pollISpyB() {
		final Job pollingJob = new Job("Polling ISpyB") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					clearItems();
					loadModelFromISPyB();
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					return Status.OK_STATUS;
				} finally {
					// start job again after specified time has elapsed
					schedule(90000);
				}
			}
		};
		// job.addJobChangeListener(new JobChangeAdapter() {
		// @Override
		// public void done(IJobChangeEvent event) {
		// if (event.getResult().isOK())
		// System.out.println("Job completed successfully");
		// else
		// System.out.println("Job did not complete successfully");
		// }
		// });
		pollingJob.schedule(); // start as soon as possible
	}

	private void loadModelFromISPyB() {
		String visit;

		try {
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
			long blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
			addItems(bioSAXSISPyB.getBioSAXSMeasurements(blSessionId));
		}
		catch (DeviceException e) {
			logger.error("Device Exception retrieving visit from GDAMetaDataProvider" + e);
		}
		catch (SQLException e) {
			logger.error("SQL Exception getting samples from ISPyB", e);
		} 
	}
}
