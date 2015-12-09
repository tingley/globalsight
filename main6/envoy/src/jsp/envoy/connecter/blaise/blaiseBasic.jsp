<%@page import="java.text.MessageFormat"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
     com.globalsight.util.edit.EditUtil,
     com.globalsight.util.GlobalSightLocale,
     com.globalsight.everest.webapp.WebAppConstants,
     com.globalsight.cxe.entity.blaise.BlaiseConnector,
	 java.util.*"
	session="true"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>

<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

	// URLs
	String saveURL = save.getPageURL() + "&action=save";
	String cancelURL = cancel.getPageURL() + "&action=cancel";
	String testURL = self.getPageURL() + "&action=test";

	// Labels
	String helper = bundle.getString("helper_text_blaise_connector_edit");
    String errorConnect = bundle.getString("error_blaise_connector");

    String title = null;
	String id = "-1";
	String name = "";
	String desc = "";
	String url = "";
	String username = "";
	String password = "";
	String clientCoreVersion = "2.0";// default "2.0".
	long companyId = -1;
    boolean edit = false;
	BlaiseConnector connector = (BlaiseConnector) request.getAttribute("blaise");
	if (connector != null)
	{
		edit = true;
		title = bundle.getString("lb_edit_blaise_connector");
        id = Long.toString(connector.getId());
		name = connector.getName();
        username = connector.getUsername();
        password = connector.getPassword();
        url = connector.getUrl();
        companyId = connector.getCompanyId();
        desc = connector.getDescription();
        desc = desc == null ? "" : desc;
        clientCoreVersion = connector.getClientCoreVersion();
	}
	else
	{
		title = bundle.getString("lb_new_blaise_connector");
	}
%>

<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js" ></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<script>
var guideNode = "Blaise";
var needWarning = false;
var helpFile = "<%=bundle.getString("help_blaise_connector_basic")%>";

function cancel()
{
	$("#blaiseForm").attr("action", "<%=cancelURL%>").submit();
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
    $("#idDiv").mask("<%=bundle.getString("msg_blaise_wait_connect")%>");
    $("#blaiseForm").ajaxSubmit({
        type: 'post',  
        url: "<%=testURL%>",
        dataType:'json',
        timeout:100000000,
        success: function(data){
        $("#idDiv").unmask("<%=bundle.getString("msg_blaise_wait_connect")%>");            
            if("" == data.error)
            {
            	$("#blaiseForm").attr("action", "<%=saveURL%>").submit();
            }
            else
            {
                alert(data.error);
            }
        },
        error: function(XmlHttpRequest, textStatus, errorThrown){
            $("#idDiv").unmask("<%=bundle.getString("msg_blaise_wait_connect")%>");
            alert("<%=errorConnect%>");
        }
    });
}

function confirmForm()
{
    <%String msgTemp = bundle.getString("msg_validate_text_empty");%>
    if (isEmptyString(blaiseForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_name")))%>");
        blaiseForm.name.focus();
        return false;
    }

    if (!validName())
    {
    	blaiseForm.name.focus();
        return false;
    }

    if (isEmptyString(blaiseForm.url.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_url")))%>");
        blaiseForm.url.focus();
        return false;
    }

    if (isEmptyString(blaiseForm.username.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_user_name")))%>");
        blaiseForm.username.focus();
        return false;
    }

    if (isEmptyString(blaiseForm.password.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_password")))%>");
        blaiseForm.password.focus();
        return false;
    }

    return true;
}
														
// Ensure the name has no special chars and not an existed one already.
function validName()
{
    var name = allTrim(blaiseForm.name.value);
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
        blaiseForm.name.value.focus();
        return false;
    }

    blaiseForm.name.value = name;

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

    <FORM name="blaiseForm" id="blaiseForm" method="post" action="">
    <input type="hidden" name="id" value="<%=id%>" />
	<input type="hidden" name="clientCoreVersion" value="<%=clientCoreVersion%>" />
    <%if(edit) {%>
    <input type="hidden" name="companyId" value="<%=companyId%>" />
    <%} %>
    <table class="standardText">
    	<tr>
    		<td width="120px"><%=bundle.getString("lb_name")%> <span class="asterisk">*</span>:</td>
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
            <td><input type="text" name="username" id="username" style="width: 360px;" value="<%=username%>" maxLength="200" autocomplete="off"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_password")%><span class="asterisk">*</span>:</td>
            <td><input type="password" name="password" id="password" style="width: 360px;" value="<%=password%>" maxLength="200" autocomplete="off"></td>
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