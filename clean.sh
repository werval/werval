#!/bin/bash

set -e

if hash figlet 2>/dev/null; then
	FIGLET="figlet -w 114 -W -f rectangles"
else
	FIGLET="echo"
fi

# builds
$FIGLET builds
find . -type d -name build | xargs rm -rf
echo "Clean."

# repository
$FIGLET repository
rm -rf repository/org
echo "Clean."

echo


