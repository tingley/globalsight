<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.gsedition.*,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.util.comparator.GSEditionActivityComparator, 
      java.util.ArrayList,
      java.util.ResourceBundle"
    session="true"
%>

<%@page import="com.globalsight.everest.webapp.WebAppConstants"%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="gsactivity" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
		
    String newURL = new1.getPageURL() + "&action=create"; 
    String modifyURL = edit.getPageURL() + "&action=modify";
    String removeURL = remove.getPageURL() + "&action=remove" ;
    
    String title = bundle.getString("lb_gsedition_actions");
    String helper_text_gsEdition= bundle.getString("helper_text_gsEditionActions");
    HashMap xliffMap = new HashMap();
    
    if(request.getAttribute("xliffMap") != null) {
        xliffMap = (HashMap)request.getAttribute("xliffMap");
    }
%>

<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "gsEditionAction";
var helpFile = "<%=bundle.getString("help_gsEdition_actionMain")%>";

function submitForm(button)
{   
    if (button == "New")
    {
        GSEditionForm.action = "<%=newURL%>" + "&gsID=<%=request.getParameter("GSEditionID")%>";
    }
    else {
        value = getRadioValue(GSEditionForm.id);
    
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
        
        if (button == "Edit")
        {
            GSEditionForm.action = "<%=modifyURL%>" + "&id=" + value + "&gsID=<%=request.getParameter("GSEditionID")%>";
        } else {
            GSEditionForm.action = "<%=removeURL%>" + "&id=" + value + "&GSEditionID=<%=request.getParameter("GSEditionID")%>";
        }
    }
    
    GSEditionForm.submit();
    return;
}

function enableButtons()
{
    if (GSEditionForm.removeBtn)
    	GSEditionForm.removeBtn.disabled = false;
    if (GSEditionForm.editBtn)
    	GSEditionForm.editBtn.disabled = false;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helper_text_gsEdition%>" />

<form name="GSEditionForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="GSEditionActionList" key="GSEditonActionKey" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="GSEditionActionList" id="gsEditionActivity" key="GSEditonActionKey"
       dataClass="com.globalsight.everest.gsedition.GSEditionActivity" pageUrl="self"
       emptyTableMsg="msg_no_gsedition_activity" >
      <amb:column label="">
      <input type="radio" name="id" value="<%=gsEditionActivity.getId()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=GSEditionActivityComparator.NAME%>"
       width="150px">
      <%= gsEditionActivity.getName() %>
      </amb:column>
      <amb:column label="lb_gsEdition_acitivty_fileProfile" sortBy="<%=GSEditionActivityComparator.FILEPROFILE%>"
        width="200px">
      <% out.print(gsEditionActivity.getFileProfileName()); %>
      </amb:column>
      <amb:column label="lb_gsEdition_acitivty_sourcefile" sortBy="<%=GSEditionActivityComparator.SOURCEFILE%>" 
       width="200px">
       <%
       if(gsEditionActivity.getSourceFileReference() == 0) {
           out.print(bundle.getString("lb_no"));
       }
       else {
           out.print(bundle.getString("lb_yes"));
       }
       %>
      </amb:column>
            <amb:column label="lb_description" sortBy="<%=GSEditionActivityComparator.DESCRIPTION%>" 
       width="200px">
      <% out.print(gsEditionActivity.getDescription()); %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.GSEDITION_ACTIONS_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..." 
      name="removeBtn" onclick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.GSEDITION_ACTIONS_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.GSEDITION_ACTIONS_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
      name="newBtn" onclick="submitForm('New');">
    </amb:permission>

    </td>
  </tr>
</table>
</form>
</div>
<SCRIPT>
    <%
    if(request.getAttribute("canBeRemoved") != null) {
        if(request.getAttribute("canBeRemoved").equals("false")) {
    %>
        alert("<%=bundle.getString("jsmsg_gsedition_action_remove_warning")%>");
    <%
        }
    }
    %>
    
    <%
    if(request.getAttribute("noGSEditionVersion") != null) {
        if(request.getAttribute("noGSEditionVersion").equals("true")) {
    %>
        alert("<%=bundle.getString("lb_error_version_GSEdition")%>");
    <%
        }
    }
    %>
    
    <%
    if(request.getAttribute("wsErrorInfo") != null) {
    %>
        alert("<%=request.getAttribute("wsErrorInfo")%>");
    <%
    }
    %>
</SCRIPT>
</BODY>
</html>


