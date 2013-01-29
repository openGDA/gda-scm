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

import java.sql.Connection;
import oracle.jdbc.OracleConnection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.sql.PreparedStatement;

/*
 * An Oracle extension of the abstract BioSAXSISPyB class
 * 
 * TODO run ispybb-oracle.sql on live database
 * TODO run create_ro_role.sql, create_rw_role.sql and users.sql on live database
 */

public class BioSAXSISPyBviaOracle implements BioSAXSISPyB {

	Connection conn = null;
	
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
	
//	protected String testURL = "jdbc:oracle:thin:@sci-serv2.diamond.ac.uk:1521:xe";
//	protected String liveURL = "jdbc:oracle:thin:@duoserv12.diamond.ac.uk:1521:ispyb";
	
	String URL = null;
	
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
				return rs.getLong(1);
		}

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
		return measurementToDataCollectionId;
	}
	
	@Override
	public long createBufferMeasurement(long blsessionId, short plate, short row, short column,
			float storageTemperature, float exposureTemperature, int numFrames, double timePerFrame, double flow,
			double volume, double energyInkeV, String viscosity, String fileName, String internalPath)  throws SQLException {

		connectIfNotConnected();
		
		long bufferId = createBuffer(blsessionId);
		long samplePlateId = createSamplePlate(blsessionId, String.valueOf(plate));
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
		long samplePlateId = createSamplePlate(blsessionId, String.valueOf(plate));
		long samplePlatePositionId = createSamplePlatePosition(samplePlateId, row, column);
		long sampleId = createSample(blsessionId, (Long)null, (Long)null, samplePlatePositionId, stockSolutionId, concentration, volume);
		long runId = createRun(storageTemperature, energyInkeV, numFrames, timePerFrame);		
		long frameSetId = createFrameSet(runId, fileName, internalPath);
		long measurementId = createMeasurement(sampleId, runId, exposureTemperature, flow, viscosity);
		return measurementId;
	}
}