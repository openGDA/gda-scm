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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import oracle.jdbc.OracleConnection;
import uk.ac.gda.devices.bssc.beans.LocationBean;

/*
 * An Oracle extension of the abstract BioSAXSISPyB class
 * 
 * TODO run ispybb-oracle.sql on live database
 * TODO run create_ro_role.sql, create_rw_role.sql and users.sql on live database
 */
public class BioSAXSISPyBviaOracle implements BioSAXSISPyB {

	Connection conn = null;
	String URL = null;
	
	public BioSAXSISPyBviaOracle(String mode) {
		URL = mode;
	}
	
	@Override
	public void disconnect() throws SQLException {
		if (conn != null) {
			if (!conn.isClosed()) {
				conn.close();
			}
		}
	}
	
	protected boolean connect() throws SQLException {
		Driver driver = new oracle.jdbc.OracleDriver();
		DriverManager.registerDriver(driver);
		Properties props = new Properties();
		props.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_VSESSION_OSUSER, "gda2"); 

		conn = DriverManager.getConnection(URL, props);
		return true;
	}

	protected void connectIfNotConnected() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}
	
	@Override
	public long getSessionForVisit(String visitname) throws SQLException {
		long sessionId = -1;
		connectIfNotConnected();

		String selectSql = "SELECT sessionId " +
				"FROM ispyb4a_db.BLSession bs INNER JOIN ispyb4a_db.Proposal p on (bs.proposalId = p.proposalId) " +
				"WHERE p.proposalCode || p.proposalNumber || '-' || bs.visit_number = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setString(1, visitname);
		boolean success = stmt.execute();
		if (success){
			ResultSet rs = stmt.getResultSet();
			if (rs.next()) 
				sessionId = rs.getLong(1);
			rs.close();
		}
		stmt.close();

		return sessionId;
	}

	@Override
	public long createSaxsDataCollection(long blsessionId) throws SQLException {
		long saxsDataCollectionId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SaxsDataCollection (datacollectionId, blsessionId) " +
				"VALUES (ispyb4a_db.s_SaxsDataCollection.nextval, ?) RETURNING datacollectionId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, blsessionId);
		stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
		stmt.execute();
		saxsDataCollectionId = stmt.getLong(2);
		stmt.close();
		
		return saxsDataCollectionId;
	}
	
	protected long createMeasurement(long sampleId, long runId, float exposureTemperature, double flow, String viscosity) 
			throws SQLException {
		long measurementId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Measurement (" +
				"specimenId, sampleId, runId, exposureTemperature, flow, viscosity) " +
				"VALUES (ispyb4a_db.s_Measurement.nextval, ?, ?, ?, ?, ?) RETURNING specimenId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, sampleId);
		stmt.setLong(2, runId);
		stmt.setFloat(3, exposureTemperature);
		stmt.setDouble(4, flow);
		stmt.setString(5, viscosity);

		stmt.registerOutParameter(6, java.sql.Types.VARCHAR);
		stmt.execute();
		measurementId = stmt.getLong(6);
		stmt.close();
		
		return measurementId;
	}
	
	protected long createBuffer(long blsessionId) throws SQLException {
		long bufferId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Buffer (bufferId, blsessionId) " +
				"VALUES (ispyb4a_db.s_Buffer.nextval, ?) RETURNING bufferId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, blsessionId);
		stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
		stmt.execute();
		bufferId = stmt.getLong(2);
		stmt.close();
		
		return bufferId;
	}
	
	protected long createSamplePlate(long blsessionId, String name) throws SQLException {
		long samplePlateId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SamplePlate (samplePlateId, blsessionId, name) " +
				"VALUES (ispyb4a_db.s_SamplePlate.nextval, ?, ?) RETURNING samplePlateId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, blsessionId);
		stmt.setString(2, name);

		stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
		stmt.execute();
		samplePlateId = stmt.getLong(3);
		stmt.close();

		return samplePlateId;
	}
	
	protected long createSamplePlatePosition(long samplePlateId, short row, short column) throws SQLException {
		long samplePlatePositionId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SamplePlatePosition (" +
				"samplePlatePositionId, samplePlateId, rowNumber, columnNumber) " +
				"VALUES (ispyb4a_db.s_SamplePlatePosition.nextval, ?, ?, ?) RETURNING samplePlatePositionId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, samplePlateId);
		stmt.setLong(2, row);
		stmt.setLong(3, column);

		stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
		stmt.execute();
		samplePlatePositionId = stmt.getLong(4);
		stmt.close();
		
		return samplePlatePositionId;
	}
	
	protected long createSample(long blsessionId, Long bufferId, Long macromoleculeId, Long samplePlatePositionId, 
			Long stockSolutionId, Double concentration, Double volume) throws SQLException {
		long sampleId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Sample (" +
				"sampleId, blsessionId, bufferId, macromoleculeId, samplePlatePositionId, stockSolutionId, concentration, volumen) " +
				"VALUES (ispyb4a_db.s_Sample.nextval, ?, ?, ?, ?, ?, ?, ?) RETURNING sampleId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, blsessionId);

		if (bufferId == null)
			stmt.setNull(2, java.sql.Types.BIGINT);
		else
			stmt.setLong(2, bufferId);

		if (macromoleculeId == null)
			stmt.setNull(3, java.sql.Types.BIGINT);
		else 
			stmt.setLong(3, macromoleculeId);

		if (samplePlatePositionId == null)
			stmt.setNull(4, java.sql.Types.BIGINT);
		else
			stmt.setLong(4, samplePlatePositionId);

		if (stockSolutionId == null)
			stmt.setNull(5, java.sql.Types.BIGINT);
		else
			stmt.setLong(5, stockSolutionId);

		if (concentration == null)
			stmt.setNull(6, java.sql.Types.DOUBLE);
		else
			stmt.setDouble(6, concentration);

		if (volume == null)
			stmt.setNull(7, java.sql.Types.DOUBLE);
		else
			stmt.setDouble(7, volume);

		stmt.registerOutParameter(8, java.sql.Types.VARCHAR);
		stmt.execute();
		sampleId = stmt.getLong(8);
		stmt.close();
		return sampleId;
	}

	protected long createRun(float storageTemperature, double energyInkeV, int numFrames, double timePerFrame) 
			throws SQLException {
		long runId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Run (" +
				"runId, storageTemperature, energy, frameCount, timePerFrame) " +
				"VALUES (ispyb4a_db.s_Run.nextval, ?, ?, ?, ?) RETURNING runId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setFloat(1, storageTemperature);
		stmt.setDouble(2, energyInkeV);
		stmt.setInt(3, numFrames);
		stmt.setDouble(4, timePerFrame);

		stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
		stmt.execute();
		runId = stmt.getLong(5);
		stmt.close();
		
		return runId;
	}
	
	protected long createFrameSet(long runId, String fileName, String internalPath) throws SQLException {
		long frameSetId = -1;
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.FrameSet (" +
				"frameSetId, runId, filePath, internalPath) " +
				"VALUES (ispyb4a_db.s_FrameSet.nextval, ?, ?, ?) RETURNING frameSetId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, runId);
		stmt.setString(2, fileName);
		stmt.setString(3, internalPath);

		stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
		stmt.execute();
		frameSetId = stmt.getLong(4);
		stmt.close();
		
		return frameSetId;
	}

	protected long createStockSolution(long blsessionId, String name, double concentration) throws SQLException {
		long stockSolutionId = -1;

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.StockSolution (" +
				"stockSolutionId, blsessionId, name, concentration) " +
				"VALUES (ispyb4a_db.s_FrameSet.nextval, ?, ?, ?) RETURNING stockSolutionId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, blsessionId);
		stmt.setString(2, name);
		stmt.setDouble(3, concentration);

		stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
		stmt.execute();
		stockSolutionId = stmt.getLong(4);
		stmt.close();
		return stockSolutionId;
	}

	
	/*
	 * Add row to the MeasurementToDataCollection table with a dataCollectionOrder which is 1 greater than the max
	 * for existing entries for this dataCollectionId.
	 */
	@Override
	public long createMeasurementToDataCollection(long saxsDataCollectionId, long measurementId) throws SQLException {
		long measurementToDataCollectionId = -1;

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.MeasurementToDataCollection (" +
				"measurementToDataCollectionId, dataCollectionId, measurementId, dataCollectionOrder) " +
				"VALUES (ispyb4a_db.s_MeasurementToDataCollection.nextval, ?, ?, (" +
				"  SELECT nvl(max(dataCollectionOrder) ,0)+1" +
				"    FROM ispyb4a_db.MeasurementToDataCollection" +
				"    WHERE dataCollectionId = ?)" +
				") RETURNING measurementToDataCollectionId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, saxsDataCollectionId);
		stmt.setLong(2, measurementId);
		stmt.setLong(3, saxsDataCollectionId);

		stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
		stmt.execute();
		measurementToDataCollectionId = stmt.getLong(4);
		stmt.close();
		
		return measurementToDataCollectionId;
	}
	
	@Override
	public long createBufferMeasurement(long blsessionId, short plate, short row, short column,
			float storageTemperature, float exposureTemperature, int numFrames, double timePerFrame, double flow,
			double volume, double energyInkeV, String viscosity, String fileName, String internalPath)  throws SQLException {

		connectIfNotConnected();
		
		long bufferId = createBuffer(blsessionId);
		long samplePlateId = createSamplePlate(blsessionId, String.valueOf(plate));
		long experimentId = createExperiment(proposalId, fileName, "TEMPLATE", "BSSC");
		long samplePlatePositionId = createSamplePlatePosition(samplePlateId, row, column);
		long sampleId = createSample(blsessionId, bufferId, null, samplePlatePositionId, null, null, volume);
		long runId = createRun(storageTemperature, energyInkeV, numFrames, timePerFrame);		
		long frameSetId = createFrameSet(runId, fileName, internalPath);
		long measurementId = createMeasurement(sampleId, runId, exposureTemperature, flow, viscosity);
		return measurementId;
	}

	
	@Override
	public long createSampleMeasurement(long blsessionId, short plate, short row, short column, String name,
			double concentration, float storageTemperature, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity, String fileName,
			String internalPath) throws SQLException {

		connectIfNotConnected();

		long stockSolutionId = createStockSolution(blsessionId, name, concentration);
		long experimentId = createExperiment(proposalId, fileName, "TEMPLATE", "BSSC");
		long samplePlateId = createSamplePlate(blsessionId, String.valueOf(plate));
		long samplePlatePositionId = createSamplePlatePosition(samplePlateId, row, column);
		long sampleId = createSample(blsessionId, (Long)null, (Long)null, samplePlatePositionId, stockSolutionId, concentration, volume);
		long runId = createRun(storageTemperature, energyInkeV, numFrames, timePerFrame);		
		long frameSetId = createFrameSet(runId, fileName, internalPath);
		long measurementId = createMeasurement(sampleId, runId, exposureTemperature, flow, viscosity);
		return measurementId;
	}

	@Override
	public List<SampleInfo> getSaxsDataCollectionInfo(long saxsDataCollectionId) throws SQLException {
		List<SampleInfo> sinfos = new ArrayList<SampleInfo>();
		SampleInfo sinfo = new SampleInfo();
		
		connectIfNotConnected();

		String selectSql = "SELECT ispyb4a_db.sampleplate.name AS plate, ispyb4a_db.sampleplateposition.rownumber, ispyb4a_db.sampleplateposition.columnnumber, ispyb4a_db.stocksolution.name, ispyb4a_db.frameset.filepath FROM ispyb4a_db.MeasurementToDataCollection INNER JOIN ispyb4a_db.measurement ON ispyb4a_db.MeasurementToDataCollection.measurementid = ispyb4a_db.measurement.specimenid INNER JOIN ispyb4a_db.sample ON ispyb4a_db.measurement.sampleid = ispyb4a_db.sample.sampleid INNER JOIN ispyb4a_db.frameset ON ispyb4a_db.measurement.runid = ispyb4a_db.frameset.runid INNER JOIN ispyb4a_db.sampleplateposition ON ispyb4a_db.sample.sampleplatepositionid = ispyb4a_db.sampleplateposition.sampleplatepositionid INNER JOIN ispyb4a_db.sampleplate ON ispyb4a_db.sampleplate.sampleplateid = ispyb4a_db.sampleplateposition.sampleplateid LEFT JOIN ispyb4a_db.stocksolution ON ispyb4a_db.sample.stocksolutionid = ispyb4a_db.stocksolution.stocksolutionid WHERE ispyb4a_db.MeasurementToDataCollection.dataCollectionId=? ORDER BY ispyb4a_db.MeasurementToDataCollection.datacollectionorder ASC";
			
		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, saxsDataCollectionId);
		boolean success = stmt.execute();
		if (success){
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				String name = rs.getString(4);
				String filename = rs.getString(5);
				if (name == null) {
					if (sinfo.name == null) {
						sinfo.bufferBeforeFileName = filename;
					} else {
						sinfo.bufferAfterFileName = filename;
						sinfos.add(sinfo);
						sinfo = new SampleInfo();
						sinfo.bufferBeforeFileName = filename;
					}
				} else {
					sinfo.name = name;
					sinfo.sampleFileName = filename;
					LocationBean loc = new LocationBean();
					loc.setPlate(Short.parseShort(rs.getString(1)));
					loc.setRow((char) ('A' + rs.getInt(2) - 1));
					loc.setColumn(rs.getShort(3));
					sinfo.location = loc;
				}
			}
			rs.close();
			stmt.close();
		}

		return sinfos;
	}

	@Override
	public List<Long> getSaxsDataCollectionsForSession(long blsessionId) throws SQLException {
		List<Long> collections = new ArrayList<Long>();
		
		connectIfNotConnected();

		String selectSql = "SELECT datacollectionId " +
				"FROM ispyb4a_db.SaxsDataCollection " +
				"WHERE blsessionId = ? " +
				"ORDER BY datacollectionId ASC";
		
		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, blsessionId);
		boolean success = stmt.execute();
		if (success){
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) 
				collections.add(rs.getLong(1));
			rs.close();
		}
		stmt.close();

		return collections;
	}
	
	@Override
	public long createExperiment(long proposalId, String name, String experimentType, String comments) throws SQLException {
		long experimentId = -1;

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Experiment (" +
				"experimentId, proposalId, name, experimentType, sourceFilePath, comments, dataAcquisitionFilePath) " +
				"VALUES (ispyb4a_db.s_Experiment.nextval, ?, ?, ?, ?) RETURNING experimentId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, proposalId);
		stmt.setString(2, name);
		stmt.setString(3, experimentType);
		stmt.setString(4, comments);

		stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
		stmt.execute();
		experimentId = stmt.getLong(5);
		stmt.close();
		return experimentId;
	}
}
