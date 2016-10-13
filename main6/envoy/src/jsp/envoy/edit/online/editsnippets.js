/*
 * Copyright (c) 2002 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */
var g_SnippetConnection = null;

function SnippetError(strMessage, bFatal)
{
    ShowError(strMessage);

    if (bFatal)
    {
        alert("Please close this window and try again.");
    }
}

function getConnection(p_async)
{
    var conn = new ActiveXObject("Microsoft.XMLHTTP");

    var re = new RegExp("AMBASSADOR");
    var url = window.location.href.toUpperCase();
    var baseUrl = url.substring(0, url.search(re));
    url = baseUrl + "globalsight/SnippetServlet";

    conn.open("POST", url, p_async);

    return conn;
}

function addArg(p_dom, p_arg)
{
    var node = p_dom.createElement("arg");
    node.text = p_arg;
    p_dom.documentElement.appendChild(node);
}

function makeRequest(p_command, arg1, arg2, arg3, arg4, arg5,
  arg6, arg7, arg8, arg9)
{
    var dom = new ActiveXObject("Microsoft.XMLDOM");
    var xml = "<message><request></request></message>";

    dom.loadXML(xml);

    dom.documentElement.selectSingleNode("request").text = p_command;
    if (typeof(arg1) != "undefined") addArg(dom, arg1);
    if (typeof(arg2) != "undefined") addArg(dom, arg2);
    if (typeof(arg3) != "undefined") addArg(dom, arg3);
    if (typeof(arg4) != "undefined") addArg(dom, arg4);
    if (typeof(arg5) != "undefined") addArg(dom, arg5);
    if (typeof(arg6) != "undefined") addArg(dom, arg6);
    if (typeof(arg7) != "undefined") addArg(dom, arg7);
    if (typeof(arg8) != "undefined") addArg(dom, arg8);
    if (typeof(arg9) != "undefined") addArg(dom, arg9);

    return dom;
}

function getSnippet(p_name, p_locale, p_id)
{
    document.body.style.cursor = "wait";

    g_SnippetConnection = getConnection(false);

    var request = makeRequest("getsnippet", p_name, p_locale, p_id);
    g_SnippetConnection.send(request);

    document.body.style.cursor = "auto";
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
        return null;
    }

    var result = new Snippet(
        node.selectSingleNode("name").text,
        node.selectSingleNode("description").text,
        node.selectSingleNode("locale").text,
        node.selectSingleNode("displayLocale").text,
        node.selectSingleNode("id").text,
        node.selectSingleNode("value").text);

    return result;
}

function doModifySnippet(p_snippet)
{
    document.body.style.cursor = "wait";

    g_SnippetConnection = getConnection(false);

    var request = makeRequest("modifysnippet",
        p_snippet.name, p_snippet.desc, p_snippet.locale,
        p_snippet.id, p_snippet.value);
    g_SnippetConnection.send(request);

    document.body.style.cursor = "auto";

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
        return null;
    }

    alert("Snippet has been updated.");
}

function editSnippet(p_element)
{
    if (p_element.GSversion == "0")
    {
        alert("A generic snippet cannot be modified.");
        return;
    }

    // Retrieve the snippet from the server, pass it to the editor,
    // then save it back and update the page.
    var snippet = getSnippet(p_element.GSname, g_targetLocale,
        p_element.GSversion);

    if (snippet)
    {
        idSnippetEditorDialog.myarg = snippet;
        var result = idSnippetEditorDialog.runModal(window.event.srcElement);

        if (result)
        {
            var snippet = idSnippetEditorDialog.getArgument("myarg");
            doModifySnippet(snippet);
        }
    }
}

