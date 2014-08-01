import h5py, sys
class ispybDataCollection:
	def __init__(self):
		host = "ispybb-test.diamond.ac.uk"
		webServiceName = "ToolsForCollectionWebService"
		self.createWebServiceClient(host, webServiceName)
		self.createDataCollection()

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
		self.client = Client(URL, transport=httpAuthenticatedWebService)
		self.client.options.cache.clear()

	def setCollectionValues(self, collectionValues):
		for (key,value) in collectionValues.items():
			self.collection.__setattr__(key,value)
		return self.collection
	
	def createDataCollection(self):
		self.collection = self.client.factory.create('dataCollectionWS3VO')

	def storeCollection(self):
		returned = self.client.service.storeOrUpdateDataCollection(self.collection)
		return returned

if __name__ == '__main__':
	print "this is not meant to be run standalone, exiting"
	sys.exit(1)
