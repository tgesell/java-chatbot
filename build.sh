#!/usr/bin/env bash
set -e

echo "Cleaning old build artifacts..."
rm -rf server/build client-console/build
rm -f server/ChatServer.jar client-console/ChatClient.jar
rm -f server/manifest.txt client-console/manifest.txt

echo "Creating build folders..."
mkdir -p server/build
mkdir -p client-console/build

echo "Compiling server for Java 8..."
javac --release 8 -d server/build server/src/*.java

echo "Compiling client for Java 8..."
javac --release 8 -d client-console/build client-console/src/ChatClient.java

echo "Creating manifest files..."
printf "Main-Class: ChatServer\n\n" > server/manifest.txt
printf "Main-Class: ChatClient\n\n" > client-console/manifest.txt

echo "Building server JAR..."
jar cfm server/ChatServer.jar server/manifest.txt -C server/build .

echo "Building client JAR..."
jar cfm client-console/ChatClient.jar client-console/manifest.txt -C client-console/build .

echo
echo "Build complete."
echo "Created:"
echo "  server/ChatServer.jar"
echo "  client-console/ChatClient.jar"