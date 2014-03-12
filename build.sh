#!/bin/bash

# QiWeb SDK Build Script
#
# The QiWeb SDK is split in several projects, some use gradle, some maven.
# This script allows easy ordered build of several projects at once.
# It is provided as a convenience purpose for QiWeb contributors to assist
# them while working on the SDK code.
# You can also use this script to create a custom build of the QiWeb SDK.

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
$GRADLE -b org.qiweb/build.gradle install
echo "Built."

# modules
$FIGLET org.qiweb.modules
$GRADLE -b org.qiweb.modules/build.gradle install
echo "Built."

# gradle plugin
$FIGLET org.qiweb.gradle
$GRADLE -b org.qiweb.gradle-tooling/build.gradle install
$GRADLE -b org.qiweb.gradle/build.gradle install
echo "Built."

# maven plugin
$FIGLET org.qiweb.maven
# $MAVEN -f org.qiweb.maven/pom.xml test
echo "NOT BUILT - No Java 8 support for Maven plugins yet"

# samples
$FIGLET org.qiweb.samples
$GRADLE -b org.qiweb.samples/build.gradle assemble
echo "Built."

echo

