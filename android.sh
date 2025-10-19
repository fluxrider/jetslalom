set -e
shopt -s nullglob
./clean.sh

# I dislike modern framework so much. I'm storing only the files I want, location, format, and all then create the idiotic requirements android wants on the fly. I ain't storing 5 icon flavors or one line files that mean nothing to me.
# Of course this means you better be running this script in an environment similar to mine (e.g. image magick is installed)

# boring files for gradle
[ ! -f local.properties ] && echo "ERROR: local.properties is missing. Create it with path to android SDK (e.g. sdk.dir=/home/flux/_/apps/android-sdk/)" && exit 1
#echo "org.gradle.jvmargs=-Xmx1536m" > gradle.properties # I'm gonna go out on a limb here and comment this out. Works for me with what I imagine are sensible defaults.
echo "include ':android'" > settings.gradle
cat > build.gradle <<EOF
buildscript {
  repositories { mavenCentral(); google(); }
  // I'm living on the edge. What is the point of improving gradle, the JDK, or android if everyone is encouraged to lock versions?
  // There is nothing on my end that says: I want JDK 25, gradle 9.0.0-alpha11, and Android Platform 36.1, except those are the current latest version (actually gradle on my system is version 9.1 but android doesn't support it yet).
  // The real question is why does gradle break on new JDK releases, and why does android break on new gradle releases? This is a very disfunctional toolchain.
  // Anyway, let's assume latest is best and that the devs of those libs try their best not to break userspace (spoiler alert: they don't care). This will break in a week.
  dependencies { classpath 'com.android.tools.build:gradle:+' }
}

allprojects {
  repositories { mavenCentral(); google(); }
  // gradle.projectsEvaluated { tasks.withType(JavaCompile) { options.compilerArgs.add("-Xlint:deprecation") } }
}
EOF

# android config, mostly boring but it does contain the package name and android version min/target
mkdir android
cat > android/build.gradle <<EOF
apply plugin: 'com.android.application'
android {
  namespace = "fluxrider.jetslalom"
  compileSdkVersion = "android-36.1"; defaultConfig { minSdkVersion = 36; targetSdkVersion = 36; versionCode = 1; versionName = "1.0"; }
  buildTypes { release { minifyEnabled = false } }
  lint { abortOnError = false }
}
EOF
mkdir -p android/src/main
cat > android/src/main/AndroidManifest.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" android:versionCode="1" android:versionName="1.0" android:installLocation="auto">
  <application android:label="@string/app_name" android:icon="@mipmap/app_icon" android:roundIcon="@mipmap/app_icon" android:allowBackup="true" android:theme="@android:style/Theme.NoTitleBar.Fullscreen" android:hardwareAccelerated="true">
    <activity android:name="Android"
      android:label="@string/app_name"
      android:alwaysRetainTaskState="true"
      android:launchMode="singleInstance"
      android:screenOrientation="fullUser"
      android:configChanges="layoutDirection|locale|orientation|uiMode|screenLayout|screenSize|smallestScreenSize|keyboard|keyboardHidden|navigation"
      android:preferMinimalPostProcessing="true"
      android:exported="true">
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
    </activity>
  </application>
</manifest>
EOF
mkdir -p android/src/main/res/values
cat > android/src/main/res/values/strings.xml <<EOF
<resources>
  <string name="app_name">Jet Slalom Resurrected</string>
</resources>
EOF
cat > android/src/main/res/values/colors.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <color name="colorPrimary">#3F51B5</color>
  <color name="colorPrimaryDark">#303F9F</color>
  <color name="colorAccent">#FF4081</color>
  <color name="app_icon_background">#f6d66c</color>
</resources>
EOF
cat > android/src/main/res/values/styles.xml <<EOF
<resources>
  <style name="AppTheme" parent="android:Theme.Holo.Light.DarkActionBar"></style>
</resources>
EOF

# src, but also prepend the package to all files, damn nothing is ever simple.
mkdir -p android/src/main/java/fluxrider.jetslalom/
for f in *.java; do
  f=$(basename -- "$f")
  echo "package fluxrider.jetslalom;" > android/src/main/java/fluxrider.jetslalom/$f
  cat $f >> android/src/main/java/fluxrider.jetslalom/$f
done
rm android/src/main/java/fluxrider.jetslalom/AWT.java

# icon
mkdir -p android/src/main/res/mipmap-xxxhdpi
cp icon_512.png android/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png

# icon madness (from dpi folder mess, to handling round icon like idiots, now android wants a so called adaptive icon, which ends up being a xml file that is identical for us all, great engineering bud)
# and why the monochrome version, can't grayscale yourself?
mkdir android/src/main/res/mipmap-anydpi-v26
cat > android/src/main/res/mipmap-anydpi-v26/app_icon.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
  <background android:drawable="@color/app_icon_background"/>
  <foreground android:drawable="@mipmap/app_icon_foreground"/>
  <monochrome android:drawable="@mipmap/app_icon_grayscale"/>
</adaptive-icon>
EOF
# if the script fails on this line, maybe just comment it out and remove the line from the app_icon.xml just above. Who cares about monochrome icons?
magick android/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png -colorspace Gray android/src/main/res/mipmap-xxxhdpi/app_icon_grayscale.png

# finally, build & install
gradle --warning-mode all assembleDebug
gradle installDebug