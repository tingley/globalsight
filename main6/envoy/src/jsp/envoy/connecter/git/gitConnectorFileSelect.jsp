<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,java.io.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  com.globalsight.cxe.entity.gitconnector.GitConnector,
                  com.globalsight.connector.git.GitConnectorManagerLocal,
                  com.globalsight.connector.git.util.GitConnectorHelper,
                  java.util.ResourceBundle,
      			  com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
      			  com.globalsight.util.GlobalSightLocale"
          session="true" 
%>

<%
	ResourceBundle bundle = PageHandler.getBundle(session); 
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
	String title = bundle.getString("lb_select_file_path");
%>

<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%=title %></title>
	<style>
		body {font-size:12px}
		.{font-family:arial;font-size:12px}
		h1 {cursor:hand;font-size:16px;margin-left:10px;line-height:10px}
		xmp {color:green;font-size:12px;margin:0px;font-family:courier;background-color:#e6e6fa;padding:2px}
		.hdr{
			background-color:lightgrey;
			margin-bottom:10px;
			padding-left:10px;
		}
	</style>

	<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/tree/css/dhtmlXTree.css">
	<script  src="/globalsight/includes/tree/js/dhtmlXCommon.js"></script>
	<script  src="/globalsight/includes/tree/js/dhtmlXTree.js"></script>		
	<script  src="/globalsight/includes/tree/js/dhtmlXTreeExtend.js"></script>

</head>
<%
String mode = request.getParameter("mode");
String gitConnectorId = request.getParameter("gitConnectorId");
GitConnector gc = GitConnectorManagerLocal.getGitConnectorById(Long.parseLong(gitConnectorId));
GitConnectorHelper gcHelper = new GitConnectorHelper(gc);
String gitFolderPath = gcHelper.getGitFolder().getPath();
int length = gitFolderPath.length() + 1;
session.setAttribute("gitConnectorId", gitConnectorId);
%>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0">
	<table align="center" valign="center">
	  <tr><td>&nbsp;</td></tr>
	  <tr><td>&nbsp;</td></tr>
		<tr><td align=center><h2><%=title %></h2></td></tr>
		<tr>
			<td valign="top">
				<div id="treeboxbox_tree2" style="width:450; height:300;background-color:#f5f5f5;border :1px solid Silver;; overflow:auto;"></div>
			</td>
		</tr>
		<tr>
			<td align="center"><input type="button" name="ok" value="<%=bundle.getString("lb_select_n") %>" onclick="returnValue('<%=mode %>')"></td>
		</tr>
	</table>
	<script>
			tree2=new dhtmlXTreeObject("treeboxbox_tree2","100%","100%",0);
			tree2.setImagePath("/globalsight/includes/tree/imgs/");
			tree2.enableThreeStateCheckboxes(false);
			tree2.setXMLAutoLoading("/globalsight/envoy/connecter/git/gitConnectorGetFileList.jsp");	
			tree2.loadXML("/globalsight/envoy/connecter/git/gitConnectorGetFileList.jsp?id=");
	</script>
</body>
<script language="JavaScript">
function returnValue(mode) 
{
    if (mode == "Source") 
    {
        window.opener.currentForm.sourceMappingPath.value = tree2.getSelectedItemId().substring(<%=length%>);
    }
    else if (mode == "Target") 
    {
        window.opener.currentForm.targetMappingPath.value = tree2.getSelectedItemId().substring(<%=length%>);
    } 
    else
    {
        window.opener.currentForm.targetMappingPath<%=mode%>.value = tree2.getSelectedItemId().substring(<%=length%>);
    }
    window.close();
}
</script>
</html>
