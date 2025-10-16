import java.awt.*;

public class RoadRound extends RoundManager {
  private double OX1;

  private double OX2;

  private double OVX;

  private double WX;

  private int direction;

  private int roadCounter;

  private boolean isBrokenRoad;

  public RoadRound(int paramInt, Color paramColor1, Color paramColor2, boolean paramBoolean) {
    this.nextRoundScore = paramInt;
    this.skyColor = paramColor1;
    this.groundColor = paramColor2;
    this.isBrokenRoad = paramBoolean;
  }

  public Obstacle generateObstacle() {
    double d2;
    this.gameTime++;
    this.roadCounter--;
    double d1 = 1.1D;
    if (this.isBrokenRoad && this.roadCounter % 13 < 7) {
      d1 = 0.7D;
      d2 = (MainGame.getRandom() % 256) / 8.0D - 16.0D;
      if (d2 < this.OX2 && d2 > this.OX1) {
        d1 = 1.2D;
        if (MainGame.getRandom() % 256 > 128) {
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
    if (this.OX2 - this.OX1 > 9.0D) {
      this.OX1 += 0.6D;
      this.OX2 -= 0.6D;
      if (this.OX2 - this.OX1 > 10.0D)
        d1 = 2.0D;
    } else if (this.OX1 > 18.0D) {
      this.OX2 -= 0.6D;
      this.OX1 -= 0.6D;
    } else if (this.OX2 < -18.0D) {
      this.OX2 += 0.6D;
      this.OX1 += 0.6D;
    } else {
      if (this.roadCounter < 0) {
        this.direction = -this.direction;
        this.roadCounter += 2 * (MainGame.getRandom() % 8 + 4);
      }
      if (this.direction > 0) {
        this.OVX += 0.125D;
      } else {
        this.OVX -= 0.125D;
      }
      if (this.OVX > 0.7D)
        this.OVX = 0.7D;
      if (this.OVX < -0.7D)
        this.OVX = -0.7D;
      this.OX1 += this.OVX;
      this.OX2 += this.OVX;
    }
    return createObstacle(d2, d1);
  }

  public void init() {
    this.OX1 = -17.0D;
    this.OX2 = 17.0D;
    this.OVX = 0.0D;
    this.roadCounter = 0;
    this.direction = 1;
    this.gameTime = 0;
  }

  public void move(double paramDouble) {
    this.OX1 += paramDouble;
    this.OX2 += paramDouble;
  }
}