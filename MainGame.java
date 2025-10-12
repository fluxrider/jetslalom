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

class MainGame extends Canvas implements Runnable, MouseListener, MouseMotionListener, KeyListener {
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
    this.gameMode = 1;
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
    if (this.gameMode != 0 && (paramInt == 32 || paramInt == 67))
      startGame(0, !(paramInt != 67)); 
    if (this.gameMode == 1 && paramInt == 68 && this.hiscoreRec != null)
      startGame(2, false); 
    if (this.gameMode != 0 && paramInt == 84) {
      this.prevScore = 110000;
      this.contNum = 100;
      startGame(0, true);
    } 
  }
  
  void keyOperate() {
    boolean bool1 = this.rFlag;
    boolean bool2 = this.lFlag;
    if (this.gameMode == 0) {
      int i = 0;
      if (bool1)
        i |= 0x2; 
      if (bool2)
        i |= 0x1; 
      this.recorder.writeStatus(i);
    } else if (this.gameMode == 2) {
      int i = this.hiscoreRec.readStatus();
      bool1 = !((i & 0x2) == 0);
      bool2 = !((i & 0x1) == 0);
    } 
    if (this.damaged == 0 && (this.gameMode == 0 || this.gameMode == 2)) {
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
    if (this.gameMode == 2)
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
    if (this.gameMode == 0)
      return; 
    this.vx = 0.0D;
    if (paramInt == 0 || paramInt == 2) {
      if (paramInt == 2 && this.hiscoreRec == null)
        return; 
      this.gameMode = paramInt;
      if (paramInt == 0) {
        this.recorder = new GameRecorder();
      } else {
        this.hiscoreRec.toStart();
      } 
    } else {
      this.gameMode = 1;
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
    if (this.gameMode == 0) {
      this.score += 20;
      if (this.scFlag)
        this.parent.scoreWin.setNum(this.score); 
    } 
    this.scFlag = !this.scFlag;
    this.ground.color = this.rounds[this.round].getGroundColor();
    this.ground.draw(this.gra, this.env);
    this.obstacles.draw(this.gra, this.env);
    this.shipCounter++;
    if (this.gameMode != 1) {
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
    if (this.gameMode == 1) {
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
    if (this.gameMode == 0)
      return; 
    if (!this.isFocus2) {
      this.isFocus2 = true;
      return;
    } 
    if (this.isInPage && this.gameMode == 1)
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
    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    for (byte b = 1; b < this.rounds.length; b++)
      this.rounds[b].setPrevRound(this.rounds[b - 1]); 
  }
  
  public void mouseMoved(MouseEvent paramMouseEvent) {
    this.mouseX = paramMouseEvent.getX();
    this.mouseY = paramMouseEvent.getY();
  }
  
  public void keyTyped(KeyEvent paramKeyEvent) {}
  
  public void paint(Graphics paramGraphics) {
    if (this.registMode) {
      paramGraphics.setColor(Color.lightGray);
      paramGraphics.fill3DRect(0, 0, this.width, this.height, true);
      paramGraphics.setColor(Color.black);
      paramGraphics.drawString("Wait a moment!!", this.centerX - 32, this.centerY + 8);
      return;
    } 
    if (this.img != null)
      paramGraphics.drawImage(this.img, 0, 0, this); 
  }
  
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
      image = getToolkit().getImage(ClassLoader.getSystemResource(paramString));
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
    this.thisGra = getGraphics();
    this.obstacles.removeAll();
    for (byte b = 0; b < this.rounds.length; b++)
      this.rounds[b].init(); 
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0D;
    this.gameMode = 1;
    while (this.gameThread != null) {
      if (this.rounds[this.round].isNextRound(this.score))
        this.round++; 
      keyOperate();
      moveObstacle();
      prt();
      putExtra();
      this.thisGra.drawImage(this.img, 0, 0, null);
      getToolkit().sync();
      if (!this.spcFlag)
        this.timer.wait1step(); 
    } 
  }
  
  public void init() {
    // DAVE
    this.titleFont = new Font("Courier", Font.PLAIN, 12);
    this.title = new StringObject(this.titleFont, Color.white, "Jet slalom", 100, 80); // width/2, centerY);
    this.author = new StringObject(this.titleFont, Color.white, "author", 100, 100);
    this.startMsg = new StringObject(this.titleFont, Color.white, "startMsg", 100, 120);
    this.contMsg = new StringObject(this.titleFont, Color.white, "contMsg", 100, 140);
    this.clickMsg = new StringObject(this.titleFont, Color.white, "clickMsg", 100, 160);
    this.hpage = new StringObject(this.titleFont, Color.white, "hpage", 100, 180);
    width = 320;
    height = 200;
    centerX = width / 2;
    centerY = height / 2;
    env.width = width;
    env.height = height;
    img = createImage(width, height);
    this.gra = img.getGraphics();
    this.gra.setColor(new Color(0,128,128));
    this.gra.fillRect(0, 0, width, height);
    for(int i = 0; i < si.length; i++) {
      si[i] = Math.sin(Math.PI * 75 / 6);
      co[i] = Math.cos(Math.PI * 75 / 6);
    }
    //   121: iconst_0
    //   122: istore_1
    //   123: getstatic MainGame.si : [D
    //   126: iload_1
    //   127: ldc2_w 3.141592653589793
    //   130: iload_1
    //   131: i2d
    //   132: dmul
    //   133: ldc2_w 75.0
    //   136: ddiv
    //   137: ldc2_w 6.0
    //   140: ddiv
    //   141: invokestatic sin : (D)D
    //   144: dastore
    //   145: getstatic MainGame.co : [D
    //   148: iload_1
    //   149: ldc2_w 3.141592653589793
    //   152: iload_1
    //   153: i2d
    //   154: dmul
    //   155: ldc2_w 75.0
    //   158: ddiv
    //   159: ldc2_w 6.0
    //   162: ddiv
    //   163: invokestatic cos : (D)D
    //   166: dastore
    //   167: iinc #1, 1
    //   170: iload_1
    //   171: sipush #128
    //   174: if_icmplt -> 123
    //   177: aload_0
    //   178: aload_0
    //   179: getfield width : I
    //   182: i2d
    //   183: aload_0
    //   184: getfield mywidth : D
    //   187: dmul
    //   188: ldc2_w 120.0
    //   191: dmul
    //   192: ldc2_w 1.6
    //   195: ddiv
    //   196: ldc2_w 320.0
    //   199: ddiv
    //   200: d2i
    //   201: putfield mywidth2 : I
    //   204: getstatic Game3D.isLocal : Z
    //   207: ifne -> 231
    //   210: aload_0
    //   211: aload_0
    //   212: getfield parent : LGame3D;
    //   215: aload_0
    //   216: getfield parent : LGame3D;
    //   219: invokevirtual getCodeBase : ()Ljava/net/URL;
    //   222: ldc_w 'bomb.au'
    //   225: invokevirtual getAudioClip : (Ljava/net/URL;Ljava/lang/String;)Ljava/applet/AudioClip;
    //   228: putfield auBomb : Ljava/applet/AudioClip;
    //   231: aload_0
    //   232: new java/awt/MediaTracker
    //   235: dup
    //   236: aload_0
    //   237: getfield parent : LGame3D;
    //   240: invokespecial <init> : (Ljava/awt/Component;)V
    //   243: putfield tracker : Ljava/awt/MediaTracker;
    //   246: aload_0
    //   247: aload_0
    //   248: ldc_w 'jiki.gif'
    //   251: invokespecial loadImage : (Ljava/lang/String;)Ljava/awt/Image;
    //   254: putfield myImg : Ljava/awt/Image;
    //   257: aload_0
    //   258: aload_0
    //   259: ldc_w 'jiki2.gif'
    //   262: invokespecial loadImage : (Ljava/lang/String;)Ljava/awt/Image;
    //   265: putfield myImg2 : Ljava/awt/Image;
    //   268: aload_0
    //   269: getfield tracker : Ljava/awt/MediaTracker;
    //   272: invokevirtual waitForAll : ()V
    //   275: goto -> 279
    //   278: pop
    //   279: aload_0
    //   280: aload_0
    //   281: getfield myImg : Ljava/awt/Image;
    //   284: aload_0
    //   285: getfield mywidth2 : I
    //   288: iconst_2
    //   289: imul
    //   290: aload_0
    //   291: getfield mywidth2 : I
    //   294: bipush #16
    //   296: imul
    //   297: bipush #52
    //   299: idiv
    //   300: iconst_4
    //   301: invokevirtual getScaledInstance : (III)Ljava/awt/Image;
    //   304: putfield myRealImg : Ljava/awt/Image;
    //   307: aload_0
    //   308: aload_0
    //   309: getfield myImg2 : Ljava/awt/Image;
    //   312: aload_0
    //   313: getfield mywidth2 : I
    //   316: iconst_2
    //   317: imul
    //   318: aload_0
    //   319: getfield mywidth2 : I
    //   322: bipush #16
    //   324: imul
    //   325: bipush #52
    //   327: idiv
    //   328: iconst_4
    //   329: invokevirtual getScaledInstance : (III)Ljava/awt/Image;
    //   332: putfield myRealImg2 : Ljava/awt/Image;
    //   335: aload_0
    //   336: new java/awt/Font
    //   339: dup
    //   340: ldc_w 'TimesRoman'
    //   343: iconst_1
    //   344: aload_0
    //   345: getfield width : I
    //   348: bipush #32
    //   350: imul
    //   351: sipush #320
    //   354: idiv
    //   355: iconst_4
    //   356: iadd
    //   357: invokespecial <init> : (Ljava/lang/String;II)V
    //   360: putfield titleFont : Ljava/awt/Font;
    //   363: aload_0
    //   364: new java/awt/Font
    //   367: dup
    //   368: ldc_w 'Courier'
    //   371: iconst_0
    //   372: bipush #12
    //   374: invokespecial <init> : (Ljava/lang/String;II)V
    //   377: putfield normalFont : Ljava/awt/Font;
    //   380: aload_0
    //   381: new StringObject
    //   384: dup
    //   385: aload_0
    //   386: getfield titleFont : Ljava/awt/Font;
    //   389: getstatic java/awt/Color.white : Ljava/awt/Color;
    //   392: ldc_w 'Jet slalom'
    //   395: aload_0
    //   396: getfield width : I
    //   399: iconst_2
    //   400: idiv
    //   401: aload_0
    //   402: getfield centerY : I
    //   405: bipush #20
    //   407: aload_0
    //   408: getfield width : I
    //   411: imul
    //   412: sipush #320
    //   415: idiv
    //   416: isub
    //   417: invokespecial <init> : (Ljava/awt/Font;Ljava/awt/Color;Ljava/lang/String;II)V
    //   420: putfield title : LStringObject;
    //   423: aload_0
    //   424: new StringObject
    //   427: dup
    //   428: aload_0
    //   429: getfield normalFont : Ljava/awt/Font;
    //   432: getstatic java/awt/Color.black : Ljava/awt/Color;
    //   435: ldc_w 'Programed by MR-C'
    //   438: aload_0
    //   439: getfield centerX : I
    //   442: aload_0
    //   443: getfield centerY : I
    //   446: bipush #68
    //   448: iadd
    //   449: invokespecial <init> : (Ljava/awt/Font;Ljava/awt/Color;Ljava/lang/String;II)V
    //   452: putfield author : LStringObject;
    //   455: aload_0
    //   456: new StringObject
    //   459: dup
    //   460: aload_0
    //   461: getfield normalFont : Ljava/awt/Font;
    //   464: getstatic java/awt/Color.black : Ljava/awt/Color;
    //   467: aload_0
    //   468: getfield parent : LGame3D;
    //   471: getfield toStartMsg : [Ljava/lang/String;
    //   474: aload_0
    //   475: getfield parent : LGame3D;
    //   478: getfield lang : I
    //   481: aaload
    //   482: aload_0
    //   483: getfield centerX : I
    //   486: aload_0
    //   487: getfield centerY : I
    //   490: bipush #24
    //   492: iadd
    //   493: invokespecial <init> : (Ljava/awt/Font;Ljava/awt/Color;Ljava/lang/String;II)V
    //   496: putfield startMsg : LStringObject;
    //   499: aload_0
    //   500: new StringObject
    //   503: dup
    //   504: aload_0
    //   505: getfield normalFont : Ljava/awt/Font;
    //   508: getstatic java/awt/Color.black : Ljava/awt/Color;
    //   511: aload_0
    //   512: getfield parent : LGame3D;
    //   515: getfield contMsg : [Ljava/lang/String;
    //   518: aload_0
    //   519: getfield parent : LGame3D;
    //   522: getfield lang : I
    //   525: aaload
    //   526: aload_0
    //   527: getfield centerX : I
    //   530: aload_0
    //   531: getfield centerY : I
    //   534: bipush #44
    //   536: iadd
    //   537: invokespecial <init> : (Ljava/awt/Font;Ljava/awt/Color;Ljava/lang/String;II)V
    //   540: putfield contMsg : LStringObject;
    //   543: aload_0
    //   544: new StringObject
    //   547: dup
    //   548: aload_0
    //   549: getfield normalFont : Ljava/awt/Font;
    //   552: getstatic java/awt/Color.red : Ljava/awt/Color;
    //   555: aload_0
    //   556: getfield parent : LGame3D;
    //   559: getfield clickMsg : [Ljava/lang/String;
    //   562: aload_0
    //   563: getfield parent : LGame3D;
    //   566: getfield lang : I
    //   569: aaload
    //   570: aload_0
    //   571: getfield centerX : I
    //   574: aload_0
    //   575: getfield centerY : I
    //   578: invokespecial <init> : (Ljava/awt/Font;Ljava/awt/Color;Ljava/lang/String;II)V
    //   581: putfield clickMsg : LStringObject;
    //   584: aload_0
    //   585: new StringObject
    //   588: dup
    //   589: aload_0
    //   590: getfield normalFont : Ljava/awt/Font;
    //   593: getstatic java/awt/Color.black : Ljava/awt/Color;
    //   596: ldc_w 'http://www.kdn.gr.jp/~shii/'
    //   599: aload_0
    //   600: getfield centerX : I
    //   603: aload_0
    //   604: getfield centerY : I
    //   607: bipush #86
    //   609: iadd
    //   610: invokespecial <init> : (Ljava/awt/Font;Ljava/awt/Color;Ljava/lang/String;II)V
    //   613: putfield hpage : LStringObject;
    //   616: return
    // Exception table:
    //   from	to	target	type
    //   268	275	278	java/lang/InterruptedException
  }
  
  void endGame() {
    this.parent.scoreWin.setNum(this.score);
    if (this.gameMode == 0)
      this.prevScore = this.score; 
    if (this.score - this.contNum * 1000 > this.hiscore && this.gameMode == 0) {
      this.hiscore = this.score - this.contNum * 1000;
      this.hiscoreRec = this.recorder;
    } 
    this.parent.hiScoreLabel.setText("Your Hi-score:" + this.hiscore);
    this.gameMode = 1;
    this.parent.endGame();
  }
}


/* Location:              C:\a\!\MainGame.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */