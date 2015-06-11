@echo off

rem echo Copy win32 service required files...
xcopy /E /C /I /F /H /R /K /Y "%~dp0..\service\win32\bin\*" "%~dp0..\..\server\bin"

rem echo.
cd %~dp0..\..\server\bin
rem echo Install win32 GlobalSight service...
service.bat install
