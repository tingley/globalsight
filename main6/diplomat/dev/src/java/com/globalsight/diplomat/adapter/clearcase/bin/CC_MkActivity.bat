@echo off
rem This script adds the given directory to Clear Case
rem USAGE: CC_MkDir.bat <realDir> <activityName> <drive>

rem *****************************************
rem *** Modify these variables if need be ***
rem *****************************************


rem *****************************************
rem REALDIR="W:\Clearcase\fr_FR"
set REALDIR=%1
rem ACTIVITYNAME="Localizing foo.html to fr_FR"
set ACTNAME=%2
rem DRIVE=D:
set DRIVE=%3
@echo on
%DRIVE% & cd %REALDIR%
cleartool mkact %ACTNAME%

