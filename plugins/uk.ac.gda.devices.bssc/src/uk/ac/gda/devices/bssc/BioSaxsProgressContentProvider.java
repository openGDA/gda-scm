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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BioSaxsProgressContentProvider implements IStructuredContentProvider {

	private BioSaxsProgressModel model;


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		model = (BioSaxsProgressModel)newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return model.getMeasurements().toArray();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}


}
