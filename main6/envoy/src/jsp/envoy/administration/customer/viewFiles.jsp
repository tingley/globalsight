<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,
             com.globalsight.everest.servlet.util.SessionManager,                 
             com.globalsight.everest.webapp.WebAppConstants,
             com.globalsight.everest.webapp.javabean.NavigationBean,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
             com.globalsight.everest.webapp.pagehandler.administration.customer.SourceFile,
             com.globalsight.everest.webapp.pagehandler.administration.customer.SourceFileComparator,
             com.globalsight.util.resourcebundle.ResourceBundleConstants,
             com.globalsight.everest.webapp.webnavigation.LinkHelper,
             com.globalsight.everest.servlet.util.ServerProxy,
             com.globalsight.everest.servlet.EnvoyServletException,
             com.globalsight.everest.util.system.SystemConfigParamNames,
             com.globalsight.everest.util.system.SystemConfiguration,
             com.globalsight.everest.foundation.SearchCriteriaParameters,
             com.globalsight.util.GeneralException,
             java.util.ArrayList,
             java.util.List,
             java.util.Locale, 
             java.util.ResourceBundle"
    session="true" 
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="sourceFiles" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    String title= bundle.getString("lb_files");
                                 
    //Button names
    String okBtn = bundle.getString("lb_ok");

    //Urls of the links on this page
    String selfUrl = self.getPageURL();
    String okUrl = done.getPageURL() + "&action=ok";

    //Data
    String jobName = (String)sessionMgr.getAttribute("jobName");
    String targLocale = (String)sessionMgr.getAttribute("targLocale");
    
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var helpFile = "<%=bundle.getString("help_customer_view_files")%>";


</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
 ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" />

<FORM NAME="filesForm" METHOD="POST" action="<%=okUrl%>" >
    <table cellpadding=0 cellspacing=0 border=0 CLASS="standardText">
      <tr>
        <td><b><%=bundle.getString("lb_job")%>: </b><%=jobName%></td>
      </tr>
      <tr>
        <td><b><%=bundle.getString("lb_target_locale")%>: </b><%=targLocale%></td>
      </tr>
    </table>
<p>
    <table cellpadding=0 cellspacing=0 border=0 CLASS="standardText">
      <tr valign="top">    
         <td align="right">
            <amb:tableNav bean="sourceFiles" key="<%=SourceFile.FILE_KEY%>"
                 pageUrl="self" />
         </td>
      </tr>
        <tr>
          <td>
            <amb:table bean="sourceFiles" id="srcFile"
                 key="<%=SourceFile.FILE_KEY%>"
                 dataClass="com.globalsight.everest.webapp.pagehandler.administration.customer.SourceFile"
                 pageUrl="self" 
                 emptyTableMsg="msg_customer_jobs_empty" >
              <amb:column label="lb_name" width="180px" sortBy="<%=SourceFileComparator.NAME%>" >
                  <%= srcFile.getName() %>
              </amb:column>
              <amb:column label="lb_status" width="100px" sortBy="<%=SourceFileComparator.STATUS%>">
                <%= srcFile.getStatus() %>
              </amb:column>
            </amb:table>
          </td>
        </tr>
        <tr>
                      

<td style="padding-top:5px">
    <INPUT TYPE="submit" name="okBtn" VALUE="<%=okBtn%>" >
</td>
</tr>
</table>
</FORM>
</BODY>
</HTML>
