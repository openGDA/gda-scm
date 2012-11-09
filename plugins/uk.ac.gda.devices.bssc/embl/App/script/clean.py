import sc
import time
import sys

sc.stopFluidDetection()


import sc
import time
import sys


VISCOSITY_LOW=1
VISCOSITY_MEDIUM=2
VISCOSITY_HIGH=3

MINIMUM_DRYING_TIME=15

def getDefaultCleanTimes(viscosity):
    if (viscosity == VISCOSITY_LOW):
        return (3,3,30)                     #test with (3,3,5) for example - bad drying mesage
    if (viscosity == VISCOSITY_MEDIUM):
        return (5,5,30)
    if (viscosity == VISCOSITY_HIGH):
        return (10,10,30)
    raise "Bad viscosity value"

def clean(wash_time,rinse_time,dry_time,yellow_sample,viscosity):
    passes=1
    if yellow_sample==True:
        passes=3

    sc.stopFluidDetection()

    if (wash_time<0):
      (wash_time,rinse_time,dry_time)=getDefaultCleanTimes(viscosity)

    if dry_time == 0:
        raise "Cleaning without drying not permitted. Cleaning routine stopped."
    if rinse_time == 0 and wash_time > 0:
        raise "Cleaning without rinsing not permitted. Cleaning routine stopped."

    sc.movePlateDefault()
    sc.setOverflowVenturiStateOn()
    sc.moveCleaningStationWash()
    sc.setSyringeValvePos("output")
    sc.setSyringeSpeed(400) #80% of max speed
    sc.moveSyringe (0)
    for i in range(passes):
        if wash_time > 0:
           
            sc.setPeristalticPumpDirectionClockwise(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"speed", 3000)
            sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            time.sleep(1.6)#outside cleaning
            sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            sc.setSyringeValvePos("bypass")
            sc.setCleanVenturiStateOn()
            sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"speed", 1500)
            sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            time.sleep(wash_time)#inside cleaning
            sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            #sc.setPeristalticPumpDirectionCounterClockwise(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            #sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_DETERGENT_PUMP,"speed", 5800)
            #sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            #time.sleep(0.2)#time needed to empty the cleaning well
            #sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
            sc.setCleanVenturiStateOff()
            sc.setSyringeValvePos("output")
            
        if rinse_time > 0:
            sc.moveCleaningStationWash()
            sc.setOverflowVenturiStateOn()
            sc.setPeristalticPumpDirectionClockwise(sc.DEV_PERISTALTIC_WATER_PUMP)
            sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"speed", 3000)
            sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_WATER_PUMP)
            time.sleep(1.6)#outside rinsing
            sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_WATER_PUMP)
            sc.setSyringeValvePos("bypass")
            sc.setCleanVenturiStateOn()
            sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"speed", 1800)
            sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_WATER_PUMP)
            time.sleep(rinse_time)#inside rinsing
            sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_WATER_PUMP)
            #sc.setPeristalticPumpDirectionCounterClockwise(sc.DEV_PERISTALTIC_WATER_PUMP)
            #sc.setPeristalticPumpPar(sc.DEV_PERISTALTIC_WATER_PUMP,"speed", 5800)
            #sc.setPeristalticPumpStateOn(sc.DEV_PERISTALTIC_WATER_PUMP)
            #time.sleep(0.2)#time needed to empty the cleaning well
            #sc.setPeristalticPumpStateOff(sc.DEV_PERISTALTIC_WATER_PUMP)

        sc.moveCleaningStationDry()
        sc.setOverflowVenturiStateOff()
        sc.setSyringeValvePos("bypass")
        sc.setCleanVenturiStateOn()
        sc.setDryValveStateOn()
        
        #drying detection: at least 10 sec. max 20 sec, can be stopped by detection in between those values.
        # for statistics: log real drying detection time (f.e.: 5.4 sec,....)
        #time.sleep(dry_time)
        detected_dry_time=dry_time        
        sc.startDryingDetection()
        start=time.clock()
        while True:
            timespan=time.clock() - start
            if timespan>=dry_time:
                break
            if sc.isDryingComplete():
                if detected_dry_time==dry_time:
                    detected_dry_time=timespan
                if timespan>=MINIMUM_DRYING_TIME:
                    break
            time.sleep(0.01)

        sc.setDryValveStateOff()
        sc.setCleanVenturiStateOff()
        sc.setSyringeValvePos("input")
        sc.setSyringeSpeed(400)   #80% of max speed
        sc.moveSyringe(100) #syringe config: no microstep!
        sc.parkCleaningStation()
        sc.stopDryingDetection()
        return detected_dry_time

if (__name__.find('main')>=0):  # IF THIS IS THE MAIN MODULE
    sc.requestRemoteControl()

    try:
        # read parameters
        remote=(sys.argv[1].lower()=="true")
        wash_time=float(sys.argv[2])
        rinse_time=float(sys.argv[3])
        dry_time=float(sys.argv[4])
        yellow_sample=(sys.argv[5].lower()=="true")
        viscosity=int(sys.argv[6])

        actual_dry_time= clean(wash_time,rinse_time,dry_time,yellow_sample,viscosity)

        ret="System cleaned and ready for next sample."
        if (actual_dry_time!=dry_time):
            ret+=("\nDetected dried after %1.2fs." % actual_dry_time)
        if (actual_dry_time==dry_time):
            ret+=("\nEnd of drying was not confirmed by image prozessing.")
        print ret

    finally:
        sc.releaseRemoteControl()