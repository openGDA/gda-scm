def retrieveVisitInfo(h5In):
	visitNumberPath = "/entry1/experiment_identifier"
	beamlineNamePath = "/entry1/instrument/name"
	visitName = h5In[visitNumberPath][0]
	proposalCode = visitName[0:2]
	proposalNumber = visitName.split("-")[0][2:]
	visitNumber = visitName.split("-")[1]
	beamlineName = h5In[beamlineNamePath][0]
	return proposalCode, proposalNumber, beamlineName, visitNumber

def getDataFromH5File(h5In, reducedFile):
	map = {}
	exposureTimePath = "/entry1/instrument/detector/count_time"
	map["exposureTime"]= h5In[exposureTimePath][0][0]
	assert h5In[exposureTimePath].attrs["units"]=="s" #should be s
	
	numberOfImagesPath = "/entry1/instrument/detector/count_time/"
	numberOfImages = len(h5In[numberOfImagesPath][0])
	map["numberOfImages"] = numberOfImages

	#wavelength - need to convert from energy, which is assumed to be stored as keV
	energyPath = "/entry1/instrument/monochromator/energy"
	energyInKev = h5In[energyPath][0]
	energyUnitAttribute = "units"
	assert h5In[energyPath].attrs[energyUnitAttribute]=="keV"
	map["wavelength"] = 12.398425/energyInKev

	qPath = "/entry1/detector_result/q"
	qDataInvNm = reducedFile[qPath]
	assert reducedFile[qPath].attrs["units"]=="Angstrom^-1"
	import numpy
	qSize = len(qDataInvNm)
	qArray = numpy.zeros((qSize,),dtype='float32')
	qDataInvNm.read_direct(qArray,numpy.s_[0:qSize])
	qData = 1. / qArray.max()
	map["resolution"] = qData #this value will be Angstrom for now - need to discuss whether to change to nm for SAXS

	#TODO the following value works for both dummy and real databases - null causes a silent failure in the web service
	map["dataCollectionGroupId"] = 158801

	map["comments"] = "no comment"

	scanNumber = h5In["/entry1/entry_identifier"][0]
	map["dataCollectionNumber"] = scanNumber
	visitPathSplit = (str(h5In.filename)).split("/")
	import os
	visitPath = os.path.sep + os.path.join(visitPathSplit[0], visitPathSplit[1], visitPathSplit[2], visitPathSplit[3], visitPathSplit[4], visitPathSplit[5])
	map["imageDirectory"] = os.path.join(visitPath , ".ispyb")
	map["imagePrefix"] = scanNumber
	map["fileTemplate"] = scanNumber+".dat"

	return map

def storeTransmission(map, d3FilterPositioner):
	#TODO transmission will be 100% if D3 filter positioner is in the "Scatter Diode" position. if not in this position, we should put in NaN, if Oracle can handle it
	assert d3FilterPositioner=="Scatter Diode"
	transmission = 100
	map["transmission"] = transmission
	return map

def storeBeamSize(map, beamSizeXMM, beamSizeYMM):
	#TODO these values should be from S5 X and Y sizes in EPICS. beam is assumed to be parallel from there
	map["beamSizeAtSampleX"] = beamSizeXMM
	map["beamSizeAtSampleY"] = beamSizeYMM
	return map

def storeImages(map, summaryImageFile1, snapshotImageFile1, snapshotImageFile2, snapshotImageFile3):
	map["xtalSnapshotFullPath1"] = summaryImageFile1
	map["xtalSnapshotFullPath2"] = snapshotImageFile1
	map["xtalSnapshotFullPath3"] = snapshotImageFile2
	map["xtalSnapshotFullPath4"] = snapshotImageFile3
	return map

def createZipFile(baseDirectory, rawFile, reducedFile):
	import zipfile
	downloadDirectoryName = os.path.join(baseDirectory, "download")
	print baseDirectory, downloadDirectoryName
	if not os.path.exists(downloadDirectoryName):
		os.makedirs(downloadDirectoryName)
	z=zipfile.ZipFile(os.path.join(downloadDirectoryName, "download.zip"),'w')
	z.write(rawFile)
	z.write(reducedFile)
	z.close()

if __name__ == '__main__':

	import argparse, sys
	parser = argparse.ArgumentParser()
	parser.add_argument("--rawfile", type=str, help="scanned data Nexus file")
	parser.add_argument("--reducedfile", type=str, help="reduced data Nexus file")
	parser.add_argument("--summaryimage", type=str, help="summary image name (first panel in ISPyB)")

	args = parser.parse_args()

	if args.rawfile:
		rawFilename = args.rawfile
	else:
		print "rawfile must be defined"
		sys.exit(1)
	if args.reducedfile:
		reducedFilename = args.reducedfile
	else:
		print "reducedfile must be defined"
		sys.exit(1)

	if args.summaryimage:
		summaryImage = args.summaryimage

	#set up data collection class
	import ispybDataCollection
	dc=ispybDataCollection.ispybDataCollection()

	import h5py
	fileIn=rawFilename
	reducedFilename=reducedFilename
	h5File = h5py.File(fileIn,'r')
	reducedFile = h5py.File(reducedFilename, 'r')
	values = getDataFromH5File(h5File, reducedFile)
	
	proposalCode, proposalNumber, beamlineName, visitNumber = retrieveVisitInfo(h5File)
	sessionId = dc.getSessionId(proposalCode, proposalNumber, beamlineName, visitNumber)
	if sessionId == 0:
		print "warning, the data collection may not be stored correctly because the sessionId was not found"
	values["sessionId"] = sessionId
	values = storeTransmission(values, "Scatter Diode") #actual value as of 2014-07-31
	values = storeBeamSize(values, 4.0925, 0.8195) #actual values (in MM) as of 2014-07-31
	
	#create summary 3d surface plot
	from create3dPlotIvsQ import createPlot
	createPlot(reducedFilename, "/entry1/detector_processing/Normalisation/data", "/entry1/detector_result/q", summaryImage, False)
	#create 3 snapshots from beginning, middle, end of data collection
	from create3Plots import create3Plots
	filenames = create3Plots(reducedFilename, summaryImage)
	values = storeImages(values, summaryImage, filenames[0], filenames[1], filenames[2])

	import os
	baseDirectory = os.path.join(values["imageDirectory"], values["imagePrefix"])
	createZipFile(baseDirectory, rawFilename, reducedFilename)
	
	dc.setCollectionValues(values)
	dc.storeCollection()