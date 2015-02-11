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
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.SortUtil,
            java.io.File,
            java.io.IOException,
            com.globalsight.ling.common.URLDecoder,
            com.globalsight.ling.common.URLEncoder,
            java.util.ArrayList,
            java.util.Collections,
            java.util.Enumeration,
            java.util.HashSet,
            java.util.List,
            java.util.ResourceBundle"
    session="true" %>
<jsp:useBean id="selectFile" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadApplet" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    List list = (List)sessionMgr.getAttribute(DownloadFileHandler.DOWNLOAD_JOB_LOCALES);
    String moduleLink="/globalsight/ControlServlet?activityName=";
    String title = bundle.getString("lb_select_files_to_download");
    String selectFileURL = selectFile.getPageURL();
    String downloadAppletURL = downloadApplet.getPageURL();
    String doneURL = done.getPageURL();
    
    String fromJobDetail = request.getParameter("fromJobDetail");
    if ("true".equals(fromJobDetail))
    {
        downloadAppletURL += "&fromJobDetail=true";
        selectFileURL += "&fromJobDetail=true";
    }
    else
    {
        String fromTaskDetail = request.getParameter("fromTaskDetail");
        if ("true".equals(fromTaskDetail))
        {
            downloadAppletURL += "&fromTaskDetail=true";
            selectFileURL += "&fromTaskDetail=true";
        }
        else
        {
            String from = request.getParameter("from");
            if (from != null && from.length() > 0)
            {
                downloadAppletURL += "&from=" + from;
                selectFileURL += "&from=" + from;
            }
        }
    }
    
    
    // Get the folder selected if there was any.
    String currentFolder = (String)session.getAttribute(DownloadFileHandler.PARAM_CURRENT_FOLDER);
    List pageList = (List)sessionMgr.getAttribute(DownloadFileHandler.PARAM_DOWNLOAD_FILE_NAME);
    String companyType = (String)sessionMgr.getAttribute(DownloadFileHandler.CompanyType);
    File baseDir = DownloadFileHandler.getCXEBaseDir();
    
    if (currentFolder != null )
    {
//        baseDir = new File(DownloadFileHandler.getAbsolutePath(
//          URLDecoder.decode(currentFolder)));
        baseDir = new File(DownloadFileHandler.getAbsolutePath(currentFolder));

    }
    String folderSelectedName = baseDir.getName();
    String folderSelectedAbs = baseDir.getAbsolutePath();

    // get the parent directory.
    File parentDir = baseDir.getParentFile();
    File baseDirParentFile = DownloadFileHandler.getCXEBaseDir();
    if(list == null && currentFolder.endsWith(DownloadFileHandler.DESKTOP_FOLDER))
    {
        String currentbasefolder = currentFolder.substring(0, currentFolder.indexOf(DownloadFileHandler.DESKTOP_FOLDER));
        baseDirParentFile = new File(DownloadFileHandler.getAbsolutePath(currentbasefolder + DownloadFileHandler.DESKTOP_FOLDER ));
        parentDir = new File(DownloadFileHandler.getAbsolutePath(currentbasefolder + DownloadFileHandler.DESKTOP_FOLDER));
    }

    String parentDirName = null;
    File cxeBaseDirFile = DownloadFileHandler.getCXEBaseDir();
    if (parentDir == null || list != null)
    {
       parentDirName = "";
       if (!parentDir.equals(cxeBaseDirFile) && (parentDir.getPath().length() > cxeBaseDirFile.getPath().length()))
        {
            parentDirName = DownloadFileHandler.getRelativePath(
              cxeBaseDirFile, parentDir);
        }
    }
    else if (!parentDir.equals(baseDirParentFile))
    {
        if (!parentDir.equals(cxeBaseDirFile) && (parentDir.getPath().length() > cxeBaseDirFile.getPath().length()))
        {
            parentDirName = DownloadFileHandler.getRelativePath(
              cxeBaseDirFile, parentDir);
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
       DownloadFileHandler.class.getName());

    // prepare the folder that has been selected to be displayed.
    private void prepareFolderListing(HttpServletRequest p_request,
                                      SessionManager p_sessionMgr,
                                      String p_selectFileURL,
                                      File p_baseDir,
                                      String currentFolder,
                                      List pageList,
                                      JspWriter out, 
                                      List list)
    {
        String uploadName = (String) p_sessionMgr.getAttribute(
        DownloadFileHandler.PARAM_UPLOAD_NAME);
        String downloadName = (String) p_sessionMgr.getAttribute(
        DownloadFileHandler.PARAM_DOWNLOAD_NAME);
        String companyType=(String) p_sessionMgr.getAttribute(
        DownloadFileHandler.CompanyType);
        if (uploadName == null)
        {
            category.warn("CustomerDownload uploadName is null, using 'Upload' instead.");
            uploadName = "Upload";
        }
        
    
    try {
        // obtain the file directory of the selected document.
        File[] directoryContent = p_baseDir.listFiles();
        ArrayList folderList = new ArrayList();
        ArrayList fileList = new ArrayList();
        boolean atTopLevelDocsDir = false;
        int depth = 0;
        File xlzFile = null;

        if (p_baseDir.getPath().equals(DownloadFileHandler.getCXEBaseDir().getPath()))
        {
            atTopLevelDocsDir = true;
            depth = 0;
        }
        else if (p_baseDir.getPath().endsWith("webservice"))
        {
            depth = 1;
        }
        else
        {
            String baseDirPath = p_baseDir.getPath();
            String cxeDirPath = DownloadFileHandler.getCXEBaseDir().getPath();
            int idx = baseDirPath.indexOf(cxeDirPath) + cxeDirPath.length();
            String relPath = p_baseDir.getPath().substring(idx).replace('\\','/');
            depth = (new StringTokenizer(relPath,"/")).countTokens();
        }

        // Place the directory content under the folder
        // list or the file list for proper sequencing.
        for (int i=0; i< directoryContent.length; i++)
        {
            File curDoc = directoryContent[i];
            if (curDoc.isFile() && pageList != null && pageList.size() != 0)
            {
                 for(Iterator pages = pageList.iterator(); pages.hasNext();)
                 {
                   String fileName =  (String)pages.next();
                   if(curDoc.getName().equals(fileName))
                   {
                       fileList.add(curDoc);
                   }
                 }
            }
            else
            {
              String webservice = (String)p_sessionMgr.getAttribute(DownloadFileHandler.DESKTOP_FOLDER);
              
              if(webservice != null && webservice.equals(DownloadFileHandler.DESKTOP_FOLDER))
              {
                if(depth == 1 && curDoc.getName().equals(webservice))
                     folderList.add(curDoc);
              }
              xlzFile = new File(curDoc.getAbsoluteFile() + ".xlz");
              if (xlzFile.exists() && xlzFile.isFile())
                  continue;
              String tmp = curDoc.getAbsolutePath();
              if (tmp.endsWith(".sub"))
                  continue;
              
              if(downloadName != null && downloadName.length() != 0)
              {
                 if (((depth == 1||(depth==2&&companyType.equals("superCompany"))) && !curDoc.getName().equals(downloadName)))
                 {
                  // Do nothing.
                 }
                 else
                 {
                    folderList.add(curDoc);
                 }
              }
              else
              {
                 if(depth == 1 && curDoc.isFile())
                 {
                      folderList.add(curDoc);
                 }
                 if(depth == 0)
                 {
                    folderList.add(curDoc);
                 }
              }
            }
        }

        int rowCount = 0;
        // Print out the folders first.
        for (int i=0; i<folderList.size(); i++)
        {
          File folder = (File)folderList.get(i);
          if(currentFolder.length() < 2)
          {
             for(int j = 0; j < list.size(); j++)
             {
               if(!folder.getAbsolutePath().endsWith((String)list.get(j)))
               {
                 continue;
               }
               getFolderString(folder,
                               p_selectFileURL,
                               out,
                               currentFolder);
             }
          }else
          {
               getFolderString(folder,
                               p_selectFileURL,
                               out,
                               currentFolder);
          }
            rowCount++;
        }

        // Print out the files last.
        for (int i=0; i < fileList.size(); i++)
        {
          File file = (File)fileList.get(i);
          getDocumentString(file,
                            p_selectFileURL,
                            out,
                            currentFolder);

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
            category.error("Unable to read files for the customer download UI. The docs directory setting may be incorrect\nor the permissions on that directory may be incorrect.");
        }
    }

   private void getFolderString(File p_document,
                                String p_selectFileURL,
                                JspWriter out,
                                String currentFolder)
    throws IOException
    {
     if (currentFolder == null)
        {
            currentFolder = "";
        }

        String fileNameValue = DownloadFileHandler.getRelativePath(DownloadFileHandler.getCXEBaseDir(), p_document);
        // Escape the backslashes for javascript
        String fileNameValueEscaped =
            replace(DownloadFileHandler.getRelativePath(DownloadFileHandler.getCXEBaseDir(), p_document),
                    "\\", "\\\\");
        String displayNameValue =
            DownloadFileHandler.getRelativePath(p_document.getParentFile(), p_document);
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
                                   String currentFolder)
    throws IOException
    {
        String fileName = DownloadFileHandler.getRelativePath(DownloadFileHandler.getCXEBaseDir(), p_document);
        out.println("<TR VALIGN=TOP>\n");
        out.println("<TD><INPUT TYPE=CHECKBOX NAME=file VALUE=\"" + fileName + "\"></TD>");
        out.println("<TD STYLE=\"padding-top: 2px\"><IMG SRC=\"/globalsight/images/file.gif\" HEIGHT=15 WIDTH=13></TD>");
        out.println("<TD COLSPAN=2 STYLE=\"word-wrap: break-word;\">" +
                    DownloadFileHandler.getRelativePath(p_document.getParentFile(), p_document) + "</TD>\n");
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
<!-- This JSP is: /envoy/administration/customer/download/selectFile.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/constants.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "import";
var helpFile = "<%=bundle.getString("help_customer_download")%>";

function submitForm(action)
{
    form = document.downloadFilesForm;
    if (action == "download")
    {
        //Fix for GBS-1570  
        form = document.selectedFilesForm;
        var file = "";
        if (form.file.length)
        {
           for (i = 0; i < form.file.length; i++)
           {
              if (form.file[i].checked == true)
              {
                 if(file != "")
                 {
                    file += ","; // must add a [white space] delimiter
                 }
                 file += encodeURIComponent(form.file[i].value).replace(/%C2%A0/g, "%20");
              }
           }
        }
        form = document.downloadFilesForm;
        form.selectedFileList.value = file;
           
        // Go to the Download Applet screen
        form.action = "<%=downloadAppletURL%>" + "&action=download";
    }
    else if (action == "cancel")
    {
       // Just go back to the Home page
       form.action = "<%=doneURL%>" + "&action=done";
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

   if (fileAction == "add")
   {
      file = file.replace(/\+/g,"%2B");
   }

   form.file.value = file;
   form.submit();
}

function navigateDirectories (folder)
{
   navigateDirectoriesForm.<%=DownloadFileHandler.PARAM_CURRENT_FOLDER%>.value = folder;
   navigateDirectoriesForm.submit();
}

//for GBS-2599
function handleSelectAll(selectAll,theForm) {
    if (theForm) {
        if (selectAll.checked) {
            checkAll(theForm);
        }
        else {
            clearAll(theForm); 
        }
    }
}

function cancelButton(){
	if (location.search.indexOf("redirectToWorkflow") > -1)
	{
		var jobId = location.search.split("&")[6].split("=")[1];
		location.href = "/globalsight/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId=" + jobId;
	}
	else
	{
		history.go(-1);
	}
}
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
    <TD WIDTH=600><%=bundle.getString("helper_text_customerdownload_choose_files")%></TD>
  </TR>
</TABLE>
<P></P>
<!-- Empty form that will be handled and submitted by
  -- the navigateDirectories() javascript function above.  -->
<FORM NAME="navigateDirectoriesForm" ACTION="<%=selectFileURL%>" METHOD="POST">
    <!-- This is the directory you are in.  -->
    <INPUT NAME="<%=DownloadFileHandler.PARAM_CURRENT_FOLDER%>" VALUE="<%=currentFolder%>" TYPE="HIDDEN">
</FORM>

<!-- Availabe Files -->
<FORM NAME="availableFilesForm" ACTION="<%=selectFileURL%>" METHOD="POST">
<!-- This is the directory you are in.  -->
<INPUT NAME="<%=DownloadFileHandler.PARAM_CURRENT_FOLDER%>" VALUE="<%=currentFolder%>" TYPE="HIDDEN">
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
    <COL WIDTH=21>   <!-- Checkbox -->
    <COL WIDTH=22>   <!-- Folder/File icon -->
    <COL WIDTH=259>  <!-- File -->
    <COL WIDTH=18>   <!-- Up Folder (for header) -->
    <TR CLASS="tableHeadingBasic" VALIGN="TOP">
      <TD><input type="checkbox" onclick="handleSelectAll(this,'availableFilesForm')"/></TD>
      <TD><IMG SRC="/globalsight/images/folderopen.gif" HEIGHT=13 WIDTH=16 VSPACE=3></TD>
      <TD STYLE="word-wrap: break-word; padding-top: 2px"><%=folderSelectedAbs%></TD>
      <TD ALIGN="RIGHT"><%
            boolean atTop = false;
                if (currentFolder.length() == 1 ||
                    currentFolder.length() == 0 ||
                    parentDirName == null ||
                    (parentDirName.equals("") &&
                    folderSelectedName.equals(""))||
                    (companyType!=null&&
                     companyType.equals("superCompany")&&
                     !parentDirName.contains("\\")))
                {
                    atTop = true;
                }
                if (atTop)
                    out.print("&nbsp;");
                else
                    out.print("<A HREF=\"" + selectFileURL + "&" + DownloadFileHandler.PARAM_CURRENT_FOLDER +
                              "=" + parentDirName + "&folderBack=true" + 
                              "\"><IMG SRC=\"/globalsight/images/folderback.gif\" BORDER=\"0\" HEIGHT=13 WIDTH=15 VSPACE=3 HSPACE=1></A>");
        %>
      </TD>
    </TR>
    <%

       prepareFolderListing(request, sessionMgr, selectFileURL,
                    baseDir, currentFolder, pageList, out, list);

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
<INPUT TYPE="BUTTON" VALUE="&gt;&gt;" ONCLICK="addRemoveFiles('add')" TITLE="<%=bundle.getString("lb_add") %>"><P>
<INPUT TYPE="BUTTON" VALUE="&lt;&lt;" ONCLICK="addRemoveFiles('remove')" TITLE="<%=bundle.getString("lb_remove") %>">
</DIV>
<!-- End Add/Remove buttons -->

<!-- Selected Files -->
<FORM NAME="selectedFilesForm" ACTION="<%=selectFileURL%>" METHOD="POST">
<!-- This is the directory you are in.  -->
<INPUT NAME="<%=DownloadFileHandler.PARAM_CURRENT_FOLDER%>" VALUE="<%=currentFolder%>" TYPE="HIDDEN">
<!-- This is action (Add/Remove) that you are performing on
  -- the file list. -->
<INPUT NAME="fileAction" VALUE="remove" TYPE="HIDDEN">

<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 80px; LEFT: 420px;">
<SPAN CLASS="standardTextBold">
<%= bundle.getString("msg_files_selected_for_download") %></SPAN>
</DIV>

<DIV CLASS="importList" STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 100px; LEFT: 420px;">

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="table-layout: fixed;" WIDTH=320>
    <COL WIDTH=21>   <!-- Checkbox -->
    <COL WIDTH=22>   <!-- File icon -->
    <COL WIDTH=277>  <!-- File -->
    <TR CLASS="tableHeadingBasic">
      <TD><input type="checkbox" onclick="handleSelectAll(this,'selectedFilesForm')"/></TD>
      <TD><IMG SRC="/globalsight/images/file.gif" HEIGHT=15 WIDTH=13 VSPACE=2></TD>
      <TD><%=bundle.getString("lb_file")%></TD>
    </TR>
<%
    if (currentFolder == null)
    {
        currentFolder = "";
    }

    ArrayList importFileList =
        new ArrayList((HashSet)sessionMgr.getAttribute(DownloadFileHandler.FILE_LIST));
    SortUtil.sort(importFileList);
    int rowCount = 0;
    for (int i=0; i < importFileList.size(); i++)
    {
        String curDoc = (String)importFileList.get(i);
        out.println("<TR VALIGN=TOP>");
        out.println("<TD><INPUT TYPE=CHECKBOX NAME=file VALUE=\"" + curDoc + "\"></TD>");
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

<DIV ALIGN="RIGHT" STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 415px; LEFT: 420px; WIDTH: 338">
<FORM METHOD="post" NAME="downloadFilesForm">
<INPUT NAME="fileAction" VALUE="download" TYPE="HIDDEN">
<INPUT NAME="selectedFileList" VALUE="" TYPE="HIDDEN">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel")%>" ONCLICK="cancelButton();">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_download")%>" ONCLICK="submitForm('download');"
<%
  if (importFileList.size() == 0) out.print(" DISABLED");
%>
>
</FORM>
</DIV>
</BODY>
</HTML>
