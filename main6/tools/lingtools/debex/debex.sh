#!/bin/sh Shell script to start Debex.

# You need to have the GlobalSight classpath setup already
# by running ". setenv.sh" in /globalsight.

java -cp ".:debex.jar:AbsoluteLayout.jar:$CLASSPATH" debex.ui.control.Start $*
