import sc
import time
import sys

VISCOSITY_LOW=1
VISCOSITY_MEDIUM=2
VISCOSITY_HIGH=3

SYRINGE_INIT_LOAD_POSITION = 100

def getDefaultSpeed(viscosity):
    if (viscosity == VISCOSITY_LOW):
        return 15
    if (viscosity == VISCOSITY_MEDIUM):
        return 10
    if (viscosity == VISCOSITY_HIGH):
        return 5
    raise "Bad viscosity value"

BUBBLE_UNDER_NEEDLE_COMPENSATION = 2.0  #ul


def setInsideWellSpeedZ():
    sc.setMotorZSpeed(20)

def restoreSpeedZ():
    speed=float(sc.getDeviceConfigEntry(sc.DEV_MOTOR_Z,"Default Speed"))
    sc.setMotorZSpeed(speed)

def initializeSyringeForTransfer():
    sc.setSyringeValvePos("input")
    sc.setSyringeSpeed(200)         #40% of max speed
    sc.moveSyringe(SYRINGE_INIT_LOAD_POSITION)             #300 cts = "offset"            #syringe config: no microstep!


def disposeSyringe(speed, plate,row, col, sync_move_z_up=False):
    sc.setSyringeSpeed(speed)
    setInsideWellSpeedZ()
    sc.movePlateWell(plate,row, col,sc.POSITION_BOTTOM)
    if sync_move_z_up:
        detected_position_z=sc.getSampleDetectedPositionZ()
        if (detected_position_z>0):
            offset_syringe=float(sc.getSyringePos())-SYRINGE_INIT_LOAD_POSITION #cts
            syringe_speed=float(sc.getSyringePar("speed"))
            time_move=offset_syringe/syringe_speed
            time_move*=2.0
            if time_move>0:
                offset_z=float(sc.getZMotorPos())-detected_position_z #mm
                motor_speed=offset_z/time_move
                if motor_speed>0:
                    max_speed=float(sc.getMotorZConfigEntry("Max Speed"))
                    sc.setMotorZSpeed(min(motor_speed,max_speed))
                    sc.moveZMotor(detected_position_z, False)

    sc.moveSyringe(SYRINGE_INIT_LOAD_POSITION)   #syringe config: no microstep!

    if sync_move_z_up:
        if (detected_position_z>0):
            sc.waitMotorZEndOfMove(time_move+3.0)
            setInsideWellSpeedZ()
    sc.movePlateWell(plate,row, col,sc.POSITION_MIDDLE)
    time.sleep(0.1)
    sc.setSyringeSpeed(400)   #80% of max speed
    sc.moveSyringe(0)

    sc.movePlateWell(plate,row, col,sc.POSITION_TOP)
    sc.setSyringeSpeed(200)                       #40% of max speed
    sc.moveSyringe(SYRINGE_INIT_LOAD_POSITION)

DetectedWell=(-1,-1,-1)
def assertEnoughFreeVolume(sample_volume,plate=-1,row=-1, col=-1,start_new_detection=False):
    if start_new_detection:
        detected_volume=sc.getLiquidVolumeInWell(plate,row, col)
    else:
        detected_volume=sc.getSampleVolumeWell()
    if plate==-1:
        #Just after moveBottomWellWithVolumeDetection
        (plate,row, col)=DetectedWell
    well_volume=sc.getWellVolume(plate,row, col)
    total_volume=detected_volume+sample_volume
    free_volume=well_volume-detected_volume
    sc.log("Sample volume: " + str(sample_volume) + " - Free volume in well: " + str(free_volume),sc.INFO)
    if (total_volume>well_volume):
        sc.movePlateWell(plate_from,row_from, col_from, sc.POSITION_TOP)
        raise "Not enough free volume in well"


def assertEnoughLiquidVolume(sample_volume,plate=-1,row=-1, col=-1,start_new_detection=False):
    if start_new_detection:
        detected_volume=sc.getLiquidVolumeInWell(plate,row, col)
    else:
        detected_volume=sc.getSampleVolumeWell()
    if plate==-1:
        #Just after moveBottomWellWithVolumeDetection
        (plate,row, col)=DetectedWell

    if (detected_volume<sample_volume):
        sc.movePlateWell(plate,row, col, sc.POSITION_TOP)
        raise "Not enough sample in well - detected volume = " + str(detected_volume)

def moveBottomWellWithVolumeDetection(plate,row, col, volume=-1):
    global DetectedWell
    DetectedWell=(plate,row, col)
    sc.movePlateWell(plate,row, col, sc.POSITION_TOP)
    setInsideWellSpeedZ()
    if (volume<0):
        sc.startSampleWellDetection()
    else:
        sc.startSampleWellDetectionLoading(volume)
    sc.movePlateWell(plate,row, col, sc.POSITION_BOTTOM)
    return sc.getSampleVolumeWell()

def moveBottomWell(plate,row, col):
    global DetectedWell
    DetectedWell=(-1,-1,-1)
    sc.movePlateWell(plate,row, col, sc.POSITION_TOP)
    setInsideWellSpeedZ()
    sc.movePlateWell(plate,row, col, sc.POSITION_BOTTOM)

def finalMoveTableDefault():
    try:
        restoreSpeedZ()
        sc.movePlateDefault()
    except:
        sc.log("Socket error:" + str(sys.exc_info()[1]), sc.WARNING)

def getMinMax(list):
    min=list[0]
    max=list[0]
    index=0
    min_index=0
    max_index=0
    for element in list:
        if element<min:
            min=element
            min_index=index
        if element>max:
            max=element
            max_index=index
        index=index+1
    return (min,max,min_index,max_index)

def getAverageSpectroReadout(list):
    (min,max,min_index,max_index)=getMinMax(list)
    threshold = min+((max-min)*0.5)
    start_index=max_index
    end_index=max_index
    for i in range(end_index,len(list)):
        if list[i]>threshold:
            end_index=i
        else:
            break
    for i in range(start_index,0,-1):
        if list[i]>threshold:
            start_index=i
        else:
            break

    sum=0

    if (end_index-start_index>5):
        start_index=start_index+1
        end_index=end_index-1

    count=0
    for i in range (start_index,end_index+1):
        val=list[i]
        sc.log("Value=" + str(val), sc.FINE)
        sum=sum+val
        count=count+1

    average=sum/count
    sc.log("Average=" + str(average), sc.FINE)
    return  average

def getSpectroReadout(measures):
    readout=0
    for i in range(measures):
        val=sc.readSpectrometerDirect()
        sc.log("Spectro readout=" + val,sc.INFO)
        readout=readout+float(val)
    readout=readout/measures
    return readout
