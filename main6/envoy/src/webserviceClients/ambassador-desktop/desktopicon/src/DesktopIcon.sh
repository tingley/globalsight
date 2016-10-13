RUBYLIB=.:./resource/ruby/firewatir:./resource/ruby export RUBYLIB

echo Running GlobalSight Desktop Icon
echo " "
echo RUBYLIB=$RUBYLIB

java -classpath .:lib/log4j.jar:lib/activation.jar:lib/axis-ant.jar:lib/axis.jar:lib/commons-discovery.jar:lib/commons-logging.jar:lib/dom4j.jar:lib/jaxrpc.jar:lib/junit.jar:lib/log4j-1.2.8.jar:lib/log4j.jar:lib/mail.jar:lib/mysql-connector-java-5.1.6-bin.jar:lib/saaj.jar:lib/wsdl4j.jar:lib/commons-codec-1.4.jar:lib/commons-httpclient-3.1.jar:lib/google-collect-1.0.jar:lib/json-20080701.jar:lib/selenium-java-2.0a4.jar com/globalsight/action/Main