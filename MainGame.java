import java.awt.*;
import java.awt.event.*;
import static java.awt.event.KeyEvent.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.sound.sampled.*;

class MainGame extends Panel implements Runnable, MouseListener, MouseMotionListener, KeyListener, WindowListener {

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
    MainGame game = new MainGame();
    window.addWindowListener(game);
    window.setLayout(new BorderLayout());
    window.add(game, BorderLayout.CENTER);
    window.setVisible(true);

    game.setLayout(new BorderLayout());
    game.setBackground(new Color(160, 208, 176));
    game.scoreWin = new NumberLabel(64, 12);
    game.lblContinue = new Label("            ");
    Panel panel = new Panel(new FlowLayout(0, 5, 0));
    panel.setLayout(new FlowLayout());
    panel.add(new Label("Score:"));
    panel.add(game.scoreWin);
    panel.add(new Label("Continue penalty:"));
    panel.add(game.lblContinue);
    game.add(panel, BorderLayout.NORTH);
    game.hiScoreLabel = new Label("Your Hi-score:0         ");
    game.add(game.hiScoreLabel, BorderLayout.SOUTH);

    game.addKeyListener(game);
    game.addMouseListener(game);
    game.addMouseMotionListener(game);
    for (byte b = 1; b < game.rounds.length; b++)
      game.rounds[b].setPrevRound(game.rounds[b - 1]);

    game.init();
    game.requestFocus();
    game.invalidate();
    game.validate();

    window.validate();
    window.pack();
    window.setSize(800, 600);
    game.start();
    game.startGame(1, false);
  }

  private static Random random = new Random();
  public static int getRandom() { return random.nextInt(Integer.MAX_VALUE); }

  Gamepad gamepad = new Gamepad();

  Clip explosion;

  static double[] si = new double[128];

  static double[] co = new double[128];

  DPoint3[] ground_points = new DPoint3[] { new DPoint3(-100.0, 2.0, 28.0), new DPoint3(-100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 28.0) };
  Color ground_color;

  private LinkedList<Obstacle> obstacles = new LinkedList<>();

  double vx = 0.0D;

  double mywidth = 0.7D;

  int mywidth2;

  int score;

  int prevScore;

  int hiscore;

  int shipCounter;

  int contNum;

  int gameMode;
  static final int PLAY_MODE = 0;
  static final int TITLE_MODE = 1;

  boolean isContinue = false;

  boolean registMode = false;

  int width;

  int height;

  int centerX;

  int centerY;

  int mouseX = 0;

  int mouseY = 0;

  boolean isInPage = false;

  Thread gameThread;

  Image img;

  Image myImg;

  Image myImg2;

  Image myRealImg;

  Image myRealImg2;

  Graphics gra;

  Graphics thisGra;

  MediaTracker tracker;

  boolean isLoaded = false;

  int round;

  RoundManager[] rounds = new RoundManager[] { new NormalRound(8000, new Color(0, 160, 255), new Color(0, 200, 64), 4), new NormalRound(12000, new Color(240, 160, 160), new Color(64, 180, 64), 3), new NormalRound(25000, Color.black, new Color(0, 128, 64), 2), new RoadRound(40000, new Color(0, 180, 240), new Color(0, 200, 64), false), new RoadRound(100000, Color.lightGray, new Color(64, 180, 64), true), new NormalRound(1000000, Color.black, new Color(0, 128, 64), 1) };

  boolean rFlag = false;

  boolean lFlag = false;

  boolean scFlag = true;

  Font titleFont;

  Font normalFont;

  int damaged;

  private char[] memInfo = new char[8];

  private Runtime runtime = Runtime.getRuntime();

  private int titleCounter_;

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
    if (this.gameMode != PLAY_MODE && (keycode == VK_SPACE || keycode == VK_C)) startGame(PLAY_MODE, !(keycode != VK_C));
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

  private void showTitle() {
    this.vx = 0.0D;
    byte b = 100;
    if (this.titleCounter_ < b) {
      this.gra.setFont(new Font("Courier", Font.PLAIN, 12));
      this.gra.setColor(Color.white);
      this.gra.drawString("Jet Slalom Resurrected", 100, 80);
      this.gra.drawString("by David Lareau", 100, 100);
      this.gra.drawString("Original by MR-C 1999", 100, 120);
    } else {
      int score = (this.titleCounter_ - b) / b * 5;
      if (score > 15) {
        score = 15;
        this.titleCounter_ = 0;
      }
      if(hiScoreInfoObj.size() == 5) { hiScoreInfoObj.removeLast(); } hiScoreInfoObj.addFirst(score);
      for(int i = 0; i < hiScoreInfoObj.size(); i++) {
        this.gra.drawString(hiScoreInfoObj.get(i).toString(), 100, 80 + 20 * i);
      }
    }
    this.titleCounter_++;
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
    this.gra.setColor(this.rounds[this.round].getSkyColor());
    this.gra.fillRect(0, 0, this.width, this.height);
    if (this.gameMode == PLAY_MODE) {
      this.score += 20;
      if (this.scFlag)
        this.scoreWin.setNum(this.score);
    }
    this.scFlag = !this.scFlag;
    this.ground_color = this.rounds[this.round].getGroundColor();
    this.gra.setColor(this.ground_color); DrawEnv.drawPolygon(this.gra, this.ground_points);
    for(Obstacle obstacle : obstacles) obstacle.draw(this.gra);
    this.shipCounter++;
    if (this.gameMode != TITLE_MODE) {
      int i = 24 * this.height / 200;
      Image image = this.myRealImg;
      if (this.shipCounter % 4 > 1)
        image = this.myRealImg2;
      if (this.shipCounter % 12 > 6)
        i = 22 * this.height / 200;
      if (this.score < 200)
        i = (12 + this.score / 20) * this.height / 200;
      this.gra.drawImage(image, this.centerX - this.mywidth2, this.height - i, null);
      if (this.damaged > 0)
        putbomb();
    }
    if (this.gameMode == TITLE_MODE) {
      showTitle();
      return;
    }
    this.titleCounter_ = 0;
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
    this.gra.setColor(new Color(255, 255 - this.damaged * 12, 240 - this.damaged * 12));
    int i = this.damaged * 8 * this.width / 320;
    int j = this.damaged * 4 * this.height / 200;
    this.gra.fillOval(this.centerX - i, 186 * this.height / 200 - j, i * 2, j * 2);
    this.damaged++;
  }

  private Image loadImage(String paramString) {
    Image image = this.getToolkit().getImage(ClassLoader.getSystemResource(paramString));
    this.tracker.addImage(image, 0);
    return image;
  }

  public void keyReleased(KeyEvent paramKeyEvent) {
    keyEvent(paramKeyEvent.getKeyCode(), false);
  }

  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }

  ArrayList<Integer> hiScoreInfoObj = new ArrayList<>(5);
  
  public void run() {
    this.thisGra = this.getGraphics();
    obstacles.clear();
    for (byte b = 0; b < this.rounds.length; b++) this.rounds[b].init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0D;
    this.gameMode = PLAY_MODE; // TODO start in title
    while (this.gameThread != null) {

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
      if (this.registMode) {
        this.thisGra.setColor(Color.lightGray);
        this.thisGra.fill3DRect(0, 0, this.width, this.height, true);
        this.thisGra.setColor(Color.black);
        this.thisGra.drawString("Wait a moment!!", this.centerX - 32, this.centerY + 8);
      } else {
        // letterbox scaling (i.e. respects aspect ratio)
        int b_w = this.getWidth(); int b_h = this.getHeight(); int s_w = this.width; int s_h = this.height;
        double scale; if ((b_w / (double)b_h) > (s_w / (double)s_h)) scale = b_h / (double)s_h; else scale = b_w / (double)s_w;
        int x = (int)((b_w - s_w * scale) / 2); int y = (int)((b_h - s_h * scale) / 2);
        this.thisGra.drawImage(this.img,x,y,(int)(s_w*scale), (int)(s_h*scale), Color.WHITE, null);
      }
      this.getToolkit().sync();
      try { Thread.sleep(55); } catch (InterruptedException e) { e.printStackTrace(); }
    }
  }

  public void init() {
    width = 320;
    height = 200;
    centerX = width / 2;
    centerY = height / 2;
    DrawEnv.width = width;
    DrawEnv.height = height;
    img = this.createImage(width, height);
    this.gra = img.getGraphics();
    this.gra.setColor(new Color(0,128,128));
    this.gra.fillRect(0, 0, width, height);
    for(int i = 0; i < si.length; i++) {
      //si[i] = Math.sin(Math.PI * 75 / 6);
      //co[i] = Math.cos(Math.PI * 75 / 6);
      si[i] = Math.sin(Math.PI * (i / (double)si.length));
      co[i] = Math.cos(Math.PI * (i / (double)si.length));
    }
    this.mywidth2 = (int)(this.width * this.mywidth * 120 / 1.6 / 320);
    try {
      myImg = ImageIO.read(new File("res/jiki.gif"));
      myImg2 = ImageIO.read(new File("res/jiki2.gif"));
      explosion = AudioSystem.getClip();
      explosion.open(AudioSystem.getAudioInputStream(new File("res/explosion.wav")));
    } catch(Exception e) {
      e.printStackTrace();
    }
    myRealImg = myImg.getScaledInstance(mywidth2 * 2, mywidth2 / 4, Image.SCALE_FAST);
    myRealImg2 = myImg2.getScaledInstance(mywidth2 * 2, mywidth2 / 4, Image.SCALE_FAST);
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