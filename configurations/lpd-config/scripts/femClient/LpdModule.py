'''
Created on 22 Mar 2011

@author: grm
'''

import sys
import math
import random
import time
from gda.device.detector import ILpdDetector
from gda.device import Detector
    
class LpdModule(ILpdDetector):
    '''
    classdocs
    '''
    moduleWidth = 256
    moduleHeight = 256
    status = Detector.STANDBY
    femClient = None
    hostAddr = None
    voltage = 0.0
    
    def __init__(self, hostAddr=None, dataSocket=None, timeout=None):
#        self.femClient = FemCLient(hostAddr, timeout)
        status = Detector.IDLE
        self.hostAddr = hostAddr
        print "Initialising FemClient on host ", hostAddr, " using data socket ", dataSocket
    
    def getStatus(self):
#        self.femClient.getStatus();
        return self.status
    
    def collectData(self):
#        self.femClient.collectData();
        self.status = Detector.BUSY
    
    def setDACVoltage(self, voltage):
#        self.femClient.setVoltage();
        self.voltage = voltage
        print "setting DAC voltage", voltage

    def getDACVoltage(self):
#        self.femClient.getVoltage();
        print "getting DAC voltage", self.voltage
        return self.voltage
    
    def readout(self):
        return self.generateImage(self.moduleWidth, self.moduleHeight)
        
    def stop(self):
#        self.femClient.stop();
        self.status = ILpdDetector.IDLE
    
    def getDataDimensions(self):
        dims = 2*[0]
        dims[0] = self.moduleWidth;
        dims[1] = self.moduleHeight;
        return dims

    def generateImage(self, width, height):
        buffer = width*height*[0]
        bimg_center_x = random.randint(-sys.maxint-1, sys.maxint) % int(width * 0.05)
        if random.randint(-sys.maxint-1, sys.maxint) % 2 == 0:
            bimg_center_x *= -1
        bimg_center_y = random.randint(-sys.maxint-1, sys.maxint) % int(height * 0.05)
        if random.randint(-sys.maxint-1, sys.maxint) % 2 == 0:
            bimg_center_y *= -1
        bimg_x_offset_to_zero = ((width - 1) / 2) + bimg_center_x
        bimg_y_offset_to_zero = ((height - 1) / 2) + bimg_center_y
        limit = max(width, height) / 8
        noise = random.randint(-sys.maxint-1, sys.maxint) % int(limit * 0.2)
        if random.randint(-sys.maxint-1, sys.maxint) % 2 == 0:
            noise *= -1
        limit += noise
        x=0
        y=0
        for i in range (-limit, limit, 1):
            y = i + bimg_y_offset_to_zero
            if y >= 0 and y < height:
                for j in range(-limit, limit, 1):
                    x = j + bimg_x_offset_to_zero
                    if x >= 0 and x < width:
                        value = int(math.sqrt((i * i + j * j)))
                        if value < limit:
                            buffer[(y * width + x)] = limit - value
                        else:
                            buffer[(y * width + x)] = 0
        return buffer
