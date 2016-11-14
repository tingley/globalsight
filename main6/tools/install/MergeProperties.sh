#!/bin/bash
# USAGE: MergeProperties [-noUI] [-previousHome <path to previous Ambassador home>]

java -classpath .:installer.jar:lib/commons-codec-1.3.jar:lib/ant.jar:lib/log4j.jar MergePropertiesGUI $*
