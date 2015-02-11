@echo off
rem Top level MergeProperties script for NT
rem java 1.4 must be in the path in order to run
rem USAGE: MergeProperties [-noUI] [-previousHome <path to previous Ambassador home>]

java -classpath .;installer.jar MergePropertiesGUI %*
