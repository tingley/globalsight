<%@page import="java.text.MessageFormat"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
     com.globalsight.everest.servlet.util.SessionManager,
     com.globalsight.util.GlobalSightLocale,
     com.globalsight.everest.webapp.WebAppConstants,
     com.globalsight.cxe.entity.blaise.BlaiseConnector,
	 java.util.*"
	session="true"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="exportConfig" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="importConfig" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String title = bundle.getString("lb_config_export_import");
	String helperText =  bundle.getString("helper_text_config_list");
	SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
	String exportURL = exportConfig.getPageURL() + "&action=export";
    String importsUrl = importConfig.getPageURL() + "&action=import";
%>

<html>
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<title><%=title%></title>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "configExportImport";
var helpFile = "<%=bundle.getString("help_configuration_main_screen")%>";
var checkNewMessage;

function doExport(){
	var value = findSelectedElement();
	if(value.length == 0){
		alert("Please select at least one option!");
		return;
	}
configListForm.action = "<%=exportURL%>" + "&id=" + value;
	configListForm.submit();
}

function doImport(){
	configListForm.action = "<%=importsUrl%>";
	configListForm.submit();
}

function findSelectedElement()
{
    var ids = "";
    $("input[name='checkboxBtn']:checked").each(function ()
    {
        ids += $(this).val() + ",";
    });
    if (ids != "")
        ids = ids.substring(0, ids.length - 1);

    return ids;
}

function handleSelectAll(type){
	if("user"==type)
	{
		var ch = $("#allUser").attr("checked");
		if (ch == "checked") {
			$("[mytype='user']").attr("checked", true);
		} else {
			$("[mytype='user']").attr("checked", false);
		}
	}else if("localePair"==type)
	{
		var ch = $("#allLocalePair").attr("checked");
		if (ch == "checked") {
			$("[mytype='localePair']").attr("checked", true);
		} else {
			$("[mytype='localePair']").attr("checked", false);
		}
	}else if("mt"==type)
	{
		var ch = $("#allMT").attr("checked");
		if (ch == "checked") {
			$("[mytype='mt']").attr("checked", true);
		} else {
			$("[mytype='mt']").attr("checked", false);
		}
	}else if("config"==type)
	{
		var ch = $("#allConfig").attr("checked");
		if (ch == "checked") {
			$("[mytype='config']").attr("checked", true);
		} else {
			$("[mytype='config']").attr("checked", false);
		}
	}
}
</SCRIPT>
<style>

#Main label{
  	display:inline-block;
	width: 15em;
}

</style>
<%@ include file="/envoy/common/warning.jspIncl" %>
</head>
<body">
	<%@ include file="/envoy/common/header.jspIncl" %>
	<%@ include file="/envoy/common/navigation.jspIncl" %>
	<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9px; top: 108px; left: 20px; right: 20px;">
    <form id="configListForm" name="configListForm" method="post" class="standardText">
		<div id="Main">
			<div id="MainTitle" class="mainHeading">
				<%=title%>
			</div>
			<br/>
			<div id="MainHelperText" class="standardText" style="">
				<%=helperText%>
			</div>
			<br/>
			<div>
				<INPUT TYPE="checkbox" onclick="handleSelectAll('localePair')" id="allLocalePair"><label><B><%=bundle.getString("lb_locale_pairs")%></B></label><BR/>
				<table>
				<c:forEach items="${localPairs}" var="item" varStatus="status">
					<c:if test="${status.count eq 1 || (status.count-1) % 4 eq 0}">    
			      		<tr/>    
			     	</c:if>
					<td class="standardText"><INPUT TYPE="checkbox"  NAME="checkboxBtn" id ="localePair-${status.count}" VALUE="localePair-${item.id}" mytype="localePair"><c:out value="${item.source.displayName} -> ${item.target.displayName}"/><td>
					<c:if test="${status.count % 4 eq 0 || status.count eq 4}">
					<tr/>
					</c:if>
				</c:forEach>
				</table>
			</div>
			<br/>
			<div>
				<INPUT TYPE="checkbox" onclick="handleSelectAll('user')" id="allUser"><label><b><%=bundle.getString("lb_users")%></b></label><BR/>
				<table>
				<c:forEach items="${users}" var="item" varStatus="status">
					<td class="standardText"><INPUT TYPE="checkbox"  NAME="checkboxBtn" id="user-${item.userId}" VALUE="user-${item.userId}" mytype="user"><c:out value="${item.userName}"/></td>
				</c:forEach>
				</table>
			</div>
			<br/>
			<div>
				<INPUT TYPE="checkbox" onclick="handleSelectAll('mt')" id="allMT"><label><b><%=bundle.getString("lb_mt_profiles")%></b></label><BR/>
				<table>
				<c:forEach items="${mtProfiles}" var="item" varStatus="status">
					<td class="standardText"><INPUT TYPE="checkbox"  NAME="checkboxBtn" id="mt-${status.count }" value="mt-${item.id }" mytype="mt"><c:out value="${item.mtProfileName}"/></td>
				</c:forEach>
				</table>
			</div>
			<br/>
			<div>
				<INPUT TYPE="checkbox" onclick="handleSelectAll('config')" id="allConfig"><label><b><%=bundle.getString("lb_filter_configuration")%></b></label><br/>
				<table>
				<c:forEach items="${filters}" var="map" varStatus="status1">
					<br/>
					&nbsp;&nbsp;&nbsp;&nbsp;<label><b><c:out value="${map.key}"/></b></label><br/>
					<table>
						<c:forEach items="${map.value}" var="item" varStatus="status">
							<td class="standardText"><INPUT TYPE="checkbox"  NAME="checkboxBtn" id="filter-${status.count }" value="filter-${item.filterTableName }-${item.id}" mytype="config"><c:out value="${item.filterName}"/></td>
							<c:if test="${status.count % 10 eq 0 || status.count eq 10}">
								<tr/>
							</c:if>
						</c:forEach>
					</table>
				</c:forEach>
				</table>
			</div>
		</div>
		<BR/><br/>
		<div id='FormButton'>
			<input type="button" id="exportConfig" value="Export" onclick="doExport();"/>
			<input type="button" id="importConfig" value="Import" onclick="doImport();"/>
		</div>
	</form>
</div>
</body>
</html>