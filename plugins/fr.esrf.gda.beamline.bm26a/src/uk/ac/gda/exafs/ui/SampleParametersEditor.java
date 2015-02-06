/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;


import java.net.URL;

import uk.ac.gda.beans.exafs.bm26a.SampleParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanMultiPageEditor;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class SampleParametersEditor extends ExperimentBeanMultiPageEditor {

	@Override
	public Class<?> getBeanClass() {
		return SampleParameters.class;
	}

	@Override
	public URL getMappingUrl() {
		return SampleParameters.mappingURL;
	}

	@Override
	protected RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new SampleParametersUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return SampleParameters.schemaURL;
	}
}

	