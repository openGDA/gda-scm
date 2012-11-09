import time
import org.embl.task.Script
import org.embl.ctrl.Device
import org.embl.dev.tecan.TecanValve
import org.embl.bssc


class TestScript(org.embl.task.Script):
    def run(self, parent, args):
        # @type parent org.embl.hc.hcControl
        ctr=parent
        ctr.setValvePosition(org.embl.bssc.scControl.DEVICE_SYRINGE_VALVE, org.embl.dev.tecan.TecanValve.Position.output)
        return "OK"

