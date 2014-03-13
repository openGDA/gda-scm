import scisoftpy as dnp
import math

class DummyCam():
    def __init__(self, x, y, pos):
        self.stage = pos
        self.height = y
        self.width = x
        self.array = [[0 for i in xrange(x)] for j in xrange(y)]
    
    def value(self, x, y):
        cross = self.crossHair(x,y)
        if cross:
            return int(max(0, cross))
        else:
            return int(max(0, self.circle(x,y)))
        
    def circle(self, x, y):
        return 100 * math.sin(0.04 * math.sqrt(x*x + y*y)) + 100
    
    def crossHair(self, x, y):
        w = 20
        if abs(x) < w or abs(y) < w:
            pi = 3.14159
            if abs(x) < w:
                return  (100 * math.cos(pi * 0.5/w * x))
            else:
                return (100 * math.cos(pi * 0.5/w * y))
    
    def fill(self):
        for i in xrange(self.width):
            for j in xrange(self.height):
                self.array[j][i] = self.value(i-self.x,j-self.y)

    def readLastImage(self):
        print "pc readlastimage"
        self.x = self.stage.x.getPosition()
        self.y = self.stage.y.getPosition()
        self.fill()
        return dnp.array(self.array)._jdataset()
    