import sys
import time
import sc


sc.RequestRemoteControl()

try:
    #Parameters
    par=None
    if len(sys.argv)>1:
        par=int(sys.argv[1])
        sc.showMessageBox("Par:"+sys.argv[1])
    
    #Motor status
    status=sc.getXMotorStatus()
    sc.showMessageBox(str(status))
    if int(status["error_mask"])!=0:
        raise "Motor in error"
    if status["open_loop"]=="true":
        raise "Motor control disabled"
    if status["homing_ok"]=="false":
        raise "Motor not referenced"
    if status["lim_sup"]=="true" and status["lim_inf"]=="true":
        raise "Motor movements are disabled"

    sc.setMotorXSpeed(500)
    pos=sc.getXMotorPos()
    sc.showMessageBox("Current pos: " + str(pos))
    sc.moveXMotor(pos+1000)
    sc.stopXMotor()
    pos=sc.getXMotorPos()
    sc.showMessageBox("Current pos: " + str(pos))
    sc.setMotorXSpeed(sc.getMotorXConfigEntry("Default Speed"))
    #How to start asyn moves    
    sc.moveXMotor(pos+1000,0)
    sc.moveYMotor(1000,0)
    #How to synchronize async moves
    while(sc.isMotorXMoving()):
        pass
    while(sc.isMotorYMoving()):
        pass

    
    #time.sleep(0.1)
    sc.StopXMotor()
    pos=sc.GetXMotorPos()
    sc.MsgBox("Current pos: " + str(pos))
finally:
    sc.ReleaseRemoteControl()


