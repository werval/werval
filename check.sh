#!/bin/bash

set -e

if hash gradle 2>/dev/null; then
	GRADLE="gradle -q --stacktrace"
else
	echo >&2 "I require Gradle but it's not installed.  Aborting."
	exit 1
fi
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
$GRADLE -b org.qiweb/build.gradle check install
echo "Checked."

# modules
$FIGLET org.qiweb.modules
$GRADLE -b org.qiweb.modules/build.gradle check install
echo "Checked."

# gradle plugin
$FIGLET org.qiweb.gradle
$GRADLE -b org.qiweb.gradle/build.gradle install check
echo "Checked."

# maven plugin
$FIGLET org.qiweb.maven
# $MAVEN -f org.qiweb.maven/pom.xml test
echo "NOT CHECKED - No Java 8 support for Maven plugins yet"

echo

