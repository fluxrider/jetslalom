import java.util.*;

public class Game {

  public RoundManager[] rounds = new RoundManager[] { new NormalRound(8000, C.rgb(0, 160, 255), C.rgb(0, 200, 64), 4), new NormalRound(12000, C.rgb(240, 160, 160), C.rgb(64, 180, 64), 3), new NormalRound(25000, C.black, C.rgb(0, 128, 64), 2), new RoadRound(40000, C.rgb(0, 180, 240), C.rgb(0, 200, 64), false), new RoadRound(100000, C.gray(192), C.rgb(64, 180, 64), true), new NormalRound(1000000, C.black, C.rgb(0, 128, 64), 1) };
  public DPoint3[] ground_points = new DPoint3[] { new DPoint3(-100.0, 2.0, 28.0), new DPoint3(-100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 0.1), new DPoint3(100.0, 2.0, 28.0) };
  public LinkedList<Obstacle> obstacles = new LinkedList<>();
  private double vx; // ship's left/right movement
  public int round;
  public int damaged;
  public int score, prevScore, hiscore, contNum;
  public boolean title_mode;

  public double nowSin;
  public double nowCos;

  public Game() {
    for(int b = 1; b < this.rounds.length; b++) this.rounds[b].setPrevRound(this.rounds[b - 1]);
    obstacles.clear();
    for(RoundManager r : this.rounds) r.init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0;
    this.title_mode = true;
  }

  public void startGame(boolean play_mode, boolean resume) {
    this.title_mode = !play_mode;
    obstacles.clear();
    for(RoundManager r : this.rounds) r.init();
    this.damaged = 0;
    this.round = 0;
    this.score = 0;
    this.vx = 0.0;
    if(!resume) { this.contNum = 0; } else {
      while (this.prevScore >= this.rounds[this.round].getNextRoundScore()) this.round++;
      if (this.round > 0) { this.score = this.rounds[this.round - 1].getNextRoundScore(); this.contNum++; }
    }
  }

  void tick(boolean left, boolean right) {
    if(this.rounds[this.round].isNextRound(this.score)) this.round++;

    // ship input
    // turn
    if(this.damaged == 0 && !this.title_mode) {
      if(right) this.vx = Math.max(this.vx - 0.1, -.6);
      if(left) this.vx = Math.min(this.vx + 0.1, .6);
    }
    // stabilize back
    if(!left && !right) {
      if(this.vx < 0.0) this.vx = Math.min(this.vx + .025, 0);
      if(this.vx > 0.0) this.vx = Math.max(this.vx - .025, 0);
    }
    
    // move obstacles
    double angle = Math.abs(this.vx) * 100.0; // [-60, 60]
    nowSin = Math.sin(Math.PI * angle / 180); // I can't replay the original, but the code seem to have divided by 128 here but 180 makes more sense
    nowCos = Math.cos(Math.PI * angle / 180);
    if(this.vx > 0.0) nowSin = -nowSin;
    ListIterator<Obstacle> iter = this.obstacles.listIterator(); while(iter.hasNext()) { Obstacle obstacle = iter.next();
      obstacle.move(this.vx, 0.0, -1.0);
      DPoint3[] points = obstacle.points;
      if((points[0]).z <= 1.1) {
        double d = 0.7 * nowCos;
        if (-d < (points[2]).x && (points[0]).x < d) this.damaged++;
        iter.remove();
      }
    }
    this.rounds[this.round].move(this.vx);
    { Obstacle obstacle = this.rounds[this.round].generateObstacle(); if(obstacle != null) this.obstacles.addFirst(obstacle); }
    
    // tick and bomb
    if(!this.title_mode) this.score += 20;
    if (this.damaged > 0) {
      if(this.damaged > 20) {
        if(!this.title_mode) this.prevScore = this.score;
        if(this.score - this.contNum * 1000 > this.hiscore && !this.title_mode) this.hiscore = this.score - this.contNum * 1000;
        this.title_mode = true;
      } else {
        this.damaged++;
      }
    }
    if(this.title_mode) {
      this.vx = 0.0;
    }
  }

  
}