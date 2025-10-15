import net.java.games.input.*;

class Gamepad {

  public boolean param_dpad_diag_count = true;

  // held state
  public boolean start;
  public boolean select;
  public boolean south_maybe;
  public boolean north_maybe;
  public boolean west_maybe;
  public boolean east_maybe;
  public boolean up;
  public boolean down;
  public boolean left;
  public boolean right;
  public boolean left_shoulder;
  public boolean right_shoulder;
  public double left_trigger;
  public double right_trigger;
  public double lx, ly, rx, ry;

  // pressed/release state is a bit of a hack, because a low polling rate can miss events, but whatever. I don't care that much.
  // usage: if(start && n_start) just_pressed
  //        if(!start && n_start) just released
  public boolean n_start;
  public boolean n_select;
  public boolean n_south_maybe;
  public boolean n_north_maybe;
  public boolean n_west_maybe;
  public boolean n_east_maybe;

  public void poll() {
    this.n_start = this.start;
    this.n_select = this.select;
    this.n_south_maybe = this.south_maybe;
    this.n_north_maybe = this.north_maybe;
    this.n_west_maybe = this.west_maybe;
    this.n_east_maybe = this.east_maybe;
    for(Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
      if(controller.getType() != Controller.Type.GAMEPAD) continue;
      controller.poll();
      // EventQueue queue = controller.getEventQueue(); Event event = new Event(); while(queue.getNextEvent(event)) { System.out.println(event); }
      { Component c = controller.getComponent(Component.Identifier.Button.A); this.south_maybe = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Button.Y); this.north_maybe = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Button.X); this.west_maybe = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Button.B); this.east_maybe = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Button.LEFT_THUMB); this.left_shoulder = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Button.RIGHT_THUMB); this.right_shoulder = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Button.START); this.start = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Button.SELECT); this.select = c != null && c.getPollData() > 0; }
      { Component c = controller.getComponent(Component.Identifier.Axis.X); this.lx = c == null? 0 : c.getPollData(); }
      { Component c = controller.getComponent(Component.Identifier.Axis.Y); this.ly = c == null? 0 : c.getPollData(); }
      { Component c = controller.getComponent(Component.Identifier.Axis.RX); this.rx = c == null? 0 : c.getPollData(); }
      { Component c = controller.getComponent(Component.Identifier.Axis.RY); this.ry = c == null? 0 : c.getPollData(); }
      { Component c = controller.getComponent(Component.Identifier.Axis.POV); if(c == null) { this.up = this.down = this.left = this.right = false; } else {
        this.up = c.getPollData() == Component.POV.UP;
        this.down = c.getPollData() == Component.POV.DOWN;
        this.left = c.getPollData() == Component.POV.LEFT;
        this.right = c.getPollData() == Component.POV.RIGHT;
        if(param_dpad_diag_count) {
          if(c.getPollData() == Component.POV.UP_LEFT) { this.up = true; this.left = true; }
          if(c.getPollData() == Component.POV.UP_RIGHT) { this.up = true; this.right = true; }
          if(c.getPollData() == Component.POV.DOWN_LEFT) { this.down = true; this.left = true; }
          if(c.getPollData() == Component.POV.DOWN_RIGHT) { this.down = true; this.right = true; }
        }
      }}
      { Component c = controller.getComponent(Component.Identifier.Axis.Z); if(c == null) { this.left_trigger = 0; } else {
        double value = c.getPollData(); this._saw_a_negative_trigger |= value < 0;
        if(this._saw_a_negative_trigger) value = (value + 1) / 2;
        this.left_trigger = value;
      }}
      { Component c = controller.getComponent(Component.Identifier.Axis.RZ); if(c == null) { this.left_trigger = 0; } else {
        double value = c.getPollData(); this._saw_a_negative_trigger |= value < 0;
        if(this._saw_a_negative_trigger) value = (value + 1) / 2;
        this.right_trigger = value;
      }}
    }
    this.n_start = this.start != this.n_start;
    this.n_select = this.select != this.n_select;
    this.n_south_maybe = this.south_maybe != this.n_south_maybe;
    this.n_north_maybe = this.north_maybe != this.n_north_maybe;
    this.n_west_maybe = this.west_maybe != this.n_west_maybe;
    this.n_east_maybe = this.east_maybe != this.n_east_maybe;
  }

  private boolean _saw_a_negative_trigger;

}