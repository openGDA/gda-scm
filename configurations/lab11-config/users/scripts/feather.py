# start in centre of top left hole
# you cannot use the fast shutter, as beam is required to find the barbs anyway
from gda.data.metadata import GDAMetadataProvider
import math

# Setting up the grid to be scanned
xdistance=9
xnum=10
ydistance=9
ynum=10
holediameter=3.2
expectedbarbs=6

# zero is the centre of the hole
verticalscanpositions=[0, 0.4, 0.8]
scanstepsize=0.05

# beamsize 0.250 x 0.250

try: 
    del xscannable
except:
    pass
try: 
    del yscannable
except:
    pass
try: 
    del transmission
except:
    pass

# set x and y to the appropriate motor names in GDA that the experiment is using
xscannable=x
yscannable=y

# May want to change this for the Ion chamber
transmission=bsdiode

origin = [xscannable.getPosition(), yscannable.getPosition()]

# setup brab fitting
from gdascripts.analysis.datasetprocessor.oned.GaussianPeaksInHole import GaussianPeaksInHole
gpih = GaussianPeaksInHole()
gpih.plotPanel = "Plot 1" # plots to Plot 1
gpih.minarea = 0.05
gpih.maxfwhm = 0.2
gpih.scale = -1.0
gpih.gaussians = 6

scan_processor.processors = [gpih]


# This is the loop that sets up the scan
# Outer loop is y hole pitch
# First inner lopop is x hole pitch
# Second inner loop is find positions of barbs
# Third inner is collect scattering data at positions found by second inner loop

for j in range(ynum):
    for i in range(xnum):
        for k in range(len(verticalscanpositions)):
            pos yscannable origin[1]+j*ydistance xscannable origin[0]+i*xdistance+verticalscanpositions[k]
            scand = holediameter * 0.5 * math.cos(math.pi * verticalscanpositions[k] / holediameter) 
            rscan yscannable -scand scand scanstepsize transmission
            barbpositions=peaksinhole.result.positions
            for b in barbpositions:
                # set metadata (gives a title with hole number, vertical position number in hole, relative y position from centre in hole)
                title = "hole %d scanline %d barbposition %6.5f" % (i+xnum*j+1, k+1, b)
                print title
                GDAMetadataProvider.getInstance().setMetadataValue("title", title)
                scan yscannable (b,) ncddetectors
