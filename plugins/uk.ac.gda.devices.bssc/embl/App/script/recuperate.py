import sc
import time
import sys
from sctools import *

VISCOSITY_LOW=1
VISCOSITY_MEDIUM=2
VISCOSITY_HIGH=3

def getDefaultSpeed(viscosity):
    if (viscosity == VISCOSITY_LOW):
        return 15
    if (viscosity == VISCOSITY_MEDIUM):
        return 10
    if (viscosity == VISCOSITY_HIGH):
        return 5
    raise "Bad viscosity value"

sc.requestRemoteControl()
try:
    remote=(sys.argv[1].lower()=="true")
    plate=float(sys.argv[2])
    row=float(sys.argv[3])
    col=float(sys.argv[4])
    speed=float(sys.argv[5])
    viscosity=int(sys.argv[6])
    sample_volume=float(sys.argv[7])

    if (speed<=0):
        speed=getDefaultSpeed(viscosity)

    col_text=str(int (col))
    sc.stopFluidDetection()
    sc.setSyringeValvePos("input")
    speed=speed*1.5


    #If the current sample volume is known and sample detection enabled
    if (sample_volume>0) and (sc.isSampleVolumeDetectionInWellEnabled()):
        moveBottomWellWithVolumeDetection(plate,row, col)
        assertEnoughFreeVolume(sample_volume)
    else:
        sc.movePlateWell(plate,row, col, sc.POSITION_TOP)
    disposeSyringe(speed,plate,row, col)
    print "OK"
    
finally:
    finalMoveTableDefault()
    sc.releaseRemoteControl()