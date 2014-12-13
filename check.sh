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
$FIGLET io.werval
./gradlew -b org.qiweb/build.gradle check install idea
echo "Checked."

# gradle plugin
$FIGLET io.werval.gradle
./gradlew -b io.werval.gradle/build.gradle install check idea
echo "Checked."

# maven plugin
$FIGLET io.werval.maven
$MAVEN -f io.werval.maven/pom.xml clean verify dependency:sources -Dgpg.skip
echo "Checked."

# modules
$FIGLET io.werval.modules
./gradlew -b org.qiweb.modules/build.gradle check install idea
echo "Checked."

echo ""
