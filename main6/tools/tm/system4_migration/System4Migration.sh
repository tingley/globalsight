#! /bin/sh

$JAVA_HOME/bin/java -ms256m -mx256m -classpath $CLASSPATH:migration.jar com.globalsight.migration.System4Migrate config.properties
