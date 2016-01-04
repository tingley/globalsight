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
<jsp:useBean id="next5" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel5" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="pre5" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 	ResourceBundle bundle = PageHandler.getBundle(session); %>
<%
   // bring in "state" from session
   SessionManager sessionMgr = (SessionManager)request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
   String chosenURL = (String)sessionMgr.getAttribute("previewURL");
   String chosenConn = (String)sessionMgr.getAttribute("previewConn");
   String chosenInsertSQL = (String)sessionMgr.getAttribute("previewInsertSQL");
   String chosenUpdateSQL = (String)sessionMgr.getAttribute("previewUpdateSQL");

   Vector DBColumns = (Vector)sessionMgr.getAttribute("DBColumns");
   HashtableValueOrderWalker ModePairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("ModePairs");
   HashtableValueOrderWalker PreviewUrlPairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("PreviewUrlPairs");
   HashtableValueOrderWalker DBConnectionPairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("DBConnectionPairs");
   String autodispatchURL = autodispatch.getPageURL();
   String xmlrulefileURL = xmlrulefile.getPageURL();
   String systemparmsURL = systemparms.getPageURL();
   String next5URL = next5.getPageURL();
   String cancel5URL = cancel5.getPageURL();
   String pre5URL = pre5.getPageURL();
   String title = bundle.getString("lb_preview_sql");
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
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT language="JavaScript">
var needWarning = true;
var objectName = "<%= bundle.getString("lb_db_profile") %>";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_db_preview_sql")%>";

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

	// When Preview URL is chosen
	if (isSelectionMade(formSent.previewURL) || isSelectionMade(formSent.previewConn) || !isEmptyString(formSent.previewUpdateSQL.value) || !isEmptyString(formSent.previewInsertSQL.value))
    {

        // Make sure a Preview URL has been chosen
	    if (!isSelectionMade(formSent.previewURL))
        {
		alert("<%= bundle.getString("jsmsg_db_profiles_preview_url") %>");
            return false;
        }

        // Make sure a Database Connection has been chosen
	    if (!isSelectionMade(formSent.previewConn))
        {
		    alert("<%= bundle.getString("jsmsg_db_profiles_db_connection") %>");
            return false;
        }

        // Make sure a Preview Insert SQL has been input
        if (isEmptyString(formSent.previewInsertSQL.value))
        {
		alert("<%= bundle.getString("jsmsg_db_profiles_preview_sql_insert") %>");
            formSent.previewInsertSQL.value = "";
            formSent.previewInsertSQL.focus();
            return false;
        }

        // Make sure a Preview Update SQL has been input
        if (isEmptyString(formSent.previewUpdateSQL.value))
        {
		alert("<%= bundle.getString("jsmsg_db_profiles_preview_sql_update") %>");
            formSent.previewUpdateSQL.value = "";
            formSent.previewUpdateSQL.focus();
            return false;
        }
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
<%=bundle.getString("helper_text_db_preview")%>
</TD>
</TR>
</TABLE>
<P>

<form name="profileForm" action="<%=next5URL%>" method="post">
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
    <TR>
        <TD>
            <SPAN CLASS="standardText">
            <%= bundle.getString("lb_db_preview_rules") + bundle.getString("lb_colon") %>
            </SPAN>
        </TD>
        <TD>

<select name="previewURL" size="1" CLASS="standardText">
<%
            // chosenURL non-null implies PreviewUrlPairs non-null
            Integer jtmp = new Integer("-1");
            if (chosenURL != null)
            {
                jtmp = new Integer((String)chosenURL);
                String connchosen = (String)PreviewUrlPairs.get(jtmp);
%>
                        <option value="<%= jtmp %>"><%= connchosen %></option>
<%
            }
            else
            {
%>
                        <option value="-1"><%= bundle.getString("lb_none") %></option>
<%
            }

            if (PreviewUrlPairs != null)
            {
               for(int i=0; i < PreviewUrlPairs.size(); i++)
               {
                    Integer num = (Integer)PreviewUrlPairs.getKey(i);
                    String purl = (String)PreviewUrlPairs.getValue(i);
                    if (!(num.equals(jtmp)))
                    {
%>
                        <OPTION value ="<%= num %>"><%= purl %></OPTION>
<%
                    }
                }
            }
%>
</select>
        </TD>
    </TR>
    <TR>
        <TD>
            <SPAN CLASS="standardText">
            <%= bundle.getString("lb_preview_db_connections") + bundle.getString("lb_colon") %>
            </SPAN>
        </TD>
        <TD>
<select name="previewConn" CLASS="standardText">
<%
            // chosenConn non-null implies DBConnectionPairs non-null
            Long ltmp = new Long("-1");
            if (chosenConn != null)
            {
                ltmp = new Long((String)chosenConn);
                String connchosen;
                if (ltmp.longValue() == -1)
                {
                    connchosen = bundle.getString("lb_choose");
                }
                else
                {
                    connchosen = (String)DBConnectionPairs.get(ltmp);
                }
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
                    String dbcp = (String)DBConnectionPairs.getValue(i);
                    if (!(num.equals(ltmp)))
                    {
%>
                        <OPTION value ="<%= num %>"><%= dbcp %></OPTION>
<%
                    }
                }
            }
%>
</select>
        </TD>
    </TR>
</TABLE>
<P>
<SPAN CLASS="standardText">
<%= bundle.getString("lb_preview_insert_sql") + bundle.getString("lb_colon") %>
</SPAN>
<BR>
<textarea name="previewInsertSQL" cols="70" rows="5" wrap="virtual" CLASS="standardText"><%
        if (chosenInsertSQL != null)
        {
%><%=chosenInsertSQL%>
<%
        }
%></textarea>

<P>
<SPAN CLASS="standardText">
<%= bundle.getString("lb_preview_update_sql") + bundle.getString("lb_colon") %>
</SPAN>
<BR>
<textarea name="previewUpdateSQL" cols="70" rows="5" wrap="virtual" CLASS="standardText"><%
        if (chosenUpdateSQL != null)
        {
%><%=chosenUpdateSQL%>
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
<form name="profileCancel" action="<%=cancel5URL%>" method="post">
</form>

<INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
    ONCLICK="cancelForm()">     
<INPUT TYPE="BUTTON" NAME="<%=lb_previous%>" VALUE="<%=lb_previous%>" 
    ONCLICK="location.replace('<%=pre5URL%>')">    
<INPUT TYPE="BUTTON" NAME="<%=lb_next%>" VALUE="<%=lb_next%>" 
    ONCLICK="submitForm()">  

</DIV>
</BODY>
</HTML>
