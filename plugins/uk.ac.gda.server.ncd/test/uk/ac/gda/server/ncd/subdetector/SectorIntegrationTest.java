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
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public class SectorIntegrationTest extends ReductionDetectorBaseTest {

	@Override
	SectorIntegration getInstance() {
		SectorIntegration sec = new SectorIntegration("me", "upstream");

		return sec;
	}


	@Override
	@Test
	public void testWriteout() throws DeviceException {
		SectorIntegration sec = getInstance();

		SectorROI roi = new SectorROI(0, 0, 0, 10, 0, 0.5*Math.PI, 1.0, true, 0);
		roi.setAverageArea(true);
		sec.setROI(roi);
		
		int frames = 11;
		NXDetectorData nd = generateOnesFloat(frames, 10, 10);
		
		sec.writeout(frames, nd);
		
		NexusGroupData groupData = nd.getData("me", "data", NexusExtractor.SDSClassName);
		
		assertNotNull(groupData);
//		assertArrayEquals(new int[] {frames}, groupData.dimensions);
//		float[] buffer = (float[]) groupData.getBuffer();
//		assertEquals(14196, buffer[0], 0.01);
//		assertEquals(299806, buffer[10], 0.01);
	}
}