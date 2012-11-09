import sc
import time
import sys
from sctools import *

sc.requestRemoteControl()
try:
    # read parameters
    remote=(sys.argv[1].lower()=="true")
    plate=int(sys.argv[2])
    row=int(sys.argv[3])
    col=int(sys.argv[4])
    mixing_volume=float(sys.argv[5])
    mixing_cycles=int(sys.argv[6])
    viscosity=float(sys.argv[7])
    speed=getDefaultSpeed(viscosity)

    if mixing_cycles>0:
        sc.movePlateDefault()
        initializeSyringeForTransfer()

        sc.movePlateWell(plate,row, col, sc.POSITION_TOP)
        setInsideWellSpeedZ()
        sc.movePlateWell(plate,row, col, sc.POSITION_BOTTOM)

        sc.movePlateWell(plate,row, col,sc.POSITION_BOTTOM)
        dead_volume=float(sc.getDeviceConfigEntry(sc.DEV_SAMPLE_PATH,"Dead Volume"))
        mixing_volume=min(dead_volume,mixing_volume)
        mixing_volume=max(0,mixing_volume)
        sc.pushSyringe(mixing_volume*1.2,speed)
        sc.setSyringeSpeed(speed)
        for i in range(mixing_cycles-1):
            sc.pullSyringe(mixing_volume, speed)
            sc.pushSyringe(mixing_volume, speed)

        disposeSyringe(speed,plate,row, col)
    print "OK"
finally:
    finalMoveTableDefault()
    sc.releaseRemoteControl()
