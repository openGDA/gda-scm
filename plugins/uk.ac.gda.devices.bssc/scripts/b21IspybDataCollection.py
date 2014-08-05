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

	#TODO not sure what to put in here - null causes a silent failure in the web service
	map["dataCollectionGroupId"] = 158801

	#TODO need blsessionid
	map["sessionId"] = 18881
	
	map["comments"] = "no comment"

	#TODO need some explanation on what the following fields are for	
	map["dataCollectionNumber"] = 12433
	map["imageDirectory"] = "/dls/b21/data/2014/cm4962-3/.ispyb/testDir/"
	map["imagePrefix"] = "testPrefix"
	map["fileTemplate"] = "12433.dat"

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

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--rawfile", type=str, help="scanned data Nexus file")
	parser.add_argument("--reducedfile", type=str, help="reduced data Nexus file")
	parser.add_argument("--summaryimage", type=str, help="summary image name (first panel in ISPyB)")
	parser.add_argument("--image1", type=str, help="first image in second panel of ISPyB")
	parser.add_argument("--image2", type=str, help="second image in second panel of ISPyB")
	parser.add_argument("--image3", type=str, help="third image in second panel of ISPyB")

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
	if args.image1:
		image1 = args.image1
	else:
		image1 = ""
	if args.image2:
		image2 = args.image2
	else:
		image2 = ""
	if args.image3:
		image3 = args.image3
	else:
		image3 = ""

	import h5py
	fileIn=rawFilename
	reducedFilename=reducedFilename
	h5File = h5py.File(fileIn,'r')
	reducedFile = h5py.File(reducedFilename, 'r')
	values = getDataFromH5File(h5File, reducedFile)
	values = storeTransmission(values, "Scatter Diode") #actual value as of 2014-07-31
	values = storeBeamSize(values, 4.0925, 0.8195) #actual values (in MM) as of 2014-07-31
	values = storeImages(values, summaryImage, image1, image2, image3)
	import ispybDataCollection
	dc=ispybDataCollection.ispybDataCollection()
	dc.setCollectionValues(values)
	dc.storeCollection()