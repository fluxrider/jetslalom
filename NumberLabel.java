import java.awt.*;

public class NumberLabel extends Canvas {

  private char[] data = new char[] { '0', '0', '0', '0', '0', '0' };

  public void setNum(int value) {
    for(int i = data.length - 1; i >= 0; i--) {
      this.data[i] = (char)(value % 10 + '0');
      value /= 10;
    }
    Graphics g = this.getGraphics();
    g.clearRect(0, 0, this.getWidth(), this.getHeight());
    this.paint(g);
  }

  public NumberLabel(int width, int height) {
    this.setSize(width, height);
    this.setFocusable(false);
  }

  public void paint(Graphics g) {
    g.setColor(Color.black);
    g.drawChars(this.data, 0, this.data.length, 4, this.getHeight());
  }

  public Dimension getPreferredSize() {
    return this.getSize();
  }

}