// Need to include /includes/xmlextras.js

function getSnippetConnection(p_async)
{
    var conn = XmlHttp.create();

    var re = new RegExp("envoy");
    var url = window.location.href;
    var baseUrl = url.substring(0, url.search(re));
    url = baseUrl + "SnippetServlet";

    conn.open("POST", url, p_async);

    return conn;
}

function addSnippetArg(p_dom, p_arg)
{
    var node = p_dom.createElement("arg");
    node.text = p_arg;
    p_dom.documentElement.appendChild(node);
}

function makeSnippetRequest(p_command, arg1, arg2, arg3, arg4, arg5,
    arg6, arg7, arg8, arg9)
{
    var dom = XmlDocument.create();
    var xml = "<message><request></request></message>";

    dom.loadXML(xml);

    dom.documentElement.selectSingleNode("request").text = p_command;
    if (typeof(arg1) != "undefined") addSnippetArg(dom, arg1);
    if (typeof(arg2) != "undefined") addSnippetArg(dom, arg2);
    if (typeof(arg3) != "undefined") addSnippetArg(dom, arg3);
    if (typeof(arg4) != "undefined") addSnippetArg(dom, arg4);
    if (typeof(arg5) != "undefined") addSnippetArg(dom, arg5);
    if (typeof(arg6) != "undefined") addSnippetArg(dom, arg6);
    if (typeof(arg7) != "undefined") addSnippetArg(dom, arg7);
    if (typeof(arg8) != "undefined") addSnippetArg(dom, arg8);
    if (typeof(arg9) != "undefined") addSnippetArg(dom, arg9);

    return dom;
}

