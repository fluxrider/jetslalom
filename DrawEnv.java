import java.awt.*;

public class DrawEnv {
  static final double T = 0.6D;
  
  private int[] polyX = new int[8];
  
  private int[] polyY = new int[8];
  
  private DPoint3[] dp3 = new DPoint3[3];
  
  double nowSin;
  
  double nowCos;
  
  int width;
  
  int height;
  
  synchronized void drawPolygon(Graphics paramGraphics, Face paramFace) {
    int i = paramFace.numPoints;
    DPoint3[] arrayOfDPoint3 = paramFace.points;
    double d1 = (arrayOfDPoint3[1]).x - (arrayOfDPoint3[0]).x;
    double d2 = (arrayOfDPoint3[1]).y - (arrayOfDPoint3[0]).y;
    // DAVE arrayOfDPoint3[1];
    // DAVE arrayOfDPoint3[0];
    double d3 = (arrayOfDPoint3[2]).x - (arrayOfDPoint3[0]).x;
    double d4 = (arrayOfDPoint3[2]).y - (arrayOfDPoint3[0]).y;
    // DAVE arrayOfDPoint3[2];
    // DAVE arrayOfDPoint3[0];
    float f = (float)(Math.abs(d1 * d4 - d2 * d3) / paramFace.maxZ);
    paramGraphics.setColor(new Color(paramFace.red * f, paramFace.green * f, paramFace.blue * f));
    double d5 = this.width / 320.0D;
    double d6 = this.height / 200.0D;
    for (byte b = 0; b < i; b++) {
      DPoint3 dPoint3 = arrayOfDPoint3[b];
      double d7 = 120.0D / (1.0D + 0.6D * dPoint3.z);
      double d8 = this.nowCos * dPoint3.x + this.nowSin * (dPoint3.y - 2.0D);
      double d9 = -this.nowSin * dPoint3.x + this.nowCos * (dPoint3.y - 2.0D) + 2.0D;
      this.polyX[b] = (int)(d8 * d5 * d7) + this.width / 2;
      this.polyY[b] = (int)(d9 * d6 * d7) + this.height / 2;
    } 
    paramGraphics.fillPolygon(this.polyX, this.polyY, i);
  }
  
  synchronized void drawPolygon(Graphics paramGraphics, DPoint3[] paramArrayOfDPoint3) {
    int i = paramArrayOfDPoint3.length;
    double d1 = this.width / 320.0D;
    double d2 = this.height / 200.0D;
    for (byte b = 0; b < i; b++) {
      DPoint3 dPoint3 = paramArrayOfDPoint3[b];
      double d3 = 120.0D / (1.0D + 0.6D * dPoint3.z);
      double d4 = this.nowCos * dPoint3.x + this.nowSin * (dPoint3.y - 2.0D);
      double d5 = -this.nowSin * dPoint3.x + this.nowCos * (dPoint3.y - 2.0D) + 2.0D;
      this.polyX[b] = (int)(d4 * d1 * d3) + this.width / 2;
      this.polyY[b] = (int)(d5 * d2 * d3) + this.height / 2;
    } 
    paramGraphics.fillPolygon(this.polyX, this.polyY, i);
  }
}