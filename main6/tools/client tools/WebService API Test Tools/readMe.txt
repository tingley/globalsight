To use this webservice API test tool, please follow below steps:
1. Update from CVS server to get latest source codes if possible.
2. In command line window, CD to "main6\tools\client tools\WebService API Test Tools" folder.
3. Run "ant" command to compile source codes and generate a "WebServiceTestTool.zip" file.
4. Copy this "WebServiceTestTool.zip" file to another place, unzip it. Note that space is not allowed in the path.
5. Click "run.bat" to run.

More info:
1. In "test.properties" file, multiple "webservice.url" and "webservice.class" can be defined. This tool will pick up the one that is not disabled.
2. If the API is using certain java class such as "HashMap" as parameter, this tool can't support.