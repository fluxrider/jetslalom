import java.util.concurrent.ThreadLocalRandom;

public class M {

  // I find you can easily shoot yourself in the foot with this standard library class. Besides, the best practice changes all the time.
  
  // [0, INT_MAX) the classic, never negative
  public static int rand() { return ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE); }

  // [0, 1) the weird, never 1
  public static double drand() { return ThreadLocalRandom.current().nextDouble(); }
  
  // [min, max]
  public static int rand(int min, int max) {
    if(min > max) throw new IllegalArgumentException(String.format("min > max (%d > %d)", min, max));
    return min + rand() % (max - min + 1);
  }
  public static double drand(double min, double max) {
    if(min > max) throw new IllegalArgumentException(String.format("min > max (%f > %f)", min, max));
    if(min == max) return min;
    // Original GPLv2 version copied on 2025-10-17 from https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/jdk/internal/util/random/RandomSupport.java
    // I'm adapting it to claim max as inclusive. I know the probability of a random number to be exactly min or max is mathematically 0 anyway, but practically it's not and it bothers me that max is never included. That's only ever meaningful to avoid array out of bound, which is not the sole use case of random numbers.
    double r = drand();
    if(max - min < Double.POSITIVE_INFINITY) r = r * (max - min) + min; else { double half_min = 0.5 * min; r = (r * (0.5 * max - half_min) + half_min) * 2.0; }
    if (r > max) r = max; // the original code gives nextDown(max). It is unclear if this rounding error ever happens anyway, but now I can sleep thinking my function is max inclusive.
    return r;
  }
  
  // games
  public static boolean coin() { return ThreadLocalRandom.current().nextBoolean(); }
  public static int d4() { return rand(1, 4); }
  public static int d6() { return rand(1, 6); }
  public static int d8() { return rand(1, 8); }
  public static int d12() { return rand(1, 12); }
  public static int d20() { return rand(1, 20); }
  public static int d(int faces) { return rand(1, faces); }
  
  // array utils
  public static int rand_index(int length) { return rand(0, length - 1); }
  public static <T> T rand(T t[]) { return t[rand_index(t.length)]; }
}