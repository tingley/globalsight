@echo off
echo Hello World
echo Building Console.jar...

set SRC=.\src\java
set CLASSES=..\Console_build
set javac_classpath=%CLASSES%

mkdir %CLASSES%

javac -Xdepend -d %CLASSES% -classpath %javac_classpath% -sourcepath %SRC% %SRC%\com\globalsight\ling\editor\tools\Console.java

cd %CLASSES%
jar cf Console.jar com
cd ..\envoy_workset

echo Done.
