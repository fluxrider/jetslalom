#!/bin/bash
set -e

# desktop
rm -Rf bin/

# android
rm -Rf build/
rm -Rf android/
rm -f gradle.properties
rm -f settings.gradle
rm -f build.gradle