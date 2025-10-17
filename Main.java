import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import static java.awt.event.KeyEvent.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.sound.sampled.*;

class Main extends Panel implements Runnable, MouseListener, MouseMotionListener, KeyListener, WindowListener {

  public static final int width = 320;
  public static final int height = 200;
  public static int getRandom() { return random.nextInt(Integer.MAX_VALUE); } private static Random random = new Random();

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

  // sin/cos lookup tables
  private static double[] si = new double[128];
  private static double[] co = new double[128];
  // preallocate buffers
  private static int[] buffer_polyX = new int[8];
  private static int[] buffer_polyY = new int[8];
  // draw utilities
  public static double nowSin;
  public static double nowCos;
  synchronized static void drawPolygon(Graphics g, Face face) {
    DPoint3[] points = face.points;
    double d1 = (points[1]).x - (points[0]).x;
    double d2 = (points[1]).y - (points[0]).y;
    double d3 = (points[2]).x - (points[0]).x;
    double d4 = (points[2]).y - (points[0]).y;
    float f = (float)(Math.abs(d1 * d4 - d2 * d3) / face.maxZ);
    g.setColor(new Color(ARGB.fr(face.rgb)*f, ARGB.fg(face.rgb)*f, ARGB.fb(face.rgb)*f));
    drawPolygon(g, points);
  }
  synchronized static void drawPolygon(Graphics g, DPoint3[] points) {
    double d1 = Main.width / 320.0;
    double d2 = Main.height / 200.0;
    for (byte b = 0; b < points.length; b++) {
      DPoint3 point = points[b];
      double d3 = 120.0 / (1.0 + 0.6 * point.z);
      double d4 = nowCos * point.x + nowSin * (point.y - 2.0);
      double d5 = -nowSin * point.x + nowCos * (point.y - 2.0) + 2.0;
      buffer_polyX[b] = (int)(d4 * d1 * d3) + Main.width / 2;
      buffer_polyY[b] = (int)(d5 * d2 * d3) + Main.height / 2;
    }
    g.fillPolygon(buffer_polyX, buffer_polyY, points.length);
  }
  
  private RoundManager[] rounds = new RoundManager[] { new NormalRound(8000, ARGB.rgb(0, 160, 255), ARGB.rgb(0, 200, 64), 4), new NormalRound(12000, ARGB.rgb(240, 160, 160), ARGB.rgb(64, 180, 64), 3), new NormalRound(25000, ARGB.black, ARGB.rgb(0, 128, 64), 2), new RoadRound(40000, ARGB.rgb(0, 180, 240), ARGB.rgb(0, 200, 64), false), new RoadRound(100000, ARGB.gray(192), ARGB.rgb(64, 180, 64), true), new NormalRound(1000000, ARGB.black, ARGB.rgb(0, 128, 64), 1) };
  private DPoint3[] ground_points = new DPoint3[] { new DPoint3(-100.0, 2.0, 28.0), new DPoint3(-100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 28.0) };
  private LinkedList<Obstacle> obstacles = new LinkedList<>();
  private double vx; // ship's left/right movement
  private int round;
  private int damaged;
  private int score, prevScore, hiscore, contNum;
  private int ship_animation;
  private boolean paused;
  private boolean title_mode;

  private Frame window;
  private Image scene_img;
  private Graphics scene_g;
  private Thread gameThread;
  private Color bg = new Color(160, 208, 176);
  private Image backbuffer;
  private Font font;
  private boolean stretched;
  private static long keyevent_glitch_workaround_t0; // I'm observing an issue where I sometime get random key events on start (e.g. VK_C, VK_S, VK_F). This mitigates this.

  private Image ship[] = new Image[2];
  private Clip explosion;
  
  private Gamepad gamepad = new Gamepad();
  private boolean key_held[] = new boolean[256]; // stores held state of KeyEvent for the VK range I care about
  private boolean mouse_left_button_held, mouse_right_button_held;
  private int mouse_x, mouse_y;

  public static void main(String[] args) { new Main(); } public Main() {
    keyevent_glitch_workaround_t0 = System.currentTimeMillis();

    for(int b = 1; b < this.rounds.length; b++) this.rounds[b].setPrevRound(this.rounds[b - 1]);
    for(int i = 0; i < si.length; i++) {
      si[i] = Math.sin(Math.PI * (i / (double)si.length));
      co[i] = Math.cos(Math.PI * (i / (double)si.length));
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

    this.scene_img = this.createImage(width, height);
    this.scene_g = scene_img.getGraphics();
    if(this.scene_g instanceof Graphics2D) { ((Graphics2D)this.scene_g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); }
    this.scene_g.setColor(new Color(0,128,128));
    this.scene_g.fillRect(0, 0, width, height);
    try {
      int scale = (int)(this.width * 0.7 * 120 / 1.6 / 320);
      this.ship[0] = ImageIO.read(new File("res/jiki.gif")).getScaledInstance(scale * 2, scale / 4, Image.SCALE_FAST);
      this.ship[1] = ImageIO.read(new File("res/jiki2.gif")).getScaledInstance(scale * 2, scale / 4, Image.SCALE_FAST);
      this.explosion = AudioSystem.getClip();
      this.explosion.open(AudioSystem.getAudioInputStream(new File("res/explosion.wav")));
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    this.startGame(false, false);
    this.gameThread = new Thread(this);
    this.gameThread.start();
  }

  public void keyPressed(KeyEvent e) {
    if(System.currentTimeMillis() < keyevent_glitch_workaround_t0 + 100) return;
    int keycode = e.getKeyCode();
    if(keycode >= 0 && keycode < key_held.length) key_held[keycode] = true;
    if(keycode == VK_ESCAPE) System.exit(0);
    if(this.title_mode && (keycode == VK_SPACE || keycode == VK_ENTER || keycode == VK_W || keycode == VK_UP || keycode == VK_C)) startGame(true, !(keycode != VK_C));
    if(this.title_mode && keycode == VK_T) { this.prevScore = 110000; this.contNum = 100; startGame(true, true); } // is this some sort of cheat?
  }
  public void keyReleased(KeyEvent e) {
    if(System.currentTimeMillis() < keyevent_glitch_workaround_t0 + 100) return;
    int keycode = e.getKeyCode();
    if(keycode >= 0 && keycode < key_held.length) key_held[keycode] = false;
    if(keycode == VK_F) this.toggleFullScreen();
    if(keycode == VK_P) this.paused = !this.paused;
    if(keycode == VK_S) this.stretched = !this.stretched;
  }
  public void keyTyped(KeyEvent paramKeyEvent) { }

  public void mousePressed(MouseEvent e) {
    if(System.currentTimeMillis() < keyevent_glitch_workaround_t0 + 100) return;
    int mod = e.getModifiersEx();
    this.mouse_left_button_held = (mod & BUTTON1_DOWN_MASK) == BUTTON1_DOWN_MASK;
    this.mouse_right_button_held = (mod & BUTTON3_DOWN_MASK) == BUTTON3_DOWN_MASK;
    if(this.title_mode) startGame(true, false);
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
  
  void ship_input(boolean left, boolean right) {
    // turn
    if(this.damaged == 0 && !this.title_mode) {
      if(right) this.vx = Math.max(this.vx - 0.1, -.6);
      if(left) this.vx = Math.min(this.vx + 0.1, .6);
    }
    // stabilize back
    if(!left && !right) {
      if(this.vx < 0.0) this.vx = Math.min(this.vx + .025, 0);
      if(this.vx > 0.0) this.vx = Math.max(this.vx - .025, 0);
    }
  }

  void moveObstacle() {
    int i = (int)(Math.abs(this.vx) * 100.0);
    nowSin = si[i];
    nowCos = co[i];
    if(this.vx > 0.0) nowSin = -nowSin;
    ListIterator<Obstacle> iter = this.obstacles.listIterator(); while(iter.hasNext()) { Obstacle obstacle = iter.next();
      obstacle.move(this.vx, 0.0, -1.0);
      DPoint3[] points = obstacle.points;
      if((points[0]).z <= 1.1) {
        double d = 0.7 * nowCos;
        if (-d < (points[2]).x && (points[0]).x < d) this.damaged++;
        iter.remove();
      }
    }
    this.rounds[this.round].move(this.vx);
    { Obstacle obstacle = this.rounds[this.round].generateObstacle(); if(obstacle != null) this.obstacles.addFirst(obstacle); }
  }

  public void startGame(boolean play_mode, boolean resume) {
    this.title_mode = !play_mode;
    obstacles.clear();
    for(RoundManager r : this.rounds) r.init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0;
    if(!resume) { this.contNum = 0; } else {
      while (this.prevScore >= this.rounds[this.round].getNextRoundScore()) this.round++;
      if (this.round > 0) { this.score = this.rounds[this.round - 1].getNextRoundScore(); this.contNum++; }
    }
  }

  void prt() {
    this.scene_g.setColor(new Color(this.rounds[this.round].getSkyRGB()));
    this.scene_g.fillRect(0, 0, this.width, this.height);
    if(!this.title_mode) this.score += 20;
    this.scene_g.setColor(new Color(this.rounds[this.round].getGroundRGB())); drawPolygon(this.scene_g, this.ground_points);
    for(Obstacle obstacle : obstacles) draw_obstacle(this.scene_g, obstacle);
    this.ship_animation++;
    if(!this.title_mode) {
      int i = 24 * this.height / 200;
      Image image = this.ship[this.ship_animation % 4 > 1? 1 : 0];
      if (this.ship_animation % 12 > 6) i = 22 * this.height / 200;
      if (this.score < 200) i = (12 + this.score / 20) * this.height / 200;
      if (this.damaged < 10) this.scene_g.drawImage(image, (width / 2) - image.getWidth(null)/2, this.height - i, null);
      if (this.damaged > 0) putbomb();
    }
    if(this.title_mode) {
      this.vx = 0.0;
    }
  }

  void putbomb() {
    if(this.damaged > 20) {
      if(!this.title_mode) this.prevScore = this.score;
      if(this.score - this.contNum * 1000 > this.hiscore && !this.title_mode) this.hiscore = this.score - this.contNum * 1000;
      this.title_mode = true;
    } else {
      if(this.damaged == 1 && this.explosion != null) { this.explosion.stop(); this.explosion.setFramePosition(0); this.explosion.start(); }
      this.scene_g.setColor(new Color(255, 255 - this.damaged * 12, 240 - this.damaged * 12));
      int i = this.damaged * 8 * this.width / 320;
      int j = this.damaged * 4 * this.height / 200;
      this.scene_g.fillOval((width / 2) - i, 186 * this.height / 200 - j, i * 2, j * 2);
      this.damaged++;
    }
  }
  
  void draw_obstacle(Graphics g, Obstacle o) {
    drawPolygon(g, o.faces[0]);
    drawPolygon(g, o.faces[1]);
  }
  
  public void run() {
    obstacles.clear();
    for(RoundManager r : this.rounds) r.init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0;
    this.title_mode = true;
    long t0 = System.currentTimeMillis(), t1 = t0, target_dt = 55;
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
      if(gamepad.select && gamepad.n_select) this.toggleFullScreen();
      if(this.title_mode && ((gamepad.start && gamepad.n_start) || (gamepad.south_maybe && gamepad.n_south_maybe) || (gamepad.north_maybe && gamepad.n_north_maybe) || (gamepad.west_maybe && gamepad.n_west_maybe) || (gamepad.east_maybe && gamepad.n_east_maybe))) startGame(true, false);

      boolean keyboard_left = key_held[VK_LEFT] || key_held[VK_J] || key_held[VK_A];
      boolean keyboard_right = key_held[VK_RIGHT] || key_held[VK_L] || key_held[VK_D];

      if(this.hasFocus() && !paused) {
        if(this.rounds[this.round].isNextRound(this.score)) this.round++;
        ship_input(gamepad_left | mouse_left_button_held | keyboard_left, gamepad_right | mouse_right_button_held | keyboard_right);
        moveObstacle();
        prt();
      }
      
      // letterbox scaling (i.e. respects aspect ratio)
      int b_w = this.getWidth(); int b_h = this.getHeight(); int s_w = width; int s_h = height;
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
          this.font = Font.createFont(Font.TRUETYPE_FONT, new File("res/OpenSans-Regular.ttf"));
          this.font = this.font.deriveFont(Font.PLAIN, b_h / 25);
        }
        // in the event that the window is very thin, then we'll have to reduce the font so the longuest line fits
        g.setFont(this.font); FontMetrics fm = g.getFontMetrics();
        {
          String msg = contNum == 0? "Original 1997 version by MR-C" : "Your Hi-score: 000000      Continue penalty: 000000";
          while(fm.stringWidth(msg) > b_w) {
            float size = this.font.getSize2D() * .9f;
            this.font = this.font.deriveFont(Font.PLAIN, size);
            g.setFont(font); fm = g.getFontMetrics();
            if(size < 6) break;
          }
        }
        g.setColor(Color.white);
        g.drawString("Your Hi-score:" + this.hiscore, 2*fm.getDescent()/3, b_h - fm.getDescent());
        { String msg = "Period:" + dt + "ms"; g.drawString(msg, b_w - 2*fm.getDescent()/3 - fm.stringWidth(msg), b_h - fm.getDescent()); }
        String score = "Score:" + this.score;
        String penalty = "Continue penalty:" + this.contNum * 1000;
        int score_w = fm.stringWidth(score); int penalty_w = fm.stringWidth(penalty); int padding = b_w / 10; int total_w = this.contNum > 0? score_w + padding + penalty_w : score_w; int offset = (b_w - total_w) / 2;
        g.drawString(score, offset, fm.getAscent()); offset += score_w + padding;
        if(this.contNum > 0) g.drawString(penalty, offset, fm.getAscent());
        if(this.title_mode) {
          int line_h = fm.getHeight();
          int spacing = 5;
          int n = 3;
          offset = (b_h - ((line_h + spacing) * n - spacing)) / 2;
          { String msg = "Jet Slalom Resurrected"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += line_h + spacing; }
          { String msg = "by David Lareau in 2025"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += line_h + spacing; }
          { String msg = "Original 1997 version by MR-C"; int line_w = fm.stringWidth(msg); g.drawString(msg, (b_w - line_w) / 2, offset); offset += line_h + spacing; }
        }
        if(paused || !this.hasFocus()) {
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
      long dt_bust = 0; // if(dt > target_dt) { dt_bust = dt - target_dt; } // this only fixes the next frame, then the bust comes back
      if(target_dt - dt_bust > 0) { try { Thread.sleep(target_dt - dt_bust); } catch (InterruptedException e) { e.printStackTrace(); } }
    }
  }

}