/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ispyb;

import gda.data.PathConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import uk.ac.gda.devices.bssc.ispyb.BioSAXSISPyB.SampleInfo;

public class BioSAXSISPyBUtils {

	public static void dumpCollectionReport(long collectionid) throws SQLException, IOException {
		List<SampleInfo> collectionInfo = BioSAXSDBFactory.makeAPI().getSaxsDataCollectionInfo(collectionid);
		if (collectionInfo.isEmpty()) 
			return;
		String filename = PathConstructor.createFromDefaultProperty() + String.format("bssc-collection-%d.dat", collectionid);
		File file = new File(filename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		try {
			bw.write("#location, sample name, sample file, before buffer file, after buffer file\n");
			for(SampleInfo si: collectionInfo) {
				bw.write(si.location.toString()+","+si.name+","+si.sampleFileName+","+si.bufferBeforeFileName+","+si.bufferAfterFileName+"\n");
			}
		} finally {
			bw.close();
		}
	}
}
