<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.gsedition.*,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.util.comparator.GSEditionComparator, 
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
    String modifyURL = edit.getPageURL() + "&action=modify";
    String removeURL = remove.getPageURL() + "&action=remove" ;
    String title = bundle.getString("lb_gseditions");
    String helperText= bundle.getString("helper_text_gsedition");

    String infoType = (String) request.getAttribute("infoType");
    if (infoType != null) 
    {
        String newOrModify = (String) request.getAttribute("newOrModify");
        if ("wsError".equals(infoType))
        {
        	String wsErrorInfo = (String) request.getAttribute("wsErrorInfo");
        	
        	GSEdition gsEdition = (GSEdition)request.getAttribute("GSEditionSession");
			if ("new".equals(newOrModify))
			{
                newURL = newURL + "&id=" + gsEdition.getId()
		                + "&name=" + gsEdition.getName()
		                + "&hostName=" + gsEdition.getHostName()
		                + "&hostPort=" + gsEdition.getHostPort()
		                + "&userName=" + gsEdition.getUserName()
		                + "&password=" + gsEdition.getPassword()
		                + "&description=" + gsEdition.getDescription()
		                + "&AmbassadorLoginError=" + wsErrorInfo;
                
                response.sendRedirect(newURL);
			}
			else
			{
	            modifyURL = modifyURL + "&id=" + gsEdition.getId()
		                + "&name=" + gsEdition.getName()
		                + "&hostName=" + gsEdition.getHostName()
		                + "&hostPort=" + gsEdition.getHostPort()
		                + "&userName=" + gsEdition.getUserName()
		                + "&password=" + gsEdition.getPassword()
		                + "&description=" + gsEdition.getDescription()
		                + "&AmbassadorLoginError=" + wsErrorInfo;

	            response.sendRedirect(modifyURL);
			}
        }
        else if ("noPermission".equals(infoType))
        {
        	GSEdition gsEdition = (GSEdition)request.getAttribute("GSEditionSession");
        	if ("new".equals(newOrModify))
        	{
                newURL = newURL + "&id=" + gsEdition.getId()
		                + "&name=" + gsEdition.getName()
		                + "&hostName=" + gsEdition.getHostName()
		                + "&hostPort=" + gsEdition.getHostPort()
		                + "&userName=" + gsEdition.getUserName()
		                + "&password=" + gsEdition.getPassword()
		                + "&description=" + gsEdition.getDescription()
		                + "&noPermission=true";
		    	
                response.sendRedirect(newURL);
        	}
        	else
        	{
                modifyURL = modifyURL + "&id=" + gsEdition.getId() 
		                + "&name=" + gsEdition.getName()
		                + "&hostName=" + gsEdition.getHostName()
		                + "&hostPort=" + gsEdition.getHostPort()
		                + "&userName=" + gsEdition.getUserName()
		                + "&password=" + gsEdition.getPassword()
		                + "&description=" + gsEdition.getDescription()
		                + "&noPermission=true";

			    response.sendRedirect(modifyURL);
        	}
        }
    }
    
    if(request.getAttribute("AmbassadorLoginError") != null) {
        if(request.getAttribute("AmbassadorLoginError").equals("true")) {
            GSEdition gsEdition = (GSEdition)request.getAttribute("GSEditionSession");
            modifyURL = modifyURL + "&id=" + gsEdition.getId() 
                        + "&name=" + gsEdition.getName()
                        + "&hostName=" + gsEdition.getHostName()
                        + "&hostPort=" + gsEdition.getHostPort()
                        + "&userName=" + gsEdition.getUserName()
                        + "&password=" + gsEdition.getPassword()
                        + "&description=" + gsEdition.getDescription()
                        + "&AmbassadorLoginError=true";       
            response.sendRedirect(modifyURL);
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
var guideNode = "gsEdition";
var helpFile = "<%=bundle.getString("help_gsEdition_configMain")%>";

function submitForm(button)
{
    if (button == "New")
    {
        GSEditionForm.action = "<%=newURL%>";
    }
    else {
        value = getRadioValue(GSEditionForm.id);
        
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
        
        if (button == "Edit") {
            GSEditionForm.action = "<%=modifyURL%>" + "&gsEditionID=" + value;
        } else {
            GSEditionForm.action = "<%=removeURL%>" + "&gsEditionID=" + value;
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
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />

<form name="GSEditionForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="GSEditionList" key="GSEditonKey" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="GSEditionList" id="gsEdition" key="GSEditonKey"
       dataClass="com.globalsight.everest.gsedition.GSEdition" pageUrl="self"
       emptyTableMsg="msg_no_gseditions" >
      <amb:column label="">
      <input type="radio" name="id" value="<%=gsEdition.getId()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=GSEditionComparator.NAME%>"
       width="100px">
      <%= gsEdition.getName() %>
      </amb:column>
      <amb:column label="lb_host_name" sortBy="<%=GSEditionComparator.HOSTNAME%>"
        width="100px">
      <% out.print(gsEdition.getHostName()); %>
      </amb:column>
      <amb:column label="lb_host_port" sortBy="<%=GSEditionComparator.HOSTPAORT%>" 
       width="80px">
      <% out.print(gsEdition.getHostPort()); %>
      </amb:column>
            <amb:column label="lb_description" sortBy="<%=GSEditionComparator.DESCRIPTION%>" 
       width="200px">
      <% out.print(gsEdition.getDescription()); %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.GSEDITION_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..." 
      name="removeBtn" onclick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.GSEDITION_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.GSEDITION_NEW%>" >
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
        alert("<%=bundle.getString("jsmsg_gsedition_remove_warning")%>");
    <%
        }
    }
    %>

    
    
</SCRIPT>
</BODY>
</html>