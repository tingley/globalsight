@echo off

echo Building GsEditor.jar...

set SRC=.\src\java
set CLASSES=..\GsEditor_build
set javac_classpath=%CLASSES%

mkdir %CLASSES%

javac -Xdepend -d %CLASSES% -classpath %javac_classpath% -sourcepath %SRC% %SRC%\com\globalsight\ling\editor\GsEditor.java

cd %CLASSES%
jar cf GsEditor.jar com
cd ..\envoy_workset

echo Done.
