@echo off

rem echo.
cd %~dp0..\..\server\bin\service
rem echo Remove old GlobalSight service using windows command...
sc delete "GLOBALSIGHT"
rem echo Stop GlobalSight service if it is running...
net stop "GLOBALSIGHT"
rem echo Uninstall GlobalSight service using WildFly command...
service.bat uninstall