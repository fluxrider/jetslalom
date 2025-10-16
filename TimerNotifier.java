public class TimerNotifier extends Thread {
  private volatile int interval;

  private volatile boolean notifyFlag = false;

  public TimerNotifier(int paramInt) {
    this.interval = paramInt;
    setName("TimerNotifier");
    //System.out.println(10);
    setPriority(10);
    start();
  }

  public void setInterval(int paramInt) {
    this.interval = paramInt;
  }

  public synchronized void wait1step() {
    try {
      if (!this.notifyFlag)
        wait();
    } catch (InterruptedException interruptedException) {}
    this.notifyFlag = false;
  }

  public void run() {
    while (true) {
      synchronized (this) {
        this.notifyFlag = true;
        notifyAll();
      }
      try {
        Thread.sleep(this.interval);
      } catch (InterruptedException interruptedException) {
        break;
      }
    }
  }
}