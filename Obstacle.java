import java.awt.*;

public class Obstacle {

  DPoint3[] points = new DPoint3[] { new DPoint3(), new DPoint3(), new DPoint3(), new DPoint3() };
  Face[] faces = new Face[] { new Face(), new Face(), new Face() };

  Color color;

  public Obstacle() {
    this.faces[0].points = new DPoint3[] { this.points[3], this.points[0], this.points[1] };
    this.faces[1].points = new DPoint3[] { this.points[3], this.points[2], this.points[1] };
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