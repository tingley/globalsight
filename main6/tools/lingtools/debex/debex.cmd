@echo off
REM Batch file to start Debex.

REM You need to have the Ambassador classpath setup already
REM by running setenv.cmd in d:/ambassador.

java -cp ".;debex.jar;AbsoluteLayout.jar;%CLASSPATH%" debex.ui.control.Start $*
