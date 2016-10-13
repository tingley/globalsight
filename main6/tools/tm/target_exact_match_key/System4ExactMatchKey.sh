#! /bin/sh

$JAVA_HOME/bin/java -ms256m -mx256m -classpath $CLASSPATH:ExactMatchKey.jar com.globalsight.tools.exactmatchkey.ExactMatchKeyMigration $*
