#!/bin/sh 

# Shell script to start GAlign.

%{JAVA_HOME}/bin/java -Xmx512m -jar galign.jar $*
