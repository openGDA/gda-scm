import h5py, sys

def createWebServiceClient(host, webServiceName):
	from suds.client import Client
	from suds.transport.http import HttpAuthenticated
	import sys
	URL="http://"+host+":8080/ispyb-ejb3/ispybWS/"+webServiceName+"?wsdl"
	sys.path.append("/dls_sw/dasc/important")
	from ispybbUserInfo import ispybbUser, ispybbPassword
	username = ispybbUser()
	userPassword = ispybbPassword()
	httpAuthenticatedWebService = HttpAuthenticated(username=username, password=userPassword)
	client = Client(URL, transport=httpAuthenticatedWebService)
	client.options.cache.clear()
	return client

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
	return map

def getH5File(fileName):
	import h5py
	return h5py.File(fileName,'r')

def setCollectionValues(dataCollection, collectionValues):
	for (key,value) in collectionValues.items():
		dataCollection.__setattr__(key,value)
	return dataCollection

def storeCollection(dataCollection, client):
	returned = client.service.storeOrUpdateDataCollection(dataCollection)
	return returned

#main body
sys.exit(1)
host = "ispybb-test.diamond.ac.uk"
webServiceName = "ToolsForCollectionWebService"
client = createWebServiceClient(host, webServiceName)
collection = client.factory.create('dataCollectionWS3VO')
fileIn="/dls/b21/data/2014/cm4976-3/b21-12433.nxs"
h5File = getH5File(fileIn)
values = getDataFromH5File(h5File)
collection = setCollectionValues(collection, values)
print storeCollection(collection, client)
