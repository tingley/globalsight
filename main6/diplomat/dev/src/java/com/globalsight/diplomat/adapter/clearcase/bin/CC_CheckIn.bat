@echo off
rem This script checks the given file back to Clear Case
rem USAGE: CC_CheckIn.bat <realDir> <baseFileName> <activityName> <drive>

rem *****************************************
rem *** Modify these variables if need be ***
rem *****************************************

rem REALDIR=F:\view\content\fr_FR
set REALDIR=%1
rem FILENAME=about.jsp
set FILENAME=%2
rem ACTIVITYNAME="Localizing foo.html to fr_FR"
set ACTNAME=%3
rem DRIVE=D:
set DRIVE=%4
@echo on
%DRIVE% & cd %REALDIR%
cleartool setact %ACTNAME%
cleartool ci -identical -c "Localized by GlobalSight System4" %FILENAME%

