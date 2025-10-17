public class NormalRound extends RoundManager {
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