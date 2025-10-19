set -e

./clean.sh

# I dislike modern framework so much. I'm storing files where I want, the way I want, and create the idiotic requirements android wants on the fly. I ain't storing 5 icon flavors.
# Of course this means you better be running this script in an environment similar to mine (e.g. image magick)

# TODO just include xxxhdpi and see if it works fine on my 2025 phone. The android doc about icon seems to have been written in 2015.

mkdir app/src/main/res/mipmap-mdpi
mkdir app/src/main/res/mipmap-hdpi
mkdir app/src/main/res/mipmap-xhdpi
mkdir app/src/main/res/mipmap-xxhdpi
mkdir app/src/main/res/mipmap-xxxhdpi

magick ../icon_square_512.png -resize 48x48\! app/src/main/res/mipmap-mdpi/app_icon.png
magick ../icon_square_512.png -resize 72x72\! app/src/main/res/mipmap-hdpi/app_icon.png
magick ../icon_square_512.png -resize 96x96\! app/src/main/res/mipmap-xhdpi/app_icon.png
magick ../icon_square_512.png -resize 144x144\! app/src/main/res/mipmap-xxhdpi/app_icon.png
magick ../icon_square_512.png -resize 192x192\! app/src/main/res/mipmap-xxxhdpi/app_icon.png

gradle --warning-mode all assembleDebug