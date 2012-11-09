from gda.exafs.scan import BeanGroup
from gda.exafs.scan import BeanGroups

from BeamlineParameters import JythonNameSpaceMapping

from exafsscripts.exafs.configFluoDetector import configFluoDetector

from gda.configuration.properties import LocalProperties
from gdascripts.messages.handle_messages import simpleLog

def setupBM26a(beanGroup):
 
    # switch off setting unless explicitly set
    LocalProperties.set("gda.scan.useScanPlotSettings", "false")
    LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
   
    controller = None
        
    return controller
