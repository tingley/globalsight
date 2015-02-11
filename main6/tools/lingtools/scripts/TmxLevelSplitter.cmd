@echo off
rem Splits a TMX file into 2 files that contain level 1 and level 2 segments.
java -classpath %UTILCP% -Xmx512m com.globalsight.everest.tm.util.TmxLevelSplitter %*
