public final class C { private C() { }

  public static int a(int argb) { return (argb >> 24) & 0xFF; }
  public static int r(int argb) { return (argb >> 16) & 0xFF; }
  public static int g(int argb) { return (argb >> 8) & 0xFF; }
  public static int b(int argb) { return argb & 0xFF; }
  public static int argb_safe(int a, int r, int g, int b) {
    if(a < 0) a = 0; else if(a > 255) a = 255;
    if(r < 0) r = 0; else if(r > 255) r = 255;
    if(g < 0) g = 0; else if(g > 255) g = 255;
    if(b < 0) b = 0; else if(b > 255) b = 255;
    return a << 24 | r << 16 | g << 8 | b;
  }
  public static int argb(int a, int r, int g, int b) {
    if(a < 0 || a > 255 || r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) throw new RuntimeException(String.format("Color argb components out of range [0-255]: %d %d %d %d", a, r, g, b));
    return a << 24 | r << 16 | g << 8 | b;
  }
  public static int rgb(int r, int g, int b) { return argb(255, r, g, b); }
  public static int dargb(double a, double r, double g, double b) { return argb((int)(a*255),(int)(r*255),(int)(g*255),(int)(b*255)); }
  public static int drgb(double r, double g, double b) { return argb(255,(int)(r*255),(int)(g*255),(int)(b*255)); }
  public static float fa(int argb) { return a(argb) / 255.0f; }
  public static float fr(int argb) { return r(argb) / 255.0f; }
  public static float fg(int argb) { return g(argb) / 255.0f; }
  public static float fb(int argb) { return b(argb) / 255.0f; }
  public static double da(int argb) { return a(argb) / 255.0; }
  public static double dr(int argb) { return r(argb) / 255.0; }
  public static double dg(int argb) { return g(argb) / 255.0; }
  public static double db(int argb) { return b(argb) / 255.0; }
  public static int gray(int level) { return argb(255, level, level, level); }
  public static int dgray(double level) { return gray((int)(level*255)); }
  
  public static final int black = argb(255, 0, 0, 0);
  public static final int white = argb(255, 255, 255, 255);

  public static int brighter(int argb) {
    // Original GPLv2 version copied on 2025-10-17 from https://github.com/openjdk/jdk/blob/master/src/java.desktop/share/classes/java/awt/Color.java
    // Note: I fixed a slight inconsistency found in the original code that made a 1 or 2 get a big kick to 4 instead of what I think was meant to be just 3.
    int r = r(argb); int g = g(argb); int b = b(argb); int a = a(argb);
    if(r == 0 && g == 0 && b == 0) { return argb(a, 3, 3, 3); }
    return argb_safe(a, (r > 0 && r < 3)? 3 : (int)(r*M.sqrt2), (g > 0 && g < 3)? 3 : (int)(g*M.sqrt2), (b > 0 && b < 3)? 3 : (int)(b*M.sqrt2));
  }

}