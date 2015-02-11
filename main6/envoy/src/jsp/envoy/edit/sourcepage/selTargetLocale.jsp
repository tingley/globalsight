<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.SortUtil,
            com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
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

String lb_heading = "Select Target Locale";

ArrayList locales = new ArrayList();
// locales.add(state.getSourceLocale());
locales.addAll(state.getJobTargetLocales());
SortUtil.sort(locales, new GlobalSightLocaleComparator(uiLocale));

%>
<html>
<head>
<title><%=lb_heading%></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<style>
BODY { background-color: ThreeDFace; }
BODY { font-family: verdana; font-size: 10pt; }
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
    else if (key == 13) // Return
    {
        doOk();
    }
}

function doOk()
{
    var result = getSelectedValue(idLocales);

    window.returnValue = result;
    window.close();
}

function doCancel()
{
    window.returnValue = null;
    window.close();
}

function getSelectedValue(select)
{
    return select.options[select.selectedIndex].value;
}

function initForm()
{
    idLocales.selectedIndex = 0;
}

function doOnLoad()
{
    g_arg = window.dialogArguments;

    initForm();

    idLocales.focus();
}
</script>
</head>

<body onload="doOnLoad()" onkeypress="doKeypress()">

<DIV style="margin: 12px">
  <DIV>Select the target locale to preview:</DIV>
  <DIV style="margin-top: 12px;">
    <SELECT id="idLocales" size=6 ondblclick="doOk()"
      style="width: 300px; overflow-x: auto;">
      <%
      for (int i = 0, max = locales.size(); i < max; i++)
      {
        GlobalSightLocale locale = (GlobalSightLocale)locales.get(i);
      %>
      <option value="<%=locale.toString()%>">
	<%=locale.getDisplayName(uiLocale)%>
      </option>
      <%
      }
      %>
    </SELECT>
  </DIV>

  <DIV style="margin-top: 12px; margin-right: -15px; text-align: right;">
  <input id="idCancel" type="button" value="Cancel" onclick="doCancel()"> &nbsp;
  <input id="idOk"     type="button" value="OK" onclick="doOk()">
  </DIV>
</DIV>

</body>
</html>
