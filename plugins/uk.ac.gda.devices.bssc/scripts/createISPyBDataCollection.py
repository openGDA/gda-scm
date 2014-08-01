import h5py, sys
class ispybDataCollection:
	def createWebServiceClient(self, host, webServiceName):
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
	def getH5File(self, fileName):
		import h5py
		return h5py.File(fileName,'r')
	
	def setCollectionValues(self, dataCollection, collectionValues):
		for (key,value) in collectionValues.items():
			dataCollection.__setattr__(key,value)
		return dataCollection
	
	def createDataCollection(self):
		collection = client.factory.create('dataCollectionWS3VO')

	def storeCollection(self, dataCollection, client):
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
