#!/bin/sh

#
# set up a shell environment to use the ling code
#
TOP=`pwd`

# Path separator: ";" for Windows, ":" for Unix
SEP=";"

# Required libraries - delete if in system classpath...
JGL=${TOP}/lib/jgl3.1.0.jar
JUNIT=${TOP}/lib/junit3.2.jar
ORO=${TOP}/lib/oroMatcher1.1.0.jar
REGEXP=${TOP}/lib/jakarta-regexp-1.1.jar
XALAN=${TOP}/lib/xalan.1.2.do2.jar
XERCES=${TOP}/lib/xerces.1.2.0.jar

# Output dir
DIPLOMAT=${TOP}/diplomat.jar

# now set a new classpath
CLASSPATH="${DIPLOMAT}${SEP}${XERCES}${SEP}${XALAN}${JGL}${SEP}${SEP}${REGEXP}${SEP}${ORO}${SEP}${JUNIT}${SEP}${CLASSPATH}"

