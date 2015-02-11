@echo off
setlocal
setlocal enabledelayedexpansion

rem A wrapper that runs jdb with the sourcepath set to the GS source (and
rem third-party sources, if they are installed above the GS source).
rem Meant to be run as:
rem GS_JAVA=./gsjdb.sh ./gsjava.sh ...
rem This allows to debug any wrapper using gsjava.sh

set CWD=%~dp0
if not defined GS_SOURCE (
    if exist !CWD!\..\..\..\main6\tools\build\build.xml (
        set GS_SOURCE=!CWD!\..\..\..
    ) else (
        echo set GS_SOURCE to the root of the source tree >&2
        exit /b 1
    )
)

set THIRD_SOURCE=!GS_SOURCE!\..

set SOURCEPATH=^
!GS_SOURCE!\main6\envoy\src\java;^
!GS_SOURCE!\main6\diplomat\dev\src\java;^
!GS_SOURCE!\main6\ling;^
!GS_SOURCE!\main6\test;^
!THIRD_SOURCE!\jakarta-tomcat-5.0.28-src\jakarta-tomcat-catalina\catalina\src\share;^
!THIRD_SOURCE!\commons-logging-1.0.3-src\src\java;^
!THIRD_SOURCE!\jakarta-log4j-1.2.8\src\java;^
!THIRD_SOURCE!\xerces-1_4_3\src;^
!THIRD_SOURCE!\hibernate-3.1\src;^
!THIRD_SOURCE!\jboss-4.0.1-src\system\src\main;^
!THIRD_SOURCE!\dom4j-1.6.1\src\java;^
!THIRD_SOURCE!\jaxen-1.1-beta-4\src\java\main;^
!THIRD_SOURCE!\commons-collections-3.1\src\java;^
!THIRD_SOURCE!\mysql-connector-java-5.1.6\src;^
!SOURCEPATH!

jdb -sourcepath !SOURCEPATH! %*
