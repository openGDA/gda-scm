print "Initialization Started";

from uk.ac.gda.server.exafs.scan import EnergyScan 
from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import NexusDataWriter

LocalProperties.set(NexusDataWriter.GDA_NEXUS_METADATAPROVIDER_NAME,"metashop")

xas = energyScan
xanes = xas

vararg_alias("xas")
vararg_alias("xanes")

# To make scans return to the start after being run
# Should be for commissioning only.
scansReturnToOriginalPositions = 0

from gdascripts.pd.time_pds import showtimeClass
showtime = showtimeClass("showtime")
showtime.setLevel(4) # so it is operated before anything else in a scan

print "Initialization Complete";
