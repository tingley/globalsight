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

String lb_heading = "Translatable Type";
%>
<html xmlns:mpc>
<head>
<title><%=lb_heading%></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<style>
BODY { background-color: ThreeDFace; }
BODY { font-family: verdana; font-size: 10pt; margin: 12px; }
#idCancel       { width: 60; }
#idOk           { width: 60; }
</style>
<script>
var g_arg;

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
    window.returnValue = getSelectedValue(idSelect);
    window.close();
}

function doCancel()
{
    window.returnValue = null;
    window.close();
}

function selectValue(select, value)
{
    for (var i = 0; i < select.options.length; ++i)
    {
        if (select.options(i).value == value)
        {
            select.selectedIndex = i;
            return;
        }
    }
}

function getSelectedValue(select)
{
    return select.options[select.selectedIndex].value;
}

function initForm()
{
    idSelect.options[0].value = g_arg.dataformat + ':text';

    if (g_arg.dataformat == 'javascript')
    {
        selectValue(idSelect, 'javascript:string');
    }
}

function doOnLoad()
{
    g_arg = window.dialogArguments;

    initForm();

    idOk.focus();
}
</script>
</head>
<body onload="doOnLoad()" onkeypress="doKeypress()">

<div>
Extract selected text as:
<select id="idSelect">
  <option value="html:text">Text (Default)</option>
  <option value="javascript:string">Javascript String&nbsp;</option>
</select>
</div>
<br>

<div align="center">
<input id="idCancel" type="button" value="Cancel" onclick="doCancel()">
&nbsp;&nbsp;
<input id="idOk"     type="button" value="OK" onclick="doOk()">
</div>

</body>
</html>
