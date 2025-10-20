import android.app.*; import android.os.*; import android.view.*;
import android.graphics.*; import android.text.*; import android.media.*;
import java.io.*; import java.util.*;


public class Android extends Activity {
  public void message_box(String title, String msg) { AlertDialog.Builder b = new AlertDialog.Builder(this); b.setMessage(msg); b.setTitle(title); b.setCancelable(true); b.create().show(); }
  public Bitmap load_image(String in_assets_path) { try(InputStream in = this.getAssets().open(in_assets_path)) { return BitmapFactory.decodeStream(in); } catch (IOException e) { message_box(e.getClass().getName(), String.format("load_image '%s'", in_assets_path)); } return null; }
  public TextPaint load_font(String in_assets_path) { TextPaint p = new TextPaint(); try { p.setTypeface(Typeface.createFromAsset(this.getAssets(), in_assets_path)); } catch(Exception e) { message_box(e.getClass().getName(), String.format("load_font '%s'", in_assets_path)); } p.setAntiAlias(true); return p; }
  private SoundPool audio = new SoundPool.Builder().setMaxStreams(2).build();
  public int load_audio(String in_assets_path) { try { return this.audio.load(this.getAssets().openFd(in_assets_path), 1); } catch(Exception e) { message_box(e.getClass().getName(), String.format("load_audio '%s'", in_assets_path)); } return 0; } // it's undocumented, but in practice the id returned is non-zero, and 0 will be ignored by play() when used.
  protected void onCreate(Bundle state) { super.onCreate(state); this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); this.setContentView(new View(this) {

      private Paint p = new Paint();
      private TextPaint pt = load_font("OpenSans-Regular.ttf");
      private TextPaint pst = load_font("OpenSans-Regular.ttf"); // TODO this is redundant, just reuse pt with setTextSize
      private String debug;

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
        this.setFocusable(true);
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
      
      public Map<Integer, Double> touches_x = new TreeMap<>();
      public boolean onTouchEvent(MotionEvent e) {
        int index = e.getActionIndex();
        int action = e.getActionMasked();
        int pointer_id = e.getPointerId(index);
        switch(action) {
          case MotionEvent.ACTION_DOWN:
          case MotionEvent.ACTION_POINTER_DOWN:
          case MotionEvent.ACTION_MOVE:
            synchronized(touches_x) { touches_x.put(pointer_id, (double)e.getX(index)); }
            break;
          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_POINTER_UP:
          case MotionEvent.ACTION_CANCEL:
            synchronized(touches_x) { touches_x.remove(pointer_id); }
            if(game.title_mode) {
              game.startGame(true, false);
            }
            break;
        }
        return true;
      }

      public boolean onGenericMotionEvent(MotionEvent e) {
        if(!((e.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && e.getAction() == MotionEvent.ACTION_MOVE)) return super.onGenericMotionEvent(e);
        //this.debug = e.toString();
        gamepad.left_trigger = e.getAxisValue(MotionEvent.AXIS_LTRIGGER);
        gamepad.right_trigger = e.getAxisValue(MotionEvent.AXIS_RTRIGGER);
        gamepad.lx = e.getAxisValue(MotionEvent.AXIS_X);
        gamepad.ly = e.getAxisValue(MotionEvent.AXIS_Y);
        gamepad.rx = e.getAxisValue(MotionEvent.AXIS_Z);
        gamepad.ry = e.getAxisValue(MotionEvent.AXIS_RZ);
        return super.onGenericMotionEvent(e); // I pretend I didn't handle it, so I don't prevent the DPAD key events
      }

      // Note: Missed input are very likely
      public boolean onKeyDown(int keyCode, KeyEvent event) {
        //this.debug = event.toString();
        //if((event.getSource() & InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD) return super.onKeyDown(keyCode, event);
        if(event.getRepeatCount() != 0) return super.onKeyDown(keyCode, event);
        gamepad.available = true;
        gamepad.param_dpad_diag_count = true; // note: this probably corrupts n_left/n_right states, but I don't actually use those checks in this game
        switch(keyCode) {
          case KeyEvent.KEYCODE_BUTTON_A: gamepad.south_maybe = gamepad.n_south_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_B: gamepad.east_maybe = gamepad.n_east_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_X: gamepad.west_maybe = gamepad.n_west_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_Y: gamepad.north_maybe = gamepad.n_north_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_START: gamepad.start = gamepad.n_start = true; return true;
          case KeyEvent.KEYCODE_BUTTON_SELECT: gamepad.select = gamepad.n_select = true; return true;
          case KeyEvent.KEYCODE_DPAD_UP: gamepad.up = gamepad.n_up = true; return true;
          case KeyEvent.KEYCODE_DPAD_DOWN: gamepad.down = gamepad.n_down = true; return true;
          case KeyEvent.KEYCODE_DPAD_LEFT: gamepad.left = gamepad.n_left = true; return true;
          case KeyEvent.KEYCODE_DPAD_DOWN_LEFT: gamepad.left = gamepad.n_left = true; return true;
          case KeyEvent.KEYCODE_DPAD_UP_LEFT: gamepad.left = gamepad.n_left = true; return true;
          case KeyEvent.KEYCODE_DPAD_RIGHT: gamepad.right = gamepad.n_right = true; return true;
          case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT: gamepad.right = gamepad.n_right = true; return true;
          case KeyEvent.KEYCODE_DPAD_UP_RIGHT: gamepad.right = gamepad.n_right = true; return true;
          case KeyEvent.KEYCODE_BUTTON_L1: gamepad.left_shoulder = gamepad.n_left_shoulder = true; return true;
          case KeyEvent.KEYCODE_BUTTON_R1: gamepad.right_shoulder = gamepad.n_right_shoulder = true; return true;
          case KeyEvent.KEYCODE_BUTTON_THUMBL: gamepad.l3 = gamepad.n_l3 = true; return true;
          case KeyEvent.KEYCODE_BUTTON_THUMBR: gamepad.r3 = gamepad.n_r3 = true; return true;
        }
        return false;
      }
      public boolean onKeyUp(int keyCode, KeyEvent event) {
        //if((event.getSource() & InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD) return super.onKeyUp(keyCode, event);
        gamepad.available = true;
        gamepad.param_dpad_diag_count = true;
        switch(keyCode) {
          case KeyEvent.KEYCODE_BUTTON_A: gamepad.south_maybe = false; gamepad.n_south_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_B: gamepad.east_maybe = false; gamepad.n_east_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_X: gamepad.west_maybe = false; gamepad.n_west_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_Y: gamepad.north_maybe = false; gamepad.n_north_maybe = true; return true;
          case KeyEvent.KEYCODE_BUTTON_START: gamepad.start = false; gamepad.n_start = true; return true;
          case KeyEvent.KEYCODE_BUTTON_SELECT: gamepad.select = false; gamepad.n_select = true; return true;
          case KeyEvent.KEYCODE_DPAD_UP: gamepad.up = false; gamepad.n_up = true; return true;
          case KeyEvent.KEYCODE_DPAD_DOWN: gamepad.down = false; gamepad.n_down = true; return true;
          case KeyEvent.KEYCODE_DPAD_LEFT: gamepad.left = false; gamepad.n_left = true; return true;
          case KeyEvent.KEYCODE_DPAD_DOWN_LEFT: gamepad.left = false; gamepad.n_left = true; return true;
          case KeyEvent.KEYCODE_DPAD_UP_LEFT: gamepad.left = false; gamepad.n_left = true; return true;
          case KeyEvent.KEYCODE_DPAD_RIGHT: gamepad.right = false; gamepad.n_right = true; return true;
          case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT: gamepad.right = false; gamepad.n_right = true; return true;
          case KeyEvent.KEYCODE_DPAD_UP_RIGHT: gamepad.right = false; gamepad.n_right = true; return true;
          case KeyEvent.KEYCODE_BUTTON_L1: gamepad.left_shoulder = false; gamepad.n_left_shoulder = true; return true;
          case KeyEvent.KEYCODE_BUTTON_R1: gamepad.right_shoulder = false; gamepad.n_right_shoulder = true; return true;
          case KeyEvent.KEYCODE_BUTTON_THUMBL: gamepad.l3 = false; gamepad.n_l3 = true; return true;
          case KeyEvent.KEYCODE_BUTTON_THUMBR: gamepad.r3 = false; gamepad.n_r3 = true; return true;
        }
        return false;
      }

      private void tick() {
        // gamepad input
        boolean gamepad_left = false, gamepad_right = false; double dead_zone = .05;
        if(gamepad.lx < -dead_zone) gamepad_left = true;
        if(gamepad.lx > dead_zone) gamepad_right = true;
        if(gamepad.rx < -dead_zone) gamepad_left = true;
        if(gamepad.rx > dead_zone) gamepad_right = true;
        if(gamepad.left) gamepad_left = true;
        if(gamepad.right) gamepad_right = true;
        if(gamepad.left_shoulder) gamepad_left = true;
        if(gamepad.right_shoulder) gamepad_right = true;
        if(gamepad.left_trigger > 0) gamepad_left = true;
        if(gamepad.right_trigger > 0) gamepad_right = true;
        // advanced system commands
        if(gamepad.left_shoulder && gamepad.right_shoulder && gamepad.start && gamepad.select) System.exit(0);
        if(gamepad.select) {
          if(gamepad.left_shoulder && gamepad.n_left_shoulder) this.target_dt-=5;
          if(gamepad.right_shoulder && gamepad.n_right_shoulder) this.target_dt+=5;
        }
        // normal system commands
        else {
          if(gamepad.l3 && gamepad.n_l3) this.set_logical_size(this.logical_w == 320? 6 : 1);
          if(gamepad.r3 && gamepad.n_r3) this.stretched = !this.stretched;
        }
        // title command
        if(game.title_mode) {
          if((gamepad.south_maybe && gamepad.n_south_maybe) || (gamepad.north_maybe && gamepad.n_north_maybe) || (gamepad.west_maybe && gamepad.n_west_maybe) || (gamepad.east_maybe && gamepad.n_east_maybe) || (gamepad.up && gamepad.n_up)) game.startGame(true, true); // continue
          if(gamepad.select) {
            if(gamepad.start && gamepad.n_start) { game.prevScore = 110000; game.contNum = 100; game.startGame(true, true); } // some sort of cheat
          } else {
            if((gamepad.start && gamepad.n_start) || (gamepad.down && gamepad.n_down)) game.startGame(true, false); // restart
          }
        }
        // play command
        else {
          if(gamepad.start && gamepad.n_start) this.paused = !this.paused;
        }
        // clear new flags
        gamepad.n_start = gamepad.n_select = gamepad.n_south_maybe = gamepad.n_north_maybe = gamepad.n_west_maybe = gamepad.n_east_maybe = gamepad.n_up = gamepad.n_down = gamepad.n_left = gamepad.n_right = gamepad.n_left_shoulder = gamepad.n_right_shoulder = gamepad.n_l3 = gamepad.n_r3 = false;

        // touch input
        int w = getWidth(); boolean touching_left = false, touching_right = false;
        synchronized(touches_x) { for(double x : touches_x.values()) {
          touching_left |= x < w/2;
          touching_right |= x > w/2;
        }}

        if(game.title_mode && this.paused) this.paused = false;
        if(!this.paused) {
          // game tick and draw on offscreen game surface
          game.tick(touching_left | gamepad_left, touching_right | gamepad_right);
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

        // stupid camera hole in the screen. I only care if we are in portrait not reverse. Hopefully this will survive the test of time.
        int safe_top_y = 0; { WindowInsets insets = getWindow().getDecorView().getRootWindowInsets(); if(insets != null) { DisplayCutout cutout = insets.getDisplayCutout(); if(cutout != null) { safe_top_y = cutout.getSafeInsetTop(); } } }

        // overlay, now that I'm using drawString on the window size surface for all text instead of widgets, I need to ensure the font scales
        // in the event that the window is very thin, then we'll have to reduce the font so the longuest line fits
        pt.setTextSize(b_h / 15); 
        {
          String msg = game.contNum == 0? "Your Hi-score: 000000    Period: 00 (00)" : "Your Hi-score: 000000      Continue penalty: 000000";
          while(pt.measureText(msg) > b_w) {
            pt.setTextSize(pt.getTextSize() * .9f);
            if(pt.getTextSize() < 6) break;
          }
        }
        pst.setTextSize(pt.getTextSize() * .6f); Paint.FontMetricsInt fm = pt.getFontMetricsInt(), sfm = pst.getFontMetricsInt();
        
        pt.setColor(C.white); pst.setColor(C.white); 
        canvas.drawText(String.format("Your Hi-score:%d", game.hiscore), 2*fm.descent/3, b_h - fm.descent, pt);
        { String msg = String.format("Period: %dms (%dms)", this.target_dt, this.dt); canvas.drawText(msg, b_w - 2*fm.descent/3 - (int)pt.measureText(msg), b_h - fm.descent, pt); }
        String score = "Score:" + game.score;
        String penalty = "Continue penalty:" + game.contNum * 1000;
        int score_w = (int)pt.measureText(score); int penalty_w = (int)pt.measureText(penalty); int padding = b_w / 10; int total_w = game.contNum > 0? score_w + padding + penalty_w : score_w; int offset = (b_w - total_w) / 2;
        canvas.drawText(score, offset, safe_top_y + (-fm.ascent), pt); offset += score_w + padding;
        //synchronized(touches_x) { canvas.drawText(touches_x.values().toString(), 10, safe_top_y + 2*(-fm.ascent), pst); }
        if(this.debug != null) { int i = 2; int start = 0; int end = this.debug.length() - 1; while(true) { canvas.drawText(debug, start, end, 0, safe_top_y + i*(-fm.ascent), pst); if((int)pst.measureText(debug, start, end) < b_w) break; start += 60; i++; if(start >= debug.length()) break; } }
        if(game.contNum > 0) canvas.drawText(penalty, offset, safe_top_y + (-fm.ascent), pt);
        if(game.title_mode) {
          int line_h = -fm.ascent + fm.descent; int small_line_h = -sfm.ascent + sfm.descent;
          int spacing = 3, small_spacing = 2;
          int n = 3, small_n = 6 + (gamepad.available? 4 : 0);
          offset = (b_h - ((line_h + spacing) * n + (small_line_h + small_spacing) * small_n - spacing - small_spacing)) / 2;
          offset += (-fm.ascent);
          { String msg = "Jet Slalom Resurrected"; int line_w = (int)pt.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pt); offset += line_h + spacing; }
          { String msg = "by David Lareau in 2025"; int line_w = (int)pt.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pt); offset += line_h + spacing; }
          { String msg = "Original 1997 version by MR-C"; int line_w = (int)pt.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pt); offset += line_h + spacing; }
          { String msg = "-- Keyboard --"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "(F)ullscreen, (H)ighRez, (S)tretch, Speed(num+/num-)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "Restart(Spacebar/Enter), (C)ontinue(up/W), Chea(T)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "(P)ause, Quit(ESC), Ship(Left/Right/A/D/J/L)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "-- Mouse --"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          { String msg = "Ship(L/R), Restart(L), Continue(R)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
          if(gamepad.available) {
            { String msg = "-- Gamepad --"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
            { String msg = "HighRez(L3), Stretch(R3), Speed(Select+LB/RB)"; int line_w = (int)pst.measureText(msg); canvas.drawText(msg, (b_w - line_w) / 2, offset, pst); offset += small_line_h + small_spacing; }
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
          float ship_x = (this.logical_w / 2) - this.ship_w/2, ship_y = this.logical_h - y;
          if (game.damaged < 10) this.scene_c.drawBitmap(image, null, new RectF(ship_x, ship_y, ship_x + this.ship_w, ship_y + this.ship_h), p);
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