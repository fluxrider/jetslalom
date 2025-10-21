#!/bin/bash
set -e
./clean.sh

./android.sh just-project-setup
gradle assembleRelease
cp android/build/outputs/apk/release/android-release-unsigned.apk JetSlalomResurrected.apk

# as per https://developer.android.com/tools/zipalign recommandation, align 16 for .so and 4 for the rest. I don't include .so myself but who knows what gradle does.
#~/_/apps/android-sdk/build-tools/36.1.0/zipalign -P 16 -f -v 4 JetSlalomResurrected.apk
# however, the zip align step seems to be already done, when we verify
#~/_/apps/android-sdk/build-tools/36.1.0/zipalign -c -P 16 -v 4 JetSlalomResurrected.apk

~/_/apps/android-sdk/build-tools/36.1.0/apksigner sign -ks ~/.keystore -ks-key-alias fluxrider JetSlalomResurrected.apk

# rename to something informative
mv JetSlalomResurrected.apk JetSlalomResurrected-sdk36-os16-$(date +"%Y-%m-%d").apk

./clean.sh