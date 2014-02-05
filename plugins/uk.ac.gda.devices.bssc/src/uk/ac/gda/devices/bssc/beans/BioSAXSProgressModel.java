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

import gda.observable.Observable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IStatus;

public class BioSAXSProgressModel extends ArrayList<ISAXSDataCollection> implements IProgressModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	WritableList items = new WritableList(new ArrayList<ISAXSDataCollection>(), ISAXSDataCollection.class);
	private BioSAXSProgressController controller;

	public BioSAXSProgressModel() {
		controller = new BioSAXSProgressController(this);
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
	public void addItems(final List<ISAXSDataCollection> bioSAXSSamples) {
		items.getRealm().asyncExec(new Runnable() {
			@Override
			public void run() {
				items.addAll(bioSAXSSamples);
			}
		});

	}

	public void updateItem(final Observable<Object> source, final Object arg) {
		items.getRealm().asyncExec(new Runnable() {
			@Override
			public void run() {
				// this will be done from the script
//				((ISAXSDataCollection) items.get(((Long) arg).intValue())).setCollectionStatus(ISpyBStatus.FAILED);
				
				// get the updated item from ISPyB
				long dataCollectionId = (long)arg + 2559;
				controller.updateModelFromISpyB(dataCollectionId);
			}
		});
	}
}
