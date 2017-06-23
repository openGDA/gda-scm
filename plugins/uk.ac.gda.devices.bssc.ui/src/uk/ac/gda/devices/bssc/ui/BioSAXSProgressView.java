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

import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.Activator;
import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;

public class BioSAXSProgressView extends ViewPart implements IPartListener2 {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSProgressView.class);
	public static final String ID = "uk.ac.gda.devices.bssc.biosaxsprogressview";
	private BioSAXSProgressComposite bioSAXSComposite;
	private IListChangeListener listChangedListener;
	private IObservableList model;
	private BioSAXSProgressController controller;
	private IObserver controllerObserver;
	private Action scrollLockAction;

	@Override
	public void createPartControl(Composite parent) {
		controller = (BioSAXSProgressController) GDAClientActivator.getNamedService(BioSAXSProgressController.class,
				null);

		model = (IObservableList) controller.getModel();

		bioSAXSComposite = new BioSAXSProgressComposite(parent, model, SWT.NONE);
		listChangedListener = new IListChangeListener() {

			@Override
			public void handleListChange(ListChangeEvent event) {
			}
		};
		controllerObserver = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
			}
		};
		// model.addListChangeListener(listChangedListener);
		controller.addIObserver(controllerObserver);

		createActions();
		createToolbar();

		IWorkbenchPage page = getSite().getPage();
		page.addPartListener(this);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		if (model != null && controllerObserver != null) {
			model.removeListChangeListener(listChangedListener);
			controller.deleteIObserver(controllerObserver);
			controllerObserver = null;
			model = null;
		}

		if (controller != null) {
			controller.disconnectFromISpyB();
		}

		super.dispose();
	}

	public void createActions() {
		scrollLockAction = new Action("Lock Scrollbar", SWT.CHECK) {
			@Override
			public void run() {
				if (scrollLockAction.isChecked()) {
					scrollLockAction.setChecked(true);
				} else {
					scrollLockAction.setChecked(false);
				}
			}
		};

		scrollLockAction.setImageDescriptor(getImageDescriptor("scrollLock.jpeg"));
		scrollLockAction.setChecked(false);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	private ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/";

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		ImageDescriptor scrollLockImageDesriptor = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(
				iconPath + relativePath), null));

		return scrollLockImageDesriptor;
	}

	/**
	 * Create toolbar.
	 */
	private void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(scrollLockAction);
	}

	public void reveal() {
		TableViewer bioSAXSTableViewer = (TableViewer) bioSAXSComposite.getViewer();

		if (!bioSAXSTableViewer.getControl().isDisposed()) {
			if (bioSAXSTableViewer.getControl().isVisible()) {
				if (!scrollLockAction.isChecked()) {
					int lastItemIndex = model.size() - 1;
					if (!model.isEmpty()) {
						bioSAXSTableViewer.getTable().setTopIndex(lastItemIndex);
					}
				}
			}
		}
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (partRef.getId().equals(ID)) {
			reveal();
		}
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}
}
