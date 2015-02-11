<%@ page
    contentType="text/html; charset=UTF-8"
    import="com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.WebAppConstants,
            javazoom.upload.*,
            java.util.*"
%>
<!--
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003 Frederico Caldeira Knabben
 *
 * Licensed under the terms of the GNU Lesser General Public License
 * (http://www.opensource.org/licenses/lgpl-license.php)
 *
 * For further information go to http://www.fredck.com/FCKeditor/ 
 * or contact fckeditor@fredck.com.
 *
 * upload.jsp: Basic file upload manager for the editor. You have
 *   to have set a directory called "UserImages" in the root folder
 *   of your web site.
 *
 * Authors:
 *   Simone Chiaretta (simone@piyosailing.com)
-->
<jsp:useBean id="upBean" scope="page" class="javazoom.upload.UploadBean">
  <jsp:setProperty name="upBean" property="overwrite" value="true" />
</jsp:useBean>
<%!
static private String s_basedir;

static
{
    try
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        String dir = config.getStringParameter(
          SystemConfigParamNames.FILE_STORAGE_DIR);
    
        StringBuffer buf = new StringBuffer(dir.replace('\\','/'));
    
        if (buf.charAt(buf.length() - 1) != '/')
        {
            buf.append('/');
        }
    
        // <FileStorage>/GlobalSight
        buf.append(WebAppConstants.VIRTUALDIR_TOPLEVEL);
    
        s_basedir = buf.toString();
    }
    catch (Throwable ex)
    {
        System.err.println("filemanager: cannot find upload directory");

        s_basedir = System.getProperty("java.io.tmpdir");
    }
}
%><%
String imagesDir = "/terminology/images";
String docsDir   = "/terminology/media";
String fileType  = (String)request.getParameter("type");
String selectedDir = "";

if (fileType == null || fileType.length() == 0)
{	
	fileType = "img";
}

if (fileType.equals("img"))
{
	selectedDir = imagesDir;
}
else if (fileType.equals("doc"))
{
	selectedDir = docsDir;
}
upBean.setFolderstore(s_basedir + selectedDir);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE>File Upload</TITLE>
</HEAD>
<BODY>
<!--<%=upBean.getFolderstore()%>-->
<TABLE height="100%" width="100%">
  <TR>
    <TD align=center valign=middle>
      Upload in progress...
      <img height=9 width=9 src='/globalsight/envoy/terminology/viewer/bullet2.gif'>
    </TD>
  </TR>
</TABLE>
</BODY>
</HTML>
<%
response.flushBuffer();

MultipartFormDataRequest mrequest = new MultipartFormDataRequest(request);
Hashtable files = mrequest.getFiles();
if (files != null && !files.isEmpty())
{
	UploadFile file = (UploadFile)files.get("FCKeditor_File");
	upBean.store(mrequest, "FCKeditor_File");
	String sFileURL = selectedDir + "/" + file.getFileName();
%>
<SCRIPT>
window.opener.setImage('<%=sFileURL %>');
window.close();
</SCRIPT>
<%
}
%>
