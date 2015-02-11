#!/bin/ksh
#
# This is a place-holder script that executes another script
# with the same name within the source code hierarchy.
#
ROOT_DIR=`pwd -P`
cd ../com/globalsight/ling/docproc
./make_regexps.ksh
cd $ROOT_DIR
