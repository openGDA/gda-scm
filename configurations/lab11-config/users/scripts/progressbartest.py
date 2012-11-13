from gda.commandqueue import JythonScriptProgressProvider
import math

for i in range(10000):
    f=math.floor(i/100.0)
    print f,i
    JythonScriptProgressProvider.sendProgress( i/100.0, " done %3.1f%% " % (i/100.0))