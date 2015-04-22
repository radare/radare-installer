#!/bin/sh
if [ -z "$2" ]; then
	echo "Usage: apk-clean.sh [src] [dst]"
	exit 1
fi
rm -rf _tmp
mkdir -p _tmp
unzip "$1" -d _tmp
rm -rf _tmp/doc
rm -rf _tmp/com
rm -rf _tmp/bin
cd _tmp && 
# only works with relative paths
rm -f "../$2"
zip -r "../$2" *
rm -rf _tmp
