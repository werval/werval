#!/bin/bash

set -e

if hash mvn 2>/dev/null; then
	MAVEN="mvn -q -e"
else
	echo >&2 "I require Maven but it's not installed.  Aborting."
	exit 1
fi
if hash figlet 2>/dev/null; then
	FIGLET="figlet -w 114 -W -f rectangles"
else
	FIGLET="echo"
fi


# core
$FIGLET org.qiweb
./org.qiweb/gradlew -b org.qiweb/build.gradle install check -x test
echo "Built."

# modules
$FIGLET org.qiweb.modules
./org.qiweb.modules/gradlew -b org.qiweb.modules/build.gradle install check -x test
echo "Built."

# gradle plugin
$FIGLET org.qiweb.gradle
./org.qiweb.gradle/gradlew -b org.qiweb.gradle/build.gradle install check -x test
echo "Built."

# maven plugin
$FIGLET org.qiweb.maven
$MAVEN -f org.qiweb.maven/pom.xml install -DskipTests -Dgpg.skip
echo "Built."

echo

