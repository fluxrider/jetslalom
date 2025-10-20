import android.app.*; import android.os.*; import android.view.*;
import android.graphics.*; import android.text.*;
import java.io.*;


public class Android extends Activity {
  public void message_box(String title, String msg) { AlertDialog.Builder b = new AlertDialog.Builder(this); b.setMessage(msg); b.setTitle(title); b.setCancelable(true); b.create().show(); }
  public Bitmap load_image(String in_assets_path) { try(InputStream in = this.getAssets().open(in_assets_path)) { return BitmapFactory.decodeStream(in); } catch (IOException e) { message_box("IOException", in_assets_path); } return null; }
  public TextPaint load_font(String in_assets_path, double size) {
    TextPaint p = new TextPaint();
    p.setTypeface(Typeface.createFromAsset(this.getAssets(), in_assets_path));
    p.setStrokeWidth(7);
    p.setTextSize((float)size);
    p.setAntiAlias(true);
    p.setPathEffect(null);
    return p;
  }
  protected void onCreate(Bundle state) { super.onCreate(state); this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); this.setContentView(new View(this) {

      private Paint p = new Paint();
      private TextPaint pt = load_font("OpenSans-Regular.ttf", 60);
      private Bitmap image = load_image("jiki.gif");
      private int i;

      protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xffaaaaaa);
        p.setColor(0xffff0000); canvas.drawLine((float)(Math.random() * 100), (float)(Math.random() * 100), (float)(Math.random() * 200 + 100), (float)(Math.random() * 200 + 100), p);
        p.setColor(0xff000000); canvas.drawText(String.format("Counter: %d", i++), 10, 100, p);
        pt.setColor(0xff000000); canvas.drawText(String.format("Counter: %d", i++), 10, 200, pt);
        canvas.drawBitmap(image, 10, 300, p);
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