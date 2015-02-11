<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp" import="com.globalsight.everest.webapp.javabean.NavigationBean,com.globalsight.everest.webapp.pagehandler.PageHandler,com.globalsight.everest.webapp.pagehandler.administration.config.remoteip.RemoteIpConstant,java.util.*" session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="add" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String addURL = add.getPageURL() + "&action="
			+ RemoteIpConstant.ADD;
	String editURL = add.getPageURL() + "&action="
			+ RemoteIpConstant.EDIT;
	String removeURL = remove.getPageURL() + "&action="
			+ RemoteIpConstant.REMOVE;

	String title = bundle.getString("lb_remote_ip_webservices");
	String helperText = bundle.getString("helper_text_remote_ip_main");
	String confirmRemove = bundle.getString("msg_remove_remote_ip");
%>
<%@page import="com.globalsight.everest.util.comparator.RemoteIpComparator"%>
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
var helpFile = "<%=bundle.getString("help_remote_ip_main")%>";

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        remoteIpForm.action = "<%=addURL%>";
    }
    else
    {
        value = getRadioValue(remoteIpForm.selectedIp);

        if (button == "Edit")
        {
            remoteIpForm.action = "<%=editURL%>" + "&id=" + value;
        }
        else if (button == "Remove")
        {
            isOk = confirm('<%=confirmRemove%>');
            remoteIpForm.action = "<%=removeURL%>";
        }
    }

    if (isOk)
    {
        remoteIpForm.submit();
    }
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
        remoteIpForm.remBtn.disabled = false;
    else
        remoteIpForm.remBtn.disabled = true; 
        
    if (selectedIndex.length == 1)
        remoteIpForm.editBtn.disabled = false;
    else
        remoteIpForm.editBtn.disabled = true; 
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

//for GBS-2599
function handleSelectAll() {
    if (remoteIpForm.selectAll.checked) {
    	checkAllWithName('remoteIpForm', 'selectedIp'); setButtonState();
    }
    else {
    	clearAll('remoteIpForm'); setButtonState();
    }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;"><amb:header title="<%=title%>" helperText="<%=helperText%>" />

<div id="fileListDiv" class="standardText" style="float: left; text-align: right; margin-left: 20px;">
<form name="remoteIpForm" method="post" action="<%=removeURL %>">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
    <tr valign="top">
        <td align="right"><amb:tableNav bean="<%=RemoteIpConstant.REMOTE_IP_LIST %>" key="<%=RemoteIpConstant.REMOTE_IP_KEY%>" pageUrl="self" /></td>
    </tr>
    <tr>
        <td><amb:table bean="remoteIps" id="remoteIp" key="<%=RemoteIpConstant.REMOTE_IP_KEY%>" dataClass="com.globalsight.everest.webapp.pagehandler.administration.config.remoteip.RemoteIp" pageUrl="self" emptyTableMsg="msg_no_remote_Ip">
            <amb:column label="checkbox">
                <input type="checkbox" name="selectedIp" value="<%=remoteIp.getId()%>" onclick="setButtonState();">
            </amb:column>
            <amb:column label="lb_remote_ip" sortBy="<%=RemoteIpComparator.IP%>" width="150px">
                <%=remoteIp.getIp()%>
            </amb:column>
            <amb:column label="lb_description" sortBy="<%=RemoteIpComparator.DESC%>" width="400px">
                <%
                	out.print(remoteIp.getDescription() == null ? ""
                						: EditUtil.encodeTohtml(remoteIp
                								.getDescription()));
                %>
            </amb:column>
        </amb:table>

        <div style="float: right;"><INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>" name="remBtn" disabled onclick="submitForm('Remove');"> <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..." name="editBtn" disabled onclick="submitForm('Edit');"> <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." onclick="submitForm('New');"></div>
        </td>
    </tr>
</table>
</form>
</div>

</DIV>
</BODY>
</HTML>