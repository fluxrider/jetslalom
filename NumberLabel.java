import java.awt.*;

public class NumberLabel extends Canvas {

  private char[] data = new char[6];
  private int width;
  private int height;

  public void setNum(int value) {
    byte b = 0;
    while (true) {
      this.data[5 - b] = (char)(value % 10 + 48);
      value /= 10;
      if (++b >= 6) {
        Graphics g =  getGraphics();
        g.clearRect(0, 0, this.width, this.height);
        paint(g);
        return;
      }
    }
  }

  public NumberLabel(int width, int height) {
    byte b = 0;
    while (true) {
      this.data[b] = '0';
      if (++b >= 6) {
        this.width = width;
        this.height = height;
        setSize(width, height);
        return;
      }
    }
  }

  public void paint(Graphics g) {
    g.setColor(Color.black);
    g.drawChars(this.data, 0, 6, 4, this.height);
  }

  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
}