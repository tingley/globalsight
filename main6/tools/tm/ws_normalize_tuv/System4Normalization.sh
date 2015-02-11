#! /bin/sh

$JAVA_HOME/bin/java -ms256m -mx256m -classpath $CLASSPATH:normalization.jar com.globalsight.tools.normalizetuv.WsNormalizeMigrate $*
