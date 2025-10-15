import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Game3D extends Panel implements WindowListener {
  
  MainGame game;
  
  Label hiScoreLabel;
  Label lblContinue;
  NumberLabel scoreWin;
  
  public void stop() {
    this.game.stop();
  }
  
  private String decodeString(String paramString) {
    if (paramString.charAt(0) != 'Z')
      return paramString; 
    StringBuffer stringBuffer = new StringBuffer();
    for (byte b = 1; b < paramString.length(); b += 4) {
      int i = Integer.parseInt(paramString.substring(b, b + 4), 16);
      stringBuffer.append((char)i);
    } 
    return stringBuffer.toString();
  }
  
  public Frame window;
  public void windowDeactivated(WindowEvent paramWindowEvent) {}
  public void windowClosing(WindowEvent paramWindowEvent) { System.exit(0); }
  public void windowOpened(WindowEvent paramWindowEvent) {}
  public void windowClosed(WindowEvent paramWindowEvent) {}
  public void windowDeiconified(WindowEvent paramWindowEvent) {}
  public void windowActivated(WindowEvent paramWindowEvent) {}
  public void windowIconified(WindowEvent paramWindowEvent) {}
  public void toggleFullScreen() {
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    if(gd.isFullScreenSupported()) gd.setFullScreenWindow(gd.getFullScreenWindow() == this.window? null : this.window);
  }  
  
  public static void main(String[] paramArrayOfString) {
    Game3D game3D = new Game3D();
    Frame frame = game3D.window = new Frame("Jet Slalom Resurrected");
    frame.addWindowListener(game3D);
    frame.setVisible(true);
    frame.setLayout(new BorderLayout());
    game3D.setSize(800, 600);
    frame.add(game3D, BorderLayout.CENTER);
    game3D.init();
    frame.validate();
    frame.pack();
    frame.setSize(800, 600);
    game3D.start();
  }
  
  public void start() {
    this.game.start();
    this.game.startGame(1, false);
  }
  
  public void init() {
    setLayout(new BorderLayout());
    setBackground(new Color(160, 208, 176));
    this.scoreWin = new NumberLabel(64, 12);
    this.lblContinue = new Label("            ");
    Panel panel = new Panel(new FlowLayout(0, 5, 0));
    panel.setLayout(new FlowLayout());
    panel.add(new Label("Score:"));
    panel.add(this.scoreWin);
    panel.add(new Label("Continue penalty:"));
    panel.add(this.lblContinue);
    add(panel, BorderLayout.NORTH);
    this.hiScoreLabel = new Label("Your Hi-score:0         ");
    add(this.hiScoreLabel, BorderLayout.SOUTH);
    this.game = new MainGame(this);
    //add("Center", this.game);
    this.game.init();
    this.requestFocus();
    invalidate();
    validate();
  }
  
  private String encodeString(String paramString) {
    StringBuffer stringBuffer = new StringBuffer("Z");
    for (byte b = 0; b < paramString.length(); b++) {
      char c = paramString.charAt(b);
      String str = "0000" + Integer.toHexString(c);
      str = str.substring(str.length() - 4);
      stringBuffer.append(str);
    } 
    return stringBuffer.toString();
  }
  
}