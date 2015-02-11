GlobalSight And Mediasurface (Version 5.0.0, build 192) Integration (CMC UI only)
---------------------------------------------------------------------------------

Content:

1. GlobalSightHost.properties - A new property file used to list GlobalSight GlobalSight host(s).
2. globalsight.cmcss - The GlobalSight style sheet used for the CMS UI.
3. logo.gif - The GlobalSight logo used for the CMS UI.
4. ms-browse.jsp - Modified to store an GlobalSight username in the session.
5. ms-localize.jsp and localize-body.jsp - New JSP files for the localization purposes.
6. navbar.inc - The existing include file which was modified for adding the localize tab.

================================================================================================

Step 1 - Instructions:

1. Please make sure to have a backup of the original files and extract this zip file under
../Tomcat4.1/webapps directory.


2. Extract The CMCMessages.properties file from cmc/WEB-INF/lib/ms-taglib.jar

jar -xvf ms-taglib.jar com\mediasurface\client\servlets\CMCMessages.properties


3. Add the following to the end of the CMCMessages.properties (the com\...\CMCMessage.properties is 
extracted relative to the ms-taglib.jar's lib directory):

# Localization 
globalsightHost=GlobalSight Host
checkAll = Check All
clearAll=Clear All
itemId = Item Id
localize=Localize
localizeItem=Localize item <i>"{0}"</i>
pleaseSelect=Please select...
selectGlobalSightHost=Please select an GlobalSight host.
selectMediaType=Please select a media type and at least an item to be imported.


4. Now update the ms-taglib.jar:

jar -uvf ms-taglib.jar com\mediasurface\client\servlets\CMCMessages.properties


5. Once done with the update, remove the 'com' folder (and it's sub-folders) from the lib directory.

================================================================================================

Step 2 - Updating the CMS style sheet:

1. Login to Mediasurface.
2. Click on View -> Options
3. Click on Administration tab
4. Select the 'globalsight' from the Colour Scheme combo-box.
5. Click on the 'Commit Changes' button to update the style sheet.