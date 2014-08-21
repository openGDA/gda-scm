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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import oracle.jdbc.OracleConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.BioSAXSDataCollectionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;

/*
 * An Oracle extension of the abstract BioSAXSISPyB class TODO run ispybb-oracle.sql on live database TODO run
 * create_ro_role.sql, create_rw_role.sql and users.sql on live database
 */
public class BioSAXSISPyBviaOracle implements BioSAXSISPyB {
	private static final long INVALID_VALUE = -1l;
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSISPyBviaOracle.class);
	private static final String DATA_ANALYSIS_NOT_STARTED = "DataAnalysisNotStarted";
	private static final String DATA_ANALYSIS_RUNNING = "DataAnalysisRunning";
	private static final String DATA_ANALYSIS_FAILED = "DataAnalysisFailed";
	private static final String DATA_ANALYSIS_COMPLETE = "DataAnalysisComplete";

	private static final String DATA_REDUCTION_NOT_STARTED = "DatRedNot";
	private static final String DATA_REDUCTION_COMPLETE = "DatRedCom";
	private static final String DATA_REDUCTION_FAILED = "DatRedFai";
	private static final String DATA_REDUCTION_RUNNING = "DatRedRun";

	private static final String DATA_COLLECTION_FAILED = "DataCollectionFailed";
	private static final String DATA_COLLECTION_RUNNING = "DataCollectionRunning";

	// the following are values of MeasurementToDataCollection datacollectionorder for the named measurements
	private static final int BUFFER_BEFORE_MEASUREMENT = 1;
	private static final int SAMPLE_MEASUREMENT = 2;
	private static final int BUFFER_AFTER_MEASUREMENT = 3;

	private static String EXPERIMENTSTATUS_FINISHED = "FINISHED";
	private static String EXPERIMENTSTATUS_ABORTED = "ABORTED";

	Connection conn = null;
	String URL = null;
	long blsessionId;
	private NotifyISpyBObserversObject notifyObject;

	public BioSAXSISPyBviaOracle(String mode, NotifyISpyBObserversObject notifyObject) {
		URL = mode;
		this.notifyObject = notifyObject;
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

		String selectSql = "SELECT sessionId "
				+ "FROM ispyb4a_db.BLSession bs INNER JOIN ispyb4a_db.Proposal p on (bs.proposalId = p.proposalId) "
				+ "WHERE p.proposalCode || p.proposalNumber || '-' || bs.visit_number = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)){
			stmt.setString(1, visitname);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next())
						sessionId = rs.getLong(1);
				} catch (SQLException e) {
					logger.error("Exception while getting results for session for visit", e);
					throw new SQLException(e);
				}
			}
		} catch (SQLException e) {
			logger.error("Exception while getting session for visit", e);
			throw new SQLException(e);
		}

		return sessionId;
	}

	private long createSaxsDataCollection(long blsessionId, long experimentId) throws SQLException {
		long saxsDataCollectionId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SaxsDataCollection (datacollectionId, blsessionId, experimentId) "
				+ "VALUES (ispyb4a_db.s_SaxsDataCollection.nextval, ?, ?) RETURNING datacollectionId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.setLong(1, blsessionId);
			stmt.setLong(2, experimentId);
			stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			stmt.execute();
			saxsDataCollectionId = stmt.getLong(3);
		} catch (SQLException e) {
			logger.error("Exception while creating SAXS data collection", e);
			throw new SQLException(e);
		}

		return saxsDataCollectionId;
	}

	protected long createMeasurement(long sampleId, float exposureTemperature, double flow, String viscosity)
			throws SQLException {
		long measurementId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Measurement ("
				+ "measurementId, specimenId, exposureTemperature, flow, viscosity, priorityLevelId) "
				+ "VALUES (ispyb4a_db.s_Measurement.nextval, ?, ?, ?, ?, 1) RETURNING measurementId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setLong(index++, sampleId);
			stmt.setFloat(index++, exposureTemperature);
			stmt.setDouble(index++, flow);
			stmt.setString(index++, viscosity);

			stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
			stmt.execute();
			measurementId = stmt.getLong(index);
		} catch (SQLException e) {
			logger.error("Exception while creating measurement", e);
			throw new SQLException(e);
		}

		return measurementId;
	}

	protected long createBuffer(long blsessionId, String name, String acronym, String composition) throws SQLException {
		long bufferId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Buffer (bufferId, blsessionId, proposalId, name, acronym, composition) "
				+ "VALUES (ispyb4a_db.s_Buffer.nextval, ?, ?, ?, ?, ?) RETURNING bufferId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setLong(index++, blsessionId);
			stmt.setLong(index++, getProposalFromSession(blsessionId));
			stmt.setString(index++, name);
			stmt.setString(index++, acronym);
			stmt.setString(index++, composition);

			stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
			stmt.execute();
			bufferId = stmt.getLong(index);
		} catch (SQLException e) {
			logger.error("Exception while creating buffer", e);
			throw new SQLException(e);
		}

		return bufferId;
	}

	private long createPlateGroup(String name, double storageTemperature) throws SQLException {
		long plateGroupId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.PlateGroup (plateGroupId, name, storageTemperature) "
				+ "VALUES (ispyb4a_db.s_PlateGroup.nextval, ?, ?) RETURNING plateGroupId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.setString(1, name);
			stmt.setDouble(2, storageTemperature);

			stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
			stmt.execute();
			plateGroupId = stmt.getLong(3);
		} catch (SQLException e) {
			logger.error("Exception while creating plate group", e);
			throw new SQLException(e);
		}

		return plateGroupId;
	}

	protected long createSamplePlate(long blsessionId, long experimentId, String name, long plateGroupId, short plate) throws SQLException {
		long samplePlateId = -1;
		short plateTypeId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SamplePlate (samplePlateId, experimentId, blsessionId, name, plateGroupId, plateTypeId, slotPositionColumn) "
				+ "VALUES (ispyb4a_db.s_SamplePlate.nextval, ?, ?, ?, ?, ?, ?) RETURNING samplePlateId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setLong(index++, experimentId);
			stmt.setLong(index++, blsessionId);
			stmt.setString(index++, name);
			stmt.setLong(index++, plateGroupId);

			//TODO use some beamline-staff changeable method instead of hard-coding
			//the plateTypeIds are from ISPyBB table PlateType
			if (plate == 1) {
				plateTypeId = 4; //96-well plate
			}
			else if (plate == 2) {
				plateTypeId = 1; //deep 96-well plate
			}
			else if (plate == 3) {
				plateTypeId = 2; // 4 x (8+3) plate
			}

			stmt.setShort(index++, plateTypeId);
			stmt.setShort(index++, plate);

			stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
			stmt.execute();
			samplePlateId = stmt.getLong(index);
		} catch (SQLException e) {
			logger.error("Exception while creating sample plate", e);
			throw new SQLException(e);
		}

		return samplePlateId;
	}

	protected long createSamplePlatePosition(long samplePlateId, short row, short column) throws SQLException {
		long samplePlatePositionId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.SamplePlatePosition ("
				+ "samplePlatePositionId, samplePlateId, rowNumber, columnNumber) "
				+ "VALUES (ispyb4a_db.s_SamplePlatePosition.nextval, ?, ?, ?) RETURNING samplePlatePositionId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.setLong(1, samplePlateId);
			stmt.setLong(2, row);
			stmt.setLong(3, column);

			stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
			stmt.execute();
			samplePlatePositionId = stmt.getLong(4);
		} catch (SQLException e) {
			logger.error("Exception while creating sample plate position", e);
			throw new SQLException(e);
		}

		return samplePlatePositionId;
	}

	private long createSpecimen(long blsessionId, long experimentId, Long bufferId, Long macromoleculeId,
			double sampleConcentrationMgMl, Long samplePlatePositionId, Long stockSolutionId, Double volume) throws SQLException {
		long specimenId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Specimen ("
				+ "specimenId, experimentId, blsessionId, bufferId, macromoleculeId, samplePlatePositionId, stockSolutionId, volume, code, concentration) "
				+ "VALUES (ispyb4a_db.s_Specimen.nextval, ?, ?, ?, ?, ?, ?, ?, ' ', ?) RETURNING specimenId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
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

			stmt.setDouble(8, sampleConcentrationMgMl);

			stmt.registerOutParameter(9, java.sql.Types.VARCHAR);
			stmt.execute();
			specimenId = stmt.getLong(9);
		} catch (SQLException e) {
			logger.error("Exception while creating specimen", e);
			throw new SQLException(e);
		}

		return specimenId;
	}

	protected long createRun(double timePerFrame, float storageTemperature, float exposureTemperature,
			double energyInkeV, int numFrames, double transmission, double beamCenterX, double beamCenterY,
			double pixelSizeX, double pixelSizeY, double radiationRelative, double radiationAbsolute,
			double normalization) throws SQLException {
		long runId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Run ("
				+ "runId, storageTemperature, exposureTemperature, energy, frameCount, timePerFrame, "
				+ "transmission, beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute, normalization, timeStart) "
				+ "VALUES (ispyb4a_db.s_Run.nextval, ?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING runId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
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
			String startDate = new Date().toString();
			stmt.setString(index++, startDate);

			stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
			stmt.execute();
			runId = stmt.getLong(index);
		} catch (SQLException e) {
			logger.error("Exception while creating run", e);
			throw new SQLException(e);
		}

		return runId;
	}

	protected long createFrameSet(long runId, String fileName, String internalPath) throws SQLException {
		long frameSetId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.FrameSet (" + "frameSetId, runId, filePath, internalPath) "
				+ "VALUES (ispyb4a_db.s_FrameSet.nextval, ?, ?, ?) RETURNING frameSetId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.setLong(1, runId);
			stmt.setString(2, fileName);
			stmt.setString(3, internalPath);

			stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
			stmt.execute();
			frameSetId = stmt.getLong(4);
		} catch (SQLException e) {
			logger.error("Exception while creating frame set", e);
			throw new SQLException(e);
		}

		return frameSetId;
	}

	private long createFrameList() throws SQLException {
		long frameListId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.FrameList (frameListId) "
				+ "VALUES (ispyb4a_db.s_FrameList.nextval) RETURNING frameListId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
			stmt.execute();
			frameListId = stmt.getLong(1);
		} catch (SQLException e) {
			logger.error("Exception while creating frame list", e);
			throw new SQLException(e);
		}

		return frameListId;
	}

	private long createMerge(long measurementId) throws SQLException {
		long mergeId = -1;
		connectIfNotConnected();
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Merge (" + "mergeId, measurementId) "
				+ "VALUES (ispyb4a_db.s_Merge.nextval, ?) RETURNING mergeId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.setLong(1, measurementId);

			stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
			stmt.execute();
			mergeId = stmt.getLong(2);
		} catch (SQLException e) {
			logger.error("Exception while creating merge", e);
			throw new SQLException(e);
		}

		return mergeId;
	}

	private boolean updateFrameSetWithFrameList(long frameSetId, long frameListId) throws SQLException {
		connectIfNotConnected();
		String selectSql1 = "UPDATE ispyb4a_db.FrameSet fr SET frameListId = ? "
				+ "WHERE fr.frameSetId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			int index = 1;
			stmt1.setLong(index++, frameListId);
			stmt1.setLong(index++, frameSetId);

			boolean success1 = stmt1.execute();
			return success1;
		} catch (SQLException e) {
			logger.error("Exception while updating frame set with frame list", e);
			throw new SQLException(e);
		}

	}

	private boolean updateMergeWithFrameList(long mergeId, long frameListId) throws SQLException {
		connectIfNotConnected();
		String selectSql1 = "UPDATE ispyb4a_db.Merge m SET frameListId = ? "
				+ "WHERE m.mergeId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			int index = 1;
			stmt1.setLong(index++, frameListId);
			stmt1.setLong(index++, mergeId);

			boolean success1 = stmt1.execute();
			return success1;
		} catch (SQLException e) {
			logger.error("Exception while updating merge with frame list", e);
			throw new SQLException(e);
		}

	}

	protected long createMacromolecule(long proposalId, String name, String acronym, double molecularMassKda) throws SQLException {
		long macromoleculeId = -1;
		connectIfNotConnected();

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Macromolecule ("
				+ "macromoleculeId, proposalId, name, acronym, molecularMass) "
				+ "VALUES (ispyb4a_db.s_Macromolecule.nextval, ?, ?, ?, ?) RETURNING macromoleculeId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setLong(index++, proposalId);
			stmt.setString(index++, name);
			stmt.setString(index++, acronym);
			stmt.setString(index++, Double.toString(molecularMassKda));

			stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
			stmt.execute();
			macromoleculeId = stmt.getLong(index);
		} catch (SQLException e) {
			logger.error("Exception while creating macromolecule", e);
			throw new SQLException(e);
		}

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
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.setLong(1, saxsDataCollectionId);
			stmt.setLong(2, measurementId);
			stmt.setLong(3, saxsDataCollectionId);

			stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
			stmt.execute();
			measurementToDataCollectionId = stmt.getLong(4);
		} catch (SQLException e) {
			logger.error("Exception while creating measurement to data collection", e);
			throw new SQLException(e);
		}

		return measurementToDataCollectionId;
	}

	@Override
	public List<SampleInfo> getSaxsDataCollectionInfo(long saxsDataCollectionId) throws SQLException {
		List<SampleInfo> sinfos = new ArrayList<SampleInfo>();
		SampleInfo sinfo = new SampleInfo();

		connectIfNotConnected();

		String selectSql = "SELECT ispyb4a_db.sampleplate.name AS plate, ispyb4a_db.sampleplateposition.rownumber, ispyb4a_db.sampleplateposition.columnnumber, ispyb4a_db.macromolecule.name, ispyb4a_db.frameset.filepath FROM ispyb4a_db.MeasurementToDataCollection INNER JOIN ispyb4a_db.measurement ON ispyb4a_db.MeasurementToDataCollection.measurementid = ispyb4a_db.measurement.measurementid INNER JOIN ispyb4a_db.specimen ON ispyb4a_db.measurement.specimenid = ispyb4a_db.specimen.specimenid INNER JOIN ispyb4a_db.frameset ON ispyb4a_db.measurement.runid = ispyb4a_db.frameset.runid INNER JOIN ispyb4a_db.sampleplateposition ON ispyb4a_db.specimen.sampleplatepositionid = ispyb4a_db.sampleplateposition.sampleplatepositionid INNER JOIN ispyb4a_db.sampleplate ON ispyb4a_db.sampleplate.sampleplateid = ispyb4a_db.sampleplateposition.sampleplateid LEFT JOIN ispyb4a_db.macromolecule ON ispyb4a_db.specimen.macromoleculeid = ispyb4a_db.macromolecule.macromoleculeid WHERE ispyb4a_db.MeasurementToDataCollection.dataCollectionId=? ORDER BY ispyb4a_db.MeasurementToDataCollection.datacollectionorder ASC";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, saxsDataCollectionId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					while (rs.next()) {
						String name = rs.getString(4);
						String filename = rs.getString(5);
						if (name == null) { // no Macromolecule name for buffer collections
							if (sinfo.getName() == null) { // no sample name is set when buffer before is processed
								sinfo.setBufferBeforeFileName(filename);
							} else { // must be a buffer after
								sinfo.setBufferAfterFileName(filename);
								sinfos.add(sinfo);
								sinfo = new SampleInfo();
								sinfo.setBufferBeforeFileName(filename);
							}
						} else { // sample collection
							sinfo.setName(name);
							sinfo.setSampleFileName(filename);
							LocationBean loc = new LocationBean(BSSCSessionBean.BSSC_PLATES);
							loc.setPlate(Short.parseShort(rs.getString(1)));
							loc.setRow((char) ('A' + rs.getInt(2) - 1));
							loc.setColumn(rs.getShort(3));
							sinfo.setLocation(loc);
						}
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}

		} catch (SQLException e) {
			logger.error("Exception while getting SAXS data collection info", e);
			throw new SQLException(e);
		}

		return sinfos;
	}

	private List<Long> getSaxsDataCollectionsForSession(long blsessionId) throws SQLException {
		List<Long> collections = new ArrayList<Long>();

		connectIfNotConnected();

		String selectSql = "SELECT datacollectionId " + "FROM ispyb4a_db.SaxsDataCollection "
				+ "WHERE blsessionId = ? " + "ORDER BY datacollectionId ASC";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, blsessionId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					while (rs.next())
						collections.add(rs.getLong(1));
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting SAXS data collections for session", e);
			throw new SQLException(e);
		}

		return collections;
	}

	@Override
	public long createExperiment(long blsessionId, String name, String experimentType, String comments)
			throws SQLException {
		long experimentId = -1;

		connectIfNotConnected();

		this.blsessionId = blsessionId; // hold onto this information
		long proposalId = getProposalFromSession(blsessionId);
		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Experiment ("
				+ "experimentId, proposalId, name, experimentType, comments) "
				+ "VALUES (ispyb4a_db.s_Experiment.nextval, ?, ?, ?, ?) RETURNING experimentId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			stmt.setLong(1, proposalId);
			stmt.setString(2, name);
			stmt.setString(3, experimentType);
			stmt.setString(4, comments);

			stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
			stmt.execute();
			experimentId = stmt.getLong(5);
		} catch (SQLException e) {
			logger.error("Exception while creating experiment", e);
			throw new SQLException(e);
		}

		return experimentId;
	}

	private long getProposalFromSession(long blsessionId) throws SQLException {
		long proposalId = -1;

		connectIfNotConnected();

		String selectSql = "SELECT proposalId FROM ispyb4a_db.BLSession bs WHERE bs.sessionId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, blsessionId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next())
						proposalId = rs.getLong(1);
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}
			}
		} catch (SQLException e) {
			logger.error("Exception while getting proposal from session", e);
			throw new SQLException(e);
		}

		return proposalId;
	}

	@SuppressWarnings("unused")
	private long getProposalFromExperiment(long experimentId) throws SQLException {
		long proposalId = -1;

		connectIfNotConnected();

		String selectSql = "SELECT proposalId FROM ispyb4a_db.Experiment ex WHERE ex.experimentId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, experimentId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next())
						proposalId = rs.getLong(1);
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting proposal from experiment", e);
			throw new SQLException(e);
		}

		return proposalId;
	}

	@Override
	public List<Long> getDataCollectionsForExperiments(long experimentId) throws SQLException {
		List<Long> dataCollectionIds = new ArrayList<Long>();

		connectIfNotConnected();

		String selectSql = "SELECT dataCollectionId FROM ispyb4a_db.SaxsDataCollection sd WHERE sd.experimentId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, experimentId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					while (rs.next())
						dataCollectionIds.add(rs.getLong(1));
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting data collections for experiments", e);
			throw new SQLException(e);
		}

		return dataCollectionIds;
	}

	private boolean isRunInMultipleMeasurementToDataCollection(Long runId) throws SQLException {
		boolean toReturn = false;

		connectIfNotConnected();

		String selectSql = "SELECT COUNT(*) FROM ispyb4a_db.Measurement m "
				+ "INNER JOIN ispyb4a_db.MeasurementToDataCollection mtd ON mtd.measurementId = m.measurementId "
				+ "WHERE m.runId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, runId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next()) {
						int numOfItems = (int) rs.getLong(1);
						if (numOfItems > 1) {
							toReturn = true;
						} else {
							toReturn = false;
						}

					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while checking if run in multiple MeasurementToDataCollections", e);
			throw new SQLException(e);
		}

		return toReturn;
	}

	@Override
	public List<Long> getExperimentsForSession(long blsessionId) throws SQLException {
		List<Long> experimentIds = new ArrayList<Long>();

		connectIfNotConnected();

		String selectSql = "SELECT DISTINCT sd.experimentId FROM ispyb4a_db.SaxsDataCollection sd "
				+ "INNER JOIN ispyb4a_db.Experiment ex ON sd.experimentId = ex.experimentId WHERE sd.blsessionId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, blsessionId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					while (rs.next())
						experimentIds.add(rs.getLong(1));
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting experiments for session", e);
			throw new SQLException(e);
		}

		return experimentIds;
	}

	private long createSubtractionForDataAnalysis(long dataCollectionId) throws SQLException {
		long subtractionId = -1;

		connectIfNotConnected();

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.Subtraction ("
				+ "subtractionId, dataCollectionId, gnomFilePath) "
				+ "VALUES (ispyb4a_db.s_Subtraction.nextval, ?, ?) RETURNING subtractionId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setLong(index++, dataCollectionId);
			stmt.setString(index++, DATA_ANALYSIS_RUNNING);

			stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
			stmt.execute();
			subtractionId = stmt.getLong(index);
		} catch (SQLException e) {
			logger.error("Exception while creating subtraction for data analysis", e);
			throw new SQLException(e);
		}

		return subtractionId;
	}

	private boolean setDataAnalysisStatusInDatabase(long dataCollectionId, ISpyBStatusInfo status) throws SQLException {
		connectIfNotConnected();
		String selectSql1 = "UPDATE ispyb4a_db.Subtraction su SET gnomFilePath=?, subtractedFilePath = ?, guinierFilePath = ? "
				+ "WHERE su.dataCollectionId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			int index = 1;
			stmt1.setString(index++, getAnalysisStringFromStatus(status.getStatus()));
			List<String> filenames = status.getFileNames();
			if (!filenames.isEmpty()) {
				stmt1.setString(index++, filenames.get(0));
			} else {
				stmt1.setNull(index++, java.sql.Types.VARCHAR);
			}
			stmt1.setString(index++, status.getMessage());
			stmt1.setLong(index++, dataCollectionId);

			boolean success1 = stmt1.execute();
			return success1;
		} catch (SQLException e) {
			logger.error("Exception while setting data analysis status in database", e);
			throw new SQLException(e);
		}

	}

	private ISpyBStatusInfo getDataAnalysisStatusFromDatabase(long dataCollectionId) throws SQLException {
		// TODO create message column in Subtraction so we can store the information without using up an existing column
		ISpyBStatusInfo info = new ISpyBStatusInfo();
		String rg = null;
		String rgGnom = null;
		String gnomFilePath = null;
		String subtractedFilePath = null;
		String guinierFilePath = null;

		connectIfNotConnected();

		String selectSql = "SELECT rg, rggnom, gnomFilePath, subtractedFilePath, guinierFilePath FROM ispyb4a_db.Subtraction su WHERE su.dataCollectionId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, dataCollectionId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next()) {
						rg = rs.getString(1);
						rgGnom = rs.getString(2);
						gnomFilePath = rs.getString(3);
						subtractedFilePath = rs.getString(4);
						guinierFilePath = rs.getString(5);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting data analysis status from database", e);
			throw new SQLException(e);
		}

		if (gnomFilePath == null || gnomFilePath.equals(DATA_ANALYSIS_NOT_STARTED)) {
			info.setStatus(ISpyBStatus.NOT_STARTED);
		} else if (gnomFilePath.equals(DATA_ANALYSIS_FAILED)) {
			info.setStatus(ISpyBStatus.FAILED);
		} else if (gnomFilePath.equals(DATA_ANALYSIS_RUNNING)) {
			info.setStatus(ISpyBStatus.RUNNING);
		} else if (gnomFilePath.equals(DATA_ANALYSIS_COMPLETE)) {
			info.setStatus(ISpyBStatus.COMPLETE);
			info.setProgress(100);
			info.addFileName(subtractedFilePath);
		} else if (rg == null || rgGnom == null) {
			info.setStatus(ISpyBStatus.FAILED);
		} else {
			info.setStatus(ISpyBStatus.FAILED);
		}

		if (guinierFilePath != null) {
			info.setMessage(guinierFilePath);
		}

		return info;
	}

	/**
	 * Check whether a data analysis has already been started - this would result in a Subtraction entry.
	 * 
	 * @param dataCollectionId
	 * @return does a Subtraction entry exist for this dataCollectionId?
	 * @throws SQLException
	 */
	private boolean isDataAnalysisExisting(long dataCollectionId) throws SQLException {
		boolean toReturn = false;

		connectIfNotConnected();

		String selectSql = "SELECT COUNT(*) FROM ispyb4a_db.Subtraction s " + "WHERE s.datacollectionid = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, dataCollectionId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next()) {
						int numOfItems = (int) rs.getLong(1);
						if (numOfItems >= 1) {
							toReturn = true;
						} else {
							toReturn = false;
						}

					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while checking whether data analysis exists", e);
			throw new SQLException(e);
		}

		return toReturn;
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
				+ "  dc.blsessionId "
				+ "FROM ispyb4a_db.SaxsDataCollection dc"
				+ "  INNER JOIN ispyb4a_db.Blsession bls ON bls.sessionid=dc.blsessionid"
				+ "  INNER JOIN ispyb4a_db.Proposal pr ON pr.proposalid=bls.proposalid"
				+ "  INNER JOIN ispyb4a_db.MeasurementToDataCollection mtd1 ON mtd1.datacollectionid = dc.datacollectionid AND mtd1.datacollectionorder = 1"
				+ "  INNER JOIN ispyb4a_db.MeasurementToDataCollection mtd3 ON mtd3.datacollectionid = dc.datacollectionid AND mtd3.datacollectionorder = 3"
				+ "  INNER JOIN ispyb4a_db.MeasurementToDataCollection mtd2 ON mtd2.datacollectionid = dc.datacollectionid AND mtd2.datacollectionorder = 2"
				+ "  INNER JOIN ispyb4a_db.Measurement m2 ON mtd2.measurementid = m2.measurementid"
				+ "  INNER JOIN ispyb4a_db.Specimen sp ON m2.specimenid = sp.specimenid"
				+ "  INNER JOIN ispyb4a_db.Macromolecule mac ON mac.macromoleculeid = sp.macromoleculeid WHERE dc.datacollectionid= ? ";
		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, dataCollectionId);
			boolean success = stmt.execute();
			if (success) {
				int index = 1;
				try (ResultSet rs = stmt.getResultSet()) {
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
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting SAXS data collection from data collection", e);
			throw new SQLException(e);
		}

		return bean;
	}

	@Override
	public List<ISAXSDataCollection> getSAXSDataCollections(long blSessionId) throws SQLException {
		List<ISAXSDataCollection> saxsDataCollections = new ArrayList<ISAXSDataCollection>();

		List<Long> allCollectionIds = getSaxsDataCollectionsForSession(blSessionId);
		for (Long collectionId : allCollectionIds) {
			ISAXSDataCollection collection = getSAXSDataCollection(collectionId);
			if (collection != null) {
				saxsDataCollections.add(collection);
			}
		}
		return saxsDataCollections;
	}

	private long retrievePreviousMeasurement(long previousDataCollectionId, int measurementType) throws SQLException {
		long measurementId = -1;
		connectIfNotConnected();

		String selectSql = "SELECT m.measurementId FROM ispyb4a_db.MeasurementToDataCollection m WHERE m.dataCollectionId = ? and m.dataCollectionOrder=?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, previousDataCollectionId);
			stmt.setShort(2, (short) measurementType);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next()) {
						measurementId = rs.getLong(1);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while retrieving previous measurement", e);
			throw new SQLException(e);
		}

		return measurementId;
	}

	private void updateMeasurementWithRunId(long measurementId, long runId) throws SQLException {
		connectIfNotConnected();
		String selectSql1 = "UPDATE ispyb4a_db.Measurement m SET m.runId= ? WHERE m.measurementId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			stmt1.setLong(1, runId);
			stmt1.setLong(2, measurementId);
			stmt1.execute();
		} catch (SQLException e) {
			logger.error("Exception while updating measurment with run ID", e);
			throw new SQLException(e);
		}

	}

	private List<Long> getRunsForDataCollection(long dataCollectionId) throws SQLException {
		List<Long> runs = new ArrayList<Long>();
		connectIfNotConnected();
		String selectSql = "SELECT "
				+ "  m1.runid,"
				+ "  m2.runid,"
				+ "  m3.runid "
				+ "FROM ispyb4a_db.SaxsDataCollection dc"
				+ "  INNER JOIN ispyb4a_db.MeasurementToDataCollection mtd1 ON mtd1.datacollectionid = dc.datacollectionid AND mtd1.datacollectionorder = 1"
				+ "  INNER JOIN ispyb4a_db.MeasurementToDataCollection mtd2 ON mtd2.datacollectionid = dc.datacollectionid AND mtd2.datacollectionorder = 2"
				+ "  INNER JOIN ispyb4a_db.MeasurementToDataCollection mtd3 ON mtd3.datacollectionid = dc.datacollectionid AND mtd3.datacollectionorder = 3"
				+ "  INNER JOIN ispyb4a_db.Measurement m1 ON mtd1.measurementid = m1.measurementid"
				+ "  INNER JOIN ispyb4a_db.Measurement m2 ON mtd2.measurementid = m2.measurementid"
				+ "  INNER JOIN ispyb4a_db.Measurement m3 ON mtd3.measurementid = m3.measurementid "
				+ "WHERE dc.datacollectionid= ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql)) {
			stmt1.setLong(1, dataCollectionId);
			boolean success = stmt1.execute();
			if (success) {
				try (ResultSet rs = stmt1.getResultSet()) {
					while (rs.next()) {
						if (rs.getLong(1) != 0) {
							runs.add(rs.getLong(1));
						}
						if (rs.getLong(2) != 0) {
							runs.add(rs.getLong(2));
						}
						if (rs.getLong(3) != 0) {
							runs.add(rs.getLong(3));
						}
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting runs for data collection", e);
			throw new SQLException(e);
		}

		return runs;
	}

	private void updateRunWithStatus(long runId, String status) throws SQLException {
		connectIfNotConnected();
		String selectSql1 = "UPDATE ispyb4a_db.Run r SET r.timeend= ? WHERE r.runId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			stmt1.setString(1, status);
			stmt1.setLong(2, runId);
			stmt1.execute();
		} catch (SQLException e) {
			logger.error("Exception while updating run with status", e);
			throw new SQLException(e);
		}

	}

	private String getStatusFromRun(long runId) throws SQLException {
		String status = null;
		connectIfNotConnected();
		String selectSql1 = "SELECT r.timeend FROM ispyb4a_db.Run r WHERE r.runId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			stmt1.setLong(1, runId);
			boolean success = stmt1.execute();
			if (success) {
				try (ResultSet rs = stmt1.getResultSet()) {
					if (rs.next()) {
						status = rs.getString(1);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting status from run", e);
			throw new SQLException(e);
		}

		return status;
	}

	private void setDataCollectionStatusInDatabase(long dataCollectionId, ISpyBStatusInfo status) {
		if (status.getStatus() == ISpyBStatus.FAILED) {
			try {
				List<Long> runList = getRunsForDataCollection(dataCollectionId);
				for (long runId : runList) {
					updateRunWithStatus(runId, DATA_COLLECTION_FAILED);
				}
			} catch (SQLException e) {
				logger.error("Exception while retrieving data collection status", e);
			}
		} else if (status.getStatus() == ISpyBStatus.RUNNING) {
			try {
				setDataCollectionStarted(dataCollectionId);
			} catch (SQLException e) {
				logger.error("Exception while attempting to set data collection started", e);
			}
		}
		else {
			logger.error("Not expecting to be able to set data collection status");
		}
	}

	@Override
	public void setDataCollectionStarted(long dataCollectionId) throws SQLException {
		connectIfNotConnected();

		long measurementId = retrievePreviousMeasurement(dataCollectionId, 3);
		String insertSql = "UPDATE ispyb4a_db.Measurement m SET m.comments = ? WHERE m.measurementId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(insertSql)) {
			stmt1.setString(1, DATA_COLLECTION_RUNNING);
			stmt1.setLong(2, measurementId);
			stmt1.execute();
		} catch (SQLException e) {
			logger.error("Exception while setting data collection start", e);
			throw new SQLException(e);
		}

	}

	private boolean isDataCollectionRunning(long dataCollectionId) throws SQLException {
		String status = null;
		connectIfNotConnected();
		long measurementId = retrievePreviousMeasurement(dataCollectionId, 3);
		String selectSql1 = "SELECT m.comments FROM ispyb4a_db.Measurement m WHERE m.measurementId = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			stmt1.setLong(1, measurementId);
			boolean success = stmt1.execute();
			if (success) {
				try (ResultSet rs = stmt1.getResultSet()) {
					if (rs.next()) {
						status = rs.getString(1);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while checking whether data collection is running", e);
			throw new SQLException(e);
		}

		return (status != null && status.equals(DATA_COLLECTION_RUNNING));
	}

	private void setOrUpdateDataReductionStatus(long dataCollectionId, ISpyBStatusInfo status) throws SQLException {
		ISpyBStatusInfo newInfo = getDataReductionStatusFromDatabase(dataCollectionId);
		if (newInfo.getStatus() != null || newInfo.getProgress() > 0) {
			updateDataReductionStatusInDatabase(dataCollectionId, status);
		} else {
			setDataReductionStatusInDatabase(dataCollectionId, status);
		}
	}

	private void setDataReductionStatusInDatabase(long dataCollectionId, ISpyBStatusInfo status) throws SQLException {
		String statusToSet = getReductionStatusString(status);
		@SuppressWarnings("unused")
		long dataReductionStatusId = -1;

		connectIfNotConnected();

		String insertSql = "BEGIN INSERT INTO ispyb4a_db.DataReductionStatus ("
				+ "dataReductionStatusId, dataCollectionId, status, filename, message) "
				+ "VALUES (ispyb4a_db.s_DataReductionStatus.nextval, ?, ?, ?, ?) RETURNING dataReductionStatusId INTO ?; END;";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setLong(index++, dataCollectionId);
			stmt.setString(index++, statusToSet);
			if (status.getFileNames().isEmpty()) {
				stmt.setNull(index++, java.sql.Types.VARCHAR);
			} else {
				stmt.setString(index++, status.getFileNames().get(0));
			}
			if (status.getMessage().isEmpty()) {
				stmt.setNull(index++, java.sql.Types.VARCHAR);
			} else {
				stmt.setString(index++, status.getMessage());
			}

			stmt.registerOutParameter(index, java.sql.Types.VARCHAR);
			stmt.execute();
			dataReductionStatusId = stmt.getLong(index);
		} catch (SQLException e) {
			logger.error("Exception while setting data reduction status in database", e);
			throw new SQLException(e);
		}

	}

	private void updateDataReductionStatusInDatabase(long dataCollectionId, ISpyBStatusInfo status) throws SQLException {
		String statusToSet = getReductionStatusString(status);
		@SuppressWarnings("unused")
		long dataReductionStatusId = -1;

		connectIfNotConnected();

		String insertSql = "UPDATE ispyb4a_db.DataReductionStatus dr "
				+ "SET dr.status = ?, dr.filename = ?, dr.message = ? " + "WHERE dr.datacollectionid = ?";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setString(index++, statusToSet);
			if (status.getFileNames().isEmpty()) {
				stmt.setNull(index++, java.sql.Types.VARCHAR);
			} else {
				stmt.setString(index++, status.getFileNames().get(0));
			}
			if (status.getMessage().isEmpty()) {
				stmt.setNull(index++, java.sql.Types.VARCHAR);
			} else {
				stmt.setString(index++, status.getMessage());
			}
			stmt.setLong(index++, dataCollectionId);

			stmt.execute();
		} catch (SQLException e) {
			logger.error("Exception while updating data reduction status in database", e);
			throw new SQLException(e);
		}


	}

	private ISpyBStatusInfo getDataReductionStatusFromDatabase(long dataCollectionId) throws SQLException {
		ISpyBStatusInfo info = new ISpyBStatusInfo();

		connectIfNotConnected();

		String selectSql = "SELECT dr.status, dr.filename, dr.message FROM ispyb4a_db.DataReductionStatus dr WHERE dr.dataCollectionId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setLong(1, dataCollectionId);
			boolean success = stmt.execute();
			if (success) {
				try (ResultSet rs = stmt.getResultSet()) {
					if (rs.next()) {
						ISpyBStatus drStatus = getStatusFromString(rs.getString(1));
						if (drStatus == ISpyBStatus.COMPLETE) {
							info.setProgress(100);
						} else {
							info.setProgress(0);
						}
						info.setStatus(drStatus);
						String filename = rs.getString(2);
						if (filename != null) {
							info.addFileName(filename);
						}
						String message = rs.getString(3);
						if (message != null) {
							info.setMessage(message);
						}
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting data reduction status from database", e);
			throw new SQLException(e);
		}

		return info;
	}

	private void updateExperimentStatus(long experimentId, String statusToSet) throws SQLException {
		connectIfNotConnected();

		String insertSql = "UPDATE ispyb4a_db.Experiment e "
				+ "SET e.status = ? WHERE e.experimentId = ?";
		try (CallableStatement stmt = conn.prepareCall(insertSql)) {
			int index = 1;
			stmt.setString(index++, statusToSet);
			stmt.setLong(index++, experimentId);

			stmt.execute();
		} catch (SQLException e) {
			logger.error("Exception while updating experiment status", e);
			throw new SQLException(e);
		}
		
	}

	@Override
	public void setExperimentAborted(long experimentId) {
		try {
			updateExperimentStatus(experimentId, EXPERIMENTSTATUS_ABORTED);
		} catch (SQLException e) {
			logger.error("Exception while attempting to set the experiment status to " + EXPERIMENTSTATUS_ABORTED, e);
		}
	}

	@Override
	public void setExperimentFinished(long experimentId) {
		try {
			updateExperimentStatus(experimentId, EXPERIMENTSTATUS_FINISHED);
		} catch (SQLException e) {
			logger.error("Exception while attempting to set the experiment status to " + EXPERIMENTSTATUS_FINISHED, e);
		}
	}

	private long getSamplePlate(long blsessionId, long experimentId, short slotPositionColumn) throws SQLException {
		long samplePlateId = INVALID_VALUE;

		connectIfNotConnected();
		String selectSql1 = "SELECT sp.samplePlateId FROM ispyb4a_db.SamplePlate sp " + 
				"WHERE sp.blsessionId = ? AND sp.experimentId = ? AND sp.slotPositionColumn = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			stmt1.setLong(1, blsessionId);
			stmt1.setLong(2, experimentId);
			stmt1.setShort(3, slotPositionColumn);

			boolean success = stmt1.execute();
			if (success) {
				try (ResultSet rs = stmt1.getResultSet()) {
					if (rs.next()) {
						samplePlateId = rs.getLong(1);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting sample plate", e);
			throw new SQLException(e);
		}

		return samplePlateId;
	}

	private long getSamplePlatePosition(long experimentId, long samplePlateId, short row,
			short column) throws SQLException {
		long samplePlatePositionId = INVALID_VALUE;

		connectIfNotConnected();
		String selectSql1 = "SELECT spp.samplePlatePositionId FROM ispyb4a_db.SamplePlatePosition spp " + 
				"INNER JOIN ispyb4a_db.SamplePlate sp ON spp.samplePlateId = sp.samplePlateId " +
				"WHERE sp.experimentId = ? AND spp.samplePlateId = ? AND spp.rownumber = ? AND spp.columnnumber = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			int index = 1;
			stmt1.setLong(index++, experimentId);
			stmt1.setLong(index++, samplePlateId);
			stmt1.setShort(index++, row);
			stmt1.setShort(index++, column);

			boolean success = stmt1.execute();
			if (success) {
				try (ResultSet rs = stmt1.getResultSet()) {
					if (rs.next()) {
						samplePlatePositionId = rs.getLong(1);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting sample plate position", e);
			throw new SQLException(e);
		}

		return samplePlatePositionId;
	}

	private long getBuffer(long blsessionId, long experimentId, String name, String acronym, String composition) throws SQLException {
		long bufferId = INVALID_VALUE;

		connectIfNotConnected();
		String selectSql1 = "SELECT b.bufferId FROM ispyb4a_db.Buffer b " + 
				"INNER JOIN ispyb4a_db.Specimen sp ON sp.bufferId = b.bufferId " +
				"WHERE sp.blsessionId = ? AND sp.experimentId = ? AND b.name = ? AND b.acronym = ? AND b.composition = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			int index = 1;
			stmt1.setLong(index++, blsessionId);
			stmt1.setLong(index++, experimentId);
			stmt1.setString(index++, name);
			stmt1.setString(index++, acronym);
			stmt1.setString(index++, composition);

			boolean success = stmt1.execute();
			if (success) {
				try (ResultSet rs = stmt1.getResultSet()) {
					if (rs.next()) {
						bufferId = rs.getLong(1);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting buffer", e);
			throw new SQLException(e);
		}

		return bufferId;
	}

	private long getSpecimen(long blsessionId, long experimentId, long bufferId, long samplePlatePositionId, double volume) throws SQLException {
		long specimenId = INVALID_VALUE;

		connectIfNotConnected();
		String selectSql1 = "SELECT sp.specimenId FROM ispyb4a_db.Specimen sp " + 
				"WHERE sp.blsessionId = ? AND sp.experimentId = ? AND sp.bufferId = ? " +
				"AND sp.sampleplatepositionId = ? and sp.volume = ?";
		try (PreparedStatement stmt1 = conn.prepareStatement(selectSql1)) {
			int index = 1;
			stmt1.setLong(index++, blsessionId);
			stmt1.setLong(index++, experimentId);
			stmt1.setLong(index++, bufferId);
			stmt1.setLong(index++, samplePlatePositionId);
			stmt1.setDouble(index++, volume);

			boolean success = stmt1.execute();
			if (success) {
				try (ResultSet rs = stmt1.getResultSet()) {
					if (rs.next()) {
						specimenId = rs.getLong(1);
					}
				} catch (SQLException e) {
					logger.error("Exception while getting result set", e);
					throw new SQLException(e);
				}

			}
		} catch (SQLException e) {
			logger.error("Exception while getting specimen", e);
			throw new SQLException(e);
		}

		return specimenId;
	}

	/*
	 * above here are the methods that interact directly with the database. other methods, including the interface
	 * methods are below here.
	 */

	/**
	 * Creates Measurement, Buffer, SamplePlate, and Specimen-related items
	 * 
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
	private long createMeasurementAndAssociatedItems(long blsessionId, long experimentId, short plate, short row,
			short column, String sampleName, double sampleConcentrationMgMl, double molecularMassKda, float exposureTemperature, double flow, double volume, String viscosity)
			throws SQLException {
		Long macromoleculeId = null;

		String bufferName = "buffer";
		String bufferAcronym = "acronym";
		String bufferComposition = "composition";
		long bufferId = getBuffer(blsessionId, experimentId, bufferName, bufferAcronym, bufferComposition);
		if (bufferId == INVALID_VALUE) {
			bufferId = createBuffer(blsessionId, bufferName, bufferAcronym, bufferComposition);
		}

		boolean isSample = sampleName != null && sampleName.isEmpty() == false; //sample has a defined, non-null name, buffer does not
		if (isSample) { // if this is a sample, name and concentration are
																	// defined
			macromoleculeId = createMacromolecule(getProposalFromSession(blsessionId), sampleName, sampleName, molecularMassKda);
		}

		long samplePlateId = getSamplePlate(blsessionId, experimentId, plate);
		//if plate does not already exist, create a new one
		if (samplePlateId == INVALID_VALUE) {
			long plateGroupId = createPlateGroup(sampleName, exposureTemperature);
			samplePlateId = createSamplePlate(blsessionId, experimentId, String.valueOf(plate), plateGroupId, plate);
		}

		long samplePlatePositionId = getSamplePlatePosition(experimentId, samplePlateId, row, column);
		if (samplePlatePositionId == INVALID_VALUE) {
			samplePlatePositionId = createSamplePlatePosition(samplePlateId, row, column);
		}

		long sampleId = getSpecimen(blsessionId, experimentId, bufferId, samplePlatePositionId, volume);
		if (sampleId == INVALID_VALUE || isSample) {
			double concentrationToUse = 0; //for buffer, we want 0 concentration
			if (isSample) {
				concentrationToUse = sampleConcentrationMgMl; //for sample, we want to use the declared concentration
			}
			sampleId = createSpecimen(blsessionId, experimentId, bufferId, macromoleculeId, concentrationToUse,
					samplePlatePositionId, null, volume);
		}
		long measurementId = createMeasurement(sampleId, exposureTemperature, flow, viscosity);
		return measurementId;
	}

	@Override
	public long createSaxsDataCollection(long experimentID, short plate, short row, short column, String sampleName, 
			double sampleConcentrationMgMl, double molecularMassKda,short bufferPlate, short bufferRow, short bufferColumn,
			float exposureTemperature, int numFrames, double timePerFrame, double flow, double volume, double energyInkeV, String viscosity) throws SQLException {
		return createSaxsDataCollectionUsingPreviousBuffer(experimentID, plate, row, column, sampleName, sampleConcentrationMgMl,
				molecularMassKda, bufferPlate, bufferRow, bufferColumn, exposureTemperature, numFrames, timePerFrame, flow, volume,
				energyInkeV, viscosity, INVALID_VALUE);
	}

	@Override
	public long createSaxsDataCollectionUsingPreviousBuffer(long experimentID, short plate, short row, short column,
			String sampleName, double sampleConcentrationMgMl, double molecularMassKda, short bufferPlate, short bufferRow,
			short bufferColumn, float exposureTemperature, int numFrames, double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity, long previousDataCollectionId) throws SQLException {
		long bufferBeforeMeasurementId;
		long saxsDataCollectionId = createSaxsDataCollection(blsessionId, experimentID);
		if (previousDataCollectionId == INVALID_VALUE) {
			bufferBeforeMeasurementId = createMeasurementAndAssociatedItems(blsessionId, experimentID, bufferPlate,
					bufferRow, bufferColumn, null, sampleConcentrationMgMl, molecularMassKda, exposureTemperature,
					flow, volume, viscosity);
		} else {
			bufferBeforeMeasurementId = retrievePreviousMeasurement(previousDataCollectionId, BUFFER_AFTER_MEASUREMENT);
		}
		long sampleMeasurementId = createMeasurementAndAssociatedItems(blsessionId, experimentID, plate, row, column,
				sampleName, sampleConcentrationMgMl, molecularMassKda, exposureTemperature, flow, volume, viscosity);
		long bufferAfterMeasurementId = createMeasurementAndAssociatedItems(blsessionId, experimentID, bufferPlate,
				bufferRow, bufferColumn, null, sampleConcentrationMgMl, molecularMassKda, exposureTemperature, flow,
				volume, viscosity);
		// now we must relate the Measurements to the SaxsDataCollection
		createMeasurementToDataCollection(saxsDataCollectionId, bufferBeforeMeasurementId);
		createMeasurementToDataCollection(saxsDataCollectionId, sampleMeasurementId);
		createMeasurementToDataCollection(saxsDataCollectionId, bufferAfterMeasurementId);

		sendISpyBUpdate(saxsDataCollectionId);

		return saxsDataCollectionId;
	}

	@Override
	public long createBufferRun(long currentDataCollectionId, double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath) {
		long runId = 0;
		try {
			long bufferMeasurementId = 0;
			ISpyBStatusInfo currentStatus = getDataCollectionStatus(currentDataCollectionId);
			if (currentStatus.getStatus() == ISpyBStatus.RUNNING && currentStatus.getProgress() == 0) { // must be
																											// buffer
																											// before
				currentStatus.setProgress(33);
				currentStatus.addFileName(filename);
				bufferMeasurementId = retrievePreviousMeasurement(currentDataCollectionId, BUFFER_BEFORE_MEASUREMENT);
			} else if (currentStatus.getStatus() == ISpyBStatus.RUNNING && currentStatus.getProgress() == 66) { // must
																												// be
																												// buffer
																												// after
				currentStatus.setStatus(ISpyBStatus.COMPLETE);
				currentStatus.setProgress(100);
				currentStatus.addFileName(filename);
				bufferMeasurementId = retrievePreviousMeasurement(currentDataCollectionId, BUFFER_AFTER_MEASUREMENT);
			}

			// TODO store status information in database
			runId = createRunFrameSetFrameListMerge(bufferMeasurementId, timePerFrame, storageTemperature, exposureTemperature, energy, frameCount, transmission,
					beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute, normalization, filename, internalPath);

			updateMeasurementWithRunId(bufferMeasurementId, runId);

			sendISpyBUpdate(currentDataCollectionId);

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
		long runId = 0;
		ISpyBStatusInfo currentStatus;
		try {
			currentStatus = getDataCollectionStatus(dataCollectionId);
			currentStatus.setStatus(ISpyBStatus.RUNNING);
			currentStatus.setProgress(66);
			currentStatus.addFileName(filename);
		} catch (SQLException e1) {
			logger.error("Exception while trying to update collection status during sample run", e1);
		}
		long sampleMeasurementId = 0;
		try {
			sampleMeasurementId = retrievePreviousMeasurement(dataCollectionId, SAMPLE_MEASUREMENT);
			runId = createRunFrameSetFrameListMerge(sampleMeasurementId, timePerFrame, storageTemperature, exposureTemperature, energy, frameCount, transmission,
					beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute, normalization, filename, internalPath);
			updateMeasurementWithRunId(sampleMeasurementId, runId);

			sendISpyBUpdate(dataCollectionId);

		} catch (SQLException e) {
			logger.error("Exception while getting sample measurement or updating Measurement.runId", e);
		}

		return runId;
	}

	private long createRunFrameSetFrameListMerge(long measurementId, double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath) {
		long runId = 0;
		try {
			runId = createRun(timePerFrame, storageTemperature, exposureTemperature, energy, frameCount, transmission,
					beamCenterX, beamCenterY, pixelSizeX, pixelSizeY, radiationRelative, radiationAbsolute, normalization);
			long frameSetId = createFrameSet(runId, filename, internalPath);
			long frameListId = createFrameList();
			long mergeId = createMerge(measurementId);
			updateFrameSetWithFrameList(frameSetId, frameListId);
			updateMergeWithFrameList(mergeId, frameListId);
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
		setDataCollectionStatusInDatabase(dataCollectionId, status);

		sendISpyBUpdate(dataCollectionId);
		
		System.out.println("Sending UDP update for dataCollectionId : " + dataCollectionId);
		System.out.println("Collection Status is : " + status.getStatus());
		System.out.println("Collection Progress is : " + status.getProgress());
	}
	
	@Override
	public ISpyBStatusInfo getDataCollectionStatus(long dataCollectionId) throws SQLException {
		return getSAXSDataCollection(dataCollectionId).getCollectionStatus();

	}

	@Override
	public void setDataReductionStatus(long dataCollectionId, ISpyBStatusInfo status) throws SQLException {
		setOrUpdateDataReductionStatus(dataCollectionId, status);

		sendISpyBUpdate(dataCollectionId);
	}

	@Override
	public ISpyBStatusInfo getDataReductionStatus(long dataCollectionId) throws SQLException {
		return getSAXSDataCollection(dataCollectionId).getReductionStatus();
	}

	@Override
	public long createDataAnalysis(long dataCollectionId) throws SQLException {
		long subtractionId = createSubtractionForDataAnalysis(dataCollectionId);
		return subtractionId;
	}

	@Override
	public void setDataAnalysisStatus(long dataCollectionId, ISpyBStatusInfo status) throws SQLException {
		if (!isDataAnalysisExisting(dataCollectionId)) {
			createSubtractionForDataAnalysis(dataCollectionId);
		}
		setDataAnalysisStatusInDatabase(dataCollectionId, status);

		sendISpyBUpdate(dataCollectionId);
	}

	@Override
	public ISpyBStatusInfo getDataAnalysisStatus(long dataCollectionId) throws SQLException {
		return getSAXSDataCollection(dataCollectionId).getAnalysisStatus();
	}

	/**
	 * Retrieve data collection status and place in collectionsMap
	 * 
	 * @param dataCollectionId
	 */
	@Override
	public ISAXSDataCollection getSAXSDataCollection(long dataCollectionId) {
		try {
			ISAXSDataCollection bioSaxsDataCollection;
			bioSaxsDataCollection = getSAXSDataCollectionFromDataCollection(dataCollectionId);
			bioSaxsDataCollection.setCollectionStatus(getDataCollectionStatusFromDatabase(dataCollectionId));
			bioSaxsDataCollection.setReductionStatus(getDataReductionStatusFromDatabase(dataCollectionId));
			if (bioSaxsDataCollection.getReductionStatus().getStatus() == null) {
				bioSaxsDataCollection.getReductionStatus().setStatus(ISpyBStatus.NOT_STARTED);
			}
			bioSaxsDataCollection.setAnalysisStatus(getDataAnalysisStatusFromDatabase(dataCollectionId));

			return bioSaxsDataCollection;
		} catch (Exception e) {
			logger.error("Could not create SAXS data collection object for data collection id " + dataCollectionId, e);
		}
		return null;
	}

	/**
	 * Get the status of a data collection. If Runs were created, some measurements must have been made. Check for
	 * failure flags.
	 * 
	 * @param dataCollectionId
	 * @return status, including progress and state
	 * @throws Exception
	 */
	private ISpyBStatusInfo getDataCollectionStatusFromDatabase(long dataCollectionId) throws Exception {
		ISpyBStatusInfo status = new ISpyBStatusInfo();
		List<Long> runs = getRunsForDataCollection(dataCollectionId);

		List<String> filenames = getFilesForFrameSet(runs);
		for (String filename : filenames) {
			status.addFileName(filename);
		}

		if (isDataCollectionFailed(dataCollectionId)) { // failed data collection always results in progress of 0
			status.setStatus(ISpyBStatus.FAILED);
			status.setProgress(0);
		}

		else if (runs.size() == 0 && isDataCollectionRunning(dataCollectionId)) {
			status.setProgress(0);
			status.setStatus(ISpyBStatus.RUNNING);
		}

		else if (runs.size() == 0) {
			status.setProgress(0);
			status.setStatus(ISpyBStatus.NOT_STARTED);
		}

		else if (runs.size() == 1) {
			status.setProgress(33);
			// if using previous data collection buffer after, then status is NOT_STARTED
			if (isRunInMultipleMeasurementToDataCollection(runs.get(0))) {
				if (isDataCollectionRunning(dataCollectionId)) {
					status.setStatus(ISpyBStatus.RUNNING);
				}
				else {
					status.setStatus(ISpyBStatus.NOT_STARTED);
				}
			} else {
				status.setStatus(ISpyBStatus.RUNNING);
			}
		}

		else if (runs.size() == 2) {
			status.setProgress(66);
			status.setStatus(ISpyBStatus.RUNNING);
		}

		else if (runs.size() == 3) {
			status.setProgress(100);
			status.setStatus(ISpyBStatus.COMPLETE);
		}

		else {
			throw new Exception("Invalid number of Runs");
		}

		return status;
	}

	private List<String> getFilesForFrameSet(List<Long> runs) throws SQLException {
		List<String> files = new ArrayList<String>();
		connectIfNotConnected();
		for (long runId : runs) {
			String selectSql = "SELECT " + "  fs.filepath " + "FROM ispyb4a_db.FrameSet fs " + "WHERE fs.runId= ?";
			PreparedStatement stmt1 = conn.prepareStatement(selectSql);
			stmt1.setLong(1, runId);
			boolean success = stmt1.execute();
			if (success) {
				ResultSet rs = stmt1.getResultSet();
				if (rs.next()) {
					files.add(rs.getString(1));
				}

				rs.close();
			}
			stmt1.close();
		}
		return files;
	}

	@Override
	public long createDataReduction(long dataCollectionId) throws SQLException {
		ISpyBStatusInfo status = new ISpyBStatusInfo();
		status.setStatus(ISpyBStatus.RUNNING);
		setOrUpdateDataReductionStatus(dataCollectionId, status);
		return 0;
	}

	private String getReductionStatusString(ISpyBStatusInfo status) {
		String statusToSet = null;
		switch (status.getStatus()) {
		case NOT_STARTED:
			statusToSet = DATA_REDUCTION_NOT_STARTED;
			break;
		case RUNNING:
			statusToSet = DATA_REDUCTION_RUNNING;
			break;
		case FAILED:
			statusToSet = DATA_REDUCTION_FAILED;
			break;
		case COMPLETE:
			statusToSet = DATA_REDUCTION_COMPLETE;
			break;
		}
		return statusToSet;
	}

	private ISpyBStatus getStatusFromString(String statusString) {
		if (statusString.equals(DATA_REDUCTION_COMPLETE)) {
			return ISpyBStatus.COMPLETE;
		} else if (statusString.equals(DATA_REDUCTION_FAILED)) {
			return ISpyBStatus.FAILED;
		} else if (statusString.equals(DATA_REDUCTION_RUNNING)) {
			return ISpyBStatus.RUNNING;
		} else if (statusString.equals(DATA_REDUCTION_NOT_STARTED)) {
			return ISpyBStatus.NOT_STARTED;
		} else {
			return ISpyBStatus.NOT_STARTED;
		}
	}

	private String getAnalysisStringFromStatus(ISpyBStatus status) {
		if (status == ISpyBStatus.COMPLETE) {
			return DATA_ANALYSIS_COMPLETE;
		} else if (status == ISpyBStatus.FAILED) {
			return DATA_ANALYSIS_FAILED;
		} else if (status == ISpyBStatus.NOT_STARTED) {
			return DATA_ANALYSIS_NOT_STARTED;
		} else if (status == ISpyBStatus.RUNNING) {
			return DATA_ANALYSIS_RUNNING;
		}
		return DATA_ANALYSIS_NOT_STARTED;
	}

	private boolean isDataCollectionFailed(long dataCollectionId) {
		try {
			List<Long> runList = getRunsForDataCollection(dataCollectionId);
			for (long runId : runList) {
				String statusFromRun = getStatusFromRun(runId);
				if (statusFromRun != null && statusFromRun.equals(DATA_COLLECTION_FAILED)) {
					return true;
				}
			}
		} catch (SQLException e) {
			logger.error("Exception while retrieving data collection status", e);
		}
		return false;
	}

	private void sendISpyBUpdate(long collectionId) {
		if (notifyObject != null)
			notifyObject.notifyObservers(collectionId);
	}
}
