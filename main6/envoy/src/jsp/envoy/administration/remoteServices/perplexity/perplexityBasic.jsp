<%@page import="com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService"%>
<%@page import="java.text.MessageFormat"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.util.FormUtil,
    com.globalsight.util.edit.EditUtil,
    com.globalsight.cxe.entity.eloqua.EloquaConnector,
	java.util.*"
	session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="connect" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);

	String saveURL = save.getPageURL() + "&action=save";
	String connectURL = self.getPageURL() + "&action=connect";
	String connectURL2 = connect.getPageURL() + "&action=connect";
	String cancelURL = cancel.getPageURL() + "&action=cancel";
	String validateURL = self.getPageURL() + "&action=validate";
	String testURL = self.getPageURL() + "&action=test";
	
    String errorConnect = bundle.getString("error_eloqua_connector");
    
	String id = "-1";
	String name = "";
	String username = "";
	String password = "";
	String url = "";
	String desc = "";
	PerplexityService perplexity = (PerplexityService) request
			.getAttribute("perplexity");
    
	String title = "";
    boolean edit = false;
	if (perplexity != null) {
		edit = true;
		title = bundle.getString("lb_edit_perplexity");
        id = Long.toString(perplexity.getId());
		name = perplexity.getName();
        username = perplexity.getUserName();
        password = perplexity.getPassword();
        url = perplexity.getUrl();
        desc = perplexity.getDescription();
        
        desc = desc == null ? "" : desc;
	} else {
		title = bundle.getString("lb_new_perplexity");
	}
%>

<html>
<!-- perplexityBasic.jsp -->
<head>
<title><%=title%></title>
<style type="text/css">
.detailDivClass {
    float: left;
    left: 410px;
    position: absolute;
    width: 400px;
}
</style>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js" ></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<script>
var guideNode = "createEloquaJob";
var needWarning = true;
var objectName = "";
var typeIndex = 0;
var types = new Array();
var validateURL = "<%=validateURL%>";
var helpFile = "<%=bundle.getString("help_perplexity_add_modify")%>";

function validName() {
	
	var name = allTrim(form.name.value);
	if (hasSpecialChars(name))
    {
        alert("<%= bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
	
    var existNames = "<c:out value='${names}'/>";
    var lowerName = name.toLowerCase();
    existNames = existNames.toLowerCase();

    if (existNames.indexOf("," + lowerName + ",") != -1) {
        alert('<%=bundle.getString("jsmsg_duplicate_xmlrulefile")%>');
        form.name.value.focus();
        return false;
    }
    
    form.name.value = name;
    
	return true;
}

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        form.action = "<%=cancelURL%>";
        form.submit();
    }
    else if (formAction == "save")
    {
        if (confirmForm())
        {
        	testConnect();
        }
    }
}

function testConnect()
{
    $("#idDiv").mask("<%=bundle.getString("msg_connect")%>");
     $("#form").ajaxSubmit({  
           type: 'post',  
           url: "<%=testURL%>" , 
           dataType:'json',
           success: function(data){  
            $("#idDiv").unmask("<%=bundle.getString("msg_connect")%>");            
            if (data.canUse){
                    form.action = "<%=saveURL%>";
                    form.submit();
               }else{
            	   alert("<%=errorConnect%>");
               }
           },  
           error: function(XmlHttpRequest, textStatus, errorThrown){  
            $("#idDiv").unmask("<%=bundle.getString("msg_connect")%>");
               alert("<%=errorConnect%>");
           }  
       }); 
}

function confirmForm()
{
	 <%String msgTemp = bundle.getString("msg_validate_text_empty");%>
    if (isEmptyString(form.name.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_name")))%>");
        form.name.focus();
        return false;
    }
    
    if (!validName())
   	{
         form.name.focus();
         return false;
   	}

    if (isEmptyString(form.userName.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_user_name")))%>");
        form.userName.focus();
        return false;
    }

    if (isEmptyString(form.password.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_password")))%>");
        form.password.focus();
        return false;
    }

    if (isEmptyString(form.url.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_url")))%>");
        form.url.focus();
        return false;
    }

    return true;
}
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<div id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<amb:header title="<%=title%>" helperText="" /> 
<span class=errorMsg></span>

<div style="float: left;">
<FORM name="form" id="form" method="post" action="<%=saveURL%>">
<input type="hidden" name="id" value="<%=id%>"> 

<table class="standardText">
     <tr>
        <td >&nbsp;</td>
        <td class="errorMsg" id="nameMsg">&nbsp;</td>
    </tr>
	<tr>
		<td ><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
		<td><input type="text" name="name" id="name" value="<%=name%>"  maxlength="40" size="30"></td>
	</tr>

    <tr>
          <td valign="top">
            <%=bundle.getString("lb_description")%>:
          </td>
          <td>
            <textarea rows="4" cols="40" name="description"><%=desc%></textarea>
          </td>
        </tr>    
     <tr>
        <td><%=bundle.getString("lb_user_name")%><span class="asterisk">*</span>:</td>
        <td><input type="text" name="userName" id="userName" style="width: 360px;" value="<%=username%>" maxLength="200"></td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_password")%><span class="asterisk">*</span>:</td>
        <td><input type="password" name="password" id="password" style="width: 360px;" value="<%=password%>" maxLength="200"></td>
    </tr>
	<tr>
        <td><%=bundle.getString("lb_url")%><span class="asterisk">*</span>:</td>
        <td><input type="text" name="url" id="url" style="width: 360px;" value="<%=url%>" maxLength="200"></td>
    </tr>
    <tr>
        <td colspan="2" align="left">
            &nbsp;
        </td>
    </tr>
	<tr>
		<td colspan="2" align="left">
            <input type="button" name="return" value="<%=bundle.getString("lb_cancel")%>" onclick="submitForm('cancel')"/> 		    
		    <input type="button" name="saveBtn" value="<%=bundle.getString("lb_save")%>" onclick="submitForm('save')">
		</td>
	</tr>
</table>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_PERPLEXITY); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
</FORM>
</div>
</div>
</div>
</body>
</html>