#requires module load numpy
import numpy

defaultErrorRatio = 0.5
fileIn = "/dls/b21/data/2014/mx9007-20/processing/results_b21-7475_detector_010314_194552.nxs"
fileIn = "/dls/b21/data/2013/cm5947-3/processing/results_b21-2672_detector_040713_102411.nxs"

def setupPathNames(fileIn):
	if fileIn.startswith("/dls/b21"):
		detectorName = "detector"
	elif fileIn.startswith("/dls/i22"):
		detectorName = "Pilatus2M"
	detectorPrefix = "/entry1/"+detectorName
	dataPath = detectorPrefix+"_result/data"
	dataErrorsPath = detectorPrefix+"_result/errors"
	qPath = detectorPrefix+"_result/q"
	qErrorsPath = qPath+"_errors"
	backgroundPath = detectorPrefix+"_processing/BackgroundSubtraction/background"
	normalizationPath = detectorPrefix+"_processing/Normalisation/data"
	normalizationErrorsPath = detectorPrefix+"_processing/Normalisation/errors"
	return (dataPath, dataErrorsPath), (qPath, qErrorsPath), (normalizationPath, normalizationErrorsPath), backgroundPath

def getDataAndErrors(fileIn, dataPaths, qPaths, normalizationPaths, backgroundPath):
	import h5py
	f=h5py.File(fileIn)

	dataPath = dataPaths[0]
	dataErrorsPath = dataPaths[1]
	data = f[dataPath][0][0]
	if f.has_key(dataErrorsPath):
		dataErrors=f[dataErrorsPath][0][0]
	else:
		dataErrors = numpy.multiply(data, defaultErrorRatio)

	if f.has_key(backgroundPath):
		backgroundData = f[backgroundPath][0][0]

	normalizationPath = normalizationPaths[0]
	normalizationErrorsPath = normalizationPaths[1]
	if f.has_key(normalizationPath):
		normalizationData = f[normalizationPath][0] #multidimensional array
	if f.has_key(normalizationErrorsPath):
		normalizationErrors = f[normalizationErrorsPath][0]
	else:
		normalizationErrors = numpy.multiply(normalizationData, defaultErrorRatio)

	qPath = qPaths[0]
	qErrorsPath = qPaths[1]
	q = numpy.multiply(f[qPath] , 10.) #convert to 1/nm from 1/A
	if f.has_key(qErrorsPath):
		qErrors = f[qErrorsPath]
	else:
		qErrors = numpy.multiply(q, defaultErrorRatio)
	return (data, dataErrors), (q, qErrors), (normalizationData, normalizationErrors), backgroundData

def writeOutData(outputDir, datas, qs, normalizations, backgroundData):
	data = datas[0]
	dataErrors = datas[1]
	q = qs[0]
	qErrors = qs[1] #not used by ISPyBB
	subtractedFileSuffix = "_sub.dat"
	numpy.savetxt("dataq.dat",numpy.column_stack((q,data, dataErrors))) #TODO filename

	numpy.savetxt("background.dat",numpy.column_stack((q,backgroundData))) #TODO filename

	normalizationData = normalizations[0]
	normalizationErrors = normalizations[1]
	for i in xrange(0,normalizationData.shape[0]):
		numpy.savetxt("frame"+str(i).zfill(4)+".dat",numpy.column_stack((q,normalizationData[i], normalizationErrors[i])))

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--filename", type=str, help="input filename after data reduction")
	parser.add_argument("--outputFolderName", type=str, help="output folder location")
	parser.add_argument("--detector", type=str, help="detector name")
	args = parser.parse_args()

	if args.detector:
		detector = args.detector
	else:
		defaultDetectorName = "detector"
		if filename.startswith("/dls/b21"):
			detector = "detector"
		elif filename.startswith("/dls/i22"):
			detector = "Pilatus2M"
		else:
			print "Unexpected data path, setting detector name to " + defaultDetectorName
			detector = defaultDetectorName

	(dataPaths, qPaths, normalizationPaths, backgroundPath) = setupPathNames(fileIn)
	getDataAndErrors(fileIn, dataPaths, qPaths, normalizationPaths, backgroundPath)
	writeOutData(outputDir)

