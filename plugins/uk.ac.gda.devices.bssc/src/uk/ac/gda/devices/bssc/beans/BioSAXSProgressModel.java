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

import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;

import uk.ac.gda.devices.bssc.ispyb.ISAXSDataCollection;

public class BioSAXSProgressModel implements IProgressModel {

	WritableList items;
	BioSAXSProgressController controller;

	public BioSAXSProgressModel(BioSAXSProgressController controller) {
		super();
		this.controller = controller;
		controller.setModel(this);
	}

	@Override
	public IObservableList getItems() {
		if( items == null){
			items = new WritableList(new ArrayList<ISAXSProgress>(), ISAXSDataCollection.class);
		}
		return items;
	}

	@Override
	public void clearItems() {
		getItems().clear();
	}

	@Override
	public void addItems(final List<ISAXSProgress> progressList) {
		getItems().getRealm().asyncExec(new Runnable() {
			@Override
			public void run() {
				getItems().addAll(progressList);
			}
		});
	}

	@Override
	public void addItem(ISAXSProgress progressItem) {
		getItems().add(progressItem);
	}
	
	@Override
	public void addIObserver(IObserver anIObserver) {
		controller.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		controller.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		controller.deleteIObservers();
	}
}