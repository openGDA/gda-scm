package uk.ac.gda.devices.bssc.perspectives;
 
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
 
public class PerspecticePropertyTester extends PropertyTester {
 
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 
    /*
    * Returns true if the actual perspective supports chromatogram editing.
    */
    IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    String perspectiveId = perspective.getId();
    if(perspectiveId.startsWith("uk.ac.gda.devices.bssc.biosaxs")) {
      return true;
    }
    return false;
  }
}