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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;

public class BioSAXSSampleProgressCollection extends ArrayList<ISampleProgress>  implements ISampleProgressCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	WritableList items;

	public BioSAXSSampleProgressCollection(Object value) {
		items = new WritableList((List<?>) value,
				ISampleProgress.class);
	}

	@Override
	public WritableList getItems() {
		return items;
	}

}
