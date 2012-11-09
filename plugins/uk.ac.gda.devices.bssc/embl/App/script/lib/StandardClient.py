"""
  Project: JLib

  Date       Author    Changes
  01.07.09   Gobbo     Created

  Copyright 2009 by European Molecular Biology Laboratory - Grenoble
"""

import time
import socket
import thread
import sys
import threading

class TimeoutError(Exception):
    pass
class ProtocolError(Exception):
    pass
class SocketError(Exception):
    pass

STX=chr(2)
ETX=chr(3)
MAX_SIZE_STREAM_MSG=500000

class PROTOCOL:
    DATAGRAM=1
    STREAM=2


class StandardClient:
    def __init__(self,server_ip,server_port,protocol,timeout,retries):
        self.server_ip=server_ip
        self.server_port=server_port
        self.timeout=timeout
        self.default_timeout=timeout
        self.retries =retries
        self.protocol=protocol
        self.thread_error=None
        self.lock = threading.Lock()

        self.__msg_index__=-1
        self.__sock__=None
        self.__CONSTANT_LOCAL_PORT__=1

    def __createSocket__(self):
        if self.protocol==PROTOCOL.DATAGRAM:
            self.__sock__=socket.socket(socket.AF_INET, socket.SOCK_DGRAM)#, socket.IPPROTO_UDP)
            #self.__sock__.setblocking(1)
            self.__sock__.settimeout(self.timeout)
        else:
            self.__sock__=socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def __closeSocket__(self):
        try:
            self.__sock__.close()
        except:
            pass
        self.__sock__=None
        self.received_msg=None

    def __connect__(self):
        if self.protocol==PROTOCOL.DATAGRAM:
            return
        if self.__sock__==None: self.__createSocket__()
        self.__sock__.connect((self.server_ip, self.server_port))
        self.thread_error=None
        self.received_msg=None
        thread.start_new_thread(self.recv_thread,())

    def __isconnected__(self):
        if self.protocol==PROTOCOL.DATAGRAM:
            return 0
        if self.__sock__ is None:
            return 0
        try:
            p=self.__sock__.getpeername()
        except:
            return 0
        return 1

    def __sendReceiveDatagramSingle__(self,cmd):
        try:
            if self.__CONSTANT_LOCAL_PORT__==0 or self.__sock__==None: self.__createSocket__()
            msg_number= "%04d " % self.__msg_index__
            msg=msg_number+cmd
            try:
                self.__sock__.sendto(msg,(self.server_ip, self.server_port))
            except:
                raise SocketError,"Socket error:" + str(sys.exc_info()[1])
            received=0
            while received==0:
                try:
                    ret=self.__sock__.recv(4096)
                except socket.timeout:
                    raise TimeoutError,"Timeout error:" + str(sys.exc_info()[1])
                except:
                    raise SocketError,"Socket error:" + str(sys.exc_info()[1])
                if ret[0:5] == msg_number:
                    received=1;
            ret=ret[5:]
        except SocketError:
            self.__closeSocket__()
            raise
        except:
            if  self.__CONSTANT_LOCAL_PORT__==0: self.__closeSocket__()
            raise
        if  self.__CONSTANT_LOCAL_PORT__==0: self.__closeSocket__()
        return ret

    def __sendReceiveDatagram__(self,cmd,timeout=-1):
        self.__msg_index__=self.__msg_index__+1
        if self.__msg_index__ >= 10000:self.__msg_index__=1
        for i in range (0, self.retries):
            try:
              ret=self.__sendReceiveDatagramSingle__(cmd);
              return ret
            except TimeoutError:
                if (i>= self.retries-1): raise
            except ProtocolError:
                if (i>= self.retries-1): raise
            except SocketError:
                if (i>= self.retries-1): raise
            except:
                raise

    def setTimeout (self,timeout):
        self.timeout=timeout
        if self.protocol==PROTOCOL.DATAGRAM:
            if self.__sock__ != None:
                self.__sock__.settimeout(self.timeout)
               # self.__sock__.setblocking(1)

    def restoreTimeout (self):
        self.setTimeout(self.default_timeout)


    def dispose(self):
        if self.protocol==PROTOCOL.DATAGRAM:
            if  self.__CONSTANT_LOCAL_PORT__:
                self.__closeSocket__()
            else:
                pass
        else:
            self.__closeSocket__()


    def onMessageReceived(self,msg):
        self.received_msg=msg

    def recv_thread(self):        
        try:
            buffer=""
            mReceivedSTX=0
            while 1:
                ret=self.__sock__.recv(4096)
                #print "RX:"+ ret
                if self.__isconnected__()==0:
                    return
                for b in ret:
                    if (b==STX):
                        buffer="";
                        mReceivedSTX=1
                    elif (b==ETX):
                        if mReceivedSTX==1:
                            self.onMessageReceived(buffer)

                            mReceivedSTX=0
                            buffer=""
                    else:
                        if mReceivedSTX==1:
                            buffer=buffer+b;

                if (len(buffer)>MAX_SIZE_STREAM_MSG):
                    mReceivedSTX=0;
                    buffer="";
        except:
            self.thread_error=str(sys.exc_info()[1])
            #print "Thread error" + self.thread_error
        #print "Quitting thread"


    def __sendStream__(self,cmd):
        if self.__isconnected__()==0:
            self.__connect__()

        try:
            pack=STX+cmd+ETX
            #print "TX:"+ pack
            self.__sock__.send(pack)
        except:
            raise SocketError,"Socket error:" + str(sys.exc_info()[1])

    def __sendReceiveStream__(self,cmd):

        self.thread_error=None
        self.received_msg=None
        self.__sendStream__(cmd)
        
        start=time.clock()

        while self.received_msg is None:
            if (time.clock() - start) > self.timeout:
                raise TimeoutError()
            if not self.thread_error is None:
                raise SocketError,"Socket error:" + str(self.thread_error)
            time.sleep(0.01)
        return self.received_msg


    def sendReceive(self,cmd, timeout=-1):
        try:
            self.lock.acquire()
            if ((timeout is None) or (timeout >= 0)):
                self.setTimeout(timeout)
            if self.protocol==PROTOCOL.DATAGRAM:
                return self.__sendReceiveDatagram__(cmd)
            else:
                return self.__sendReceiveStream__(cmd)
        finally:
            try:
                if ((timeout is None) or (timeout >= 0)):
                    self.restoreTimeout()
            finally:
                self.lock.release()


    def send(self,cmd):
        if self.protocol==PROTOCOL.DATAGRAM:
            raise ProtocolError,"Protocol error: send command not support in datagram clients"
        else:
            return self.__sendStream__(cmd)

