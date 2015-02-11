<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.edit.SynchronizationStatus,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.foundation.User,
            java.util.*"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String str_userId =
  ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();

String lb_heading = "Validation Messages";
%>
<html>
<head>
<title><%=lb_heading%></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<style>
BODY { font-family: verdana; font-size: 10pt; padding: 12px; }
#idOk { width: 60; }
#idButtons { height: 20px; }

.scrollTable {
        width: 100%;
        height: expression(idBody.clientHeight - 58);
        overflow: auto;
        border: 1px solid black;
}

.scrollTableHead    { position: relative;
                      top: expression(this.offsetParent.scrollTop); }
.scrollTableHead th { padding-left: 4px; padding-right: 4px;
                      font: 10pt verdana; font-weight: bold;
                      background-color: #738EB5 !important;
                      color: white !important; }
.scrollTableBody td { padding-left: 4px; padding-right: 4px;
                      font: 10pt verdana; }

#idBodyTable {
  behavior: url(/globalsight/includes/rowover.htc);
  ro--light-background: #eeeeee;
  ro--shade-background: white;
  ro--selected-color: black;
  ro--selected-background: lightskyblue;
}
</style>

<script>
var g_args;

var g_currentRow = 0;

function doKeypress()
{
    var key = event.keyCode;

    if (key == 27) // Escape
    {
        doCancel();
    }
}

function doOk()
{
    window.close();
}

function doCancel()
{
    window.close();
}

function toJavascript(p_arg)
{
    if (!p_arg || p_arg.length == 0)
    {
        return p_arg;
    }

    var result = "";

    for (var i = 0, max = p_arg.length; i < max; i++)
    {
        var ch = p_arg.charAt(i);

        switch (ch)
        {
        case '\\': result += "\\\\"; break;
        case '"':  result += "\\\""; break;
        case '\'': result += "\\\'"; break;
        case '\b': result += "\\b";  break;       // backspace
        case '\u000b': result += "\\v";  break;   // vertical tab
        case '\u000c': result += "\\f";  break;   // form feed
        case '\n': result += "\\n";  break;
        case '\r': result += "\\r";  break;
        case '\t': result += "\\t";  break;
        default: result += ch; break;
        }
    }

    return result;
}

function selectMessage()
{
    g_currentRow = event.srcRow;
    if (g_currentRow.rowIndex > 0)
    {
        var line = g_currentRow.line;
        var col  = g_currentRow.col;

        g_args.window.gotoLine(line, col);
    }
}

function initTable()
{
    var tbody = idMessages;
    for (i = tbody.rows.length; i > 0; --i)
    {
        tbody.deleteRow(i-1);
    }

    var errors = g_args.element.selectNodes('//error');

    for (var i = 0, max = errors.length; i < max; i++)
    {
        var error = errors.item(i).text;
        var tmp = error.split(";");
        var line = tmp[0];
        var col = tmp[1];
        var level = tmp[2];
        var message = "";
        for (var jj = 3, jmax = tmp.length; jj < jmax; jj++)
        {
           message += tmp[jj];
           if (jj < jmax - 1)
           {
             message += ";";
           }
        }
        message = message.replace(/\r?\n/g, " ");

        var row, cell;

        row = tbody.insertRow();
        row.line = line;
        row.col = col;

        cell = row.insertCell();
        cell.innerText = line;
        cell.align = "right";
        cell.vAlign = "top";
        cell = row.insertCell();
        cell.innerText = col;
        cell.align = "right";
        cell.vAlign = "top";
        cell = row.insertCell();
        cell.innerText = level;
        cell.vAlign = "top";
        cell = row.insertCell();
        cell.innerText = message;
        cell.vAlign = "top";
    }

    idBodyTable.Format();
}

function doOnLoad()
{
    g_args = window.dialogArguments;

    initTable();

    idOk.focus();
}
</script>
</head>
<body id="idBody" onload="doOnLoad()" onkeypress="doKeypress()">

<div class="scrollTable">
  <table id="idBodyTable" CELLSPACING="0" selectable="true"
   striped="true" selection="true" onrowselect="selectMessage()">
    <thead>
      <tr class="scrollTableHead">
        <th>Line</th>
        <th>Column</th>
        <th>Level</th>
        <th align="left">Message</th>
      </tr>
    </thead>
    <tbody id="idMessages" class="scrollTableBody"></tbody>
  </table>
</div>

<div id="idButtons" align="center" style="margin-top: 10px">
<input id="idOk" type="button" value="OK" onclick="doOk()">
</div>

</body>
</html>
