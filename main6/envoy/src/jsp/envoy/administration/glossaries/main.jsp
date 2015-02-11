<%@page import="com.globalsight.ling.common.URLEncoder"%><%@page import="java.io.File"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.glossaries.GlossaryFile,
            com.globalsight.everest.glossaries.GlossaryUpload,
            com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryConstants,
            com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState,
            com.globalsight.util.AmbFileStoragePathUtils,
            java.util.ArrayList,
            java.util.Iterator,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="delete" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="update" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

GlossaryState state =
  (GlossaryState)session.getAttribute(WebAppConstants.GLOSSARYSTATE);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

session.removeAttribute("Comparator");
// Allow translator to get back to the activity's upload screen.
// The activity's upload screen is not switching the "UI activity"
// ("/globalsight/ControlServlet?activityName=") so the sessionMgr still holds
// the data of the current activity and we can go back to it.
boolean b_calledFromActivityPage = false;
boolean b_calledFromSimpleUploadPage = false;
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
if (sessionMgr.getAttribute(WebAppConstants.WORK_OBJECT) != null)
{
  b_calledFromActivityPage = true;
}
if(sessionMgr.getAttribute(WebAppConstants.UPLOAD_ORIGIN) != null)
{
    b_calledFromSimpleUploadPage = true;
}
String title = bundle.getString("lb_supportFiles");

String url_upload  = upload.getPageURL();
String url_refresh = refresh.getPageURL();
String url_delete  = delete.getPageURL();

// sorting urls
String url_sort_src = url_refresh + "&" + GlossaryConstants.SORT + "=1";
String url_sort_trg = url_refresh + "&" + GlossaryConstants.SORT + "=2";
String url_sort_cat = url_refresh + "&" + GlossaryConstants.SORT + "=3";
String url_sort_nam = url_refresh + "&" + GlossaryConstants.SORT + "=4";

String url_activityUploadPage =
  "/globalsight/ControlServlet?linkName=upload&pageName=TK2";
String url_offlineUploadPage =
  "/globalsight/ControlServlet?activityName=simpleofflineupload";

String lb_fileNotSelected = bundle.getString("jsmsg_file_not_selected");
String lb_duuudeDoYouWantToDoThis = bundle.getString("msg_remove_glossary_file");
String lb_sourceLocale = bundle.getString("lb_source_locale");
String lb_targetLocale = bundle.getString("lb_target_locale");
String lb_category = "Category";
String lb_fileName = bundle.getString("lb_filename");
String lb_upload = bundle.getString("lb_upload");
String lb_remove = bundle.getString("lb_remove");
String lb_return_to_activity = bundle.getString("lb_return_to_activity");
String lb_return_to_offlineUpload = bundle.getString("lb_return_to_offlineUpload");

%>
<HTML>
<!-- administration/glossaries/main.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/modalDialog.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "supportFiles";
var helpFile = "<%=bundle.getString("help_support_files_main_screen")%>";

function loadPage() 
{
   // Load the Guide
   loadGuides();

   if (deleteForm.<%=lb_remove%>)
   {
       // Only show the Remove button if something is available to download
       if (deleteForm.<%=GlossaryConstants.FILE_CHECKBOXES%>) 
       {
           deleteForm.<%=lb_remove%>.disabled = false;
       }
       else
       {
          deleteForm.<%=lb_remove%>.disabled = true;
       }
    }
}

function returnToActivity()
{
   window.location.href = "<%=url_activityUploadPage%>";
}

function returnToOfflineUpload()
{
	window.location.href = "<%=url_offlineUploadPage%>";
}

function optionTest(formSent)
{
    var pageChecked = false;

    if (formSent.<%=GlossaryConstants.FILE_CHECKBOXES%>.value)
    {
        if (formSent.<%=GlossaryConstants.FILE_CHECKBOXES%>.checked)
        {
            pageChecked = true;
        }
    }
    else
    { 
        for (var i = 0;
             i < formSent.<%=GlossaryConstants.FILE_CHECKBOXES%>.length; i++)
        {
            if (formSent.<%=GlossaryConstants.FILE_CHECKBOXES%>[i].checked == true)
            {
                pageChecked = true;
                break;
            }
        }
    }

    if (!pageChecked)
    {
        alert("<%=EditUtil.toJavascript(lb_fileNotSelected)%>");
        return(false);
    }

    return confirm("<%=EditUtil.toJavascript(lb_duuudeDoYouWantToDoThis)%>");
}

function submitForm(form)
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.deleteForm;
    }
    else
    {
        theForm = document.all.deleteForm;
    }

    if (optionTest(theForm))
    {
        theForm.submit();
    }
}

//for GBS-2599
function handleSelectAll() {
	if (deleteForm && deleteForm.selectAll) {
		if (deleteForm.selectAll.checked) {
			checkAllWithName('deleteForm', '<%=GlossaryConstants.FILE_CHECKBOXES%>'); 
	    }
	    else {
			clearAll('deleteForm'); 
	    }
	}
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onload="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=bundle.getString("helper_text_support_files")%></TD>
  </TR>
</TABLE>
<P></P>

<FORM action="<%=url_delete%>" METHOD="post" NAME="deleteForm">
<INPUT TYPE="hidden" NAME="<%=GlossaryConstants.DELETE%>" VALUE=""></INPUT>
<TABLE BORDER="0" CELLPADDING="3" CELLSPACING="0">
  <TR CLASS="tableHeadingBasic">
    <TD><input type="checkbox" onclick="handleSelectAll()" name="selectAll"></TD>
    <TD>
      <A CLASS="sortHREFWhite" HREF="<%=url_sort_src%>" onclick-"<%session.setAttribute("Comparator", state.getComparator());%>"><%=lb_sourceLocale%></A>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    </TD>
    <TD>
      <A CLASS="sortHREFWhite" HREF="<%=url_sort_trg%>" onclick-"<%session.setAttribute("Comparator", state.getComparator()); %>"><%=lb_targetLocale%></A>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    </TD>
    <TD >
      <A CLASS="sortHREFWhite" HREF="<%=url_sort_nam%>" onclick-"<%session.setAttribute("Comparator", state.getComparator()); %>"><%=lb_fileName%></A>
    </TD>
  </TR>
<%
// insert filename rows into the table
ArrayList glossaries = state.getGlossaries();
StringBuilder sb = null;
if (glossaries != null)
{
    for (int i = 0, max = glossaries.size(); i < max; ++i)
    {
        GlossaryFile file = (GlossaryFile)glossaries.get(i);

        out.print("<TR BGCOLOR='");
	if (i % 2 == 0)
	{
	  out.print("#FFFFFF");
	}
	else
	{
	  out.print("#EEEEEE");
	}
	out.print("'>");

	// Col 1: checkbox
        out.print("<TD><INPUT TYPE='checkbox' CLASS='standardText' ");
	out.print("NAME='");
	out.print(GlossaryConstants.FILE_CHECKBOXES);
	out.print("' VALUE='");
	out.print(i);
	out.print("'>");
	out.println("</INPUT></TD>");

	// Col 2: source locale
        out.print("<TD class='standardText'><SPAN title=\"");
        if(file.isForAnySourceLocale())
        {
            out.print(file.getGlobalSourceLocaleName());
        }
        else
        {
            out.print(file.getSourceLocale().getDisplayName());
        }
	out.print("\">");
        if(file.isForAnySourceLocale())
        {
            out.print(file.getGlobalSourceLocaleName());
        }
        else
        {
            out.print(file.getSourceLocale().toString());
        }
        out.println("</SPAN></TD>");

	// Col 3: target locale
	out.print("<TD class='standardText'><SPAN title=\"");
        if(file.isForAnyTargetLocale())
        {
            out.print(file.getGlobalTargetLocaleName());
        }
        else
        {
            out.print(file.getTargetLocale().getDisplayName());
        }
	out.print("\">");
        if(file.isForAnyTargetLocale())
        {
            out.print(file.getGlobalTargetLocaleName());
        }
        else
        {
            out.print(file.getTargetLocale().toString());
        }
        out.println("</SPAN></TD>");

	// Col 4: category
	// out.print("<TD class='standardText'>");
        // out.print(EditUtil.encodeHtmlEntities(file.getCategory()));
        // out.println("</TD>");

	// Col 5: filename (as link)
        out.print("<TD>");
        sb = new StringBuilder("/globalsight/");
        sb.append(AmbFileStoragePathUtils.SUPPORT_FILES_SUB_DIRECTORY).append(File.separator);
        
	      out.print("<A class='standardHREF' target='_blank' href='");
        if(file.isForAnySourceLocale())
        {
            sb.append(file.getGlobalSourceLocaleName());
        }
        else
        {
            sb.append(file.getSourceLocale().toString());
        }
        sb.append(File.separator);
        if(file.isForAnyTargetLocale())
        {
            sb.append(file.getGlobalTargetLocaleName());
        }
        else
        {
            sb.append(file.getTargetLocale().toString());
        }
        sb.append(File.separator).append(file.getFilename().replace("'","%27"));
        out.print(URLEncoder.encodeUrlStr(sb.toString()));
	out.print("'>");
        out.print(EditUtil.encodeHtmlEntities(file.getFilename()));
        out.print("</A>");
        out.println("</TD>");

        out.println("</TR>");
    }
}
%>

  <TR>
    <TD COLSPAN=4 ALIGN="RIGHT"></TD>
  </TR>

  <TR>
    <TD COLSPAN=4 ALIGN="RIGHT">
    <amb:permission name="<%=Permission.SUPPORT_FILES_REMOVE%>" >
      <INPUT TYPE="BUTTON" NAME="<%=lb_remove%>" VALUE="<%=lb_remove%>" 
      onclick="submitForm()">&nbsp;
    </amb:permission>
    <amb:permission name="<%=Permission.SUPPORT_FILES_UPLOAD%>" >
      <INPUT TYPE="BUTTON" NAME="<%=lb_upload%>" VALUE="<%=lb_upload%>..." 
      onclick="createModalDialog('<%=url_upload%>',600,260,200,200)">  
    </amb:permission>
    </TD>
  </TR>
  
<%
if (b_calledFromActivityPage)
{ // Allow translator to get back to the activity's upload screen.
%>
  <TR>
    <TD COLSPAN=4 ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" VALUE="<%=lb_return_to_activity%>" 
      onclick="returnToActivity()">
    </TD>
  </TR>
<%
}
else if(b_calledFromSimpleUploadPage)
{ 
%>
  <TR>
    <TD COLSPAN=4 ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" VALUE="<%=lb_return_to_offlineUpload%>" 
      onclick="returnToOfflineUpload()">
    </TD>
  </TR>
<% } %>
</TABLE>
<P>
</FORM>
</DIV>
</BODY>
</HTML>
