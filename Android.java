import android.app.*; import android.os.*; import android.view.*;
import android.graphics.*; import android.text.*; import android.media.*;
import java.io.*;


public class Android extends Activity {
  public void message_box(String title, String msg) { AlertDialog.Builder b = new AlertDialog.Builder(this); b.setMessage(msg); b.setTitle(title); b.setCancelable(true); b.create().show(); }
  public Bitmap load_image(String in_assets_path) { try(InputStream in = this.getAssets().open(in_assets_path)) { return BitmapFactory.decodeStream(in); } catch (IOException e) { message_box(e.getClass().getName(), String.format("load_image '%s'", in_assets_path)); } return null; }
  public TextPaint load_font(String in_assets_path, double size) { TextPaint p = new TextPaint(); try { p.setTypeface(Typeface.createFromAsset(this.getAssets(), in_assets_path)); } catch(Exception e) { message_box(e.getClass().getName(), String.format("load_font '%s'", in_assets_path)); } p.setAntiAlias(true); p.setTextSize((float)size); return p; }
  private SoundPool audio = new SoundPool.Builder().setMaxStreams(2).build();
  public int load_audio(String in_assets_path) { try { return this.audio.load(this.getAssets().openFd(in_assets_path), 1); } catch(Exception e) { message_box(e.getClass().getName(), String.format("load_audio '%s'", in_assets_path)); } return 0; } // it's undocumented, but in practice the id returned is non-zero, and 0 will be ignored by play() when used.
  protected void onCreate(Bundle state) { super.onCreate(state); this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); this.setContentView(new View(this) {

      private Paint p = new Paint();
      private TextPaint pt = load_font("OpenSans-Regular.ttf", 60);
      private Bitmap image = load_image("jiki.gif");
      private int i;
      private int explosion = load_audio("explosion.wav");

      protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xffaaaaaa);
        p.setColor(0xffff0000); canvas.drawLine((float)(Math.random() * 100), (float)(Math.random() * 100), (float)(Math.random() * 200 + 100), (float)(Math.random() * 200 + 100), p);
        p.setColor(0xff000000); canvas.drawText(String.format("Sound ID: %d", explosion), 10, 100, p);
        pt.setColor(0xff000000); canvas.drawText(String.format("Counter: %d", i++), 10, 200, pt);
        if(image != null) { canvas.drawBitmap(image, 10, 300, p); }
      }

      public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
          case MotionEvent.ACTION_UP:
            audio.play(explosion, 1, 1, 0, 0, 1);
            this.invalidate();
            break;
        }
        return true;
      }

    });
  }
}