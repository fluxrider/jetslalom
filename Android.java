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

      private int ship_animation;
      private int ship_w, ship_h;
      private Bitmap ship[] = new Bitmap[] { load_image("jiki.gif"), load_image("jiki2.gif") };
      private int explosion = load_audio("explosion.wav");
      private Bitmap scene_img;
      private Canvas scene_c;

      private int logical_w, logical_h;
      private Thread gameThread;
      private int bg = C.rgb(160, 208, 176);
      private TextPaint font, small_font;
      private boolean stretched;
      private boolean paused;
      private int target_dt = 55;
      private int delay = target_dt;

      private int i = 0;

      private Game game;

      {
        this.set_logical_size(1);
        this.gameThread = new Thread(new Runnable() {
          public void run() {
            long t0 = System.currentTimeMillis(), t1 = t0;
            while (gameThread == Thread.currentThread()) {
              t0 = t1; t1 = System.currentTimeMillis(); long dt = t1 - t0;

              invalidate();
              if(dt > target_dt) { delay--; } // framerate too low, try faster
              if(dt < target_dt) { delay++; } // framerate too high, try slower
              if(delay < 1) delay = 1;
              { try { Thread.sleep(delay); } catch (InterruptedException e) { e.printStackTrace(); } }
            }
          }
        });
        this.gameThread.start();
      }

      private void set_logical_size(double scale) {
        this.logical_w = (int)(320 * scale);
        this.logical_h = (int)(200 * scale);
        int image_scale = (int)(this.logical_w * 0.7 * 120 / 1.6 / 320); this.ship_w = image_scale * 2; this.ship_h = image_scale / 4;
        this.scene_img = Bitmap.createBitmap(this.logical_w, this.logical_h, Bitmap.Config.ARGB_8888);
        this.scene_c = new Canvas(scene_img);
        this.scene_c.drawColor(C.rgb(0,128,128));
      }

      protected void onDraw(Canvas canvas) {
        canvas.drawColor(bg);
        canvas.drawBitmap(scene_img, null, new RectF(0, 0, canvas.getWidth()/2, canvas.getHeight()/2), p);
        p.setColor(0xffff0000); canvas.drawLine((float)(Math.random() * 100), (float)(Math.random() * 100), (float)(Math.random() * 200 + 100), (float)(Math.random() * 200 + 100), p);
        p.setColor(0xff000000); canvas.drawText(String.format("Sound ID: %d", explosion), 10, 100, p);
        pt.setColor(0xff000000); canvas.drawText(String.format("Counter %d", i++), 10, 200, pt);
        if(ship[0] != null) { canvas.drawBitmap(ship[0], 10, 300, p); }
      }

      public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
          case MotionEvent.ACTION_UP:
            audio.play(explosion, 1, 1, 0, 0, 1);
            break;
        }
        return true;
      }

    });
  }
}