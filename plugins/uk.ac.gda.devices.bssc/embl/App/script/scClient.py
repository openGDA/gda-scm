import time
from ExporterClient import *


SC_PORT=9555

STATE_READY                = "Ready"
STATE_MOVING               = "Moving"
STATE_RUNNING              = "Running"


class scClient(ExporterClient):
    def __init__(self,server_ip,timeout=5,retries=2):
        ExporterClient.__init__(self,server_ip, SC_PORT, PROTOCOL.STREAM, timeout, retries)

    def getState(self):
        return self.execute("getState")

    def getStatus(self):
        return self.execute("getStatus")

    def emergencyStop(self):
        return self.execute("emergencyStop")

    def init(self):
        self.waitReady()
        return self.execute("init")

    def reset(self):
        self.waitReady()
        return self.execute("reset")

    def fill(self,row,col,volume,speed):
        self.waitReady()
        return self.execute("fill",(int(row),int(col),float(volume),float(speed)))

    def recuperate(self,row,col):
        self.waitReady()
        return self.execute("fill",(int(row),int(col)))

    def flow(self,volume,time):
        self.waitReady()
        return self.execute("flow",(float(volume),float(speed)))

    def clean(self,time_wash, time_rinse, time_dry):
        self.waitReady()
        return self.execute("clean",(int(time_wash),int(time_rinse),int(time_dry)))

    def waitReady(self, timeout=60):
        start=time.time()
        while(1):
            state=self.getState()
            if (state==STATE_READY):
                return
            elif (state==STATE_RUNNING):
                pass
            elif (state==STATE_MOVING):
                pass
            else:
                raise "Bad application state: " + state
            if (time.time()-start) > timeout:
                raise "Timeout waiting application ready"


if (__name__.find('main') >= 0):
    sc=scClient("localhost")
    name = sc.getServerObjectName()
    print name
    state = sc.getState()
    print state
    status=sc.getStatus()
    print status
    methods = sc.getMethodList()
    for method in methods:
        print method
    #for i in range(3):
    #    sc.fill(1, 1, 0.1, 0.1)
    sc.executeAsync("clean", (1,2,1))
