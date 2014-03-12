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
$GRADLE -b org.qiweb/build.gradle clean
echo "Clean."

# modules
$FIGLET org.qiweb.modules
$GRADLE -b org.qiweb.modules/build.gradle clean
echo "Clean."

# gradle plugin
$FIGLET org.qiweb.gradle
$GRADLE -b org.qiweb.gradle-tooling/build.gradle clean
$GRADLE -b org.qiweb.gradle/build.gradle clean
echo "Clean."

# maven plugin
$FIGLET org.qiweb.maven
$MAVEN -f org.qiweb.maven/pom.xml clean
echo "Clean."

# samples
$FIGLET org.qiweb.samples
$GRADLE -b org.qiweb.samples/build.gradle clean
echo "Clean."

echo


