
class BM26aSamplePreparer:
    def __init__(self, samplestage_scannable, cryostage_scannable):
        self.samplestage_scannable = samplestage_scannable
        self.cryostage_scannable = cryostage_scannable
        self.logging_enabled = True
    
    def setLoggingEnabled(self, enabled):
        self.logging_enabled = enabled
    
    def log(self, msg):
        if self.logging_enabled == True:
            simpleLog(msg)
        else:
            print msg
    
    def prepare(self, sampleParameters):
        if sampleParameters.getStage() == "sampleStage":
            self._control_sample_stage(sampleParameters.getXyzStageParameters()) 
        elif sampleParameters.getStage() == "cryoStage":
            self._control_cryo_stage(sampleParameters.getCryoStageParameters())
     
    def _control_sample_stage(self, bean):
        targetPosition = [bean.getX(), bean.getY(), bean.getZ()]
        self.log( "moving sampleStage (" + self.samplestage_scannable.name + ") to " + str(targetPosition))
        self.samplestage_scannable(targetPosition);
        self.log(  "sampleStage move complete.")

    def _control_cryo_stage(self, bean):
        targetPosition = [bean.getX(), bean.getY(), bean.getZ()]
        self.log( "moving cryoStage (" + self.cryostage_scannable.name + ") to " + str(targetPosition))
        self.cryostage_scannable(targetPosition);
        self.log(  "cryoStage move complete.")
    