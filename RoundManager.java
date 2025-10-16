import java.awt.*;

public abstract class RoundManager {
  private RoundManager prevRound;

  static final Color[] colors = new Color[] { Color.lightGray, new Color(96, 160, 240), new Color(200, 128, 0), new Color(240, 210, 100) };

  protected int nextRoundScore;
  protected Color skyColor;
  protected Color groundColor;
  protected int gameTime;
  public void setPrevRound(RoundManager round) { this.prevRound = round; }
  public Color getGroundColor() { return this.groundColor; }
  public int getNextRoundScore() { return this.nextRoundScore; }

  protected final Obstacle createObstacle(double x1, double x2) {
    Obstacle obstacle = new Obstacle();
    DPoint3[] arrayOfDPoint3 = obstacle.points;
    arrayOfDPoint3[0].setXYZ(x1 - x2, 2.0, 25.5);
    arrayOfDPoint3[1].setXYZ(x1, -1.4, 25.0);
    arrayOfDPoint3[2].setXYZ(x1 + x2, 2.0, 25.5);
    arrayOfDPoint3[3].setXYZ(x1, 2.0, 24.5);
    obstacle.color = colors[MainGame.getRandom() % 4];
    obstacle.prepareNewObstacle();
    return obstacle;
  }

  protected final Obstacle createObstacle(double paramDouble) { return createObstacle((MainGame.getRandom() % 256) / 8.0 - 16.0, 0.6); }

  public boolean isNextRound(int score) { return !(score < this.nextRoundScore); }

  public Color getSkyColor() {
    if (this.prevRound == null || this.gameTime > 32) return this.skyColor;
    int i = this.gameTime;
    int j = 32 - i;
    Color color = this.prevRound.skyColor;
    int k = color.getRed() * j + this.skyColor.getRed() * i;
    int m = color.getGreen() * j + this.skyColor.getGreen() * i;
    int n = color.getBlue() * j + this.skyColor.getBlue() * i;
    return new Color(k / 32, m / 32, n / 32);
  }

  public void init() { }
  public void move(double dx) { }
  public abstract Obstacle generateObstacle();
}