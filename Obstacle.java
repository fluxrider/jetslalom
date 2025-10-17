public class Obstacle {

  DPoint3[] points = new DPoint3[] { new DPoint3(), new DPoint3(), new DPoint3(), new DPoint3() };
  Face[] faces = new Face[] { new Face(), new Face(), new Face() };

  int rgb;

  public Obstacle() {
    this.faces[0].points = new DPoint3[] { this.points[3], this.points[0], this.points[1] };
    this.faces[1].points = new DPoint3[] { this.points[3], this.points[2], this.points[1] };
  }

  void prepareNewObstacle() {
    this.faces[0].calcMaxZ(); this.faces[0].rgb = ARGB.brighter(this.rgb);
    this.faces[1].calcMaxZ(); this.faces[1].rgb = this.rgb;
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