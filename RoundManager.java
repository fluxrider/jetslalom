import java.awt.*;

public abstract class RoundManager {
  private RoundManager prevRound;

  static final Color[] colors = new Color[] { Color.lightGray, new Color(96, 160, 240), new Color(200, 128, 0), new Color(240, 210, 100) };

  protected int nextRoundScore;

  protected Color skyColor;

  protected Color groundColor;

  protected int gameTime;

  protected final Obstacle createObstacle(double paramDouble1, double paramDouble2) {
    Obstacle obstacle = new Obstacle();
    DPoint3[] arrayOfDPoint3 = obstacle.points;
    arrayOfDPoint3[0].setXYZ(paramDouble1 - paramDouble2, 2.0D, 25.5D);
    arrayOfDPoint3[1].setXYZ(paramDouble1, -1.4D, 25.0D);
    arrayOfDPoint3[2].setXYZ(paramDouble1 + paramDouble2, 2.0D, 25.5D);
    arrayOfDPoint3[3].setXYZ(paramDouble1, 2.0D, 24.5D);
    obstacle.color = colors[MainGame.getRandom() % 4];
    obstacle.prepareNewObstacle();
    return obstacle;
  }

  protected final Obstacle createObstacle(double paramDouble) {
    double d = (MainGame.getRandom() % 256) / 8.0D - 16.0D;
    return createObstacle(d, 0.6D);
  }

  public abstract Obstacle generateObstacle();

  public boolean isNextRound(int paramInt) {
    return !(paramInt < this.nextRoundScore);
  }

  public void setPrevRound(RoundManager paramRoundManager) {
    this.prevRound = paramRoundManager;
  }

  public Color getGroundColor() {
    return this.groundColor;
  }

  public int getNextRoundScore() {
    return this.nextRoundScore;
  }

  public void init() {}

  public Color getSkyColor() {
    if (this.prevRound == null || this.gameTime > 32)
      return this.skyColor;
    int i = this.gameTime;
    int j = 32 - i;
    Color color = this.prevRound.skyColor;
    int k = color.getRed() * j + this.skyColor.getRed() * i;
    int m = color.getGreen() * j + this.skyColor.getGreen() * i;
    int n = color.getBlue() * j + this.skyColor.getBlue() * i;
    return new Color(k / 32, m / 32, n / 32);
  }

  public void move(double paramDouble) {}
}