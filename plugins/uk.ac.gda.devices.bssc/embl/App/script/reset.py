import sc
import time
import sys
import thread


sc.restoreLightLevel()

#sc.resetPowerSpectroAndBCR()
sc.stopFluidDetection()
sc.resetCleaningStation()
sc.parkCleaningStation()
#If cover is closed
if sc.isCoverOpen() == False:
    sc.stopMotor(sc.DEV_MOTOR_X)
    sc.stopMotor(sc.DEV_MOTOR_Y)
    sc.stopMotor(sc.DEV_MOTOR_Z)
    sc.moveMotorHome(sc.DEV_MOTOR_X)
    sc.moveMotorHome(sc.DEV_MOTOR_Y)
    sc.moveMotorHome(sc.DEV_MOTOR_Z)
sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_WATER_PUMP)
sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
sc.setCleanVenturiStateOff()
sc.setOverflowVenturiStateOff()
sc.setDryValveStateOff()
sc.stopSyringe();

sc.setSyringePar("speed",1400)
sc.setSyringePar("start_speed",900)
sc.setSyringePar("cutoff_speed",900)
sc.setSyringePar("backlash",0) #default=20

sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"speed", 3000)
sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"start_speed", 500)
sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"cutoff_speed", 500)
sc.setPeristalticPumpDirectionClockwise(sc.DEV_PERISTALTIC_WATER_PUMP)

sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"speed", 3000)
sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"start_speed", 500)
sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"cutoff_speed", 500)
sc.setPeristalticPumpDirectionClockwise(sc.DEV_PERISTALTIC_DETERGENT_PUMP)



print "OK"



