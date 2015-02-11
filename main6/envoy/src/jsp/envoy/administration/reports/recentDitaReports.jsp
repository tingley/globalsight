<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
       		 java.util.ResourceBundle,
       		 java.text.MessageFormat"
          session="true"
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
	String taskId = (String) request.getParameter("taskId");
%>
<html>
<!-- This JSP is: /envoy/administration/reports/recentDitaReports.jsp -->
<head>
<title><%=bundle.getString("lb_recent_reports")%></title>
<style type="text/css">
table td,table td * {
	vertical-align: top;
}

.btnCancel {
	width: 60px; font-size: smaller;
}
</style>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.cookie.js"></script>

<link href="/globalsight/jquery/dynatree-1.2.4/skin-vista/ui.dynatree.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.dynatree.min.js"></script>
<script type="text/javascript">

var defaultWidth = "98%";
var defaultHeight = "60%";

// Adds JS endsWith function.
String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

$(document).ready(function () {
	// Set CSS Value
	$("#treeDIV").width(defaultWidth);
	$("#treeDIV").height(defaultHeight);

    // --- Initialize Report Files tree
    $("#treeDIV").dynatree({
        title: "Lazy loading sample",
        persist: true,
        checkbox: true,
        selectMode: 3,
        initAjax: {
            url: "/globalsight/ControlServlet?activityName=ditaReports&action=view&taskId=<%=taskId%>"
        },
        onSelect: function(select, node) {
            // Get a list of all selected nodes, and convert to a key array:
            var selKeys = $.map(node.tree.getSelectedNodes(), function(node){
              return node.data.key;
            });

            // Get a list of all selected TOP nodes
            var selRootNodes = node.tree.getSelectedNodes(true);
            // ... and convert to a key array:
            var selRootKeys = $.map(selRootNodes, function(node){
              return node.data.key;
            });
        },
        onDblClick: function(node, event) {
            node.toggleSelect();
        },
        onKeydown: function(node, event) {
			if( event.which == 32 ) {
              node.toggleSelect();
              return false;
            }
        }
    });

    // Expand All Button
    $("#expandBtn").click(function () {
    	$("#treeDIV").dynatree("getRoot").visit(function (node) {
    	    node.expand(true);
    	});
    });
    
    // Collapse All Button
    $("#collapseBtn").click(function () {
    	$("#treeDIV").dynatree("getRoot").visit(function (node) {
    	    node.expand(false);
    	});
    });
});

function fnDownload(){
	var selNodes = $("#treeDIV").dynatree("getSelectedNodes");
	if(selNodes.length == 0)
		alert("<%=bundle.getString("msg_select_report")%>");
	var selectedReports = selNodes[0].data.key;
	for(var i=1; i<selNodes.length; i++){
		selectedReports += ("," + selNodes[i].data.key);
	}
		
	$("#selReports").val(selectedReports);
	recentReportsForm.action = "/globalsight/ControlServlet?activityName=ditaReports&action=download&taskId=<%=taskId%>";
	recentReportsForm.submit();
}

function fnDelete(){
	var selNodes = $("#treeDIV").dynatree("getSelectedNodes");
	if(selNodes.length == 0)
		alert("<%=bundle.getString("msg_select_report")%>");
	var selectedReports = selNodes[0].data.key;
	for(var i=1; i<selNodes.length; i++){
		selectedReports += ("," + selNodes[i].data.key);
	}
		
	//alert(selectedReports);
	$("#selReports").val(selectedReports);
	recentReportsForm.action = "/globalsight/ControlServlet?activityName=ditaReports&action=delete&taskId=<%=taskId%>";
	recentReportsForm.submit();
}

function fnSelectAll(){
	var isChecked = $("#control").is(":checked");
	$("#treeDIV").dynatree("getRoot").visit(function(node){
		node.select(isChecked);
	});
}

function fnReload()
{
	// Reload Reports Folder Tree.
	$("#treeDIV").dynatree("getTree").reload();
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="recentReportsForm" method="post" action="">
    <input type="hidden" id="selReports" name="selReports">
</form>
<TABLE>
<TR><TD class="standardText"><%=bundle.getString("lb_recent_dita_reports_desc")%></TD></TR>
</TABLE>
<br/>
<table CELLSPACING="0" CELLPADDING="0" style="border:0px solid black">
<tr VALIGN="middle">
	<td style="border:1px solid black;width:50px;height:20px;background-color:#738eb5;" align="center">
		<input type="checkbox" id="control" title="Select/Deselect All" onClick="fnSelectAll();">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="expandBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/expand_all.gif')" title="Expand All">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="collapseBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/collapse_all.gif')" title="Collapse All">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="removeFile" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/trash.png')" title="Delete" onClick="fnDelete();">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="refreshFile" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/refresh.png')" title="Reload" onClick="fnReload();">
	</td>
</tr>
<tr><td style="height:3px" colspan="7"></td></tr>
</table>

<div id="treeDIV">
<!-- When using initAjax, it may be nice to put a throbber here, that spins until the initial content is loaded: -->
</div>

<div>&nbsp;</div>
<div align="center">
    <input type="BUTTON" VALUE="<%=bundle.getString("lb_download")%>" onClick="fnDownload();">&nbsp;&nbsp;
    <input type="BUTTON" VALUE="<%=bundle.getString("lb_close")%>" onClick="window.close();">
</div>
<body>
</HTML>
