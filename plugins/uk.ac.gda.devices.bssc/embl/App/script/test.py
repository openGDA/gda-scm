import sc
import time
import sys
import tools






#Over night test!
#
#sc.requestRemoteControl()
#try:
#    i = 0
#    while (i < 500):
#        i += 1
#        sc.setSyringeValvePos("input")
#        sc.setSyringeSpeed(400)                       #80% of max speed
#        sc.moveSyringe(300)                           #syringe config: no microstep!
#        sc.movePlateWell(1, 9, sc.POSITION_MIDDLE)
#        sc.setMotorZSpeed(15)
#        sc.movePlateWell(1, 9, sc.POSITION_BOTTOM)
#        #sample_volume=sample_volume+2.5              #2.5ul correction offset (air bubble under needle)
#        sc.pushSyringe(10, 10)
#        sc.movePlateWell(1, 9, sc.POSITION_TOP)
#        default_speed_z=float(sc.getDeviceConfigEntry(sc.DEV_MOTOR_Z,"Default Speed"))
#        sc.setMotorZSpeed(default_speed_z)
#        sc.startFluidDetection()
#        dead_volume=float(sc.getSyringeConfigEntry("Dead Volume"))
#        dead_volume=dead_volume-10
#        sc.pushSyringeAsync(dead_volume,10)
#        sc.movePlateDefault()
#        change_speed_time=(dead_volume-50)/10     #50ul before reaching end of cap: change speed
#        if (change_speed_time>0):
#            time.sleep(change_speed_time)
#        sc.setSyringeSpeed(5.0)                       #5ul/s cap filling speed
#        sc.waitDeviceReady(sc.DEV_SYRINGE,30)
#        liquid_pos=sc.getCurrentLiquidPosition();
#        if (liquid_pos is None) or (len(liquid_pos)>0):    #Disabled or detected
#            ret="Sample filled"
#        else:
#            ret="No sample detected"
#
#    #recuperate
#        sc.setSyringeValvePos("input")
#        sc.movePlateWell(1, 9,sc.POSITION_MIDDLE)
#        sc.setSyringeSpeed(10)
#        sc.moveSyringe(290)   #syringe config: no microstep!
#        time.sleep(0.1)
#        sc.setSyringeSpeed(800)   #80% of max speed
#        sc.moveSyringe(0)
#        time.sleep(0.1)
#        sc.movePlateDefault()
#
#    #clean
#        sc.setOverflowVenturiStateOn()
#        sc.setPeristalticPumpDirectionClockwise(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
#        sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"speed", 3000)
#        sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
#        sc.setMotorZSpeed(15)
#        sc.movePlateWashWell()
#        sc.setSyringeValvePos("bypass")
#        sc.setCleanVenturiStateOn()
#        time.sleep(3)
#        default_speed_z=float(sc.getDeviceConfigEntry(sc.DEV_MOTOR_Z,"Default Speed"))
#        sc.setMotorZSpeed(default_speed_z)
#        sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
#
#        sc.movePlateRinseWell(sc.POSITION_TOP)
#        sc.setCleanVenturiStateOff()
#        sc.setOverflowVenturiStateOn()
#        sc.setPeristalticPumpDirectionClockwise(sc.DEV_PERISTALTIC_WATER_PUMP)
#        sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"speed", 3000)
#        sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_WATER_PUMP)
#        sc.setMotorZSpeed(15)
#        sc.movePlateRinseWell()
#        sc.setSyringeValvePos("bypass")
#        sc.setCleanVenturiStateOn()
#        time.sleep(3)
#        default_speed_z=float(sc.getDeviceConfigEntry(sc.DEV_MOTOR_Z,"Default Speed"))
#        sc.setMotorZSpeed(default_speed_z)
#        sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_WATER_PUMP)
#
#        sc.movePlateDryWell()
#        sc.setOverflowVenturiStateOff()
#        sc.setCleanVenturiStateOn()
#        sc.setDryValveStateOn()
#        time.sleep(25)
#        sc.setDryValveStateOff()
#        sc.setCleanVenturiStateOff()
#        sc.setSyringeValvePos("input")
#        sc.setSyringeSpeed(400)   #80% of max speed
#        sc.moveSyringe(300) #syringe config: no microstep!
#        sc.movePlateDefault()
#Over night test END


#Tecan dev test

sc.requestRemoteControl()
try:
    i = 0
    while (i < 1000):
        i += 1
        sc.setSyringeValvePos("output")
        sc.setSyringeSpeed(100)
        sc.moveSyringe(600)
        time.sleep(0.2)
        sc.moveSyringe(1000)
        sc.setSyringeValvePos("bypass")
        time.sleep(0.2)
        sc.setPeristalticPumpDirectionCounterClockwise(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
        sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"speed", 2000)
        sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
        time.sleep(0.5)
        sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
        time.sleep(0.2)
        sc.setPeristalticPumpDirectionCounterClockwise(sc.DEV_PERISTALTIC_WATER_PUMP)
        sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"speed", 2000)
        sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_WATER_PUMP)
        time.sleep(0.5)
        sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_WATER_PUMP)
        time.sleep(0.2)

#Tecan dev test END

finally:
    sc.releaseRemoteControl()




