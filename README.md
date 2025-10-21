# Jet Slalom Restoration Project

![AI Generated Cover](slalom.jpg?raw=true)

![Gameplay Screenshot](doc/screenshot_play1.png?raw=true)
![Gameplay Screenshot](doc/screenshot_play2.png?raw=true)
![Title Screenshot](doc/screenshot_hq.png?raw=true)
![HighRez Feature](doc/screenshot_title.png?raw=true)

I played Jet Slalom as an online Java applet when I was a young adult back in 1999.
I wrote a crappy WebGL homage test around 2010.
Some random guy on the internet emailed me about it in 2025, praising my controls but pooping on my inexistant levels design.

The guy also sent me a half broken code base of what he could salvage off the internet of what may have been a decompiled version of the original code.
It didn't compile, and had commented bytecode sections. This is me getting it to a working state.

## State

- It compiles and is playable on JDK 25.0.1
- Heck, I've ported it to Android too, so you can play either on desktop or mobile.
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
  - It started with 18 files 2000 lines, and now it's 5 files 800 lines (not including Android.java).
  - There are still some vestige of decompilation.
  - My personal coding style is offensive to most because I'm a disabled hermit.
  - I've done changes that alter a bit the math to my taste, so if you wanted maximum fidelity this ain't it, but it's close enough.

### Future Work

- Web Browser version somehow. How low did Java fall? Applet and JavaWebStart are both dead. Wikipedia encourages me to look into IcedTea-Web. What the hell is that?

### Known Issue

- JInput, the external library I use for gamepad support, requires the gamepad to be plugged before the app starts. I've only tested on Linux.
- The world of desktop scaling, 2K, 4K, and upscaling is a mess.
  - When the app was using AWT widgets, it required `-Dsun.java2d.uiScale.enabled=true -Dsun.java2d.uiScale=2 -Djava.awt.headless=false` to look good.
  - I've replaced the widgets with drawString to make the app more portable and simpler, but I still suspect there are problems depending of your environment, and getting the Font to look good is now a chore.
- Android
  - I can't figure how to detect when the navigation (i.e. 3 button overlay) or status bar (i.e. clock, battery) overlays show up. I would love to pause when this happens. I tried 3 different ways to no avail.

### How to run the release files

The jar file is the simplest possible, which sadly still requires command line 101, and an intuition that when I write 'version' in the filename, it's not verbatim.
```
java -jar JetSlalomResurrected-version.jar
```

The zip is for Desktop too and adds gamepad support but has a 3rd party library which is bound to backfire one day.
```
./run.sh
```

The apk is an android release. The development is hard enough, so it is easier to pick the latest version of android as a target. It's a jerk move but it's what I did. You need Android 16 to install it. Get the file on your phone, and run it. If you live in an alternate universe it will work no problem. In this one you'll have an ugly dialog telling you it wasn't scanned by an authority, and the button to install anyway will be hidden. Worst, when it shows it has no background so it doesn't look like a button compared to the rest of the dialog. Worst, I'm writting this in 2025. Google is threatening to disable all sideloading soon. Worst, I probably forget that you have to allow sideloading to begin with in the phone settings, something I've done once and forgot how to do.

## How to build

I assume you got a Java JDK installed and know how to use the command line for your OS.

Simple most portable way:
- no gamepad support
```
javac AWT.java
java AWT
```

If you want gamepad support, but aren't running a posix system (e.g. Linux), you'll have to look under the hood in desktop.sh and figure out the altered syntax yourself for the classpath. I ain't supporting Windows ever.
```
./desktop.sh
```

If you want to build for android, you are probably going to suffer. One because Google breaks their build system every other month. Second because I'm a jerk and you probably noticed there aren't any android folder structure commited.
The scripts generates everything on the fly. I ain't bending the knee to no conventions.
```
./android.sh
```
