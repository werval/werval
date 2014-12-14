#!/bin/bash
#
# Build Werval distribution archives without running quality checks and tests

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
./gradlew -b io.werval/build.gradle install check idea -x licenseMain -x checkstyleMain -x test
echo "Built."

# gradle plugin
$FIGLET io.werval.gradle
./gradlew -b io.werval.gradle/build.gradle install check idea -x checkstyleMain -x test
echo "Built."

# maven plugin
$FIGLET io.werval.maven
$MAVEN -f io.werval.maven/pom.xml install dependency:sources -DskipTests -Dgpg.skip
echo "Built."

# modules
$FIGLET oio.werval.modules
./gradlew -b io.werval.modules/build.gradle install check idea -x licenseMain -x checkstyleMain -x test
echo "Built."

# dist
$FIGLET io.werval.dist
./gradlew -b io.werval.dist/build.gradle install
echo "Built."

echo ""
