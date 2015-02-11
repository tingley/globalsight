@echo off
rem This script updates the local view with all the latest changes
rem USAGE: VssGetLatest.bat <project> <workingDirectory>

rem *****************************************
rem *** Modify these variables if need be ***
rem VSSLOGIN=-y<user>[,<password>]
set VSSLOGIN=-yAdmin

rem VSSTLPROJECT is the VSS Top Level Project that is mapped to the DocsDirectory
set VSSTLPROJECT=$/Baxter

set PATH=%PATH%;E:\Program Files\Microsoft Visual Studio\Common\VSS\win32
rem *****************************************

set PROJECT=%1
set WD=%2
set VSSPROJECT=%VSSTLPROJECT%/%PROJECT%

rem set the current project
SS GET %VSSPROJECT% %VSSLOGIN% -GL%WD% -I-Y

