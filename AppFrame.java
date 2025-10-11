import java.applet.Applet;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class AppFrame extends Frame implements WindowListener {
  Applet applet;
  
  public AppFrame(Applet paramApplet, String paramString) {
    super(paramString);
    addWindowListener(this);
    this.applet = paramApplet;
  }
  
  public void windowDeactivated(WindowEvent paramWindowEvent) {}
  
  public void windowClosing(WindowEvent paramWindowEvent) {
    this.applet.stop();
    System.exit(0);
  }
  
  public void windowOpened(WindowEvent paramWindowEvent) {}
  
  public void windowClosed(WindowEvent paramWindowEvent) {}
  
  public void windowDeiconified(WindowEvent paramWindowEvent) {}
  
  public void windowActivated(WindowEvent paramWindowEvent) {}
  
  public void windowIconified(WindowEvent paramWindowEvent) {}
}


/* Location:              C:\a\!\AppFrame.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */