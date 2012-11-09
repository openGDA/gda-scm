
import org.embl.bssc.scDevPlateTable.WellPosition;
import org.embl.dev.tecan.TecanValve.Position;

public class TestScript extends org.embl.task.Script
{
    @Override
    protected Object run(Object parent, Object[] args) throws Exception
    {
        org.embl.bssc.scControl ctr = (org.embl.bssc.scControl) parent;
        ctr.setValvePosition(org.embl.bssc.scControl.DEVICE_SYRINGE_VALVE, Position.output);
        ctr.log(this, "Done");
        ctr.movePlateWell(0, 0, 0, WellPosition.TOP);
        Thread.sleep(1000);
        ctr.movePlateWell(0, 0, 0, WellPosition.BOTTOM);
        Thread.sleep(1000);
        ctr.movePlateWell(0, 0, 0, WellPosition.TOP);
        Thread.sleep(1000);
        ctr.movePlateWell(0, 0, 0, WellPosition.BOTTOM);        
        return true;
    }
}