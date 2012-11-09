"""
  Project: JLib

  Date       Author    Changes
  01.07.09   Gobbo     Created

  Copyright 2009 by European Molecular Biology Laboratory - Grenoble
"""

from ExporterClient import *

OPTION_TYPE_DEFAULT=-1
OPTION_TYPE_YES_NO=0
OPTION_TYPE_YES_NO_CANCEL=1
OPTION_TYPE_OK_CANCEL=2

RESULT_YES=0
RESULT_NO=1
RESULT_CANCEL=2
RESULT_OK=0
RESULT_CLOSED=-1

FINE="FINE"
INFO="INFO"
WARNING="WARNING"
ERROR="SEVERE"


class ControllerRemoteClient(ExporterClient):

    def log(self,log,level=INFO):
        return self.execute("log",(str(level),str(log)))

    def showMessageBox(self,msg):
        return self.execute("msgbox",(str(msg),))

    def showOptionBox(self,msg,option_type):
        return int(self.execute("optionbox",(str(option_type),str(msg))))

    def showInputBox(self,msg):
        ret = str(self.execute("inputbox",(str(msg),)))
        return ret

    def getWorkingPath(self):
        return self.execute("path")

    def requestRemoteControl(self):
        return self.execute("remctr")

    def releaseRemoteControl(self):
        try:
            return self.execute("release")
        except:
            pass