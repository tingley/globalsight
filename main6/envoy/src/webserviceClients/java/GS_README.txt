Both java clients use the Apache Axis generated stubs for the web
service which are based on the WSDL from a running server at the time
of stub generation. 

If you make web service additions and update the clients, you first 
need to have Ambassador running your new version of the web service.
Then checkout all the client stubs classes:
com\globalsight\webservices\WebServiceException.java
and all the files in:
\com\globalsight\www\webservices

Then, to regenerate the stubs classes (from webserviceClients\java), execute this:

java org.apache.axis.wsdl.WSDL2Java http://<server>:<port>/globalsight/services/AmbassadorWebService?wsdl
and
java org.apache.axis.wsdl.WSDL2Java http://<server>:<port>/globalsight/services/VendorManagementWebService?wsdl

Then rerun the webservice client build

