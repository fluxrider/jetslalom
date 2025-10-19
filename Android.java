import android.app.*; import android.os.*; import android.view.*;
import android.graphics.*;

public class Android extends Activity {
  protected void onCreate(Bundle state) { super.onCreate(state); this.setContentView(new View(this) {

    private Paint p = new Paint();

    protected void onDraw(Canvas canvas) {
      canvas.drawColor(0xffaaaaaa);
      p.setColor(0xffff0000); canvas.drawLine((float)(Math.random() * 100), (float)(Math.random() * 100), (float)(Math.random() * 200 + 100), (float)(Math.random() * 200 + 100), p);
    }

    public boolean onTouchEvent(MotionEvent e) {
      switch(e.getAction()) {
        case MotionEvent.ACTION_UP:
          this.invalidate();
          break;
      }
      return true;
    }

  });}
}