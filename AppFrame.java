// DAVE import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public class AppFrame extends Frame implements WindowListener {
  // DAVE Applet applet;
  
  public AppFrame(/* DAVE Applet paramApplet, */String paramString) {
    super(paramString);
    addWindowListener(this);
    // DAVE this.applet = paramApplet;
  }
  
  public void windowDeactivated(WindowEvent paramWindowEvent) {}
  
  public void windowClosing(WindowEvent paramWindowEvent) {
    // DAVE this.applet.stop();
    System.exit(0);
  }
  
  public void windowOpened(WindowEvent paramWindowEvent) {}
  
  public void windowClosed(WindowEvent paramWindowEvent) {}
  
  public void windowDeiconified(WindowEvent paramWindowEvent) {}
  
  public void windowActivated(WindowEvent paramWindowEvent) {}
  
  public void windowIconified(WindowEvent paramWindowEvent) {}
  
  public void toggleFullScreen() {
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    if(gd.isFullScreenSupported()) gd.setFullScreenWindow(gd.getFullScreenWindow() == this? null : this);
  }  
}


/* Location:              C:\a\!\AppFrame.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */