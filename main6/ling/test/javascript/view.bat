@echo off
REM                              -*- Mode: Bat -*- 
REM 
REM test/javascript/view.bat
REM 
REM Copyright (C) GlobalSight Corporation 2000 (Cornelis van der Laan)
REM 
REM Responsible     : Cornelis van der Laan
REM Author          : Cornelis van der Laan
REM EMail           : nils@globalsight.com
REM Created On      : Mon Aug 07 15:33:01 2000
REM Last Modified By: Cornelis van der Laan
REM Last Modified On: Wed Aug 16 20:52:46 2000
REM Update Count    : 31
REM Status          : Unknown, use with caution!
REM Description     : 
REM 

call jpython Extract.py %1 %2 %3 > ~~~x
head -1 ~~~x > ~diplomat.xml
cat diplomat.pi >> ~diplomat.xml
tail +2 ~~~x >> ~diplomat.xml
~diplomat.xml
del /f /q ~~~x ~diplomat.xml 2>NUL
