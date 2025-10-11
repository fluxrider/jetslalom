import java.awt.Color;

public class NormalRound extends RoundManager {
  private int interval;
  
  private int counter;
  
  public NormalRound(int paramInt1, Color paramColor1, Color paramColor2, int paramInt2) {
    this.nextRoundScore = paramInt1;
    this.skyColor = paramColor1;
    this.groundColor = paramColor2;
    this.interval = paramInt2;
  }
  
  public void generateObstacle(ObstacleCollection paramObstacleCollection, GameRecorder paramGameRecorder) {
    this.gameTime++;
    this.counter++;
    if (this.counter < this.interval)
      return; 
    this.counter = 0;
    Obstacle obstacle = createObstacle(paramGameRecorder, 0.6D);
    paramObstacleCollection.add(obstacle);
  }
  
  public void init() {
    this.counter = 0;
    this.gameTime = 0;
  }
}


/* Location:              C:\a\!\NormalRound.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */