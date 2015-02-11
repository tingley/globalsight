@echo on

if exist %windir%\Microsoft.NET\Framework\v2.0.50727\InstallUtil.exe goto setHome2
if exist %windir%\Microsoft.NET\Framework\v1.1.4322\InstallUtil.exe goto setHome1

:echoErrors
echo "Can not find .NET Framework Home" > log.txt
goto end

:setHome1
set INSTALL_UTIL_HOME=%windir%\Microsoft.NET\Framework\v1.1.4322
goto uninstall


:setHome2
set INSTALL_UTIL_HOME=%windir%\Microsoft.NET\Framework\v2.0.50727
goto uninstall


:uninstall
%INSTALL_UTIL_HOME%\InstallUtil /u PowerPoint2003Converter.exe

:end

