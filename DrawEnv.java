import java.awt.*;

public class DrawEnv {
  // preallocate buffers
  private static int[] buffer_polyX = new int[8];  
  private static int[] buffer_polyY = new int[8];
  
  public static double nowSin;
  public static double nowCos;
  
  public static int width;
  public static int height;
  
  synchronized static void drawPolygon(Graphics g, Face face) {
    DPoint3[] points = face.points;
    double d1 = (points[1]).x - (points[0]).x;
    double d2 = (points[1]).y - (points[0]).y;
    double d3 = (points[2]).x - (points[0]).x;
    double d4 = (points[2]).y - (points[0]).y;
    float f = (float)(Math.abs(d1 * d4 - d2 * d3) / face.maxZ);
    g.setColor(new Color(face.red * f, face.green * f, face.blue * f));
    drawPolygon(g, points);
  }
  
  synchronized static void drawPolygon(Graphics g, DPoint3[] points) {
    double d1 = width / 320.0;
    double d2 = height / 200.0;
    for (byte b = 0; b < points.length; b++) {
      DPoint3 point = points[b];
      double d3 = 120.0 / (1.0 + 0.6 * point.z);
      double d4 = nowCos * point.x + nowSin * (point.y - 2.0);
      double d5 = -nowSin * point.x + nowCos * (point.y - 2.0) + 2.0;
      buffer_polyX[b] = (int)(d4 * d1 * d3) + width / 2;
      buffer_polyY[b] = (int)(d5 * d2 * d3) + height / 2;
    }
    g.fillPolygon(buffer_polyX, buffer_polyY, points.length);
  }
}