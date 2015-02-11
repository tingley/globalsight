Section I: Import from TeamSite Workflow
########################################################
Instructions for setting up 
Import from TeamSite Workflow using External Task
########################################################

Note: No need to start the autoimport daemon or service to use this
feature. This is totally independent of autoimport process used 
for automatically importing files from watched WORKARES. Go to 
"Instructions for AutoImport from Watched TeamSite WorkArea" below
if you intend to do so.

1. Before running any script make sure
   that you have jdk1.4.2 installed on
   your machine.

2. Make sure that you have copied all the 
   properties files created
   by the install process in the 
   dist/data/teamsite/tsautoimport/properties
   to
   the globalsight/tsautoimport/properties directory
   of this machine of Teamsite Server.
   Please verify the following before proceeding: 
   a. Teamsite server is running
   b. Ambassador server is running
   c. autoTeamSiteImport.map file contains a workarea path 
   d. just make sure all the property files (in d:\globalsight\tsAutoImport\properties)
      contain valid data

3. If Windows skip this step 
   On unix Go to globalsight/tsautoimport/bin directory 
   and make all the files executable.
   # cd bin
   # chmod +x *

4. Set the environment variable JAVA_HOME
   to point to proper location of your jdk.


To enable import from TeamSite Workflow do the following:
A.Create an external task in teamsite workflow.
B.Create a job using the above workflow.
C.Add files to the job(task) if not already added.
D.External task will be invoked and it'll call the import cgis
E.After the job gets completed in Ambassador, the files are
 exported back to TeamSite and external task will complete itself
 and the workflow will advance itself to the next available task.

Example External task Attributes are
Name: callAmbassador
Description: This script helps to import files to Ambassador
Owner: domain/master_user
lock: no
retry: no
start: no
AreaVPath:\store2003\main\SourceEnglish\WORKAREA\HTML
Command:e:/progra~1/interw~1/teamsite/iw-perl/bin/iwperl.exe E:/globalsight/tsautoimport/bin/GlobalSightExternalTask.ipl

Note:
I. "Command" is the most important attribute. the GlobalSightExternalTask.ipl script
needs to be copied in the tsautoimport/bin if not already present.
GlobalSightExternalTask.ipl script is generated when in Ambassador 
"TeamSite Server Profiles" -> "Create CGI" is clicked after selecting
the teamsite server.
This file is created in dist/teamsite/server.domain.com/tsautoimport/bin 
on the Ambassador server.
II. "AreaVPath" must match the entry in 
<autoimport-home>/tsautoimport/properties/autoTeamSiteImport.map file.
III. Set the Debug mode off for the workflow.

------------------------------------------------------------
Section II: Instructions for AutoImport from 
  Watched TeamSite WorkArea

#############################################################
The following instructions are for solaris 
install only. For instruction for NT installation
go to NT Installation section below. 
############################################################
1. Before running any script make sure
   that you have jdk1.4.2 installed on
   your machine.

2. Make sure that you have copied all the 
   properties files created
   by the install process in the 
   dist/data/teamsite/tsautoimport/properties
   to
   the globalsight/tsautoimport/properties directory
   of this machine of Teamsite Server.
   Please verify the following before proceeding: 
   a. Teamsite server is running
   b. Ambassador server is running
   c. autoTeamSiteImport.map file contains a directory to watch
   d. just make sure all the property files (in d:\globalsight\tsAutoImport\properties)
      contain valid data

3. Go to globalsight/tsautoimport/bin directory 
   and make all the files executable.
   # cd bin
   # chmod +x *

4. Set the environment variable JAVA_HOME
   to point to proper location of your jdk.
   Please run tsAutoImport script to start
   the Teamsite Automatic Import Daemon.
   Go to the command prompt
   
   # ./tsAutoImport start
   Teamsite Automatic import Daemon started.
   To get help type
   # ./tsAutoImport help

5. Go to globalsight/tsautoimport/logs/tsAutoImport.log 
   file to see if there are any errors.
____________________________________________________________________________________________

Installation instructions for windows-NT/2000.
____________________________________________________________________________________________
1. Before running any script make sure
   that you have jdk1.4.2 installed on
   your machine.

2. Make sure that you have copied all the 
   properties files created
   by the install process in the 
   dist/data/teamsite/tsautoimport/properties
   to
   the globalsight/tsautoimport/properties directory
   of this machine of Teamsite Server.

3. create the NT service by issuing the following
   command. (Assuming the instsrv and srvany.exe are
   located in d:\globalsight\tsAutoImport\bin)

   d:\globalsight\tsAutoImport\bin\INSTSRV "GlobalSight TS AutoImport" d:\globalsight\tsAutoImport\bin\srvany.exe

4. run the regedt32 program and create the 
   required keys and values.
   Go to
   HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\GlobalSight TS AutoImport
   Then from main menu, Edit -> Add Key...
   Type Key Name = "Parameters"
   Leave the class blank.

   Add the following parameters using Edit -> Add Value:
   AppDirectory:REG_SZ:d:\globalsight\tsautoimport\lib
   Application:REG_SZ:%JAVA_HOME%\bin\java
   AppParameters:REG_SZ:-cp  ..\properties;.\;.\tsAutoImport.jar;.\log4j.jar  -Xrs  com.globalsight.cxe.adapter.teamsite.autoimport.AutomaticImportService

5. Now you have set the service correctly. Before starting the service,
   Please verify the following: 
   a. Teamsite server is running
   b. Ambassador server is running
   c. autoTeamSiteImport.map file contains a directory to watch
   d. just make sure all the property files (in d:\globalsight\tsAutoImport\properties)
      contain valid data

5. Go to Control Panel -> services 
   Scroll down to the service and hit start.

6. Go to d:/globalsight/tsautoimport/logs/tsAutoImport.log 
   file to see if there are any errors.
____________________________________________________________________________________________
Section III 
TeamSite Workflow Integration

1.	Install Teamsite WorkflowBuilder Client and Server on the TeamSite machine.
2.	Install jdk1.4.2  on the TeamSite machine.
3.	Set the environment variable JAVA_HOME to point to the above.
4.	Please verify that Teamsite server and Ambassador server are up and running.
5.	From Ambassador Create TeamSite Server Configuration for the required server.
6.	Select the required server and hit "Create CGI" to create the essential scripts.
7.	Make sure to have basic teamsite cgi/ipl files copied to teamsite server.
8.	Pick up the Ambassador_xxx_TeamsiteAutoImport.zip file and extract it on the TeamSite server in a directory. (e.g. c:/globalsight)
9.	Copy all the properties files created by the install process in the 
        dist/data/teamsite/tsautoimport/properties to the    
        <auto-import-home>/tsautoimport/properties directory on the teamsite server.
        Copy dist/data/teamsite/tsserver.domain.com/tsAutoImport/properties/tsStoreName/teamsiteParams.properties to 
        <auto-import-home>/tsautoimport/properties directory on the teamsite server.
        Copy GlobalSightExternalTask.ipl file from dist/teamsite/server.domain.com/tsautoimport/bin to 
                <auto-import-home>/tsautoimport/bin directory on the teamsite server.
        Edit the properties/autoFileImport.map file to contain something like
        /globalsight/main/GS_English/WORKAREA/html |  job name here | file_profile
        Note: Always use forward slashes ("/") while specifying the above path.
10.	Also make sure other properties file contain valid data.
11.	Copy the AutomaticImporter.class file from Ambassador server machine, which is included in the Ambassador_5.2.1_ER25.zip to the teamsite server. Or you can just extract the AutomaticImporter.class file from the Ambassador_5.2.1_ER25.zip. Add it to 
        <auto-import-home>/tsautoimport/lib/tsAutoImport.jar.
        For this:
        a.	Go to <auto-import-home>/tsautoimport/lib/
        b.	Create the directory structure com/globalsight/cxe/adapter/teamsite/autoimport/ and put the AutomaticImporter.class file under this directory.
        c.	From command prompt go to <auto-import-home>/tsautoimport/lib/ and give the following command:
        jar uvf tsAutoImport.jar com/globalsight/cxe/adapter/teamsite/autoimport/ AutomaticImporter.class
12.	Note : There is no need to create the NT service for autoimport from TeamSite Workflows.
13.	Create a Teamsite Workflow template. To do this
        a.	Start Workflow Builder Client.
        b.	Click on New Workflow.
        c.	Add User Task1 -> External Task -> User Task2 -> End
        i.	For User Task1 there are 3 tabs on the left
        1.	Workfl.. Tab
        a.	Here, Name: anyName (e.g. workflow1)
        b.	DebugMode: No
        c.	Preselect: Yes
        d.	Variables: [Optional]
        e.	Description: [Optional]
        f.	owner: domain/username {e.g. globalsj1/pgautam}
        2.	Attrib... Tab
        a.	Name: anyName (e.g. Task1)
        b.	Description: [Optional]
        c.	owner: domain/username {e.g. globalsj1/pgautam}
        d.	Lock: No
        e.	ReadOnly: No
        f.	Start: Yes
        g.	AreaVPath:starting with the storename upto the WORKAREA name
        h.	(e.g. /store2003/main/gs_english/WORKAREA/html)
        i.	Files: $iw_selected_files []; 
        3.	Variab... Tab
        a.	Nothing here...

        ii.	For the External Task
        1.	Workfl.. Tab
        a.	Here, Name: anyName (e.g. workflow1)
        b.	DebugMode: No
        c.	Preselect: Yes
        d.	Variables: [Optional]
        e.	Description: [Optional]
        f.	owner: domain/username {e.g. globalsj1/pgautam}
        2.	Attrib... Tab
        a.	Name: anyName (e.g. callAmbassador)
        b.	Description: [Optional]
        c.	owner: domain/username {e.g. globalsj1/pgautam}
        d.	Lock: No
        e.	ReadOnly: No
        f.	Start: No
        g.	Command:<iw-home>/iw-perl/bin/iwperl.exe <autoimport-home>/tsautoimport/bin/GlobalSightExternalTask.ipl
        h.	e.g. e:/progra~1/interw~1/teamsite/iw-perl/bin/iwperl.exe E:/globalsight/tsautoimport/bin/GlobalSightExternalTask.ipl

        i.	AreaVPath:starting with the storename upto the WORKAREA name
        j.	(e.g. /store2003/main/gs_english/WORKAREA/html)
        k.	Files: [Blank]
        l.	Variables: [Blank]
        3.	Variab... Tab
        a.	Nothing here...

        iii.	For User Task2
        1.	Workfl.. Tab
        a.	Here, Name: anyName (e.g. workflow1)
        b.	DebugMode: No
        c.	Preselect: Yes
        d.	Variables: [Optional]
        e.	Description: [Optional]
        f.	owner: domain/username {e.g. globalsj1/pgautam}
        2.	Attrib... Tab
        a.	Name: anyName (e.g. Task2)
        b.	Description: [Optional]
        c.	owner: domain/username {e.g. globalsj1/pgautam}
        d.	Lock: No
        e.	ReadOnly: No
        f.	Start: No
        g.	AreaVPath:starting with the storename upto the WORKAREA name
        h.	(e.g. /store2003/main/gs_english/WORKAREA/html)
        i.	Files: $iw_selected_files []; (Should appear automatically) 
        3.	Variab... Tab
        a.	Nothing here...

        iv.	For End Task
        1.	Workfl.. Tab
        a.	Name: anyName (e.g. workflow1)
        b.	DebugMode: No
        c.	Preselect: Yes
        d.	Variables: [Optional]
        e.	Description: [Optional]
        f.	owner: domain/username {e.g. globalsj1/pgautam}
        2.	Attrib... Tab
        a.	Name: EndTask
        b.	Description: [Optional]
        3.	Variab... Tab
        a.	Nothing here...

14.	Click on Save.
15.	Note: Clicking on save may generate some errors if the above is not configured properly. Correct those errors and you'll be allowed to save.
        a.	Select File->Send to Server
        b.	A workflow constraints dialog will open up.
        c.	Wft_type select "all". (If you have edited this template and sending it to the server for second time don't forget to select "overwrite existing file")
        d.	On the users tab select the required user.
        e.	You may leave the Branch tab as it's.
16.	So you have successfully created a workflow template.
17.	Logon to TeamSite instance.
18.	Go to the specific workarea
19.	Select a file or files to be added to a job.
20.	From the file menu, select "New Job"
21.	A list of workflow templates will be shown. Select the one you just created.
22.	Note: If you don't find the workflow template you just create here, then you'll need to look at the "available_templates.cfg file which is present at <ts-home>/local/config/wft.
23.	Find your workflow template here by name and change 'include="yes"' for your role.
24.	Create a job.
25.	Start the Task by selecting "Task Options" combo's "Start Task"
26.	Add more files to the job if required.
27.	Do some changes to the file if required in the first stage by editing the files.
28.	When the first task is finished, select goto CallAmbassador from the dropdown menu.
29.	Now the files will be sent to Ambassdor for translation.
30.	Logon to Ambassador and translate/localize the files.
31.	Complete the job in Ambassador
32.	When the job is Localized it's automatically exported back to teamsite
33.	As all files reach Teamsite server, the current external task is finished and the next task in the TeamSite workflow is triggered.
____________________________________________________________________________________________
