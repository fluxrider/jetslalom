#!/bin/bash
set -e
./clean.sh

javac -cp gamepad:.:gamepad/jinput-2.0.10.jar AWT.java -d bin
java -cp bin:gamepad/jinput-2.0.10.jar:. -Djava.library.path=gamepad/jinput-2.0.10-natives-all --enable-native-access=ALL-UNNAMED -Djava.util.logging.config.file=gamepad/logging.properties AWT