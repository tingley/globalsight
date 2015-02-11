<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.cxe.entity.dbconnection.DBConnectionImpl,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.util.comparator.DBConnectionComparator, 
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.webapp.pagehandler.administration.config.dbconnection.DBConnectionConstants,
         java.util.Locale, java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="edit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="test" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="dbConnections" class="java.util.ArrayList" scope="request"/>

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String newURL = new1.getPageURL() + "&action=" + DBConnectionConstants.NEW;
  String editURL = edit.getPageURL() + "&action=" + DBConnectionConstants.EDIT;
  String testURL = test.getPageURL() + "&action=" + DBConnectionConstants.TEST;
  String title= bundle.getString("lb_db_connections");
  String helperText = bundle.getString("helper_text_db_connections");
  String results = (String)request.getAttribute("testResults");
  String id = (String)request.getAttribute("id");
  long dbId = 0;
  if (id != null)
     dbId = Long.parseLong(id);
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
var guideNode = "dbConnections";
var helpFile = "<%=bundle.getString("help_db_connections_main_screen")%>";

function submitForm(button)
{
    if (button == "New")
    {
        dbForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(dbForm.radioBtn);
        if (button == "Edit")
        {
            dbForm.action = "<%=editURL%>" + "&id=" + value;
        } else if (button == "Test") {
            dbForm.action = "<%=testURL%>" + "&id=" + value;
        }
    }
    dbForm.submit();
    return;

}

function enableButtons()
{
    dbForm.editBtn.disabled = false;
    dbForm.testBtn.disabled = false;
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
<form name="dbForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="dbConnections"
                 key="<%=DBConnectionConstants.DB_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="dbConnections" id="dbConnection"
                     key="<%=DBConnectionConstants.DB_KEY%>"
                     dataClass="com.globalsight.cxe.entity.dbconnection.DBConnectionImpl" 
                     pageUrl="self"
                     emptyTableMsg="msg_no_db_connections" >
                <amb:column label="">
                    <% if (dbId == dbConnection.getId()) { %>
                    <input type="radio" name="radioBtn" value="<%=dbConnection.getId()%>"
                        onclick="enableButtons()" checked>
                    <% } else { %>
                    <input type="radio" name="radioBtn" value="<%=dbConnection.getId()%>"
                        onclick="enableButtons()" >
                    <% } %>
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=DBConnectionComparator.NAME%>"
                    width="150px">
                    <%= dbConnection.getName() %>
                </amb:column>
                <amb:column label="lb_driver"
                     sortBy="<%=DBConnectionComparator.DRIVER%>"
                     width="200">
                     <% out.print(dbConnection.getDriver() == null ?
                         "" : dbConnection.getDriver()); %>
                </amb:column>
                <amb:column label="lb_connection"
                     sortBy="<%=DBConnectionComparator.CONNECTION%>"
                     width="300">
                     <% out.print(dbConnection.getConnection() == null ?
                         "" : dbConnection.getConnection()); %>
                </amb:column>
                <amb:column label="lb_user_name"
                     sortBy="<%=DBConnectionComparator.USERNAME%>"
                     width="150">
                     <% out.print(dbConnection.getUserName() == null ?
                         "" : dbConnection.getUserName()); %>
                </amb:column>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
        <% if (results != null) { %>
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_test")%>"
            name="testBtn" onClick="submitForm('Test');">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
            name="editBtn" onClick="submitForm('Edit');">
        <% } else { %>
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_test")%>"
            name="testBtn" disabled onClick="submitForm('Test');">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
            name="editBtn" disabled onClick="submitForm('Edit');">
        <% } %>
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
            onClick="submitForm('New');">
    </td>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
<script>
<%
    if (results != null)
    {
%>
        alert("<%=results%>");
<%  } %>
</script>
</FORM>
</BODY>
</HTML>

