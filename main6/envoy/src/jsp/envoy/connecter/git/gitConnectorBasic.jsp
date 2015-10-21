<%@page import="java.text.MessageFormat"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
    com.globalsight.util.edit.EditUtil,
    com.globalsight.cxe.entity.gitconnector.GitConnector,
	java.util.*"
	session="true"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>

<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);

	String saveURL = save.getPageURL() + "&action=save";
	String cancelURL = cancel.getPageURL() + "&action=cancel";
	String testURL = self.getPageURL() + "&action=test";
	
	String helper = bundle.getString("helper_text_git_connector_edit");
    String errorConnect = bundle.getString("error_git_connector");

    String title = null;
	String id = "-1";
	String name = "";
	String username = "";
	String password = "";
	String email = "";
	String branch = "master";
	String url = "";
	String desc = "";
	String privateKeyFile = "";
	long companyId = -1;
	GitConnector connector = (GitConnector) request.getAttribute("gitConnector");
    boolean edit = false;
	if (connector != null)
	{
		edit = true;
		title = bundle.getString("lb_edit_git_connector");
        id = Long.toString(connector.getId());
		name = connector.getName();
        username = connector.getUsername();
        password = connector.getPassword();
        email = connector.getEmail();
        branch = connector.getBranch();
        privateKeyFile = connector.getPrivateKeyFile();
        url = connector.getUrl();
        companyId = connector.getCompanyId();
        desc = connector.getDescription();
        desc = desc == null ? "" : desc;
	}
	else
	{
		title = bundle.getString("lb_new_git_connector");
	}
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
var guideNode = "GitConnector";
var needWarning = false;
var helpFile = "<%=bundle.getString("help_git_connector_basic")%>";

function cancel()
{
	$("#gitForm").attr("action", "<%=cancelURL%>").submit();
}

function save()
{
    if (confirmForm())
    {
    	<%if(edit){%>
	    	$("#branch").attr("disabled",false);
    	<%}%>
        testConnect();
    }
}

function testConnect()
{
	$("#idDiv").mask("<%=bundle.getString("msg_git_wait_connect")%>");
    var url = "<%=testURL%>";
    $("#gitForm").ajaxSubmit({
        type: 'post',  
        url: url , 
        dataType:'json',
        timeout:100000000,
        success: function(data){
        $("#idDiv").unmask("<%=bundle.getString("msg_git_wait_connect")%>");            
            if("" == data.error)
            {
            	$("#gitForm").attr("action", "<%=saveURL%>").submit();
            }
            else
            {
                alert(data.error);
            }
        },
        error: function(XmlHttpRequest, textStatus, errorThrown){
            $("#idDiv").unmask("<%=bundle.getString("msg_git_wait_connect")%>");
            alert("<%=errorConnect%>");
        }
    });
}

function confirmForm()
{
    <%String msgTemp = bundle.getString("msg_validate_text_empty");%>
    if (isEmptyString(gitForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_name")))%>");
        gitForm.name.focus();
        return false;
    }

    if (!validName())
    {
        gitForm.name.focus();
        return false;
    }

    if (isEmptyString(gitForm.url.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_url")))%>");
        gitForm.url.focus();
        return false;
    }

    if (isEmptyString(gitForm.branch.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_branch")))%>");
        gitForm.branch.focus();
        return false;
    }
    
    if(!isEmptyString(gitForm.email.value) && !validEmail(gitForm.email.value))
    {
    	alert("<%=bundle.getString("jsmsg_email_invalid")%>");
    	gitForm.email.focus();
        return false;
    }
    
    var url = $("#url").val();
    if (url.indexOf("http") != 0 && isEmptyString(gitForm.privateKeyFile.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_private_key_file_path")))%>");
        gitForm.privateKeyFile.focus();
        return false;
    }

    if(url.indexOf("http") == 0)
    {
    	var end = url.indexOf("@");
    	if(end > 0)
    	{
	    	var start = 7;
	    	if(url.indexOf("https") == 0)
	    	{
	    		start = 8;
	    	}
	    	
	    	var temp = url.substring(start, end);
	    	if(temp.indexOf(":") > 0)
	    	{
	    		var temp2 = $("#username").val() + ":" + $("#password").val();
	    		if(temp2 != temp)
	    		{
	    			alert("User name or password doesn’t match the value in URL, please check them again.");
	    			return false;
	    		}
	    	}
	    	else
	    	{
	    		if($("#username").val() != temp)
	    		{
	    			alert("User name doesn’t match the name in URL, please check it again.");
	    			return false;
	    		}
	    	}
    	}
    	
    	$("#privateKeyFile").val("");
    }

    return true;
}

// Ensure the name has no special chars and not an existed one already.
function validName()
{
    var name = allTrim(gitForm.name.value);
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
        gitForm.name.value.focus();
        return false;
    }

    gitForm.name.value = name;

    return true;
}
</script>
</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<div id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helper%>" />

    <div style="float: left;">
    <FORM name="gitForm" id="gitForm" method="post" action="">
    <input type="hidden" name="id" id="id" value="<%=id%>" />
    <%if(edit){%>
    <input type="hidden" name="companyId" value="<%=companyId%>" />
    <%}%>
    <table class="standardText">
    	<tr>
    		<td ><%=bundle.getString("lb_name")%> <span class="asterisk">*</span>:</td>
    		<td><input type="text" name="name" id="name" value="<%=name%>"  maxlength="40" size="30"></input></td>
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
            <td><%=bundle.getString("lb_branch")%><span class="asterisk">*</span>:</td>
            <td><input type="text" name="branch" id="branch" style="width: 360px;" value="<%=branch%>" <% if(edit){%>disabled<%} %> maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_user_name")%>:</td>
            <td><input type="text" name="username" id="username" style="width: 360px;" value="<%=username%>" maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_password")%>:</td>
            <td><input type="password" name="password" id="password" style="width: 360px;" value="<%=password%>" maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_email")%>:</td>
            <td><input type="text" name="email" id="email" style="width: 360px;" value="<%=email%>" maxLength="200"></td>
        </tr>
        <tr id="privateKeyFilePathTr">
            <td><%=bundle.getString("lb_private_key_file_path")%><span class="asterisk">*</span>:</td>
            <td><input type="text" name="privateKeyFile" id="privateKeyFile" style="width: 360px;" value="<%=privateKeyFile%>" maxLength="300" ></td>
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
</div>
<script>
var url = $("#url").val();
if(url.indexOf("http") == 0)
{
	$("#privateKeyFilePathTr").hide();
}
else
{
	$("#privateKeyFilePathTr").show();
}

$("#url").keyup(function(){
	var url = $("#url").val();
	if(url.indexOf("http") == 0)
	{
		$("#privateKeyFilePathTr").hide();
	}
	else
	{
		$("#privateKeyFilePathTr").show();
	}
});

$("#url").keydown(function(){
	var url = $("#url").val();
	if(url.indexOf("http") == 0)
	{
		$("#privateKeyFilePathTr").hide();
	}
	else
	{
		$("#privateKeyFilePathTr").show();
	}
});

$("#url").focus(function(){
	var url = $("#url").val();
	if(url.indexOf("http") == 0)
	{
		$("#privateKeyFilePathTr").hide();
	}
	else
	{
		$("#privateKeyFilePathTr").show();
	}
});

$("#url").blur(function(){
	var url = $("#url").val();
	if(url.indexOf("http") == 0)
	{
		$("#privateKeyFilePathTr").hide();
	}
	else
	{
		$("#privateKeyFilePathTr").show();
	}
});
</script>
</body>
</html>