#!/bin/bash
#
# Quick build that run no quality checks nor tests
#
# First run is not quick at all as it will download all dependencies sources and javadocs for you to get them
# automatically linked in your favourite IDE.

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
./org.qiweb/gradlew -b org.qiweb/build.gradle install check idea -x licenseMain -x checkstyleMain -x test
echo "Built."

# modules
$FIGLET org.qiweb.modules
./org.qiweb.modules/gradlew -b org.qiweb.modules/build.gradle install check idea -x licenseMain -x checkstyleMain -x test
echo "Built."

# gradle plugin
$FIGLET org.qiweb.gradle
./org.qiweb.gradle/gradlew -b org.qiweb.gradle/build.gradle install check idea -x checkstyleMain -x test
echo "Built."

# maven plugin
$FIGLET org.qiweb.maven
$MAVEN -f org.qiweb.maven/pom.xml install dependency:sources -DskipTests -Dgpg.skip
echo "Built."

echo ""
