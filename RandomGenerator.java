public class RandomGenerator {
  private int seed;
  
  public RandomGenerator(int paramInt) {
    this.seed = paramInt;
  }
  
  public void setSeed(int paramInt) {
    this.seed = paramInt;
  }
  
  public int nextInt() {
    this.seed = this.seed * 1593227 + 13;
    return this.seed >>> 16;
  }
}


/* Location:              C:\a\!\RandomGenerator.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       1.1.3
 */