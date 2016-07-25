from gda.analysis import RCPPlotter
from org.eclipse.january.dataset import DatasetFactory

class KeyenceDisplay:
    
    def __init__(self, keyence, panel):
        self.keyence = keyence
        self.panel = panel
        
    def showScreen(self):
        print "getting image"
        self.__showBufferedImage(self.keyence.getScreenShot())
    
    def showMeasurment(self):
        self.__showBufferedImage(self.keyence.getLastMeasurementImage())
    
    def __showBufferedImage(self, bi):
        print "getting data"
        raster=bi.getData()
        width=raster.getWidth()
        height=raster.getHeight()
        ds=DatasetFactory.zeros(height,width)
        for x in range(width):
            for y in range(height):
                try:
                    ds.set(raster.getSampleDouble(x,y,0), y, x)
                except:
                    print x,y
                    return
        print "sending "
        RCPPlotter.imagePlot(self.panel, ds)
