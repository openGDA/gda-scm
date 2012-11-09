print "===================================================================";
print "Performing beamline specific initialisation code (lpd).";

from femClient.LpdModule import LpdModule
lpdModule1 = LpdModule(("localhost", 20300, 5000))
lpdModule2 = LpdModule(("localhost", 20301, 5000))
lpdModule3 = LpdModule(("localhost", 20302, 5000))
lpdModule4 = LpdModule(("localhost", 20303, 5000))

print "===================================================================";
