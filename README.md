# Jet Slalom Restoration Project

![AI Generated Cover](slalom.jpg?raw=true)

I played Jet Slalom as an online applet when I was a young adult back in 1999.
I wrote a WebGL crappy homage test around 2010.
Some random guy on the internet emailed me about it in 2025, praising my controls but pooping on my inexistant levels design.

The guy also sent me a half broken code base of what he could salvage on the internet of what may have been the original code.
It didn't compile, and had commented bytecode sections.
This is an attempt at getting it working.

## State

I got it to compile and render the obstacles and ship. It looks like you can play one game. I bypass the title screen because it's broken. It shows up when you die. Spacebar starts a new game.

I stripped the applet part. Now it's just a AWT window. I even removed the canvas object and simply paint scaled to the panel so it fits the window.

### Future Work

- Gamepad
  - Organise project such that it can be built without gamepad support, and therefore without external libraries.
  - Quiet maven logs from this library -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN
- Fix the title
- Remove unused code
- Fix the turn strenght, because I'm pretty sure it's way too hard right now.

## How to run

Simple way:
```
javac Game3D.java
java Game3D
```

The way I do it (desktop scaling, and a different folder for the binaries):
```
rm -Rf java_out && javac -cp .:jinput-2.0.10.jar Game3D.java -d java_out
java -Dsun.java2d.uiScale.enabled=true -Dsun.java2d.uiScale=2 -Djava.awt.headless=false -cp java_out:jinput-2.0.10.jar -Djava.library.path=jinput-2.0.10-natives-all --enable-native-access=ALL-UNNAMED Game3D
```

This obviously assumes you got a Java JDK installed and know how to use the command line.
