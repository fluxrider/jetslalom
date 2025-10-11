import java.awt.Graphics;

public class ObstacleCollection {
  Obstacle head = new Obstacle();
  
  Obstacle tail = new Obstacle();
  
  synchronized void removeAll() {
    for (Obstacle obstacle = this.head.next; obstacle != this.tail; ) {
      Obstacle obstacle1 = obstacle.next;
      obstacle.release();
      obstacle = obstacle1;
    } 
  }
  
  synchronized void draw(Graphics paramGraphics, DrawEnv paramDrawEnv) {
    for (Obstacle obstacle = this.head.next; obstacle != this.tail; obstacle = obstacle.next)
      obstacle.draw(paramGraphics, paramDrawEnv); 
  }
  
  ObstacleCollection() {
    this.head.next = this.tail;
    this.tail.prev = this.head;
  }
  
  synchronized void add(Obstacle paramObstacle) {
    paramObstacle.prev = this.head;
    paramObstacle.next = this.head.next;
    this.head.next.prev = paramObstacle;
    this.head.next = paramObstacle;
  }
}


/* Location:              C:\a\!\ObstacleCollection.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */