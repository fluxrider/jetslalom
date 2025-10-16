import java.awt.*;

class StringObject {
  private Graphics currentGra = null;

  private Font font;

  private String str;

  private Color color;

  private int x;

  private int y;

  boolean isUnderLine = false;

  private int strWidth;

  private int strHeight;

  private int align_ = 0;

  public static final int CENTER = 0;

  public static final int LEFT = 1;

  void draw(Graphics paramGraphics, DrawEnv paramDrawEnv) {
    if (this.str == null)
      return;
    paramGraphics.setColor(this.color);
    paramGraphics.setFont(this.font);
    if (paramGraphics != this.currentGra || this.strWidth < 0) {
      this.currentGra = paramGraphics;
      setStrSize();
    }
    int i = this.x;
    if (this.align_ == 0)
      i = this.x - this.strWidth / 2;
    paramGraphics.drawString(this.str, i, this.y + this.strHeight / 2);
    if (this.isUnderLine)
      paramGraphics.drawLine(i, this.y + this.strHeight / 2 + 1, i + this.strWidth, this.y + this.strHeight / 2 + 1);
  }

  StringObject(Font paramFont, Color paramColor, String paramString, int paramInt1, int paramInt2) {
    this.font = paramFont;
    this.str = paramString;
    this.color = paramColor;
    this.x = paramInt1;
    this.y = paramInt2;
    this.strWidth = -1;
  }

  void setColor(Color paramColor) {
    this.color = paramColor;
  }

  void setAlign(int paramInt) {
    this.align_ = paramInt;
  }

  int getAlign() {
    return this.align_;
  }

  boolean hitTest(int paramInt1, int paramInt2) {
    return (this.currentGra == null) ? false : (!(this.x - this.strWidth / 2 >= paramInt1 || paramInt1 >= this.x + this.strWidth / 2 || this.y - this.strHeight / 2 >= paramInt2 || paramInt2 >= this.y + this.strHeight / 2));
  }

  private void setStrSize() {
    FontMetrics fontMetrics = this.currentGra.getFontMetrics();
    this.strWidth = fontMetrics.stringWidth(this.str);
    this.strHeight = fontMetrics.getHeight();
  }

  void setText(String paramString) {
    if (paramString == this.str)
      return;
    if (paramString != null && paramString.equals(this.str))
      return;
    this.str = paramString;
    this.strWidth = -1;
  }
}