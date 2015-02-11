1. Check out source code from cvs.
   1.1 Create a new folder.
	   Input: mkdir ~/cvs_root		
	   Input: cd ~/cvs_root
   1.2 Login to cvs. 
	   Input: cvs -d :pserver:yorkjin@124.133.49.190:/CVSServer login 
	   You can change the "silver" to another account. After it, you can see a line just like "CVS password:", Then you need to input the password.
   1.3 Check out code.
	   Input: cvs -d :pserver:silver@124.133.49.190:/CVSServer checkout GlobalSightSource
	   Checkout specified branch: cvs -d :pserver:yorkjin@124.133.49.190:/CVSServer checkout -r GlobalSight_7_1_5_0_Released -d GS_7_1_5_3 -P GlobalSightSource
	   Note: If you changed "silver" to another account, you need to change "silver" too.
   1.4 Input: cd "~/cvs_root/GlobalSightSource/main6/tools/client tools/GlobalSight Installer/buildTool_CVS"
	   Input: chmod 777 run.sh
	   Input: chmod 777 build.sh
	   Input: chmod 777 buildGlobalSight.sh
   1.5 Check zip/unzip commands can be used.
       1.5.1 Check the zip command can be used
           Input: zip
           If you can not get some message like "Copyright (c) 1990-2006 Info-ZIP ...", please install the zip by input apt-get install zip. 
       1.5.2 Check the unzip command can be used
           Input: unzip
           If you can not get some message like "UnZip 5.52 of 28 February 2005, by Ubuntu...", please install the unzip by input apt-get install unzip.          

2. Update your code to latest version with CVS.
   Input: cd ~/cvs_root/GlobalSightSource
   Input: cvs -d :pserver:silver@124.133.49.190:/CVSServer update -d
   Note: If you changed "silver" to another account, you need to change "silver" too.

3. Run "buildGlobalSight.sh" to build GlobalSight. This step is making sure the built server is the latest version.

4. Run "build.sh" to build buildTool_CVS/src.

5. Edit "release.properties". (You can get reference to each item comment for how to make the change.)

6. Run "run.sh". Then you can get a zip file named GlobalSight_Installer_%version%.zip which is the patch/release you want.

