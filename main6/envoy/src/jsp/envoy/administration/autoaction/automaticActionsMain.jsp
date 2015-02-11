<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.autoactions.*,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.util.comparator.AutoActionsComparator, 
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
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
		
    String newURL = new1.getPageURL() + "&action=create"; 
    String modifyURL = edit.getPageURL() + "&action=modify" ;
    String removeURL = remove.getPageURL() + "&action=remove" ;
    String title = bundle.getString("lb_autoactions");
    String helperText= bundle.getString("helper_text_autoaction");
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
var guideNode = "autoAction";
var helpFile = "#";

function submitForm(button)
{
    if (button == "New")
    {
        cvsserverForm.action = "<%=newURL%>";
    }
    else 
    {
        value = getRadioValue(cvsserverForm.id);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
        
        if (button == "Edit")
        {
        	cvsserverForm.action = "<%=modifyURL%>" + "&id=" + value;
        } else 
            cvsserverForm.action = "<%=removeURL%>" + "&id=" + value;
    }
    
    cvsserverForm.submit();
    return;
}

function enableButtons()
{
    if (cvsserverForm.removeBtn)
    	cvsserverForm.removeBtn.disabled = false;
    if (cvsserverForm.editBtn)
    	cvsserverForm.editBtn.disabled = false;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />

<form name="cvsserverForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="actionList" key="actionKey" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="actionList" id="autoAction" key="actionKey"
       dataClass="com.globalsight.everest.autoactions.AutoAction" pageUrl="self"
       emptyTableMsg="msg_no_autoactions" >
      <amb:column label="">
      <input type="radio" name="id" value="<%=autoAction.getId()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=AutoActionsComparator.NAME%>"
       width="150px">
      <%= autoAction.getName() %>
      </amb:column>
      <amb:column label="lb_email" sortBy="<%=AutoActionsComparator.MAILADDRESS%>"
        width="200px">
      <% out.print(autoAction.getEmail()); %>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=AutoActionsComparator.DESCRIPTION%>" 
       width="200px">
      <% out.print(autoAction.getDescription()); %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.AUTOMATIC_ACTIONS_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.AUTOMATIC_ACTIONS_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..." 
      name="removeBtn" onclick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.AUTOMATIC_ACTIONS_NEW%>" >
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
        alert("<%=bundle.getString("jsmsg_autoActions_remove_warning")%>");
    <%
        }
    }
    %>
</SCRIPT>
</BODY>
</html>


