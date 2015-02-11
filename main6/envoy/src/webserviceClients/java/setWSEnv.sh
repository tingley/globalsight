# This sets the environment so you can run the web
# service tester programs
ALIB=./lib
CLASSPATH=.
CLASSPATH=$CLASSPATH:$ALIB/axis.jar
CLASSPATH=$CLASSPATH:$ALIB/axis-ant.jar
CLASSPATH=$CLASSPATH:$ALIB/commons-discovery.jar
CLASSPATH=$CLASSPATH:$ALIB/commons-logging.jar
CLASSPATH=$CLASSPATH:$ALIB/jaxrpc.jar
CLASSPATH=$CLASSPATH:$ALIB/log4j-1.2.8.jar
CLASSPATH=$CLASSPATH:$ALIB/saaj.jar
CLASSPATH=$CLASSPATH:$ALIB/wsdl4j.jar
CLASSPATH=$CLASSPATH:$ALIB/mail.jar
CLASSPATH=$CLASSPATH:$ALIB/activation.jar

echo "Your CLASSPATH is set. You can run the web service"
echo "tester programs by executing:"
echo "java AmbassadorWebServiceClient [args]"
echo "or"
echo "java VendorManagementWebServiceClient [args]"

