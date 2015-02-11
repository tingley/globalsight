<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.cxe.entity.dbconnection.DBDispatchImpl,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.config.dbdispatch.DBDispatchMainHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.ResourceBundle,
                  java.util.Vector"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String saveURL = save.getPageURL();
    String title = null;
    if (sessionMgr.getAttribute("edit") != null)
    {
        saveURL +=  "&action=edit";
        title = bundle.getString("msg_edit_db_import_settings");
    }
    else
    {
        saveURL +=  "&action=new";
        title = bundle.getString("msg_new_db_import_settings");
    }
    if (sessionMgr.getAttribute("duplicate") != null)
    {
        saveURL +=  "&action=duplicate";
        title = bundle.getString("msg_new_db_import_settings");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + DBDispatchMainHandler.CANCEL;

    // Data
    ArrayList names = (ArrayList)request.getAttribute(DBDispatchMainHandler.NAMES);
    Hashtable dbConnectionPairs = (Hashtable)sessionMgr.getAttribute("dbConnectionPairs");
    String dbname = "";
    String desc = "";
    String recordsTable = "";
    long records = 10;
    long batch = 5;
    long force = 7;
    String connection = "";
    DBDispatchImpl db = (DBDispatchImpl)sessionMgr.getAttribute(DBDispatchMainHandler.DB_KEY);
    if (db != null)
    {
        if (sessionMgr.getAttribute("duplicate") == null)
            dbname = db.getName();
        else
            dbname = "new_name";
        desc = db.getDescription();
        if (desc == null) desc = "";
        recordsTable = db.getTableName();
        records = db.getRecordsPerPage();
        batch = db.getPagesPerBatch();
        force = db.getMaxElapsedMillis() / DBDispatchMainHandler.MILLIS_PER_DAY;
        connection = (String)dbConnectionPairs.get(new Long(db.getConnectionId()));
    }

%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>


<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_db_import_settings")%>";
var guideNode = "dbImportSettings";
var helpFile = "<%=bundle.getString("help_db_import_settings_basic_screen")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        dbForm.action = "<%=cancelURL%>";
        dbForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            dbForm.action = "<%=saveURL%>";
            dbForm.submit();
        }
    }
}

//
// Check required fields.
//
function confirmForm()
{
    if (isEmptyString(stripBlanks(dbForm.nameField.value)))
    {
        alert("<%=bundle.getString("jsmsg_db_import_name")%>");
        dbForm.nameField.focus();
        return false;
    }
    if (isEmptyString(stripBlanks(dbForm.pendingField.value)))
    {
        alert("<%=bundle.getString("jsmsg_db_import_pending")%>");
        dbForm.pendingField.focus();
        return false;
    }
    if (isEmptyString(stripBlanks(dbForm.batchField.value)))
    {
        alert("<%=bundle.getString("jsmsg_db_import_batch")%>");
        dbForm.batchField.focus();
        return false;
    }
    if (isEmptyString(stripBlanks(dbForm.forceField.value)))
    {
        alert("<%=bundle.getString("jsmsg_db_import_force")%>");
        dbForm.forceField.focus();
        return false;
    }
    // check for dups
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String thename = (String)names.get(i);
%>
            if ("<%=thename%>" == dbForm.nameField.value && "<%=thename%>" != "<%=dbname%>")
            {
                alert("<%=bundle.getString("jsmsg_duplicate_db_import")%>");
                return false;
            }
<%
        }
    }
%>

    return true;
}

</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading">
        <%=title%>
    </span>
    <br>
    <br>

<form name="dbForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="nameField" value="<%=dbname%>" >
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_description")%>:
          </td>
          <td>
            <textarea name="descField" rows=4 cols=40><%=desc%></textarea>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_pending_records_table")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="pendingField" value="<%=recordsTable%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_records_per_page")%>:
          </td>
          <td>
            <input type="text" name="recordsField" value="<%=records%>" size=5>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_pages_per_batch")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="batchField" value="<%=batch%>" size=5>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_maximum_elapsed_time")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="forceField" value="<%=force%>" size=5>
            <%=bundle.getString("lb_days") %>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_database_connection")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="connectionField">
            <%
                Enumeration keys = dbConnectionPairs.keys();
                while (keys.hasMoreElements())
                {
                    String selected = ""; 
                    Long id = (Long)keys.nextElement();
                    String value = (String) dbConnectionPairs.get(id);
                    if (value.equals(connection))
                        selected = "selected"; 
                    out.println("<option value=" + id + " " + selected + " >" +
                          dbConnectionPairs.get(id) + "</option>"); 
                }
            %>
            </select>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
      <tr>
        <td>
          <input type="button" name="cancel" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="save" value="<%=lbsave%>"
            onclick="submitForm('save')">
        </td>
      </tr>
    </table>
  </td>
</tr>
</table>
</form>

