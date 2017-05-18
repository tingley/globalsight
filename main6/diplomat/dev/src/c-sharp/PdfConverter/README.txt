NOTE: The zip file actually used to install the service is created by our build process.
In order to build, you must have OmniPage12 installed so the appropriate COM libraries
can be located.

The PdfConversionService is a separate Windows Service that monitors
a pdf watch directory. The service name is "GlobalSight PDF Converter".

It expects a Registry key called HKLM\Software\GlobalSight.
Under that key, there should be a String key (REG_SZ) called PdfConvDir
with the value of a real directory.

The service will expect there to be a "pdf" sub-directory under the PdfConvDir
directory. And under this directory, there should be locale specific directories
for each source locale that has PDF files (en_US, fr_FR, etc.)

Any errors trying to access the registry or get started will be logged to the EventLog.
Any other errors in normal processing will be logged to a log file called pdfConverter.log
under the <PdfConvDir>\pdf location.

There is a separate zip [GlobalSightPdfConverter.zip] file that contains
the binary files necessary to run the service. The files should be extracted
to D:\globalsight. It will create a sub-directory called PdfConverter\bin
which contains the .exe and .dll files needed to run the service, as well
as the REG.exe program needed to create the registry key.

Before extracting the files, make sure that the DotNetRedistributable package
has been installed on the Windows2000 machine.
Then extract the files from the zip file, and bring up an NT shell window and CD
to the PdfConverter\bin directory.
Then install the service by executing:
# C:\WINNT\Microsoft.NET\framework\v1.0.3705\InstallUtil.exe PdfConversionService.exe

To create this registry key execute the following command (for example).
The last value is the actual directory to use for conversion. It should not
end in a \, or include the pdf-subdirectory:
REG ADD HKLM\Software\GlobalSight /v PdfConvDir /t REG_SZ /d D:\WINFILES

Now you can go to the ControlPanel->Services and manually set how the service should log-on.
The default is the "LocalSystem" account. If the "conversion directory" is on a separate server,
then this service may need to run as a user with appropriate access rights.


NOTE: The service can be uninstalled by executing:
# C:\WINNT\Microsoft.NET\framework\v1.0.3705\InstallUtil.exe /uninstall PdfConversionService.exe
The registry key can be manually deleted by executing:
# REG DELETE HKLM\Software\GlobalSight



