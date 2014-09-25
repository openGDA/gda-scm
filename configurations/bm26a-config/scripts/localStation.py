print "Initialization Started";

from gda.exafs.scan.preparers import BM26aDetectorPreparer
from gda.exafs.scan.preparers import BM26aSamplePreparer
from gda.exafs.scan.preparers import BM26aOutputPreparer
from exafsscripts.exafs.xas_scan import XasScan
from gda.factory import Finder
from gda.configuration.properties import LocalProperties
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.scan import ScanBase#this is required for skip current repetition to work BLXVIIIB-99
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.device.detector.xspress import XspressDetectorConfiguration
from gdascripts.metadata.metadata_commands import meta_add,meta_ll,meta_ls,meta_rm, meta_clear_alldynamical

XASLoggingScriptController = Finder.getInstance().find("XASLoggingScriptController")
commandQueueProcessor = Finder.getInstance().find("commandQueueProcessor")
ExafsScriptObserver = Finder.getInstance().find("ExafsScriptObserver")

xspressConfig = XspressDetectorConfiguration(xspress1system, ExafsScriptObserver)
print "configured xspress"

datawriterconfig = Finder.getInstance().find("datawriterconfig")
original_header = datawriterconfig.getHeader()[:]

detectorPreparer = BM26aDetectorPreparer(xspressConfig)
samplePreparer = BM26aSamplePreparer(sampleStage, cryoStage)
outputPreparer = BM26aOutputPreparer(datawriterconfig)
xas = XasScan(detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, bragg1, counterTimer01, False, False, False, False, False)
xanes = xas

alias("xas")
alias("xanes")
alias("xspress")

# To make scans return to the start after being run
# Should be for commissioning only.
scansReturnToOriginalPositions = 0

from gdascripts.pd.time_pds import showtimeClass
showtime = showtimeClass("showtime")
showtime.setLevel(4) # so it is operated before anything else in a scan

print "Initialization Complete";
