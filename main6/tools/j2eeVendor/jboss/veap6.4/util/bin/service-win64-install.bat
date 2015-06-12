@echo off

rem echo Copy win64 service required files...
xcopy /E /C /I /F /H /R /K /Y "%~dp0..\service\win64\bin\*" "%~dp0..\..\server\bin"

rem echo.
cd %~dp0..\..\server\bin
rem echo Install win64 GlobalSight service...
service.bat install