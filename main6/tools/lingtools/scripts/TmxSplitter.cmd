@echo off
rem Splits a TMX file into smaller chunks, preserving the original header.
java -classpath %UTILCP% -Xmx512m com.globalsight.everest.tm.util.TmxSplitter %*
