/*-
 * Copyright © 2011 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury & Rutherford Laboratory
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

package gda.device.detector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;

@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class LpdDetector extends DetectorBase implements ILpdDetector, NexusDetector, Runnable, PositionCallableProvider<NexusTreeProvider> {

	private static final Logger logger = LoggerFactory.getLogger(LpdDetector.class);
	private String detectorType;
	private String detectorID = "01";
	private String description;
	private JythonServerFacade jsf;
	private int imageWidth;
	private int imageHeight;
	private boolean connected = false;
	private boolean started = false;
	private Thread runner;
	private List<String> lpdModuleNames = new ArrayList<String>();
	private Map<String, ILpdDetector> lpdModules = new LinkedHashMap<String, ILpdDetector>();
	private int xmodules = 1;
	private int ymodules = 1;
	private int[] moduleDims = null;

	@Override
	public void configure(){
		jsf = JythonServerFacade.getInstance();
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		runner.start();
		logger.info("Configuring LpdDetector");
	}

	private void connect() throws DeviceException {
		if (!connected) {
			// This cannot be executed in the configure method as the command server has not started
			JythonServer js = (JythonServer) jsf.getFromJythonNamespace("command_server");
			lpdModules.clear();
			for (String moduleName : lpdModuleNames) {
				logger.debug("module name is " + moduleName);
				PyObject object = js.getInterp().get(moduleName);
				if (object != null) {
					lpdModules.put(moduleName, (ILpdDetector) object.__tojava__(ILpdDetector.class));
				}
			}
			connected = true;
			moduleDims = lpdModules.get(lpdModuleNames.get(0)).getDataDimensions();
			imageWidth = xmodules * moduleDims[0];
			imageHeight = ymodules * moduleDims[1];
		}
	}

	public List<String> getLpdModuleNames() {
		return lpdModuleNames;
	}

	public void setLpdModuleNames(List<String> moduleNames) {
		lpdModuleNames = moduleNames;
	}

	public void addLpdModuleName(String name) {
		lpdModuleNames.add(name);
	}

	public int getXmodules() {
		return xmodules;
	}

	public void setXmodules(int xmodules) {
		this.xmodules = xmodules;
	}

	public int getYmodules() {
		return ymodules;
	}

	public void setYmodules(int ymodules) {
		this.ymodules = ymodules;
	}

	@Override
	public synchronized void collectData() throws DeviceException {
		connect();
		for (Detector lpdModule : lpdModules.values()) {
			lpdModule.collectData();
		}
		started = true;
		notifyAll();
	}

	@Override
	public void stop() throws DeviceException {
		for (Detector lpdModule : lpdModules.values()) {
			lpdModule.stop();
		}
		started = false;
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		collectionTime = time;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return collectionTime;
	}

	@Override
	public int getStatus() throws DeviceException {
		int status = 0;
		connect();
		// check the status of all modules
		// probably need to rethink this!!!!!!
		for (Detector lpdModule : lpdModules.values()) {
			status &= lpdModule.getStatus();
		}
		return status;
	}

	private NXDetectorData readoutPhysicalDetectors() throws DeviceException {
		NXDetectorData nxdata = new NXDetectorData();
		connect();
		int[] detectorData = readAndMergeModuleData();
		int[] datadims = getDataDimensions();
		NexusGroupData ngd = new NexusGroupData(datadims, detectorData);
		ngd.isDetectorEntryData = true;
		nxdata.addData(getName(), ngd, "counts", 1);
//		addMetadata(dataTree);
		return nxdata;
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		Callable<NexusTreeProvider> callable = new Callable<NexusTreeProvider>(){
			@Override
			public NexusTreeProvider call() throws Exception {
				return readoutPhysicalDetectors();
			}
		};
		return callable;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		Callable<NexusTreeProvider> positionCallable = getPositionCallable();

		try {
			NexusTreeProvider treeProvider = positionCallable.call();
			return treeProvider;
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("LpdDetector readout: something wrong in the callable", e);
		}

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return description;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] {imageWidth, imageHeight};
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return detectorType;
	}

	@Override
	public void setAttribute(String attributeName, Object attributeValue) throws DeviceException {
		if ("DACvoltage".equalsIgnoreCase(attributeName)) {
			setDACVoltage((Double)attributeValue);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object obj = null;
		if ("DACvoltage".equalsIgnoreCase(attributeName)) {
			obj = getDACVoltage();
		}
		return obj;
	}

	@Override
	public synchronized void run() {
		while (true) {
			try {
				while (!started)
					wait();
				while (started) {
					connect();
					Dataset dataset = DatasetFactory.createFromObject(readAndMergeModuleData(), imageWidth, imageHeight);
					notifyIObservers(this, dataset);
					wait(100);
				}
			} catch (InterruptedException iox) {
				logger.debug("LpdDetector run thread interrupted");
			} catch (DeviceException e) {
				logger.error("LpdDetector readout error", e);
			}
		}
	}

	private int[] readAndMergeModuleData() throws DeviceException {
		int[] detectorData = new int[imageWidth * imageHeight];
		int module = 0;
		for (Detector lpdModule : lpdModules.values()) {
			List<?> genData = (List<?>) lpdModule.readout();
			Integer[] data = genData.toArray(new Integer[] {});
			int index = (module / xmodules) * xmodules * moduleDims[0] * moduleDims[1];
			index += (module % xmodules) * moduleDims[0];
			int k = 0;
			for (int i = 0; i < moduleDims[1]; i++) {
				for (int j = 0; j < moduleDims[0]; j++) {
					detectorData[index++] = data[k++];
				}
				index += (xmodules - 1) * moduleDims[0];
			}
			module++;
		}
	return detectorData;
	}

	@Override
	public void setDACVoltage(double voltage) throws DeviceException {
		connect();
		// This needs to change we will have to address the correct module
		lpdModules.get(lpdModuleNames.get(0)).setDACVoltage(voltage);
	}

	@Override
	public double getDACVoltage() throws DeviceException {
		connect();
		// This needs to change we will have to address the correct module
		return lpdModules.get(lpdModuleNames.get(0)).getDACVoltage();
	}

	@Override
	public void setSomethingElse(int somethingElse) throws DeviceException {
	}

	@Override
	public int getSomethingElse() throws DeviceException {
		return 0;
	}
}
