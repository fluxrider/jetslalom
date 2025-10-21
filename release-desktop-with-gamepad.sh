#!/bin/bash
set -e
./clean.sh

javac -cp gamepad:.:gamepad/jinput-2.0.10.jar AWT.java -d bin
cd bin
cat > MANIFEST.MF <<EOF
Main-Class: AWT
Class-Path: jinput-2.0.10.jar
EOF
jar cfmv ../JetSlalomResurrected.jar MANIFEST.MF *.class ../res
cd ..
mkdir -p build
mv JetSlalomResurrected.jar build/
cp -R gamepad/jinput-2.0.10-natives-all build/
cp gamepad/jinput-2.0.10.jar build/
cp gamepad/logging.properties build/
echo "java -Djava.library.path=jinput-2.0.10-natives-all --enable-native-access=ALL-UNNAMED -Djava.util.logging.config.file=logging.properties -jar JetSlalomResurrected.jar" > build/run.sh
cd build
chmod +x run.sh
zip -r JetSlalomResurrected.zip jinput-2.0.10-natives-all *.*
mv JetSlalomResurrected.zip ..
cd ..

# extract the zip and execute run.sh