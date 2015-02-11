@REM Set Library
set classpath=.;./lib/dispatcherMWClient.jar;./lib/httpcore-4.3.2.jar;./lib/httpclient-4.3.2.jar;./lib/httpmime-4.3.2.jar;./lib/commons-logging-1.1.1.jar;
@REM Run Java Code
@REM java com.welocalize.dispatcherMW.client.Main translate http://localhost:8888/dispatcherMW Welocalize test.xlf
java com.welocalize.dispatcherMW.client.Main config.properties
@REM Pause Console
Pause
