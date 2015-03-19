/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import org.junit.Test;
import gda.data.nexus.NexusGlobals;

import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;

public class ReductionDetectorBaseTest {

	ReductionDetectorBase getInstance() {
		return new ReductionDetectorBase("me", "upstream");
	}
	
	NXDetectorData generateIncrementingUpstreamFloat(int frames, int x, int y) {
		NXDetectorData nxdata = new NXDetectorData();

		int[] devicedims = new int[] { frames, x, y };
		int len = 1;
		for(int n: devicedims) len *= n;
		
		float[] data = new float[len];
		for (int i = 0; i < data.length; i++) {
			data[i] = i;
		}
		NexusGroupData ngd = new NexusGroupData(devicedims, NexusGlobals.NX_FLOAT32, data);
		ngd.isDetectorEntryData = true;
		nxdata.addData("upstream", ngd, "counts", 1);
		
		return nxdata;
	}
	
	NXDetectorData generateOnesFloat(int frames, int x, int y) {
		NXDetectorData nxdata = new NXDetectorData();

		int[] devicedims = new int[] { frames, x, y };
		int len = 1;
		for(int n: devicedims) len *= n;
		
		float[] data = new float[len];
		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}
		NexusGroupData ngd = new NexusGroupData(devicedims, NexusGlobals.NX_FLOAT32, data);
		ngd.isDetectorEntryData = true;
		nxdata.addData("upstream", ngd, "counts", 1);
		
		return nxdata;
	}
	
	@Test
	public void testWriteout() throws DeviceException {
		ReductionDetectorBase rdb = new ReductionDetectorBase("me", "upstream");
		
		int frames = 11;
		NXDetectorData nd = generateIncrementingUpstreamFloat(frames, 2, 2);
		
		rdb.writeout(frames, nd);
		
		NexusGroupData groupData = nd.getData("me", "data", NexusExtractor.SDSClassName);
		assertNull(groupData); // base does not provide data

		groupData = nd.getData("me", "sas_type", NexusExtractor.SDSClassName);
		assertNotNull(groupData); 
		
		CharBuffer decode = Charset.forName("UTF-8").decode(ByteBuffer.wrap((byte[]) groupData.getBuffer()));
		assertEquals(NcdDetectorSystem.REDUCTION_DETECTOR, decode.toString());
	}

	@Test
	public void testGetDetectorType()
		throws Exception {
		ReductionDetectorBase reductionDetectorBase = getInstance();
		String result = reductionDetectorBase.getDetectorType();
		assertEquals(NcdDetectorSystem.REDUCTION_DETECTOR, result);
	}
}