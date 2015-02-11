@echo off
rem Cleans up a bilingual Trados TTX file by writing out the source or target.
java -classpath %UTILCP% -Xmx256m com.globalsight.everest.tm.util.ttx.TtxClean %*
