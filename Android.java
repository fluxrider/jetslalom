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
      private TextPaint pst = load_font("OpenSans-Regular.ttf", 60 * .6);

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
      private long dt;

      private Gamepad gamepad = new Gamepad();

      private Game game = new Game();

      {
        this.set_logical_size(1);
        this.game.startGame(false, false);
        this.gameThread = new Thread(new Runnable() {
          public void run() {
            long t0 = System.currentTimeMillis(), t1 = t0;
            while (gameThread == Thread.currentThread()) {
              t0 = t1; t1 = System.currentTimeMillis(); dt = t1 - t0;
              tick();
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
      
      public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
          case MotionEvent.ACTION_UP:
            //audio.play(explosion, 1, 1, 0, 0, 1);
            break;
        }
        return true;
      }

      private void tick() {
        if(game.title_mode && this.paused) this.paused = false;
        if(!this.paused) {
          game.tick(false, false); // TODO input
          this.prt();
        }
      }

      protected void onDraw(Canvas canvas) {
        p.setFilterBitmap(false);

        // letterbox scaling (i.e. respects aspect ratio)
        int b_w = canvas.getWidth(); int b_h = canvas.getHeight(); int s_w = this.logical_w; int s_h = this.logical_h;
        double scale; if((b_w / (double)b_h) > (s_w / (double)s_h)) scale = b_h / (double)s_h; else scale = b_w / (double)s_w;
        int x = (int)((b_w - s_w * scale) / 2); int y = (int)((b_h - s_h * scale) / 2);
        int w = (int)(s_w*scale); int h = (int)(s_h*scale);
        if(this.stretched) { x = 0; y = 0; w = b_w; h = b_h; }
        if(!this.stretched) canvas.drawColor(bg);
        canvas.drawBitmap(scene_img, null, new RectF(x, y, x+w, y+h), p);

        // overlay, now that I'm using drawString on the window size surface for all text instead of widgets, I need to ensure the font scales
        pt.setColor(C.white); pst.setColor(C.white); Paint.FontMetricsInt fm = pt.getFontMetricsInt(), sfm = pst.getFontMetricsInt();
        canvas.drawText(String.format("Your Hi-score:%d", game.hiscore), 2*fm.descent/3, b_h - fm.descent, pt);
        { String msg = String.format("Period: %dms (%dms)", this.target_dt, this.dt); canvas.drawText(msg, b_w - 2*fm.descent/3 - (int)pt.measureText(msg), b_h - fm.descent, pt); }
        String score = "Score:" + game.score;
        String penalty = "Continue penalty:" + game.contNum * 1000;
        int score_w = (int)pt.measureText(score); int penalty_w = (int)pt.measureText(penalty); int padding = b_w / 10; int total_w = game.contNum > 0? score_w + padding + penalty_w : score_w; int offset = (b_w - total_w) / 2;
        canvas.drawText(score, offset, -fm.ascent, pt); offset += score_w + padding;
        if(game.contNum > 0) canvas.drawText(penalty, offset, -fm.ascent, pt);
        if(game.title_mode) {
          int line_h = -fm.ascent + fm.descent; int small_line_h = -sfm.ascent + sfm.descent;
          int spacing = 5, small_spacing = 3;
          int n = 4, small_n = 6 + (gamepad.available? 4 : 0);
          offset = (b_h - ((line_h + spacing) * n + (small_line_h + small_spacing) * small_n)) / 2;
          { String msg = "Jet Slalom Resurrected"; int line_w = (int)pt.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pt); offset += line_h + spacing; }
          { String msg = "by David Lareau in 2025"; int line_w = (int)pt.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pt); offset += line_h + spacing; }
          { String msg = "Original 1997 version by MR-C"; int line_w = (int)pt.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pt); offset += line_h + spacing; }
          { offset += line_h + spacing; }
          { String msg = "-- Keyboard --"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "(F)ullscreen, (H)ighRez, (S)tretch, Speed(num+/num-)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "Restart(Spacebar/Enter), (C)ontinue(up/W), Chea(T)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "(P)ause, Quit(ESC), Ship(Left/Right/A/D/J/L)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "-- Mouse --"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "Ship(L/R), Restart(L), Continue(R)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          if(gamepad.available) {
            { String msg = "-- Gamepad --"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
            { String msg = "Fullscreen(L3), HighRez(Select+L3), Stretch(R3), Speed(Select+LB/RB)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
            { String msg = "Re(Start/Down), Resume(A/B/X/Y/Up), Cheat(Select+Start)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
            { String msg = "Pause(Start), Quit(LB+RB+Start+Select), Ship(Sticks/Dpad/Shoulders)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          }
        }
        if(this.paused) {
          pt.setColor(C.rgb(255,0,0));
          offset = (int)(b_h * (System.currentTimeMillis() % 10000) / 10000.0);
          { String msg = "Paused"; int line_w = (int)pt.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pt); }
        }
      }

      // draw game primitives
      private void drawPolygon(Game.Face face) {
        Game.DPoint3[] points = face.points;
        double d1 = (points[1]).x - (points[0]).x;
        double d2 = (points[1]).y - (points[0]).y;
        double d3 = (points[2]).x - (points[0]).x;
        double d4 = (points[2]).y - (points[0]).y;
        float f = (float)(Math.abs(d1 * d4 - d2 * d3) / face.maxZ);
        drawPolygon(C.drgb(C.fr(face.rgb)*f, C.fg(face.rgb)*f, C.fb(face.rgb)*f), points);
      }
      private void drawPolygon(int color, Game.DPoint3[] points) {
        double d1 = this.logical_w / 320.0;
        double d2 = this.logical_h / 200.0;
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        for (byte b = 0; b < points.length; b++) {
          Game.DPoint3 point = points[b];
          double d3 = 120.0 / (1.0 + 0.6 * point.z);
          double d4 = game.nowCos * point.x + game.nowSin * (point.y - 2.0);
          double d5 = -game.nowSin * point.x + game.nowCos * (point.y - 2.0) + 2.0;
          int x  = (int)(d4 * d1 * d3) + this.logical_w / 2;
          int y = (int)(d5 * d2 * d3) + this.logical_h / 2;
          if(b == 0) path.moveTo(x,y);
          else path.lineTo(x,y);
        }
        path.close();
        p.setColor(color); p.setStyle(Paint.Style.FILL); this.scene_c.drawPath(path, p);
      }
      private void draw_obstacle(Game.Obstacle o) {
        drawPolygon(o.faces[0]);
        drawPolygon(o.faces[1]);
      }

      private void prt() {
        p.setFilterBitmap(false);
        p.setAntiAlias(false);

        this.scene_c.drawColor(game.rounds[game.round].getSkyRGB());
        this.drawPolygon(game.rounds[game.round].getGroundRGB(), game.ground_points);
        for(Game.Obstacle obstacle : game.obstacles) draw_obstacle(obstacle);
        this.ship_animation++;
        if(!game.title_mode) {
          int y = 24 * this.logical_h / 200;
          Bitmap image = this.ship[this.ship_animation % 4 > 1? 1 : 0];
          if (this.ship_animation % 12 > 6) y = 22 * this.logical_h / 200;
          if (game.score < 200) y = (12 + game.score / 20) * this.logical_h / 200;
          if (game.damaged < 10) this.scene_c.drawBitmap(image, (this.logical_w / 2) - image.getWidth()/2, this.logical_h - y, p);
          if (game.damaged > 0) {
            if(game.damaged <= 20) {
              if(game.damaged == 1) { audio.play(explosion, 1, 1, 0, 0, 1); }
              this.p.setColor(C.rgb(255, 255 - game.damaged * 12, 240 - game.damaged * 12));
              int i = game.damaged * 8 * this.logical_w / 320;
              int j = game.damaged * 4 * this.logical_h / 200;
              int left = (this.logical_w / 2) - i;
              int top = 186 * this.logical_h / 200 - j;
              this.scene_c.drawOval(left, top, left + i * 2, top + j * 2, p);
            }
          }
        }
      }
  
    });
  }
}