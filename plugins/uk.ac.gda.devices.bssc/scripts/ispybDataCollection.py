import h5py, sys
class ispybDataCollection(object):
	def __init__(self):
		host = "ispybb.diamond.ac.uk"
		webServiceName = "ToolsForCollectionWebService"
		self.createWebServiceClient(host, webServiceName)
		self.createDataCollection()
		self.collectionValuesStored = False
		self.collectionIncomplete = False

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
		self.checkCollectionValues(collectionValues)
	
	def checkCollectionValues(self, collectionValues):
		#check that all required fields for data collection creation exist in self.collection
		self.collectionValuesStored = True
		keys_to_check = self.getKeysToCheck()
		for key in keys_to_check:
			if not collectionValues.has_key(key):
				self.collectionIncomplete = True
				print "expect key '" + key + "' to be in collection values"

	def getKeysToCheck(self):
		#override this method to ensure that you have a complete set of keys before storing the data collection
		return ["sessionId", "dataCollectionGroupId", "exposureTime", "numberOfImages", "wavelength", 
						"transmission", "beamSizeAtSampleX", "beamSizeAtSampleY", "resolution", "dataCollectionNumber",
						"imageDirectory", "imagePrefix", "fileTemplate", "xtalSnapshotFullPath1", "xtalSnapshotFullPath2",
						"xtalSnapshotFullPath3","xtalSnapshotFullPath4"]

	def createDataCollection(self):
		self.collection = self.client.factory.create('dataCollectionWS3VO')

	def storeCollection(self):
		#make sure collection items and client are there already!
		assert self.client != None
		if not self.collectionValuesStored:
			print "DataCollection values not stored, aborting"
			sys.exit(1)
		if self.collectionIncomplete:
			print "not all required fields in DataCollection were specified, aborting"
			sys.exit(1)
		returned = self.client.service.storeOrUpdateDataCollection(self.collection)
		return returned

	#return sessionId of the specified visit or 0
	def getSessionId(self, proposalCode, proposalNumber, beamlineName, visitNumber):
		sessions = self.client.service.findSessionsByCodeAndNumberAndBeamLine(proposalCode, proposalNumber, beamlineName)
		for session in sessions:
			if session.sessionId == visitNumber:
				return session.sessionId
		return 0
	
if __name__ == '__main__':
	print "this is not meant to be run standalone, exiting"
	sys.exit(1)
