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
$FIGLET io.werval
./gradlew -q --stacktrace -b io.werval/build.gradle install
echo "Built."

# gradle plugin
$FIGLET io.werval.gradle
./gradlew -q --stacktrace -b io.werval.gradle/build.gradle install
echo "Built."

# maven plugin
$FIGLET io.werval.maven
$MAVEN -q -e -f io.werval.maven/pom.xml install -DskipTests -Dgpg.skip
echo "Built."

# modules
$FIGLET io.werval.modules
./gradlew -q --stacktrace -b io.werval.modules/build.gradle install
echo "Built."

echo ""
