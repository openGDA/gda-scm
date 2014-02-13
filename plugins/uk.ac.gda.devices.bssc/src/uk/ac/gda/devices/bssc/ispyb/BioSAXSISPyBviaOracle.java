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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import oracle.jdbc.OracleConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BioSAXSDataCollectionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;

/*
 * An Oracle extension of the abstract BioSAXSISPyB class TODO run ispybb-oracle.sql on live database TODO run
 * create_ro_role.sql, create_rw_role.sql and users.sql on live database
 */
public class BioSAXSISPyBviaOracle implements BioSAXSISPyB {
	private static final long INVALID_VALUE = -1l;
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSISPyBviaOracle.class);
	private static final String DATA_REDUCTION_STARTED = "DataReductionStarted";
	private static final String DATA_REDUCTION_ERROR = "DataReductionError";

	//the following are values of MeasurementToDataCollection datacollectionorder for the named measurements
	private static final int BUFFER_BEFORE_MEASUREMENT = 1;
	private static final int SAMPLE_MEASUREMENT = 2;
	private static final int BUFFER_AFTER_MEASUREMENT = 3;
	
	Connection conn = null;
	String URL = null;
	Map<Long, ISAXSDataCollection> collectionsMap = new HashMap<Long, ISAXSDataCollection>();
	private int previousCollectionId;

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

	private long getProposalForVisit(String visitname) throws SQLException {
		long proposalId = -1;
		connectIfNotConnected();

		String selectSql = "SELECT bs.proposalId "
				+ "FROM ispyb4a_db.BLSession bs INNER JOIN ispyb4a_db.Proposal p on (bs.proposalId = p.proposalId) "
				+ "WHERE p.proposalCode || p.proposalNumber || '-' || bs.visit_number = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setString(1, visitname);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next())
				proposalId = rs.getLong(1);
			rs.close();
		}
		stmt.close();

		return proposalId;
	}

	@Override
	public long getSessionForVisit(String visitname) throws SQLException {
		long sessionId = -1;
		connectIfNotConnected();

		String selectSql = "SELECT sessionId "
				+ "FROM ispyb4a_db.BLSession bs INNER JOIN ispyb4a_db.Proposal p on (bs.proposalId = p.proposalId) "
				+ "WHERE p.proposalCode || p.proposalNumber || '-' || bs.visit_number = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setString(1, visitname);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next())
				sessionId = rs.getLong(1);
			rs.close();
		}
		stmt.close();

		return sessionId;
	}

	private long createSaxsDataCollection(long blsessionId, long experimentId) throws SQLException {
		long saxsDataCollectionId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SaxsDataCollection (datacollectionId, blsessionId, experimentId) "
				+ "VALUES (ispyb4a_db.s_SaxsDataCollection.nextval, ?, ?) RETURNING datacollectionId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, blsessionId);
		stmt.setLong(2, experimentId);
		stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
		stmt.execute();
		saxsDataCollectionId = stmt.getLong(3);
		stmt.close();

		return saxsDataCollectionId;
	}

	protected long createMeasurement(long sampleId, float exposureTemperature, double flow, String viscosity)
			throws SQLException {
		long measurementId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Measurement ("
				+ "measurementId, specimenId, exposureTemperature, flow, viscosity) "
				+ "VALUES (ispyb4a_db.s_Measurement.nextval, ?, ?, ?, ?) RETURNING measurementId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		int index = 1;
		stmt.setLong(index++, sampleId);
		stmt.setFloat(index++, exposureTemperature);
		stmt.setDouble(index++, flow);
		stmt.setString(index++, viscosity);

		stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
		stmt.execute();
		measurementId = stmt.getLong(index);
		stmt.close();

		return measurementId;
	}

	protected long createBuffer(long blsessionId, String name, String acronym, String composition) throws SQLException {
		long bufferId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Buffer (bufferId, blsessionId, proposalId, name, acronym, composition) "
				+ "VALUES (ispyb4a_db.s_Buffer.nextval, ?, ?, ?, ?, ?) RETURNING bufferId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		int index = 1;
		stmt.setLong(index++, blsessionId);
		stmt.setLong(index++, getProposalFromSession(blsessionId));
		stmt.setString(index++, name);
		stmt.setString(index++, acronym);
		stmt.setString(index++, composition);

		stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
		stmt.execute();
		bufferId = stmt.getLong(index);
		stmt.close();

		return bufferId;
	}

	protected long createSamplePlate(long blsessionId, long experimentId, String name) throws SQLException {
		long samplePlateId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SamplePlate (samplePlateId, experimentId, blsessionId, name) "
				+ "VALUES (ispyb4a_db.s_SamplePlate.nextval, ?, ?, ?) RETURNING samplePlateId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, experimentId);
		stmt.setLong(2, blsessionId);
		stmt.setString(3, name);

		stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
		stmt.execute();
		samplePlateId = stmt.getLong(4);
		stmt.close();

		return samplePlateId;
	}

	protected long createSamplePlatePosition(long samplePlateId, short row, short column) throws SQLException {
		long samplePlatePositionId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SamplePlatePosition ("
				+ "samplePlatePositionId, samplePlateId, rowNumber, columnNumber) "
				+ "VALUES (ispyb4a_db.s_SamplePlatePosition.nextval, ?, ?, ?) RETURNING samplePlatePositionId INTO ?; END;";
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

	private long createSpecimen(long blsessionId, long experimentId, Long bufferId, Long macromoleculeId,
			Long samplePlatePositionId, Long stockSolutionId, Double volume) throws SQLException {
		long specimenId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Specimen ("
				+ "specimenId, experimentId, blsessionId, bufferId, macromoleculeId, samplePlatePositionId, stockSolutionId, volumen) "
				+ "VALUES (ispyb4a_db.s_Specimen.nextval, ?, ?, ?, ?, ?, ?, ?) RETURNING specimenId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		stmt.setLong(1, experimentId);

		stmt.setLong(2, blsessionId);

		if (bufferId == null)
			stmt.setNull(3, java.sql.Types.BIGINT);
		else
			stmt.setLong(3, bufferId);

		if (macromoleculeId == null)
			stmt.setNull(4, java.sql.Types.BIGINT);
		else
			stmt.setLong(4, macromoleculeId);

		if (samplePlatePositionId == null)
			stmt.setNull(5, java.sql.Types.BIGINT);
		else
			stmt.setLong(5, samplePlatePositionId);

		if (stockSolutionId == null)
			stmt.setNull(6, java.sql.Types.BIGINT);
		else
			stmt.setLong(6, stockSolutionId);

		if (volume == null)
			stmt.setNull(7, java.sql.Types.DOUBLE);
		else
			stmt.setDouble(7, volume);

		stmt.registerOutParameter(8, java.sql.Types.VARCHAR);
		stmt.execute();
		specimenId = stmt.getLong(8);
		stmt.close();
		return specimenId;
	}

	protected long createRun(double timePerFrame, float storageTemperature,
			float exposureTemperature, double energyInkeV, int numFrames, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization)
			throws SQLException {
		long runId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Run ("
				+ "runId, storageTemperature, exposureTemperature, energy, frameCount, timePerFrame"
				+ "transmission, beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute,"
				+ "normalization, filename, internalPath) "
				+ "VALUES (ispyb4a_db.s_Run.nextval, ?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?, ?, ?) RETURNING runId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		int index = 1;
		stmt.setFloat(index++, storageTemperature);
		stmt.setDouble(index++, exposureTemperature);
		stmt.setDouble(index++, energyInkeV);
		stmt.setInt(index++, numFrames);
		stmt.setDouble(index++, timePerFrame);

		stmt.setDouble(index++, transmission);
		stmt.setDouble(index++, beamCenterX);
		stmt.setDouble(index++, beamCenterY);
		stmt.setDouble(index++, pixelSizeX);
		stmt.setDouble(index++, pixelSizeY);
		stmt.setDouble(index++, radiationRelative);
		stmt.setDouble(index++, radiationAbsolute);
		stmt.setDouble(index++, normalization);

		stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
		stmt.execute();
		runId = stmt.getLong(index);
		stmt.close();

		return runId;
	}

	protected long createFrameSet(long runId, String fileName, String internalPath) throws SQLException {
		long frameSetId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.FrameSet (" + "frameSetId, runId, filePath, internalPath) "
				+ "VALUES (ispyb4a_db.s_FrameSet.nextval, ?, ?, ?) RETURNING frameSetId INTO ?; END;";
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

	protected long createMacromolecule(long proposalId, String name, String acronym) throws SQLException {
		long macromoleculeId = -1;
		connectIfNotConnected();

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Macromolecule ("
				+ "macromoleculeId, proposalId, name, acronym) "
				+ "VALUES (ispyb4a_db.s_Macromolecule.nextval, ?, ?, ?) RETURNING macromoleculeId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		int index = 1;
		stmt.setLong(index++, proposalId);
		stmt.setString(index++, name);
		stmt.setString(index++, acronym);

		stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
		stmt.execute();
		macromoleculeId = stmt.getLong(index);
		stmt.close();
		return macromoleculeId;
	}

	/*
	 * Add row to the MeasurementToDataCollection table with a dataCollectionOrder which is 1 greater than the max for
	 * existing entries for this dataCollectionId.
	 */
	private long createMeasurementToDataCollection(long saxsDataCollectionId, long measurementId) throws SQLException {
		long measurementToDataCollectionId = -1;
		connectIfNotConnected();

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.MeasurementToDataCollection ("
				+ "measurementToDataCollectionId, dataCollectionId, measurementId, dataCollectionOrder) "
				+ "VALUES (ispyb4a_db.s_MeasurementToDataCollection.nextval, ?, ?, ("
				+ "  SELECT nvl(max(dataCollectionOrder) ,0)+1" + "    FROM ispyb4a_db.MeasurementToDataCollection"
				+ "    WHERE dataCollectionId = ?)" + ") RETURNING measurementToDataCollectionId INTO ?; END;";
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
	public List<SampleInfo> getSaxsDataCollectionInfo(long saxsDataCollectionId) throws SQLException {
		List<SampleInfo> sinfos = new ArrayList<SampleInfo>();
		SampleInfo sinfo = new SampleInfo();

		connectIfNotConnected();

		String selectSql = "SELECT ispyb4a_db.sampleplate.name AS plate, ispyb4a_db.sampleplateposition.rownumber, ispyb4a_db.sampleplateposition.columnnumber, ispyb4a_db.macromolecule.name, ispyb4a_db.frameset.filepath FROM ispyb4a_db.MeasurementToDataCollection INNER JOIN ispyb4a_db.measurement ON ispyb4a_db.MeasurementToDataCollection.measurementid = ispyb4a_db.measurement.specimenid INNER JOIN ispyb4a_db.specimen ON ispyb4a_db.measurement.specimenid = ispyb4a_db.specimen.specimenid INNER JOIN ispyb4a_db.frameset ON ispyb4a_db.measurement.runid = ispyb4a_db.frameset.runid INNER JOIN ispyb4a_db.sampleplateposition ON ispyb4a_db.specimen.sampleplatepositionid = ispyb4a_db.sampleplateposition.sampleplatepositionid INNER JOIN ispyb4a_db.sampleplate ON ispyb4a_db.sampleplate.sampleplateid = ispyb4a_db.sampleplateposition.sampleplateid LEFT JOIN ispyb4a_db.macromolecule ON ispyb4a_db.specimen.macromoleculeid = ispyb4a_db.macromolecule.macromoleculeid WHERE ispyb4a_db.MeasurementToDataCollection.dataCollectionId=? ORDER BY ispyb4a_db.MeasurementToDataCollection.datacollectionorder ASC";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, saxsDataCollectionId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				String name = rs.getString(4);
				String filename = rs.getString(5);
				if (name == null) {
					if (sinfo.getName() == null) {
						sinfo.setBufferBeforeFileName(filename);
					} else {
						sinfo.setBufferAfterFileName(filename);
						sinfos.add(sinfo);
						sinfo = new SampleInfo();
						sinfo.setBufferBeforeFileName(filename);
					}
				} else {
					sinfo.setName(name);
					sinfo.setSampleFileName(filename);
					LocationBean loc = new LocationBean();
					loc.setPlate(Short.parseShort(rs.getString(1)));
					loc.setRow((char) ('A' + rs.getInt(2) - 1));
					loc.setColumn(rs.getShort(3));
					sinfo.setLocation(loc);
				}
			}
			rs.close();
			stmt.close();
		}

		return sinfos;
	}

	private List<Long> getSaxsDataCollectionsForSession(long blsessionId) throws SQLException {
		List<Long> collections = new ArrayList<Long>();

		connectIfNotConnected();

		String selectSql = "SELECT datacollectionId " + "FROM ispyb4a_db.SaxsDataCollection "
				+ "WHERE blsessionId = ? " + "ORDER BY datacollectionId ASC";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, blsessionId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			while (rs.next())
				collections.add(rs.getLong(1));
			rs.close();
		}
		stmt.close();

		return collections;
	}

	@Override
	public long createExperiment(long proposalId, String name, String experimentType, String comments)
			throws SQLException {
		long experimentId = -1;

		connectIfNotConnected();

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Experiment ("
				+ "experimentId, proposalId, name, experimentType, comments) "
				+ "VALUES (ispyb4a_db.s_Experiment.nextval, ?, ?, ?, ?) RETURNING experimentId INTO ?; END;";
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

	private long getProposalFromSession(long blsessionId) throws SQLException {
		long proposalId = -1;

		connectIfNotConnected();

		String selectSql = "SELECT proposalId FROM ispyb4a_db.BLSession bs WHERE bs.sessionId = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, blsessionId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next())
				proposalId = rs.getLong(1);
			rs.close();
		}
		stmt.close();

		return proposalId;
	}

	@SuppressWarnings("unused")
	private long getProposalFromExperiment(long experimentId) throws SQLException {
		long proposalId = -1;

		connectIfNotConnected();

		String selectSql = "SELECT proposalId FROM ispyb4a_db.Experiment ex WHERE ex.experimentId = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, experimentId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next())
				proposalId = rs.getLong(1);
			rs.close();
		}
		stmt.close();

		return proposalId;
	}

	@Override
	public List<Long> getDataCollectionsForExperiments(long experimentId) throws SQLException {
		List<Long> dataCollectionIds = new ArrayList<Long>();

		connectIfNotConnected();

		String selectSql = "SELECT dataCollectionId FROM ispyb4a_db.SaxsDataCollection sd WHERE sd.experimentId = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, experimentId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			while (rs.next())
				dataCollectionIds.add(rs.getLong(1));
			rs.close();
		}
		stmt.close();

		return dataCollectionIds;
	}

	@Override
	public List<Long> getExperimentsForSession(long blsessionId) throws SQLException {
		List<Long> experimentIds = new ArrayList<Long>();

		connectIfNotConnected();

		String selectSql = "SELECT experimentId FROM SaxsDataCollection sd "
				+ "INNER JOIN Experiment ex ON sd.experimentId = ex.experimentId WHERE sd.blsessionId = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, blsessionId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			while (rs.next())
				experimentIds.add(rs.getLong(1));
			rs.close();
		}
		stmt.close();

		return experimentIds;	}

	@Override
	public long createDataReduction(long dataCollectionId) throws SQLException {
		long subtractionId = -1;

		connectIfNotConnected();

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Subtraction ("
				+ "subtractionId, dataCollectionId, gnomFilePath) "
				+ "VALUES (ispyb4a_db.s_Subtraction.nextval, ?, ?) RETURNING experimentId INTO ?; END;";
		CallableStatement stmt = conn.prepareCall(insertSql);
		int index = 1;
		stmt.setLong(index++, dataCollectionId);
		stmt.setString(index++, DATA_REDUCTION_STARTED);

		stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
		stmt.execute();
		subtractionId = stmt.getLong(index);
		stmt.close();
		return subtractionId;
	}

	private boolean isDataReductionRunning(long subtractionId) throws SQLException {
		String gnomFilePath = getGnomFilePathFromSubtraction(subtractionId);

		return (gnomFilePath != null) && (gnomFilePath.equals(DATA_REDUCTION_STARTED));
	}

	private boolean clearDataReductionStarted(long subtractionId) {
		try {
			connectIfNotConnected();
			// now remove the current dataCollectionId so that it's effectively been deleted
			String selectSql1 = "UPDATE ispyb4a_db.Subtraction su SET dataCollectionId=-1 WHERE su.subtractionId = ?";
			PreparedStatement stmt1 = conn.prepareStatement(selectSql1);
			stmt1.setLong(1, subtractionId);
			boolean success1 = stmt1.execute();
			return success1;
		} catch (SQLException e) {
			return false;
		}
	}

	private String getGnomFilePathFromSubtraction(long subtractionId) throws SQLException {
		String gnomFilePath = null;
		connectIfNotConnected();
		String selectSql = "SELECT gnomFilePath FROM ispyb4a_db.Subtraction su WHERE su.subtractionId = ?";
		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, subtractionId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next())
				gnomFilePath = rs.getString(1);
			rs.close();
		}
		stmt.close();
		return gnomFilePath;
	}

	private boolean isDataReductionFailedToComplete(long dataCollectionId) throws SQLException {
		String gnomFilePath = getGnomFilePathFromSubtraction(dataCollectionId);
		return (gnomFilePath != null && gnomFilePath.equals(DATA_REDUCTION_ERROR));
	}

	private void setDataReductionFailedToComplete(long dataCollectionId) throws SQLException {
		connectIfNotConnected();
		String selectSql1 = "UPDATE ispyb4a_db.Subtraction su SET gnomFilePath=? WHERE su.dataCollectionId = ?";
		PreparedStatement stmt1 = conn.prepareStatement(selectSql1);
		int index = 1;
		stmt1.setString(index++, DATA_REDUCTION_ERROR);
		stmt1.setLong(index++, dataCollectionId);

		@SuppressWarnings("unused")
		boolean success1 = stmt1.execute();
		return;
	}

	private boolean isDataReductionFailed(long dataCollectionId) throws SQLException {
		String rg = null;
		String rgGnom = null;
		String gnomFilePath = null;

		connectIfNotConnected();

		String selectSql = "SELECT rg, rggnom, subtractedFilePath FROM ispyb4a_db.Subtraction su WHERE su.dataCollectionId = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, dataCollectionId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next()) {
				rg = rs.getString(1);
				rgGnom = rs.getString(2);
				gnomFilePath = rs.getString(3);
			}

			rs.close();
		}
		stmt.close();

		return (gnomFilePath == null || rg == null || rgGnom == null);
	}

	private boolean isDataReductionSuccessful(long dataCollectionId, long subtractionId) throws SQLException {
		return (!isDataReductionFailed(dataCollectionId) && !isDataReductionFailedToComplete(dataCollectionId) && !isDataReductionRunning(subtractionId));
	}

	private ISAXSDataCollection getSAXSDataCollectionFromDataCollection(long dataCollectionId) throws SQLException {
		BioSAXSDataCollectionBean bean = null;

		connectIfNotConnected();

		String selectSql = "SELECT "
				+ "  dc.experimentid,"
				+ "  pr.proposalcode,"
				+ "  bls.visit_number,"
				+ "  mtd1.measurementid as bufferbeforemeasurementid,"
				+ "  mtd3.measurementid as bufferaftermeasurementid,"
				+ "  mac.name as samplename,"
				+ "  dc.blsessionId"
				+ "FROM SaxsDataCollection dc"
				+ "  INNER JOIN Blsession bls ON bls.sessionid=dc.blsessionid"
				+ "  INNER JOIN Proposal pr ON pr.proposalid=bls.proposalid"
				+ "  INNER JOIN MeasurementToDataCollection mtd1 ON mtd1.datacollectionid = ? AND mtd1.datacollectionorder = 1"
				+ "  INNER JOIN MeasurementToDataCollection mtd3 ON mtd3.datacollectionid = ? AND mtd3.datacollectionorder = 3"
				+ "  INNER JOIN MeasurementToDataCollection mtd2 ON mtd2.datacollectionid = ? AND mtd2.datacollectionorder = 2"
				+ "  INNER JOIN Measurement m2 ON mtd2.measurementid = m2.measurementid"
				+ "  INNER JOIN Specimen sp ON m2.specimenid = sp.specimenid"
				+ "  INNER JOIN Macromolecule mac ON mac.macromoleculeid = sp.macromoleculeid WHERE dc.datacollectionid= ? ;";
		if (!collectionsMapHasDataCollection(dataCollectionId)) {
			PreparedStatement stmt = conn.prepareStatement(selectSql);
			stmt.setLong(1, dataCollectionId);
			boolean success = stmt.execute();
			if (success) {
				int index = 1;
				ResultSet rs = stmt.getResultSet();
				if (rs.next()) {
					long experimentId = rs.getLong(index++);
					String proposalCode = rs.getString(index++);
					short visitNumber = rs.getShort(index++);
					long bufferBeforeId = rs.getLong(index++);
					long bufferAfterId = rs.getLong(index++);
					String sampleName = rs.getString(index++);
					long blSessionId = rs.getLong(index++);
					bean = new BioSAXSDataCollectionBean();
					bean.setBlSessionId(blSessionId);
					bean.setBufferAfterMeasurementId(bufferAfterId);
					bean.setBufferBeforeMeasurementId(bufferBeforeId);
					bean.setExperimentId(experimentId);
					bean.setId(dataCollectionId);
					bean.setSampleName(sampleName);
					String visit = proposalCode + "-" + visitNumber;
					bean.setVisit(visit);
				}
				rs.close();
				stmt.close();
			}

		}

		return bean;
	}

	@Override
	public List<ISAXSDataCollection> getSAXSDataCollections(long blSessionId) throws SQLException {
		List<ISAXSDataCollection> saxsDataCollections = new ArrayList<ISAXSDataCollection>();

		List<Long> allCollectionIds = getSaxsDataCollectionsForSession(blSessionId);
		for (Long collectionId : allCollectionIds) {
			retrieveCollectionInfoIfNecessary(collectionId);
			ISAXSDataCollection collection = collectionsMap.get(collectionId);
			saxsDataCollections.add(collection);
		}
		return saxsDataCollections;
	}

	private long retrievePreviousMeasurement(long previousDataCollectionId, int measurementType) throws SQLException {
		long measurementId = -1;
		connectIfNotConnected();

		String selectSql = "SELECT m.measurementId FROM ispyb4a_db.MeasurementToDataCollection m WHERE m.dataCollectionId = ? and m.dataCollectionOrder=?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, previousDataCollectionId);
		stmt.setShort(2, (short)measurementType);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next()) {
				measurementId = rs.getLong(1);
			}

			rs.close();
		}
		stmt.close();

		return measurementId;
	}

	private long getBlSessionIdFromExperiment(long experimentId) throws SQLException {
		long blsessionId = -1;
		connectIfNotConnected();

		String selectSql = "SELECT e.sessionId FROM ispyb4a_db.Experiment e WHERE e.experimentId = ?";

		PreparedStatement stmt = conn.prepareStatement(selectSql);
		stmt.setLong(1, experimentId);
		boolean success = stmt.execute();
		if (success) {
			ResultSet rs = stmt.getResultSet();
			if (rs.next()) {
				blsessionId = rs.getLong(1);
			}

			rs.close();
		}
		stmt.close();

		return blsessionId;
	}

	private void updateMeasurementWithRunId(long measurementId, long runId) throws SQLException {
		connectIfNotConnected();
		String selectSql1 = "UPDATE ispyb4a_db.Measurement m SET m.runId= ? WHERE m.measurementId = ?";
		PreparedStatement stmt1 = conn.prepareStatement(selectSql1);
		stmt1.setLong(1, runId);
		stmt1.setLong(2, measurementId);
		stmt1.execute();
	}

	/* above here are the methods that interact directly with the database.
	 * other methods, including the interface methods are below here.
	 */

	/**
	 * Creates Measurement, Buffer, SamplePlate, and Specimen-related items
	 * @param blsessionId
	 * @param experimentId
	 * @param plate
	 * @param row
	 * @param column
	 * @param sampleName
	 * @param exposureTemperature
	 * @param flow
	 * @param volume
	 * @param viscosity
	 * @return measurementId
	 * @throws SQLException
	 */
	private long createMeasurementAndAssociatedItems(long blsessionId, long experimentId, short plate, short row, short column,
			String sampleName, float exposureTemperature, double flow, double volume, String viscosity) throws SQLException {
		Long macromoleculeId = null;
		
		long bufferId = createBuffer(blsessionId, "buffer", "acronym", "composition");
		if (sampleName != null && sampleName.isEmpty() == false) { //if this is a sample, name and concentration are defined
			macromoleculeId = createMacromolecule(getProposalFromSession(blsessionId), sampleName, sampleName);
		}
		long samplePlateId = createSamplePlate(blsessionId, experimentId, String.valueOf(plate));
		long samplePlatePositionId = createSamplePlatePosition(samplePlateId, row, column);
		long sampleId = createSpecimen(blsessionId, experimentId, bufferId, macromoleculeId, samplePlatePositionId,
					null, volume);
		long measurementId = createMeasurement(sampleId, exposureTemperature, flow, viscosity);
		return measurementId;
	}

	@Override
	public long createSaxsDataCollection(long experimentID, short plate, short row, short column, String sampleName,
			short bufferPlate, short bufferRow, short bufferColumn, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity) throws SQLException {
		return createSaxsDataCollectionUsingPreviousBuffer(experimentID, plate, row, column, sampleName, bufferPlate, bufferRow, bufferColumn, exposureTemperature, numFrames, timePerFrame, flow, volume, energyInkeV, viscosity, INVALID_VALUE);
	}

	@Override
	public long createSaxsDataCollectionUsingPreviousBuffer(long experimentID, short plate, short row, short column,
			String sampleName, short bufferPlate, short bufferRow, short bufferColumn, float exposureTemperature,
			int numFrames, double timePerFrame, double flow, double volume, double energyInkeV, String viscosity,
			long previousDataCollectionId) throws SQLException {
		long bufferBeforeMeasurementId;
		long blsessionId = getBlSessionIdFromExperiment(experimentID);
		long saxsDataCollectionId = createSaxsDataCollection(blsessionId, experimentID);
		if (previousDataCollectionId == INVALID_VALUE) {
			bufferBeforeMeasurementId = createMeasurementAndAssociatedItems(blsessionId, experimentID, bufferPlate, bufferRow, bufferColumn, null, exposureTemperature, flow, volume, viscosity);
		}
		else {
			bufferBeforeMeasurementId = retrievePreviousMeasurement(previousDataCollectionId, SAMPLE_MEASUREMENT);
		}
		long sampleMeasurementId = createMeasurementAndAssociatedItems(blsessionId, experimentID, bufferPlate, bufferRow, bufferColumn, sampleName, exposureTemperature, flow, volume, viscosity);
		long bufferAfterMeasurementId = createMeasurementAndAssociatedItems(blsessionId, experimentID, bufferPlate, bufferRow, bufferColumn, null, exposureTemperature, flow, volume, viscosity);
		//now we must relate the Measurements to the SaxsDataCollection
		createMeasurementToDataCollection(saxsDataCollectionId, bufferBeforeMeasurementId);
		createMeasurementToDataCollection(saxsDataCollectionId, sampleMeasurementId);
		createMeasurementToDataCollection(saxsDataCollectionId, bufferAfterMeasurementId);
		BioSAXSDataCollectionBean bean = new BioSAXSDataCollectionBean();
		bean.setBlSessionId(blsessionId);
		bean.setExperimentId(experimentID);
		bean.setId(saxsDataCollectionId);
		bean.setBufferBeforeMeasurementId(bufferBeforeMeasurementId);
		bean.setBufferAfterMeasurementId(bufferAfterMeasurementId);
		collectionsMap.put(saxsDataCollectionId, bean);
		return saxsDataCollectionId;
	}

	@Override
	public long createBufferRun(long currentDataCollectionId, double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath) {
		long runId = createRunAndFrameSet(timePerFrame, storageTemperature, exposureTemperature, energy, frameCount, transmission,
				beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute, normalization, filename, internalPath);
		try {
			long bufferMeasurementId = 0;
			ISpyBStatusInfo currentStatus = getDataCollectionStatus(currentDataCollectionId);
			if (currentStatus.getStatus() == ISpyBStatus.NOT_STARTED && currentStatus.getProgress() == 0) { //must be buffer before
				ISpyBStatusInfo newStatus = new ISpyBStatusInfo();
				newStatus.setStatus(ISpyBStatus.RUNNING);
				newStatus.setProgress(33);
				setDataCollectionStatus(currentDataCollectionId, newStatus);
				bufferMeasurementId = retrievePreviousMeasurement(currentDataCollectionId, BUFFER_BEFORE_MEASUREMENT); //TODO replace with ISAXSDataCollection object query?
			}
			else if (currentStatus.getStatus() == ISpyBStatus.RUNNING && currentStatus.getProgress() == 66) { //must be buffer after
				ISpyBStatusInfo newStatus = new ISpyBStatusInfo();
				newStatus.setStatus(ISpyBStatus.COMPLETE);
				newStatus.setProgress(100);
				setDataCollectionStatus(currentDataCollectionId, newStatus);
				bufferMeasurementId = retrievePreviousMeasurement(currentDataCollectionId, BUFFER_AFTER_MEASUREMENT);
			}
			
			updateMeasurementWithRunId(bufferMeasurementId, runId);
		} catch (SQLException e) {
			ISpyBStatusInfo newStatus = new ISpyBStatusInfo();
			newStatus.setStatus(ISpyBStatus.FAILED);
			setDataCollectionStatus(currentDataCollectionId, newStatus);
			logger.error("Exception while getting data collection status or updating Measurement.runId", e);
		}
		return runId;
	}

	@Override
	public long createSampleRun(long dataCollectionId, double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath) {
		long runId = createRunAndFrameSet(timePerFrame, storageTemperature, exposureTemperature, energy, frameCount, transmission,
					beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute, normalization, filename, internalPath);
		ISpyBStatusInfo newStatus = new ISpyBStatusInfo();
		newStatus.setStatus(ISpyBStatus.RUNNING);
		newStatus.setProgress(66);
		setDataCollectionStatus(dataCollectionId, newStatus);
		long sampleMeasurementId = 0;
		try {
			sampleMeasurementId = retrievePreviousMeasurement(dataCollectionId, SAMPLE_MEASUREMENT);
			updateMeasurementWithRunId(sampleMeasurementId, runId);
		} catch (SQLException e) {
			logger.error("Exception while getting sample measurement or updating Measurement.runId", e);
		}
		return runId;
	}

	private long createRunAndFrameSet(double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath) {
		long runId = 0;
		try {
			runId = createRun(timePerFrame, storageTemperature, exposureTemperature, energy, frameCount, transmission,
					beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute, normalization);
			createFrameSet(runId, filename, internalPath);
		} catch (SQLException e) {
			logger.error("problem while creating Run", e);
		}
		return runId;
	}

	@Override
	public List<SampleInfo> getExperimentInfo(long experimentId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDataCollectionStatus(long dataCollectionId, ISpyBStatusInfo status) {
		retrieveCollectionInfoIfNecessary(dataCollectionId);
		collectionsMap.get(dataCollectionId).setCollectionStatus(status);
	}

	@Override
	public ISpyBStatusInfo getDataCollectionStatus(long dataCollectionId) throws SQLException {
		retrieveCollectionInfoIfNecessary(dataCollectionId);
		return collectionsMap.get(dataCollectionId).getCollectionStatus();

	}

	@Override
	public void setDataReductionStatus(long dataCollectionId, ISpyBStatusInfo status)
			throws SQLException {
		retrieveCollectionInfoIfNecessary(dataCollectionId);
		collectionsMap.get(dataCollectionId).setReductionStatus(status);
	}

	@Override
	public ISpyBStatusInfo getDataReductionStatus(long dataCollectionId) throws SQLException {
		retrieveCollectionInfoIfNecessary(dataCollectionId);
		return collectionsMap.get(dataCollectionId).getReductionStatus();
	}

	@Override
	public long createDataAnalysis(long dataCollectionId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDataAnalysisStatus(long dataCollectionId, ISpyBStatusInfo status)
			throws SQLException {
		retrieveCollectionInfoIfNecessary(dataCollectionId);
		collectionsMap.get(dataCollectionId).setAnalysisStatus(status);
	}

	@Override
	public ISpyBStatusInfo getDataAnalysisStatus(long dataCollectionId) throws SQLException {
		retrieveCollectionInfoIfNecessary(dataCollectionId);
		return collectionsMap.get(dataCollectionId).getAnalysisStatus();
	}

	@Override
	public int getPreviousCollectionId(long dataCollectionId) {
		return previousCollectionId;
	}
	
	private boolean collectionsMapHasDataCollection(long dataCollectionId) {
		return collectionsMap.containsKey(dataCollectionId);
	}
	
	/**
	 * Retrieve data collection status and place in collectionsMap
	 * @param dataCollectionId
	 */
	private void retrieveCollectionInfoIfNecessary(long dataCollectionId) {
		if (collectionsMapHasDataCollection(dataCollectionId)) {
			try {
				ISAXSDataCollection bioSaxsDataCollection;
				bioSaxsDataCollection = getSAXSDataCollectionFromDataCollection(dataCollectionId);
				collectionsMap.put(dataCollectionId, bioSaxsDataCollection);
			} catch (SQLException e) {
				logger.error("Could not create SAXS data collection object", e);
			}
		}
	}
}
