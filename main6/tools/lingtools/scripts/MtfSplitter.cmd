@echo off
rem Splits a Trados Terminology file in MTF format into smaller chunks.
java -classpath %UTILCP% -Xmx512m com.globalsight.terminology.util.MtfSplitter %*
