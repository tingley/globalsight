@echo off
rem This script checks in the given file from Visual Source Safe
rem USAGE: VssCheckIn.bat <relativeProject> <realDirectory> <baseFileName>

rem *****************************************
rem *** Modify these variables if need be ***
rem VSSLOGIN=-y<user>[,<password>]
set VSSLOGIN=-yAdmin

rem VSSTLPROJECT is the VSS Top Level Project that is mapped to the DocsDirectory
set VSSTLPROJECT=$/Baxter

set PATH=%PATH%;E:\Program Files\Microsoft Visual Studio\Common\VSS\win32
rem *****************************************

rem PROJECT=fr/generic/about
set PROJECT=%1
rem REALDIR=E:\baxter\web\ias\fr\generic\about
set REALDIR=%2
rem FILENAME=about.jsp
set FILENAME=%3

set VSSPROJECT=%VSSTLPROJECT%/%PROJECT%

rem set the current project
SS CP %VSSPROJECT% %VSSLOGIN%

rem set the workfolder to the correct subdir in the docs directory
SS WORKFOLD %VSSPROJECT% %REALDIR% %VSSLOGIN%

rem add the file to VSS (ignore errors)
SS ADD %REALDIR%\%FILENAME% %VSSLOGIN% -C"Localized by GlobalSight System4"

rem check in the file (ignore errors)
cd %REALDIR%
SS CHECKIN %VSSPROJECT%/%FILENAME% %VSSLOGIN% -C"Localized by GlobalSight System4"

