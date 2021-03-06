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

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class BM26aBeamlineActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "fr.esrf.gda.beamline.bm26a";

	// The shared instance
	private static BM26aBeamlineActivator plugin;

	/**
	 * The constructor
	 */
	public BM26aBeamlineActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BM26aBeamlineActivator getDefault() {
		return plugin;
	}

	public static <T> T getService(Class<T> serviceClass) {
		ServiceReference<T> ref = plugin.getBundle().getBundleContext().getServiceReference(serviceClass);
		return plugin.getBundle().getBundleContext().getService(ref);
	}

}
