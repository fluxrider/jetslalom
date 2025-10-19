set -e

./clean.sh

# TODO copy files where android needs them to be for building

gradle --warning-mode all assembleDebug