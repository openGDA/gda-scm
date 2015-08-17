
from gda.observable import IObserver

class DetGuard(IObserver):
    def __init__(self, detsys, *shutters):
        self.detsys = detsys
        self.limits = {}
        self.checks = {"peak" : (lambda x: x.maxCounts/x.countingTime),
                  "peakcounts" : (lambda x: x.maxCounts),
                  "sum" : (lambda x: x.integratedCounts/x.countingTime) }
        self.severities = {"hard" : self.hardAction, "soft" : self.softAction }
        self.shutters = shutters
        self.detsys.addIObserver(self)
        
    def update(self, observed, message):
        try:
            for rate in message:
                detname = rate.detName
                for type in self.checks.keys():
                    for severity in self.severities.keys():
                        if detname+type+severity in self.limits:
                            if self.checks[type](rate) > self.limits[detname+type+severity]:
                                self.severities[severity](detname)
        except:
            pass
        
    def softAction(self, message):
        print "INFO: detector %s is operating close to the maximum allowed count rate" % message
        
    def hardAction(self, message):
        print "ERROR: count rate on detector %s intolerable, closing shutter" % message
        for shutter in self.shutters:
            shutter("Close")

detguard=DetGuard(ncddetectors, eh_shutter, det_shutter)

detguard.limits["Rapid2Dpeaksoft"]=0.75*1000000
detguard.limits["Rapid2Dpeakhard"]=1000000
detguard.limits["Rapid2Dsumsoft"]=0.75*3*1000000
detguard.limits["Rapid2Dsumhard"]=3*1000000

detguard.limits["Hotwaxspeaksoft"]=0.75*1000000
detguard.limits["Hotwaxspeakhard"]=1000000

detguard.limits["Hotsaxspeaksoft"]=0.75*1000000
detguard.limits["Hotsaxspeakhard"]=1000000

detguard.limits["Pilatus2Mpeakcountssoft"]=0.75*1000000
detguard.limits["Pilatus2Mpeakcountshard"]=1000000

# 65536
detguard.limits["Marpeakcountssoft"]=65500
