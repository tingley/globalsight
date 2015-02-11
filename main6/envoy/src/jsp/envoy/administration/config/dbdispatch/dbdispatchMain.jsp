<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.cxe.entity.dbconnection.DBDispatchImpl,
         com.globalsight.everest.util.comparator.DBDispatchesComparator,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.pagehandler.administration.config.dbdispatch.DBDispatchMainHandler, 
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         java.util.Locale, java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="edit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="dup" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="dbImports" class="java.util.ArrayList" scope="request"/>

<%
    SessionManager sessionMgr = (SessionManager)
        session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);
    String newURL = new1.getPageURL();
    String editURL = edit.getPageURL() + "&action=edit";
    String dupURL = dup.getPageURL() + "&action=duplicate";
    String title= bundle.getString("lb_db_import_settings");
    String helperText = bundle.getString("helper_text_db_dispatch");

    // Data
    Hashtable dbConnectionPairs = (Hashtable)sessionMgr.getAttribute("dbConnectionPairs");
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
var guideNode = "dbImportSettings";
var helpFile = "<%=bundle.getString("help_db_import_settings_main_screen")%>";

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
        } else if (button == "Duplicate") {
            dbForm.action = "<%=dupURL%>" + "&id=" + value;
        }
    }
    dbForm.submit();
    return;

}
function enableButtons()
{
    dbForm.editBtn.disabled = false;
    dbForm.dupBtn.disabled = false;
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
hi
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="dbForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="dbImports"
                 key="<%=DBDispatchMainHandler.DB_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="dbImports" id="dbImport"
                     key="<%=DBDispatchMainHandler.DB_KEY%>"
                     dataClass="com.globalsight.cxe.entity.dbconnection.DBDispatchImpl"
                     pageUrl="self"
                     emptyTableMsg="msg_no_db_import_profiles" >
                <amb:column label="">
                    <input type="radio" name="radioBtn" value="<%=dbImport.getId()%>"
                        onclick="enableButtons()" >
                </amb:column>
                <amb:column label="lb_name"
                    sortBy="<%=DBDispatchesComparator.NAME%>"
                    width="250px">
                    <%= dbImport.toString() %>
                </amb:column>
                <amb:column label="lb_records_per_page"
                    sortBy="<%=DBDispatchesComparator.PERPAGE%>"
                    width="150">
                    <% out.print(dbImport.getRecordsPerPage()); %>
                </amb:column>
                <amb:column label="lb_pages_per_batch"
                    sortBy="<%=DBDispatchesComparator.PERBATCH%>"
                    width="100">
                    <% out.print(dbImport.getPagesPerBatch()); %>
                </amb:column>
                <amb:column label="lb_db_connections"
                    sortBy="<%=DBDispatchesComparator.CONNECTIONS%>"
                    width="150">
                    <% out.println(dbConnectionPairs.get(
                        new Long(dbImport.getConnectionId()))); %>
                </amb:column>
              </amb:table>
            </td>
         </tr>
         <tr>
         <tr>
    <td style="padding-top:5px" align="right">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_duplicate")%>..."
            name="dupBtn" disabled onClick="submitForm('Duplicate');">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
            name="editBtn" disabled onClick="submitForm('Edit');">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
            onClick="submitForm('New');">
    </td>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>

