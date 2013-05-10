#    localStation.py
#
#    For beamline specific initialisation code.
print "Performing beamline specific initialisation code"

from gdascripts.pd.dummy_pds import DummyPD
from gdascripts.pd.dummy_pds import MultiInputExtraFieldsDummyPD
from gdascripts.pd.dummy_pds import ZeroInputExtraFieldsDummyPD

from gdascripts.pd.time_pds import showtimeClass
from gdascripts.pd.time_pds import showincrementaltimeClass
from gdascripts.pd.time_pds import waittimeClass

print "Creating dummy devices x,y and z"
x=DummyPD("x")
y=DummyPD("y")
z=DummyPD("z")

energy=DummyPD("energy")
energy(12.4)
bkeV=DummyPD("bkeV")
bkeV(12.4)

print "Creating timer devices t, dt, and w"
t = showtimeClass("t") # cannot also be driven.
dt= showincrementaltimeClass("dt")
w = waittimeClass("w")

print "Creating multi input/extra field device, mi, me and mie"
mi=MultiInputExtraFieldsDummyPD('mi',['i1','i2'],[])
me=MultiInputExtraFieldsDummyPD('me',[],['e1','e2'])
mie=MultiInputExtraFieldsDummyPD('mie',['i1'],['e2','e3'])

print "Creating zero input/extra field device, zie"
zie=ZeroInputExtraFieldsDummyPD('zie')

from installStandardScansWithProcessing import *
scan_processor.rootNamespaceDict=globals()

import gridscan
 
print "Create ncdgridscan"
try:
    del(gridxy)
except:
    pass

from ncdutils import DetectorMeta
waxs_distance = DetectorMeta("waxs_distance", ncddetectors, "WAXS", "distance", "m")
saxs_distance = DetectorMeta("saxs_distance", ncddetectors, "SAXS", "distance", "m")
saxs_centre_x = DetectorMeta("saxs_centre_x", ncddetectors, "SAXS", "beam_center_x")
saxs_centre_y = DetectorMeta("saxs_centre_y", ncddetectors, "SAXS", "beam_center_y")

from gda.device.scannable.scannablegroup import ScannableGroup
gridxy=ScannableGroup()
gridxy.setName("gridxy")
gridxy.setGroupMembers([x, y])
gridxy.configure()
# make work without camera
camera=bsdiode
ncdgridscan=gridscan.Grid("Saxs Plot", "Mapping Grid", camera, gridxy, ncddetectors)
ncdgridscan.snap()
