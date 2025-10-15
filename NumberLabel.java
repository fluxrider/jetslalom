import java.awt.*;

public class NumberLabel extends Canvas {
  char[] data = new char[6];
  
  Graphics gra;
  
  int width;
  
  int height;
  
  public void setNum(int paramInt) {
    byte b = 0;
    while (true) {
      this.data[5 - b] = (char)(paramInt % 10 + 48);
      paramInt /= 10;
      if (++b >= 6) {
        if (this.gra == null)
          this.gra = getGraphics(); 
        this.gra.clearRect(0, 0, this.width, this.height);
        paint(this.gra);
        return;
      } 
    } 
  }
  
  public NumberLabel(int paramInt1, int paramInt2) {
    byte b = 0;
    while (true) {
      this.data[b] = '0';
      if (++b >= 6) {
        this.width = paramInt1;
        this.height = paramInt2;
        setSize(paramInt1, paramInt2);
        return;
      } 
    } 
  }
  
  public void paint(Graphics paramGraphics) {
    paramGraphics.setColor(Color.black);
    paramGraphics.drawChars(this.data, 0, 6, 4, this.height);
  }
  
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
}