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

public class BioSAXSProgressModel extends ArrayList<ISampleProgress> implements IProgressModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	WritableList items = new WritableList(new ArrayList<ISampleProgress>(), ISampleProgress.class);

	public BioSAXSProgressModel() {
		new BioSAXSProgressController(this);
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
