<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.permission.PermGroupBasicHandler,
                 com.globalsight.everest.permission.Permission,
                 com.globalsight.everest.permission.PermissionGroup,
                 com.globalsight.everest.permission.PermissionSet,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.GeneralException,
                 com.globalsight.everest.company.CompanyWrapper,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    String EMEA = CompanyWrapper.getCurrentCompanyName();
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbok = bundle.getString("lb_ok");
    String lbpermissions = bundle.getString("lb_permissions");
    String lbcollapse = bundle.getString("lb_collapse_all");
    String lbexpand = bundle.getString("lb_expand_all");
    String lbusers = bundle.getString("lb_users");

    String okURL = ok.getPageURL() + "&action=ok";
    String pgname = (String)sessionMgr.getAttribute("permGroupName");
    String title = bundle.getString("lb_permission_group") +
      " (" + pgname + ")" + " - " + bundle.getString("lb_details");

    // Data
    String xml = (String)sessionMgr.getAttribute("permissionXML");
    ArrayList users = (ArrayList)sessionMgr.getAttribute("users");

%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/envoy/administration/permission/tree.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<style type="text/css">
.root {
  padding: 0px 0px 2px 0px
}

.root div {
  padding: 0px 0px 0px 0px;
  display: none;
  margin-left: 2em;
}
</style>
<script>
var needWarning = false;
var objectName = "<%=bundle.getString("lb_permission_group")%>";
var guideNode="permissionGroups";
var helpFile = "<%=bundle.getString("help_permission_group_details")%>";

// Parse the xml file and read it into js objects and create
// a tree.
function loadTree()
{
    var xmlDocument;
    var xmlStr = "<%=xml%>";
    var loaded;
    
    if(window.ActiveXObject)
    {
    xmlDocument = new ActiveXObject('Microsoft.XMLDOM');
    xmlDocument.async = false;
    loaded = xmlDocument.loadXML(xmlStr);
    if (!loaded)
    {
        alert("ERROR: The permission xml is malformed. Please contact Welocalize Support.");
        return;
    }
    }
    else if(window.DOMParser)
    { 
    var parser = new DOMParser();
    xmlDocument = parser.parseFromString(xmlStr,"text/xml");
    if (!xmlDocument)
    {
        alert("ERROR: The permission xml is malformed. Please contact Welocalize Support.");
        return;
    }
    }
    else
    {
    alert("You brower can not run this script!");
    }
    
    

    var objTree = new jsTree;
    objTree.createRoot("<%=lbpermissions%>", "cat.lb_permissions", false);
    readXmlFile(xmlDocument.documentElement, objTree.root);
    objLocalTree.root.writeNode(true);
    openNode(document.getElementById("root"));
}

// function to obtain node information and create tree
function readXmlFile(node, treeNode)
{
    if (node.childNodes == null) return;

    for (var i = 0; i < node.childNodes.length; i++)
    {
        var id = null;
        var text = null;
        var type = null;
        var set = false;
        child = node.childNodes[i];
        var attrs = node.childNodes[i].attributes;
        if (child.nodeName == "category")
        {
            for (var j=0; j<attrs.length; j++)
            {
                type = "cat";
                if (attrs[j].name == "id")
                    id = "cat." + attrs[j].value;
                else if (attrs[j].name == "label")
                {
                    text = attrs[j].value.replace("CUSTOM ", "<%= EMEA %> ");
                }
            }
        }
        else if (child.nodeName == "permission")
        {
            for (var j = 0; j<attrs.length; j++)
            {
                type = "perm";
                if (attrs[j].name == "id")
                    id = "perm."+attrs[j].value;
                else if (attrs[j].name == "label")
                    text = attrs[j].value.replace("CUSTOM ", "<%= EMEA %> " );
                else if (attrs[j].name == "set")
                    set = attrs[j].value;
            }
        }
        if(text)
        {
        newChild = treeNode.addChild(text, id, type, set);
        readXmlFile(node.childNodes[i], newChild);
        }
    }
}

function submitForm(formAction)
{
    if (formAction == "ok")
    {
        permForm.action = "<%=okURL%>";
    }
    permForm.submit();
}
</script>
</head>

<body id="idBody" leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

<form name="permForm" method="post" action="">

<input type="button" name="expand" value="<%=lbexpand%>"
 onclick="objLocalTree.openAllNodes()">
<input type="button" name="collapse" value="<%=lbcollapse%>"
 onclick="objLocalTree.closeAllNodes()">
<p>
<script>loadTree();</script>
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr><td><%=lbusers%><br>
  <% for (int i =0 ; i < users.size(); i++)
     {
        User user = (User)users.get(i);
        String username = user.getUserName();
  %>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=username%><br>
  <% } %>
  </td></tr>
  <tr><td></td></tr>
  <tr>
    <td>
      <input type="button" name="ok" value="<%=lbok%>"
      onclick="submitForm('ok')">
    </td>
  </tr>
</table>
</form>
</div>
</body>
</html>
