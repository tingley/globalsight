<%@page import="java.text.MessageFormat"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
    com.globalsight.util.edit.EditUtil,
    com.globalsight.cxe.entity.mindtouch.MindTouchConnector,
    com.globalsight.cxe.entity.mindtouch.MindTouchConnectorTargetServer,
     com.globalsight.util.GlobalSightLocale,
     com.globalsight.everest.webapp.WebAppConstants,
	java.util.*"
	session="true"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>

<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

	String saveURL = save.getPageURL() + "&action=save";
	String cancelURL = cancel.getPageURL() + "&action=cancel";
	String testURL = self.getPageURL() + "&action=test";
	
	String helper = bundle.getString("helper_text_mindtouch_connector_edit");
    String errorConnect = bundle.getString("error_mindtouch_connector");

    String title = null;
	String id = "-1";
	String name = "";
	String username = "";
	String password = "";
	String url = "";
	String desc = "";
	String postToSourceServer = "";
	String isPostToSourceServer = "false";
	long companyId = -1;
	MindTouchConnector connector = (MindTouchConnector) request.getAttribute("mindtouch");
    boolean edit = false;
	if (connector != null)
	{
		edit = true;
		title = bundle.getString("lb_edit_mindtouch_connector");
        id = Long.toString(connector.getId());
		name = connector.getName();
        username = connector.getUsername();
        password = connector.getPassword();
        url = connector.getUrl();
        companyId = connector.getCompanyId();
        desc = connector.getDescription();
        desc = desc == null ? "" : desc;
        if(connector.getIsPostToSourceServer())
        {
        	postToSourceServer = "checked";
        	isPostToSourceServer = "true";
        }
	}
	else
	{
		title = bundle.getString("lb_new_mindtouch_connector");
	}
	
	List<MindTouchConnectorTargetServer> targetServers = (List<MindTouchConnectorTargetServer>) request.getAttribute("targetServers");
	String targetLocaleStr = "";
	if(targetServers != null && targetServers.size() > 0)
	{
		for(MindTouchConnectorTargetServer ts: targetServers)
		{
			targetLocaleStr += ts.getTargetLocale() + ",";
		}
	}
	
	 Vector<GlobalSightLocale> targetLocales = (Vector)request.getAttribute("targetLocales");

	 Vector<GlobalSightLocale> allAvailableLocales = (Vector) request.getAttribute("allAvailableLocales");
%>

<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js" ></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<script>
var guideNode = "MindTouch";
var needWarning = false;
var helpFile = "<%=bundle.getString("help_mindtouch_connector_basic")%>";
var targetLocaleStr = "<%=targetLocaleStr%>";
var availableLocaleMap = {};
<%
	for (GlobalSightLocale locale : allAvailableLocales)
{%>
	availableLocaleMap['<%=locale.toString()%>'] = '<%=locale.getDisplayName(uiLocale)%>';
<%
}
%>

function cancel()
{
	$("#mindtouchForm").attr("action", "<%=cancelURL%>").submit();
}

function save()
{
    if (confirmForm())
    {
        testConnect();
    }
}

function testConnect()
{
    $("#idDiv").mask("<%=bundle.getString("msg_mindtouch_wait_connect")%>");
    $("#targetLocaleStr").val(targetLocaleStr);
    $("#mindtouchForm").ajaxSubmit({
        type: 'post',  
        url: "<%=testURL%>" , 
        dataType:'json',
        timeout:100000000,
        success: function(data){
        $("#idDiv").unmask("<%=bundle.getString("msg_mindtouch_wait_connect")%>");            
            if("" == data.error)
            {
            	$("#mindtouchForm").attr("action", "<%=saveURL%>").submit();
            }
            else
            {
                alert(data.error);
            }
        },
        error: function(XmlHttpRequest, textStatus, errorThrown){
            $("#idDiv").unmask("<%=bundle.getString("msg_mindtouch_wait_connect")%>");
            alert("<%=errorConnect%>");
        }
    });
}

function confirmForm()
{
    <%String msgTemp = bundle.getString("msg_validate_text_empty");%>
    if (isEmptyString(mindtouchForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_name")))%>");
        mindtouchForm.name.focus();
        return false;
    }

    if (!validName())
    {
        mindtouchForm.name.focus();
        return false;
    }

    if (isEmptyString(mindtouchForm.url.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_url")))%>");
        mindtouchForm.url.focus();
        return false;
    }

    if (isEmptyString(mindtouchForm.username.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_user_name")))%>");
        mindtouchForm.username.focus();
        return false;
    }

    if (isEmptyString(mindtouchForm.password.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_password")))%>");
        mindtouchForm.password.focus();
        return false;
    }

    return true;
}
														
// Ensure the name has no special chars and not an existed one already.
function validName()
{
    var name = allTrim(mindtouchForm.name.value);
    if (hasSpecialChars(name))
    {
        alert("<%=bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }

    var existNames = "<c:out value='${names}'/>";
    var lowerName = name.toLowerCase();
    existNames = existNames.toLowerCase();

    if (existNames.indexOf("," + lowerName + ",") != -1)
    {
        alert('<%=bundle.getString("msg_duplicate_name")%>');
        mindtouchForm.name.value.focus();
        return false;
    }

    mindtouchForm.name.value = name;

    return true;
}

function changePostToSourceServer()
{
	if($("#postToSourceServer").is(':checked') == true)
	{
		$("#isPostToSourceServer").val("true");
	}
	else
	{
		$("#isPostToSourceServer").val("false");
	}
}

var trnode=$("<tr class='standardText' style='height:15pt;'><td></td><td></td><td></td><td></td><td align='center'></td></tr>");

function add()
{
	if(!confirmTargetServer()) {
		return;
	}
	
	var targetLocale = $("#targetLocales").val();
	if (isEmptyString(targetLocale)) {
		return;
	}

	var idPageHtml=$("#idPageHtml");
	var temp=trnode.clone(true);
	var targetUrl = $("#targetUrl").val();
	var targetUsername = $("#targetUsername").val();
	var targetPassword = $("#targetPassword").val();
	
	temp.attr("id","tr"+targetLocale);
	temp.children('td').eq(0).html(targetLocale + "<input type='hidden' name='targetLocale" + targetLocale + "' value='" + targetLocale+"'>");
	temp.children('td').eq(1).html(targetUrl + "<input type='hidden' name='targetUrl"+targetLocale+"' value='"+targetUrl+"'>");
	temp.children('td').eq(2).html(targetUsername + "<input type='hidden' name='targetUsername"+targetLocale+"' value='"+targetUsername+"'>");
	temp.children('td').eq(3).html("******<input type='hidden' name='targetPassword"+targetLocale+"' value='"+targetPassword+"'>");
	temp.children('td').eq(4).html("<a href='#' onclick='removetest(\""+targetLocale+"\")'>X</a>");

	idPageHtml.append(temp);
	
	targetLocaleStr = targetLocaleStr + "," + targetLocale;
	$("#targetLocales option:selected").remove();
}

function confirmTargetServer()
{
	if (isEmptyString(mindtouchForm.targetUrl.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_url")))%>");
        return false;
    }
	
    if (isEmptyString(mindtouchForm.targetUsername.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_user_name")))%>");
        return false;
    }
	
    if (isEmptyString(mindtouchForm.targetPassword.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_password")))%>");
        return false;
    }

    return true;
}

function removetest(targetLocale)
{
	// add option back to "Target Locale" select options
	$("#targetLocales").append("<option value='" + targetLocale + "'>" + availableLocaleMap[targetLocale] + "</option>");

	$("tr[id=tr"+targetLocale+"]").remove();
	targetLocaleStr = targetLocaleStr.replace(targetLocale,"");
}

$(document).ready(function ()
{
	// Source username/password as default for target servers.
	$("#targetUsername").val($("#username").val());
	$("#targetPassword").val($("#password").val());

	$("#username").blur(function () {
    	$("#targetUsername").val($("#username").val());
    });

	$("#password").blur(function () {
		$("#targetPassword").val($("#password").val());
	});
});
</script>
</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<div id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helper%>" />

    <FORM name="mindtouchForm" id="mindtouchForm" method="post" action="">
    <input type="hidden" name="id" value="<%=id%>" />
    <input type="hidden" id="isPostToSourceServer" name="isPostToSourceServer" value="<%=isPostToSourceServer%>"/>
    <input type="hidden" id="targetLocaleStr" name="targetLocaleStr" value="">
    <%if(edit) {%>
    <input type="hidden" name="companyId" value="<%=companyId%>" />
    <%} %>
    <table class="standardText">
    	<tr>
    		<td width="70"><%=bundle.getString("lb_name")%> <span class="asterisk">*</span>:</td>
    		<td><input type="text" name="name" id="name" value="<%=name%>"  maxlength="40" size="30"></td>
    	</tr>
        <tr>
            <td valign="top"><%=bundle.getString("lb_description")%>:</td>
            <td><textarea rows="4" cols="40" name="description"><%=desc%></textarea></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_url")%><span class="asterisk">*</span>:</td>
            <td><input type="text" name="url" id="url" style="width: 360px;" value="<%=url%>" maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_user_name")%><span class="asterisk">*</span>:</td>
            <td><input type="text" name="username" id="username" style="width: 360px;" value="<%=username%>" maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_password")%><span class="asterisk">*</span>:</td>
            <td><input type="password" name="password" id="password" style="width: 360px;" value="<%=password%>" maxLength="200"></td>
        </tr>
        <tr>
        	<td colspan="2" align="left"><%=bundle.getString("lb_post_to_source_server")%>:<input type="checkbox" name="postToSourceServer" id="postToSourceServer" <%=postToSourceServer%> onclick="changePostToSourceServer()"></td>
        </tr>
    </table>
	<br/>
    <table class="standardText">
        <tr><td align="left"><b>Target Servers (Translated target pages will be posted to target servers by locale):</b></td></tr>
        <tr>
            <td>
            <TABLE class="listborder" CELLSPACING="0" CELLPADDING="0" BORDER="1" style="border-color: lightgrey; border-collapse: collapse; border-style: solid; border-width: 1px;">
			  <THEAD>
			    <TR CLASS="tableHeadingGray" style="height:15pt;">
			      <TD ALIGN="LEFT" WIDTH="100px">Target Locale</TD>
			      <TD ALIGN="LEFT" WIDTH="300px">URL</TD>
			      <TD ALIGN="LEFT" WIDTH="100px">User Name</TD>
			      <TD ALIGN="LEFT" WIDTH="60px">Password</TD>
			      <TD ALIGN="LEFT" WIDTH="45px">Delete</TD>
			    </TR>
			  </THEAD>
			  <TBODY id="idPageHtml">
			  <% if(targetServers != null && targetServers.size() > 0) {
				  for(MindTouchConnectorTargetServer ts :targetServers)
				  {%>
			  		<tr class="standardText" style="height:15pt;" id="tr<%=ts.getTargetLocale()%>">
			  		<td><%=ts.getTargetLocale()%><input type= 'hidden' name='targetLocale<%=ts.getTargetLocale()%>' value='<%=ts.getTargetLocale()%>'></td>
			  		<td><%=ts.getUrl()%><input type= 'hidden' name='targetUrl<%=ts.getTargetLocale()%>' value='<%=ts.getUrl()%>'></td>
			  		<td><%=ts.getUsername()%><input type= 'hidden' name='targetUsername<%=ts.getTargetLocale()%>' value='<%=ts.getUsername()%>'></td>
			  		<td>******<input type= 'hidden' name='targetPassword<%=ts.getTargetLocale()%>' value='<%=ts.getPassword()%>'></td>
			  		<td align='center'><a href='#' onclick="removetest('<%=ts.getTargetLocale()%>')">X</a></td>
			  		</tr>
			  <% }} %>
			  </TBODY>
			</TABLE>
            </td>
        </tr>
    </table>
    <table class="standardText">
        <tr>
            <td colspan="2" align="left">&nbsp;</td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_target_locale")%>:</td>
            <td style="font-size: 7pt;">
	            <select id="targetLocales" name="targetLocales">
	            <%for(GlobalSightLocale targetLocale: targetLocales) {
	            	if (targetLocaleStr.indexOf(targetLocale.toString()) == -1) {
	            %>
	            <option value="<%= targetLocale.toString()%>"><%= targetLocale.getDisplayName(uiLocale)%> </option>
	            <%}} %>
	            </select>
            </td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_url")%>:</td>
            <td><input type="text" name="targetUrl" id="targetUrl" style="width: 360px;" maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_user_name")%>:</td>
            <td><input type="text" name="targetUsername" id="targetUsername" style="width: 360px;" maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_password")%>:</td>
            <td><input type="password" name="targetPassword" id="targetPassword" style="width: 360px;" maxLength="200"></td>
        </tr>
        <tr>
        	<td colspan="2" align="left"><input type="button" name="addTarget" value="<%=bundle.getString("lb_add")%>" onclick="add()"/></td>
        </tr>
        <tr>
            <td colspan="2" align="left">&nbsp;</td>
        </tr>
    	<tr>
    		<td colspan="2" align="left">
                <input type="button" name="return" value="<%=bundle.getString("lb_cancel")%>" onclick="cancel();"/>
    		    <input type="button" name="saveBtn" value="<%=bundle.getString("lb_save")%>" onclick="save();"/>
    		</td>
    	</tr>
    </table>
    </FORM>
</div>
</div>
</body>
</html>