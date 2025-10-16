import java.awt.*;

public class NumberLabel extends Canvas {

  private char[] data = new char[] { '0', '0', '0', '0', '0', '0' };
  private int width;
  private int height;

  public void setNum(int value) {
    for(int i = data.length - 1; i >= 0; i--) {
      this.data[i] = (char)(value % 10 + '0');
      value /= 10;
    }
    Graphics g = getGraphics();
    g.clearRect(0, 0, this.width, this.height);
    paint(g);
  }

  public NumberLabel(int width, int height) {
    this.width = width;
    this.height = height;
    setSize(width, height);
  }

  public void paint(Graphics g) {
    g.setColor(Color.black);
    g.drawChars(this.data, 0, this.data.length, 4, this.height);
  }

  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }

}