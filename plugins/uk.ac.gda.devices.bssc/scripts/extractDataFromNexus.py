#requires module load numpy
import numpy
import os
import sys

defaultErrorRatio = 0.5
#fileIn = "/dls/b21/data/2014/mx9007-20/processing/results_b21-7475_detector_010314_194552.nxs"
#fileIn = "/dls/b21/data/2013/cm5947-3/processing/results_b21-2672_detector_040713_102411.nxs"

def setupPathNames(fileIn, detectorName):
	detectorPrefix = "/entry1/"+detectorName
	dataPath = detectorPrefix+"_result/data"
	dataErrorsPath = detectorPrefix+"_result/errors"
	qPath = detectorPrefix+"_result/q"
	qErrorsPath = qPath+"_errors"
	backgroundPath = detectorPrefix+"_processing/BackgroundSubtraction/background"
	backgroundErrorsPath = backgroundPath + "_errors"
	normalizationPath = detectorPrefix+"_processing/Normalisation/data"
	normalizationErrorsPath = detectorPrefix+"_processing/Normalisation/errors"
	return (dataPath, dataErrorsPath), (qPath, qErrorsPath), (normalizationPath, normalizationErrorsPath), (backgroundPath, backgroundErrorsPath)

def getDataAndErrors(fileIn, dataPaths, qPaths, normalizationPaths, backgroundPaths):
	import h5py
	f=h5py.File(fileIn)

	dataPath = dataPaths[0]
	dataErrorsPath = dataPaths[1]
	data = f[dataPath][0][0]
	if dataErrorsPath[1:] in f:
		dataErrors=f[dataErrorsPath][0][0]
	else:
		dataErrors = numpy.multiply(data, defaultErrorRatio)

	backgroundPath = backgroundPaths[0]
	backgroundErrorsPath = backgroundPaths[1]
	if backgroundPath[1:] in f:
		backgroundData = f[backgroundPath][0][0]
	if backgroundErrorsPath[1:] in f:
		backgroundErrors = numpy.multiply(backgroundData, defaultErrorRatio)

	normalizationPath = normalizationPaths[0]
	normalizationErrorsPath = normalizationPaths[1]
	if normalizationPath[1:] in f:
		normalizationData = f[normalizationPath][0] #multidimensional array
	if normalizationErrorsPath[1:] in f:
		normalizationErrors = f[normalizationErrorsPath][0]
	else:
		normalizationErrors = numpy.multiply(normalizationData, defaultErrorRatio)

	qPath = qPaths[0]
	qErrorsPath = qPaths[1]
	q = numpy.multiply(f[qPath] , 10.) #convert to 1/nm from 1/A
	if qErrorsPath[1:] in f:
		qErrors = f[qErrorsPath]
	else:
		qErrors = numpy.multiply(q, defaultErrorRatio)
	return (data, dataErrors), (q, qErrors), (normalizationData, normalizationErrors), (backgroundData, backgroundErrors)

def writeOutData(outputDir, datas, qs, normalizations, backgrounds):
	curveFiles = []
	data = datas[0]
	dataErrors = datas[1]
	q = qs[0]
	qErrors = qs[1] #not used by ISPyBB
	subtractedFileSuffix = "_sub.dat"
	if not os.path.exists(outputDir):
		os.makedirs(outputDir)
	if not outputDir.endswith(os.sep):
		outputDir += os.sep
	#check that outputDir ends with file separator
	sampleAverageFilename = outputDir + "dataq_ave.dat"
	numpy.savetxt(sampleAverageFilename,numpy.column_stack((q,data, dataErrors))) #TODO filename
	curveFiles.append(sampleAverageFilename)

	backgroundData = backgrounds[0]
	backgroundErrors = backgrounds[1]
	backgroundAverageFilename = outputDir + "background_averbuffer.dat"
	numpy.savetxt(backgroundAverageFilename,numpy.column_stack((q,backgroundData, backgroundErrors))) #TODO filename
	curveFiles.append(backgroundAverageFilename)

	normalizationData = normalizations[0]
	normalizationErrors = normalizations[1]
	for i in xrange(0,normalizationData.shape[0]):
		frameFilename = outputDir + "frame"+str(i).zfill(4)+".dat"
		numpy.savetxt(frameFilename,numpy.column_stack((q,normalizationData[i], normalizationErrors[i])))
		curveFiles.append(frameFilename)
	return curveFiles

def directCall(filename, outputFolderName, detector, returnFilenames):
	(dataPaths, qPaths, normalizationPaths, backgroundPaths) = setupPathNames(filename, detector)
	(datas, qs, normalizations, backgrounds) = getDataAndErrors(filename, dataPaths, qPaths, normalizationPaths, backgroundPaths)
	curveFiles = writeOutData(outputFolderName, datas, qs, normalizations, backgrounds)
	if returnFilenames:
		return curveFiles

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--filename", type=str, help="input filename after data reduction")
	parser.add_argument("--outputFolderName", type=str, help="output folder location")
	parser.add_argument("--detector", type=str, help="detector name")
	args = parser.parse_args()

	if args.filename:
		filename = args.filename
	else:
		print "filename must be defined"
		sys.exit(1)
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
	if args.outputFolderName:
		outputFolderName = args.outputFolderName
	else:
		print "filename must be defined"
		sys.exit(1)

	directCall(filename, outputFolderName, detector, False)
