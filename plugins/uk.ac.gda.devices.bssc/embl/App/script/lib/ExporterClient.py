"""
  Project: JLib

  Date       Author    Changes
  01.07.09   Gobbo     Created

  Copyright 2009 by European Molecular Biology Laboratory - Grenoble
"""

from StandardClient import *

CMD_SYNC_CALL = "EXEC"
CMD_ASNC_CALL = "ASNC"
CMD_METHOD_LIST = "LIST"
CMD_PROPERTY_READ = "READ";
CMD_PROPERTY_WRITE = "WRTE";
CMD_PROPERTY_LIST = "PLST";
CMD_NAME = "NAME"
RET_ERR = "ERR:"
RET_OK = "RET:"
RET_NULL = "NULL"
EVENT = "EVT:"

PARAMETER_SEPARATOR = "\t";
ARRAY_SEPARATOR = "";  # 0x001F

class ExporterClient(StandardClient):

    def onMessageReceived(self,msg):
        if msg[:4]=="EVT:":
            try:
                evtstr=msg[4:]                
                tokens=evtstr.split(PARAMETER_SEPARATOR)
                self.onEvent(tokens[0],tokens[1],long(tokens[2]))
            except:
                pass
        else:
            StandardClient.onMessageReceived(self,msg)

    def getMethodList(self):
        cmd=CMD_METHOD_LIST
        ret = self.sendReceive(cmd)
        ret = self.__processReturn(ret)
        if ret is None:
            return None
        return  ret.split(PARAMETER_SEPARATOR);

    def getPropertyList(self):
        cmd=CMD_PROPERTY_LIST
        ret = self.sendReceive(cmd)
        ret = self.__processReturn(ret)
        if ret is None:
            return None
        return  ret.split(PARAMETER_SEPARATOR);

    def getServerObjectName(self):
        cmd=CMD_NAME
        ret = self.sendReceive(cmd)
        return self.__processReturn(ret)

    def execute(self,method,pars=None,timeout=-1):
        cmd=CMD_SYNC_CALL + " " + method + " "
        if pars is not None:
            for par in pars:
                cmd += (str(par) + PARAMETER_SEPARATOR)
        ret = self.sendReceive(cmd,timeout)
        return self.__processReturn(ret)

    def __processReturn(self,ret):
        if ret[:4]==RET_ERR:
            raise Exception,ret[4:]
        elif ret==RET_NULL:
            return None
        elif ret[:4]==RET_OK:
            return ret[4:]
        else:
            raise ProtocolError

    def executeAsync(self,method,pars=None):
        cmd=CMD_ASNC_CALL + " " + method + " "
        if pars is not None:
            for par in pars:
                cmd += (str(par) + PARAMETER_SEPARATOR)
        return self.send(cmd)

    def readProperty(self,property,timeout=-1):
        cmd=CMD_PROPERTY_READ + " " + property
        ret = self.sendReceive(cmd,timeout)
        return self.__processReturn(ret)

    def writeProperty(self,property,value,timeout=-1):
        cmd=CMD_PROPERTY_WRITE + " " + property + " " + str(value)
        ret = self.sendReceive(cmd,timeout)
        return self.__processReturn(ret)

    def parseArray(self,value):
        value=str(value)
        if value.startswith(ARRAY_SEPARATOR)==False:
            return None
        if value==ARRAY_SEPARATOR:
            return []
        value=value.lstrip(ARRAY_SEPARATOR).rstrip(ARRAY_SEPARATOR)
        return value.split(ARRAY_SEPARATOR)

    def onEvent(self, name, value, timestamp):
        print "Received Event:" + name + " Value:" + value

