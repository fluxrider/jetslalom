import java.awt.*;

public class RoadRound extends RoundManager {
  private double OX1;
  private double OX2;
  private double OVX;
  private double WX;
  private int direction;
  private int roadCounter;
  private boolean isBrokenRoad;

  public RoadRound(int score, Color sky_color, Color ground_color, boolean is_broken) { this.nextRoundScore = score; this.skyColor = sky_color; this.groundColor = ground_color; this.isBrokenRoad = is_broken; }

  public Obstacle generateObstacle() {
    double d2;
    this.gameTime++;
    this.roadCounter--;
    double d1 = 1.1;
    if (this.isBrokenRoad && this.roadCounter % 13 < 7) {
      d1 = 0.7;
      d2 = (Main.getRandom() % 256) / 8.0 - 16.0;
      if (d2 < this.OX2 && d2 > this.OX1) {
        d1 = 1.2;
        if (Main.getRandom() % 256 > 128) {
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
        this.roadCounter += 2 * (Main.getRandom() % 8 + 4);
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