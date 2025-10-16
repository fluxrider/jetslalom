import java.awt.*;

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

  synchronized void draw(Graphics g) {
    for (Obstacle obstacle = this.head.next; obstacle != this.tail; obstacle = obstacle.next)
      obstacle.draw(g);
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