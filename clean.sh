#!/bin/bash
#
# Clean the QiWeb source tree
#
# Don't run from outside of the QiWeb source tree, it is pretty greedy :)

set -e

if hash figlet 2>/dev/null; then
	FIGLET="figlet -w 114 -W -f rectangles"
else
	FIGLET="echo"
fi

# builds
$FIGLET builds
find . -type d -name build | xargs rm -rf
rm -rf io.werval.maven/target
echo "Clean."

# repository
$FIGLET repository
rm -rf repository/org
echo "Clean."

echo ""
