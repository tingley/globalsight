@echo off
rem Analyzes a TMX file, counting entries and printing out languages.
java -classpath %UTILCP% -Xmx256m com.globalsight.everest.tm.util.TmxAnalyzer %*
