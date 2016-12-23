<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,
                 com.globalsight.everest.company.Company,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 com.globalsight.util.StringUtil,
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
var objectName = "";
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
  		
<%--   		<tr valign="top">
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
  		</tr> --%>
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
