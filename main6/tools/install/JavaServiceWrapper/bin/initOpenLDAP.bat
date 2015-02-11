@echo off

rem echo.
net stop OpenLDAP-slapd

rem echo.
rem echo Clean OpenLDAP database... done
del /F /Q %1\globalsight\data\*

rem echo.
rem echo Copy OpenLDAP configuration files...
xcopy /E /C /I /F /H /R /K /Y "%~dp0..\..\data\openldap\*" %1

rem echo.
rem echo Import OpenLDAP initial data...
cd /D %1
slapadd.exe -l globalsight.ldif
slapadd.exe -l addCustomer.ldif
slapadd.exe -l vendor_management.ldif

rem echo.
net start OpenLDAP-slapd

rem cd %~dp0
