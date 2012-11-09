import sc
import time
import sys
from sctools import *

#implement sample path device
SAMPLE_DETECTED_MSG="Sample filled"
SAMPLE_NOT_DETECTED_MSG="No sample detected"


def getLostVolumeCompensation(sample_volume, viscosity):
    #TODO
    #detected_volume=float(sc.getDeviceConfigEntry(sc.DEV_SAMPLE_PATH,"Detected Volume"))
    BUBBLE_UNDER_NEEDLE_COMPENSATION = 4
    if (viscosity == VISCOSITY_LOW):
        return sample_volume*0.18 +  BUBBLE_UNDER_NEEDLE_COMPENSATION
    if (viscosity == VISCOSITY_MEDIUM):
        return sample_volume*0.22 +  BUBBLE_UNDER_NEEDLE_COMPENSATION
    if (viscosity == VISCOSITY_HIGH):
        return sample_volume*0.26 +  BUBBLE_UNDER_NEEDLE_COMPENSATION
    raise "Bad viscosity value"

def getTempExchangeTime(seu_temp, scu_temp):
    dif_temp=seu_temp-scu_temp
    if (dif_temp<-2):
        t_comp_dif= (-2-dif_temp)*20/36  #0-20sec
    if (dif_temp>=-2) and (dif_temp<=20):
        t_comp_dif=  0
    if (dif_temp>20):
        t_comp_dif= (dif_temp-20)*20/36  #0-20 sec

    if (seu_temp<20):
        t_comp_RT= (20-seu_temp)*10/18  #0-10sec
    if (seu_temp>=20) and (seu_temp<=25):
        t_comp_RT=  0
    if (seu_temp>25):
        t_comp_RT= (seu_temp-25)*10/35  #0-10 sec

    return t_comp_dif+t_comp_RT

def getCapFillSpeed(seu_temp, scu_temp):
    dif_temp=seu_temp-scu_temp
    if (dif_temp<-2):
        speed_comp_dif =(-2-dif_temp)*2/36     #0-2ul
    if (dif_temp>=-2) and (dif_temp<=20):
        speed_comp_dif= 0
    if (dif_temp>20):
        speed_comp_dif =(dif_temp-20)*2/36     #0-2ul

    if (seu_temp<20):
        speed_comp_RT = (20-seu_temp)*1/18     #0-1ul
    if (seu_temp>=20) and (seu_temp<=25):
        speed_comp_RT = 0
    if (seu_temp>25):
        speed_comp_RT = (seu_temp-25)*1/35     #0-1ul

    return 5-speed_comp_dif-speed_comp_RT

if (__name__.find('main')>=0):  # IF THIS IS THE MAIN MODULE
    sc.requestRemoteControl()
    try:
        remote=(sys.argv[1].lower()=="true")
        plate=int(sys.argv[2])
        row=int(sys.argv[3])
        col=int(sys.argv[4])
        sample_volume=float(sys.argv[5])
        speed=float(sys.argv[6])
        viscosity=float(sys.argv[7])
        read_spectro=(str((sys.argv[8])).lower()=="true")

        if (speed<=0):
            speed=getDefaultSpeed(viscosity)

        default_speed_z=float(sc.getDeviceConfigEntry(sc.DEV_MOTOR_Z,"Default Speed"))
        dead_volume=float(sc.getDeviceConfigEntry(sc.DEV_SAMPLE_PATH,"Dead Volume"))
        detected_volume=float(sc.getDeviceConfigEntry(sc.DEV_SAMPLE_PATH,"Detected Volume"))
        sample_volume=sample_volume + getLostVolumeCompensation(sample_volume,viscosity)
        temp_exchange_offset=float(sc.getDeviceConfigEntry(sc.DEV_SAMPLE_PATH,"Temp Exch Offset"))

        temp_exchange_pos=detected_volume-temp_exchange_offset   #left end of view volume (found by calibration)-capilary_temp_exchange_position_offset_value
        temp_exchange_push=temp_exchange_pos-sample_volume
        if read_spectro:
            spectro_pos=float(sc.getDeviceConfigEntry(sc.DEV_SAMPLE_PATH,"Spectro Position"))
            spectro_push=spectro_pos-sample_volume
            temp_exchange_push=temp_exchange_pos-spectro_pos
            sc.storeSpectrometerDarkReadout()
            sc.setSpectrometerStrobeEnabled(True)

        final_push=dead_volume-temp_exchange_pos

        try:
            seu_temp=sc.getExposureTemperature()
            scu_temp=sc.getStorageTemperature()
            temp_exchange_time=getTempExchangeTime(seu_temp, scu_temp)
            cap_fill_speed=min(getCapFillSpeed(seu_temp, scu_temp),speed)
            #Compensating the drift in position for final push due to temperature difference
            if (temp_exchange_time>0) and (seu_temp>25.0):
                temp_dif=seu_temp-25.0 # Worst case, just after
                speed_temp_dif=0.5 # ul/s/degree
                final_push=final_push + (temp_dif*temp_exchange_time*speed_temp_dif)
        except:
            temp_exchange_time=0
            cap_fill_speed=speed
            sc.log("Error reading temperature - no temperature exchange delay", sc.WARNING)

        if (sample_volume>temp_exchange_pos):
            raise "Demanded Sample Volume is too big - install the needle tube for big volumes and execute a 'Calibration' (in tab 'Advanced')"

        sc.stopFluidDetection()
        sc.movePlateDefault()
        initializeSyringeForTransfer()

        moveBottomWellWithVolumeDetection(plate,row, col, sample_volume)

        sc.pushSyringe(sample_volume, speed)
        sc.movePlateWell(plate, row, col, sc.POSITION_TOP)
        sc.setMotorZSpeed(default_speed_z)

        if read_spectro:
            timeout=10+int(spectro_push/speed)
            sc.pushSyringeAsync(spectro_push,speed)
            sc.movePlateDefault()
            sc.waitDeviceReady(sc.DEV_SYRINGE,timeout)            
            sc.storeSpectrometerReadout()
            sc.pushSyringe(temp_exchange_push,speed)
        else:
            timeout=10+int(temp_exchange_push/speed)
            sc.pushSyringeAsync(temp_exchange_push,speed)
            sc.movePlateDefault()
            sc.waitDeviceReady(sc.DEV_SYRINGE,timeout)

        time.sleep(temp_exchange_time)
        sc.startFluidDetection()
        sc.pushSyringe(final_push,cap_fill_speed)
        if (sc.isFluidDetectionActive()==False) or (sc.isTaskInterruptedByLiquidDetection()==True):
            ret=SAMPLE_DETECTED_MSG
        else:
            ret=SAMPLE_NOT_DETECTED_MSG
        print ret

    finally:
        try:
            if read_spectro:
                sc.setSpectrometerStrobeEnabled(False)
        except:
            pass
        sc.releaseRemoteControl()


