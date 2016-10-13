@echo off
rem This script creates the projects in VSS
rem USAGE: VssMkProj.bat <relativeProject>

rem *****************************************
rem *** Modify these variables if need be ***
rem VSSLOGIN=-y<user>[,<password>]
set VSSLOGIN=-yAdmin

rem VSSTLPROJECT is the VSS Top Level Project that is mapped to the DocsDirectory
set VSSTLPROJECT=$/Baxter

set PATH=%PATH%;E:\Program Files\Microsoft Visual Studio\Common\VSS\win32
rem *****************************************

rem PROJECT=fr/generic/about -- the project directory where to add the file
rem this is relative to the top level project
set PROJECT=%1

set VSSPROJECT=%VSSTLPROJECT%/%PROJECT%

SS CREATE %VSSPROJECT% %VSSLOGIN% -C"Created by GlobalSight System4"

