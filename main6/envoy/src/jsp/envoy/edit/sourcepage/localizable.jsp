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

String lb_heading = "Localizable Type";
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
<option value="charset">charset</option>
<option value="css-color">css-color</option>
<option value="css-font-family">css-font-family</option>
<option value="img-height">img-height</option>
<option value="img-width">img-width</option>
<option value="input-value">input-value</option>
<option value="url-a">url-a</option>
<option value="url-animation">url-animation</option>
<option value="url-applet">url-applet</option>
<option value="url-applet-codebase">url-applet-codebase</option>
<option value="url-area">url-area</option>
<option value="url-audio">url-audio</option>
<option value="url-base">url-base</option>
<option value="url-bgsound">url-bgsound</option>
<option value="url-blockquote">url-blockquote</option>
<option value="url-body">url-body</option>
<option value="url-del">url-del</option>
<option value="url-embed">url-embed</option>
<option value="url-form">url-form</option>
<option value="url-frame">url-frame</option>
<option value="url-frame-longdesc">url-frame-longdesc</option>
<option value="url-head">url-head</option>
<option value="url-iframe">url-iframe</option>
<option value="url-iframe-longdesc">url-iframe-longdesc</option>
<option value="url-img">url-img</option>
<option value="url-img-longdesc">url-img-longdesc</option>
<option value="url-img-usemap">url-img-usemap</option>
<option value="url-input">url-input</option>
<option value="url-input-usemap">url-input-usemap</option>
<option value="url-ins">url-ins</option>
<option value="url-layer">url-layer</option>
<option value="url-link">url-link</option>
<option value="url-media">url-media</option>
<option value="url-object-classid">url-object-classid</option>
<option value="url-object-codebase">url-object-codebase</option>
<option value="url-object-data">url-object-data</option>
<option value="url-object-usemap">url-object-usemap</option>
<option value="url-q">url-q</option>
<option value="url-script">url-script</option>
<option value="url-style">url-style</option>
<option value="url-table">url-table</option>
<option value="url-td">url-td</option>
<option value="url-th">url-th</option>
<option value="url-video">url-video</option>
<option value="url-xml">url-xml</option>
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
