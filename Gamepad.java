// This class is a mock stub for when you want to compile without gamepad support on desktop, but it also used directly as a struct by the Android port.

class Gamepad {

  public boolean param_dpad_diag_count;

  public boolean start, select;
  public boolean south_maybe, north_maybe, west_maybe, east_maybe;
  public boolean up, down, left, right;
  public boolean left_shoulder, right_shoulder;
  public boolean l3, r3;
  public double left_trigger, right_trigger;
  public double lx, ly, rx, ry;

  public boolean n_start, n_select;
  public boolean n_south_maybe, n_north_maybe, n_west_maybe, n_east_maybe;
  public boolean n_up, n_down, n_left, n_right;
  public boolean n_left_shoulder, n_right_shoulder;
  public boolean n_l3, n_r3;

  public boolean available;
  public void poll() { }

}