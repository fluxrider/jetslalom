// DAVE import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Game3D extends Panel /* DAVE Applet*/ implements ActionListener, TextListener {
  static final boolean isFreeware = true;
  
  MainGame game;
  
  Label hiScoreLabel;
  
  Label lblContinue;
  
  NumberLabel scoreWin;
  
  static boolean isLocal = false;
  
  int lang = 0;
  
  String[] bt1 = new String[] { "Regist your Hi-score", "自分のハイスコアの登録" };
  
  String[] contMsg = new String[] { "Push [C] key to start from this stage!!", "途中から始める場合は [C]key を押して下さい!!" };
  
  String[] toStartMsg = new String[] { "Click this game screen or push [space] key!!", "クリックするか、[space]keyを押して下さい" };
  
  String[] clickMsg = new String[] { "Click!!", "クリックして下さい" };
  
  private String strSessionId_;
  
  private int sentScore_ = 0;
  
  private TextField txtName = new TextField("No name", 16);
  
  private Button btnInput = new Button("Ok");
  
  private boolean isModified_ = false;
  
  public void stop() {
    this.game.stop();
  }
  
  private void rankInit() {
    this.strSessionId_ = Long.toString(Calendar.getInstance().getTime().getTime());
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
  
  private synchronized void sendScore(int paramInt, String paramString) {
    /*
    if (this.sentScore_ >= paramInt && !this.isModified_)
      return; 
    try {
      int i = paramInt % 8191 + paramInt % 237;
      System.out.println("...");
      String str1 = "regist.cgi?" + paramInt + "+" + i + "+" + encodeString(paramString) + "+" + this.strSessionId_;
      InputStream inputStream = (new URL(getCodeBase(), str1)).openStream();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String str2 = bufferedReader.readLine();
      System.out.println(str2);
      this.sentScore_ = paramInt;
    } catch (Exception exception) {
      System.out.println(exception);
    } 
    loadRanking();
    */
  }
  
  public static void main(String[] paramArrayOfString) {
    isLocal = true;
    Game3D game3D = new Game3D();
    AppFrame appFrame = new AppFrame(/* DAVE game3D,*/ "Jet slalom");
    // DAVE appFrame.show();
    appFrame.setVisible(true); // DAVE
    // DAVE appFrame.setLayout(new AbsoluteLayout());
    appFrame.setLayout(null); // DAVE
    
// DAVE	layout.setWidth("800px");
// DAVE	layout.setHeight("600px");
    game3D.setSize(800, 600); // DAVE
    appFrame.add("Center", game3D);
    game3D.init();
    appFrame.validate();
    appFrame.pack();
    appFrame.setSize(800, 600); // DAVE
    game3D.start();
  }
  
  public void actionPerformed(ActionEvent paramActionEvent) {
    sendScore(this.game.getHiScore(), this.txtName.getText());
    this.requestFocus();
  }
  
  public void start() {
    this.game.start();
    this.game.startGame(1, false);
  }
  
  public void init() {
    if (!isLocal) {
      String str = "JP"; // DAVE getParameter("LANG");
      if (!isLocal && str != null && str.equals("JP"))
        this.lang = 1; 
    } 
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
    add("North", panel);
    this.hiScoreLabel = new Label("Your Hi-score:0         ");
    add("South", this.hiScoreLabel);
    this.game = new MainGame(this);
    //add("Center", this.game);
    this.game.init();
    this.requestFocus();
    invalidate();
    validate();
  }
  
  public void textValueChanged(TextEvent paramTextEvent) {
    this.isModified_ = true;
  }
  
  private synchronized void loadRanking() {
    // Byte code:
    //   0: bipush #20
    //   2: anewarray java/lang/String
    //   5: astore_1
    //   6: iconst_0
    //   7: istore_2
    //   8: aload_1
    //   9: iload_2
    //   10: ldc_w ''
    //   13: aastore
    //   14: iinc #2, 1
    //   17: iload_2
    //   18: bipush #20
    //   20: if_icmplt -> 8
    //   23: new java/net/URL
    //   26: dup
    //   27: aload_0
    //   28: invokevirtual getCodeBase : ()Ljava/net/URL;
    //   31: ldc_w 'rank.dat'
    //   34: invokespecial <init> : (Ljava/net/URL;Ljava/lang/String;)V
    //   37: invokevirtual openStream : ()Ljava/io/InputStream;
    //   40: astore_3
    //   41: new java/io/BufferedReader
    //   44: dup
    //   45: new java/io/InputStreamReader
    //   48: dup
    //   49: aload_3
    //   50: invokespecial <init> : (Ljava/io/InputStream;)V
    //   53: invokespecial <init> : (Ljava/io/Reader;)V
    //   56: astore #4
    //   58: iconst_0
    //   59: istore_2
    //   60: aload #4
    //   62: invokevirtual readLine : ()Ljava/lang/String;
    //   65: astore #5
    //   67: aload #5
    //   69: ifnull -> 208
    //   72: new java/util/StringTokenizer
    //   75: dup
    //   76: aload #5
    //   78: ldc_w ','
    //   81: invokespecial <init> : (Ljava/lang/String;Ljava/lang/String;)V
    //   84: astore #6
    //   86: ldc_w '000000 : ???'
    //   89: astore #7
    //   91: aload #6
    //   93: invokevirtual hasMoreTokens : ()Z
    //   96: ifeq -> 194
    //   99: aload #6
    //   101: invokevirtual nextToken : ()Ljava/lang/String;
    //   104: invokestatic parseInt : (Ljava/lang/String;)I
    //   107: istore #8
    //   109: aload #6
    //   111: invokevirtual hasMoreTokens : ()Z
    //   114: ifeq -> 194
    //   117: aload_0
    //   118: aload #6
    //   120: invokevirtual nextToken : ()Ljava/lang/String;
    //   123: invokespecial decodeString : (Ljava/lang/String;)Ljava/lang/String;
    //   126: astore #9
    //   128: new java/lang/StringBuffer
    //   131: dup
    //   132: invokespecial <init> : ()V
    //   135: ldc_w '000000'
    //   138: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   141: iload #8
    //   143: invokevirtual append : (I)Ljava/lang/StringBuffer;
    //   146: invokevirtual toString : ()Ljava/lang/String;
    //   149: astore #10
    //   151: aload #10
    //   153: aload #10
    //   155: invokevirtual length : ()I
    //   158: bipush #6
    //   160: isub
    //   161: invokevirtual substring : (I)Ljava/lang/String;
    //   164: astore #10
    //   166: new java/lang/StringBuffer
    //   169: dup
    //   170: invokespecial <init> : ()V
    //   173: aload #10
    //   175: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   178: ldc_w ' : '
    //   181: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   184: aload #9
    //   186: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   189: invokevirtual toString : ()Ljava/lang/String;
    //   192: astore #7
    //   194: aload_1
    //   195: iload_2
    //   196: aload #7
    //   198: aastore
    //   199: iinc #2, 1
    //   202: iload_2
    //   203: bipush #20
    //   205: if_icmplt -> 60
    //   208: aload_0
    //   209: getfield game : LMainGame;
    //   212: aload_1
    //   213: invokevirtual setHiScoreInfo : ([Ljava/lang/String;)V
    //   216: getstatic java/lang/System.out : Ljava/io/PrintStream;
    //   219: ldc_w 'Success to load hi-score list.'
    //   222: invokevirtual println : (Ljava/lang/String;)V
    //   225: return
    //   226: astore_3
    //   227: getstatic java/lang/System.out : Ljava/io/PrintStream;
    //   230: ldc_w 'Fail to load hi-score list.'
    //   233: invokevirtual println : (Ljava/lang/String;)V
    //   236: aload_3
    //   237: invokevirtual printStackTrace : ()V
    //   240: return
    // Exception table:
    //   from	to	target	type
    //   23	225	226	java/lang/Exception
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
  
  public synchronized void endGame() {
    sendScore(this.game.getHiScore(), this.txtName.getText());
  }
}


/* Location:              C:\a\!\Game3D.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */