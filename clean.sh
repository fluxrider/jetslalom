#!/bin/bash
set -e

# desktop
rm -Rf bin/

# android
rm -Rf android/
rm -f gradle.properties
rm -f settings.gradle
rm -f build.gradle

# mixed (android and some release script)
rm -Rf build/

# release
rm -f *.apk.idsig
# don't delete the final release files unless there is an arg
if [ "$#" -eq 1 ]; then
  rm -f *.apk
  rm -f *.jar
  rm -f *.zip
fi