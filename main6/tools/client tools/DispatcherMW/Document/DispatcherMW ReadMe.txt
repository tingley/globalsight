===============================================================================
Dispatcher Middleware Readme

This file provides the following information about DispatcherMW Beta v0.3:

  Running Dispatcher Middleware
  Getting Started
  Server Management
  Notes and Limitations
  
===============================================================================

=============================
Running Dispatcher Middleware
=============================

Dispatcher Middleware binary distribution are bundled with Apache Tomcat 7.0. If
you have already deployed Apache Tomcat instance or other web application server
instance on your system, see 3. Deploy Dispatcher Middleware to existing Apache
Tomcat instance in Server Management section.

(1) Copy and unpack DispatcherMW binary file to your local system.

(2) Download and Install a Java SE Runtime Environment (JRE) or full Java
    Development kit (JDK).

(3) Configure environment variables

    (3.1) Set JRE_HOME or JAVA_HOME (required)

    These variables are used to specify location of a Java Runtime
    Environment or of a Java Development Kit that is used to start Tomcat.

    (3.2) Set CATALINA_HOME (optional)

    The CATALINA_HOME environment variable should be set to the location of the
    root directory of the "binary" distribution of Tomcat.

(4) Start up Tomcat

    (4.1) Tomcat can be started by executing one of the following commands:

    On Windows:
      %CATALINA_HOME%\bin\startup.bat
    or
      %CATALINA_HOME%\bin\catalina.bat start

    On *nix:
      $CATALINA_HOME/bin/startup.sh
    or
      $CATALINA_HOME/bin/catalina.sh start

    (4.2) After startup, DispatcherMW will be available by visiting:

      http://localhost:8888/

(5) Shut down Tomcat

Tomcat can be shut down by executing one of the following commands:

  On Windows:
      %CATALINA_HOME%\bin\shutdown.bat
    or
      %CATALINA_HOME%\bin\catalina.bat stop

  On *nix:
      $CATALINA_HOME/bin/shutdown.sh
    or
      $CATALINA_HOME/bin/catalina.sh stop

Refer to <CATALINA_HOME>\RUNNING.txt for more information about
setting up and running Apache Tomcat. 


=============================
Getting Started
=============================

1. Login DispatcherMW

DispatcherMW uses Tomcat integrated roles and users management mechanism to
control online access.

The default login account is:
User name: superAdmin (it is case sensitive) 
Password: password

To change the default user or add more users, see (1.5.3) Add user to tomcat in
Server Management section.


2. Set MT Profile

Define hubs for available Machine Translation engines. DispatcherMW Beta v0.3
supports following MT engines:
   Asia Online
   IP Translator
   Microsoft Translator
   ProMT
   Safaba


3. Set Accounts

Define account and generate security code. You can connect to DispatcherMW with
account and security code you received to translate text or
develop you applications.


4. Set Languages

Define the source and target language pairs for certain account with specified
MT engine hub. Note: Not all source and target language pairs are supported by
some MT engine.
 
 
5. Manage Files

You can download or remove source and target files for translation in File
Management page.

Select an account name from Account drop down list to view all job files of
specified account. You can also visit job files associated with certain account
by Manage Files link on Accounts page.

Check one or several jobs to remove or download them. You can download the
individual file by click on the file name.


6. Test translation

DispatcherMW provides two methods to test MT engines, by plain text and by
XLIFF file.

    (5.1) Translate plain text:

      (5.1.1) Select Account, Source and Target locale via drop down list.
      (5.1.2) Type or paste plain text in Source Text box.
      (5.1.3) Click on Translate button.
       And after a while, the translated text is available in Target Text box.

    (5.2) Translate XLIFF file:

      (5.2.1) Select Account via drop down list.
      (5.2.2) Choose a local XLIFF file via File Upload dialog.
      (5.2.3) Click on Translate XLIFF File button.
       And after a while, the browser will prompts to download the translated
       text.

Note: The selected account should have defined corresponding source and
target language as that of the XLIFF file.
 
 
=============================
Server Management
=============================

1. Log and track translation text for certain MT engine

By default, DispatcherMW does not log translation details for each MT engine.
You may need view detailed information about text sent to and received from MT
engine in some cases.

    (1.1) Edit <CATALINA_HOME>/webapps/dispatcherMW/WEB-INF/classes/properties/
       mt.config.properties
 
    (1.2) Set <ENGINE_NAME>.log.detailed.info = true


2. Configuration backup and files cleanup

When Tomcat is started up, a directory named DispatcherMW is generated
automatically under Tomcat instance directory.

 <data> stores all configuration files for Machine translation engines, account
 settings and translation languages settings.

To upgrade DispatcherMW and keep original settings, you can copy and paste files
in this directory to the new DispatcherMW instance. 

 <fileStorage> stores source and target files of translation, structured by
 account names and job ids.

DispatcherMW Beta v0.3 release does not clean up files uploaded by clients and
translated XLIFF files. You can remove them manually from backend. 


3. Deploy Dispatcher Middleware to existing Apache Tomcat instance

  (3.1) Unpack DispatcherMW binary distribution to your local system
 
  (3.2) Shut down Tomcat

  Refer to (5) Shut down Tomcat in Running Dispatcher Middleware section
 
  (3.3) Copy dispatcherMW.war into webapps directory under your Tomcat instance
 
  (3.4) Enable UTF-8 encoding support
 
  Edit conf/server.xml and find the line where the HTTP Connector is defined. It
  will look something like this, possibly with more parameters:
 
  <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />

  Add a URIEncoding="UTF-8"property to the connector:
  <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               redirectPort="8443" />
 
  (3.5) Add DispatcherMW administrative role and user
 
  (3.5.1) Edit conf/tomcat-users.xml and find the line where tomcat user is
  defined. It will look something like this:
 
   <tomcat-users>
   </tomcat-users>
 
  (3.5.2) Add role dispatcherMWRole to tomcat:
  
   <role rolename="dispatcherMWRole"/>
   
   Note: DO NOT change the role name
   
  (3.5.3) Add user to tomcat:
   
   <user username="superAdmin" password="password" roles="dispatcherMWRole"/>
   
   Note: you can change user name and password as you want, but DO NOT change
   value of roles.
  
  (3.6) Start up Tomcat

  Refer to (4) Start up Tomcat in Running Dispatcher Middleware section
 
  Note: if you deploy DispatcherMW to existing Tomcat instance, it may not be
  the default web application. Thus you need visit following URL:
  http://localhost:<port>/dispatcherMW/
 
 
=============================
Notes and Limitations
=============================

1. System Requirements

Operating System: 
  Windows 7/Windows Server 2008 R2 or later Windows system; *nix system
  
Memory: 
  Minimum of 1GB when no other web applications are running.
  
Free Hard Disk Space:
  2GB of free disk space for application files and folders. You must allocate
  enough free disk space to store the source and target files for translation.


2. Supported Browsers:

All features are fully supported by following browser:
  Microsoft Internet Explorer 10 and later supported version;
  Mozilla Firefox (version 27.0 and above); 
  Google Chrome (version 34.0 and above);
  
Translate XLIFF File may not be well supported by other browsers.

Downloaded XLIFF file from Safari will append additional .xml extension. You can
rename the file by removing .xml in file name. This is a known limitation of
Safari, more information is available at:
http://support.apple.com/kb/ta24293


3. File encoding

DispatcherMW Beta v0.3 supports XLIFF files in following encoding:
  ISO-8859-1 (ANSI)
  UTF-8
  UTF-16LE
  UTF-16BE


4. Limitations
   (1) Complicate tags in segments may not well supported.
   
   (2) Translate large XLIFF file via online Test page may take long time, and
   in some cases, the browser may look like not responding.
