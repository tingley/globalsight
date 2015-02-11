<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.util.comparator.FileExtensionComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.config.fileextension.FileExtensionMainHandler,
         com.globalsight.cxe.entity.fileextension.FileExtensionImpl,
         com.globalsight.everest.servlet.util.ServerProxy,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="remove" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="extensions" class="java.util.ArrayList" scope="request"/>

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String newURL = new1.getPageURL();
  String removeURL = remove.getPageURL() + "&action=remove";
  String title = bundle.getString("lb_file_extensions");                
  String helperText = bundle.getString("helper_text_file_extensions");
  String confirmRemove = bundle.getString("msg_remove_file_extension");
  SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
  String deps = (String)sessionMgr.getAttribute("dependencies");
  
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "fileExtensions";
    var helpFile = "<%=bundle.getString("help_file_extensions_main_screen")%>";
function submitForm(button)
{
    if (button == "New")
    {
	    feForm.action = "<%=newURL%>";
	}
	else if(feForm.radioBtn != null) 
	{
	    var radio = document.getElementsByName("radioBtn");
	    if(radio.length)
		{
	        value = getRadioValue(feForm.radioBtn);
            if (button == "Remove") 
			{
			    if (!confirm('<%=confirmRemove%>'))
			    {
			        return false;
			    } 
			    feForm.action = "<%=removeURL%>" + "&id=" + value;
			 }
		 }
	}
    feForm.submit();
    return;
}

function enableButtons()
{
    if (feForm.removeBtn)
        feForm.removeBtn.disabled = false;
}</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<% if (deps != null) {
    sessionMgr.removeElement("dependencies");
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>
<form name="feForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="extensions" key="<%=FileExtensionMainHandler.EXTENSION_KEY
%>" pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="extensions" id="fe"
                     key="<%=FileExtensionMainHandler.EXTENSION_KEY%>"
                     dataClass="com.globalsight.cxe.entity.fileextension.FileExtensionImpl"
                     pageUrl="self"
                     emptyTableMsg="msg_no_file_profiles" >
                <amb:column label="" width="20px">
                    <input type="radio" name="radioBtn" value="<%=fe.getId()%>" onclick="enableButtons()">
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=FileExtensionComparator.NAME%>"
                    width="250px">
                    <%= fe.getName() %>
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=FileExtensionComparator.ASC_COMPANY%>">
                    <%=ServerProxy.getJobHandler().getCompanyById(Long.parseLong(fe.getCompanyId())).getCompanyName()%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
        <td style="padding-top:5px" align="right">
		    <amb:permission name="<%=Permission.FILE_EXT_NEW%>" >
		            <input type="BUTTON" value="<%=bundle.getString("lb_new")%>"  onClick="submitForm('New');">
		    </amb:permission>
		    <amb:permission name="<%=Permission.FILE_EXT_REMOVE%>" >
		            <input type="BUTTON" name="removeBtn" disabled value="<%=bundle.getString("lb_remove")%>"  onClick="submitForm('Remove');" >
		    </amb:permission>
        </td>
    </tr>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>

