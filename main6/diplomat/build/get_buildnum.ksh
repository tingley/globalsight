#!/bin/ksh
#
# This script reads the contents of the buildnum file and formats the
# build number by padding with up to 3 zeros.
#
BUILD_NUM="000"$(cat buildnum.txt)
START=$(echo $BUILD_NUM | wc -c)
END=$((START-1))
START=$((START-4))
echo $BUILD_NUM | cut -c$START-$END
