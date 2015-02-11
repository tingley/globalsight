@echo off
rem Converts a bilingual Trados TTX file into a TMX file.
java -classpath %UTILCP% -Xmx256m com.globalsight.everest.tm.util.ttx.TtxToTmx %*
