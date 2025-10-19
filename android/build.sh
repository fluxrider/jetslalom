set -e

./clean.sh

# I dislike modern framework so much. I'm storing only the files I want, location, format, and all then create the idiotic requirements android wants on the fly. I ain't storing 5 icon flavors or one line files that mean nothing to me.
# Of course this means you better be running this script in an environment similar to mine (e.g. image magick is installed)

# boring files, enjoy your slop gradle
#echo "org.gradle.jvmargs=-Xmx1536m" > gradle.properties # I'm gonna go out on a limb here and comment this out. Works for me with what I imagine are sensible defaults.
[ ! -f local.properties ] && echo "ERROR: local.properties is missing. It should exist and contain a line to the path to your android SDK (e.g. sdk.dir=/home/flux/_/apps/android-sdk/)" && exit 1

# icon madness (from dpi folder mess, to handling round icon like idiots, now android wants a so called adaptive icon, which ends up being a xml file that is identical for us all, great engineering bud)
mkdir app/src/main/res/mipmap-xxxhdpi
cp ../icon_512.png app/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png
magick app/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png -colorspace Gray app/src/main/res/mipmap-xxxhdpi/app_icon_grayscale.png
mkdir app/src/main/res/mipmap-anydpi-v26
cat > app/src/main/res/mipmap-anydpi-v26/app_icon.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
  <background android:drawable="@color/app_icon_background"/>
  <foreground android:drawable="@mipmap/app_icon_foreground"/>
  <monochrome android:drawable="@mipmap/app_icon_grayscale"/>
</adaptive-icon>
EOF

gradle --warning-mode all assembleDebug