<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="exportConfig" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="importConfig" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
	String title = bundle.getString("lb_config_export_import");
	String helperText =  bundle.getString("helper_text_config_list");
    String lbexport = bundle.getString("lb_export");
    String lbimport = bundle.getString("lb_import");
	String exportURL = exportConfig.getPageURL() + "&action=export";
    String importsUrl = importConfig.getPageURL() + "&action=import";
%>
<html>
<head>
<title><%=title%></title>
 <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
<script SRC="/globalsight/jquery/jquery-1.11.3.min.js"></script>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<script>
var needWarning = true;
var objectName = "Configuration Export/Import";
var guideNode="configExportImport";
var helpFile = "<%=bundle.getString("help_configuration_main_screen")%>";
	//Move option from f to t
	function move(f,t) {
		var $from = $("#" + f + " option:selected");
		var $to = $("#" + t);
		if ($from.length>0) {
			$from.each(function() {
				$to.append("<option value='" + $(this).val() + "' title='"+$(this).text()+"'>" + $(this).text()+"</option>");
				$(this).remove();
			});
		}
	}


	function doExport(){
		var value = findSelectedElement();
		if(value.length == 0){
			alert("Please select at least one option!");
			return;
		}
		configForm.action = "<%=exportURL%>" + "&id=" + value;
		configForm.submit();
	}

	function doImport(){
		configForm.action = "<%=importsUrl%>" + "&action=import";
		configForm.submit();
	}
	
	function findSelectedElement()
	{
	    var ids = "";
	    $("select[name$=To]").find("option").each(function ()
	    {
	        ids+=$(this).val()+",";
	    });
	    if (ids != "")
	        ids = ids.substring(0, ids.length - 1);

	    return ids;
	}

function initConfigShow(id, dif)
{
	  if ($("#"+dif).css("display") == "none") {
          $("#"+dif).css("display", "block");
          $("#"+id).find("img").attr("src", "/globalsight/images/ecllapse.jpg");
      }
      else {
          $("#"+dif).css("display", "none");
          $("#"+id).find("img").attr("src", "/globalsight/images/enlarge.jpg");
      }
}
</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<div id="MainTitle" class="mainHeading">
	<%=title%>
</div>
<br/>
<div id="MainHelperText" class="standardText" style="">
	<%=helperText%>
</div>
<form name="configForm" method="post" action="">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">

		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowAttr" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowAttr','AttrPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_attributes")%></b>
                </div>
                <div id="AttrPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_attributes")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_attributes")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="attrFrom" name="attrFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${allAttributes}">
	      					<option title="${op.displayName}" value="attr-${op.id}">${op.displayName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('attrFrom','attrTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('attrTo','attrFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="attrTo" name="attrTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowAttrSet" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowAttrSet','AttrSetPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_attribute_groups")%></b>
                </div>
                <div id="AttrSetPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_attribute_groups")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_attribute_groups")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="AttrSetFrom" name="AttrSetFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${allAttributeSets}">
	      					<option title="${op.name}" value="attrSet-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('AttrSetFrom','AttrSetTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('AttrSetTo','AttrSetFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="AttrSetTo" name="AttrSetTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
        <tr valign="top">
    		<td colspan=3>
                 <br/>        
               <div id="toShowLocalePair" style="cursor:pointer;font-weight:bold;display:inline-block;"
               								  onclick="initConfigShow('toShowLocalePair','localePairPanel')">
                <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                <%=bundle.getString("lb_locale_pairs")%>
                </div>
    			<br/>
                <div id="localePairPanel" style="display:none;">
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all")+ " "+ bundle.getString("lb_locale_pairs")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_locale_pairs")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="localePairFrom" name="localePairFrom" multiple class="standardText" size="10" style="width:370">
        				<c:forEach var="op" items="${localPairs}">
	      					<option title="${op.source.displayName} -> ${op.target.displayName}" value="localePair-${op.id}">${op.source.displayName} -> ${op.target.displayName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('localePairFrom','localePairTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('localePairTo','localePairFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="localePairTo" name="localePairTo" multiple class="standardText" size="10" style="width:370">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowActivity" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowActivity','activityPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_activity_types")%></b>
                </div>
                <div id="activityPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_activity_types")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_activity_types")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="activityFrom" name="activityFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${activities}">
	      					<option title="${op.displayName}" value="activity-${op.name}">${op.displayName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('activityFrom','activityTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('activityTo','activityFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="activityTo" name="activityTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowCurr" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowCurr','CurrPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_currency")%></b>
                </div>
                <div id="CurrPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_currency")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_currency")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="CurrFrom" name="CurrFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${currencies}">
	      					<option title="${op.name}" value="curr-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('CurrFrom','CurrTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('CurrTo','CurrFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="CurrTo" name="CurrTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowRate" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowRate','RatePanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_rates")%></b>
                </div>
                <div id="RatePanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_rates")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_rates")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="rateFrom" name="rateFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${rates}">
	      					<option title="${op.name}" value="rate-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('rateFrom','rateTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('rateTo','rateFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="rateTo" name="rateTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowPermission" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowPermission','permissionPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_permission_groups")%></b>
                </div>
                <div id="permissionPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_permission_groups")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_permission_groups")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="permissionFrom" name="permissionFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${permissionGroups}">
	      					<option title="${op.name}" value="perm-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('permissionFrom','permissionTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('permissionTo','permissionFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="permissionTo" name="permissionTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowUser" style="cursor:pointer;display:inline-block;"
                						onclick="initConfigShow('toShowUser','userPanel')">
                    <span style="display:show;" onclick="ab()"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_users")%></b>
                </div>
                <div id="userPanel" style="display:none;">
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_users")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_users")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="userFrom" name="userFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${users}">
	      					<option title="${op.userName}" value="user-${op.userId}">${op.userName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('userFrom','userTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('userTo','userFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="userTo" name="userTo" multiple class="standardText" size="10" style="width:250" mytype="to">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowTM" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowTM','TMPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_tm")%></b>
                </div>
                <div id="TMPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_tms")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_tms")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="tmFrom" name="tmFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${projectTMs}">
	      					<option title="${op.name}" value="tm-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('tmFrom','tmTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('tmTo','tmFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="tmTo" name="tmTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowSRX" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowSRX','SRXPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_segmentation_rules")%></b>
                </div>
                <div id="SRXPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_segmentation_rules")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_segmentation_rules")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="srxFrom" name="srxFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${segRules}">
	      					<option title="${op.name}" value="srx-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('srxFrom','srxTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('srxTo','srxFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="srxTo" name="srxTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowTMP" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowTMP','TMPPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_tm_profiles")%></b>
                </div>
                <div id="TMPPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_tm_profiles")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_tm_profiles")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="tmpFrom" name="tmpFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${tmProfiles}">
	      					<option title="${op.name}" value="tmp-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('tmpFrom','tmpTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('tmpTo','tmpFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="tmpTo" name="tmpTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                 <br/>        
               <div id="toShowMTProfile" style="cursor:pointer;display:inline-block;" 
               								onclick="initConfigShow('toShowMTProfile','mtprofilePanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_mt_profiles")%></b>
                </div>
                <div id="mtprofilePanel" style="display:none;">
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_mt_profiles")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_mt_profiles")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="mtprofileFrom" name="mtprofileFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${mtProfiles}">
	      					<option title="${op.mtProfileName}" value="mt-${op.id}">${op.mtProfileName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('mtprofileFrom','mtprofileTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('mtprofileTo','mtprofileFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="mtprofileTo" name="mtprofileTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                 <br/>        
               <div id="toShowPS" style="cursor:pointer;display:inline-block;" 
               								onclick="initConfigShow('toShowPS','psPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_perplexity_services")%></b>
                </div>
                <div id="psPanel" style="display:none;">
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_perplexity_services")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_perplexity_services")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="psFrom" name="psFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${perplexityServices}">
	      					<option title="${op.name}" value="ps-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('psFrom','psTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('psTo','psFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="psTo" name="psTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowTerm" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowTerm','TermPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_terminology")%></b>
                </div>
                <div id="TermPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_terminology")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_terminology")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="TermFrom" name="TermFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${termBases}">
	      					<option title="${op.name}" value="term-${op.termbaseId}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('TermFrom','TermTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('TermTo','TermFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="TermTo" name="TermTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowPro" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowPro','ProPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_projects")%></b>
                </div>
                <div id="ProPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_projects")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_projects")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="projectFrom" name="projectFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${projects}">
	      					<option title="${op.name}" value="pro-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('projectFrom','projectTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('projectTo','projectFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="projectTo" name="projectTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowWF" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowWF','WFPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_workflows")%></b>
                </div>
                <div id="WFPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_workflows")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_workflows")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="wfFrom" name="wfFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${wfTemplates}">
	      					<option title="${op.name}" value="wf-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('wfFrom','wfTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('wfTo','wfFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="wfTo" name="wfTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowWspf" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowWspf','WspfPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_workflow_state_post_profiles")%></b>
                </div>
                <div id="WspfPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_workflow_state_post_profiles")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_workflow_state_post_profiles")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="wspfFrom" name="wspfFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${wfstatePostProfiles}">
	      					<option title="${op.name}" value="wspf-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('wspfFrom','wspfTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('wspfTo','wspfFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="wspfTo" name="wspfTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowLP" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowLP','LPPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_loc_profiles")%></b>
                </div>
                <div id="LPPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_loc_profiles")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_loc_profiles")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="lpFrom" name="lpFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${locProfiles}">
	      					<option title="${op.name}" value="lp-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('lpFrom','lpTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('lpTo','lpFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="lpTo" name="lpTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowFP" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowFP','FPPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_file_profiles")%></b>
                </div>
                <div id="FPPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_file_profiles")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_file_profiles")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="fpFrom" name="fpFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${fileProfiles}">
	      					<option title="${op.name}" value="fp-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('fpFrom','fpTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('fpTo','fpFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="fpTo" name="fpTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		 <tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowXR" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowXR','XRPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_xml_rules")%></b>
                </div>
                <div id="XRPanel" style="display:none;">
               <table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_xml_rules")%>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_xml_rules")%>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="xrFrom" name="xrFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${xmlruleFiles}">
	      					<option title="${op.name}" value="xr-${op.id}">${op.name}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('xrFrom','xrTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('xrTo','xrFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="xrTo" name="xrTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowFilter" style="cursor:pointer;display:inline-block;"
                							onclick="initConfigShow('toShowFilter','filterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_configuration")%>s</b>
                </div>
                <div id="filterPanel" style="display:none;">
                	<jsp:include page="configFilter.jsp"/>
                </div>
    		</td>
  		</tr>
  		
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr>
            <td colspan="3">
                <input type="button" name="<%=lbexport%>" value="<%=lbexport%>" onclick="doExport();">
                <input type="button" name="<%=lbimport%>" value="<%=lbimport%>" onclick="doImport();">
            </td>
        </tr>

      </table>
    </td>
  </tr>
  
</table>
</form>
</div>
</body>
</html>
