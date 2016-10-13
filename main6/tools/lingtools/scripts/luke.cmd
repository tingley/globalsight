@echo off
rem Utility to inspect Lucene indexes.
java -classpath %UTILCP% -Xmx256m org.getopt.luke.Luke
