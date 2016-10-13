@echo off
rem
rem set up a shell environment to use the ling code
rem
set TOP=c:/gs/ling

rem Required libraries - delete if in system classpath...
set JGL=%TOP%/lib/jgl3.1.0.jar
set JUNIT=%TOP%/lib/junit3.2.jar
set ORO=%TOP%/lib/oroMatcher1.1.0.jar
set REGEXP=%TOP%/lib/jakarta-regexp-1.1.jar
set XALAN=%TOP%/lib/xalan.1.2.do2.jar
set XERCES=%TOP%/lib/xerces.1.2.0.jar

rem Output dir
set DIPLOMAT=%TOP%/diplomat.jar

rem now set a new classpath
set CLASSPATH=%DIPLOMAT;%XERCES%;%XALAN%;%JGL%;%REGEXP%;%ORO%;%JUNIT%;%CLASSPATH%

rem clear unneeded variables
set JGL=
set JUNIT=
set ORO=
set REGEXP=
set XALAN=
set XERCES=
set DIPLOMAT=
set TOP=
