import sc
import time
import sys
import thread

thread_quit=False
thread_error=None
def initSamplePath(*args):
    global thread_quit,thread_error
    try:
        sc.restoreLightLevel()
        sc.moveMotorHome(sc.DEV_FOCUS,False)
        while(sc.isMotorHoming(sc.DEV_FOCUS)):
            time.sleep(0.1)
        sc.restoreFocusPosition(False)
        
    except:
        thread_error=str(sys.exc_info()[1])
    thread_quit=True


sc.log("Starting Cold Start", sc.WARNING)
args=()
thread.start_new_thread(initSamplePath,args)

sc.stopFluidDetection()
"""
sc.writeTwincat("Twincat PLC",0xF021,0x140,0)    #Top piston in GUI
time.sleep(0.6)
sc.writeTwincat("Twincat PLC",0xF021,0x142,1)    #Rotation H2O pos GUI
sc.writeTwincat("Twincat PLC",0xF021,0x143,0)    #Rotation Dry pos GUI
time.sleep(0.3)
sc.writeTwincat("Twincat PLC",0xF021,0x141,0)    #Bottom piston in GUI
sc.writeTwincat("Twincat PLC",0xF021,0x160,1)    #Service station reset
time.sleep(0.8)
sc.writeTwincat("Twincat PLC",0xF021,0x160,0)    #Service station reset
"""
sc.parkCleaningStation()
#If cover is closed
if sc.isCoverOpen() == False:
    sc.moveMotorHome(sc.DEV_MOTOR_X)
    sc.moveMotorHome(sc.DEV_MOTOR_Y)
    sc.moveMotorHome(sc.DEV_MOTOR_Z)
sc.movePlateDefault()
sc.initCleaningValve()
sc.initSyringe()
sc.setSyringeValvePos("input")
sc.setSyringePar("backlash",0) #default=20
sc.setOverflowVenturiStateOn()
sc.initPeristalticPump(sc.DEV_PERISTALTIC_WATER_PUMP)
sc.initPeristalticPump(sc.DEV_PERISTALTIC_DETERGENT_PUMP)
time.sleep(3)
sc.setOverflowVenturiStateOff()
sc.resetCleaningStation();
while(thread_quit==False):
    time.sleep(0.2)

if not thread_error is None:
    raise str(thread_error)
print "OK"

