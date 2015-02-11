<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            org.apache.log4j.Logger,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.aligner.ImportFileFilter,
            com.globalsight.everest.webapp.pagehandler.aligner.AlignerPackagePageHandler,
            com.globalsight.everest.aligner.AlignerPackageOptions,
            com.globalsight.everest.aligner.AlignerPackageOptions.FilePair,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.AmbFileStoragePathUtils,
            com.globalsight.util.SortUtil,
            java.io.File,
            java.io.IOException,
            java.util.ArrayList,
            java.util.Collections,
            java.util.ResourceBundle"
    session="true" %>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr =
    (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

AlignerPackageOptions gapOptions = (AlignerPackageOptions)
  sessionMgr.getAttribute(WebAppConstants.GAP_OPTIONS);
File s_cxeBaseDir = AmbFileStoragePathUtils.getCxeDocDir();
ArrayList selectedFiles =
  (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_FILELIST);

String lb_title = bundle.getString("lb_align_select_files");
String lb_helptext = bundle.getString("helper_text_aligner_package_create2");

String urlSelf = self.getPageURL();
String urlNext = next.getPageURL();
String urlPrevious = previous.getPageURL();
String urlCancel = cancel.getPageURL();

// Get the selected source folder.
String folderSelectedSrc =
  (String)session.getAttribute(WebAppConstants.GAP_CURRENTFOLDERSRC);
File baseDirSrc = s_cxeBaseDir;
if (folderSelectedSrc != null)
{
    baseDirSrc = new File(
        AlignerPackagePageHandler.getAbsolutePath(folderSelectedSrc));
}
else
{
    folderSelectedSrc = "";
}

// Get the selected target folder.
String folderSelectedTrg =
  (String)session.getAttribute(WebAppConstants.GAP_CURRENTFOLDERTRG);
File baseDirTrg = s_cxeBaseDir;
if (folderSelectedTrg != null)
{
    baseDirTrg = new File(
        AlignerPackagePageHandler.getAbsolutePath(folderSelectedTrg));
}
else
{
    folderSelectedTrg = "";
}
String folderSelectedNameSrc = baseDirSrc.getName();
String folderSelectedAbsSrc  = baseDirSrc.getAbsolutePath();
String folderSelectedNameTrg = baseDirTrg.getName();
String folderSelectedAbsTrg  = baseDirTrg.getAbsolutePath();

// get the parent directory.
File parentDirSrc = baseDirSrc.getParentFile();
File parentDirTrg = baseDirTrg.getParentFile();

File baseDirParentFileSrc = s_cxeBaseDir.getParentFile();
File baseDirParentFileTrg = s_cxeBaseDir.getParentFile();

String parentDirNameSrc = "";
if (parentDirSrc != null && !parentDirSrc.equals(baseDirParentFileSrc))
{
    if (!parentDirSrc.equals(s_cxeBaseDir))
    {
        parentDirNameSrc = AlignerPackagePageHandler.getRelativePath(
            s_cxeBaseDir, parentDirSrc);
    }
}

String parentDirNameTrg = "";
if (parentDirTrg != null && !parentDirTrg.equals(baseDirParentFileTrg))
{
    if (!parentDirTrg.equals(s_cxeBaseDir))
    {
        parentDirNameTrg = AlignerPackagePageHandler.getRelativePath(
            s_cxeBaseDir, parentDirTrg);
    }
}
%>
<%!
static private Logger category =
    Logger.getLogger(
        AlignerPackagePageHandler.class);

// prepare the folder that has been selected to be displayed.
private void prepareFolderListing(HttpServletRequest p_request,
    SessionManager p_sessionMgr, String p_selectFileURL, File p_baseDir,
    String p_folderSelected, boolean p_source, AlignerPackageOptions p_options,
    JspWriter out, File s_cxeBaseDir)
{
    try
    {
        ImportFileFilter filter = new ImportFileFilter(
            p_options.getExtensions());
        File[] directoryContent = p_baseDir.listFiles(filter);
        ArrayList folderList = new ArrayList();
        ArrayList fileList = new ArrayList();
        boolean atTopLevelDocsDir = false;

        if (p_baseDir.getPath().equals(s_cxeBaseDir.getPath()))
        {
            atTopLevelDocsDir = true;
        }

        // Place the directory content under the folder list or the
        // file list for proper sequencing.
        for (int i = 0; i < directoryContent.length; i++)
        {
            File curDoc = directoryContent[i];

            if (curDoc.isFile())
            {
                if (atTopLevelDocsDir)
                {
                    //do not add files at the top level
                    category.warn("Ignoring file " + curDoc +
                        " since it is not under a locale specific subdirectory.");
                }
                else
                {
                    fileList.add(curDoc);
                }
            }
            else
            {
                folderList.add(curDoc);
            }
        }

        int rowCount = 0;

        // Print out the folders first.
        for (int i = 0; i < folderList.size(); i++)
        {
            getFolderString((File)folderList.get(i), p_selectFileURL,
                out, p_folderSelected, p_source, s_cxeBaseDir);
            rowCount++;
        }

        // Print out the files last.
        for (int i = 0; i < fileList.size(); i++)
        {
            getDocumentString((File)fileList.get(i), p_selectFileURL,
                out, p_folderSelected, p_source, s_cxeBaseDir);
            rowCount++;
        }
    }
    catch (Exception e)
    {
        category.error("Unable to read files for the aligner file selection UI. The docs directory setting or the permissions on that directory may be incorrect.");
    }
}

private void getFolderString(File p_document, String p_selectFileURL,
    JspWriter out, String p_folderSelected, boolean p_source, File s_cxeBaseDir)
    throws IOException
{
    String fileName = AlignerPackagePageHandler.getRelativePath(
        s_cxeBaseDir, p_document);
    String fileNameValueEscaped = EditUtil.toJavascript(fileName);

    String displayNameValue = AlignerPackagePageHandler.getRelativePath(
        p_document.getParentFile(), p_document);

    out.println("<TR VALIGN=TOP>");
    out.println("<TD>&nbsp;</TD>");
    out.print("<TD STYLE='padding-top: 2px'><A CLASS='standardHREF' ");
    out.print("HREF=\"javascript:navigateDirectories");
    if (p_source)
    {
      out.print("Src");
    }
    else
    {
      out.print("Trg");
    }
    out.print("('");
    out.print(fileNameValueEscaped);
    out.print("')\">");
    out.print("<IMG SRC='/globalsight/images/folderclosed.gif' ");
    out.println("BORDER=0 HEIGHT=13 WIDTH=15></A></TD>");
    out.print("<TD COLSPAN=2><A CLASS='standardHREF' ");
    out.print("HREF=\"javascript:navigateDirectories");
    if (p_source)
    {
      out.print("Src");
    }
    else
    {
      out.print("Trg");
    }
    out.print("('");
    out.print(fileNameValueEscaped);
    out.print("')\">");
    out.print(displayNameValue);
    out.println("</A></TD>");
    out.println("</TR>");
}

private void getDocumentString(File p_document, String p_selectFileURL,
    JspWriter out, String p_folderSelected, boolean p_source, File s_cxeBaseDir)
    throws IOException
{
    String fileName = AlignerPackagePageHandler.getRelativePath(
        s_cxeBaseDir, p_document);

    out.println("<TR VALIGN=TOP>\n");
    out.print("<TD><INPUT TYPE=CHECKBOX NAME=file VALUE=\"");
    out.print(fileName);
    out.print("\" ");
    if (p_source)
    {
      out.print("src='true'");
    }
    else
    {
      out.print("trg='true'");
    }
    out.print("></TD>");
    out.print("<TD STYLE='padding-top: 2px'>");
    out.print("<IMG SRC='/globalsight/images/file.gif' HEIGHT=15 WIDTH=13>");
    out.println("</TD>");
    out.print("<TD COLSPAN=2 STYLE='word-wrap: break-word;'>");
    out.print(AlignerPackagePageHandler.getRelativePath(
        p_document.getParentFile(), p_document));
    out.println("</TD>");
    out.println("</TR>");
}
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<!-- JSP file: createPackage2.jsp -->
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = true;
var objectName = "Alignment Package";
var guideNode = "aligner";
var helpFile = "<%=bundle.getString("help_align_createPackage2")%>";

function submitForm(action)
{
    form = document.importFilesForm;

    if (action == 'next')
    {
       form.action = "<%=urlNext%>";
    }
    else if (action == 'previous')
    {
       form.action = "<%=urlPrevious%>";
    }
    else if (action == 'cancel')
    {
       if (confirmJump())
       {
          form.action = "<%=urlCancel%>";
       }
       else
       {
          return false;
       }
    }

    form.submit();
}

function addFilePair()
{
    var message = "Please select one source and one target file.";
    var form = document.availableFilesForm;

    // Quick check to see if anything is displayed/checked.
    if (!form.file || !form.file.length)
    {
        alert(message);
        return false;
    }

    var srcFile = null;
    var trgFile = null;

    // Loop through the array to find the checked source & target files
    for (var i = 0; i < form.file.length; i++)
    {
        var file = form.file[i];

        if (file.getAttribute("src") && file.checked)
        {
            if (srcFile)
            {
                alert(message);
                return false;
            }

            srcFile = file.value;
        }
    }

    for (var i = 0; i < form.file.length; i++)
    {
        var file = form.file[i];

        if (file.getAttribute("trg") && file.checked)
        {
            if (trgFile)
            {
                alert(message);
                return false;
            }

            trgFile = file.value;
        }
    }

    if (!srcFile || !trgFile)
    {
        alert(message);
        return false;
    }

    form.filePair.value = srcFile + "|" + trgFile;
    form.submit();
}

function removeFilePair(fileAction)
{
    var message = "Please select a file pair.";
    var form = document.selectedFilesForm;

    if (!form.file)
    {
        alert(message);
        return false;
    }

    var pairs = "";
    // Loop through the array to find the checked source & target files
    if (form.file.length)
    {
      for (i = 0; i < form.file.length; i++)
      {
         if (form.file[i].checked == true)
         {
            if (pairs != "")
            {
               pairs += "|"; // must add delimiter
            }

            pairs += form.file[i].value;
         }
      }
    }
    // If only one radio button is displayed, there is no
    // radio button array, so just check if the single
    // radio button is checked
    else
    {
      if (form.file.checked == true)
      {
         pairs = form.file.value;
      }
    }

    if (!pairs)
    {
        alert(message);
        return false;
    }

    form.filePair.value = pairs;
    form.submit();
}

function navigateDirectoriesSrc(folder)
{
   navigateDirectoriesForm.<%=WebAppConstants.GAP_CURRENTFOLDERSRC%>.value = folder;
   navigateDirectoriesForm.submit();
}

function navigateDirectoriesTrg(folder)
{
   navigateDirectoriesForm.<%=WebAppConstants.GAP_CURRENTFOLDERTRG%>.value = folder;
   navigateDirectoriesForm.submit();
}
//for gbs-2599
$(document).ready(function(){
	$("#selectAll").click(function(){
		$("form[name='selectedFilesForm'] :checkbox[name!='selectAll']").each(function(){
			if($("#selectAll").attr("checked")){
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

.importListSmall {
    position: relative;
    width: 338px;
    height: 135px;
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

<SPAN CLASS="mainHeading"><%=lb_title%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=600><%=lb_helptext%></TD>
  </TR>
</TABLE>
<BR>

<!-- Empty form handled by navigateDirectories() above.  -->
<FORM NAME="navigateDirectoriesForm" ACTION="<%=urlSelf%>" METHOD="POST">
    <INPUT TYPE="HIDDEN" NAME="<%=WebAppConstants.GAP_ACTION%>"
     VALUE="<%=WebAppConstants.GAP_ACTION_SELECTFILES%>">
    <!-- These are the directory you are in.  -->
    <INPUT TYPE="HIDDEN" NAME="<%=WebAppConstants.GAP_CURRENTFOLDERSRC%>"
     VALUE="<%=folderSelectedSrc%>">
    <INPUT TYPE="HIDDEN" NAME="<%=WebAppConstants.GAP_CURRENTFOLDERTRG%>"
     VALUE="<%=folderSelectedTrg%>">
</FORM>

<!-- File Pair Selection -->
<FORM NAME="availableFilesForm" ACTION="<%=urlSelf%>" METHOD="POST">
    <INPUT TYPE="HIDDEN" NAME="<%=WebAppConstants.GAP_ACTION%>"
     VALUE="<%=WebAppConstants.GAP_ACTION_SELECTFILES%>">
    <!-- This is the action (Add/Remove) being performed on the file list. -->
    <INPUT TYPE="HIDDEN" NAME="fileAction" VALUE="add">
    <!-- This is the selected file pair. -->
    <INPUT TYPE="HIDDEN" NAME="filePair" VALUE="">

<!-- Available Source Files -->
<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 80px; LEFT: 0px;">
<SPAN CLASS="standardTextBold"><%=bundle.getString("lb_source_file") %></SPAN>
</DIV>

<DIV CLASS="importListSmall"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 100px; LEFT: 0px; overflow-y: scroll;">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="table-layout: fixed;" WIDTH=320>
    <COL WIDTH=21>   <!-- Checkbox -->
    <COL WIDTH=22>   <!-- Folder/File icon -->
    <COL WIDTH=259>  <!-- File -->
    <COL WIDTH=18>   <!-- Up Folder (for header) -->
    <TR CLASS="tableHeadingBasic" VALIGN="TOP">
      <TD>&nbsp;</TD>
      <TD><IMG SRC="/globalsight/images/folderopen.gif" HEIGHT=13 WIDTH=16 VSPACE=3></TD>
      <TD STYLE="word-wrap: break-word; padding-top: 2px">
        <%=folderSelectedAbsSrc%>
      </TD>
      <TD ALIGN="RIGHT"><%
        boolean atTopSrc = false;
        if (parentDirNameSrc.equals("") && folderSelectedNameSrc.equals(""))
        {
            atTopSrc = true;
        }

        if (atTopSrc)
        {
            out.print("&nbsp;");
        }
        else
        {
            out.print("<A HREF=\"javascript:navigateDirectoriesSrc('");
            out.print(EditUtil.toJavascript(parentDirNameSrc));
            out.print("')\"><IMG SRC=\"/globalsight/images/folderback.gif\" ");
            out.print("BORDER=0 HEIGHT=13 WIDTH=15 VSPACE=3 HSPACE=1></A>");
        }
        %>
      </TD>
    </TR>
    <%
    prepareFolderListing(request, sessionMgr, urlSelf, baseDirSrc,
      folderSelectedSrc, true, gapOptions, out, s_cxeBaseDir);
    %>
  </TABLE>
</DIV>
<!-- End Available Source Files -->

<!-- Available Target Files -->
<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 245px; LEFT: 0px;">
<SPAN CLASS="standardTextBold"><%=bundle.getString("lb_target_file") %></SPAN>
</DIV>

<DIV CLASS="importListSmall"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 265px; LEFT: 0px; overflow-y: scroll;">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="table-layout: fixed;" WIDTH=320>
    <COL WIDTH=21>   <!-- Checkbox -->
    <COL WIDTH=22>   <!-- Folder/File icon -->
    <COL WIDTH=259>  <!-- File -->
    <COL WIDTH=18>   <!-- Up Folder (for header) -->
    <TR CLASS="tableHeadingBasic" VALIGN="TOP">
      <TD>&nbsp;</TD>
      <TD><IMG SRC="/globalsight/images/folderopen.gif" HEIGHT=13 WIDTH=16 VSPACE=3></TD>
      <TD STYLE="word-wrap: break-word; padding-top: 2px">
        <%=folderSelectedAbsTrg%>
      </TD>
      <TD ALIGN="RIGHT"><%
        boolean atTopTrg = false;
        if (parentDirNameTrg.equals("") && folderSelectedNameTrg.equals(""))
        {
            atTopTrg = true;
        }

        if (atTopTrg)
        {
            out.print("&nbsp;");
        }
        else
        {
            out.print("<A HREF=\"javascript:navigateDirectoriesTrg('");
            out.print(EditUtil.toJavascript(parentDirNameTrg));
            out.print("')\"><IMG SRC=\"/globalsight/images/folderback.gif\" ");
            out.print("BORDER=0 HEIGHT=13 WIDTH=15 VSPACE=3 HSPACE=1></A>");
        }
        %>
      </TD>
    </TR>
    <%
    prepareFolderListing(request, sessionMgr, urlSelf, baseDirTrg,
      folderSelectedTrg, false, gapOptions, out, s_cxeBaseDir);
    %>
  </TABLE>
</DIV>
<!-- End Available Target Files -->
</FORM>
<!-- End File Pair Form -->

<!-- Add/Remove buttons in the center -->
<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 6; TOP: 200px; LEFT: 370px;">
<INPUT TYPE="BUTTON" VALUE="&gt;&gt;" onclick="addFilePair()"
  TITLE="<%=bundle.getString("lb_add") %>">
<BR><BR>
<INPUT TYPE="BUTTON" VALUE="&lt;&lt;" onclick="removeFilePair()"
  TITLE="<%=bundle.getString("lb_remove") %>">
</DIV>
<!-- End Add/Remove buttons -->

<!-- Selected Files -->
<FORM NAME="selectedFilesForm" ACTION="<%=urlSelf%>" METHOD="POST">
    <INPUT TYPE="HIDDEN" NAME="<%=WebAppConstants.GAP_ACTION%>"
     VALUE="<%=WebAppConstants.GAP_ACTION_SELECTFILES%>">
    <!-- This is action (Add/Remove) being performed on the file list. -->
    <INPUT TYPE="HIDDEN" NAME="fileAction" VALUE="remove">
    <!-- This is the selected file pair(s). -->
    <INPUT TYPE="HIDDEN" NAME="filePair" VALUE="">

<DIV STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 80px; LEFT: 420px;">
<SPAN CLASS="standardTextBold"><%=bundle.getString("lb_selected_file_pairs") %></SPAN>
</DIV>

<DIV CLASS="importList"
  STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 100px; LEFT: 420px; overflow-y: scroll;">

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="table-layout: fixed;" WIDTH="320">
    <COL WIDTH=21>   <!-- Checkbox -->
    <COL WIDTH=22>   <!-- File icon -->
    <COL WIDTH=277>  <!-- File -->
    <TR CLASS="tableHeadingBasic">
      <TD><input type="checkbox" id="selectAll" name="selectAll"/></TD>
      <TD><IMG SRC="/globalsight/images/file.gif" HEIGHT=15 WIDTH=13 VSPACE=2></TD>
      <TD><%=bundle.getString("lb_file")%></TD>
    </TR>
<%
    SortUtil.sort(selectedFiles);
    int rowCount = 0;
    for (int i = 0, max = selectedFiles.size(); i < max; i++)
    {
        FilePair pair = (FilePair)selectedFiles.get(i);
        String value = pair.getSource() + "|" + pair.getTarget();

        out.println("<TR VALIGN=TOP>");
        out.print("<TD><INPUT TYPE=CHECKBOX NAME=file VALUE=\"");
        out.print(value);
        out.println("\"></TD>");
        out.print("<TD STYLE='padding-top: 2px'>");
        out.print("<IMG SRC='/globalsight/images/file.gif' HEIGHT=15 WIDTH=13>");
        out.println("</TD>");
        out.print("<TD STYLE='word-wrap: break-word;'>");
        out.print(pair.getSource());
        out.print("<b>&nbsp;+</b><BR>");
        out.print(pair.getTarget());
        out.println("</TD>");
        out.println("</TR>");
        rowCount++;
    }
%>
</TABLE>
</DIV>
<!-- End Selected Files -->
</FORM>

<!--for gbs-2599
DIV CLASS="standardText"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 400px; LEFT: 420px;">
<A CLASS="standardHREF" HREF="#" onclick="checkAll('selectedFilesForm')"
 ><%=bundle.getString("lb_check_all")%></A> |
<A CLASS="standardHREF" HREF="#" onclick="clearAll('selectedFilesForm')"
 ><%=bundle.getString("lb_clear_all")%></A>
</DIV-->

<!-- Navigation Buttons -->
<!-- <DIV ALIGN="RIGHT" STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 415px; LEFT: 420px; WIDTH: 338"> -->
<DIV ALIGN="LEFT"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 7; TOP: 415px; LEFT: 0px; WIDTH: 338">
<SPAN CLASS="standardTextBold">

<FORM METHOD="post" NAME="importFilesForm">
  <INPUT TYPE="HIDDEN" NAME="<%=WebAppConstants.GAP_ACTION%>"
   VALUE="<%=WebAppConstants.GAP_ACTION_ALIGNOPTIONS%>">

  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
   onclick="submitForm('cancel');">
  &nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_previous")%>"
   onclick="submitForm('previous');">
  &nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_next")%>"
   onclick="submitForm('next');"
<%
  if (selectedFiles.size() == 0) out.print(" DISABLED");
%>
  >
</FORM>
</DIV>
<!-- End Navigation Buttons -->

</BODY>
</HTML>
