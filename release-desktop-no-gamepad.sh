#!/bin/bash
set -e
./clean.sh

javac AWT.java -d bin
cd bin
jar cfev ../JetSlalomResurrected.jar AWT *.class ../res
cd ..
# java -jar JetSlalomResurrected.jar