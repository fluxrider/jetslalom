import java.awt.*;

public class Ground extends DrawObject {
  DPoint3[] points = new DPoint3[] { new DPoint3(-100.0D, 2.0D, 28.0D), new DPoint3(-100.0D, 2.0D, 0.1D), new DPoint3(100.0D, 2.0D, 0.1D), new DPoint3(100.0D, 2.0D, 28.0D) };
  
  Color color;
  
  void draw(Graphics g) {
    g.setColor(this.color);
    DrawEnv.drawPolygon(g, this.points);
  }
}