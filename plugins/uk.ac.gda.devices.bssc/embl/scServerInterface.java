/*
 * Project: BSSC - BioSAXS Sample Changer
 *
 * Date       Author    Changes
 * 01.07.09   Gobbo     Created
 *
 * Copyright 2009 by European Molecular Biology Laboratory - Grenoble
 */

package org.embl.bssc;

import org.embl.BaseException;
import org.embl.State;
import org.embl.bssc.scPrefs.SampleType;
import org.embl.bssc.scPrefs.ViscosityLevel;
import org.embl.bssc.scDevSamplePath.FocusPosition;

/**
 * The interface exported through the embedded Controller Server.
 *
 * @author <a href="mailto:alexgobbo@gmail.com">Alexandre Gobbo</a>
 */
public interface scServerInterface
{
    //Overall application state
    public State getState() throws BaseException;
    public String getStatus() throws BaseException;

    //Task synchronization
    public String getCommandOutput() throws BaseException;
    public String getCommandException() throws BaseException;
    public String[] getLastTaskInfo() throws BaseException;
    public String[] getTaskInfo(int id) throws BaseException;
    public boolean isTaskRunning(int id) throws BaseException;
    public String checkTaskResult(int id) throws BaseException;

    //Async task Methods
    public void abort() throws BaseException;
    public void restart(boolean init_hardware) throws BaseException;
    public int fill(int plate, int row, int col,double volume) throws BaseException;
    public int recuperate(int plate, int row, int col) throws BaseException;
    public int transfer(int from_plate, int from_row, int from_col, int to_plate, int to_row, int to_col,double volume) throws BaseException;
    public int mix(int plate, int row, int col,double volume, int cycles) throws BaseException;
    public int measureConcentration(int plate, int row, int col) throws BaseException;
    public int clean() throws BaseException;
    public int dry(double dry_time) throws BaseException;
    public int flow(double volume,double time) throws BaseException;
    public int flowAll(double time) throws BaseException;
    public int push(double volume, double speed) throws BaseException;
    public int pull(double volume, double speed) throws BaseException;
    public int calibrate() throws BaseException;
    public int loadPlates() throws BaseException;
    public int scanAndPark() throws BaseException;
    public int waitTemperatureSEU(double value) throws BaseException;
    public int waitTemperatureSample(double value) throws BaseException;
    public int getWellLiquidVolume(int plate, int row, int col) throws BaseException;

    //Sync methods
    public boolean detectCapillary() throws BaseException;
    public void moveSyringeForward(double speed) throws BaseException;
    public void moveSyringeBackward(double speed) throws BaseException;
    public void stopSyringe() throws BaseException;
    public double[] getPlateInfo(int index) throws BaseException;
    public double[] getPlateTypeInfo(String type) throws BaseException;
    public double getWellVolume(int plate, int row, int col) throws BaseException;
    public double getEffectiveLoadedVolume(final double volume, final ViscosityLevel viscosity) throws BaseException;
    public String[] getPlatesIDs() throws BaseException;
    public void forceEnablePlate(int plate) throws BaseException;

    //Events
    public void onExposureCellFilled() throws BaseException;

    //Atributes
    public void setTemperatureSEU(double value) throws BaseException;
    public double getTemperatureSEU() throws BaseException;
    public void setTemperatureSEUSetpoint(double value) throws BaseException;
    public double getTemperatureSEUSetpoint() throws BaseException;
    public void setTemperatureSampleStorage(double value) throws BaseException;
    public double getTemperatureSampleStorage() throws BaseException;
    public void setTemperatureSampleStorageSetpoint(double value) throws BaseException;
    public double getTemperatureSampleStorageSetpoint() throws BaseException;
    public double getSpectrometerReadout() throws BaseException;
    public double getSpectrometerDarkReadout() throws BaseException;
    public double getSpectrometerRealPathLength() throws BaseException;
    public void setBeamLocation(String str) throws BaseException;
    public String getBeamLocation() throws BaseException;
    public void setLiquidPositionFixed(boolean value) throws BaseException;
    public boolean getLiquidPositionFixed() throws BaseException;
    public void setBeamShapeEllipse(boolean value) throws BaseException;
    public double getBeamMarkVolume() throws BaseException;
    public void setSampleType(SampleType value) throws BaseException;
    public SampleType getSampleType() throws BaseException;
    public ViscosityLevel getViscosityLevel()  throws BaseException;
    public void setViscosityLevel (ViscosityLevel value)  throws BaseException;
    public void setEnablePlateBarcodeScan(boolean value) throws BaseException;
    public boolean getEnablePlateBarcodeScan() throws BaseException;
    public void setEnableSpectrometer(boolean value) throws BaseException;
    public boolean getEnableSpectrometer() throws BaseException;
    public void setEnableVolumeDetectionInWell(boolean value) throws BaseException;
    public boolean getEnableVolumeDetectionInWell() throws BaseException;
    public double getSamplePathDeadVolume() throws BaseException;
    public void setSamplePathDeadVolume(double value) throws BaseException;
    public void setLocalLockout(boolean value) throws BaseException;
    public boolean getLocalLockout() throws BaseException;
    public String getVersion() throws  BaseException;
    public String getUptime() throws  BaseException;
    public String[]  getAlarmList() throws BaseException;
    public boolean getHardwareInitPending() throws BaseException;
    public boolean getCoverOpen() throws BaseException;
    public boolean getCollisionDetected() throws BaseException;
    public boolean getPower12OK() throws BaseException;
    public boolean getWaterEmpty() throws BaseException;
    public boolean getDetergentEmpty() throws BaseException;
    public boolean getWasteFull() throws BaseException;
    public int getWaterLevel() throws BaseException;
    public int getDetergentLevel() throws BaseException;
    public int getWasteLevel() throws BaseException;
    public boolean getOverflowVenturiOK() throws BaseException;
    public boolean getCleanVenturiOK() throws BaseException;
    public boolean getFlooding() throws BaseException;
    public boolean getVacuumOK() throws BaseException;
    public State getPLCState() throws BaseException;
    public int[] getCurrentLiquidPosition() throws BaseException;
    public void setFocusPosition(FocusPosition position) throws BaseException;
    public FocusPosition getFocusPosition() throws BaseException;
    public void setLightLevel(int light_level) throws BaseException;
    public int getLightLevel() throws BaseException;
    public double getSampleVolumeWell() throws BaseException;
    public byte[] getImageJPG() throws BaseException;
}
