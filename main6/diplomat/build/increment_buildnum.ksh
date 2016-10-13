#!/bin/ksh
#
# This script checks out the text file containing the build number, 
# increments the number and writes the the result back to the file,
# then checks the file back in.
#
if [ $LOGNAME = build ]
then
	echo "Incrementing build number"
	BUILD_HOST=`hostname`
	BUILD_DATE=`date "+%B %e, %Y"`
	BUILD_NUM_FILE=buildnum.txt
	COMMENT="Incrementing build number for $BUILD_HOST on $BUILD_DATE"
	cleartool setactivity $INCREMENT_BUILDNUM > /dev/null
	cleartool checkout -c "$COMMENT" $BUILD_NUM_FILE > /dev/null
	BUILD_NUM=$(cat $BUILD_NUM_FILE)
	echo $((BUILD_NUM+1)) > $BUILD_NUM_FILE
	BUILD_NUM=$(cat $BUILD_NUM_FILE)
	cleartool checkin -nc $BUILD_NUM_FILE > /dev/null
	echo "Updating help file"
	HELP_PATH=/vob/globalsightVOB/as-SSL/lib/AS_docs/help
	HELP_FILE=about_system3.html
	CURR_PATH=`pwd`
	cd $HELP_PATH
	COMMENT="Updating build details for $BUILD_HOST on $BUILD_DATE"
	cleartool checkout -c "$COMMENT" $HELP_FILE > /dev/null
	sed "s/Build Number:.*<\/f/Build Number: $BUILD_NUM<\/f/" $HELP_FILE > temp_a
	sed "s/Build Date:.*<\/f/Build Date: $BUILD_DATE<\/f/" temp_a > temp_b
	mv -f temp_b $HELP_FILE
	rm -f temp_a temp_b
	cleartool checkin -nc $HELP_FILE > /dev/null
	cleartool setactivity -none > /dev/null
	cd $CURR_PATH
else
	echo "Only user build is allowed to execute the command $0"
fi
