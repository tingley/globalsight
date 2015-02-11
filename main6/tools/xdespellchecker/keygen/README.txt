Installation Key Generator for XDE Spell Checker
================================================

Thu Apr 29 23:37:57 2004 CvdL

The key generator utility is used to create a customer-specific
installation key for the XDE Spell Checker engine.

Usage: 

    set JAVA_HOME=...
    GSrun_MakeKey.bat

A GUI will appear in which you can specify the domain name for which
the key should be created. Then click "generate key".

The key is generated in the file "system.jar" in the current
directory. Copy that file (system.jar) into the GlobalSight
installation directory:

    copy system.jar <gs_home>/applications/xdespellchecker/WEB-INF/lib

Replace the existing system.jar that came pre-installed with GlobalSight.

Start or restart GlobalSight.

-eof-
