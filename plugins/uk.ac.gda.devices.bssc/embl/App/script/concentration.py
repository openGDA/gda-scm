import sc
import time
import sys
from sctools import *

SPECTROMETER_MINIMAL_VOLUME=20.0

def getLostVolumeCompensation(sample_volume, viscosity):
    BUBBLE_UNDER_NEEDLE_COMPENSATION = 2.0  #ul
    if (viscosity == VISCOSITY_LOW):
        return sample_volume*0.08 +  BUBBLE_UNDER_NEEDLE_COMPENSATION
    if (viscosity == VISCOSITY_MEDIUM):
        return sample_volume*0.10 +  BUBBLE_UNDER_NEEDLE_COMPENSATION
    if (viscosity == VISCOSITY_HIGH):
        return sample_volume*0.12 +  BUBBLE_UNDER_NEEDLE_COMPENSATION
    raise "Bad viscosity value"
# losses in transfer mode are smaller than in fill mode.

sc.requestRemoteControl()
try:
    remote=(sys.argv[1].lower()=="true")
    plate=int(sys.argv[2])
    row=int(sys.argv[3])
    col=int(sys.argv[4])
    viscosity=float(sys.argv[5])
    speed=getDefaultSpeed(viscosity)
    sample_volume=SPECTROMETER_MINIMAL_VOLUME
    sample_volume=sample_volume + getLostVolumeCompensation(sample_volume,viscosity)

    sc.setSpectrometerStrobeEnabled(False)
    sc.movePlateDefault()

    initializeSyringeForTransfer()

    dark_readout=getSpectroReadout(5)

    sc.setSpectrometerStrobeEnabled(True)
    moveBottomWellWithVolumeDetection(plate, row, col,sample_volume)

    sc.pushSyringe(sample_volume, speed)
    sc.movePlateWell(plate, row, col, sc.POSITION_TOP)

    spectro_pos=float(sc.getDeviceConfigEntry(sc.DEV_SAMPLE_PATH,"Spectro Position"))

    sc.pushSyringe(spectro_pos-2*sample_volume,speed)
    readouts=[]
    for i in range (2*int(sample_volume)):
        sc.pushSyringe(1.0,speed)
        val=getSpectroReadout(1)
        readouts.append(float(val))
    disposeSyringe(speed,plate,row, col)

    readout=getAverageSpectroReadout(readouts)

    print ("%4.2f %4.2f") % (readout,dark_readout)
finally:
    try:
        sc.setSpectrometerStrobeEnabled(False)
        finalMoveTableDefault()
    except:
        pass
    sc.releaseRemoteControl()


