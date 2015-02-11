@echo off
rem This script adds the given directory to Clear Case
rem USAGE: CC_MkDir.bat <parentDir> <dirName> <activityName> <drive>

rem *****************************************
rem *** Modify these variables if need be ***
rem *****************************************


rem *****************************************
rem PARENTDIR = W:\content
set PARENTDIR=%1

rem DIRNAME=fr_FR
set DIRNAME=%2

rem ACTIVITYNAME="Localizing foo.html to fr_FR"
set ACTNAME=%3

rem DRIVE=D:
set DRIVE=%4
@echo on
%DRIVE% & cd %PARENTDIR%
cleartool setact %ACTNAME%
cleartool co -nc .
cleartool mkdir -c "Localized by GlobalSight System4" %DIRNAME%

