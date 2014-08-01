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

package fr.esrf.gda.beamline.bm26a;

import gda.configuration.properties.LocalProperties;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.SampleParametersEditor;
import uk.ac.gda.exafs.ui.SampleParametersUIEditor;
import uk.ac.gda.exafs.ui.describers.SampleDescriber;
/**
 * Setting up the data prior to other views connecting to it.
 */
public class StartupService implements IStartup {

	
	@Override
	public void earlyStartup() {
		
		if (!LocalProperties.get("gda.factory.factoryName").equals("bm26a")) return;
				
		
		// If the SampleParametersEditor is there, we refresh its element list. This is because
		// it needs the definition of beans which are not made until this startup method is run, 
		// but editors are created *before* this method is run.
//		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//			@Override
//			public void run() {
//				final IEditorReference[] eds = EclipseUtils.getDefaultPage().findEditors(null, SampleDescriber.ID, IWorkbenchPage.MATCH_ID);
//				if (eds!=null) {
//					for (int i = 0; i < eds.length; i++) {
//						SampleParametersUIEditor ed = (SampleParametersUIEditor)((SampleParametersEditor)eds[i].getEditor(false)).getRichBeanEditor();
////						ed.updateElementLabel();
//					}
//				}
//			}
//		});
		
	}
	

}
