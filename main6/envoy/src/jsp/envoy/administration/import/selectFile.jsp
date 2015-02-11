<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            org.apache.log4j.Logger,
            com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.imp.ImportFileFilter,
            com.globalsight.everest.webapp.pagehandler.administration.imp.SelectFileHandler,
            com.globalsight.everest.servlet.EnvoyServletException,
            java.io.File,
            java.io.IOException,
            com.globalsight.ling.common.URLDecoder,
            com.globalsight.ling.common.URLEncoder,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.SortUtil,
            java.util.ArrayList,
            java.util.Collections,
            java.util.Enumeration,
            java.util.HashSet,
            java.util.ResourceBundle"
    session="true" %>
<%@page import="com.globalsight.everest.cvsconfig.CVSServerManagerLocal"%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="selectFileProfile" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    if(request.getAttribute("nextPageFlag") != null) {
        if(request.getAttribute("nextPageFlag").equals("false")) {
            response.sendRedirect("/globalsight/ControlServlet?activityName=jobsAll&searchType=stateOnly");
        } else {
        	String jobType = (String)request.getAttribute("jobType");
        	if (jobType != null && "rssJob".equals(jobType))
        		response.sendRedirect("/globalsight/ControlServlet?linkName=showItems&pageName=RSSMAIN&channelId=" + (String)request.getAttribute("RSS_CHANNEL_ID"));
        }
    }
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String moduleLink="/globalsight/ControlServlet?activityName=";
    String title= bundle.getString("lb_import");
    String selfURL = self.getPageURL();
    String fileProfileURL = selectFileProfile.getPageURL();
    Boolean batch = (Boolean)sessionMgr.getAttribute(SelectFileHandler.BATCH_CONDITION);

    // Get the folder selected if there was any.
    String folderSelected = (String)session.getAttribute(SelectFileHandler.FOLDER_SELECTED);
    File baseDir = SelectFileHandler.getCXEBaseDir();
    if (folderSelected != null)
    {
        baseDir = new File(SelectFileHandler.getAbsolutePath(folderSelected));
    }

    String folderSelectedName = baseDir.getName();
    String folderSelectedAbs = baseDir.getAbsolutePath();

    // get the parent directory.
    File parentDir = baseDir.getParentFile();

    File baseDirParentFile = SelectFileHandler.getCXEBaseDir().getParentFile();
    String parentDirName = null;
    if (parentDir == null)
    {
       parentDirName = "";
    }
    else if (!parentDir.equals(baseDirParentFile))
    {
        if (!parentDir.equals(SelectFileHandler.getCXEBaseDir()))
        {
            parentDirName = SelectFileHandler.getRelativePath(
              SelectFileHandler.getCXEBaseDir(), parentDir);
        }
        else
        {
            parentDirName = "";
        }
    }
%>
<%!
   private static Logger category =
     Logger.getLogger(
       SelectFileHandler.class.getName());

    // prepare the folder that has been selected to be displayed.
    private void prepareFolderListing(HttpServletRequest p_request,
                                      SessionManager p_sessionMgr,
                                      String p_selectFileURL,
                                      File p_baseDir,
                                      String folderSelected,
                                      JspWriter out)
    {
    try {
        // obtain the file directory of the selected document.
        ImportFileFilter filter =
            new ImportFileFilter(new Vector());
        File[] directoryContent = p_baseDir.listFiles(filter);
        ArrayList folderList = new ArrayList();
        ArrayList fileList = new ArrayList();
        boolean atTopLevelDocsDir = false;
        CVSServerManagerLocal cvsServerManager = new CVSServerManagerLocal();
        String tmp = "";

        if (p_baseDir.getPath().equals(SelectFileHandler.getCXEBaseDir().getPath()))
          atTopLevelDocsDir = true;
        
        //category.info("atTopLevelDocsDir==" + atTopLevelDocsDir + ", baseDir==" + p_baseDir.getPath() + ", CXEBaseDir==" + SelectFileHandler.getCXEBaseDir().getPath()); 
        // Place the directory content under the folder
        // list or the file list for proper sequencing.
        Vector<String> cvsSandBoxs = cvsServerManager.getAllServerSandbox();
		if (directoryContent != null && directoryContent.length > 0)
		{
	        for (int i=0; i<directoryContent.length; i++)
	        {
	            File curDoc = directoryContent[i];
	            if (curDoc.isFile())
	            {
	               if (atTopLevelDocsDir)
	               {
	                 //do not add files at the top level
	                 category.info("Ignoring file " + curDoc +
	                 " since it is not under a locale specific subdirectory.");
	               }
	               else
	               {
	                 fileList.add(curDoc);
	               }
	            }
	            else
	            {
	            	//To ignore CVS folders
	            	tmp = curDoc.getAbsolutePath();
	            	tmp = tmp.substring(tmp.lastIndexOf(File.separator) + 1);
	            	if (!cvsSandBoxs.contains(tmp) && !tmp.endsWith(".sub"))
	            	    folderList.add(curDoc);
	            }
	        }
		}

        int rowCount = 0;
        // Print out the folders first.
        for (int i=0; i<folderList.size(); i++)
        {
            getFolderString((File)folderList.get(i),
                            p_selectFileURL,
                            out,
                            folderSelected);
            rowCount++;
        }

        // Print out the files last.
        for (int i=0; i < fileList.size(); i++)
        {
            getDocumentString((File)fileList.get(i),
                              p_selectFileURL,
                              out,
                              folderSelected);
            rowCount++;
        }

        // Print out extra rows so the table will always have a scrollbar
        // -- purely cosmetic!
        // The table should always have 18 rows for there to be a scrollbar.
        if (rowCount < 18)
        {
            int rowsNeeded = 18 - rowCount;

            for (int i = 0; i < rowsNeeded; i++)
            {
                out.println("<TR><TD COLSPAN=4>&nbsp;</TD></TR>");
            }
        }
        }
        catch (Exception e) {
            category.warn("Unable to read files for the manual import UI. The docs directory setting may be incorrect\nor the permissions on that directory may be incorrect.");
        }
    }

   private void getFolderString(File p_document,
                                String p_selectFileURL,
                                JspWriter out,
                                String folderSelected)
    throws IOException
    {
	    if (folderSelected == null)
        {
            folderSelected = "";
        }

        String fileNameValue =
            URLEncoder.encode(SelectFileHandler.getRelativePath(SelectFileHandler.getCXEBaseDir(), p_document), "UTF-8");
     	// Escape Javascript special characters, such as "\", "'".
        String fileNameValueEscaped =
            replace(SelectFileHandler.getRelativePath(SelectFileHandler.getCXEBaseDir(), p_document),
                    "\\", "\\\\");
        fileNameValueEscaped = fileNameValueEscaped.replace("\'", "\\'");
        String displayNameValue =
            SelectFileHandler.getRelativePath(p_document.getParentFile(), p_document);

        out.println("<TR VALIGN=TOP>\n");
        out.println("<TD><INPUT TYPE=CHECKBOX NAME=file VALUE=\"" + fileNameValue + "\"></TD>" +
                    "<TD STYLE=\"padding-top: 2px\"><A CLASS=\"standardHREF\" " +
                    "HREF=\"javascript:navigateDirectories('" + fileNameValueEscaped + "')\">" +
                    "<IMG SRC=\"/globalsight/images/folderclosed.gif\" BORDER=0 HEIGHT=13 WIDTH=15></A></TD>" +
                    "<TD COLSPAN=2><A CLASS=\"standardHREF\" HREF=\"javascript:navigateDirectories('" +
                    fileNameValueEscaped + "')\">" + displayNameValue + "</A></TD>\n");
        out.println("</TR>\n");
    }

    private void getDocumentString(File p_document,
                                   String p_selectFileURL,
                                   JspWriter out,
                                   String folderSelected)
    throws IOException
    {
        String fileName = SelectFileHandler.getRelativePath(SelectFileHandler.getCXEBaseDir(), p_document);
        out.println("<TR VALIGN=TOP>\n");
        out.println("<TD><INPUT TYPE=CHECKBOX NAME=file VALUE=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"></TD>");
        out.println("<TD STYLE=\"padding-top: 2px\"><IMG SRC=\"/globalsight/images/file.gif\" HEIGHT=15 WIDTH=13></TD>");
        out.println("<TD COLSPAN=2 STYLE=\"word-wrap: break-word;\">" +
                    SelectFileHandler.getRelativePath(p_document.getParentFile(), p_document) + "</TD>\n");
        out.println("</TR>\n");
    }
%>
<%!
String replace(String s, String one, String another)
{
    // Utility function for replacing one substring with another.
    // For handling backslashes (or other problem chars) for javascript because
    // javascript will eat the first backslash
    if (s.equals("")) return "";
    String res = "";
    int i = s.indexOf(one,0);
    int lastpos = 0;
    while (i != -1)
    {
        res += s.substring(lastpos,i) + another;
        lastpos = i + one.length();
        i = s.indexOf(one,lastpos);
    }
    res += s.substring(lastpos);  // the rest
    return res;
}
%>
<HTML>
<!-- /envoy/src/jsp/envoy/administration/import/selectFile.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/constants.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "import";
var helpFile = "<%=bundle.getString("help_import_select_files")%>";

function submitForm(action)
{
    form = document.importFilesForm;
    if (action == "<%=WebAppConstants.NEXT%>")
    {
       form.pageAction.value = "<%=WebAppConstants.IMPORT%>"; // default
       form.action = "<%=fileProfileURL%>";
    }
    else if (action == "<%=WebAppConstants.CANCEL%>")
    {
       // Go back to the File Import (grid) screen and clear
       // the file listing session attribute
       form.pageAction.value = "<%=WebAppConstants.CANCEL%>";
       form.action = "<%=selfURL%>";
    }

    form.submit();
}

function addRemoveFiles(fileAction)
{
   // When the to/form buttons (>>, <<) are clicked
   // submit the corresponding form
   if (fileAction == "add")
   {
      form = document.availableFilesForm;
   }
   else if (fileAction == "remove")
   {
      form = document.selectedFilesForm;
   }

   if (!form.file || !isRadioChecked(form.file)) return false;

   var file = "";
   // If more than one radio button is displayed, loop
   // through the array to find the one checked
   if (form.file.length)
   {
      for (i = 0; i < form.file.length; i++)
      {
         if (form.file[i].checked == true)
         {
            if(file != "")
            {
               file += " "; // must add a [white space] delimiter
            }
            file += form.file[i].value;
         }
      }
   }
   // If only one radio button is displayed, there is no radio button array, so
   // just check if the single radio button is checked
   else
   {
      if (form.file.checked == true)
      {
         file = form.file.value;
      }
   }
   form.file.value = file;
   form.submit();
}

function navigateDirectories (folder)
{
   navigateDirectoriesForm.<%=SelectFileHandler.FOLDER_SELECTED%>.value = encodeURIComponent(folder);
   navigateDirectoriesForm.submit();
}

//for GBS-2599
$(document).ready(function(){
	$("#selectAll_1").click(function(){
		$("form[name='availableFilesForm'] :checkbox[name!='selectAll_1']").each(function(){
			if($("#selectAll_1").attr("checked")){
				$(this).attr("checked",true);
			}else{
				$(this).attr("checked",false);
			}
		});      
	});
	$("#selectAll_2").click(function(){
		$("form[name='selectedFilesForm'] :checkbox[name!='selectAll_2']").each(function(){
			if($("#selectAll_2").attr("checked")){
				$(this).attr("checked",true);
			}else{
				$(this).attr("checked",false);
			}
		});      
	});
});
</script>
<STYLE type="text/css">
.importList {
	position: relative;
	width: 338px;
	height: 300px;
	overflow-y: auto;
	overflow-x: auto;
    border: solid silver 1px;
	padding: 0px;
}
</STYLE>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=600><%=bundle.getString("helper_text_import_choose_files")%></TD>
  </TR>
</TABLE>
<P></P>
<%
String curFolder = folderSelected;
if (folderSelected != null)
{
    curFolder = URLEncoder.encode(curFolder, "UTF-8");
}
%>
<!-- Empty form that will be handled and submitted by
  -- the navigateDirectories() javascript function above.  -->
<FORM NAME="navigateDirectoriesForm" ACTION="<%=selfURL%>" METHOD="POST">
    <!-- This is the directory you are in.  -->
    <INPUT NAME="<%=SelectFileHandler.FOLDER_SELECTED%>" VALUE="<%=curFolder%>" TYPE="HIDDEN">
</FORM>

<!-- Availabe Files -->
<FORM NAME="availableFilesForm" ACTION="<%=selfURL%>" METHOD="POST">
<!-- This is the directory you are in.  -->
<INPUT NAME="<%=SelectFileHandler.FOLDER_SELECTED%>" VALUE="<%=curFolder%>" TYPE="HIDDEN">
<!-- This is action (Add/Remove) that you are performing on
  -- the file list. -->
<INPUT NAME="fileAction" VALUE="add" TYPE="HIDDEN">

<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 80px; LEFT: 0px;">
<SPAN CLASS="standardTextBold"><%=bundle.getString("lb_available_files")%></SPAN>
</DIV>

<DIV CLASS="importList"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 100px; LEFT: 0px;">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="table-layout: fixed;" WIDTH=320>
    <COL WIDTH="21px"/>   <!-- Checkbox -->
    <COL WIDTH="22px"/>   <!-- Folder/File icon -->
    <COL WIDTH="259px"/>  <!-- File -->
    <COL WIDTH="18px"/>   <!-- Up Folder (for header) -->
    <TR CLASS="tableHeadingBasic" VALIGN="TOP">
      <TD><input type="checkbox" id="selectAll_1" name="selectAll_1"/></TD>
      <TD><IMG SRC="/globalsight/images/folderopen.gif" HEIGHT=13 WIDTH=16 VSPACE=3></TD>
      <TD STYLE="word-wrap: break-word; padding-top: 2px"><%=folderSelectedAbs%></TD>
      <TD ALIGN="RIGHT"><%
            boolean atTop = false;
                if (parentDirName == null ||
                    (parentDirName.equals("") &&
                     folderSelectedName.equals("")))
                {
                    atTop = true;
                }
                if (atTop)
                    out.print("&nbsp;");
                else
                {
                    String tempParDir = replace(parentDirName, "\\", "\\\\");
                    tempParDir = EditUtil.toJavascript(tempParDir);
                    out.print("<A HREF=\"javascript:navigateDirectories('" +tempParDir+ "')\"><IMG SRC=\"/globalsight/images/folderback.gif\" BORDER=\"0\" HEIGHT=13 WIDTH=15 VSPACE=3 HSPACE=1></A>");
                }
        %>
      </TD>
    </TR>
<% 
prepareFolderListing(request, sessionMgr, selfURL, baseDir, folderSelected, out);
%>
  </TABLE>
</DIV>
</FORM>

<!--for gbs-2599
DIV ID="AvailableCheckAllLayer"
  STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 400px; LEFT: 0px;"
  CLASS=standardText>
  <A CLASS="standardHREF" HREF="#"
  ONCLICK="checkAll('availableFilesForm')"><%=bundle.getString("lb_check_all")%></A> |
  <A CLASS="standardHREF" HREF="#"
  ONCLICK="clearAll('availableFilesForm')"><%=bundle.getString("lb_clear_all")%></A>
</DIV-->
<!-- End Availabe Files -->

<!-- Add/Remove buttons -->
<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 6; TOP: 200px; LEFT: 370px;">
<INPUT TYPE="BUTTON" VALUE="&gt;&gt;" ONCLICK="addRemoveFiles('add')" TITLE="Add"><P>
<INPUT TYPE="BUTTON" VALUE="&lt;&lt;" ONCLICK="addRemoveFiles('remove')" TITLE="Remove">
</DIV>
<!-- End Add/Remove buttons -->

<!-- Selected Files -->
<FORM NAME="selectedFilesForm" ACTION="<%=selfURL%>" METHOD="POST">
<!-- This is the directory you are in.  -->
<INPUT NAME="<%=SelectFileHandler.FOLDER_SELECTED%>" VALUE="<%=curFolder%>" TYPE="HIDDEN">
<!-- This is action (Add/Remove) that you are performing on
  -- the file list. -->
<INPUT NAME="fileAction" VALUE="remove" TYPE="HIDDEN">

<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 80px; LEFT: 420px;">
<SPAN CLASS="standardTextBold">
<%= bundle.getString("msg_files_selected_for_import") %></SPAN>
</DIV>

<DIV CLASS="importList" STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 100px; LEFT: 420px;">

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="table-layout: fixed;" WIDTH=320>
    <COL WIDTH="21px"/>   <!-- Checkbox -->
    <COL WIDTH="22px"/>   <!-- File icon -->
    <COL WIDTH="277px"/>  <!-- File -->
    <TR CLASS="tableHeadingBasic">
      <TD><input type="checkbox" id="selectAll_2" name="selectAll_2"/></TD>
      <TD><IMG SRC="/globalsight/images/file.gif" HEIGHT=15 WIDTH=13 VSPACE=2></TD>
      <TD><%=bundle.getString("lb_file")%></TD>
    </TR>
<%
    if (folderSelected == null)
    {
        folderSelected = "";
    }

    ArrayList importFileList =
        new ArrayList((HashSet)sessionMgr.getAttribute(SelectFileHandler.FILE_LIST));
	SortUtil.sort(importFileList);
    int rowCount = 0;
	for (int i=0; i < importFileList.size(); i++)
	{
		String curDoc = (String)importFileList.get(i);
		out.println("<TR VALIGN=TOP>");
		out.println("<TD><INPUT TYPE=CHECKBOX NAME=file VALUE=\"" + URLEncoder.encode(curDoc, "UTF-8") + "\"></TD>");
		out.println("<TD STYLE=\"padding-top: 2px\"><IMG SRC=\"/globalsight/images/file.gif\" HEIGHT=15 WIDTH=13></TD>");
		out.println("<TD STYLE=\"word-wrap: break-word;\">" + curDoc + "</TD>");
		out.println("</TR>");
        rowCount++;
	}

    // Print out extra rows so the table will always have a scrollbar
    // -- purely cosmetic!
    // The table should always have 16 rows for there to be a scrollbar.
    if (rowCount < 18) {
        int rowsNeeded = 18 - rowCount;

        for (int i = 0; i < rowsNeeded; i++)
        {
            out.println("<TR><TD COLSPAN=3>&nbsp;</TD></TR>");
        }
    }
%>
</TABLE>
</DIV>
<!-- End Selected Files -->
</FORM>

<!--for gbs-2599
DIV ID="ImportCheckAllLayer" CLASS="standardText"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 400px; LEFT: 420px;">
<A CLASS="standardHREF" HREF="#"
 onclick="checkAll('selectedFilesForm')"><%=bundle.getString("lb_check_all")%></A> |
<A CLASS="standardHREF" HREF="#"
 onclick="clearAll('selectedFilesForm')"><%=bundle.getString("lb_clear_all")%></A>
</DIV-->

<!-- Import Button-->
<DIV ALIGN="RIGHT" STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 415px; LEFT: 420px; WIDTH: 338">
<SPAN CLASS="standardTextBold">

<FORM METHOD="post" NAME="importFilesForm">
<INPUT TYPE="HIDDEN" NAME="jobName" VALUE="">
<INPUT TYPE="HIDDEN" NAME="pageAction" VALUE="<%=WebAppConstants.IMPORT%>">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel")%>" ONCLICK="submitForm('<%=WebAppConstants.CANCEL%>');">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_next")%>" ONCLICK="submitForm('<%=WebAppConstants.NEXT%>');"
<%
  if (importFileList.size() == 0) out.print(" DISABLED");
%>
>
</FORM>
</DIV>
<!-- End Import Button-->

</BODY>
</HTML>
