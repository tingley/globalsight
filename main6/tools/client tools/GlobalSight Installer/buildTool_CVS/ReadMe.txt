1. Update your code to latest version with CVS.
2. Build GlobalSight with "buildGlobalSight.bat".
   2.1 Edit "buildGlobalSight.bat". Change "C:\cvsroot\GlobalSightSource" to your code base path. 
   2.2 Run "buildGlobalSight.bat". (This step is making sure the built server is the latest version.)
3. Copy folder "buildTool_CVS" from "main6/tools/client tools/GlobalSight Installer" of your CVS code base to a temp folder to do the installer build. (For example, D:\temp. This is because doing the build in the original place may fail of windows path too deep error.)
4. Run "build.bat" to build buildTool_CVS/src.
5. Edit "release.properties". (You can get reference to each item comment for how to make the change.)
6. Run "run.bat". Then you can get a zip file named GlobalSight_Installer_%version%.zip which is the patch/release you want.
  
   