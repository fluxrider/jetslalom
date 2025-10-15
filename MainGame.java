// DAVE import java.applet.AudioClip;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.*;

class MainGame implements Runnable, MouseListener, MouseMotionListener, KeyListener {
  
  static double[] si = new double[128];
  
  static double[] co = new double[128];
  
  DrawEnv env = new DrawEnv();
  
  Ground ground = new Ground();
  
  TimerNotifier timer;
  
  GameRecorder recorder = new GameRecorder();
  
  GameRecorder hiscoreRec = null;
  
  ObstacleCollection obstacles = new ObstacleCollection();
  
  double vx = 0.0D;
  
  double mywidth = 0.7D;
  
  int mywidth2;
  
  int score;
  
  int prevScore;
  
  int hiscore;
  
  int shipCounter;
  
  int contNum;
  
  private String[] strHiScoreInfo_;
  
  int gameMode = 2;
  
  static final int PLAY_MODE = 0;
  
  static final int TITLE_MODE = 1;
  
  static final int DEMO_MODE = 2;
  
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
  
  // DAVE AudioClip auBomb = null;
  
  boolean isLoaded = false;
  
  int round;
  
  RoundManager[] rounds = new RoundManager[] { new NormalRound(8000, new Color(0, 160, 255), new Color(0, 200, 64), 4), new NormalRound(12000, new Color(240, 160, 160), new Color(64, 180, 64), 3), new NormalRound(25000, Color.black, new Color(0, 128, 64), 2), new RoadRound(40000, new Color(0, 180, 240), new Color(0, 200, 64), false), new RoadRound(100000, Color.lightGray, new Color(64, 180, 64), true), new NormalRound(1000000, Color.black, new Color(0, 128, 64), 1) };
  
  boolean rFlag = false;
  
  boolean lFlag = false;
  
  boolean spcFlag = false;
  
  Game3D parent;
  
  boolean isFocus = true;
  
  boolean isFocus2 = true;
  
  boolean scFlag = true;
  
  Font titleFont;
  
  Font normalFont;
  
  StringObject title;
  
  StringObject author;
  
  StringObject startMsg;
  
  StringObject contMsg;
  
  StringObject clickMsg;
  
  StringObject hpage;
  
  int damaged;
  
  private char[] memInfo = new char[8];
  
  private Runtime runtime = Runtime.getRuntime();
  
  private int titleCounter_;
  
  private StringObject[] hiScoreInfoObj;
  
  public void stop() {
    // DAVE if (this.gameThread != null) this.gameThread.stop(); 
    this.gameThread = null;
    this.registMode = false;
    this.gameMode = TITLE_MODE;
    this.timer.interrupt();
  }
  
  void keyEvent(int paramInt, boolean paramBoolean) {
    if (paramInt == 39 || paramInt == 76)
      this.rFlag = paramBoolean; 
    if (paramInt == 37 || paramInt == 74)
      this.lFlag = paramBoolean; 
    if (paramInt == 65)
      this.spcFlag = paramBoolean; 
    if (!paramBoolean)
      return; 
    if (paramInt == 71)
      System.gc(); 
    if (this.gameMode != PLAY_MODE && (paramInt == 32 || paramInt == 67))
      startGame(0, !(paramInt != 67)); 
    if (this.gameMode == TITLE_MODE && paramInt == 68 && this.hiscoreRec != null)
      startGame(2, false); 
    if (this.gameMode != PLAY_MODE && paramInt == 84) {
      this.prevScore = 110000;
      this.contNum = 100;
      startGame(0, true);
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
      this.recorder.writeStatus(i);
    } else if (this.gameMode == DEMO_MODE) {
      int i = this.hiscoreRec.readStatus();
      bool1 = !((i & 0x2) == 0);
      bool2 = !((i & 0x1) == 0);
    } 
    if (this.damaged == 0 && (this.gameMode == PLAY_MODE || this.gameMode == DEMO_MODE)) {
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
    GameRecorder gameRecorder = this.recorder;
    if (this.gameMode == DEMO_MODE)
      gameRecorder = this.hiscoreRec; 
    int i = (int)(Math.abs(this.vx) * 100.0D);
    this.env.nowSin = si[i];
    this.env.nowCos = co[i];
    if (this.vx > 0.0D)
      this.env.nowSin = -this.env.nowSin; 
    for (Obstacle obstacle = this.obstacles.head.next; obstacle != this.obstacles.tail;) {
      Obstacle obstacle1 = obstacle.next;
      obstacle.move(this.vx, 0.0D, -1.0D);
      DPoint3[] arrayOfDPoint3 = obstacle.points;
      if ((arrayOfDPoint3[0]).z <= 1.1D) {
        double d = this.mywidth * this.env.nowCos;
        if (-d < (arrayOfDPoint3[2]).x && (arrayOfDPoint3[0]).x < d)
          this.damaged++; 
        obstacle.release();
      }
      obstacle = obstacle1;
    } 
    this.rounds[this.round].move(this.vx);
    this.rounds[this.round].generateObstacle(this.obstacles, gameRecorder);
  }
  
  private void updateHiScoreInfoObj(int paramInt) {
    byte b = 0;
    do {
      String str = " " + (paramInt + b + 1);
      str = str.substring(str.length() - 2);
      this.hiScoreInfoObj[b + 1].setText(str + ".  " + this.strHiScoreInfo_[paramInt + b]);
    } while (++b < 5);
  }
  
  public void mouseEntered(MouseEvent paramMouseEvent) {}
  
  public void mouseExited(MouseEvent paramMouseEvent) {}
  
  public void start() {
    this.timer = new TimerNotifier(55);
    this.gameThread = new Thread(this);
    this.gameThread.start();
  }
  
  private void showTitle() {
    this.vx = 0.0D;
    byte b = 100;
    if (this.titleCounter_ < b || this.strHiScoreInfo_ == null) {
      this.title.draw(this.gra, null);
      this.startMsg.draw(this.gra, null);
      this.author.draw(this.gra, null);
      if (this.hpage.hitTest(this.mouseX, this.mouseY)) {
        this.hpage.setColor(Color.white);
        this.isInPage = true;
      } else {
        this.isInPage = false;
        this.hpage.setColor(Color.black);
      } 
      this.hpage.draw(this.gra, null);
      if (this.rounds[0].isNextRound(this.prevScore))
        this.contMsg.draw(this.gra, null); 
    } else {
      int i = (this.titleCounter_ - b) / b * 5;
      if (i > 15) {
        i = 15;
        this.titleCounter_ = 0;
      } 
      if (this.hiScoreInfoObj == null)
        initHiScoreInfoObj(); 
      updateHiScoreInfoObj(i);
      byte b1 = 0;
      do {
        this.hiScoreInfoObj[b1].draw(this.gra, null);
      } while (++b1 < 6);
    } 
    this.titleCounter_++;
    if (!this.isFocus)
      this.clickMsg.draw(this.gra, null); 
  }
  
  public void startGame(int paramInt, boolean paramBoolean) {
    if (this.gameMode == PLAY_MODE)
      return; 
    this.vx = 0.0D;
    if (paramInt == PLAY_MODE || paramInt == DEMO_MODE) {
      if (paramInt == DEMO_MODE && this.hiscoreRec == null)
        return; 
      this.gameMode = paramInt;
      if (paramInt == PLAY_MODE) {
        this.recorder = new GameRecorder();
      } else {
        this.hiscoreRec.toStart();
      } 
    } else {
      this.gameMode = TITLE_MODE;
    } 
    this.obstacles.removeAll();
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
    if (paramInt == 2) {
      this.round = this.hiscoreRec.startRound;
      this.score = this.hiscoreRec.startScore;
    } else {
      this.recorder.startRound = this.round;
      this.recorder.startScore = this.score;
    } 
    this.parent.lblContinue.setText("" + (this.contNum * 1000));
  }
  
  void prt() {
    this.gra.setColor(this.rounds[this.round].getSkyColor());
    this.gra.fillRect(0, 0, this.width, this.height);
    if (this.gameMode == PLAY_MODE) {
      this.score += 20;
      if (this.scFlag)
        this.parent.scoreWin.setNum(this.score); 
    } 
    this.scFlag = !this.scFlag;
    this.ground.color = this.rounds[this.round].getGroundColor();
    this.ground.draw(this.gra, this.env);
    this.obstacles.draw(this.gra, this.env);
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
    if (!this.isFocus2) {
      this.isFocus2 = true;
      return;
    } 
    if (this.isInPage && this.gameMode == TITLE_MODE)
      /* DAVE
      try {
        // DAVE this.parent.getAppletContext().showDocument(new URL("http://www.kdn.gr.jp/~shii/"));
        return;
      } catch (MalformedURLException malformedURLException) {
        return;
      } 
      */
    startGame(0, false);
  }
  
  public void mouseDragged(MouseEvent paramMouseEvent) {}
  
  public MainGame(Game3D paramGame3D) {
    this.parent = paramGame3D;
    this.parent.addKeyListener(this);
    this.parent.addMouseListener(this);
    this.parent.addMouseMotionListener(this);
    for (byte b = 1; b < this.rounds.length; b++)
      this.rounds[b].setPrevRound(this.rounds[b - 1]); 
  }
  
  public void mouseMoved(MouseEvent paramMouseEvent) {
    this.mouseX = paramMouseEvent.getX();
    this.mouseY = paramMouseEvent.getY();
  }
  
  public void keyTyped(KeyEvent paramKeyEvent) {}
  
 
  public synchronized void setHiScoreInfo(String[] paramArrayOfString) {
    this.strHiScoreInfo_ = paramArrayOfString;
  }
  
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
    // DAVE if (this.damaged == 1 && this.auBomb != null) this.auBomb.play(); 
    this.gra.setColor(new Color(255, 255 - this.damaged * 12, 240 - this.damaged * 12));
    int i = this.damaged * 8 * this.width / 320;
    int j = this.damaged * 4 * this.height / 200;
    this.gra.fillOval(this.centerX - i, 186 * this.height / 200 - j, i * 2, j * 2);
    this.damaged++;
  }
  
  private Image loadImage(String paramString) {
    Image image;
    //if (Game3D.isLocal) {
      image = this.parent.getToolkit().getImage(ClassLoader.getSystemResource(paramString));
    //} else {
      // DAVE image = this.parent.getImage(this.parent.getCodeBase(), paramString);
    //} 
    this.tracker.addImage(image, 0);
    return image;
  }
  
  public void keyReleased(KeyEvent paramKeyEvent) {
    keyEvent(paramKeyEvent.getKeyCode(), false);
  }
  
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
  
  private void initHiScoreInfoObj() {
    this.hiScoreInfoObj = new StringObject[6];
    this.hiScoreInfoObj[0] = new StringObject(this.normalFont, Color.white, "Ranking", this.width / 2, 24);
    byte b = 1;
    do {
      this.hiScoreInfoObj[b] = new StringObject(this.normalFont, Color.white, "", this.width / 8, 24 + 24 * b);
      this.hiScoreInfoObj[b].setAlign(1);
    } while (++b < 6);
  }
  
  public void run() {
    this.thisGra = this.parent.getGraphics();
    this.obstacles.removeAll();
    for (byte b = 0; b < this.rounds.length; b++)
      this.rounds[b].init(); 
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0D;
    this.gameMode = PLAY_MODE; // DAVE PLAY_MODE, TITLE_MODE, DEMO_MODE
    while (this.gameThread != null) {
      if (this.rounds[this.round].isNextRound(this.score))
        this.round++; 
      keyOperate();
      moveObstacle();
      prt();
      putExtra();
      //this.thisGra.drawImage(this.img, 0, 0, null);
      
      if (this.registMode) {
        this.thisGra.setColor(Color.lightGray);
        this.thisGra.fill3DRect(0, 0, this.width, this.height, true);
        this.thisGra.setColor(Color.black);
        this.thisGra.drawString("Wait a moment!!", this.centerX - 32, this.centerY + 8);
      } else {
        //this.thisGra.drawImage(this.img, 0, 0, this); 
        this.thisGra.drawImage(this.img,0,0,this.parent.getWidth(), this.parent.getHeight(), Color.WHITE, null);
      }
      
      this.parent.getToolkit().sync();
      if (!this.spcFlag)
        this.timer.wait1step(); 
    } 
  }
  
  public void init() {
    // DAVE this semi inspired by the commented bytecode that was there
    this.titleFont = new Font("Courier", Font.PLAIN, 12);
    this.normalFont = new Font("Courier", Font.PLAIN, 12);
    this.title = new StringObject(this.titleFont, Color.white, "Jet slalom", 100, 80); // width/2, centerY);
    this.author = new StringObject(this.titleFont, Color.white, "Programed by MR-C", 100, 100);
    this.startMsg = new StringObject(this.titleFont, Color.white, "startMsg", 100, 120);
    this.contMsg = new StringObject(this.titleFont, Color.white, "contMsg", 100, 140);
    this.clickMsg = new StringObject(this.titleFont, Color.white, "clickMsg", 100, 160);
    this.hpage = new StringObject(this.titleFont, Color.white, "http://www.kdn.gr.jp/~shii/", 100, 180);
    width = 320;
    height = 200;
    centerX = width / 2;
    centerY = height / 2;
    env.width = width;
    env.height = height;
    img = this.parent.createImage(width, height);
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
    // DAVE load the bomb audio
    try {
      myImg = ImageIO.read(new File("jiki.gif"));
      myImg2 = ImageIO.read(new File("jiki2.gif"));
    } catch(IOException e) {
      e.printStackTrace();
    }
    myRealImg = myImg.getScaledInstance(mywidth2 * 2, mywidth2 / 4, Image.SCALE_FAST);
    myRealImg2 = myImg2.getScaledInstance(mywidth2 * 2, mywidth2 / 4, Image.SCALE_FAST);
  }
  
  void endGame() {
    this.parent.scoreWin.setNum(this.score);
    if (this.gameMode == PLAY_MODE)
      this.prevScore = this.score; 
    if (this.score - this.contNum * 1000 > this.hiscore && this.gameMode == PLAY_MODE) {
      this.hiscore = this.score - this.contNum * 1000;
      this.hiscoreRec = this.recorder;
    } 
    this.parent.hiScoreLabel.setText("Your Hi-score:" + this.hiscore);
    this.gameMode = TITLE_MODE;
    this.parent.endGame();
  }
}


/* Location:              C:\a\!\MainGame.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */