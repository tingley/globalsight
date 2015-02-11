Office Converters for Office XP
-------------------------------

All converters and their installers are built in Visual Studio 2003.
The main solution is OfficeConverters.sln, which includes all other
projects.

After you make code changes to any converter, rebuild them all in
Visual Studio 2003.
 
Then check-out the appropriate zip file from toolsVOB/converters/build/dist.
The zip files are 

    conv_excelxp.zip
    conv_powerpointxp.zip
    conv_wordxp.zip

Then run the GlobalSight build with the appropriate target name. 
For example: ant ExcelXPConverter. To rebuild all zip files, run: 
ant AllConverters.

You should see the note "Building zip ..." to indicate that the package 
was actually rebuilt.

There is a target for each converter that will create the .zip file
that the customer will install to run the converter. Those packages are
not zipped up during the normal build.

    PdfConverter,
    WordConverter,
    ExcelConverter,
    PowerPointConverter,
    WordXPConverter,
    ExcelXPConverter,
    PowerPointXPConverter

After you create the .zip file for the converter you modified, check the .zip
file back into ClearCase in this location toolsVOB/converters/build/dist/.

Then please notify Cheryl that the Converter has been updated.
The converters are shipped separately of the main GlobalSight build.

