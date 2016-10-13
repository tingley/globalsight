@echo off
set DESKTOPICON_HOME=.
set DESKTOPICON_JAR=desktopicon.jar

if "%OS%"=="Windows_NT" goto setHome
else goto setEnv

:setHome
set DESKTOPICON_HOME=%~dp0
goto setEnv

:setEnv
set RUBYLIB=%RUBYLIB%;%DESKTOPICON_HOME%resource\ruby\Watir
set RUBYLIB=%RUBYLIB%;%DESKTOPICON_HOME%resource\ruby\firewatir
set RUBYLIB=%RUBYLIB%;%DESKTOPICON_HOME%resource\ruby

goto runDesktopIcon

:runDesktopIcon
@echo off
start javaw -Xms32m -Xmx256m -jar "%DESKTOPICON_HOME%\%DESKTOPICON_JAR%" %1 %2 %3 %4 %5