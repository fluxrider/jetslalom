public class Face {
  DPoint3[] points;
  double maxZ;
  int rgb;
  void calcMaxZ() {
    double d1 = (this.points[1]).x - (this.points[0]).x;
    double d2 = (this.points[1]).y - (this.points[0]).y;
    double d3 = (this.points[1]).z - (this.points[0]).z;
    double d4 = (this.points[2]).x - (this.points[0]).x;
    double d5 = (this.points[2]).y - (this.points[0]).y;
    double d6 = (this.points[2]).z - (this.points[0]).z;
    double a = d2 * d6 - d3 * d5;
    double b = d1 * d6 - d3 * d4;
    double c = d1 * d5 - d2 * d4;
    this.maxZ = Math.sqrt(a*a + b*b + c*c);
  }
}