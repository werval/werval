#!/bin/bash
#
# Extensive build that runs all quality checks and tests
#
# First run will download all dependencies sources and javadocs for you to get them automatically linked in your
# favourite IDE.

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
./org.qiweb/gradlew -b org.qiweb/build.gradle check install idea
echo "Checked."

# modules
$FIGLET org.qiweb.modules
./org.qiweb.modules/gradlew -b org.qiweb.modules/build.gradle check install idea
echo "Checked."

# gradle plugin
$FIGLET org.qiweb.gradle
./org.qiweb.gradle/gradlew -b org.qiweb.gradle/build.gradle install check idea
echo "Checked."

# maven plugin
$FIGLET org.qiweb.maven
$MAVEN -f org.qiweb.maven/pom.xml clean verify dependency:sources -Dgpg.skip
echo "Checked."

echo ""
