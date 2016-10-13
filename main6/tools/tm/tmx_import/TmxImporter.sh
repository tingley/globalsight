#! /bin/sh

$JAVA_HOME/bin/java -ms128m -mx128m -classpath $CLASSPATH:importer.jar com.globalsight.tools.tmximport.TmxImport $*
