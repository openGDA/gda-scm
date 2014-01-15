import scisoftpy as dnp

class PseudoCam():
    def readLastImage(self):
        print "pc readlastimage"
        return dnp.io.load("/scratch/exporttest.png", "png", )["image_01"]._jdataset()