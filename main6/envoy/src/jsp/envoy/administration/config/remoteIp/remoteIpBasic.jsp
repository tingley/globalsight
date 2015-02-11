<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp" import="com.globalsight.everest.webapp.javabean.NavigationBean,com.globalsight.everest.webapp.pagehandler.PageHandler,com.globalsight.everest.webapp.pagehandler.administration.config.remoteip.RemoteIpConstant,com.globalsight.everest.webapp.pagehandler.administration.config.remoteip.RemoteIp,java.util.*" session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);

	String saveURL = save.getPageURL() + "&action="
			+ RemoteIpConstant.SAVE;
	String previousURL = previous.getPageURL() + "&action="
    + RemoteIpConstant.PREVIOUS;
	String validateIp = self.getPageURL() + "&action="
			+ RemoteIpConstant.VALIDATE_IP;
    
	String helperText = bundle.getString("helper_text_remote_ip_basic");
	String confirmRemove = bundle.getString("msg_remove_remote_ip");
    
    RemoteIp remoteIp = (RemoteIp)request.getAttribute(RemoteIpConstant.REMOTE_IP);
    String title = null;
    String id = "-1";
    String ip = "";
    String desc = "";
    if (remoteIp != null) {
        title = bundle.getString("lb_edit_remote_ip");
        id = Long.toString(remoteIp.getId());
        ip = remoteIp.getIp();
        desc = remoteIp.getDescription();
    } else {
        title = bundle.getString("lb_add_remote_ip");
    }
%>
<%@page import="com.globalsight.util.edit.EditUtil"%>
<HTML>
<!-- remoteIp.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "systemParameter";
var helpFile = "<%=bundle.getString("help_remote_ip_create_edit")%>";

function conformRemove()
{
    return confirm('<%=confirmRemove%>');
}

function previous()
{
    addIpForm.action="<%=previousURL%>";
    addIpForm.submit();
}
function validateIp(submitForm)
{
    if (isEmptyString(addIpForm.ip.value))
    {
        if (submitForm)
        {
            alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_null_ip"))%>");
            addIpForm.ip.focus();
        }

        return;
    }
    
    dojo.xhrPost(
    {
        url:"<%=validateIp%>" + "&ip=" + addIpForm.ip.value + "&id=" + addIpForm.id.value,
        handleAs: "text", 
        load:function(data)
        {
            if (data=="")
            {
                if (submitForm)
                {
                    addIpForm.action="<%=saveURL%>";
                    addIpForm.submit();
                }
            }
            else
            {
                if (submitForm)
                {
                    alert(data);
                }
                else
                {
                    changeMsg(data);
                }
            }
        },
        error:function(error)
        {
            alert(error.message);
        }
    });
}

function setButtonState()
{
    var selectedIndex = new Array();
    var boxes = remoteIpForm.selectedIp;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                if (checkbox.checked) 
                    selectedIndex.push(i);
            }
        } 
        else 
        {
            if (boxes.checked) 
                selectedIndex.push(0);
        }
    }
    
    if (selectedIndex.length > 0)
        document.getElementById("idRemoveSubmit").disabled = false;
    else
        document.getElementById("idRemoveSubmit").disabled = true;      
}

function changeMsg(msg)
{
    var rootNode = document.getElementById("ipMsg");
    var msgNode = document.createTextNode(msg);
    if (rootNode.hasChildNodes())
    {
        rootNode.replaceChild(msgNode,rootNode.firstChild);
    }
    else
    {
        rootNode.appendChild(msgNode);
    }
}

function initIpMsg()
{
    changeMsg("");
}

function checkIp()
{
    validateIp(false);
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;"><amb:header title="<%=title%>" helperText="<%=helperText%>" />

<div style="float: left">
<form name="addIpForm" method="post">
<table class="standardText">
    <tr>
        <td>&nbsp;<input type="hidden" name="id" value="<%=id%>">  </td>
        <td class="errorMsg" id="ipMsg">&nbsp;</td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_ip")%><span class="asterisk">*</span>:</td>
        <td><input type="text" name="ip" maxlength="30" width="30px" onfocus="initIpMsg()" onblur="checkIp()" value="<%=ip%>"></td>
    </tr>
    <tr>
        <td valign="top"><%=bundle.getString("lb_description")%>:</td>
        <td><textarea rows="8" style="width: 300px;" name="description"><%=desc%></textarea></td>
    </tr>
    <tr>
        <td colspan="2"><input type="button" value="<%=bundle.getString("lb_previous") %>" onclick="previous()"><input type="button" value="<%=bundle.getString("lb_save") %>" onclick="validateIp(true)"></td>
    </tr>
</table>
</form>
</div>

</DIV>
</BODY>
</HTML>