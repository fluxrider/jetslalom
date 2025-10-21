import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import static java.awt.event.KeyEvent.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.sound.sampled.*;

class AWT extends Panel implements Runnable, MouseListener, MouseMotionListener, KeyListener, WindowListener {

  public void windowDeactivated(WindowEvent paramWindowEvent) {}
  public void windowClosing(WindowEvent paramWindowEvent) { System.exit(0); }
  public void windowOpened(WindowEvent paramWindowEvent) {}
  public void windowClosed(WindowEvent paramWindowEvent) {}
  public void windowDeiconified(WindowEvent paramWindowEvent) {}
  public void windowActivated(WindowEvent paramWindowEvent) {}
  public void windowIconified(WindowEvent paramWindowEvent) {}
  public void toggleFullScreen() {
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    if(gd.isFullScreenSupported()) {
      // exit fullscreen
      if(gd.getFullScreenWindow() == this.window) {
        this.window.setCursor(Cursor.getDefaultCursor());
        gd.setFullScreenWindow(null);
      }
      // enter fullscreen
      else {
        gd.setFullScreenWindow(this.window);
        this.window.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "invisible"));
      }
    }
  }

  private int logical_w, logical_h;
  private Frame window;
  private Image scene_img;
  private Graphics scene_g;
  private Thread gameThread;
  private Color bg = new Color(160, 208, 176);
  private Image backbuffer;
  private Font font, small_font;
  private boolean stretched;
  private boolean paused;
  private static long keyevent_glitch_workaround_t0; // I'm observing an issue where I sometime get random key events on start (e.g. VK_C, VK_S, VK_F). This mitigates this.
  private int target_dt = 55;
  private int delay = target_dt;

  private int ship_animation;
  private Image ship[] = new Image[2];
  private Clip explosion;
  
  private Gamepad gamepad = new Gamepad();
  private boolean key_held[] = new boolean[256]; // stores held state of KeyEvent for the VK range I care about
  private boolean mouse_left_button_held, mouse_right_button_held;
  private int mouse_x, mouse_y;
  
  private Game game;

  public static void main(String[] args) { new AWT(args); } public AWT(String [] args) {
    keyevent_glitch_workaround_t0 = System.currentTimeMillis();
    
    // parse args
    boolean arg_fullscreen = false;
    boolean arg_hq = false;
    for(int i = 0; i < args.length; i++) {
      switch(args[i]) {
        case "stretch": this.stretched = true; break;
        case "fullscreen": arg_fullscreen = true; break;
        case "hq": arg_hq = true; break;
        case "period": this.target_dt = Integer.parseInt(args[i+1]); break; // can crash and I like it that way
      }
    }

    this.addKeyListener(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.setBackground(bg);

    window = new Frame("Jet Slalom Resurrected");
    window.addWindowListener(this);
    window.setBackground(bg);
    window.add(this, BorderLayout.CENTER);
    window.setSize(800, 600);
    window.setVisible(true);
    this.requestFocus();

    this.set_logical_size(arg_hq? 6 : 1);
    try {
      this.explosion = AudioSystem.getClip();
      this.explosion.open(AudioSystem.getAudioInputStream(new BufferedInputStream(AWT.class.getResourceAsStream("/res/explosion.wav")))); // the buffer is necessary when inside a jar
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    game = new Game();
    game.startGame(false, false);
    this.gameThread = new Thread(this);
    this.gameThread.start();
    if(arg_fullscreen) this.toggleFullScreen();
  }

  private void set_logical_size(double scale) {
    this.logical_w = (int)(320 * scale);
    this.logical_h = (int)(200 * scale);
    this.scene_img = this.createImage(this.logical_w, this.logical_h);
    this.scene_g = scene_img.getGraphics();
    if(this.scene_g instanceof Graphics2D) { ((Graphics2D)this.scene_g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); }
    this.scene_g.setColor(new Color(0,128,128));
    this.scene_g.fillRect(0, 0, this.logical_w, this.logical_h);
    try {
      int image_scale = (int)(this.logical_w * 0.7 * 120 / 1.6 / 320);
      this.ship[0] = ImageIO.read(AWT.class.getResourceAsStream("/res/jiki.gif")).getScaledInstance(image_scale * 2, image_scale / 4, Image.SCALE_FAST);
      this.ship[1] = ImageIO.read(AWT.class.getResourceAsStream("/res/jiki2.gif")).getScaledInstance(image_scale * 2, image_scale / 4, Image.SCALE_FAST);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void keyPressed(KeyEvent e) {
    if(System.currentTimeMillis() < keyevent_glitch_workaround_t0 + 100) return;
    int keycode = e.getKeyCode();
    if(keycode >= 0 && keycode < key_held.length) key_held[keycode] = true;
    if(keycode == VK_ESCAPE) System.exit(0);
    if(game.title_mode) {
      if(keycode == VK_SPACE || keycode == VK_ENTER) game.startGame(true, false);
      if(keycode == VK_W || keycode == VK_UP || keycode == VK_C) game.startGame(true, true);
      if(keycode == VK_T) { game.prevScore = 110000; game.contNum = 100; game.startGame(true, true); } // some sort of cheat
    }
  }
  public void keyReleased(KeyEvent e) {
    if(System.currentTimeMillis() < keyevent_glitch_workaround_t0 + 100) return;
    int keycode = e.getKeyCode();
    if(keycode >= 0 && keycode < key_held.length) key_held[keycode] = false;
    if(keycode == VK_F) this.toggleFullScreen();
    if(keycode == VK_P) this.paused = !this.paused;
    if(keycode == VK_S) this.stretched = !this.stretched;
    if(keycode == VK_ADD) this.target_dt+=5;
    if(keycode == VK_SUBTRACT) this.target_dt-=5;
    if(keycode == VK_H) this.set_logical_size(this.logical_w == 320? 6 : 1);
  }
  public void keyTyped(KeyEvent paramKeyEvent) { }

  public void mousePressed(MouseEvent e) {
    if(System.currentTimeMillis() < keyevent_glitch_workaround_t0 + 100) return;
    int mod = e.getModifiersEx();
    this.mouse_left_button_held = (mod & BUTTON1_DOWN_MASK) == BUTTON1_DOWN_MASK;
    this.mouse_right_button_held = (mod & BUTTON3_DOWN_MASK) == BUTTON3_DOWN_MASK;
    if(game.title_mode) { game.startGame(true, e.getButton() == MouseEvent.BUTTON3); }
  }
  public void mouseReleased(MouseEvent e) {
    if(System.currentTimeMillis() < keyevent_glitch_workaround_t0 + 100) return;
    int mod = e.getModifiersEx();
    this.mouse_left_button_held = (mod & BUTTON1_DOWN_MASK) == BUTTON1_DOWN_MASK;
    this.mouse_right_button_held = (mod & BUTTON3_DOWN_MASK) == BUTTON3_DOWN_MASK;
  }
  public void mouseMoved(MouseEvent e) {
    this.mouse_x = e.getX();
    this.mouse_y = e.getY();
  }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseClicked(MouseEvent e) { }
  public void mouseDragged(MouseEvent e) { }
  
  public void run() {
    long t0 = System.currentTimeMillis(), t1 = t0;
    while (this.gameThread == Thread.currentThread()) {
      t0 = t1; t1 = System.currentTimeMillis(); long dt = t1 - t0;
      // gamepad: note that the external library I found does not seem to support plug and play at least in my Linux environment (i.e. the gamepad must be plugged before the game starts)
      boolean gamepad_left = false, gamepad_right = false; double dead_zone = .05;
      gamepad.poll();
      if(gamepad.lx < -dead_zone) gamepad_left = true;
      if(gamepad.lx > dead_zone) gamepad_right = true;
      if(gamepad.rx < -dead_zone) gamepad_left = true;
      if(gamepad.rx > dead_zone) gamepad_right = true;
      if(gamepad.left) gamepad_left = true;
      if(gamepad.right) gamepad_right = true;
      if(gamepad.left_shoulder) gamepad_left = true;
      if(gamepad.right_shoulder) gamepad_right = true;
      if(gamepad.left_trigger > 0) gamepad_left = true;
      if(gamepad.right_trigger > 0) gamepad_right = true;
      // advanced system commands
      if(gamepad.left_shoulder && gamepad.right_shoulder && gamepad.start && gamepad.select) System.exit(0);
      if(gamepad.select) {
        if(gamepad.l3 && gamepad.n_l3) this.set_logical_size(this.logical_w == 320? 6 : 1);
        if(gamepad.left_shoulder && gamepad.n_left_shoulder) this.target_dt-=5;
        if(gamepad.right_shoulder && gamepad.n_right_shoulder) this.target_dt+=5;
      }
      // normal system commands
      else {
        if(gamepad.l3 && gamepad.n_l3) this.toggleFullScreen();
        if(gamepad.r3 && gamepad.n_r3) this.stretched = !this.stretched;
      }
      // title command
      if(game.title_mode) {
        if((gamepad.south_maybe && gamepad.n_south_maybe) || (gamepad.north_maybe && gamepad.n_north_maybe) || (gamepad.west_maybe && gamepad.n_west_maybe) || (gamepad.east_maybe && gamepad.n_east_maybe) || (gamepad.up && gamepad.n_up)) game.startGame(true, true); // continue
        if(gamepad.select) {
          if(gamepad.start && gamepad.n_start) { game.prevScore = 110000; game.contNum = 100; game.startGame(true, true); } // some sort of cheat
        } else {
          if((gamepad.start && gamepad.n_start) || (gamepad.down && gamepad.n_down)) game.startGame(true, false); // restart
        }
      }
      // play command
      else {
        if(gamepad.start && gamepad.n_start) this.paused = !this.paused;
      }

      boolean keyboard_left = key_held[VK_LEFT] || key_held[VK_J] || key_held[VK_A];
      boolean keyboard_right = key_held[VK_RIGHT] || key_held[VK_L] || key_held[VK_D];

      if(game.title_mode && this.paused) this.paused = false;
      if(this.hasFocus() && !this.paused) {
        game.tick(gamepad_left | mouse_left_button_held | keyboard_left, gamepad_right | mouse_right_button_held | keyboard_right);
        prt();
      }
      
      // letterbox scaling (i.e. respects aspect ratio)
      int b_w = this.getWidth(); int b_h = this.getHeight(); int s_w = this.logical_w; int s_h = this.logical_h;
      double scale; if((b_w / (double)b_h) > (s_w / (double)s_h)) scale = b_h / (double)s_h; else scale = b_w / (double)s_w;
      int x = (int)((b_w - s_w * scale) / 2); int y = (int)((b_h - s_h * scale) / 2);
      int w = (int)(s_w*scale); int h = (int)(s_h*scale);
      if(this.stretched) { x = 0; y = 0; w = b_w; h = b_h; }

      // awt needs a backbuffer, there is not final presentation, it paints live
      if(this.backbuffer == null || this.backbuffer.getWidth(null) != b_w || this.backbuffer.getHeight(null) != b_h) {
        this.backbuffer = new BufferedImage(b_w, b_h, BufferedImage.TYPE_INT_RGB);
        this.font = null;
      }
      
      Graphics g = this.backbuffer == null? this.getGraphics() : this.backbuffer.getGraphics();
      if(g instanceof Graphics2D) {
        //((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); if(desktopHints != null) ((Graphics2D)g).setRenderingHints(desktopHints);
      }
      g.setColor(bg);
      if(x > 0) { g.fillRect(0, 0, x, b_h); g.fillRect(x+w, 0, x, b_h); }
      if(y > 0) { g.fillRect(0, 0, b_w, y); g.fillRect(0, y+h, b_w, y); }
      g.drawImage(this.scene_img,x,y,w, h, Color.WHITE, null);

      // overlay, now that I'm using drawString on the window size surface for all text instead of widgets, I need to ensure the font scales
      try {
        if(this.font == null) {
          this.font = Font.createFont(Font.TRUETYPE_FONT, AWT.class.getResourceAsStream("/res/OpenSans-Regular.ttf"));
          this.font = this.font.deriveFont(Font.PLAIN, b_h / 25);
        }
        // in the event that the window is very thin, then we'll have to reduce the font so the longuest line fits
        g.setFont(this.font); FontMetrics fm = g.getFontMetrics();
        {
          String msg = game.contNum == 0? "Original 1997 version by MR-C" : "Your Hi-score: 000000      Continue penalty: 000000";
          while(fm.stringWidth(msg) > b_w) {
            float size = this.font.getSize2D() * .9f;
            this.font = this.font.deriveFont(Font.PLAIN, size);
            g.setFont(this.font); fm = g.getFontMetrics();
            if(size < 6) break;
          }
        }
        this.small_font = this.font.deriveFont(Font.PLAIN, this.font.getSize2D() * .6f);
        g.setColor(Color.white);
        g.drawString("Your Hi-score:" + game.hiscore, 2*fm.getDescent()/3, b_h - fm.getDescent());
        { String msg = String.format("Period: %dms (%dms)", this.target_dt, dt); g.drawString(msg, b_w - 2*fm.getDescent()/3 - fm.stringWidth(msg), b_h - fm.getDescent()); }
        String score = "Score:" + game.score;
        String penalty = "Continue penalty:" + game.contNum * 1000;
        int score_w = fm.stringWidth(score); int penalty_w = fm.stringWidth(penalty); int padding = b_w / 10; int total_w = game.contNum > 0? score_w + padding + penalty_w : score_w; int offset = (b_w - total_w) / 2;
        g.drawString(score, offset, fm.getAscent()); offset += score_w + padding;
        if(game.contNum > 0) g.drawString(penalty, offset, fm.getAscent());
        if(game.title_mode) {
          int line_h = fm.getHeight(); g.setFont(this.small_font); int small_line_h = g.getFontMetrics().getHeight(); g.setFont(this.font);
          int spacing = 5, small_spacing = 3;
          int n = 4, small_n = 6 + (gamepad.available? 4 : 0);
          offset = (b_h - ((line_h + spacing) * n + (small_line_h + small_spacing) * small_n)) / 2;
          offset += fm.getAscent();
          { String msg = "Jet Slalom Resurrected"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += line_h + spacing; }
          { String msg = "by David Lareau in 2025"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += line_h + spacing; }
          { String msg = "Original 1997 version by MR-C"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += line_h + spacing; }
          { offset += line_h + spacing; }
          g.setFont(this.small_font); fm = g.getFontMetrics();
          { String msg = "-- Keyboard --"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
          { String msg = "(F)ullscreen, (H)ighRez, (S)tretch, Speed(num+/num-)"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
          { String msg = "Restart(Spacebar/Enter), (C)ontinue(up/W), Chea(T)"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
          { String msg = "(P)ause, Quit(ESC), Ship(Left/Right/A/D/J/L)"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
          { String msg = "-- Mouse --"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
          { String msg = "Ship(L/R), Restart(L), Continue(R)"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
          if(gamepad.available) {
            { String msg = "-- Gamepad --"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
            { String msg = "Fullscreen(L3), HighRez(Select+L3), Stretch(R3), Speed(Select+LB/RB)"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
            { String msg = "Re(Start/Down), Resume(A/B/X/Y/Up), Cheat(Select+Start)"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
            { String msg = "Pause(Start), Quit(LB+RB+Start+Select), Ship(Sticks/Dpad/Shoulders)"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += small_line_h + small_spacing; }
          }
        }
        if(this.paused || !this.hasFocus()) {
          g.setColor(Color.red);
          offset = (int)(b_h * (System.currentTimeMillis() % 10000) / 10000.0);
          { String msg = this.hasFocus()? "Paused" : "Lost Keyboard Input Focus"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); }
        }
      } catch(Exception e) {
        e.printStackTrace();
      }

      // final double buffer blit
      if(this.backbuffer != null) { g = this.getGraphics(); g.drawImage(this.backbuffer, 0, 0, null); }
      this.getToolkit().sync();
      if(dt > target_dt) { this.delay--; } // framerate too low, try faster
      if(dt < target_dt) { this.delay++; } // framerate too high, try slower
      if(this.delay < 1) this.delay = 1;
      { try { Thread.sleep(this.delay); } catch (InterruptedException e) { e.printStackTrace(); } }
    }
  }

  // draw game primitives
  private static int[] buffer_polyX = new int[8];
  private static int[] buffer_polyY = new int[8];
  private void drawPolygon(Graphics g, Game.Face face) {
    Game.DPoint3[] points = face.points;
    double d1 = (points[1]).x - (points[0]).x;
    double d2 = (points[1]).y - (points[0]).y;
    double d3 = (points[2]).x - (points[0]).x;
    double d4 = (points[2]).y - (points[0]).y;
    float f = (float)(Math.abs(d1 * d4 - d2 * d3) / face.maxZ);
    g.setColor(new Color(C.fr(face.rgb)*f, C.fg(face.rgb)*f, C.fb(face.rgb)*f));
    drawPolygon(g, points);
  }
  private void drawPolygon(Graphics g, Game.DPoint3[] points) {
    double d1 = this.logical_w / 320.0;
    double d2 = this.logical_h / 200.0;
    for (byte b = 0; b < points.length; b++) {
      Game.DPoint3 point = points[b];
      double d3 = 120.0 / (1.0 + 0.6 * point.z);
      double d4 = game.nowCos * point.x + game.nowSin * (point.y - 2.0);
      double d5 = -game.nowSin * point.x + game.nowCos * (point.y - 2.0) + 2.0;
      buffer_polyX[b] = (int)(d4 * d1 * d3) + this.logical_w / 2;
      buffer_polyY[b] = (int)(d5 * d2 * d3) + this.logical_h / 2;
    }
    g.fillPolygon(buffer_polyX, buffer_polyY, points.length);
  }
  private void draw_obstacle(Graphics g, Game.Obstacle o) {
    drawPolygon(g, o.faces[0]);
    drawPolygon(g, o.faces[1]);
  }

  private void prt() {
    this.scene_g.setColor(new Color(game.rounds[game.round].getSkyRGB()));
    this.scene_g.fillRect(0, 0, this.logical_w, this.logical_h);
    this.scene_g.setColor(new Color(game.rounds[game.round].getGroundRGB())); drawPolygon(this.scene_g, game.ground_points);
    for(Game.Obstacle obstacle : game.obstacles) draw_obstacle(this.scene_g, obstacle);
    this.ship_animation++;
    if(!game.title_mode) {
      int y = 24 * this.logical_h / 200;
      Image image = this.ship[this.ship_animation % 4 > 1? 1 : 0];
      if (this.ship_animation % 12 > 6) y = 22 * this.logical_h / 200;
      if (game.score < 200) y = (12 + game.score / 20) * this.logical_h / 200;
      if (game.damaged < 10) this.scene_g.drawImage(image, (this.logical_w / 2) - image.getWidth(null)/2, this.logical_h - y, null);
      if (game.damaged > 0) {
        if(game.damaged <= 20) {
          if(game.damaged == 1 && this.explosion != null) { this.explosion.stop(); this.explosion.setFramePosition(0); this.explosion.start(); }
          this.scene_g.setColor(new Color(255, 255 - game.damaged * 12, 240 - game.damaged * 12));
          int i = game.damaged * 8 * this.logical_w / 320;
          int j = game.damaged * 4 * this.logical_h / 200;
          this.scene_g.fillOval((this.logical_w / 2) - i, 186 * this.logical_h / 200 - j, i * 2, j * 2);
        }
      }
    }
  }

}