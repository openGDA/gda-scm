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
import gda.observable.Observable;
import gda.observable.Observer;

import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSDBFactory;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;

public class BioSAXSProgressController implements Observer<Object> {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressController.class);
	private BioSAXSISPyB bioSAXSISPyB;
	private BioSAXSProgressModel bioSAXSProgressModel;
	private String visit;
	private long blSessionId;

	public BioSAXSProgressController(BioSAXSProgressModel bioSAXSProgressModel) {
		this.bioSAXSProgressModel = bioSAXSProgressModel;
		new BioSAXSDBFactory().setJdbcURL("jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb");
		bioSAXSISPyB = BioSAXSDBFactory.makeAPI();
		try {
			bioSAXSISPyB.addObserver(this);
		} catch (Exception e) {
			logger.error("Exception adding observer to BioSAXSIspyB", e);
		}
		try {
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
			blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
		} catch (DeviceException e) {
			logger.error("TODO put description of error here", e);
		} catch (SQLException e) {
			logger.error("TODO put description of error here", e);
		}

		pollISpyB();
	}

	public void pollISpyB() {
		final Job pollingJob = new Job("Polling ISpyB") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// bioSAXSProgressModel.clearItems();
					loadModelFromISPyB();
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					return Status.OK_STATUS;
				} finally {
					// start job again after specified time has elapsed
					schedule(10000);
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
		// start as soon as possible
		pollingJob.schedule();
	}

	private void loadModelFromISPyB() {
		try {
			bioSAXSProgressModel.addItems(bioSAXSISPyB.getBioSAXSMeasurements(blSessionId));
		} catch (SQLException e) {
			logger.error("SQL EXception getting data collections from ISpyB", e);
		}
	}

	public void updateModelFromISpyB(long dataCollectionId) {
		try {
			ISAXSDataCollection collection = bioSAXSISPyB.getDataCollection(blSessionId, dataCollectionId);
//			bioSAXSProgressModel.set(((Long) dataCollectionId).intValue(), collection);
		} catch (SQLException e) {
			logger.error("SQL EXception getting data collection from ISpyB", e);
		}
	}

	@Override
	public void update(Observable<Object> source, Object arg) {
		bioSAXSProgressModel.updateItem(source, arg);
	}
}
