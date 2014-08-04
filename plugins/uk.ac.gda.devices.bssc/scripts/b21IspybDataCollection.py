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

	#TODO transmission will be 100% if D3 filter positioner is in the "Scatter Diode" position. if not in this position, we should put in NaN, if Oracle can handle it
	transmission = 100
	map["transmission"] = transmission

	#TODO get from S5 X and Y sizes in EPICS. beam is assumed to be parallel from there
	beamSizeX = 4.0925
	beamSizeY = 0.8195
	map["beamSizeAtSampleX"] = beamSizeX
	map["beamSizeAtSampleY"] = beamSizeY

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

	map["xtalSnapshotFullPath1"] = "/dls/b21/data/2014/cm4962-3/.ispyb/testDir/testPrefix/ispybb_multiple_curves_20140731_5_crop.png"
	map["xtalSnapshotFullPath2"] = "/dls/b21/data/2014/cm4962-3/.ispyb/testDir/testPrefix/ispybb_multiple_curves_20140731_4_crop.png"
	map["xtalSnapshotFullPath3"] = "/dls/b21/data/2014/cm4962-3/.ispyb/testDir/testPrefix/ispybb_multiple_curves_20140731_1_crop.png"
	map["xtalSnapshotFullPath4"] = "/dls/b21/data/2014/cm4962-3/.ispyb/testDir/testPrefix/ispybb_multiple_curves_20140731_3_crop.png"

	#TODO need some explanation on what the following fields are for	
	map["dataCollectionNumber"] = 12433
	map["imageDirectory"] = "/dls/b21/data/2014/cm4962-3/.ispyb/testDir/"
	map["imagePrefix"] = "testPrefix"
	map["fileTemplate"] = "12433.dat"

	return map

import h5py
fileIn="/dls/b21/data/2014/cm4976-3/b21-12433.nxs"
reducedFileIn="/dls/b21/data/2014/cm4976-3/b21-12434.nxs.22967.reduction/output/background_b21-12433_detector_260614_132314.nxs"
h5File = h5py.File(fileIn,'r')
reducedFile = h5py.File(reducedFileIn, 'r')
values = getDataFromH5File(h5File, reducedFile)
import ispybDataCollection
dc=ispybDataCollection.ispybDataCollection()
dc.setCollectionValues(values)
dc.storeCollection()