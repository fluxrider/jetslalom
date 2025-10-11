import java.awt.Color;
import java.awt.Graphics;

public class Ground extends DrawObject {
  DPoint3[] points = new DPoint3[] { new DPoint3(-100.0D, 2.0D, 28.0D), new DPoint3(-100.0D, 2.0D, 0.1D), new DPoint3(100.0D, 2.0D, 0.1D), new DPoint3(100.0D, 2.0D, 28.0D) };
  
  Color color;
  
  void draw(Graphics paramGraphics, DrawEnv paramDrawEnv) {
    paramGraphics.setColor(this.color);
    paramDrawEnv.drawPolygon(paramGraphics, this.points);
  }
}


/* Location:              C:\a\!\Ground.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */