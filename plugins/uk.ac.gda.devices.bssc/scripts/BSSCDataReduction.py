from gda.data.metadata import GDAMetadataProvider

#note that this class will only work for one data reduction at a time. each self.startedDataReductionId will be unique.
#if multiple data reductions are being done, each one will need a separate instance of this object
class BSSCDataReduction:
    def __init__(self):
        self.ispyb = BioSAXSDBFactory.makeAPI()
        currentVisit = GDAMetadataProvider.getInstance().getMetadataValue("visit")
        self.proposal = self.ispyb.getProposalForVisit(currentVisit)
        self.session = self.ispyb.getSessionForVisit(currentVisit)
    def startDataReduction(self):
        self.startedDataReductionId = self.ispyb.createDataReductionStarted(self.dataCollectionId)
        #call the data reduction here
    def isDataReductionRunning(self):
        return self.ispyb.isDataReductionRunning(self.startedDataReductionId)
    def waitUntilDataReductionFinished(self):
        try:
            while (1): #replace this with a query of the data reduction process
                sleep(1)
        except:
            self.ispyb.setDataReductionFailedToComplete(self.startedDataReductionId)
            return
        self.ispyb.clearDataReductionStarted(self.startedDataReductionId)
        #store results in ispyb
    def isDataReductionFailedToComplete(self):
        return self.ispyb.isDataReductionFailedToComplete()
    def isDataReductionFailed(self):
        return self.ispyb.isDataReductionFailed(self.dataCollectionId)
    def isDataReductionSuccessful(self):
        return self.ispyb.isDataReductionSuccessful(self.dataCollectionId, self.startedDataReductionId)
    def setDataCollectionId(self, dataCollectionId):
        self.dataCollectionId = dataCollectionId
    #experimentId is more commonly used than dataCollectionId and should be readily available within BSSC.py
    def setExperimentId(self, experimentId):
        self.dataCollectionId = self.ispyb.getDataCollectionForExperiment(experimentId)