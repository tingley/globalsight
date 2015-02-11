<%@ page
    contentType="text/html; charset=UTF-8"
    import="java.io.*,
            java.util.*,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.WebAppConstants"
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
 * browse.jsp: Sample server images/files browser for the editor.
 *
 * Authors:
 *   Simone Chiaretta (simone@piyosailing.com)
-->
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
String imagesDir = "/terminology/images/";
String docsDir = "/terminology/media/";
String fileType = (String)request.getParameter("type");
String selectedDir = "";
String functionName = "";

if (fileType == null || fileType.length() == 0)
{
	fileType = "img";
}

if (fileType.equals("img"))
{
	selectedDir = imagesDir;
	functionName = "getImage";
}
else if (fileType.equals("doc"))
{
	selectedDir = docsDir;
	functionName = "getDoc";
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" >
<HTML>
<HEAD>
<TITLE>File Browser</TITLE>
<STYLE>
BODY, TD, INPUT { FONT-SIZE: 12px; FONT-FAMILY: Arial, Helvetica, sans-serif }
#imgPreview { /*width: 200;*/ }
</STYLE>
<SCRIPT>
var sImagesPath  = "<%=selectedDir%>";
var sActiveImage = "";

function getImage(imageName)
{
	sActiveImage = sImagesPath + imageName;
	imgPreview.alt = imageName;
	imgPreview.src = '/ambassador' + sActiveImage;
}

function ok()
{
	opener.setImage(sActiveImage);
	window.close();
}

function getDoc(fileName)
{
	opener.setImage(sImagesPath + fileName);
	window.close();
}
</SCRIPT>
</HEAD>
<BODY>
<TABLE height="100%" cellspacing="0" cellpadding="0" width="100%" border="0">
  <TR>
    <TD height="100%">
      <TABLE height="100%" cellspacing="5" cellpadding="0" width="100%" border="0">
	<TR>
	  <TD valign="top" align="middle" width="200">
	    <fieldset style="position: relative; height: 220; width:100%;">
	    <legend><b>Select the file to use</b></legend>
	    <div style="position: relative; height: 210; width:100%;
	    overflow: auto; ">
<%
String imagesFolder = s_basedir + selectedDir;
File folder = new File(imagesFolder);
String[] filesImmagini = folder.list();
if (filesImmagini != null && filesImmagini.length > 0)
{
  for (int i = 0; i < filesImmagini.length; i++)
  {
	out.println("<A href=\"javascript:" + functionName +
	    "('" + EditUtil.toJavascript(filesImmagini[i]) +
	    "');\">" + EditUtil.encodeHtmlEntities(filesImmagini[i]) +
	    "</A><BR>");
  }
}
else if (fileType.equals("img"))
{
	out.println("No images available.");
}
else
{
	out.println("No files available.");
}
%>
	    </div>
	    </fieldset>
	  </TD>
<% if (fileType.equals("img")) {%>
	  <TD valign="top" align="middle" width="220">
	    <fieldset style="position: relative; height: 220; width: 100%;">
	    <legend><b>Preview</b></legend>
	    <div style="position: relative; height: 210; width:210;
	    overflow: auto;">
	    <IMG src="/globalsight/images/spacer.gif" id="imgPreview"
	     onerror="this.alt='Image failed to load'">
	    </div>
	  </TD>
<% } %>
	</TR>
      </TABLE>
    </TD>
  </TR>
  <TR>
    <TD valign="bottom" align="middle">
      <INPUT style="WIDTH: 80px" type="button" value="OK" onclick="ok();">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <INPUT style="WIDTH: 80px" type="button" value="Cancel"
      onclick="window.close();">
    </TD>
  </TR>
</TABLE>
</BODY>
</HTML>
