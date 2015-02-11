<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,java.util.ResourceBundle,com.globalsight.util.edit.EditUtil,com.globalsight.everest.servlet.util.SessionManager,com.globalsight.everest.webapp.pagehandler.PageHandler,com.globalsight.everest.webapp.WebAppConstants,com.globalsight.everest.company.CompanyThreadLocal,com.globalsight.everest.company.CompanyWrapper"
    session="true"
%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	SessionManager sessionMgr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);

	String tmName = (String) sessionMgr
			.getAttribute(WebAppConstants.TM_TM_NAME);
    boolean isAdmin = ((Boolean)sessionMgr.getAttribute("isAdmin")).booleanValue();
    boolean isSuperAdmin = ((Boolean)sessionMgr.getAttribute("isSuperAdmin")).booleanValue();;
	String urlNext = next.getPageURL();
	String urlCancel = cancel.getPageURL();

	String lb_title = bundle.getString("lb_reindex_tm");
    String allTmsLabel = "lb_all_tms_accessible";
    if (isSuperAdmin) {
        allTmsLabel = "lb_all_tms";
    } else if(isAdmin) {
        allTmsLabel = "lb_all_tms_company";
    }
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=bundle.getString("lb_tm_reindex")%></TITLE>
<STYLE>
{ font: Tahoma Verdana Arial 10pt; }
INPUT, SELECT { font: Tahoma Verdana Arial 10pt; }

LEGEND        { font-size: smaller; font-weight: bold; }
.link         { color: blue; cursor: hand; }
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_reindex")%>";


function doCancel()
{   
    oForm.action = "<%=urlCancel%>";
    oForm.submit();
}

function doNext()
{
    var url = '<%= urlNext + "&" + WebAppConstants.TM_ACTION +
        "=" + WebAppConstants.TM_ACTION_REINDEX_START %>';

    oForm.action = url;
    oForm.submit();
}

function initSelection()
{
    oTmAll = document.getElementById("idAllTms");
    oTmSelected = document.getElementById("idSelectedTm");
<%  if ("".equals(tmName)) { %>
        oTmAll.checked = true;
        oTmSelected.disabled = true;
<%  } else { %>
        oTmSelected.checked = true;
<%  } %>

}


function doOnLoad()
{
   // Load the Guides
   loadGuides();

   initSelection();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    <DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
    <BR>

<FORM NAME="oForm" ACTION="" METHOD="post">

<div style="margin-bottom:10px">
    <%=bundle.getString("lb_select_tm_to_reindex")%>:
    <BR>
    <div style="margin-left: 40px">
        <TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="">
            <tr>
                <td>
                    <input type="radio" name="oTm" id="idAllTms" value="idAllTms"/>        
                </td>
                <td colspan="2">
                    <label for="idAllTms"><%=bundle.getString(allTmsLabel)%> </label>
                </td>
            </tr>
            <tr>
                <td valign="top"><input type="radio" name="oTm" id="idSelectedTm" value="idSelectedTm"/></td>
                <td valign="top"><label for="idSelectedTm">Selected TM(s):</label></td>
                <td><%="".equals(tmName)?"null":tmName%></td>
            </tr>
            <tr>
                <td><input type="checkbox" name="indexTarget" id="idIndexTarget"></td>
                <td colspan="2">Index Target</td>
            </tr>
        </TABLE>
    </div>
</div>
</FORM>

<BR>

    <DIV id="idButtons" align="left">
        <button TABINDEX="0" onclick="doCancel();"><%=bundle.getString("lb_cancel")%></button>&nbsp;
        <button TABINDEX="0" onclick="doNext();"><%=bundle.getString("lb_next")%></button>
    </DIV>
</DIV>
</BODY>
</HTML>
