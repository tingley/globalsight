@rem This sets the environment so you can run the web
@rem service tester programs
@echo off
set ALIB=.\lib
set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;%ALIB%\axis.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\axis-ant.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\commons-discovery.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\commons-logging.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\jaxrpc.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\saaj.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\wsdl4j.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\mail.jar
set CLASSPATH=%CLASSPATH%;%ALIB%\activation.jar

@echo Your CLASSPATH is set. You can run the web service
@echo tester programs by executing:
@echo java AmbassadorWebServiceClient [args]
@echo or
@echo java VendorManagementWebServiceClient [args]

