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
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.cxe.entity.fileextension.FileExtensionImpl,
         com.globalsight.everest.company.CompanyWrapper,
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
  String FileExtensionName = (String) sessionMgr.getAttribute("FileExtensionName");
  FileExtensionName = FileExtensionName == null ? "" : FileExtensionName;
  String FileExtensionCName = (String) sessionMgr.getAttribute("FileExtensionCName");
  FileExtensionCName = FileExtensionCName == null ? "" : FileExtensionCName;
  String selfURL = self.getPageURL();
  String emptyMsg  = "msg_no_users";
  
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
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
	else if(feForm.checkboxBtn != null) 
	{   
	    var checkbox = document.getElementsByName("checkboxBtn");
	    if(checkbox.length)
		{

            if (button == "Remove") 
			{
			    if (!confirm('<%=confirmRemove%>'))
			    {    
			        return false;
			    } 
			    var rv="";
			    $(":checkbox:checked").each(
			        function(i){
			        	rv+=$(this).val()+" ";
			        }		
			    )
			    $(":checkbox:checked").each(
			        function(i){
			        	$(this).val(rv);
			        }		
			    )
			    feForm.action = "<%=removeURL%>";
			 }
		 }
	}
    feForm.submit();
    return;
}

function handleSelectAll() {
	var ch = $("#selectAll").attr("checked");
	if (ch == "checked") {
		$("[name='checkboxBtn']").attr("checked", true);
	} else {
		$("[name='checkboxBtn']").attr("checked", false);
	}
	buttonManagement();
}

function buttonManagement()
{
    var count = $("input[name='checkboxBtn']:checked").length;
    if (count != 0)
    {
        $("#removeBtn").attr("disabled", false);
    }
    else
    {
        $("#removeBtn").attr("disabled", true);
    }
}

function filterItems(e) {
	e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
	if (keyCode == 13) {
       feForm.action = "<%=selfURL%>";
       feForm.submit();
	}
}
</SCRIPT>
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
    <table cellpadding=0 cellspacing=0 border=0 class="standardText"  width="100%">
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
                     emptyTableMsg="msg_no_file_profiles" hasFilter="true">
                <amb:column label="checkbox" width="2%">
                    <input type="checkbox" name="checkboxBtn"  id="checkboxBtn"  value="<%=fe.getId()%>"   onClick="buttonManagement()">
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=FileExtensionComparator.NAME%>"
                 filter="FileExtensionName" filterValue="<%=FileExtensionName%>"   width="22%">
                    <%= fe.getName() %>
                </amb:column>
                <amb:column label="" sortBy=""width="40%">
                    &nbsp;
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=FileExtensionComparator.ASC_COMPANY%>"
                filter="FileExtensionCName" filterValue="<%=FileExtensionCName%>">
                    <%=CompanyWrapper.getCompanyNameById(fe.getCompanyId())%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="extensions" key="<%=FileExtensionMainHandler.EXTENSION_KEY%>"  scope="10,20,50,All"  showTotalCount="false"  pageUrl="self" />
          </td>
        </tr>
        <td style="padding-top:5px" align="left">
		    <amb:permission name="<%=Permission.FILE_EXT_NEW%>" >
		            <input type="BUTTON" value="<%=bundle.getString("lb_new")%>"  onClick="submitForm('New');">
		    </amb:permission>
		    <amb:permission name="<%=Permission.FILE_EXT_REMOVE%>" >
		            <input type="BUTTON" name="removeBtn"   id="removeBtn"  disabled value="<%=bundle.getString("lb_remove")%>"  onClick="submitForm('Remove');" >
		    </amb:permission>
        </td>
    </tr>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>

