<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  java.util.ResourceBundle,
                  com.globalsight.everest.cvsconfig.*,
                  com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.modulemapping.ModuleMappingConstants,
		      			  com.globalsight.everest.cvsconfig.modulemapping.*,
		      			  com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
		      			  com.globalsight.util.GlobalSightLocale"
          session="true" 
%>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="previous" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
%>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%=bundle.getString("lb_rss_job") %></title>
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
	
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>
    
<script language="JavaScript">
var needWarning = true;
var objectName = "selectFile";
var guideNode="rename";
var helpFile = "";
</script>
</head>

<%

String doneUrl = done.getPageURL() + "&action=next";
String previousUrl = previous.getPageURL() + "&action=previous";
String cancelUrl = cancel.getPageURL() + "&action=cancel";

SessionManager sessionMgr =
    (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
//Get base information
String jobName = (String)sessionMgr.getAttribute("jobName");
String sourceLocale = (String)sessionMgr.getAttribute("srcLocale");
String projectId = (String)sessionMgr.getAttribute("projectId");
String projectName = (String)sessionMgr.getAttribute("projectName");
String notes = (String)sessionMgr.getAttribute("notes");
session.setAttribute("jobName", jobName);
session.setAttribute("sourceLocale", sourceLocale);
session.setAttribute("projectId", projectId);
session.setAttribute("projectName", projectName);
session.setAttribute("notes", notes);
%>
<script language="JavaScript">
function submitForm(action) {
	if (action == "done") {
		form1.selectFiles.value = tree2.getAllCheckedLeaf();
		if (form1.selectFiles.value == "") {
			alert("Please select some files");
			return false;
		}
		form1.action = "<%=doneUrl%>";
	}	else if (action == "previous")
			form1.action= "<%=previousUrl%>";
			else 
				form1.action="<%=cancelUrl%>";

	form1.submit();
}
</script>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
  <amb:header title="<%=bundle.getString("lb_cvs_job_file_select") %>"/>

<form name="form1" method="post" >
  <input type="hidden" name="selectFiles" value="">
  <input type="hidden" name="baseInfo" value="">

	<table width="80%" align="left" valign="center">
	  <tr>
	  	<td align="left" width="30%" class="standardText"><%=bundle.getString("lb_job_name") %>:</td>
	  	<td align="left" width="70%" class="standardText"><%=jobName %></td>
	  </tr>
	  <tr>
	  	<td align="left" class="standardText"><%=bundle.getString("lb_project") %>:</td>
	  	<td align="left" class="standardText"><%=projectName %></td>
	  </tr>
	  <tr>
	  	<td align="left" class="standardText"><%=bundle.getString("lb_cvs_mm_source_locale") %>:</td>
	  	<td align="left" class="standardText"><%=sourceLocale %></td>
	  </tr>
	  <tr>
	  	<td align="left" class="standardText"><%=bundle.getString("lb_cvs_job_notes") %>:</td>
	  	<td align="left" class="standardText"><%=notes %></td>
	  </tr>
		<tr>
			<td valign="top" colspan=2>
				<div align="left">
					<div id="treeboxbox_tree2" style="width:650; height:400;background-color:#f5f5f5;border :1px solid Silver;; overflow:auto;"></div>
					<script>
							tree2=new dhtmlXTreeObject("treeboxbox_tree2","100%","100%",0);
							tree2.setImagePath("/globalsight/includes/tree/imgs/");
							tree2.enableCheckBoxes(1);
							tree2.enableThreeStateCheckboxes(true);
							tree2.setXMLAutoLoading("/globalsight/envoy/rss/getFileList.jsp");	
							tree2.loadXML("/globalsight/envoy/rss/getFileList.jsp?id=");
					</script>
				</div>
			</td>
		</tr>
		<tr><td colspan="2">&nbsp;</td></tr>
		<tr>
			<td colspan="2">
			  <div align="center">
  			    <input type="button" name="cancel" value="<%=bundle.getString("lb_cancel") %>" onclick="submitForm('cancel')">&nbsp;&nbsp;
			    <input type="button" name="previous" value="<%=bundle.getString("lb_previous") %>" onclick="submitForm('previous')">&nbsp;&nbsp;
   			    <input type="button" name="done" value="<%=bundle.getString("lb_done") %>" onclick="submitForm('done')">
			  </div>
			</td>
		</tr>
	</table>
	</form>
</DIV>
</body>
</html>
