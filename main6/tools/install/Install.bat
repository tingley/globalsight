@echo off
rem Top level Install script for NT
rem some bin\java must be in the path in order to run
rem USAGE: Install [-noUI] [-properties <path to installValues.properties>]

set NLS_LANG=American_America.UTF8
java -classpath .;installer.jar;commons-codec-1.3.jar;ant.jar InstallAmbassador %*

