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
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelEdit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    String EMEA = CompanyWrapper.getCurrentCompanyName();
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    
    PermissionGroup permGroup = null;
    boolean isGlobalLpGroup = false;  
    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbprev = bundle.getString("lb_previous");
    String lbnext = bundle.getString("lb_next");
    String lbdone = bundle.getString("lb_done");
    String lbpermissions = bundle.getString("lb_permissions");
    String lbcollapse = bundle.getString("lb_collapse_all");
    String lbexpand = bundle.getString("lb_expand_all");
    String lbviewxml = bundle.getString("lb_view_xml");

    String prevURL = prev.getPageURL() + "&action=prev";
    String nextURL = next.getPageURL() + "&action=next";
    String doneURL = done.getPageURL() + "&action=donePermSet";

    boolean edit = false;
    String cancelURL = null;
    String title = null;
    String helpFile = null;

    if (sessionMgr.getAttribute("edit") != null)
    {
        edit = true;
        permGroup = (PermissionGroup)sessionMgr.getAttribute("permGroup");
        if (1 == permGroup.getCompanyId()
        	&& !WebAppConstants.SUPER_ADMINISTRATOR_NAME.equals(permGroup.getName())
        	&& !WebAppConstants.SUPER_PM_NAME.equals(permGroup.getName()))
        {
        	isGlobalLpGroup = true;
        }
        helpFile = bundle.getString("help_permission_edit_group_perms");
        String pgname = (String)sessionMgr.getAttribute("permGroupName");
        title = bundle.getString("lb_edit") + " " +
          bundle.getString("lb_permission_group") +
          " (" + pgname + ")" + " - " + bundle.getString("lb_permissions");
        cancelURL = cancelEdit.getPageURL() + "&action=cancel";
    }
    else
    {
        helpFile = bundle.getString("help_permission_new_group_perms");
        title = bundle.getString("lb_new") + " " +
          bundle.getString("lb_permission_group") + " - " +
          bundle.getString("lb_permissions");
        cancelURL = cancel.getPageURL() + "&action=cancel";
    }

    // Data
    String xml = (String)sessionMgr.getAttribute("permissionXML");
    
%>                                                                         
<html>
<!-- This JSP is: envoy/administration/permisison/permGroupSet.jsp -->
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
var needWarning = true;
var objectName = "<%=bundle.getString("lb_permission_group")%>";
var guideNode="permissionGroups";
var helpFile = "<%=helpFile%>";

//popup a little window with the permission XML in it
function viewXml()
{
  window.open('/globalsight/envoy/administration/permission/viewPermissions.jsp',
    'PermissionXML',
    'status=0,menubar=1,resizable=1,width=500,height=500,scrollbars=1');
}

//
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
        alert("<%=bundle.getString("jsmsg_permission_param_molformed")%>");
        return;
    }
    }
    else if(window.DOMParser)
    { 
    var parser = new DOMParser();
    xmlDocument = parser.parseFromString(xmlStr,"text/xml");
    if (!xmlDocument)
    {
        alert("<%=bundle.getString("jsmsg_permission_param_molformed")%>");
        return;
    }
    }
    else
    {
    	alert("<%=bundle.getString("jsmsg_error_notParseXML")%>");
    }

    var objTree = new jsTree;
    objTree.createRoot("<%=lbpermissions%>", "cat.lb_permissions", "<%=edit%>");
    readXmlFile(xmlDocument.documentElement, objTree.root);
    objLocalTree.root.writeNode(false);
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
    if (formAction == "cancel")
    {
        if (confirmJump())
            permForm.action = "<%=cancelURL%>";
    }
    else if (formAction == "next")
    {
        permForm.action = "<%=nextURL%>";
    }
    else if (formAction == "done")
    {
        permForm.action = "<%=doneURL%>";
    }
    else if (formAction == "prev")
    {
        permForm.action = "<%=prevURL%>";
    }
    permForm.submit();
}

function initPermissions()
{
	<%
		if (permGroup == null && "Transware".equals(EMEA) 
			|| permGroup != null && isGlobalLpGroup)
		{
	%>
		for (var m = 0; m < permForm.elements.length; m++)
		{
			if (permForm.elements[m].type == "checkbox")
			{
	<%
				for (int i = 0; i < Permission.GLOBAL_LP_PERMS.length; i++)
				{
	%>
					if (permForm.elements[m].value != "perm." + "<%=Permission.GLOBAL_LP_PERMS[i]%>")
					{
						permForm.elements[m].disabled = true;
					}
					else
					{
						permForm.elements[m].disabled = false;
						continue;
					}
	<%
				}
	%>
			}
			if (permForm.elements[m].value == "cat.lb_my_activities"
					    ||permForm.elements[m].value == "cat.lb_setup"
						|| permForm.elements[m].value == "cat.lb_cms"
						|| permForm.elements[m].value == "cat.lb_notification_options")
					permForm.elements[m].disabled = false;
		}
	<%
		}
	%>
	loadGuides();
}


</script>
</head>

<body id="idBody" leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="initPermissions()">
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
<INPUT TYPE="button" name="view" VALUE="<%=lbviewxml %>" ONCLICK="viewXml()">
<p>
<script>loadTree();</script>
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr><td></td></tr>
  <tr>
    <td>
      <input type="button" name="cancel" value="<%=lbcancel%>"
      onclick="submitForm('cancel')">
      <% if (edit) { %>
      <input type="button" name="done" value="<%=lbdone%>"
      onclick="submitForm('done')">
      <% } else { %>
      <input type="button" name="prev" value="<%=lbprev%>"
      onclick="submitForm('prev')">
      <input type="button" name="next" value="<%=lbnext%>"
      onclick="submitForm('next')">
      <% } %>
    </td>
  </tr>
</table>
</form>
</div>
</body>
</html>
