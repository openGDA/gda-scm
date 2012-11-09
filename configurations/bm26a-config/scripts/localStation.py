from org.jscience.physics.quantities import Quantity
from org.jscience.physics.units import Unit
from gda.configuration.properties import LocalProperties
from gda.device.scannable import DummyScannable
from gda.jython import JythonServerFacade

from exafsscripts.exafs import xas_scans
from exafsscripts.exafs.xas_scans import xas
#from exafsscripts.exafs.xas_scans import xanes
from exafsscripts.exafs.xas_scans import estimateXas
#from exafsscripts.exafs.xas_scans import estimateXanes
#from exafsscripts.exafs.xas_scans import xes

#from exafsscripts.vortex import vortexConfig
#from exafsscripts.vortex.vortexConfig import vortex

from exafsscripts.xspress import xspressConfig
from exafsscripts.xspress.xspressConfig import xspress

alias("xas")
#alias("xanes")
#alias("xes")
alias("estimateXas")
#alias("estimateXanes")
#alias("vortex")
alias("xspress")

# To make scans return to the start after being run
# Should be for commissioning only.
scansReturnToOriginalPositions = 1
