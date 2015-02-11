@echo off
setlocal
setlocal enabledelayedexpansion

rem A wrapper that sets up a stand-alone environment for GS code.

rem If the script resides in an install tree, it will use it.
rem If the script resides in a source tree, first run
rem ant write-run-classpath -Dgs.home=<path to globalsight.ear>

set DIR=%~dp0

rem Setting GS_JAVA allows you to run an alternate command, eg gsjdb.sh
if not defined GS_JAVA set GS_JAVA=java

if exist !DIR!\jboss_server\server\default\deploy\globalsight.ear\lib\classes\properties\envoy_generated.properties (
    set LIB=!DIR!\jboss_server\server\default\deploy\globalsight.ear\lib
    rem - this DIR for log4j.properties
    rem - globalsight.ear/lib/classes for properties and resources
    rem - globalsight.ear/lib/* for compiled code (GS and third-party)
    set CLASSPATH=^
!DIR!;^
!LIB!\classes;^
!LIB!\dom4j-1.6.1.jar;^
!LIB!\jaxen-1.1-beta-4.jar;^
!LIB!\*;^
!CLASSPATH!
) else if exist !DIR!\..\..\..\main6\tools\build\build.xml (
    set GS_SOURCE=!DIR!\..\..\..
    if not exist !GS_SOURCE!\main6\tools\build\run.classpath.bat (
        ant -buildfile !GS_SOURCE!\main6\tools\build\build.xml write-run-classpath >nul
        if ERRORLEVEL 1 exit /b 1
    )
    rem sets RUNCLASSPATH
    call !GS_SOURCE!\main6\tools\build\run.classpath.bat
    set CLASSPATH=!DIR!;!RUNCLASSPATH!;!CLASSPATH!
) else (
    echo gsjava.sh not located in a source tree or an install!! >&2
    exit /b 1
)
!GS_JAVA! %*
