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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.gda.devices.bssc.ui.MeasurementsFieldComposite;

public class BioSaxsProgressLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		// if (element instanceof BioSaxsSession) {
		// BioSaxsSession session = (BioSaxsSession) element;
		//
		// switch (columnIndex) {
		// case 0:
		// return session.getName();
		// }
		// } else if (element instanceof BioSaxsMeasurement) {
		BioSaxsMeasurement measurement = (BioSaxsMeasurement) element;

		switch (columnIndex) {
		case 0:
			return measurement.getSessionName();
		case 1:
			return measurement.getName();
		case 2:
			return "(" + String.valueOf(measurement.getWellColumn()) + ", "
					+ String.valueOf(measurement.getWellRow() + ")");
		}
		return null;
	}

}
