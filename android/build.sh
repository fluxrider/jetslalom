set -e

./clean.sh

# I dislike modern framework so much. I'm storing only the files I want, location, format, and create the idiotic requirements android wants on the fly. I ain't storing 5 icon flavors.
# Of course this means you better be running this script in an environment similar to mine (e.g. image magick is installed)

mkdir app/src/main/res/mipmap-xxxhdpi
#magick ../icon_square_512.png -resize 512x512\! app/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png
cp ../icon_square_512.png app/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png
magick app/src/main/res/mipmap-xxxhdpi/app_icon_foreground.png -colorspace Gray app/src/main/res/mipmap-xxxhdpi/app_icon_grayscale.png

gradle --warning-mode all assembleDebug