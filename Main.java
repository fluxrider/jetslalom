import java.awt.*;
import java.awt.event.*;
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
  private double[] si = new double[128];
  private double[] co = new double[128];

  private int gameMode;
  private static final int PLAY_MODE = 0;
  private static final int TITLE_MODE = 1;
  
  private Frame window;
  private Label hiScoreLabel;
  private Label lblContinue;
  private NumberLabel scoreWin;
  private Gamepad gamepad = new Gamepad();

  private Image ship[] = new Image[2];
  private Clip explosion;

  private RoundManager[] rounds = new RoundManager[] { new NormalRound(8000, new Color(0, 160, 255), new Color(0, 200, 64), 4), new NormalRound(12000, new Color(240, 160, 160), new Color(64, 180, 64), 3), new NormalRound(25000, Color.black, new Color(0, 128, 64), 2), new RoadRound(40000, new Color(0, 180, 240), new Color(0, 200, 64), false), new RoadRound(100000, Color.lightGray, new Color(64, 180, 64), true), new NormalRound(1000000, Color.black, new Color(0, 128, 64), 1) };
  private DPoint3[] ground_points = new DPoint3[] { new DPoint3(-100.0, 2.0, 28.0), new DPoint3(-100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 28.0) };
  private LinkedList<Obstacle> obstacles = new LinkedList<>();

  private Image scene_img;
  private Graphics scene_g;
  
  public static void main(String[] args) { new Main(); } public Main() {
    Color bg = new Color(160, 208, 176);
    for (byte b = 1; b < this.rounds.length; b++) this.rounds[b].setPrevRound(this.rounds[b - 1]);
    for(int i = 0; i < si.length; i++) {
      si[i] = Math.sin(Math.PI * (i / (double)si.length));
      co[i] = Math.cos(Math.PI * (i / (double)si.length));
    }
    
    this.addKeyListener(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.setBackground(bg);

    this.hiScoreLabel = new Label("Your Hi-score:0         ");
    this.lblContinue = new Label("            ");
    this.scoreWin = new NumberLabel(64, 12);
    Panel npanel = new Panel(); npanel.add(new Label("Score:")); npanel.add(this.scoreWin); npanel.add(new Label("Continue penalty:")); npanel.add(this.lblContinue);
    
    window = new Frame("Jet Slalom Resurrected");
    window.addWindowListener(this);
    window.setBackground(bg);
    window.add(this, BorderLayout.CENTER);
    window.add(this.hiScoreLabel, BorderLayout.SOUTH);
    window.add(npanel, BorderLayout.NORTH);
    window.setSize(800, 600);
    window.setVisible(true);
    this.requestFocus();

    this.scene_img = this.createImage(width, height);
    this.scene_g = scene_img.getGraphics();
    this.scene_g.setColor(new Color(0,128,128));
    this.scene_g.fillRect(0, 0, width, height);
    this.mywidth2 = (int)(this.width * this.mywidth * 120 / 1.6 / 320);
    try {
      this.ship[0] = ImageIO.read(new File("res/jiki.gif")).getScaledInstance(mywidth2 * 2, mywidth2 / 4, Image.SCALE_FAST);
      this.ship[1] = ImageIO.read(new File("res/jiki2.gif")).getScaledInstance(mywidth2 * 2, mywidth2 / 4, Image.SCALE_FAST);
      this.explosion = AudioSystem.getClip();
      this.explosion.open(AudioSystem.getAudioInputStream(new File("res/explosion.wav")));
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    this.start();
    this.startGame(1, false);
  }
  


  private double vx = 0.0D;

  private double mywidth = 0.7D;

  private int mywidth2;

  private int score;

  private int prevScore;

  private int hiscore;

  private int shipCounter;

  private int contNum;

  private boolean isContinue = false;

  private boolean registMode = false;

  private int mouseX = 0;

  private int mouseY = 0;

  private boolean isInPage = false;

  private Thread gameThread;

  private boolean isLoaded = false;

  private int round;


  private boolean rFlag = false;

  private boolean lFlag = false;

  private boolean scFlag = true;

  private int damaged;

  private char[] memInfo = new char[8];

  private Runtime runtime = Runtime.getRuntime();

  public void stop() {
    this.gameThread = null;
    this.registMode = false;
    this.gameMode = TITLE_MODE;
  }

  void keyEvent(int keycode, boolean held) {
    if (keycode == VK_RIGHT || keycode == VK_L || keycode == VK_D) this.rFlag = held;
    if (keycode == VK_LEFT || keycode == VK_J || keycode == VK_A) this.lFlag = held;
    if (!held) return;
    if(keycode == VK_F) this.toggleFullScreen();
    if(keycode == VK_ESCAPE) System.exit(0);
    if (keycode == VK_G) System.gc();
    if (this.gameMode != PLAY_MODE && (keycode == VK_SPACE || keycode == VK_ENTER || keycode == VK_W || keycode == VK_UP || keycode == VK_C)) startGame(PLAY_MODE, !(keycode != VK_C));
    // TODO is this some sort of cheat?
    if (this.gameMode != PLAY_MODE && keycode == VK_T) {
      this.prevScore = 110000;
      this.contNum = 100;
      startGame(PLAY_MODE, true);
    }
  }

  void keyOperate() {
    boolean bool1 = this.rFlag;
    boolean bool2 = this.lFlag;
    if (this.gameMode == PLAY_MODE) {
      int i = 0;
      if (bool1)
        i |= 0x2;
      if (bool2)
        i |= 0x1;
    }
    if (this.damaged == 0 && this.gameMode == PLAY_MODE) {
      if (bool1)
        this.vx -= 0.1D;
      if (bool2)
        this.vx += 0.1D;
      if (this.vx < -0.6D)
        this.vx = -0.6D;
      if (this.vx > 0.6D)
        this.vx = 0.6D;
    }
    if (!bool2 && !bool1) {
      if (this.vx < 0.0D) {
        this.vx += 0.025D;
        if (this.vx > 0.0D)
          this.vx = 0.0D;
      }
      if (this.vx > 0.0D) {
        this.vx -= 0.025D;
        if (this.vx < 0.0D)
          this.vx = 0.0D;
      }
    }
  }

  public void mouseReleased(MouseEvent paramMouseEvent) {
    this.rFlag = false;
    this.lFlag = false;
  }

  public void keyPressed(KeyEvent paramKeyEvent) {
    keyEvent(paramKeyEvent.getKeyCode(), true);
  }

  void moveObstacle() {
    int i = (int)(Math.abs(this.vx) * 100.0);
    DrawEnv.nowSin = si[i];
    DrawEnv.nowCos = co[i];
    if (this.vx > 0.0) DrawEnv.nowSin = -DrawEnv.nowSin;
    ListIterator<Obstacle> iter = this.obstacles.listIterator(); while(iter.hasNext()) { Obstacle obstacle = iter.next();
      obstacle.move(this.vx, 0.0, -1.0);
      DPoint3[] points = obstacle.points;
      if ((points[0]).z <= 1.1) {
        double d = this.mywidth * DrawEnv.nowCos;
        if (-d < (points[2]).x && (points[0]).x < d) this.damaged++;
        iter.remove();
      }
    }
    this.rounds[this.round].move(this.vx);
    { Obstacle obstacle = this.rounds[this.round].generateObstacle(); if(obstacle != null) this.obstacles.addFirst(obstacle); }
  }

  public void mouseEntered(MouseEvent paramMouseEvent) {}

  public void mouseExited(MouseEvent paramMouseEvent) {}

  public void start() {
    this.gameThread = new Thread(this);
    this.gameThread.start();
  }

  private Font titleFont = new Font("Courier", Font.PLAIN, 14);
  private void showTitle() {
    this.vx = 0.0;
    this.scene_g.setFont(this.titleFont);
    this.scene_g.setColor(Color.white);
    FontMetrics fm = this.scene_g.getFontMetrics();
    int line_h = fm.getHeight();
    int spacing = 5;
    int n = 3;
    int h = (line_h + spacing) * n - spacing;
    int y = (this.height - h) / 2;
    
    { String msg = "Jet Slalom Resurrected"; int line_w = fm.stringWidth(msg); this.scene_g.drawString(msg, (this.width - line_w) / 2, y); y += line_h + spacing; }
    { String msg = "by David Lareau in 2025"; int line_w = fm.stringWidth(msg); this.scene_g.drawString(msg, (this.width - line_w) / 2, y); y += line_h + spacing; }
    { String msg = "Original 1999 version by MR-C"; int line_w = fm.stringWidth(msg); this.scene_g.drawString(msg, (this.width - line_w) / 2, y); y += line_h + spacing; }
  }

  public void startGame(int mode, boolean paramBoolean) {
    if (this.gameMode == PLAY_MODE)
      return;
    this.vx = 0.0D;
    if (mode == PLAY_MODE) {
      this.gameMode = mode;
    } else {
      this.gameMode = TITLE_MODE;
    }
    obstacles.clear();
    for (byte b = 0; b < this.rounds.length; b++)
      this.rounds[b].init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0D;
    if (paramBoolean) {
      while (this.prevScore >= this.rounds[this.round].getNextRoundScore())
        this.round++;
      if (this.round > 0) {
        this.score = this.rounds[this.round - 1].getNextRoundScore();
        this.contNum++;
      }
    } else {
      this.contNum = 0;
    }
    this.lblContinue.setText("" + (this.contNum * 1000));
  }

  void prt() {
    this.scene_g.setColor(this.rounds[this.round].getSkyColor());
    this.scene_g.fillRect(0, 0, this.width, this.height);
    if (this.gameMode == PLAY_MODE) {
      this.score += 20;
      if (this.scFlag)
        this.scoreWin.setNum(this.score);
    }
    this.scFlag = !this.scFlag;
    this.scene_g.setColor(this.rounds[this.round].getGroundColor()); DrawEnv.drawPolygon(this.scene_g, this.ground_points);
    for(Obstacle obstacle : obstacles) obstacle.draw(this.scene_g);
    this.shipCounter++;
    if (this.gameMode != TITLE_MODE) {
      int i = 24 * this.height / 200;
      Image image = this.ship[this.shipCounter % 4 > 1? 1 : 0];
      if (this.shipCounter % 12 > 6) i = 22 * this.height / 200;
      if (this.score < 200) i = (12 + this.score / 20) * this.height / 200;
      if (this.damaged < 10) this.scene_g.drawImage(image, (width / 2) - this.mywidth2, this.height - i, null);
      if (this.damaged > 0) putbomb();
    }
    if (this.gameMode == TITLE_MODE) {
      showTitle();
    }
    if(!this.hasFocus()) {
      this.scene_g.setFont(this.titleFont); this.scene_g.setColor(Color.red);
      FontMetrics fm = this.scene_g.getFontMetrics();
      int y = (int)(this.height * (System.currentTimeMillis() % 10000) / 10000.0);
      { String msg = "Lost Keyboard Input Focus"; int line_w = fm.stringWidth(msg); this.scene_g.drawString(msg, (this.width - line_w) / 2, y); }
    }
  }

  public void mouseClicked(MouseEvent paramMouseEvent) {}

  public void mousePressed(MouseEvent paramMouseEvent) {
    int i = paramMouseEvent.getModifiersEx(); // DAVE deprecated warning, but since the constants aren't used here, this probably breaks things BUTTON3_MASK VS BUTTON3_DOWN_MASK
    if ((i & 0x4) != 0) {
      this.rFlag = true;
      this.lFlag = false;
    } else if ((i & 0x10) != 0) {
      this.rFlag = false;
      this.lFlag = true;
    }
    if (this.gameMode == PLAY_MODE)
      return;
    if (this.isInPage && this.gameMode == TITLE_MODE) startGame(PLAY_MODE, false);
  }

  public void mouseDragged(MouseEvent paramMouseEvent) {}

  public void mouseMoved(MouseEvent paramMouseEvent) {
    this.mouseX = paramMouseEvent.getX();
    this.mouseY = paramMouseEvent.getY();
  }

  public void keyTyped(KeyEvent paramKeyEvent) {}


  private void drawMemInfo(Graphics paramGraphics) {
    int i = (int)this.runtime.freeMemory();
    byte b = 7;
    while (true) {
      int j = i % 10;
      i /= 10;
      this.memInfo[b] = (char)(48 + j);
      if (--b < 0) {
        paramGraphics.setColor(Color.red);
        paramGraphics.drawChars(this.memInfo, 0, 8, 0, 32);
        return;
      }
    }
  }

  public int getHiScore() {
    return this.hiscore;
  }

  void putExtra() {}

  void putbomb() {
    if (this.damaged > 20) {
      endGame();
      return;
    }
    if (this.damaged == 1 && this.explosion != null) { this.explosion.stop(); this.explosion.setFramePosition(0); this.explosion.start(); }
    this.scene_g.setColor(new Color(255, 255 - this.damaged * 12, 240 - this.damaged * 12));
    int i = this.damaged * 8 * this.width / 320;
    int j = this.damaged * 4 * this.height / 200;
    this.scene_g.fillOval((width / 2) - i, 186 * this.height / 200 - j, i * 2, j * 2);
    this.damaged++;
  }

  public void keyReleased(KeyEvent paramKeyEvent) {
    keyEvent(paramKeyEvent.getKeyCode(), false);
  }

  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }

  public void run() {
    obstacles.clear();
    for (byte b = 0; b < this.rounds.length; b++) this.rounds[b].init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0D;
    this.gameMode = TITLE_MODE;
    while (this.gameThread != null) {
      //if(!this.hasFocus()) this.requestFocus();

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
      if(this.gameMode != PLAY_MODE && ((gamepad.start && gamepad.n_start) || (gamepad.south_maybe && gamepad.n_south_maybe) || (gamepad.north_maybe && gamepad.n_north_maybe) || (gamepad.west_maybe && gamepad.n_west_maybe) || (gamepad.east_maybe && gamepad.n_east_maybe))) startGame(PLAY_MODE, false);

      if (this.rounds[this.round].isNextRound(this.score))
        this.round++;
      boolean lFlag_stored = this.lFlag; this.lFlag |= gamepad_left;
      boolean rFlag_stored = this.rFlag; this.rFlag |= gamepad_right;
      keyOperate();
      this.lFlag = lFlag_stored; this.rFlag = rFlag_stored;
      moveObstacle();
      prt();
      putExtra();
      Graphics g = this.getGraphics();
      if (this.registMode) {
        g.setColor(Color.lightGray);
        g.fill3DRect(0, 0, this.width, this.height, true);
        g.setColor(Color.black);
        g.drawString("Wait a moment!!", (width / 2) - 32, (height / 2) + 8);
      } else {
        // letterbox scaling (i.e. respects aspect ratio)
        int b_w = this.getWidth(); int b_h = this.getHeight(); int s_w = this.width; int s_h = this.height;
        double scale; if ((b_w / (double)b_h) > (s_w / (double)s_h)) scale = b_h / (double)s_h; else scale = b_w / (double)s_w;
        int x = (int)((b_w - s_w * scale) / 2); int y = (int)((b_h - s_h * scale) / 2);
        g.drawImage(this.scene_img,x,y,(int)(s_w*scale), (int)(s_h*scale), Color.WHITE, null);
      }
      this.getToolkit().sync();
      try { Thread.sleep(55); } catch (InterruptedException e) { e.printStackTrace(); }
    }
  }

  void endGame() {
    this.scoreWin.setNum(this.score);
    if (this.gameMode == PLAY_MODE)
      this.prevScore = this.score;
    if (this.score - this.contNum * 1000 > this.hiscore && this.gameMode == PLAY_MODE) {
      this.hiscore = this.score - this.contNum * 1000;
    }
    this.hiScoreLabel.setText("Your Hi-score:" + this.hiscore);
    this.gameMode = TITLE_MODE;
  }
}