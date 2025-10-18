import java.util.LinkedList;
import java.util.ListIterator;

public class Game {

  public class DPoint3 {
    public double x, y, z;
    public DPoint3() { }
    public DPoint3(double x, double y, double z) { setXYZ(x,y,z); }
    public void setXYZ(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
  }
  
  public class Face {
    DPoint3[] points;
    double maxZ;
    int rgb;
    void calcMaxZ() {
      double d1 = (this.points[1]).x - (this.points[0]).x;
      double d2 = (this.points[1]).y - (this.points[0]).y;
      double d3 = (this.points[1]).z - (this.points[0]).z;
      double d4 = (this.points[2]).x - (this.points[0]).x;
      double d5 = (this.points[2]).y - (this.points[0]).y;
      double d6 = (this.points[2]).z - (this.points[0]).z;
      double a = d2 * d6 - d3 * d5;
      double b = d1 * d6 - d3 * d4;
      double c = d1 * d5 - d2 * d4;
      this.maxZ = Math.sqrt(a*a + b*b + c*c);
    }
  }

  public class Obstacle {
    DPoint3[] points = new DPoint3[] { new DPoint3(), new DPoint3(), new DPoint3(), new DPoint3() };
    Face[] faces = new Face[] { new Face(), new Face(), new Face() };
    int rgb;
    public Obstacle() {
      this.faces[0].points = new DPoint3[] { this.points[3], this.points[0], this.points[1] };
      this.faces[1].points = new DPoint3[] { this.points[3], this.points[2], this.points[1] };
    }
    void prepareNewObstacle() {
      this.faces[0].calcMaxZ(); this.faces[0].rgb = C.brighter(this.rgb);
      this.faces[1].calcMaxZ(); this.faces[1].rgb = this.rgb;
    }
    void move(double x, double y, double z) {
      for(int i = 0; i < 4; i++) {
        DPoint3 dPoint3 = this.points[i];
        dPoint3.x += x;
        dPoint3.y += y;
        dPoint3.z += z;
      }
    }
  }

  public RoundManager[] rounds = new RoundManager[] { new NormalRound(8000, C.rgb(0, 160, 255), C.rgb(0, 200, 64), 4), new NormalRound(12000, C.rgb(240, 160, 160), C.rgb(64, 180, 64), 3), new NormalRound(25000, C.black, C.rgb(0, 128, 64), 2), new RoadRound(40000, C.rgb(0, 180, 240), C.rgb(0, 200, 64), false), new RoadRound(100000, C.gray(192), C.rgb(64, 180, 64), true), new NormalRound(1000000, C.black, C.rgb(0, 128, 64), 1) };
  public DPoint3[] ground_points = new DPoint3[] { new DPoint3(-100.0, 2.0, 28.0), new DPoint3(-100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 28.0) };
  public LinkedList<Obstacle> obstacles = new LinkedList<>();
  private double vx; // ship's left/right movement
  public int round;
  public int damaged;
  public int score, prevScore, hiscore, contNum;
  public boolean title_mode;

  public double nowSin;
  public double nowCos;

  public Game() {
    for(int b = 1; b < this.rounds.length; b++) this.rounds[b].setPrevRound(this.rounds[b - 1]);
    obstacles.clear();
    for(RoundManager r : this.rounds) r.init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0;
    this.title_mode = true;
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

  void tick(boolean left, boolean right) {
    if(this.rounds[this.round].isNextRound(this.score)) this.round++;
    if(this.damaged > 0) this.damaged++;

    // ship input
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
    
    // move obstacles
    double angle = Math.abs(this.vx) * 100.0; // [-60, 60]
    nowSin = Math.sin(Math.PI * angle / 180); // I can't replay the original, but the code seem to have divided by 128 here but 180 makes more sense
    nowCos = Math.cos(Math.PI * angle / 180);
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
    
    // tick
    if(!this.title_mode) this.score += 20;
    if(this.damaged > 20) {
      if(!this.title_mode) this.prevScore = this.score;
      if(this.score - this.contNum * 1000 > this.hiscore && !this.title_mode) this.hiscore = this.score - this.contNum * 1000;
      this.title_mode = true;
    }
    if(this.title_mode) {
      this.vx = 0.0;
    }
  }

  public abstract class RoundManager {
    private RoundManager prevRound;

    protected int nextRoundScore;
    protected int sky_rgb;
    protected int ground_rgb;
    protected int gameTime;
    public void setPrevRound(RoundManager round) { this.prevRound = round; }
    public int getGroundRGB() { return this.ground_rgb; }
    public int getNextRoundScore() { return this.nextRoundScore; }

    protected final Obstacle createObstacle(double x1, double x2) {
      Obstacle o = new Obstacle();
      DPoint3[] arrayOfDPoint3 = o.points;
      arrayOfDPoint3[0].setXYZ(x1 - x2, 2.0, 25.5);
      arrayOfDPoint3[1].setXYZ(x1, -1.4, 25.0);
      arrayOfDPoint3[2].setXYZ(x1 + x2, 2.0, 25.5);
      arrayOfDPoint3[3].setXYZ(x1, 2.0, 24.5);
      switch(M.rand_index(4)) {
        case 0: o.rgb = C.gray(192); break;
        case 1: o.rgb = C.rgb(96, 160, 240); break;
        case 2: o.rgb = C.rgb(200, 128, 0); break;
        case 3: o.rgb = C.rgb(240, 210, 100); break;
      }
      o.prepareNewObstacle();
      return o;
    }

    protected final Obstacle createObstacle() { return createObstacle(M.drand(-16.0, 16.0), 0.6); }

    public boolean isNextRound(int score) { return !(score < this.nextRoundScore); }

    public int getSkyRGB() {
      if (this.prevRound == null || this.gameTime > 32) return this.sky_rgb;
      int i = this.gameTime;
      int j = 32 - i;
      int prev = this.prevRound.sky_rgb;
      int curr = this.sky_rgb;
      int k = C.r(prev) * j + C.r(curr) * i;
      int m = C.g(prev) * j + C.g(curr) * i;
      int n = C.b(prev) * j + C.b(curr) * i;
      return C.argb_safe(255, k / 32, m / 32, n / 32);
    }

    public void init() { }
    public void move(double dx) { }
    public abstract Obstacle generateObstacle();
  }

  private class NormalRound extends RoundManager {
    private int interval;
    private int counter;

    public NormalRound(int round_score, int sky_rgb, int ground_rgb, int interval) { this.nextRoundScore = round_score; this.sky_rgb = sky_rgb; this.ground_rgb = ground_rgb; this.interval = interval; }

    public Obstacle generateObstacle() {
      this.gameTime++;
      this.counter++;
      if (this.counter < this.interval) return null;
      this.counter = 0;
      return createObstacle();
    }

    public void init() {
      this.counter = 0;
      this.gameTime = 0;
    }
  }

  private class RoadRound extends RoundManager {
    private double OX1;
    private double OX2;
    private double OVX;
    private double WX;
    private int direction;
    private int roadCounter;
    private boolean isBrokenRoad;

    public RoadRound(int score, int sky_rgb, int ground_rgb, boolean is_broken) { this.nextRoundScore = score; this.sky_rgb = sky_rgb; this.ground_rgb = ground_rgb; this.isBrokenRoad = is_broken; }

    public Obstacle generateObstacle() {
      double d2;
      this.gameTime++;
      this.roadCounter--;
      double d1 = 1.1;
      if (this.isBrokenRoad && this.roadCounter % 13 < 7) {
        d1 = 0.7;
        d2 = M.drand(-16.0, 16.0);
        if (d2 < this.OX2 && d2 > this.OX1) {
          d1 = 1.2;
          if (M.coin()) {
            d2 = this.OX1;
          } else {
            d2 = this.OX2;
          }
        }
      } else if (this.roadCounter % 2 == 0) {
        d2 = this.OX1;
      } else {
        d2 = this.OX2;
      }
      if (this.OX2 - this.OX1 > 9.0) {
        this.OX1 += 0.6;
        this.OX2 -= 0.6;
        if (this.OX2 - this.OX1 > 10.0)
          d1 = 2.0;
      } else if (this.OX1 > 18.0) {
        this.OX2 -= 0.6;
        this.OX1 -= 0.6;
      } else if (this.OX2 < -18.0) {
        this.OX2 += 0.6;
        this.OX1 += 0.6;
      } else {
        if (this.roadCounter < 0) {
          this.direction = -this.direction;
          this.roadCounter += 2 * M.rand(4, 11);
        }
        if (this.direction > 0) {
          this.OVX += 0.125;
        } else {
          this.OVX -= 0.125;
        }
        if (this.OVX > 0.7)
          this.OVX = 0.7;
        if (this.OVX < -0.7)
          this.OVX = -0.7;
        this.OX1 += this.OVX;
        this.OX2 += this.OVX;
      }
      return createObstacle(d2, d1);
    }

    public void init() {
      this.OX1 = -17.0;
      this.OX2 = 17.0;
      this.OVX = 0.0;
      this.roadCounter = 0;
      this.direction = 1;
      this.gameTime = 0;
    }

    public void move(double dx) {
      this.OX1 += dx;
      this.OX2 += dx;
    }
  }

}