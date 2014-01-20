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

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class DummyEditorInput implements IStorageEditorInput {
	private String name;

	public DummyEditorInput(String name) {
		this.name = name;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getName() {
		return name;
	}

	public String getToolTipText() {
		return name;
	}

	public IStorage getStorage() {

		return new MyStorage();
	}

	private final class MyStorage implements IStorage {
		public InputStream getContents() throws CoreException {
			return new StringBufferInputStream("");
		}

		public IPath getFullPath() {
			return null;
		}

		public String getName() {
			return DummyEditorInput.this.getName();
		}

		public boolean isReadOnly() {
			return false;
		}

		public Object getAdapter(Class adapter) {
			return null;
		}
	}
}
