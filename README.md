# Jet Slalom Restoration Project

![AI Generated Cover](slalom.jpg?raw=true)

![Gameplay Screenshot](doc/screenshot_play1.png?raw=true)
![Gameplay Screenshot](doc/screenshot_play2.png?raw=true)
![Title Screenshot](doc/screenshot_hq.png?raw=true)
![HighRez Feature](doc/screenshot_title.png?raw=true)

I played Jet Slalom as an online applet when I was a young adult back in 1999.
I wrote a WebGL crappy homage test around 2010.
Some random guy on the internet emailed me about it in 2025, praising my controls but pooping on my inexistant levels design.

The guy also sent me a half broken code base of what he could salvage off the internet of what may have been a decompiled version of the original code.
It didn't compile, and had commented bytecode sections. This is me getting it to a working state.

## State

- It compiles and is playable on JDK 25.0.1
- I've added a few features.
  - Gamepad Support.
  - Fullscreen, Stretch or Letterbox.
  - Toggle higher resolution rendering.
  - Regulate framerate.
  - Modify game speed.
  - Command Line Arguments: 'stretch' 'fullscreen' 'hq' 'period 30'
- I have removed features that were broken when I got there.
  - Applet.
  - High Scores.
  - Fast Forward Button (use to be 'A').
- Codewise, I have removed/rewritten a lot of code.
  - It started with 18 files 2000 lines, and now it's 5 files 800 lines.
  - There are still some vestige of decompilation.
  - My personal coding style is offensive to most because I'm a disabled hermit.
  - I've done changes that alter a bit the math to my taste, so if you wanted maximum fidelity this ain't it, but it's close enough.

### Future Work

- Web Browser version somehow.
- Android version.

### Known Issue

- JInput, the external library I use for gamepad support, requires the gamepad to be plugged before the app starts. I've only tested on Linux.
- The world of desktop scaling, 2K, 4K, and upscaling is a mess.
  - When the app was using AWT widgets, it required `-Dsun.java2d.uiScale.enabled=true -Dsun.java2d.uiScale=2 -Djava.awt.headless=false` to look good.
  - I've replaced the widgets with drawString to make the app more portable and simpler, but I still suspect there are problems depending of your environment, and getting the Font to look good is now a chore.
  
## How to run

I assume you got a Java JDK installed and know how to use the command line for your OS. I'm using a posix system with bash (i.e. standard Linux).

Simple way:
- no gamepad support
```
javac AWT.java
java AWT
```

The ridiculous way:
- gamepad support
- an output folder for the binaries
```
rm -Rf bin && javac -cp gamepad:.:gamepad/jinput-2.0.10.jar AWT.java -d bin
java -cp bin:gamepad/jinput-2.0.10.jar -Djava.library.path=gamepad/jinput-2.0.10-natives-all --enable-native-access=ALL-UNNAMED -Djava.util.logging.config.file=gamepad/logging.properties AWT
```
