<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="com.globalsight.everest.webapp.javabean.NavigationBean,
	com.globalsight.everest.servlet.util.SessionManager,
    com.globalsight.everest.webapp.WebAppConstants,
    com.globalsight.util.GlobalSightLocale,
	com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocaleConstant,
	com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocale,
	java.util.*"
	session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
	SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

	String saveURL = save.getPageURL() + "&action=" + UILocaleConstant.SAVE;
	String previousURL = previous.getPageURL() + "&action=" + UILocaleConstant.PREVIOUS;
	
	String title = bundle.getString("lb_new") + " " + bundle.getString("lb_uilocale_column_title");
	String helperText = bundle.getString("helper_text_uilocale_new");
	
	Vector locales = (Vector)request.getAttribute(UILocaleConstant.AVAILABLE_UILOCALES);
%>
<%@page import="com.globalsight.util.edit.EditUtil"%>
<HTML>
<!-- uilocalebasic.jsp -->
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

function setLongName()
{
	var sindex = addUILocaleForm.shortname.selectedIndex;
	var longnameTxt = addUILocaleForm.shortname.options[sindex].text;
	addUILocaleForm.longname.value = longnameTxt;
}

function setIsDefaultLocale()
{
	addUILocaleForm.defaultLocale.value = (addUILocaleForm.chbDefaultLocale.checked)? "true" : "false";
}

function previous()
{
	addUILocaleForm.action="<%=previousURL%>";
	addUILocaleForm.submit();
}

function submitForm()
{
	if (addUILocaleForm.shortname.selectedIndex < 0)
		return;
	
	addUILocaleForm.action="<%=saveURL%>";
	addUILocaleForm.submit();
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;"><amb:header title="<%=title%>" helperText="<%=helperText%>" />

<div style="float: left">
<form name="addUILocaleForm" method="post" action="">

<table border="0" cellspacing="4" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_uilocale_column_title")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="shortname" class="standardText" onchange="setLongName()">
            <%
                for (int i = 0; i < locales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)locales.elementAt(i);
                    out.println("<option value=\"" + locale.toString() + "\">" + 
                                locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
            <input type="hidden" name="longname" value=""></input>
            <input type="hidden" name="defaultLocale" value=""></input>
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_is_default")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="checkbox" name="chbDefaultLocale" onchange="setIsDefaultLocale()"></input>
          </td>
        </tr>
      <tr><td>&nbsp;</td></tr>
      <tr>
        <td>
          <input type="button" value="<%=bundle.getString("lb_previous") %>" onclick="previous()">
          <% if (userPerms.getPermissionFor(Permission.UILOCALE_NEW)) { %>
          <input type="button" value="<%=bundle.getString("lb_save") %>" onclick="submitForm()">
          <%} %>
        </td>
      </tr>
    </table>
</form>
</div>

</DIV>
</BODY>
</HTML>