#!/usr/bin/sh
# Top level MergeProperties script for Solaris
# java 1.4 must be in the path in order to run
# USAGE: MergeProperties [-noUI] [-previousHome <path to previous Ambassador home>]

java -classpath .:installer.jar MergePropertiesGUI $*
