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
            com.globalsight.util.SortUtil,
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

String lb_heading = "Snippet";

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
#idCopy         { width: 60; margin-top: 10px; margin-bottom: 10px; }
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

// global variables to keep the XMLHTTP object alive during requests
var g_SnippetConnection = null;
var g_SnippetReadConnection = null;
var g_snippets = new Array();
var g_snippetsById = new Object();
var g_snippet = null;
var g_snippetName = null;
var g_locale = "en_US";

var helpFile = '/globalsight/help/en_US/My_Activities/Snippet_and_Content_Editor.htm';

function helpSwitch()
{
   var helpWindow = window.open(helpFile, 'helpWindow',
       'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
   helpWindow.focus();
}

function GsTagResult(name, added, id)
{
    this.name = name;
    this.added = added;
    this.id    = id;
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
    var id = getSelectedValue(idAddName);
    var snippet = g_snippetsById[id];

    var name = snippet.name;
    var added = getSelectedValue(idLocale);
    var isGeneric = snippet.isGeneric();

    window.returnValue = new GsTagResult(name, added, isGeneric ? null : id);
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

    idCopy.disabled = idEdit.disabled = idDelete.disabled = true;
}

function showPreview(p_snippet)
{
    idDesc.value = p_snippet.desc;
    idValue.value = p_snippet.value;

    idCopy.disabled = !p_snippet.isGeneric();
    idEdit.disabled = p_snippet.isGeneric();
    idDelete.disabled = false;
}

function previewSnippet()
{
    try
    {
        clearPreview(false);

        var id = getSelectedValue(idAddName);
        if (!id) return;

        g_snippet = g_snippetsById[id];
        if (!g_snippet) return;

        showPreview(g_snippet);
    }
    catch (ex)
    {
        alert("previewSnippet(): " + ex.message);
    }
}

function copySnippet()
{
    var arg = g_snippet.clone();
    arg.locale = getSelectedValue(idLocale);
    arg.displayLocale = getSelectedText(idLocale);

    idSnippetEditor.myarg = arg;
    var result = idSnippetEditor.runModal();
    if (result)
    {
        var snippet = idSnippetEditor.getArgument("myarg");

        doCreateSnippet(snippet.name, snippet.desc, snippet.locale,
          snippet.value);
    }
}

function doCreateSnippet(p_name, p_desc, p_locale, p_value)
{
    idBody.style.cursor = "wait";

    g_SnippetConnection = getSnippetConnection(false);

    var request = makeSnippetRequest("createsnippetgetsnippet",
        p_name, p_desc, p_locale, "", p_value);
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
    showPreview(result);

    g_snippetsById[result.id] = result;

    addOption(idAddName, result.name + " " +
      (result.isGeneric() ? "" : result.id), result.id);
}

function modifySnippet()
{
    idSnippetEditor.myarg = g_snippet;

    var result = idSnippetEditor.runModal();
    if (result)
    {
        var snippet = idSnippetEditor.getArgument("myarg");

        doModifySnippet(snippet.name, snippet.desc, snippet.locale,
          snippet.id, snippet.value);
    }
}

function doModifySnippet(p_name, p_desc, p_locale, p_id, p_value)
{
    idBody.style.cursor = "wait";

    g_SnippetConnection = getSnippetConnection(false);

    var request = makeSnippetRequest("modifysnippetgetsnippet",
        p_name, p_desc, p_locale, p_id, p_value);
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

    g_snippetsById[result.id] = result;

    clearPreview(false);
    showPreview(result);
}

function deleteSnippet()
{
    var id = getSelectedValue(idAddName);
    var snippet = g_snippetsById[id];

    if (snippet.isGeneric())
    {
        if (!confirm(
            "Warning: locale-specific copies of this snippet may be in use.\n\n" +
            "Do you really want to delete this snippet?"))
        {
            return;
        }
    }
    else
    {
        if (!confirm("Do you really want to delete this snippet?"))
        {
            return;
        }
    }

    doDeleteSnippet(snippet.name, snippet.locale, snippet.id);
}

function doDeleteSnippet(p_name, p_locale, p_id)
{
    idBody.style.cursor = "wait";

    g_SnippetConnection = getSnippetConnection(false);

    var request = makeSnippetRequest("removesnippet", p_name, p_locale, p_id);
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

    delete g_snippetsById[p_id];

    removeOption(idAddName, p_id);

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

function showLoadingMessage(select)
{
    clearSelect(select);

    var option = document.createElement('OPTION');
    option.text = "Loading Snippets, please wait..."
    option.value = null;
    select.options.add(option);
}

function showNoSnippetsMessage(select)
{
    clearSelect(select);

    var option = document.createElement('OPTION');
    option.text = "No snippets found..."
    option.value = null;
    select.options.add(option);
}

function clearSelect(select)
{
    var options = select.options;
    for (var i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }
}

function fillSelect(select, snippets)
{
    clearSelect(select);

    var options = select.options;

    for (var i = 0, max = snippets.length; i < max; i++)
    {
        var snippet = snippets[i];

        var option = document.createElement('OPTION');
        if (snippet.isGeneric())
        {
            option.text = snippet.name;
        }
        else
        {
            option.text = snippet.name + " " + snippet.id;
        }
        option.value = snippet.id;
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

function removeOption(p_select, p_value)
{
    var options = p_select.options;
    var i = 0;
    for (i = 0, max = options.length; i < max; i++)
    {
        var option = options.item(i);

        if (option.value == p_value)
        {
            options.remove(i);
            p_select.selectedIndex = (i == 0 ? 0 : i - 1);
            break;
        }
    }
}

function selectValue(select, value)
{
    var options = select.options;
    for (var i = 0; i < options.length; ++i)
    {
        if (options(i).value == value)
        {
            select.selectedIndex = i;
            return true;
        }
    }

    return false;
}

function selectText(select, text)
{
    var options = select.options;
    for (var i = 0; i < options.length; ++i)
    {
        if (options(i).text == text)
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

function getSelectedText(select)
{
    if (select.options.length > 0)
    {
        return select.options[select.selectedIndex].text;
    }

    return null;
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

function changeLocale()
{
    var id = getSelectedValue(idAddName);
    var snippet = g_snippetsById[id];
    var name = snippet.name;

    loadSnippets();

    selectText(idAddName, name);

    previewSnippet();
}

function loadSnippets()
{
    idBody.style.cursor = "wait";

    showLoadingMessage(idAddName);
    clearPreview();

    g_locale = getSelectedValue(idLocale);
    g_SnippetConnection = getSnippetConnection(false);
    var request = makeSnippetRequest("getsnippetsbylocale", g_locale);
    g_SnippetConnection.send(request);

    var conn = g_SnippetConnection;

    if (conn.readyState == 4)
    {
        var dom = conn.responseXML;

        if (conn.status != 200)
        {
            // SnippetError("xml = " + conn.responseText);

            SnippetError("Server error " + conn.status + ": " +
              conn.statusText, false);

            idBody.style.cursor = "auto";
            return;
        }

        if (dom.parseError.errorCode != 0)
        {
            // SnippetError("xml = " + conn.responseText, false);

            SnippetError("Error in server response: = " +
              dom.parseError.reason, false);

            idBody.style.cursor = "auto";
            return;
        }

        var hits = dom.documentElement.selectNodes("//snippet");
        g_snippets = new Array();
        g_snippetsById = new Object();

        if (hits.length == 0)
        {
            showNoSnippetsMessage(idAddName);
        }
        else
        {
            for (i = 0, max = hits.length; i < max; i++)
            {
                var hit = hits(i);
	        var id = hit.selectSingleNode("id").text;
                var snip = new Snippet(
                  hit.selectSingleNode("name").text,
                  hit.selectSingleNode("description").text,
                  hit.selectSingleNode("locale").text,
                  hit.selectSingleNode("displayLocale").text,
                  hit.selectSingleNode("id").text,
                  hit.selectSingleNode("value").text);

                g_snippets.push(snip);
                g_snippetsById[id] = snip;
            }

            fillSelect(idAddName, g_snippets);
        }

        idOk.disabled = (idAddName.options.length == 0);
        idBody.style.cursor = "auto";
    }
}

function initSnippets()
{
    loadSnippets();

    if (g_arg.added)
    {
        var found;

        if (g_arg.id)
        {
            found = selectValue(idAddName, g_arg.id);
        }
        else
        {
            g_snippetName = g_arg.name;
            g_snippetName = g_snippetName.toUpperCase();
            found = selectText(idAddName, g_snippetName);
        }
    
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

function initLocale()
{
    if (g_arg.added)
    {
        selectValue(idLocale, g_arg.added);
    }
}

function doOnLoad()
{
    g_arg = window.dialogArguments;

    idBody.style.cursor = "wait";

    initLocale();
    initSnippets();

    idBody.style.cursor = "auto";

    if (g_arg.added)
    {
        idAddName.focus();
    }
    else
    {
        idLocale.focus();
    }
}
</script>
</head>
<body id="idBody" onload="doOnLoad()" onkeypress="doKeypress()">

<DIV id="idSnippetEditor"></DIV>

    <DIV STYLE="padding:12px;">
    <DIV>
      <span class="help" style="float: right" onclick="helpSwitch()">Help</span>
      <B>Insert/Edit Snippet</B>
    </DIV>
    <BR>
    <TABLE class="table" cellspacing="0" cellpadding="0">
      <TR>
	<TD valign="top" colspan="2">
	  <label for="idLocale" ACCESSKEY="L">
	  Select a <U>l</U>ocale:
	  </label>
	</TD>
      </TR>
      <TR>
	<TD colspan="2">
	  <SELECT id="idLocale" onchange="changeLocale()" style="width:300px">
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
	</TD>
      </TR>
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
	  <BUTTON id="idCopy" ACCESSKEY="C" onclick="copySnippet()">
	  <U>C</U>opy...</BUTTON>
	  &nbsp;
	  <BUTTON id="idEdit" ACCESSKEY="E" onclick="modifySnippet()">
	  <U>E</U>dit...</BUTTON>
	  &nbsp;
	  <BUTTON id="idDelete" ACCESSKEY="D" onclick="deleteSnippet()">
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
