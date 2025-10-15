import java.io.*;

public class GameRecorder {
  public static final int LEFT = 1;
  
  public static final int RIGHT = 2;
  
  private RandomGenerator random = new RandomGenerator(this.seed);
  
  private int seed = (int)System.currentTimeMillis();
  
  private int[] data = new int[2048];
  
  private int pos;
  
  private int maxpos = 0;
  
  public int startScore;
  
  public int startRound;
  
  public void writeStatus(int paramInt) {
    if (this.pos >= this.data.length * 16)
      return; 
    this.data[this.pos / 16] = this.data[this.pos / 16] | paramInt << this.pos % 16 * 2;
    this.pos++;
    if (this.pos > this.maxpos)
      this.maxpos = this.pos; 
  }
  
  public int readStatus() {
    if (this.pos >= this.data.length * 16)
      return 0; 
    int i = this.data[this.pos / 16] >>> this.pos % 16 * 2;
    this.pos++;
    return i & 0x3;
  }
  
  public void load(InputStream paramInputStream) throws IOException {
    DataInputStream dataInputStream = new DataInputStream(paramInputStream);
    this.seed = dataInputStream.readInt();
    this.maxpos = dataInputStream.readInt();
    this.startRound = dataInputStream.readInt();
    this.startScore = dataInputStream.readInt();
    for (byte b = 0; b < this.maxpos; b++)
      this.data[b] = dataInputStream.readInt(); 
  }
  
  public void save(OutputStream paramOutputStream) throws IOException {
    DataOutputStream dataOutputStream = new DataOutputStream(paramOutputStream);
    dataOutputStream.writeInt(this.seed);
    dataOutputStream.writeInt(this.maxpos);
    dataOutputStream.writeInt(this.startRound);
    dataOutputStream.writeInt(this.startScore);
    for (byte b = 0; b < this.maxpos; b++)
      dataOutputStream.writeInt(this.data[b]); 
  }
  
  public int getRandom() {
    return this.random.nextInt() & Integer.MAX_VALUE;
  }
  
  public void toStart() {
    this.pos = 0;
    this.random.setSeed(this.seed);
  }
}