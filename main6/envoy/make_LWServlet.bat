@echo off

echo Building LWServlet.jar and LWServlet.class...

set SRC=.\src\java
set CLASSES=..\LWServlet_build
set javac_classpath=.\lib\servlet.jar;%CLASSES%

mkdir %CLASSES%

javac -Xdepend -d %CLASSES% -classpath %javac_classpath% -sourcepath %SRC% %SRC%\com\globalsight\everest\servlet\LWServlet.java

cd %CLASSES%
jar cf LWServlet.jar com
cd ..\envoy_workset

echo Done.
