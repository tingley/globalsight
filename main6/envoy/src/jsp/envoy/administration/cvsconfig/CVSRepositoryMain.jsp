<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.cvsconfig.*,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants,  
      com.globalsight.everest.util.comparator.CVSRepositoryComparator, 
      java.util.ArrayList,
      java.util.ResourceBundle"
    session="true"
%>

<%@page import="com.globalsight.everest.webapp.WebAppConstants"%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
		
    String newURL = new1.getPageURL() + "&action=" + CVSConfigConstants.CREATE; 
    String modifyURL = modify.getPageURL() + "&action=" + CVSConfigConstants.UPDATE;
    String removeURL = remove.getPageURL() + "&action=" + CVSConfigConstants.REMOVE;
    String title = bundle.getString("lb_cvsrepositroy");
    String helperText= bundle.getString("helper_text_cvsrepository");
    
    String cvsMsg = (String)sessionMgr.getAttribute("cvsmsg");
    sessionMgr.setAttribute("cvsmsg", null);

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
var guideNode = "repository";
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
        	cvsserverForm.action = "<%=modifyURL%>" + "&id=" + value;
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
      <amb:tableNav bean="<%=CVSConfigConstants.CVS_REPOSITORY_LIST%>" 
       key="<%=CVSConfigConstants.CVS_REPOSITORY_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="<%=CVSConfigConstants.CVS_REPOSITORY_LIST%>" id="repository"
       key="<%=CVSConfigConstants.CVS_REPOSITORY_KEY%>"
       dataClass="com.globalsight.everest.cvsconfig.CVSRepository" pageUrl="self"
       emptyTableMsg="msg_no_cvsrepository" >
      <amb:column label="">
      <input type="radio" name="radioBtn" value="<%=repository.getId()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=CVSRepositoryComparator.NAME%>"
       width="150px">
      <%= repository.getName() %>
      </amb:column>
      <amb:column label="lb_cvsservers" sortBy="<%=CVSRepositoryComparator.SERVERNAME%>"
        width="200px">
      <% out.print(repository.getServer().getName()); %>
      </amb:column>
      <amb:column label="lb_cvs_repository_cvs_name" sortBy="<%=CVSRepositoryComparator.REPOSITORY%>" 
       width="200px">
      <% out.print(repository.getRepository()); %>
      </amb:column>
      <amb:column label="lb_cvs_repository_foldername" sortBy="<%=CVSRepositoryComparator.FOLDERNAME%>"
       width="300px">
      <% out.print(repository.getFolderName()); %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.CVS_ADMIN%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..."
       name="removeBtn" onclick="submitForm('Remove');" disabled>
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
if ("<%=cvsMsg%>" != "null")
	alert("<%=cvsMsg%>");
</script>
</html>


