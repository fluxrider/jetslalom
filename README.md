# Jet Slalom Restoration Project

![AI Generated Cover](slalom.jpg?raw=true)

I played Jet Slalom as an online applet when I was a young adult back in 1999.
I wrote a WebGL crappy homage test around 2010.
Some random guy on the internet emailed me about it in 2025, praising my controls but pooping on my inexistant levels design.

The guy also sent me a half broken code base of what he could salvage off the internet of what may have been a decompiled version of the original code.
It didn't compile, and had commented bytecode sections.
This is an attempt at getting it working.

## State

- It compiles and seems playable on JDK 25.0.1
- I've added a few features.
  - Gamepad Support.
  - Fullscreen (using 'F' or 'Select').
- I have removed features that I did not want to maintain.
 - Applet.
 - High Scores.
 - Fast Forward Button (use to be 'A', now that's part of WASD controls).
- Codewise, it's a mess. I have removed a lot of unnecessary or archaic code and made it my own. Unfortunately for you, my coding style is offensive to most because I'm a disabled hermit, and the rest is decompiled Java.

### Future Work

- Fix the turn strength, I'm pretty sure it's way too hard right now.
- Have a web browser version somehow.

### Known Issue

- JInput, the external library I use for gamepad support, requires the gamepad to be plugged before the app starts. I've only tested on Linux.

## How to run

I assume you got a Java JDK installed and know how to use the command line for your OS. I'm using a posix system with bash (i.e. standard Linux).

Simple way:
- no gamepad support
- no desktop scaling
```
javac Main.java
java Main
```

The ridiculous way:
- gamepad support
- desktop scaling
- an output folder for the binaries
- an ironwill to run all that on the command line
```
rm -Rf bin && javac -cp gamepad:.:gamepad/jinput-2.0.10.jar Main.java -d bin
java -Dsun.java2d.uiScale.enabled=true -Dsun.java2d.uiScale=2 -Djava.awt.headless=false -cp bin:gamepad/jinput-2.0.10.jar -Djava.library.path=gamepad/jinput-2.0.10-natives-all --enable-native-access=ALL-UNNAMED -Djava.util.logging.config.file=gamepad/logging.properties Main
```
