import android.app.*; import android.os.*; import android.view.*;
import android.graphics.*; import android.text.*;

public class Android extends Activity {
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    this.setContentView(new View(this) {

      private Paint p = new Paint();
      private TextPaint pt = new TextPaint();
      private int i;

      {
        pt.setTypeface(Typeface.createFromAsset(getAssets(),"OpenSans-Regular.ttf"));
        pt.setStrokeWidth(7);
        pt.setTextSize(60);
        pt.setAntiAlias(true);
        pt.setPathEffect(null);
      }

      protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xffaaaaaa);
        p.setColor(0xffff0000); canvas.drawLine((float)(Math.random() * 100), (float)(Math.random() * 100), (float)(Math.random() * 200 + 100), (float)(Math.random() * 200 + 100), p);
        p.setColor(0xff000000); canvas.drawText(String.format("Counter: %d", i++), 10, 100, p);
        pt.setColor(0xff000000); canvas.drawText(String.format("Counter: %d", i++), 10, 200, pt);
      }

      public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
          case MotionEvent.ACTION_UP:
            this.invalidate();
            break;
        }
        return true;
      }

    });
  }
}