@echo off
setlocal
setlocal enabledelayedexpansion

rem A wrapper that sets up a stand-alone environment for GS code.

set DIR=%~dp0
set GS_JAVA=javac

!DIR!\gsjava %*
