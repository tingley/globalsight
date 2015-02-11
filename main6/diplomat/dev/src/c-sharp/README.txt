Each converter is built separately in Visual Studio.
After you make code changes, build it in Visual Studio.
 
Then check-out the appropriate zip file from toolsVOB/converters/build/dist
and then delete it.

Then run the Ambassador build with the appropriate target name. For example:
ant ExcelConverter

You should see the note "Building zip ..." to indicate that the package was
actually rebuilt.

There is a target for each converter. That will create the .zip file that the
client will install to run the converter. Those packages are not zipped up during
the normal build.

After you create the .zip file for the converter you modified, check the .zip
file back into ClearCase in this location toolsVOB/converters/build/dist/

Then please notify Cheryl that the Converter has been updated.
The converters are shipped separately of the main Ambassador build.

