<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.cvsconfig.*,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants,  
      com.globalsight.everest.util.comparator.CVSServerComparator, 
      java.util.ArrayList,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cvsservers" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
 
    cvsservers = (ArrayList)sessionMgr.getAttribute(CVSConfigConstants.CVS_SERVERS);
    String newURL = new1.getPageURL() + "&action=" + CVSConfigConstants.CREATE;
    String modifyURL = modify.getPageURL() + "&action=" + CVSConfigConstants.UPDATE;
    String removeURL = remove.getPageURL() + "&action=" + CVSConfigConstants.REMOVE;
    String title = bundle.getString("lb_cvsservers");
    String helperText= bundle.getString("helper_text_cvsserver");

    String cvsMsg = (String)sessionMgr.getAttribute("cvsmsg");
    ArrayList<String> existModules = (ArrayList<String>)sessionMgr.getAttribute("existModules");
    sessionMgr.setAttribute("cvsmsg", null);
    sessionMgr.setAttribute("existModules", null);
    if (existModules != null && existModules.size() > 0) {
         cvsMsg += "\\n";
    		for (String m : existModules) {
    			cvsMsg += "\\n" + m;	
    		}
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
var guideNode = "cvsserver";
var helpFile = "#";

function submitForm(button)
{
    if (button == "New")
    {
        cvsserverForm.action = "<%=newURL%>";
    }
    else 
    {
        value = getRadioValue(cvsserverForm.radioBtn);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
        if (button == "Edit")
        {
            if (confirm("<%=bundle.getString("jsmsg_cvs_server_edit_confirm") %>"))
        		cvsserverForm.action = "<%=modifyURL%>" + "&id=" + value;
       		else
           		return;
        } else {
            if (confirm("<%=bundle.getString("jsmsg_remove_confirm")%>"))
        		cvsserverForm.action = "<%=removeURL%>" + "&id=" + value;
       		else
           		return;
        }
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
      <amb:tableNav bean="cvsservers" key="<%=CVSConfigConstants.CVS_SERVER_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="cvsservers" id="cvsserver"
       key="<%=CVSConfigConstants.CVS_SERVER_KEY%>"
       dataClass="com.globalsight.everest.cvsconfig.CVSServer" pageUrl="self"
       emptyTableMsg="msg_no_cvsserver" >
      <amb:column label="">
      <input type="radio" name="radioBtn" value="<%=cvsserver.getId()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=CVSServerComparator.NAME%>" width="150px">
      	<%= cvsserver.getName() %>
      </amb:column>
      <amb:column label="lb_cvs_protocol" sortBy="<%=CVSServerComparator.PROTOCOL%>" width="100px">
      	<%=cvsserver.getProtocol() == 1 ? "Ext" : "Pserver"%>
      </amb:column>
      <amb:column label="lb_cvs_hostip" sortBy="<%=CVSServerComparator.HOST_IP%>" width="100px">
      	<%=cvsserver.getHostIP() == null ? "" : cvsserver.getHostIP() %>
      </amb:column>
      <amb:column label="lb_cvs_repository" sortBy="<%=CVSServerComparator.REPOSITORY%>" width="100px">
      	<%=cvsserver.getRepository() %>
      </amb:column>
      <amb:column label="lb_cvs_repository_username" sortBy="<%=CVSServerComparator.LOGIN_USER%>" width="100px">
      	<%=cvsserver.getLoginUser() %>
      </amb:column>
      <amb:column label="lb_cvs_sandbox"  width="300px">
      	<%=cvsserver.getSandbox() == null ? "" : cvsserver.getSandbox() %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.CVS_Servers_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_Servers_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..."
       name="removeBtn" onclick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_Servers_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
</BODY>
<script language="JavaScript">
if ("<%=cvsMsg%>" != "null") {
	alert("<%=cvsMsg%>");
}
</script>
</html>


