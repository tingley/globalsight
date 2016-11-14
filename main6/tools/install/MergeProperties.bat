@echo off
rem Top level MergeProperties script for NT
rem java 1.4 must be in the path in order to run
rem USAGE: MergeProperties [-noUI] [-previousHome <path to previous Ambassador home>]

cd %~dp0
java -classpath .;installer.jar;lib/commons-codec-1.3.jar;lib/ant.jar;lib/log4j.jar MergePropertiesGUI %*
