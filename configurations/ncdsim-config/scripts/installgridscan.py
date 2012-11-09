import gridscan
from gda.device.scannable.scannablegroup import ScannableGroup
gridxy=ScannableGroup()
gridxy.setName("gridxy")
gridxy.setGroupMembers([x,y])
gridxy.configure()
ncdgridscan = gridscan.Grid("Camera View", "Mapping Grid", None, gridxy, ncddetectors)
