<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.util.GlobalSightLocale,
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
// Bugs: if this dialog is opened on a snippet that doesn't exist and there
// is only 1 snippet in the library, the preview part shows an error message
// and the edit/delete buttons are disabled. The buttons get re-enabled
// only if a new snippet name is selected (onchange) but since there is only
// 1 name to select and it cannot be selected, this can never happen.

ResourceBundle bundle = PageHandler.getBundle(session);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String str_userId =
  ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();

String lb_heading = "Snippet Position";

%>
<html>
<head>
<title><%=lb_heading%></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT src="/globalsight/includes/xmlextras.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/snippets/protocol.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/snippets/snippet.js"></SCRIPT>
<style>
BODY { background-color: ThreeDFace; }
BODY, .table    { font-family: verdana; font-size: 10pt; margin: 0; }
LEGEND { font-weight: bold; }
#idAddName      { behavior: url(/globalsight/includes/SmartSelect.htc); }
#idCancel       { width: 60; margin-top: 10px; margin-bottom: 10px; }
#idOk           { width: 60; margin-top: 10px; margin-bottom: 10px; }
#idCreateNew    { width: 100; margin-top: 10px; margin-bottom: 10px; }
#idEdit         { width: 60; margin-top: 10px; margin-bottom: 10px; }
#idDelete       { width: 60; margin-top: 10px; margin-bottom: 10px; }
#idDesc, #idValue { color: gray; }
#idSnippetEditor { position: absolute; top: 0; left: 0; display: none;
  behavior: url('/globalsight/envoy/edit/snippets/SnippetEditor.htc'); }

.help           { cursor: hand; color: blue;
                  behavior: url(/globalsight/includes/hiliteHelp.htc); }
</style>
<script>
var g_arg;
var g_initialized;

// global variables to keep the XMLHTTP object alive during requests
var g_SnippetConnection = null;
var g_SnippetReadConnection = null;
var g_snippet = null;

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
}

function doOk()
{
    var name = idAddName.value;
    if (!name || name.match(/[\ \t\r\n]+/))
    {
        alert("Enter a name.");
        idAddName.focus();
        return;
    }

    window.returnValue = name;
    window.close();
}

function doCancel()
{
    window.returnValue = null;
    window.close();
}

function clearPreview(p_showerror)
{
    if (p_showerror)
    {
        idValue.style.color = 'red';
        idValue.style.fontStyle = 'italic';
        idValue.value = 'snippet could not be loaded (may not exist)';
        idDesc.value = '';
    }
    else
    {
        idValue.style.color = '';
        idValue.style.fontStyle = 'normal';
        idDesc.value = idValue.value = '';
    }

    idEdit.disabled = idDelete.disabled = true;
}

function showPreview(p_desc, p_value)
{
    idDesc.value = p_desc;
    idValue.value = p_value;
    idEdit.disabled = idDelete.disabled = false;
}

function previewSnippet()
{
    if (!g_initialized) return;

    try
    {
        var name = getSelectedValue(idAddName);
        if (!name) return;

        g_SnippetReadConnection = getSnippetConnection(true);
        g_SnippetReadConnection.onreadystatechange = previewSnippet2;
        var request = makeSnippetRequest("getgenericsnippet", name);
        g_SnippetReadConnection.send(request);
    }
    catch (ex)
    {
        alert(ex.message);
    }
}

function previewSnippet2()
{
    var conn = g_SnippetReadConnection;

    if (conn.readyState == 4)
    {
        var dom = conn.responseXML;

        if (conn.status != 200)
        {
            // SnippetError("xml = " + conn.responseText);

            // Bad Request (400) is used for "snippet not found".
            if (conn.status != 400)
            {
                SnippetError("Server error " + conn.status + ": " +
                  conn.statusText, false);
            }
            clearPreview(true);
            return;
        }

        if (dom.parseError.errorCode != 0)
        {
            // SnippetError("xml = " + conn.responseText, false);

            SnippetError("Error in server response: = " +
              dom.parseError.reason, false);
            clearPreview(true);
            return;
        }

        var node = dom.documentElement;
        if (node.nodeName == "exception")
        {
            SnippetError(node.text, false);
            clearPreview(true);
            return;
        }

        var hit = dom.selectSingleNode("//snippet");
        g_snippet = new Snippet(
               hit.selectSingleNode("name").text,
               hit.selectSingleNode("description").text,
               hit.selectSingleNode("locale").text,
               hit.selectSingleNode("displayLocale").text,
               hit.selectSingleNode("id").text,
               hit.selectSingleNode("value").text);

        clearPreview(false);
        showPreview(g_snippet.desc, g_snippet.value);
    }
}

function createSnippet()
{
    idSnippetEditor.myarg = null;
    var result = idSnippetEditor.runModal();
    if (result)
    {
        var snippet = idSnippetEditor.getArgument("myarg");

        doCreateSnippet(snippet.name, snippet.desc, snippet.value);
    }
}

function doCreateSnippet(p_name, p_desc, p_value)
{
    idBody.style.cursor = "wait";

    g_SnippetConnection = getSnippetConnection(false);

    var request = makeSnippetRequest("createsnippet", p_name, p_desc,
        "", "", p_value);
    g_SnippetConnection.send(request);

    idBody.style.cursor = "auto";
    var dom = g_SnippetConnection.responseXML;

    if (g_SnippetConnection.status != 200)
    {
        // debug
        // SnippetError("xml = " + g_SnippetConnection.responseText);

        SnippetError("Server error " + g_SnippetConnection.status +
            ": " + g_SnippetConnection.statusText, false);

        return null;
    }

    if (dom.parseError.errorCode != 0)
    {
        // debug
        // SnippetError("xml = " + g_SnippetConnection.responseText, false);

        SnippetError("Error in server response: = " + dom.parseError.reason,
            false);

        return null;
    }

    var node = dom.documentElement;
    if (node.nodeName == "exception")
    {
        SnippetError(node.text, false);
        return;
    }

    var result = new Snippet(
               node.selectSingleNode("//name").text,
               node.selectSingleNode("//description").text,
               node.selectSingleNode("//locale").text,
               node.selectSingleNode("//displayLocale").text,
               node.selectSingleNode("//id").text,
               node.selectSingleNode("//value").text);

    clearPreview(false);
    showPreview(result.desc, result.value);

    addOption(idAddName, result.name, result.name);
    idOk.disabled = false;
}

function modifySnippet()
{
    idSnippetEditor.myarg = g_snippet;
    var result = idSnippetEditor.runModal();
    if (result)
    {
        var snippet = idSnippetEditor.getArgument("myarg");
        doModifySnippet(snippet.name, snippet.id, snippet.desc, snippet.value);
    }
}

function doModifySnippet(p_name, p_id, p_desc, p_value)
{
    idBody.style.cursor = "wait";

    g_SnippetConnection = getSnippetConnection(false);

    var request = makeSnippetRequest("modifysnippet", p_name, p_desc,
        "", p_id, p_value);
    g_SnippetConnection.send(request);

    idBody.style.cursor = "auto";
    var dom = g_SnippetConnection.responseXML;

    if (g_SnippetConnection.status != 200)
    {
        // debug
        // SnippetError("xml = " + g_SnippetConnection.responseText);

        SnippetError("Server error " + g_SnippetConnection.status +
            ": " + g_SnippetConnection.statusText, false);

        return null;
    }

    if (dom.parseError.errorCode != 0)
    {
        // debug
        // SnippetError("xml = " + g_SnippetConnection.responseText, false);

        SnippetError("Error in server response: = " + dom.parseError.reason,
            false);

        return null;
    }

    var node = dom.documentElement;
    if (node.nodeName == "exception")
    {
        SnippetError(node.text, false);
        return;
    }

    var result = new Snippet(
        node.selectSingleNode("//name").text,
        node.selectSingleNode("//description").text,
        node.selectSingleNode("//locale").text,
        node.selectSingleNode("//displayLocale").text,
        node.selectSingleNode("//id").text,
        node.selectSingleNode("//value").text);

    clearPreview(false);
    showPreview(result.desc, result.value);
}

function deleteSnippet()
{
    if (!confirm(
        "Warning: locale-specific copies of this snippet may be in use.\n\n" +
        "Do you really want to delete this snippet?"))
    {
        return;
    }

    doDeleteSnippet(g_snippet.name, g_snippet.id);
}

function doDeleteSnippet(p_name, p_id)
{
    idBody.style.cursor = "wait";

    g_SnippetConnection = getSnippetConnection(false);

    var request = makeSnippetRequest("removesnippet", p_name, "", p_id);
    g_SnippetConnection.send(request);

    idBody.style.cursor = "auto";
    var dom = g_SnippetConnection.responseXML;

    if (g_SnippetConnection.status != 200)
    {
        // debug
        // SnippetError("xml = " + g_SnippetConnection.responseText);

        SnippetError("Server error " + g_SnippetConnection.status +
            ": " + g_SnippetConnection.statusText, false);

        return null;
    }

    if (dom.parseError.errorCode != 0)
    {
        // debug
        // SnippetError("xml = " + g_SnippetConnection.responseText, false);

        SnippetError("Error in server response: = " + dom.parseError.reason,
            false);

        return null;
    }

    var node = dom.documentElement;
    if (node.nodeName == "exception")
    {
        SnippetError(node.text, false);
        return;
    }

    clearPreview(false);

    removeOption(idAddName, p_name);

    idOk.disabled = (idAddName.options.length == 0);

    previewSnippet();
}

function SnippetError(p_message, p_fatal)
{
    window.showModalDialog('/globalsight/envoy/terminology/viewer/error.jsp',
        p_message,
        'center:yes; help:no; resizable:yes; status:no; ' +
        'dialogWidth: 450px; dialogHeight: 300px;');

    if (p_fatal)
    {
        alert("Please close this window and try again.");
    }
}

function clearSelect(select)
{
    var options = select.options;
    for (var i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }
}

function fillSelect(select, values)
{
    clearSelect(select);

    var options = select.options;

    for (var i = 0; i < values.length; i++)
    {
        var value = values[i];

        var option = document.createElement('OPTION');
        option.text = value;
        option.value = value;
        options.add(option);
    }
}

function addOption(p_select, p_text, p_value)
{
    var options = p_select.options;
    var i = 0;
    for (i = 0, max = options.length; i < max; i++)
    {
        var option = options.item(i);

        if (option.text > p_text)
        {
            break;
        }
    }

    var newOption = document.createElement("OPTION");
    newOption.text = p_text;
    newOption.value = p_value;
    p_select.options.add(newOption, i);
    p_select.selectedIndex = i;
}

function removeOption(p_select, p_text)
{
    var options = p_select.options;
    var i = 0;
    for (i = 0, max = options.length; i < max; i++)
    {
        var option = options.item(i);

        if (option.text == p_text)
        {
            options.remove(i);
            break;
        }
    }
}

function selectValue(select, value)
{
    for (var i = 0; i < select.options.length; ++i)
    {
        if (select.options(i).value == value)
        {
            select.selectedIndex = i;
            return true;
        }
    }

    return false;
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
    if (select.options.length > 0)
    {
        return select.options[select.selectedIndex].value;
    }

    return null;
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

function initSnippetNames()
{
    var conn = g_SnippetConnection;

    if (conn.readyState == 4)
    {
        var dom = conn.responseXML;

        if (conn.status != 200)
        {
            // SnippetError("xml = " + conn.responseText);

            SnippetError("Server error " + conn.status + ": " +
              conn.statusText, false);
            return;
        }

        if (dom.parseError.errorCode != 0)
        {
            // SnippetError("xml = " + conn.responseText, false);

            SnippetError("Error in server response: = " +
              dom.parseError.reason, false);
            return;
        }

        var names = new Array();

        var node, nodes = dom.selectNodes('/*/name');
        for (var i = 0, max = nodes.length; i < max; i++)
        {
           node = nodes.item(i);
           names.push(node.text);
        }

        clearSelect(idAddName);
        fillSelect(idAddName, names);

        idOk.disabled = (idAddName.options.length == 0);

        g_initialized = true;
        idBody.style.cursor = "auto";

        if (g_arg.add)
        {
            var name = g_arg.add.toUpperCase();
            var found = selectValue(idAddName, name);

            if (!found)
            {
                clearPreview(true);
            }
            else
            {
                previewSnippet();
            }
        }
        else
        {
            previewSnippet();
        }
    }
}

function initForm()
{
    idBody.style.cursor = "wait";

    g_SnippetConnection = getSnippetConnection(true);
    g_SnippetConnection.onreadystatechange = initSnippetNames;
    var request = makeSnippetRequest("getgenericnames");
    g_SnippetConnection.send(request);
}

function doOnLoad()
{
    g_arg = window.dialogArguments;

    initForm();

    idAddName.focus();
}
</script>
</head>
<body id="idBody" onload="doOnLoad()" onkeypress="doKeypress()">

<DIV id="idSnippetEditor"></DIV>

    <DIV STYLE="padding:12px;">
    <DIV>
      <span class="help" style="float: right" onclick="helpSwitch()">Help</span>
      <B>Create/Edit Snippet Position</B>
    </DIV>
    <BR>
    <TABLE class="table" cellspacing="0" cellpadding="0">
      <TR>
	<TD valign="top" colspan="2">
	  <label for="idAddName" ACCESSKEY="S">
	  Select an available <U>s</U>nippet for the position:
	  </label>
	</TD>
      </TR>
      <TR>
	<TD colspan="2">
	  <SELECT id="idAddName" onchange="previewSnippet()"
	    style="width:511px">
	    <OPTION>Loading Snippets, please wait...</OPTION>
	  </SELECT>
	</TD>
      </TR>
      <TR>
	<TD align="left">
	  <BUTTON id="idCreateNew" onclick="createSnippet()" ACCESSKEY="C">
	  <U>C</U>reate New...</BUTTON>
	  &nbsp;
	  <BUTTON id="idEdit" onclick="modifySnippet()" ACCESSKEY="E">
	  <U>E</U>dit...</BUTTON>
	  &nbsp;
	  <BUTTON id="idDelete" onclick="deleteSnippet()" ACCESSKEY="D">
	  <U>D</U>elete</BUTTON>
	</TD>
	<TD align="right">
	  <input id="idCancel" type="button" value="Cancel" onclick="doCancel()">
	  &nbsp;
	  <input id="idOk"     type="button" value="OK" onclick="doOk()">
	</TD>
      </TR>
      <TR>
	<TD colspan="2">
	  <FIELDSET style="padding:6px">
	  <LEGEND>Snippet Preview</LEGEND>
	  <TABLE class="table" cellspacing="0">
	    <TR>
	      <TD align="right" valign="top">Description:</TD>
	      <TD valign="top">
		<TEXTAREA id="idDesc" style="width: 400px; height: 40px;"
		READONLY TABINDEX="-1"></TEXTAREA>
	      </TD>
	    </TR>
	    <TR>
	      <TD align="right" valign="top">Value:</TD>
	      <TD valign="top">
		<TEXTAREA id="idValue" style="width: 400px; height: 170px;"
		READONLY TABINDEX="-1"></TEXTAREA>
	      </TD>
	    </TR>
	  </TABLE>
	  </FIELDSET>
	</TD>
      </TR>
    </TABLE>
    </DIV>

</body>
</html>
