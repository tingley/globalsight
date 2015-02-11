<%@ page
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*, com.globalsight.everest.foundation.User,
                com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>

<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="prev" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelEdit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelNew" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    String edit = (String) sessionMgr.getAttribute("edit");
    String customTitle = (String) sessionMgr.getAttribute("customPageTitle");
   
    String doneURL = done.getPageURL() + "&action=doneCustom";
    String prevURL = prev.getPageURL() + "&action=prev";
    String nextURL = next.getPageURL() + "&action=nextCustom";
    String cancelURL;
    if (edit != null)
        cancelURL = cancelEdit.getPageURL() + "&action=cancelEdit";
    else
        cancelURL = cancelNew.getPageURL() + "&action=cancelNew";
    String title = null;
    if (edit != null)
    {
        title= bundle.getString("lb_edit") + " " +
                bundle.getString("lb_vendor") + " - " + customTitle;
    }
    else
    {
        title= bundle.getString("lb_new") + " " +
                bundle.getString("lb_vendor") + " - " + customTitle;
    }
    
    String pageContent = (String) request.getAttribute("pageContent");

%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

var needWarning = true;
var objectName = "<%= bundle.getString("lb_vendor") %>";
var guideNode = "";
var helpFile = "<%=bundle.getString("help_vendors_contact_information")%>";

function submitForm(btnName) {
    if (btnName == "done")
    {
        customForm.action = "<%=doneURL %>";
    }
    else if (btnName == "prev")
    {
        customForm.action = "<%=prevURL %>";
    }
    else if (btnName == "next")
    {
        customForm.action = "<%=nextURL %>";
    }
    customForm.submit();
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<br>
<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P></P>

     <TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="standardText">
        <FORM NAME="customForm" METHOD="post">
            <input type="hidden" name="fromCustom" value="true">
            <%= pageContent %>
        </FORM>
        <TR>
        <TD COLSPAN="2">&nbsp;
        </TD>
        </TR>
        <TR>
            <TD COLSPAN="2">
                <INPUT TYPE="BUTTON" NAME='<%=bundle.getString("lb_cancel")%>'
                    VALUE='<%=bundle.getString("lb_cancel")%>' 
                    ONCLICK="location.replace('<%=cancelURL%>')">
<%
                if (edit == null) {
%>
                    <INPUT TYPE="BUTTON" NAME='<%=bundle.getString("lb_previous")%>'
                        VALUE='<%=bundle.getString("lb_previous")%>' 
                        ONCLICK="javascript: submitForm('prev')">
                    <INPUT TYPE="BUTTON" NAME='<%=bundle.getString("lb_next")%>'
                        VALUE='<%=bundle.getString("lb_next")%>' 
                        ONCLICK="javascript: submitForm('next')">
<%              } else { %>
                <INPUT TYPE="BUTTON" NAME='<%=bundle.getString("lb_done")%>'
                    VALUE='<%=bundle.getString("lb_done")%>' 
                    ONCLICK="javascript: submitForm('done')">
<%              }  %>
            </TD>
        </TR>
        </TABLE>
</DIV>
</BODY>
</HTML>
