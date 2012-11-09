import sc
import time
import sys

remote=(sys.argv[1].lower()=="true")
volume=float(sys.argv[2])
time=float(sys.argv[3])
sc.flowSyringe(volume,time)

if (sc.isFluidDetectionActive()==False) or (sc.isTaskInterruptedByLiquidDetection()==False):
    ret = "Sample flowed"
else:
    ret = "Sample overexposed"

print ret

