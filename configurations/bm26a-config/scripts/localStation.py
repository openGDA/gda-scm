print "Initialization Started";

from exafs.bm26aDetectorPreparer import BM26aDetectorPreparer
from exafs.bm26aSamplePreparer import BM26aSamplePreparer
from exafs.bm26aOutputPreparer import BM26aOutputPreparer
from exafsscripts.exafs.xas_scan import XasScan
from gda.factory import Finder
from gda.configuration.properties import LocalProperties
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.scan import ScanBase#this is required for skip current repetition to work BLXVIIIB-99
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from exafs.xspressConfig import XspressConfig
from gdascripts.metadata.metadata_commands import meta_add,meta_ll,meta_ls,meta_rm, meta_clear_alldynamical

XASLoggingScriptController = Finder.getInstance().find("XASLoggingScriptController")
commandQueueProcessor = Finder.getInstance().find("commandQueueProcessor")
ExafsScriptObserver = Finder.getInstance().find("ExafsScriptObserver")

xspressConfig = XspressConfig(xspress1system, ExafsScriptObserver)
print "configured xspress"

datawriterconfig = Finder.getInstance().find("datawriterconfig")
original_header = datawriterconfig.getHeader()[:]




detectorPreparer = BM26aDetectorPreparer()
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
