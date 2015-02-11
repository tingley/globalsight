<%@ page contentType="text/html; charset=UTF-8"
	 errorPage="/envoy/common/error.jsp"
	 import="com.globalsight.cxe.entity.fileprofile.FileProfileImpl,
                 com.globalsight.cxe.util.CxeProxy, 
                 com.globalsight.everest.servlet.util.ServerProxy, 
                 com.globalsight.everest.util.system.SystemConfiguration,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.mediasurface.CmsUserInfo,
                 java.util.*"
                 session="true"
%>
<jsp:useBean id="fileProfiles" class="java.util.Vector" scope="request" />
<jsp:useBean id="jobNamePrompt" class="java.util.Vector" scope="request" />
<%!
    static String s_cmsContentServerUrl = "";
    static String s_cmsContentServerName = "";
    static String s_cmsContentServerPort = "";
    
    static {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_cmsContentServerUrl = sc.getStringParameter(sc.CMS_CONTENT_SERVER_URL);
            s_cmsContentServerName = sc.getStringParameter(sc.CMS_CONTENT_SERVER_NAME);
            s_cmsContentServerPort = sc.getStringParameter(sc.CMS_CONTENT_SERVER_PORT);
        }
        catch (Throwable e)
        {
            // failed to get cms content server info
        }        
    } 
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    response.setHeader("Pragma", "No-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-cache");
    
    try
    {
      fileProfiles = new Vector (ServerProxy.
      getFileProfilePersistenceManager().getAllFileProfiles());
    }
    catch (Exception ne)
    {
    }
                                     
    String mainHeading = null;
    
    // get request parameters                 
    String itemKey = (String)request.getParameter("itemKey");
    String[] str = itemKey.split(",");
    String gsUserId = (String)request.getParameter("gsId");
    String jobName = (String)request.getParameter("jobName");
    String fpId = (String)request.getParameter("fileProfile");
    
    boolean needUserInfo = gsUserId == null || gsUserId.length() == 0;
    boolean readyForImport = jobName == null && fpId == null;    
    CmsUserInfo cmsUserInfo = null;
    if (!needUserInfo)
    {
      try
      {
         cmsUserInfo = ServerProxy.getCmsUserManager().
            findCmsUserInfo(gsUserId);
      }
      catch (Exception e)
      {      
         // failed to get cms info, so let user enter the values
         needUserInfo = true;
      }
    }
    
    String cmsUsername = null;
    String cmsPassword = null;
    if (!readyForImport)
    {
      mainHeading = bundle.getString("lb_cms_import_started");
      if (needUserInfo)
      {
         cmsUsername = (String)request.getParameter("userName");
         cmsPassword = (String)request.getParameter("password");
      }
      else
      {
         cmsUsername = cmsUserInfo.getCmsUserId();
         cmsPassword = cmsUserInfo.getCmsPassword();         
      }
      
      String batchId = jobName + System.currentTimeMillis();
      int pageCount = str.length;
      for (int i = 0; i < pageCount; i++)
      {
         try
         {         
           int msItemKey = Integer.parseInt(str[i]);
           CxeProxy.importFromMediasurface(
           msItemKey,
           s_cmsContentServerUrl,
           s_cmsContentServerName,
           s_cmsContentServerPort,
           cmsUsername,
           cmsPassword,
           jobName,
           batchId,
           i+1, // pageNum
           pageCount,
           1, // docPageNum
           1, // docPageCount
           fpId,
           false,
           CxeProxy.IMPORT_TYPE_L10N);
         }
         catch (Exception e)
         {
           // do nothing      
         }   
      }       
    }
    else
    {
      mainHeading = bundle.getString("lb_enter_job_info");
    }
%>


<HTML>
<HEAD>
<TITLE><%= bundle.getString("lb_cms_import") %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

// needNameArray to be filled in dynamically from localization profile
// with jobsize being batch (true) or determined by wordcount (false)
var needNameArray = new Array();
<%
    for(int i = 0; i < jobNamePrompt.size(); i++)
    {
%>
needNameArray[<%= i %>] = <%= jobNamePrompt.elementAt(i) %>;
<%
    }
%>

function needNameCheck(selectedOption) {
	//if ((selectedOption != -1) && (needNameArray[selectedOption])) {
    if ((selectedOption != -1)) {
		if (document.layers)
document.contentLayer.document.variableLayer.visibility = "show";
		else variableLayer.style.visibility = "visible";
	}
	else {
		if (document.layers)
document.contentLayer.document.variableLayer.visibility = "hide";
		else variableLayer.style.visibility = "hidden";
	}
}

function isReady(form) {

// check for a file profile
	if(form.fileProfile.options[form.fileProfile.selectedIndex].value == -1) {
		alert( "<%=bundle.getString("jsmsg_select_a_file_profile")%>");
		return false;
	}    
    
// check for a job name
	if ("" == form.jobName.value) {
		form.jobName.value = "-1";
        alert( "<%=bundle.getString("jsmsg_naming_job")%>");
		form.jobName.focus();
		return false;
	}
    
// check for a cms username
	if (<%=needUserInfo%> && "" == form.userName.value) {
		form.userName.value = "-1";
        alert( "<%=bundle.getString("jsmsg_cms_username_warning")%>");
		form.userName.focus();
		return false;
	}
    
// check for a cms password
	if (<%=needUserInfo%> && "" == form.password.value) {
		form.password.value = "-1";
        alert( "<%=bundle.getString("jsmsg_cms_password_warning")%>");
		form.password.focus();
		return false;
	}

	form.submit();
}
function submitForm() {
// Move Job Name if necessary
	if (document.layers) {
		if
(document.contentLayer.document.variableLayer.visibility == "show")
document.contentLayer.document.mainForm.jobName.value =
document.contentLayer.document.variableLayer.document.nameForm.jobName.value;
    if (<%=needUserInfo%>)
    {
        document.contentLayer.document.mainForm.userName.value =
        document.contentLayer.document.variableLayer.document.nameForm.userName.value;
        document.contentLayer.document.mainForm.password.value =
        document.contentLayer.document.variableLayer.document.nameForm.password.value;
    }
    else
    {
        document.contentLayer.document.mainForm.userName.value = <%= cmsUsername %>;
        document.contentLayer.document.mainForm.password.value = "<%= cmsPassword %>";
    }

		isReady(document.contentLayer.document.mainForm);
	}
	else {
		if (variableLayer.style.visibility == "visible")
mainForm.jobName.value = nameForm.jobName.value;
        if (<%=needUserInfo%>)
        {
            mainForm.userName.value = nameForm.userName.value;
            mainForm.password.value = nameForm.password.value;
        }
        else
        {
            mainForm.userName.value = <%= cmsUsername %>;
            mainForm.password.value = "<%= cmsPassword %>";
        }
		isReady(mainForm);
	}
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<DIV ID="header" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
  <TR>
    <TD><IMG SRC="/globalsight/images/logo_header.gif" HEIGHT="68" WIDTH="253"><BR>
      <HR NOSHADE SIZE=1> 
    </TD>
  </TR>
</TABLE>
</DIV>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 88px; LEFT: 20px;">

<SPAN CLASS="mainHeading"><%= mainHeading%></SPAN><P>
<%
if (readyForImport)
{
%>
<FORM method=POST onSubmit="return isReady(this)" NAME="mainForm" CLASS=standardText>
<P>
<SPAN CLASS="standardText"><%= bundle.getString("lb_select_file_profile_for_import") %>:</SPAN><BR>
<SELECT NAME=fileProfile onChange="needNameCheck(this.selectedIndex-1);">
   	 <OPTION VALUE=-1><%= bundle.getString("lb_file_profiles") %></OPTION>

    <%
    // get the options for the HTML menu from the "fileProfiles" Vector
    // the options come sorted by name
    Enumeration e = fileProfiles.elements();
    while(e.hasMoreElements())
        {
        FileProfileImpl fileProfile = (FileProfileImpl) e.nextElement();
        long id = fileProfile.getId();
        String name = fileProfile.getName();
    %>
        <OPTION VALUE="<%= id %>" > <%= name %> </OPTION>
    <%
    }
    %>

    </SELECT>

<P>
    <INPUT TYPE=Hidden NAME="jobName" VALUE="-1" >
    <INPUT TYPE=Hidden NAME="userName" VALUE="-1" >
    <INPUT TYPE=Hidden NAME="password" VALUE="-1" >
</FORM>
<P>

<DIV ID="variableLayer" STYLE=" POSITION: RELATIVE; Z-INDEX: 9; TOP: 5px; LEFT: 0px; VISIBILITY: HIDDEN">
<FORM NAME="nameForm" ACTION="javascript:submitForm();" CLASS=standardText>
<SPAN CLASS="standardText"><%= bundle.getString("lb_job_name")%>:</SPAN><BR>
<INPUT TYPE="text" SIZE="30"
NAME="jobName" VALUE="" MAXLENGTH="320"><BR>
<%
   if (needUserInfo)
   {
%>
        <SPAN CLASS="standardText"><%= bundle.getString("lb_cms_username")%>:</SPAN><BR>
        <INPUT TYPE="text" SIZE="30"
        NAME="userName" VALUE="" MAXLENGTH="30"><BR>
        <SPAN CLASS="standardText"><%= bundle.getString("lb_cms_password")%>:</SPAN><BR>
        <INPUT TYPE="password" SIZE="30"
        NAME="password" VALUE="" MAXLENGTH="30">
<% } %>
</FORM>
</DIV>
<%}%>
<P>
<SPAN CLASS="HREFBold">
<INPUT TYPE="BUTTON" NAME="Cancel" VALUE="<%=bundle.getString("lb_close")%>" ONCLICK="window.close();">
<%
if (readyForImport)
{
%>
<INPUT TYPE="BUTTON" NAME="OK" VALUE="OK" ONCLICK="submitForm();">
<%}%>
</SPAN>
</DIV>
</BODY>
</HTML>
