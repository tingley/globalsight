<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="com.globalsight.everest.webapp.javabean.NavigationBean,
	com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocaleConstant,
	java.util.*"
	session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="add" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadres" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadres" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="setdefault" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String addURL = add.getPageURL() + "&action="
			+ UILocaleConstant.ADD;
	String downloadresURL = downloadres.getPageURL() + "&action="
			+ UILocaleConstant.DOWNLOAD_RES;
	String uploadresURL = uploadres.getPageURL() + "&action="
			+ UILocaleConstant.UPLOAD_RES;
	String removeURL = remove.getPageURL() + "&action="
			+ UILocaleConstant.REMOVE;
	String setdefaultURL = setdefault.getPageURL() + "&action="
			+ UILocaleConstant.SETDEFAULT;

	String title = bundle.getString("lb_uilocale_title");
	String helperText = bundle.getString("helper_text_uilocale_main");
	String confirmRemove = bundle.getString("msg_remove_uilocale");
%>
<%@page import="com.globalsight.everest.util.comparator.UILocaleComparator"%>

<HTML>
<!-- uiLocaleMain.jsp -->
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
var helpFile = "<%=bundle.getString("help_uilocale_main")%>";

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        uilocaleForm.action = "<%=addURL%>";
    }
    else
    {
        value = getRadioValue(uilocaleForm.selectedLocale);

        if (button == "UploadRes")
        {
            uilocaleForm.action = "<%=uploadresURL%>";
        }
        else if (button == "DownloadRes")
        {
            uilocaleForm.action = "<%=downloadresURL%>" + "&name=" + value;
        }
        else if (button == "SetDefault")
        {
        	uilocaleForm.action = "<%=setdefaultURL%>";
        }
        else if (button == "Remove")
        {
            if (isEnUs(value))
            {
                alert("<%=bundle.getString("msg_remove_uilocale_enus")%>");
                isOk = false;
            }
            else
            {
                isOk = confirm('<%=confirmRemove%>');
            }
            uilocaleForm.action = "<%=removeURL%>";
        }
    }

    if (isOk)
    {
        uilocaleForm.submit();
    }
}

function isEnUs(val)
{
	return "en_US" == val;
}

function setButtonState(srcEle)
{
	var isdefault = false;
	var isEnus = false;
    if (srcEle)
    {
    	isdefault = "true" == srcEle.attributes["isdefault"].value;
    	isEnus = isEnUs(srcEle.value);
    }

    if (uilocaleForm.remBtn) uilocaleForm.remBtn.disabled = isdefault || isEnus;
    if (uilocaleForm.downBtn) uilocaleForm.downBtn.disabled = false;
    if (uilocaleForm.setdefaultBtn) uilocaleForm.setdefaultBtn.disabled = false;
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<!-- uiLocaleMain -->
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;"><amb:header title="<%=title%>" helperText="<%=helperText%>" />

<div id="fileListDiv" class="standardText" style="float: left; text-align: right; margin-left: 0px;">
<form name="uilocaleForm" method="post" action="<%=addURL%>">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
    <tr valign="top">
        <td align="right"><amb:tableNav bean="<%=UILocaleConstant.UILOCALES%>" key="<%=UILocaleConstant.UILOCALE_KEY%>" pageUrl="self" /></td>
    </tr>
    <tr>
        <td>
        <amb:table bean="<%=UILocaleConstant.UILOCALES%>" id="uilocalekeyForConfig" key="<%=UILocaleConstant.UILOCALE_KEY%>" dataClass="com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocale" pageUrl="self" emptyTableMsg="">
            <amb:column label="" width="10px">
                <input type="radio" name="selectedLocale" value="<%=uilocalekeyForConfig.getShortName()%>" isdefault="<%=uilocalekeyForConfig.isDefaultLocale() %>" onclick="setButtonState(this);">
            </amb:column>
            <amb:column label="lb_uilocale_column_title" sortBy="<%=UILocaleComparator.NAME%>" width="350px">
                <%=uilocalekeyForConfig.getLongName()%>
            </amb:column>
            <amb:column label="lb_is_default" sortBy="<%=UILocaleComparator.ISDEFAULT%>">
                <%if(uilocalekeyForConfig.isDefaultLocale()){%>
                	<IMG SRC="/globalsight/images/checkmark.gif" HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>
                <%}%>
            </amb:column>
        </amb:table>
        
        <div style="float: right;">
          <% if (userPerms.getPermissionFor(Permission.UILOCALE_REMOVE)) { %>  
          <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>" name="remBtn" disabled onclick="submitForm('Remove');">
          <% } %> 
          <% if (userPerms.getPermissionFor(Permission.UILOCALE_DOWNLOAD_RES)) { %>  
          <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_download_resource")%>" name="downBtn" disabled onclick="submitForm('DownloadRes');">
          <% } %> 
          <% if (userPerms.getPermissionFor(Permission.UILOCALE_UPLOAD_RES)) { %> 
          <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_upload_resource")%>" name="upBtn" onclick="submitForm('UploadRes');">
          <% } %> 
          <% if (userPerms.getPermissionFor(Permission.UILOCALE_SET_DEFAULT)) { %> 
          <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_set_default")%>" name="setdefaultBtn" disabled onclick="submitForm('SetDefault');">
          <% } %> 
          <% if (userPerms.getPermissionFor(Permission.UILOCALE_NEW)) { %>  
          <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." onclick="submitForm('New');">
          <% } %> 
        </div>
        </td>
    </tr>
</table>
</form>
</div>

</DIV>
</BODY>
</HTML>