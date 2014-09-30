/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.exafs.scan.preparers;

import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.device.DeviceException;
import gda.scan.ScanPlotSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.server.exafs.scan.OutputPreparer;
import uk.ac.gda.server.exafs.scan.preparers.OutputPreparerBase;

public class BM26aOutputPreparer extends OutputPreparerBase implements OutputPreparer {

	public BM26aOutputPreparer(AsciiDataWriterConfiguration datawriterconfig, NXMetaDataProvider metashop) {
		super(datawriterconfig, metashop);
	private static final Logger logger = LoggerFactory.getLogger(BM26aOutputPreparer.class);
	private AsciiDataWriterConfiguration dataWriterConfig;
	private Metadata meta;
	
	public BM26aOutputPreparer(AsciiDataWriterConfiguration dataWriterConfig) {
		this.dataWriterConfig = dataWriterConfig;
		meta = new Metadata(dataWriterConfig);
	}

	@Override
	public void configure(IOutputParameters outputParameters, IScanParameters scanBean, IDetectorParameters detectorBean) throws DeviceException{
		logger.debug("Preparing output parameters");
		List<MetadataParameters> metadata = outputParameters.getMetadataList();
		if (metadata.size() > 0) {
			meta.add_to_metadata(metadata);
		}
	}

	// Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
	// If this returns None, then let the Ascii Data Writer class find the config for itself.
	@Override
	public AsciiDataWriterConfiguration getAsciiDataWriterConfig(IScanParameters scanBean) {
		return dataWriterConfig;
	}

	// For any specific plotting requirements based on all the options in this experiment
	@Override
	public ScanPlotSettings getPlotSettings() {
		return null;
	}

	public void _resetHeader() {
// where does original_header come from ??????????????????????????????????????????		
//		dataWriterConfig.setHeader(originalHeader);
		meta.clearAlldynamical();
	}

	@Override
	public void resetNexusStaticMetadataList() {
		return;
	}

	@Override
	public void beforeEachRepetition() throws Exception {
		// 
	}
}
