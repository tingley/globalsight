<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
		 com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
		 com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl,
         com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl,
		 com.globalsight.everest.webapp.pagehandler.PageHandler, 
		 java.util.Locale,
		 com.globalsight.util.collections.HashtableValueOrderWalker, java.util.ResourceBundle, java.util.Vector"
		 session="true" %>

<jsp:useBean id="autodispatch" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="xmlrulefile" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="systemparms" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next4" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel4" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="pre4" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 	ResourceBundle bundle = PageHandler.getBundle(session); %>
<%
   // bring in "state" from session
   SessionManager sessionMgr = (SessionManager)request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
   DatabaseProfileImpl ModDBProfile = (DatabaseProfileImpl)sessionMgr.getAttribute("ModDBProfile");

   int chosenConn = (int)ModDBProfile.getCheckOutConnectionProfileId();
   String chosenAcqSQL = (String)ModDBProfile.getCheckOutSql();
   Vector DBColumns = (Vector)sessionMgr.getAttribute("DBColumns");
   HashtableValueOrderWalker ModePairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("ModePairs");
   HashtableValueOrderWalker DBConnectionPairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("DBConnectionPairs");
   String autodispatchURL = autodispatch.getPageURL();
   String xmlrulefileURL = xmlrulefile.getPageURL();
   String systemparmsURL = systemparms.getPageURL();
   String next4URL = next4.getPageURL();
   String cancel4URL = cancel4.getPageURL();
   String pre4URL = pre4.getPageURL();
   String title = bundle.getString("lb_acquisition");
   String labelReadConnection = bundle.getString("lb_acquisition_db_connection");
   String labelReadSql = bundle.getString("lb_acquisition_sql");
   String lb_previous = bundle.getString("lb_previous");
   String lb_cancel = bundle.getString("lb_cancel");
   String lb_next = bundle.getString("lb_next");
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = true;
var objectName = "<%= bundle.getString("lb_db_profile") %>";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_db_read_sql")%>";

function submitForm()
{
	// Get image to return to pre-clicked state if login fails
	if (document.layers) theForm = document.contentLayer.document.profileForm;
	else theForm = document.all.profileForm;
	if (confirmForm(theForm))
	{
      	theForm.submit();
	}
}

function confirmForm(formSent) {

	// Make sure a Database Connection has been chosen
	if (!isSelectionMade(formSent.acquisitionConn))
    {
		alert("<%= bundle.getString("jsmsg_db_profiles_db_connection") %>");
		return false;
	}

	// Make sure Read SQL has been input
	if (isEmptyString(formSent.acquisitionSQL.value))
    {
		alert("<%= bundle.getString("jsmsg_db_profiles_read_sql") %>");
		formSent.acquisitionSQL.value = "";
		formSent.acquisitionSQL.focus();
		return false;
	}

	return true;
}

function cancelForm()
{
	if (document.layers) theForm = document.contentLayer.document.profileCancel;
	else theForm = document.all.profileCancel;
	theForm.submit();
}

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

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_db_edit_read")%>
</TD>
</TR>
</TABLE>
<P>


<form name="profileForm" action="<%=next4URL%>" method="post">
<SPAN CLASS="standardTextBold">
<%= labelReadConnection %><SPAN CLASS="asterisk">*</SPAN>:
</SPAN>
<BR>
<select name="acquisitionConn" CLASS="standardText">
<%
            // chosenConn non-null implies DBConnectionPairs non-null
            Long ltmp = new Long("-1");
            if (chosenConn > 0)
            {
                ltmp = new Long(chosenConn);
                String connchosen = (String)DBConnectionPairs.get(ltmp);
%>
                        <option value="<%= ltmp %>"><%= connchosen %></option>
<%
            }
            else
            {
%>
                        <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            }

            if (DBConnectionPairs != null)
            {
                  for (int i=0; i < DBConnectionPairs.size(); i++)
                     {
                    Long num = (Long)DBConnectionPairs.getKey(i);
                    String dbpc = (String)DBConnectionPairs.getValue(i);
                    if (!(num.equals(ltmp)))
                    {
%>
                        <OPTION value ="<%= num %>"><%= dbpc %></OPTION>
<%
                    }
                }
            }
%>
</select>
<P>
<SPAN CLASS="standardTextBold">
<%= labelReadSql %><SPAN CLASS="asterisk">*</SPAN>:
</SPAN>
<BR>
<textarea name="acquisitionSQL" cols="70" rows="5" wrap="virtual" CLASS="standardText">
<%
        if (chosenAcqSQL != null)
        {
%><%=chosenAcqSQL%>
<%
        }
%></textarea>
<P>
<TABLE CELLSPACING="0" CELLPADDING="3" BORDER="0">
    <TR CLASS="tableHeadingBasic">
        <TD NOWRAP><%= bundle.getString("lb_column") %></TD>
		<TD NOWRAP><%= bundle.getString("lb_table") %></TD>
		<TD NOWRAP><%= bundle.getString("lb_mode") %></TD>
	</TR>
<%
            if (DBColumns != null)
            {
                int size = DBColumns.size();
                Integer Itmp;
                for (int i = 0; i < size; i++)
                {
                    DatabaseColumnImpl dbcolumn = (DatabaseColumnImpl)DBColumns.elementAt(i);
                    String cn = (String)dbcolumn.getColumnName();
                    String tn = (String)dbcolumn.getTableName();
                    Itmp = new Integer((int)dbcolumn.getContentMode());
                    String cm = (String)ModePairs.get(Itmp);
%>
				<TR CLASS="standardText">
					<TD NOWRAP><%= cn %></TD>
					<TD NOWRAP><%= tn %></TD>
					<TD NOWRAP><%= cm %></TD>
				</TR>
<%
                }
            }
%>
</TABLE>
</form>

<P>
<form name="profileCancel" action="<%=cancel4URL%>" method="post">
<INPUT TYPE="HIDDEN" NAME="Cancel" value="Cancel">
</form>
<P>

<INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
    ONCLICK="cancelForm()">     
<INPUT TYPE="BUTTON" NAME="<%=lb_previous%>" VALUE="<%=lb_previous%>" 
    ONCLICK="location.replace('<%=pre4URL%>')">    
<INPUT TYPE="BUTTON" NAME="<%=lb_next%>" VALUE="<%=lb_next%>" 
    ONCLICK="submitForm()">  

</DIV>
</BODY>
</HTML>
