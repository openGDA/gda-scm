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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSDBFactory;
import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB;

public class BioSAXSProgressModel extends ArrayList<ISAXSDataCollection> implements IProgressModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	WritableList items = new WritableList(new ArrayList<ISAXSDataCollection>(), ISAXSDataCollection.class);

	public BioSAXSProgressModel() {

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

	@Override
	public void addItem(ISAXSDataCollection dataCollection) {
		items.add(dataCollection);
	}
}
