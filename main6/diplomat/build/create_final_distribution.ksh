#!/bin/ksh
#
# This script assumes that the distribution package has been created.
# It will create a directory named dist/<system3_name>, where
# <system3_name> consists of the root, a build number and a date,
# e.g. dist/system3_3.0_0017_112700
# Then it will create the tar file for the build based on that directory
# name, and move the result up to dist.
#
FILE_ROOT=system3_3.0.1
BUILD_NUM=$(get_buildnum.ksh)
BUILD_DATE=$(date "+%m%d%y")
TARGET="$FILE_ROOT"_"$BUILD_NUM"_"$BUILD_DATE"
mkdir dist/$TARGET
mv -f dist/system3.tar.gz dist/$TARGET/.
cp -p install.ksh dist/$TARGET/.
LOCAL_DIR=$(pwd)
cd dist
tar cf $TARGET.tar $TARGET
cd $LOCAL_DIR
mv -f dist/$TARGET.tar .
