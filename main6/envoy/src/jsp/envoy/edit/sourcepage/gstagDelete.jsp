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

String lb_heading = "Deletable Region Start";

ArrayList locales = new ArrayList();
locales.add(state.getSourceLocale());
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
BODY, .table    { font-family: verdana; font-size: 10pt; margin: 0; }
#idCancel       { width: 60; }
#idOk           { width: 60; }
.hint           { font-size: 8pt; font-style: italic; color: gray; }
.help           { cursor: hand; color: blue;
                  behavior: url(/globalsight/includes/hiliteHelp.htc); }
</style>
<script>
var g_arg;

var helpFile = '/globalsight/help/en_US/My_Activities/Snippet_and_Content_Editor.htm';

function helpSwitch()
{
   var helpWindow = window.open(helpFile, 'helpWindow',
       'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
   helpWindow.focus();
}

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
    window.returnValue = getSelectedValues(idDeletedLocale);
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

function selectMultipleValues(select, values)
{
    var value, option;
    var arr = values.split(",");
    for (var j = 0, maxj = arr.length; j < maxj; j++)
    {
        value = arr[j];

        for (var i = 0, max = select.options.length; i < max; i++)
        {
            option = select.options(i);

            if (option.value == value)
            {
                option.selected = true;
            }
        }
    }
}

function getSelectedValue(select)
{
    return select.options[select.selectedIndex].value;
}

function getSelectedValues(select)
{
    var result = '';

    for (var i = 0, max = select.options.length; i < max; i++)
    {
        option = select.options(i);

        if (option.selected == true)
        {
            result += option.value + ",";
        }
    }

    return result;
}

function initForm()
{
    if (g_arg.locales)
    {
        selectMultipleValues(idDeletedLocale, g_arg.locales);
    }
}

function doOnLoad()
{
    g_arg = window.dialogArguments;

    initForm();

    idDeletedLocale.focus();
}
</script>
</head>
<body onload="doOnLoad()" onkeypress="doKeypress()">

<DIV STYLE="padding-top: 12px; padding-left: 12px;">
<DIV>
  <span class="help" style="float: right" onclick="helpSwitch()">Help</span>
  <B>Deletable Region Start</B>
</DIV>
<BR>
<DIV>
  <label for="idDeletedLocale" ACCESSKEY="L">
  Delete the region in these <U>l</U>ocales:
  </label>
</DIV>
<SELECT id="idDeletedLocale" multiple size=4 style="width:300px">
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
<DIV class="hint" style="padding-top: 6px;">
Ctrl-click to select multiple values, or no value.
</DIV>
<DIV style="padding-top: 12px; text-align: right;">
  <input id="idCancel" type="button" value="Cancel" onclick="doCancel()">
  &nbsp;
  <input id="idOk"     type="button" value="OK" onclick="doOk()">
</DIV>
</DIV>

</body>
</html>
