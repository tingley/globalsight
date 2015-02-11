FrameMakerConverter Installation
1. Add plugin to FrameMaker
Stop FrameMaker, copy fmconv.dll to <FrameMaker9 Installation Path>\fminit\Plugins add the following entry (all on one line) to the [APIClients] section of your maker.ini file:

fmconv=Standard, GlobalSight FMConvert, fminit\Plugins\fmconv.dll, all

2. Start FrameMakerConverter.exe, set FrameMaker9 path and conversion directory. Then start Converter.

3. FrameMakerConverter.exe will create a folder named "FrameMaker9" in <conversion directory> (like c:\winfiles\FrameMaker9). For test purpose, create a folder named "en_US" in <conversion directory>\FrameMaker9 (like c:\winfiles\FrameMaker9\en_US), copy command file and source file to folder "en_US" to test conversion function.

Note: If you want to use FrameMaker directly, please stop FrameMakerConverter and disable fmconv plugin.

How to disable fmconv:
1. Stop FrameMaker.
2. Delete or comment "fmconv=Standard, GlobalSight FMConvert, fminit\Plugins\fmconv.dll, all" in your maker.ini file