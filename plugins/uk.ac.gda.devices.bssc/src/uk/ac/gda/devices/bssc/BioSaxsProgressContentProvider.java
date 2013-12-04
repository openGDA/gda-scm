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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BioSaxsProgressContentProvider implements ITreeContentProvider {

	private BioSaxsProgressModel model;

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		model = (BioSaxsProgressModel)newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return model.getSessions().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		BioSaxsSession session = (BioSaxsSession) parentElement;
		return session.getMeasurements().toArray();
	}

	@Override
	public Object getParent(Object element) {
		BioSaxsMeasurement measurement = (BioSaxsMeasurement) element;
		return measurement.getSession();
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof BioSaxsSession) {
			return true;
		}
		return false;
	}
}
