#requires module load numpy
fileIn = "/dls/b21/data/2014/mx9007-20/processing/results_b21-7475_detector_010314_194552.nxs"
fileIn = "/dls/b21/data/2013/cm5947-3/processing/results_b21-2672_detector_040713_102411.nxs"

if fileIn.startswith("/dls/b21"):
	detectorName = "detector"
elif fileIn.startswith("/dls/i22"):
	detectorName = "Pilatus2M"
dataPath = "/entry1/"+detectorName+"_result/data"
dataErrorsPath = "/entry1/"+detectorName+"_result/errors"
qPath = "/entry1/"+detectorName+"_result/q"
qErrorsPath = qPath+"_errors"
subtractedFileSuffix = "_sub.dat"
backgroundPath = "/entry1/"+detectorName+"_processing/BackgroundSubtraction/background"
normalizationPath = "/entry1/"+detectorName+"_processing/Normalisation/data"
normalizationErrorsPath = "/entry1/"+detectorName+"_processing/Normalisation/errors"

import numpy
import h5py
f=h5py.File(fileIn)
data = f[dataPath][0][0]
dataErrors=f[dataErrorsPath][0][0] #TODO check whether path exists
#if errors do not exist, put in 0.5 * data as the error
backgroundData = f[backgroundPath][0][0] #TODO check whether path exists
normalizationData = f[normalizationPath][0] #multidimensional array TODO check whether path exists
normalizationErrors = f[normalizationErrorsPath][0] #TODO check whether path exists
q = numpy.multiply(f[qPath] , 10.) #convert to 1/nm from 1/A
qErrors = f[qErrorsPath] #TODO check whether path exists

numpy.savetxt("dataq.dat",numpy.column_stack((q,data, dataErrors))) #TODO filename
numpy.savetxt("background.dat",numpy.column_stack((q,backgroundData))) #TODO filename
for i in xrange(0,normalizationData.shape[0]): #TODO errors if they are not in file already - again, use 0.5
	numpy.savetxt("frame"+str(i).zfill(4)+".dat",numpy.column_stack((q,normalizationData[i], normalizationErrors[i])))

