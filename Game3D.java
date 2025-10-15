import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Game3D extends Panel implements WindowListener {
  
  private static Frame window;
  public void windowDeactivated(WindowEvent paramWindowEvent) {}
  public void windowClosing(WindowEvent paramWindowEvent) { System.exit(0); }
  public void windowOpened(WindowEvent paramWindowEvent) {}
  public void windowClosed(WindowEvent paramWindowEvent) {}
  public void windowDeiconified(WindowEvent paramWindowEvent) {}
  public void windowActivated(WindowEvent paramWindowEvent) {}
  public void windowIconified(WindowEvent paramWindowEvent) {}
  public void toggleFullScreen() {
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    if(gd.isFullScreenSupported()) gd.setFullScreenWindow(gd.getFullScreenWindow() == window? null : window);
  }  

  Label hiScoreLabel;
  Label lblContinue;
  NumberLabel scoreWin;
  
  
  public static void main(String[] args) {
    window = new Frame("Jet Slalom Resurrected");
    Game3D game3D = new Game3D();
    window.addWindowListener(game3D);
    window.setLayout(new BorderLayout());
    window.add(game3D, BorderLayout.CENTER);
    window.setVisible(true);
    
    game3D.setLayout(new BorderLayout());
    game3D.setBackground(new Color(160, 208, 176));
    game3D.scoreWin = new NumberLabel(64, 12);
    game3D.lblContinue = new Label("            ");
    Panel panel = new Panel(new FlowLayout(0, 5, 0));
    panel.setLayout(new FlowLayout());
    panel.add(new Label("Score:"));
    panel.add(game3D.scoreWin);
    panel.add(new Label("Continue penalty:"));
    panel.add(game3D.lblContinue);
    game3D.add(panel, BorderLayout.NORTH);
    game3D.hiScoreLabel = new Label("Your Hi-score:0         ");
    game3D.add(game3D.hiScoreLabel, BorderLayout.SOUTH);
    MainGame game = new MainGame(game3D);
    game.init();
    game3D.requestFocus();
    game3D.invalidate();
    game3D.validate();
    
    window.validate();
    window.pack();
    window.setSize(800, 600);
    game.start();
    game.startGame(1, false);
  }
  
}