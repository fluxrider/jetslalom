set -e

./clean.sh

# I dislike modern framework so much. I'm storing only the files I want, location, format, and all then create the idiotic requirements android wants on the fly. I ain't storing 5 icon flavors or one line files that mean nothing to me.
# Of course this means you better be running this script in an environment similar to mine (e.g. image magick is installed)

# boring files, enjoy your slop gradle
#echo "org.gradle.jvmargs=-Xmx1536m" > gradle.properties # I'm gonna go out on a limb here and comment this out. Works for me with what I imagine are sensible defaults.

mkdir app/src/main/res/mipmap-xxxhdpi
cp ../icon_512.png app/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png
magick app/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png -colorspace Gray app/src/main/res/mipmap-xxxhdpi/app_icon_grayscale.png

gradle --warning-mode all assembleDebug