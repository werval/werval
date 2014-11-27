#!/bin/bash
#
# Build QiWeb distribution archives without running quality checks and tests

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
./gradlew -b org.qiweb/build.gradle install check idea -x licenseMain -x checkstyleMain -x test
echo "Built."

# gradle plugin
$FIGLET org.qiweb.gradle
./gradlew -b org.qiweb.gradle/build.gradle install check idea -x checkstyleMain -x test
echo "Built."

# maven plugin
$FIGLET org.qiweb.maven
$MAVEN -f org.qiweb.maven/pom.xml install dependency:sources -DskipTests -Dgpg.skip
echo "Built."

# modules
$FIGLET org.qiweb.modules
./gradlew -b org.qiweb.modules/build.gradle install check idea -x licenseMain -x checkstyleMain -x test
echo "Built."

# dist
$FIGLET org.qiweb.dist
./gradlew -b org.qiweb.dist/build.gradle install
echo "Built."

echo ""
