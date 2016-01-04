<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
		 com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
		 com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl,
         com.globalsight.cxe.entity.databaseprofile.DatabaseProfile,
         com.globalsight.everest.servlet.EnvoyServletException,
         com.globalsight.util.GeneralException,
            com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants,
		 java.lang.Long, java.util.Locale,
		 com.globalsight.everest.webapp.pagehandler.PageHandler, 
		 java.util.ResourceBundle,
         com.globalsight.util.collections.HashtableValueOrderWalker"
		 session="true" %>

<jsp:useBean id="autodispatch" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="xmlrulefile" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="systemparms" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next2" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel2" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="pre2" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 	ResourceBundle bundle = PageHandler.getBundle(session); %>

<%
      Vector dbprofiles;
      try
      {
          dbprofiles = new Vector(ServerProxy.getDatabaseProfilePersistenceManager().getAllDatabaseProfiles());
      }
      catch (Exception e)
      {
          throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
      }

      //int size = dbprofiles.s

      String[] profileNames= new String[dbprofiles.size()];
      for (int i=0; i<dbprofiles.size(); i++) {
          DatabaseProfile dbprofile = (DatabaseProfile)dbprofiles.elementAt(i);
          profileNames[i] = dbprofile.getName();
      }
%>

<%
   // bring in "state" from session
   SessionManager sessionMgr = (SessionManager)request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
   String chosenName = (String)sessionMgr.getAttribute("DBProfileName");
   String chosenDescription = (String)sessionMgr.getAttribute("DBProfileDescription");
   String chosenL10nProfileId = (String)sessionMgr.getAttribute("locProfile");

   HashtableValueOrderWalker L10nProfilePairs = (HashtableValueOrderWalker)sessionMgr.getAttribute("L10nProfilePairs");
   String autodispatchURL = autodispatch.getPageURL();
   String xmlrulefileURL = xmlrulefile.getPageURL();
   String systemparmsURL = systemparms.getPageURL();
   String next2URL = next2.getPageURL();
   String cancel2URL = cancel2.getPageURL();
   String pre2URL = pre2.getPageURL();
   String title = bundle.getString("lb_db_basic_info");
   String labelName = bundle.getString("lb_name");
   String labelDescription = bundle.getString("lb_description");
   String labelLocProf = bundle.getString("lb_loc_profile");
   String lb_previous = bundle.getString("lb_previous");
   String lb_cancel = bundle.getString("lb_cancel");
   String lb_next = bundle.getString("lb_next");

%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = true;
var objectName = "<%= bundle.getString("lb_db_profile") %>";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_db_profiles_basic_information")%>";

function submitForm()
{   
	if (document.layers) theForm = document.contentLayer.document.profileForm;
	else theForm = document.all.profileForm;
	if (confirmForm(theForm))
	{
      	theForm.submit();
	}
}

function confirmForm(formSent) {
	if (isEmptyString(formSent.DBProfileName.value)) {
		alert("<%= bundle.getString("jsmsg_db_profiles_name") %>");
		formSent.DBProfileName.value = "";
		formSent.DBProfileName.focus();
		return false;
	}

	if (!isNotLongerThan(formSent.DBProfileDescription.value,256)) {
		alert("<%= bundle.getString("jsmsg_description") %>");
		formSent.DBProfileDescription.focus();
		return false;
	}

	if (!isSelectionMade(formSent.locProfile)) {
		alert("<%= bundle.getString("jsmsg_db_profiles_loc_profile") %>");
		return false;
	}
	var pName = formSent.DBProfileName.value;
	<% for (int i=0; i<profileNames.length; i++){ %>
	   if(pName == "<%= profileNames[i] %>")
	   {
          alert('<%=bundle.getString("jsmsg_duplicate_db_profile")%>');
          return false;
	   }
	<% } %>
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
<%=bundle.getString("helper_text_db_profile_basic_info")%>
</TD>
</TR>
</TABLE>
<P>

<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
<form name="profileForm" action="<%=next2URL%>" method="post">
		<TR>
			<TD><%=labelName%><SPAN CLASS="asterisk">*</SPAN>:</TD>
			<TD>

<%
        if (chosenName != null)
        {
%>
                <INPUT TYPE="TEXT" SIZE="20" NAME="DBProfileName" MAXLENGTH="20" VALUE="<%= chosenName %>">
<%
        }
        else
        {
%>
                <INPUT TYPE="TEXT" SIZE="20" NAME="DBProfileName" MAXLENGTH="20">
<%
        }
%>
           </TD>
		</TR>
		<TR>
			<TD><%=labelDescription%>:</TD>
			<TD>
<%
        if (chosenDescription != null)
        {
%>
                <INPUT TYPE="TEXT" SIZE="40" NAME="DBProfileDescription" MAXLENGTH="1000" VALUE="<%= chosenDescription %>">
<%
        }
        else
        {
%>
                <INPUT TYPE="TEXT" SIZE="40" NAME="DBProfileDescription" MAXLENGTH="1000">
<%
        }
%>
            </SPAN>
            </TD>
		</TR>
		<TR>
			<TD><%=labelLocProf%><SPAN CLASS="asterisk">*</SPAN>:</TD>
			<TD><SELECT name="locProfile">
<%
            // chosenL10nProfileId non-null implies L10nProfilePairs non-null
            Long itmp = new Long("-1");
            if (chosenL10nProfileId != null)
            {
                itmp = new Long((String)chosenL10nProfileId);
                String lpchosen = (String)L10nProfilePairs.get(itmp);
%>
                <OPTION VALUE="<%= itmp %>"><%= lpchosen %></OPTION>
<%
            }
            else
            {
%>
                <OPTION VALUE="-1"><%=bundle.getString("lb_choose")%></OPTION>
<%
            }

            if (L10nProfilePairs != null)
            {
                for (int i=0; i < L10nProfilePairs.size(); i++)
                   {
                    Long num = (Long)L10nProfilePairs.getKey(i);
                    String lp = (String)L10nProfilePairs.getValue(i);
                    if (!(num.equals(itmp)))
                    {
%>
                 <OPTION value ="<%= num %>"><%= lp %></OPTION>
<%
                    }
                }
            }
%>
                </SELECT>
                </TD>
		</TR>
        </form>
		<form name="profileCancel" action="<%=cancel2URL%>" method="post">
            <INPUT TYPE="HIDDEN" NAME="Cancel" value="Cancel">
        </form>
        </TABLE>
<P>

<INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
    ONCLICK="cancelForm()">     
<INPUT TYPE="BUTTON" NAME="<%=lb_previous%>" VALUE="<%=lb_previous%>" 
    ONCLICK="location.replace('<%=pre2URL%>')">    
<INPUT TYPE="BUTTON" NAME="<%=lb_next%>" VALUE="<%=lb_next%>" 
    ONCLICK="submitForm()">  


</DIV>
</BODY>
</HTML>
