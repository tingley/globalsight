README for running the java web service clients for invoking
methods on the Ambassador Web Service API

These web service tester programs are purely for educational purposes
to show how you could develop a program using Java to consume
Ambassador web services.

This package should be completely self contained, and only requires
the use of java (JDK 1.4x) to execute the programs. Jakarta Ant is required
only if you want to run the build script to compile the source code, although
that could be done with manual calls to javac.

To run a (Java) web service tester:

1) run the appropriate setWSEnv.bat or setWSEnv.sh script. This script
just sets your CLASSPATH to include the Apache Axis, and Sun Activation,
and Mail jars.

2) run the main Ambassador web service client as:
java AmbassadorWebServiceClient

3) or run the main Vendor Management web service client as:
java VendorManagementWebServiceClient

Both programs should spit out usage information if called without
any arguments.

