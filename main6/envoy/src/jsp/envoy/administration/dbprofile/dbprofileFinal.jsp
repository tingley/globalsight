<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
		 com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
		 com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl,
		 com.globalsight.everest.webapp.pagehandler.PageHandler, 
		 java.util.Locale,
		 com.globalsight.util.collections.HashtableValueOrderWalker, java.util.ResourceBundle, java.util.Vector"
		 session="true" %>

<jsp:useBean id="autodispatch" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="xmlrulefile" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="systemparms" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel6" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="pre6" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 	ResourceBundle bundle = PageHandler.getBundle(session); %>
<%
   SessionManager sessionMgr = (SessionManager)request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
   Vector DBColumns = (Vector)sessionMgr.getAttribute("DBColumns");
   HashtableValueOrderWalker ModePairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("ModePairs");
   HashtableValueOrderWalker DBConnectionPairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("DBConnectionPairs");
   String autodispatchURL = autodispatch.getPageURL();
   String xmlrulefileURL = xmlrulefile.getPageURL();
   String systemparmsURL = systemparms.getPageURL();
   String saveURL = save.getPageURL();
   String cancel6URL = cancel6.getPageURL();
   String pre6URL = pre6.getPageURL();
   String title = bundle.getString("lb_db_write_sql");
   String labelFinalConnection = bundle.getString("lb_final_db_connection");
   String labelInsertSql = bundle.getString("lb_final_insert_sql");
   String labelUpdateSql = bundle.getString("lb_final_update_sql");
   String lb_previous = bundle.getString("lb_previous");
   String lb_cancel = bundle.getString("lb_cancel");
   String lb_save = bundle.getString("lb_save");
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
var needWarning = true;
var objectName = "<%= bundle.getString("lb_db_profile") %>";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_db_write_sql")%>";

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
	if (!isSelectionMade(formSent.finalConn))
    {
		alert("<%= bundle.getString("jsmsg_db_profiles_db_connection") %>");
		return false;
	}

	// Make sure Write Insert SQL has been input
	if (isEmptyString(formSent.finalInsertSQL.value))
    {
		alert("<%= bundle.getString("jsmsg_db_profiles_write_sql_insert") %>");
		formSent.finalInsertSQL.value = "";
		formSent.finalInsertSQL.focus();
		return false;
	}

	// Make sure Write Update SQL has been input
	if (isEmptyString(formSent.finalUpdateSQL.value))
    {
		alert("<%= bundle.getString("jsmsg_db_profiles_write_sql_update") %>");
		formSent.finalUpdateSQL.value = "";
		formSent.finalUpdateSQL.focus();
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
<%=bundle.getString("helper_text_db_profile_final")%>
</TD>
</TR>
</TABLE>
<P>


<form name="profileForm" action="<%=saveURL%>" method="post">
<SPAN CLASS="standardText">
<%= labelFinalConnection %><SPAN CLASS="asterisk">*</SPAN>:
</SPAN>
<BR>
<select name="finalConn" CLASS="standardText">
    <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            if (DBConnectionPairs != null)
            {
                for (int i=0; i < DBConnectionPairs.size(); i++)
                   {
                    Long num = (Long)DBConnectionPairs.getKey(i);
                    String dbcp = (String)DBConnectionPairs.getValue(i);
%>
    <OPTION value ="<%= num %>"><%= dbcp %></OPTION>
<%
                }
            }
%>
</select>
<P>
<SPAN CLASS="standardText">
<%= labelInsertSql %><SPAN CLASS="asterisk">*</SPAN>:
</SPAN>
<BR>
<textarea name="finalInsertSQL" cols="70" rows="5" wrap="virtual" CLASS="standardText"></textarea>
<P>
<SPAN CLASS="standardText">
<%= labelUpdateSql %><SPAN CLASS="asterisk">*</SPAN>:
</SPAN>
<BR>
<textarea name="finalUpdateSQL" cols="70" rows="5" wrap="virtual" CLASS="standardText"></textarea>
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
    <TR>
        <TD CLASS="standardText" NOWRAP><%= cn %></TD>
        <TD CLASS="standardText" NOWRAP><%= tn %></TD>
        <TD CLASS="standardText" NOWRAP><%= cm %></TD>
    </TR>
<%
                }
            }
%>
</TABLE>
</form>

<form name="profileCancel" action="<%=cancel6URL%>" method="post">
<INPUT TYPE="HIDDEN" NAME="Cancel" value="Cancel">
</form>

<INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
    ONCLICK="cancelForm()">     
<INPUT TYPE="BUTTON" NAME="<%=lb_previous%>" VALUE="<%=lb_previous%>" 
    ONCLICK="location.replace('<%=pre6URL%>')">    
<INPUT TYPE="BUTTON" NAME="<%=lb_save%>" VALUE="<%=lb_save%>" 
    ONCLICK="submitForm()">  


</DIV>
</BODY>
</HTML>
