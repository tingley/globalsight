@echo off
rem $Id: GSrun_MakeKey.bat,v 1.1 2009/04/14 15:42:52 yorkjin Exp $

rem Global Sight's version of the XDE registration program
rem TODO Be sure java home is set correctly!

set CP=xdeGSSpellCheckerRegistration-20040428.jar
set CP=%CP%;AbsoluteLayout.jar
set CP=%CP%;%JAVA_HOME%\jre\lib\rt.jar

echo Using classpath %CP%

%JAVA_HOME%\bin\java -cp %CP% com.globalSight.GlobalSightSpellCheckerRegistrationKey

