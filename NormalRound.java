import java.awt.*;

public class NormalRound extends RoundManager {
  private int interval;
  private int counter;
  
  public NormalRound(int round_score, Color sky_color, Color ground_color, int interval) {
    this.nextRoundScore = round_score;
    this.skyColor = sky_color;
    this.groundColor = ground_color;
    this.interval = interval;
  }
  
  public void generateObstacle(ObstacleCollection obstacles) {
    this.gameTime++;
    this.counter++;
    if (this.counter < this.interval) return; 
    this.counter = 0;
    Obstacle obstacle = createObstacle(0.6);
    obstacles.add(obstacle);
  }
  
  public void init() {
    this.counter = 0;
    this.gameTime = 0;
  }
}