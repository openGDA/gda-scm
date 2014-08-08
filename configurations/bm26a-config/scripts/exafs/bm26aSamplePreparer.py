from gdascripts.messages.handle_messages import simpleLog

class BM26aSamplePreparer:
    def __init__(self, xyzStage_scannable, cryoStage_scannable):
        self.xyzStage_scannable = xyzStage_scannable
        self.cryoStage_scannable = cryoStage_scannable
        self.logging_enabled = True
    
    def setLoggingEnabled(self, enabled):
        self.logging_enabled = enabled
    
    def log(self, msg):
        if self.logging_enabled == True:
            simpleLog(msg)
        else:
            print msg
    
    def prepare(self, sampleParameters):
        if sampleParameters.getStage() == "xyzStage":
            self._control_sample_stage(sampleParameters.getXyzStageParameters()) 
        elif sampleParameters.getStage() == "cryoStage":
            self._control_cryo_stage(sampleParameters.getCryoStageParameters())
     
    def _control_sample_stage(self, bean):
        targetPosition = [bean.getX(), bean.getY(), bean.getZ()]
        self.log( "moving xyzStage (" + self.xyzStage_scannable.name + ") to " + str(targetPosition))
        self.xyzStage_scannable(targetPosition);
        self.log(  "xyzStage move complete.")

    def _control_cryo_stage(self, bean):
        targetPosition = [bean.getX(), bean.getY(), bean.getZ()]
        self.log( "moving cryoStage (" + self.cryoStage_scannable.name + ") to " + str(targetPosition))
        self.cryoStage_scannable(targetPosition);
        self.log(  "cryoStage move complete.")
    