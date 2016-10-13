This tells how to run all junit test cases in one run and generate report.

#1. Update source codes and run "ant clean dist".

    CD to "main6\tools\build" folder in command line to run "ant clean dist".

#2. In "main6\tools\build" folder in command line, run "ant write-run-classpath".

    This will generate "run.classpath.bat|run.classpath.sh" files which are from "build.properties" file "classpath" and "junit.classpath.properties".

#3. Copy "user.build.properties.sample" file in same folder and rename it to "user.build.properties", change the "gs.home" to your real server path.

#4. Run "ant test" to run all junit test cases.

    After running is done, the result will be in "main6\tools\build\testreports" folder.
