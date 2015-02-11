@echo off
rem Analyzes a Trados Terminology file in MTF format.
java -classpath %UTILCP% -Xmx256m com.globalsight.terminology.util.MtfAnalyzer %*
