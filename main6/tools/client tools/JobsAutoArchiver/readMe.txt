How to create build:
1. Update CVS to get latest source codes in "main6\tools\client tools\JobsAutoArchiver" folder.
2. Compile the source codes in eclipse.
3. CD to "bin" path, run "jar -cvf JobsAutoArchiver.jar *.*" command to create "JobsAutoArchiver.jar" file.
4. Edit the "MANIFEST.MF" file in "JobsAutoArchiver.jar" to specify "Main-class" and "Class-Path".
5. Create a zip build.

To run, unzip the build to a folder(it had better have no whitespace in the folder path), click "run.bat" to run.

Configuration file is "JobsAutoArchiver.cfg.xml", and log file is in "logs/JobsAutoArchiver.log".
