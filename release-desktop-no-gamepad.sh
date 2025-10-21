#!/bin/bash
set -e
./clean.sh

javac AWT.java -d bin
cd bin
jar cfev ../JetSlalomResurrected.jar AWT *.class ../res
cd ..
jarsigner JetSlalomResurrected.jar fluxrider

# rename to something informative
mv JetSlalomResurrected.jar JetSlalomResurrected-$(date +"%Y-%m-%d").jar

./clean.sh