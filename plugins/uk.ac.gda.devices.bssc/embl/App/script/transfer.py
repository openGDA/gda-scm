import sc
import time
import sys
import sctools
reload (sctools)
from sctools import *


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
    # read parameters
    remote=(sys.argv[1].lower()=="true")
    plate_from=int(sys.argv[2])
    row_from=int(sys.argv[3])
    col_from=int(sys.argv[4])
    plate_to=int(sys.argv[5])
    row_to=int(sys.argv[6])
    col_to=int(sys.argv[7])
    sample_volume=float(sys.argv[8])
    viscosity=float(sys.argv[9])
    speed=getDefaultSpeed(viscosity)
    speed=speed*1.5
    sample_volume=sample_volume + getLostVolumeCompensation(sample_volume,viscosity)

    #Get the sample
    sc.movePlateDefault()
    initializeSyringeForTransfer()

    moveBottomWellWithVolumeDetection(plate_from,row_from, col_from,sample_volume)
    
    sample_detection = sc.isSampleVolumeDetectionInWellEnabled()
    if sample_detection:
        assertEnoughLiquidVolume(sample_volume)

    sc.pushSyringe(sample_volume, speed)

    liquid_volume_in_well = moveBottomWellWithVolumeDetection(plate_to,row_to, col_to)
    if sample_detection:
        assertEnoughFreeVolume(sample_volume)

    disposeSyringe(speed,plate_to,row_to, col_to,sample_detection)
    print "OK"

finally:
    finalMoveTableDefault()
    sc.releaseRemoteControl()
