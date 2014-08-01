def getDataFromH5File(h5In):
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

	#TODO resolution - get from reduced qmin/max values of reduced data Nexus file
	map["resolution"] = 4

	#TODO not sure what to put in here - null causes a silent failure in the web service
	map["dataCollectionGroupId"] = 158801

	#TODO need blsessionid
	map["sessionId"] = 18881
	
	map["comments"] = "juntestblahblah"
	return map

import h5py
fileIn="/dls/b21/data/2014/cm4976-3/b21-12433.nxs"
h5File = h5py.File(fileIn,'r')
values = getDataFromH5File(h5File)
import ispybDataCollection
dc=ispybDataCollection.ispybDataCollection()
dc.setCollectionValues(values)
dc.storeCollection()