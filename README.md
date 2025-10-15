# Jet Slalom Restoration Project

![AI Generated Cover](slalom.jpg?raw=true)

I played Jet Slalom as an online applet when I was a young adult back in 1999.
I wrote a WebGL crappy homage test around 2010.
Some random guy on the internet emailed me about it in 2025, praising my controls but pooping on my inexistant levels design.

The guy also sent me a half broken code base of what he could salvage on the internet of what may have been the original code.
It didn't compile, and had commented bytecode sections.
This is an attempt at getting it working.

## State

- It compiles and seems playable on JDK 25.0.1
- The title screen is broken so I bypass it on launch. When you die, press 'spacebar' (or 'A/B/X/Y' on the gamepad).
- I've added gamepad support.
- I stripped the applet and canvas code. Now it's just a AWT window and the drawing scales respecting aspect ratio.
- Fullscreen support (using 'F' or 'Select').

### Future Work

- Fix the turn strength, I'm pretty sure it's way too hard right now.
- Fix the title screen.
- Remove unused code (e.g. I have no plan on supporting online high scores).
- Clean the code (it came to me in a decompiled state, where constants are hardcoded as value and whatnot).

### Known Issue

- JInput, the external library I use for gamepad support, requires the gamepad to be plugged before the game launched. I've only tested on Linux.

## How to run

I assume you got a Java JDK installed and know how to use the command line for your OS. I'm using a posix system (i.e. Linux). Yet I wish it was simpler. Java sucks for this.

Simplest way I can think of:
- no gamepad support
- no desktop scaling
- yet it still needs an ugly classpath hack
```
javac -cp .:gamepad_none Game3D.java
java -cp .:gamepad_none Game3D
```

The ridiculous way:
- gamepad support
- desktop scaling
- an output folder for the binaries
- an ironwill to run all that on the command line
```
rm -Rf java_out && javac -cp .:gamepad_jinput:gamepad_jinput/jinput-2.0.10.jar Game3D.java -d java_out
java -Dsun.java2d.uiScale.enabled=true -Dsun.java2d.uiScale=2 -Djava.awt.headless=false -cp java_out:gamepad_jinput/jinput-2.0.10.jar -Djava.library.path=gamepad_jinput/jinput-2.0.10-natives-all --enable-native-access=ALL-UNNAMED -Djava.util.logging.config.file=gamepad_jinput/logging.properties Game3D
```
