import java.awt.*;

public class Obstacle {

  // pool of obstacles
  private static Obstacle head = null; static { for(int i = 0; i < 16; i++) { Obstacle obstacle = new Obstacle(); obstacle.next = head; head = obstacle; } }
  static synchronized void releaseObstacle(Obstacle obstacle) { if (obstacle != null) { obstacle.next = head; head = obstacle; } }
  static synchronized Obstacle newObstacle() {
    if(head == null) return new Obstacle();
    Obstacle obstacle = head; head = head.next; obstacle.next = null; return obstacle;
  }

  DPoint3[] points = new DPoint3[] { new DPoint3(), new DPoint3(), new DPoint3(), new DPoint3() };
  Face[] faces = new Face[] { new Face(), new Face(), new Face() };

  Obstacle next = null;
  Obstacle prev = null;

  Color color;

  Obstacle() {
    this.faces[0].points = new DPoint3[] { this.points[3], this.points[0], this.points[1] };
    this.faces[1].points = new DPoint3[] { this.points[3], this.points[2], this.points[1] };
  }

  void release() {
    this.prev.next = this.next;
    this.next.prev = this.prev;
    releaseObstacle(this);
  }

  void draw(Graphics g) {
    DrawEnv.drawPolygon(g, this.faces[0]);
    DrawEnv.drawPolygon(g, this.faces[1]);
  }

  void prepareNewObstacle() {
    this.faces[0].setColor(this.color.brighter());
    this.faces[0].calcMaxZ();
    this.faces[1].setColor(this.color);
    this.faces[1].calcMaxZ();
  }

  void move(double x, double y, double z) {
    for(int i = 0; i < 4; i++) {
      DPoint3 dPoint3 = this.points[i];
      dPoint3.x += x;
      dPoint3.y += y;
      dPoint3.z += z;
    }
  }
}