import java.awt.Color;

public class Face {
  DPoint3[] points;
  
  int numPoints;
  
  double maxZ;
  
  float red;
  
  float green;
  
  float blue;
  
  void setColor(Color paramColor) {
    this.red = paramColor.getRed() / 255.0F;
    this.green = paramColor.getGreen() / 255.0F;
    this.blue = paramColor.getBlue() / 255.0F;
  }
  
  void calcMaxZ() {
    double d1 = (this.points[1]).x - (this.points[0]).x;
    double d2 = (this.points[1]).y - (this.points[0]).y;
    double d3 = (this.points[1]).z - (this.points[0]).z;
    double d4 = (this.points[2]).x - (this.points[0]).x;
    double d5 = (this.points[2]).y - (this.points[0]).y;
    double d6 = (this.points[2]).z - (this.points[0]).z;
    this.maxZ = Math.sqrt(two(d2 * d6 - d3 * d5) + two(d1 * d6 - d3 * d4) + two(d1 * d5 - d2 * d4));
  }
  
  private final double two(double paramDouble) {
    return paramDouble * paramDouble;
  }
}


/* Location:              C:\a\!\Face.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */