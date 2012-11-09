import sc
import time
import sys
import clean


init_volume=0#300 = 50 ul... detected volume was wrong by 50 ul! check init syringe pos
sample_volume=20
init_speed=12
final_speed=5

first_reading_roi_px=80
second_reading_roi_offset_px=200
default_load_volume_offset_ul=5

timer_check_liquid_stable=3.0
timer_liquid_stop=2.0
liquid_stable_tolerance_px=20

def getBiggestPosition(liquid_pos):
    ret=0
    for pos in liquid_pos:
        int_pos=int(pos)
        if (int_pos>ret):
            ret=int_pos
    return ret

def getLiquidPos():
    for i in range(5):
        liquid_pos=sc.getCurrentLiquidPosition()
        if (liquid_pos != None) and (len(liquid_pos)>0):
            return getBiggestPosition(liquid_pos)
    raise Exception("Liquid not detected")

def getLiquidPosAssertNotMoving():
    pos1=getLiquidPos()
    time.sleep(timer_check_liquid_stable)
    pos2=getLiquidPos()
    if abs(pos1-pos2)>liquid_stable_tolerance_px:
        raise Exception("Liquid is moving: check leaks and SEU temperature")
    return pos2


ret="Success"
reload(clean)
clean.clean(-1,-1,-1,"false",clean.VISCOSITY_LOW)
time.sleep(4)
try:
    sc.requestRemoteControl()
    remote=(sys.argv[1].lower()=="true")
    if (sc.detectCapillary().lower()=="false"):
        raise Exception("Error detecting capillary")

    sc.startStaticFluidDetection()
    if (sc.isFluidDetectionActive()==False):
        raise Exception("Fluid detection disabled")

    sc.setSyringeValvePos("input")
    sc.setSyringeSpeed(400)                       #80% of max speed
    sc.moveSyringe(init_volume)                           #syringe config: no microstep!
    sc.moveCleaningStationWash()
    sc.pushSyringe(sample_volume, init_speed)
    sc.parkCleaningStation()
    syringe_volume=float(sc.getSyringeConfigEntry("Syringe Volume"))
    push_volume=syringe_volume-sample_volume-init_volume
    sc.pushSyringeAsync(push_volume,init_speed)
    finished=False
    while(True):
        liquid_pos=sc.getCurrentLiquidPosition()
        if (liquid_pos != None) and (len(liquid_pos)>0):
            if getBiggestPosition(liquid_pos) > first_reading_roi_px:
                sc.stopSyringe()
                time.sleep(timer_liquid_stop)
                init_syringe_pos=sc.getSyringeVolumePos()
                init_liquid_pos=getLiquidPosAssertNotMoving()
                limit_location=int(sc.getROI()[2])
                sc.pushSyringeAsync(50,final_speed )
                while(True):
                    pos=getLiquidPos()
                    if (pos> (limit_location-second_reading_roi_offset_px)):
                        sc.stopSyringe()
                        time.sleep(timer_liquid_stop)
                        end_syringe_pos=sc.getSyringeVolumePos()
                        end_liquid_pos=getLiquidPosAssertNotMoving()
                        dead_volume=end_syringe_pos+default_load_volume_offset_ul;
                        if (end_syringe_pos<=init_syringe_pos)or(end_liquid_pos<=init_liquid_pos):
                            raise Exception("Invalid values : Volume=" + str(init_syringe_pos)+ "-" + str(end_syringe_pos)+ " Image=" +str(init_liquid_pos)+ "-" + str(end_liquid_pos))
                        image_scale=(end_syringe_pos-init_syringe_pos)/(end_liquid_pos-init_liquid_pos)

                        sc.setSamplePathConfigEntry("Detected Volume",init_syringe_pos)
                        sc.setSamplePathConfigEntry("Dead Volume",dead_volume)
                        sc.setSamplePathConfigEntry("Image Scale",image_scale)
                        finished=True
                        break
                    if sc.isDeviceReady(sc.DEV_SYRINGE):
                        raise Exception("Didn't detect final liquid position")
        if finished:
            break;
        if sc.isDeviceReady(sc.DEV_SYRINGE):
            raise Exception("Didn't detected the liquid")

except:
    ret=str(sys.exc_info()[1])

sc.stopFluidDetection()
clean.clean(-1,-1,-1,"false",clean.VISCOSITY_LOW)

print ret
sc.releaseRemoteControl()
