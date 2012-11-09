import string
from ControllerRemoteClient import *

###################   GENERIC SCRIPT CLIENT INTERFACE    #######################
SC_PORT=9534
sc=None

def config(host="localhost",timeout=60, retries=1):
    global sc,SC_PORT
    dispose()
    sc=ControllerRemoteClient(host,SC_PORT,PROTOCOL.DATAGRAM,timeout,retries)


def dispose():
    global sc
    if not (sc is None):
        sc.dispose()
        sc=None

def log(log,level=INFO):
    return sc.log(log,level)

def showMessageBox(msg):
    return sc.showMessageBox(msg)

def showOptionBox(msg, option):
    return sc.showOptionBox(msg,option)

def showInputBox(msg):
    return sc.showInputBox(msg)

def getWorkingPath():
    return sc.getWorkingPath()

def requestRemoteControl():
    return sc.requestRemoteControl()

def releaseRemoteControl():
    return sc.releaseRemoteControl()

def execute(method,pars=None,timeout=-1):
    return sc.execute(method,pars,timeout)

def startStaticFluidDetection():
    return sc.execute("startFluidDetection",("true",))

def startFluidDetection():
    return sc.execute("startFluidDetection")

def stopFluidDetection():
    return sc.execute("stopFluidDetection")

def getCurrentLiquidPosition():
    return sc.parseArray(sc.execute("getCurrentLiquidPosition"))

def isTaskInterruptedByLiquidDetection():
    ret=execute("isTaskInterruptedByLiquidDetection")
    return (ret.lower()=="true")

def isFluidDetectionActive():
    ret=execute("isFluidDetectionActive")
    return (ret.lower()=="true")

def startDryingDetection():
    return sc.execute("startDryingDetection")

def stopDryingDetection():
    return sc.execute("stopDryingDetection")

def isDryingComplete():
    ret=execute("isDryingComplete")
    return (ret.lower()=="true")

def resetPowerSpectroAndBCR():
    return sc.execute("resetPowerSpectroAndBCR")


###################            DEVICE ACCESS       #######################

DEV_SYRINGE="Syringe"
DEV_SYRINGE_VALVE=DEV_SYRINGE + " Valve"
DEV_CLEANING_VALVE="Cleaning Valve"
DEV_PERISTALTIC_WATER_PUMP ="Peristaltic Water Pump"
DEV_PERISTALTIC_DETERGENT_PUMP="Peristaltic Detergent Pump"
DEV_CLEAN_VENTURI="Clean Venturi"
DEV_OVERFLOW_VENTURI="Overflow Venturi"
DEV_DRY_VALVE="Dry Valve"
DEV_MOTOR_X="Table X"
DEV_MOTOR_Y="Table Y"
DEV_MOTOR_Z="Table Z"
DEV_FOCUS="Focus"
DEV_SAMPLE_PATH="Sample Path"
DEV_POWER_SPECTRO_AND_BCR= "Power Spectro And BCR"
DEV_EXPOSURE_TEMPERATURE="Exposure Temperature"
DEV_STORAGE_TEMPERATURE="Storage Temperature"
DEV_COVER_STATE="Cover State"

TWINCAT_PLC="Twincat PLC"
TWINCAT_IO="Twincat IO"
TWINCAT_NC="Twincat NC"

def setDeviceConfigEntry(dev,name,value):
    ret=execute("setConfigEntry",(dev,name,value))
    return ret

def getDeviceConfigEntry(dev,name):
    ret=execute("getConfigEntry",(dev,name))
    return ret

def waitDeviceReady(dev,timeout):
    ret=execute("waitReady",(dev,int(timeout*1000)))
    return ret

def isDeviceReady(dev):
    ret=execute("isReady",(dev,))
    return (ret.lower()=="true")

###################            3D PLATE TABLE           #######################
POSITION_BOTTOM = "BOTTOM"
POSITION_MIDDLE = "MIDDLE"
POSITION_TOP = "TOP"
def movePlateDefault():
    ret=execute("movePlateDefault")
    return ret

def movePlateWell(plate,row,col,well_position=POSITION_BOTTOM):
    ret=execute("movePlateWell",(int(plate)-1,int(row)-1,int(col)-1,well_position))
    return ret

def isPlateParked():
    ret=execute("isPlateParked",(dev,))
    return (ret.lower()=="true")


###################            2D PLATE TABLE           #######################
def moveSinglePlateWell(row,col,well_position=POSITION_BOTTOM):
    ret=execute("movePlateWell",(int(row)-1,int(col)-1,well_position))
    return ret

def movePlateWashWell(well_position=POSITION_BOTTOM):
    ret=execute("movePlateWell",("WASH",well_position))
    return ret

def movePlateRinseWell(well_position=POSITION_BOTTOM):
    ret=execute("movePlateWell",("RINSE",well_position))
    return ret

def movePlateDryWell(well_position=POSITION_BOTTOM):
    ret=execute("movePlateWell",("DRY",well_position))
    return ret



###################             MOTORS            #######################
def moveMotor(dev,pos,sync=True):
    if sync!=False:
        s="true"
    else:
        s="false"
    ret=execute("moveMotor",(dev,pos,s))
    return ret

def stopMotor(dev):
    ret=execute("stopMotor",(dev,))
    return ret

def moveMotorHome(dev,sync=True):
    if sync!=False:
        s="true"
    else:
        s="false"
    ret=execute("moveMotorHome",(dev,s))
    return ret

def getMotorPos(dev):
    ret=execute("getMotorPosition",(dev,))
    return float(ret)

def waitMotorEndOfMove(dev,timeout):
    ret=execute("waitEndOfMove",(dev,int(timeout*1000)))
    return ret

def isMotorMoving(dev):
    status=getMotorStatus(dev)
    return status["in_position"]=="false"

def isMotorHoming(dev):
    ret=execute("isMotorHoming",(dev,))
    if string.lower(ret) == "true":
        return True
    return False

def getMotorStatus(dev):
    dict={}
    ret=execute("getMotorStatus",(dev,))
    tokens= ret.split(" ")
    for token in tokens:
        aux=token.split(":")
        dict[aux[0]]=aux[1]
    return dict

def setMotorSpeed(dev,speed):
    ret=execute("setMotorSpeed",(dev,speed))
    return ret


def moveXMotor(pos,sync=1):
    return moveMotor(DEV_MOTOR_X,pos,sync)
def stopXMotor():
    return stopMotor(DEV_MOTOR_X)
def getXMotorPos():
    return getMotorPos(DEV_MOTOR_X)
def getXMotorStatus():
    return getMotorStatus(DEV_MOTOR_X)
def isMotorXMoving():
    return isMotorMoving(DEV_MOTOR_X)
def setMotorXSpeed(speed):
    return setMotorSpeed(DEV_MOTOR_X,speed)
def getMotorXConfigEntry(name):
    return getDeviceConfigEntry(DEV_MOTOR_X,name)
def waitMotorXEndOfMove(timeout):
    return waitMotorEndOfMove(DEV_MOTOR_X,timeout)


def moveYMotor(pos,sync=1):
    return moveMotor(DEV_MOTOR_Y,pos,sync)
def stopYMotor():
    return stopMotor(DEV_MOTOR_Y)
def getYMotorPos():
    return getMotorPos(DEV_MOTOR_Y)
def getYMotorStatus():
    return getMotorStatus(DEV_MOTOR_Y)
def isMotorYMoving():
    return isMotorMoving(DEV_MOTOR_Y)
def setMotorYSpeed(speed):
    return setMotorSpeed(DEV_MOTOR_Y,speed)
def getMotorYConfigEntry(name):
    return getDeviceConfigEntry(DEV_MOTOR_Y,name)
def waitMotorYEndOfMove(timeout):
    return waitMotorEndOfMove(DEV_MOTOR_Y,timeout)

def moveZMotor(pos,sync=1):
    return moveMotor(DEV_MOTOR_Z,pos,sync)
def stopZMotor():
    return stopMotor(DEV_MOTOR_Z)
def getZMotorPos():
    return getMotorPos(DEV_MOTOR_Z)
def getZMotorStatus():
    return getMotorStatus(DEV_MOTOR_Z)
def isMotorZMoving():
    return isMotorMoving(DEV_MOTOR_Z)
def setMotorZSpeed(speed):
    return setMotorSpeed(DEV_MOTOR_Z,speed)
def getMotorZConfigEntry(name):
    return getDeviceConfigEntry(DEV_MOTOR_Z,name)
def waitMotorZEndOfMove(timeout):
    return waitMotorEndOfMove(DEV_MOTOR_Z,timeout)



###################            SYRINGES           #######################

def initSyringe():
    ret=execute("initSyringe",(DEV_SYRINGE,))
    return ret

def setSyringeSpeed(speed):
    ret=execute("setSyringeSpeed",(DEV_SYRINGE,speed))
    return ret

def moveSyringe(pos):
    ret=execute("moveSyringe",(DEV_SYRINGE,pos))
    return ret

def moveSyringeAsync(pos):
    ret=execute("moveSyringe",(DEV_SYRINGE,pos,"true",0))
    return ret

def pullSyringe(volume,speed=0):
    ret=execute("pullSyringe",(DEV_SYRINGE,volume,speed))
    return ret

def pullSyringeAsync(volume,speed=0):
    ret=execute("pullSyringe",(DEV_SYRINGE,volume,speed,0))
    return ret

def pushSyringe(volume,speed=0):
    ret=execute("pushSyringe",(DEV_SYRINGE,volume,speed))
    return ret

def pushSyringeAsync(volume,speed=0):
    ret=execute("pushSyringe",(DEV_SYRINGE,volume,speed,0))
    return ret

def flowSyringe(volume,time):
    ret=execute("flowSyringe",(DEV_SYRINGE,volume,time))
    return ret
    
def flowSyringeAsync(volume,time):
    ret=execute("flowSyringe",(DEV_SYRINGE,volume,time,0))
    return ret

def stopSyringe():
    ret=execute("stopSyringe",(DEV_SYRINGE,))
    return ret

def isSyringeReady():
    ret=execute("isReady",(DEV_SYRINGE,))
    if string.lower(ret) == "true":
        return 1
    return 0

def getSyringePos():
    ret=execute("getSyringePosition",(DEV_SYRINGE,))
    return int(ret)

def getSyringeVolumePos():
    ret=execute("getSyringeVolumePosition",(DEV_SYRINGE,))
    return float(ret)

def convertSyringeVolumeToCounts(volume):
    ret=execute("convertSyringeVolumeToCounts",(DEV_SYRINGE,volume))
    return int(ret)

SYRINGE_PARS=["speed","start_speed","cutoff_speed","backlash","acceleration","min_pos","max_pos","min_speed","max_speed","min_acc","max_acc"]

def setSyringePar(par,value):
    ret=execute("setSyringeParameter",(DEV_SYRINGE,par,value))
    return ret

def getSyringePar(par):
    ret=execute("getSyringeParameter",(DEV_SYRINGE,par))
    return int(ret)

def getSyringeConfigEntry(name):
    return getDeviceConfigEntry(DEV_SYRINGE,name)

###################            SAMPLE PATH           #######################

def getSamplePathConfigEntry(name):
    return getDeviceConfigEntry(DEV_SAMPLE_PATH,name)

def setSamplePathConfigEntry(name,value):
    return setDeviceConfigEntry(DEV_SAMPLE_PATH,name,value)

def restoreFocusPosition(sync=True):
    if sync!=False:
        s="true"
    else:
        s="false"
    return sc.execute("restoreFocusPosition",(s,))

def restoreLightLevel():
    return sc.execute("restoreLightLevel")

def saveSnapshot(filename):
    ret=execute("saveSnapshot",(filename,))
    return ret

def detectCapillary():
    ret=execute("detectCapillary",("Script",))
    return ret

def autoFocus():
    ret=execute("autoFocus",("Script","true"))
    return ret

def autoSetLight():
    ret=execute("autoSetLight",("Script","true"))
    return ret

def getROI():
    ret=execute("getROI")
    return sc.parseArray(ret)

###################            VALVES           #######################

VALVE_POS=["input","output","bypass","extra"]
def initValve(dev):
    ret=execute("initValve",(dev,))
    return ret

def setValvePos(dev,pos):
    ret=execute("setValvePosition",(dev,pos))
    return ret

def getValvePos(dev):
    ret=execute("getValvePosition",(dev,))
    return ret

def setSyringeValvePos(pos):
    return setValvePos(DEV_SYRINGE_VALVE,pos)

def getSyringeValvePos():
    return getValvePos(DEV_SYRINGE_VALVE)

def initCleaningValve():
    return initValve(DEV_CLEANING_VALVE)

def setCleaningValvePos(pos):
    return setValvePos(DEV_CLEANING_VALVE,pos)

def getCleaningValvePos():
    return getValvePos(DEV_CLEANING_VALVE)

###################            PUMPS           #######################

def setPumpStateOn(dev):
    ret=execute("setPumpState",(dev,"True"))
    return ret

def setPumpStateOff(dev):
    ret=execute("setPumpState",(dev,"False"))
    return ret

def getPumpState(dev):
    ret=execute("getPumpState",(dev,))
    if string.lower(ret) == "true":
        return 1;
    return 0

PERISTALTIC_PUMP_PARS=["speed","start_speed","cutoff_speed","min_speed","max_speed"]
def setPeristalticPumpPar(dev,par,value):
    ret=execute("setPeristalticPumpParameter",(dev,par,value))
    return str(ret)

def getPeristalticPumpPar(dev,par):
    ret=execute("getPeristalticPumpParameter",(dev,par))
    return int(ret)

def initPeristalticPump(dev):
    ret=execute("initPeristalticPump",(dev,))
    return str(ret)

def setPeristalticPumpStateOn(dev):
    return setPumpStateOn (dev)

def setPeristalticPumpStateOff(dev):
    return setPumpStateOff (dev)

def getPeristalticPumpState(dev):
    return getPumpState (dev)

def setPeristalticPumpDirectionClockwise(dev):
    ret=execute("setPeristalticPumpDirection",(dev,"True"))
    return ret

def setPeristalticPumpDirectionCounterClockwise(dev):
    ret=execute("setPeristalticPumpDirection",(dev,"False"))
    return ret

def getPeristalticPumpDirectionClockwise(dev):
    ret=execute("getPeristalticPumpDirection",(dev,))
    if string.lower(ret) == "true":
        return 1;
    return 0

###################        Digital Output  Devices       #######################

def setDigDevStateOn(dev):
    ret=execute("setDigDevState",(dev,"True"))
    return ret

def setDigDevStateOff(dev):
    ret=execute("setDigDevState",(dev,"False"))
    return ret

def getDigDevState(dev):
    ret=execute("getDigDevState",(dev,))    
    if string.lower(ret) == "true":        
        return True
    return False

def setCleanVenturiStateOn():
    return setDigDevStateOn(DEV_CLEAN_VENTURI)

def setCleanVenturiStateOff():
    return setDigDevStateOff(DEV_CLEAN_VENTURI)

def getCleanVenturiState():
    return getDigDevState(DEV_CLEAN_VENTURI)

def isCoverOpen():
    return getDigDevState(DEV_COVER_STATE)

def setOverflowVenturiStateOn():
    return setDigDevStateOn(DEV_OVERFLOW_VENTURI)

def setOverflowVenturiStateOff():
    return setDigDevStateOff(DEV_OVERFLOW_VENTURI)

def getOverflowVenturiState():
    return getDigDevState(DEV_OVERFLOW_VENTURI)

def setDryValveStateOn():
    return setDigDevStateOn(DEV_DRY_VALVE)

def setDryValveStateOff():
    return setDigDevStateOff(DEV_DRY_VALVE)

def getDryValveState():
    return getOutDevState(DEV_DRY_VALVE)


###################     PNEUMATIC CLEANING STATION       #######################

def moveCleaningStation(pos):
    return execute("moveCleaningStation",(pos))

def parkCleaningStation():
    return execute("parkCleaningStation")

def moveCleaningStationWash():
    return execute("moveCleaningStationWash")

def moveCleaningStationDry():
    return execute("moveCleaningStationDry")

def getCleaningStationPosition():
    return execute("getCleaningStationPosition",(pos))

def isCleaningStationParked():
    ret=execute("isCleaningStationParked")
    return (ret.lower()=="true")

def resetCleaningStation():
    ret=execute("resetCleaningStation")
    return sc.parseArray(ret)

###################        THERMOSTATS       #######################

def getExposureTemperature():
    ret=execute("getTemperatureSampleExposure")
    return float(ret)

def getStorageTemperature():
    ret=execute("getTemperatureSampleStorage")
    return float(ret)


###################          ALIDUM          #######################

def startSampleWellDetectionLoading(volume):
    return sc.execute("startSampleWellDetection",(volume,))

def startSampleWellDetection():
    return sc.execute("startSampleWellDetection")

def stopSampleWellDetection():
    return sc.execute("stopSampleWellDetection")

def getWellVolume(plate,row,col):
    ret=execute("getWellVolume",(int(plate)-1,int(row)-1,int(col)-1))
    return float(ret)

def getLiquidVolumeInWell(plate,row,col):
    ret=execute("getLiquidVolumeInWell",(int(plate)-1,int(row)-1,int(col)-1))
    return float(ret)

def getFreeVolumeInWell(plate,row,col):
    ret=execute("getFreeVolumeInWell",(int(plate)-1,int(row)-1,int(col)-1))
    return float(ret)

def getSampleVolumeWell():
    ret=execute("getSampleVolumeWell")
    ret = float(ret)
    if str(ret).lower() == 'nan':
        return 0
    return ret

def getSampleDetectedPositionZ():
    ret=execute("getSampleDetectedPositionZ")
    ret = float(ret)
    if str(ret).lower() == 'nan':
        return 0
    return ret

def getSampleDetectionCount():
    ret=execute("getSampleDetectionCount")
    return int(ret)

def isSampleVolumeDetectionInWellEnabled():
    ret=execute("isSampleVolumeDetectionInWellEnabled")
    return (ret.lower()=="true")

###################        SPECTROMETER       #######################

def getSpectrometerReadout():
    ret=execute("getSpectrometerReadout")
    return float(ret)

def getSpectrometerDarkReadout():
    ret=execute("getSpectrometerDarkReadout")
    return float(ret)

def storeSpectrometerReadout(sync=True):
    if sync!=False:
        s="true"
    else:
        s="false"
    return execute("storeSpectrometerReadout",(s,))

def storeSpectrometerDarkReadout(sync=True):
    if sync!=False:
        s="true"
    else:
        s="false"
    return execute("storeSpectrometerDarkReadout",(s,))

def clearSpectrometerReadout():
    return execute("clearSpectrometerReadout")

def readSpectrometer(enable_strobe=True):
    if enable_strobe!=False:
        es="true"
    else:
        es="false"
    return execute("readSpectrometer",(es,))

def readSpectrometerDirect():
    return execute("readSpectrometer")

def setSpectrometerStrobeEnabled(enable=True):
    if enable!=False:
        es="true"
    else:
        es="false"
    return execute("setSpectrometerStrobeEnabled",(es,))


###################        GENERIC TWINCAT ACCESS       #######################

def readTwincat(dev,group,offset):
    ret=execute("readTwincat",(dev,group,offset))
    return int(ret)

def writeTwincat(dev,group,offset,value):
    return execute("writeTwincat",(dev,group,offset,value))

def readTwincatBySymbol(dev,symbol):
    ret=execute("readTwincat",(dev,symbol))
    return int(ret)

def writeTwincatBySymbol(dev,symbol,value):
    return execute("writeTwincat",(dev,symbol,value))

def readTwincatIO(group,offset):
    return readTwincat(TWINCAT_IO,group,offset)

def writeTwincatIO(group,offset,value):
    return writeTwincat(TWINCAT_IO,group,offset,value)

def readTwincatPLCSymbol(symbol):
    return readTwincat(TWINCAT_PLC,symbol)

def writeTwincatPLCSymbol(symbol,value):
    return writeTwincat(TWINCAT_PLC,symbol,value)


config()

if (__name__.find('main')>=0):
    requestRemoteControl()
    try:
        log("initializing")
        """
        initSyringe()
        setSyringeSpeed(1000)
        moveSyringe(200)
        pushSyringe(10,10)
        pullSyringe(10,10)
        flowSyringe(10,5)
        stopSyringe()
        log(str(isSyringeReady()),"WARNING")
        log(str(getSyringePos()),"WARNING")
        setSyringePar("speed",1000)
        log(str(getSyringePar("speed")),"WARNING")
        log(str(getSyringeConfigEntry("Syringe Volume")),"WARNING")
        initCleaningValve()
        setCleaningValvePos("output")
        log(str(getCleaningValvePos()),"WARNING")
        setSyringeValvePos("output")
        log(str(getSyringeValvePos()),"WARNING")


        setDetergentPumpStateOff()
        setDetergentPumpStateOn()
        log(getDetergentPumpState(),"WARNING")
        setWaterPumpStateOff()
        setWaterPumpStateOn()
        log(getWaterPumpState(),"WARNING")
        setPeristalticPumpStateOff()
        setPeristalticPumpStateOn()
        log(getPeristalticPumpState(),"WARNING")
        setPeristalticPumpDirectionCounterClockwise()
        setPeristalticPumpDirectionClockwise()
        log(getPeristalticPumpDirectionClockwise(),"WARNING")
        setCleanVenturiStateOff()
        setCleanVenturiStateOn()
        log(getCleanVenturiState(),"WARNING")
        setOverflowVenturiStateOff()
        setOverflowVenturiStateOn()
        log(getOverflowVenturiState(),"WARNING")

        try:
            saveSnapshot("c:\\tst.jpg")
        except:
            pass
        """
    finally:
        releaseRemoteControl()
