1. Check out source code from GIT.
   1.1 Create a new folder.
	   Input: mkdir ~/GIT_root		
	   Input: cd ~/GIT_root
   1.2 Check out code.
	   Input: git clone ssh://git@125.35.10.59:2222/globalsight
   1.3 Input: cd "~/GIT_root/globalsight/main6/tools/client tools/GlobalSight Installer/buildTool_GIT"
	   Input: chmod 777 run.sh
	   Input: chmod 777 build.sh
	   Input: chmod 777 buildGlobalSight.sh
   1.4 Check zip/unzip commands can be used.
       1.4.1 Check the zip command can be used
           Input: zip
           If you can not get some message like "Copyright (c) 1990-2006 Info-ZIP ...", please install the zip by input apt-get install zip. 
       1.4.2 Check the unzip command can be used
           Input: unzip
           If you can not get some message like "UnZip 5.52 of 28 February 2005, by Ubuntu...", please install the unzip by input apt-get install unzip.          

2. Update your code to latest version with GIT.
   Input: cd ~/GIT_root/globalsight
   Input: git pull

3. Run "buildGlobalSight.sh" to build GlobalSight. This step is making sure the built server is the latest version.

4. Run "build.sh" to build buildTool_GIT/src.

5. Edit "release.properties". (You can get reference to each item comment for how to make the change.)

6. Run "run.sh". Then you can get a zip file named GlobalSight_Installer_%version%.zip which is the patch/release you want.

