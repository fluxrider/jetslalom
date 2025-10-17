public abstract class RoundManager {
  private RoundManager prevRound;

  static final int[] rgbs = new int[] { ARGB.gray(192), ARGB.rgb(96, 160, 240), ARGB.rgb(200, 128, 0), ARGB.rgb(240, 210, 100) };

  protected int nextRoundScore;
  protected int sky_rgb;
  protected int ground_rgb;
  protected int gameTime;
  public void setPrevRound(RoundManager round) { this.prevRound = round; }
  public int getGroundRGB() { return this.ground_rgb; }
  public int getNextRoundScore() { return this.nextRoundScore; }

  protected final Obstacle createObstacle(double x1, double x2) {
    Obstacle obstacle = new Obstacle();
    DPoint3[] arrayOfDPoint3 = obstacle.points;
    arrayOfDPoint3[0].setXYZ(x1 - x2, 2.0, 25.5);
    arrayOfDPoint3[1].setXYZ(x1, -1.4, 25.0);
    arrayOfDPoint3[2].setXYZ(x1 + x2, 2.0, 25.5);
    arrayOfDPoint3[3].setXYZ(x1, 2.0, 24.5);
    obstacle.rgb = rgbs[Main.getRandom() % 4];
    obstacle.prepareNewObstacle();
    return obstacle;
  }

  protected final Obstacle createObstacle(double paramDouble) { return createObstacle((Main.getRandom() % 256) / 8.0 - 16.0, 0.6); }

  public boolean isNextRound(int score) { return !(score < this.nextRoundScore); }

  public int getSkyRGB() {
    if (this.prevRound == null || this.gameTime > 32) return this.sky_rgb;
    int i = this.gameTime;
    int j = 32 - i;
    int prev = this.prevRound.sky_rgb;
    int curr = this.sky_rgb;
    int k = ARGB.r(prev) * j + ARGB.r(curr) * i;
    int m = ARGB.g(prev) * j + ARGB.g(curr) * i;
    int n = ARGB.b(prev) * j + ARGB.b(curr) * i;
    return ARGB.argb_safe(255, k / 32, m / 32, n / 32);
  }

  public void init() { }
  public void move(double dx) { }
  public abstract Obstacle generateObstacle();
}