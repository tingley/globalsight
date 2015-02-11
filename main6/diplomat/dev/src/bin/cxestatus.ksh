#!/bin/ksh
# cxestatus.ksh
# USAGE: cxestatus.ksh
# source the environment variables
# must be run from the cxe/bin directory (usually called by cxe/bin/serv
. ../../system3.env

if [ "$1" = "-?" ]
then
   echo "USAGE: $0";
   echo "This script outputs the status of CXE";
   exit 1;   
fi

# Check the health of Tomcat
PCRED=pcred
TOMCATPIDFILE=$GLOBALSIGHT_HOME/system3/cxe/bin/.tomcat_pid;
if [ ! -f $TOMCATPIDFILE ]
then
    echo "The CXE Webserver (Tomcat) is not running.";
else
    TOMCATPID=$(grep -v "#" $TOMCATPIDFILE | awk '{print $1}' | xargs);
    TOMCATHEALTH=$($PCRED $TOMCATPID 2>/dev/null | awk -F':' '{print $1}' | xargs | cut -f 1 -d' ');
    if [ "$TOMCATHEALTH" = "$TOMCATPID" ]
    then
	echo "The CXE Webserver(Tomcat) is running.";
    else
	echo "The CXE Webserver(Tomcat) is not running.";
    fi
fi

# Check the health of the CXE Adapters
PIDFILE=$GLOBALSIGHT_HOME/system3/cxe/bin/.cxe_pids;
if [ ! -f $PIDFILE ]
then
    echo "The CXE adapters are not running.";
else
    set -A PidList $(grep -v "#" $PIDFILE | awk '{print $1}' | xargs);
    set -A AdapterList $(grep -v "#" $PIDFILE | awk '{print $2}' | xargs);
    cnt=0
    while ((cnt < ${#PidList[@]}))
    do
	CXEHEALTH=$($PCRED ${PidList[$cnt]} 2>/dev/null | awk -F':' '{print $1}' | xargs | cut -f 1 -d' ');
	if [ "$CXEHEALTH" = "${PidList[$cnt]}" ]
	then
	    echo "${AdapterList[$cnt]} is running.";
	else
	    echo "${AdapterList[$cnt]} is not running.";
	fi
	((cnt=$cnt+1))
    done    
fi
