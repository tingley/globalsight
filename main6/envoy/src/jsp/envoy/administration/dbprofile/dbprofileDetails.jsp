<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
		 com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
		 com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl,
         com.globalsight.cxe.entity.databasecolumn.DatabaseColumn,
		 com.globalsight.everest.webapp.pagehandler.PageHandler, 
		 com.globalsight.util.collections.HashtableValueOrderWalker,
		 java.lang.Long, java.util.Locale,
         java.util.ResourceBundle"
		 session="true" %>

<jsp:useBean id="ok" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    
    SessionManager sessionMgr = (SessionManager)request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);
    DatabaseProfileImpl profile = (DatabaseProfileImpl)request.getAttribute("profile");
    String title = bundle.getString("msg_profile_details");
    HashtableValueOrderWalker l10n_pairs = (HashtableValueOrderWalker)
                      sessionMgr.getAttribute("L10nProfilePairs");
    Long l10nID = new Long((long)profile.getL10nProfileId());
    String locprofile = (String)l10n_pairs.get(l10nID);
    Collection dbcolumns = (Collection)request.getAttribute("dbcolumns");
    HashtableValueOrderWalker modePairs = (HashtableValueOrderWalker)
                      request.getAttribute("modePairs");
    String okURL = ok.getPageURL();
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT language="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_db_profile") %>";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_db_profiles_basic_information")%>";


</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P>
<form method="post" action="<%=okURL%>">
<table class="standardText" border=0>
    <tr>
        <td>
            <%=bundle.getString("lb_name")%>:
        </td>
        <td>
            <b><%=profile.getName()%></b>
        </td>
    </tr>
    <tr>
        <td>
            <%=bundle.getString("lb_loc_profile")%>:
        </td>
        <td>
            <b><%=locprofile%><b>
        </td>
    </tr>
    <tr><td>&nbsp;</td></tr>
    <tr>
        <td>
            <%=bundle.getString("lb_db_column_profiles")%>:
        </td>
    </tr>
    <tr>
        <td colspan=2>
          <table cellspacing="0" cellpadding="6" border="0" class="listborder">
            <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
                <td><%=bundle.getString("lb_column")%></td>
                <td><%=bundle.getString("lb_table")%></td>
                <td><%=bundle.getString("lb_mode")%></td>
            </tr>
            <tr>
              <td>
<%
    int i = 0;
    for (Iterator iter = dbcolumns.iterator(); iter.hasNext(); )
    {
        String classStyle = "tableRowOdd";
        DatabaseColumn dbCol = (DatabaseColumn)iter.next();
        if (i++ % 2 == 0) classStyle = "tableRowEven";
%>
            <tr class=<%=classStyle%>>
                <td class="standardText" width=50>
                    <%= dbCol.getColumnName() %>
                </td>
                <td class="standardText" width=50>
                    <%= dbCol.getTableName() %>
                </td>
                <td class="standardText" width=50>
                    <%
                        Integer mode = new Integer((int)dbCol.getContentMode());
                        out.print(modePairs.get(mode));
                    %>
                </td>
            </tr>
<% } %>
              </td>
            </tr>
        </table>
      </td>
    </tr>
    <tr><td>&nbsp;</td></tr>
    <tr>
      <td>
        <input type=submit value=<%=bundle.getString("lb_ok")%>>
      </td>
    </tr>
</table>
</form>

</DIV>
</BODY>
</HTML>
