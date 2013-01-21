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

package uk.ac.gda.devices.bssc.ui;

import java.net.URL;

import org.eclipse.ui.IEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public final class BSSCSessionBeanEditor extends RichBeanMultiPageEditorPart {
	private static final Logger logger = LoggerFactory.getLogger(BSSCSessionBeanEditor.class);

	public BSSCSessionBeanEditor() {
		super();
	}
	
	@Override
	public Class<?> getBeanClass() {
		return BSSCSessionBean.class;
	}

	@Override
	public URL getMappingUrl() {
		return BSSCSessionBean.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new BSSCSessionBeanUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return BSSCSessionBean.schemaURL; // Please make sure this field is present and the schema
	}
	
	@Override
	public void setInput(final IEditorInput input) {
		try{
	        assignInput(input);
	        createBean();
	        linkUI();
		} catch (Throwable th){
			logger.error("Error setting input for editor from input " + input.getName(), th);
		}
	}
}