@echo off
rem Utility to fix corrupt termbase defintions.
java -classpath %UTILCP% com.globalsight.terminology.util.FixTbDefinitions %*
