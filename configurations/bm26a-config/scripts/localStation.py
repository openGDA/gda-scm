print "Initialization Started";

#from gda.exafs.scan.preparers import BM26aBeamlinePreparer
#from gda.exafs.scan.preparers import BM26aDetectorPreparer
#from gda.exafs.scan.preparers import BM26aSamplePreparer
#from gda.exafs.scan.preparers import BM26aOutputPreparer
from uk.ac.gda.server.exafs.scan import EnergyScan 
#, XasScanFactory
#from gda.factory import Finder
from gda.configuration.properties import LocalProperties
#from gda.jython.scriptcontroller.logging import LoggingScriptController
#from gda.scan import ScanBase #this is required for skip current repetition to work BLXVIIIB-99
from gda.data.scan.datawriter import NexusDataWriter
#from gda.device.detector.xspress import XspressDetectorConfiguration
#from gdascripts.metadata.metadata_commands import meta_add,meta_ll,meta_ls,meta_rm, meta_clear_alldynamical

#XASLoggingScriptController = Finder.getInstance().find("XASLoggingScriptController")
#commandQueueProcessor = Finder.getInstance().find("commandQueueProcessor")
#ExafsScriptObserver = Finder.getInstance().find("ExafsScriptObserver")

#xspressConfig = XspressDetectorConfiguration(xspress1system, ExafsScriptObserver)
#print "configured xspress"

#datawriterconfig = Finder.getInstance().find("datawriterconfig")
#original_header = datawriterconfig.getHeader()[:]
LocalProperties.set(NexusDataWriter.GDA_NEXUS_METADATAPROVIDER_NAME,"metashop")
#metashop = Finder.getInstance().find("metashop")

#detectorPreparer = BM26aDetectorPreparer(bragg1, xspressConfig)
#samplePreparer = BM26aSamplePreparer(sampleStage, cryoStage)
#outputPreparer = BM26aOutputPreparer(datawriterconfig, metashop)

#theFactory = Finder.getInstance().find("XasScanFactory")
# TODO this could all be done in Sping XML
#theFactory = XasScanFactory();
#theFactory.setBeamlinePreparer(BM26aBeamlinePreparer());
#theFactory.setDetectorPreparer(detectorPreparer);
#theFactory.setSamplePreparer(samplePreparer);
#theFactory.setOutputPreparer(outputPreparer);
#theFactory.setCommandQueueProcessor(commandQueueProcessor);
#theFactory.setXASLoggingScriptController(XASLoggingScriptController);
#theFactory.setDatawriterconfig(datawriterconfig);
#theFactory.setEnergyScannable(bragg1);
#theFactory.setMetashop(metashop);
#theFactory.setIncludeSampleNameInNexusName(False);
#theFactory.setOriginal_header(original_header);

#scanFactory = XasScanFactory()
xas = energyScan
xanes = xas

vararg_alias("xas")
vararg_alias("xanes")
#alias("xspress")

# To make scans return to the start after being run
# Should be for commissioning only.
scansReturnToOriginalPositions = 0

from gdascripts.pd.time_pds import showtimeClass
showtime = showtimeClass("showtime")
showtime.setLevel(4) # so it is operated before anything else in a scan

print "Initialization Complete";
