@echo off
setlocal
setlocal enabledelayedexpansion

rem A wrapper that sets up a stand-alone environment for GS code.

rem If the script resides in an install tree, it will use it.
rem If the script resides in a source tree, first run
rem ant write-run-classpath -Dgs.home=<path to globalsight.ear>

set DIR=%~dp0

rem Setting GS_JAVA allows you to run an alternate command, eg gsjdb
if not defined GS_JAVA set GS_JAVA=java

if exist !DIR!\jboss_server\server\default\deploy\globalsight.ear\lib\classes\properties\envoy_generated.properties (
    set GS_JBOSS=!DIR!
    set LIB=!GS_JBOSS!\jboss_server\server\default\deploy\globalsight.ear\lib
    rem - this DIR for log4j.properties
    rem - globalsight.ear\lib\classes for properties, resources, and patches
    rem - some specific jars that must be loaded before other versions
    rem - globalsight.ear\lib\* for most libs (GS and third-party)
    rem - jboss_server\server\default\lib\* for app server libs (eg servlet)
    set CLASSPATH=^
!GS_CLASSPATH_PRE!;^
!GS_JBOSS!;^
!LIB!\classes;^
!LIB!\hibernate\dom4j-1.6.1.jar;^
!LIB!\jaxen-1.1-beta-4.jar;^
!LIB!\*;^
!GS_JBOSS!\jboss_server\server\default\lib\*;^
!CLASSPATH!
) else if exist !DIR!\..\..\..\main6\tools\build\build.xml (
    set GS_SOURCE=!DIR!\..\..\..
    if not exist !GS_SOURCE!\main6\tools\build\run.classpath.bat (
        ant -buildfile !GS_SOURCE!\main6\tools\build\build.xml write-run-classpath >nul
        if ERRORLEVEL 1 exit /b 1
    )
    rem sets RUNCLASSPATH
    call !GS_SOURCE!\main6\tools\build\run.classpath.bat
    set CLASSPATH=!GS_CLASSPATH_PRE!;!DIR!;!RUNCLASSPATH!;!CLASSPATH!
) else (
    echo gsjava not located in a source tree or an install!! >&2
    exit /b 1
)
!GS_JAVA! %*
