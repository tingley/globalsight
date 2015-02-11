<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
             com.globalsight.everest.webapp.pagehandler.administration.config.teamsite.TeamSiteServerConstants,
             com.globalsight.everest.util.comparator.TeamSiteServerComparator,
             com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer,
             com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl,
             com.globalsight.everest.servlet.util.SessionManager,
             com.globalsight.everest.webapp.WebAppConstants,
             com.globalsight.everest.webapp.javabean.NavigationBean,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
             com.globalsight.util.resourcebundle.ResourceBundleConstants,
             com.globalsight.util.resourcebundle.SystemResourceBundle,
             com.globalsight.everest.webapp.webnavigation.LinkHelper,
             com.globalsight.everest.servlet.util.ServerProxy,
             com.globalsight.everest.servlet.EnvoyServletException,
             com.globalsight.everest.util.system.SystemConfigParamNames,
             com.globalsight.everest.util.system.SystemConfiguration,
             com.globalsight.everest.company.CompanyThreadLocal,
             com.globalsight.util.GeneralException,
             java.text.MessageFormat,
             java.util.Date,
             java.util.Vector,
             java.util.List,
             java.util.Locale,
             java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
   ResourceBundle bundle = PageHandler.getBundle(session);
   SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
   Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
   String actionType = (String)sessionMgr.getAttribute(TeamSiteServerConstants.ACTION);
   boolean isEdit = actionType != null &&
         actionType.equals(TeamSiteServerConstants.EDIT_ACTION);
   String nameField = TeamSiteServerConstants.NAME_FIELD;
   String descriptionField = TeamSiteServerConstants.DESCRIPTION_FIELD;
   String osField = TeamSiteServerConstants.OS_FIELD;
   String exportField = TeamSiteServerConstants.EXPORT_FIELD;
   String importField = TeamSiteServerConstants.IMPORT_FIELD;
   String proxyField = TeamSiteServerConstants.PROXY_FIELD;
   String homeField = TeamSiteServerConstants.HOME_FIELD;
   String userField = TeamSiteServerConstants.USER_FIELD;
   String userPassField = TeamSiteServerConstants.USER_PASS_FIELD;
   String userPassRepeatField = TeamSiteServerConstants.USER_PASS_REPEAT_FIELD;
   String mountField = TeamSiteServerConstants.MOUNT_FIELD;
   String typeField = TeamSiteServerConstants.TYPE_FIELD;
   String reimportField = TeamSiteServerConstants.REIMPORT_FIELD;
   String companyField = TeamSiteServerConstants.COMPANY_FIELD;

   //labels
   String labelServerName = bundle.getString("lb_teamsite_server_name");
   String labelDescription = bundle.getString("lb_description");
   String labelOS = bundle.getString("lb_teamsite_operating_system");
   String labelExportPort = bundle.getString("lb_teamsite_export_port");
   String labelImportPort = bundle.getString("lb_teamsite_import_port");
   String labelProxyPort = bundle.getString("lb_teamsite_proxy_port");
   String labelHome = bundle.getString("lb_teamsite_home");
   String labelUser = bundle.getString("lb_teamsite_user");
   String labelUserPass = bundle.getString("lb_teamsite_user_pass");
   String labelUserPassRepeat = bundle.getString("lb_teamsite_user_pass_repeat");
   String labelMount = bundle.getString("lb_teamsite_mount");
   String labelType = bundle.getString("lb_teamsite_user_type");
   String labelReimport = bundle.getString("lb_teamsite_reimport");
   String msgDuplicateName = bundle.getString("msg_duplicate_teamsite_server_name");
   String choose = bundle.getString("lb_choose");

   Vector servers = null;
   TeamSiteServerImpl tssi = null;
   try
   {
       Long serverId = (Long)sessionMgr.getAttribute(TeamSiteServerConstants.SERVER_ID);
       if(serverId != null)
       {
           tssi = (TeamSiteServerImpl)ServerProxy
                                    .getTeamSiteServerPersistenceManager()
                                    .readTeamSiteServer(serverId.longValue());
           servers = new Vector((Collection)ServerProxy
                                .getTeamSiteServerPersistenceManager()
                                .getAllTeamSiteServers());
       }
   }
   catch(Exception e)
   {
       throw new EnvoyServletException(e);
   }

   String companyId = null; 
   if (tssi != null)
   {
       companyId = String.valueOf(tssi.getCompanyId());
   }
   else
   {
       companyId = CompanyThreadLocal.getInstance().getValue();
   }

   String jsmsg = "";
   if( tssi == null)
   {
       if (servers != null)
       {
           for(int i=0; i<servers.size(); i++)
           {
              TeamSiteServerImpl tss = (TeamSiteServerImpl)servers.get(i);
              jsmsg += "if(basicServerForm." + nameField + ".value == \"" + tss.getName() + "\")\n" +
              "   {\n" +
              "      alert('" + msgDuplicateName + "');\n" +
              "      return false;\n" +
              "   }\n";
           }
       }
   }

   Vector operatingSystems = (Vector)sessionMgr.getAttribute(
     TeamSiteServerConstants.OPERATING_SYSTEMS);
   Vector userTypes = (Vector)sessionMgr.getAttribute(
     TeamSiteServerConstants.USER_TYPES);
   Long tsServerId = (Long)request.getAttribute(
     TeamSiteServerConstants.SERVER_ID);

   String chosenName;
   String chosenDescription;
   String chosenOS;
   String chosenExport;
   String chosenImport;
   String chosenProxy;
   String chosenHome;
   String chosenUser;
   String chosenUserPass;
   String chosenUserPassRepeat;
   String chosenType;
   String chosenMount;
   Boolean chosenReimport;

   if(tssi == null)
   {
       chosenName = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_NAME);
       chosenDescription = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_DESCRIPTION);
       chosenOS = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_OS);
       chosenExport = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_EXPORT);
       chosenImport = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_IMPORT);
       chosenProxy = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_PROXY);
       chosenHome = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_HOME);
       chosenUser = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_USER);
       chosenUserPass = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_USER_PASS);
       chosenUserPassRepeat = chosenUserPass;
       chosenMount = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_MOUNT);
       chosenType = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_TYPE);
       chosenMount = (String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_MOUNT);
       chosenReimport = new Boolean((String)sessionMgr.getAttribute(TeamSiteServerConstants.CHOSEN_REIMPORT));
   }
   else
   {
       // values to be populated in the UI fields
       chosenName = tssi.getName();
       chosenDescription = tssi.getDescription();
       chosenOS = tssi.getOS();
       chosenExport = (new Long(tssi.getExportPort())).toString();
       chosenImport = (new Long(tssi.getImportPort())).toString();
       chosenProxy = (new Long(tssi.getProxyPort())).toString();
       chosenHome = tssi.getHome();
       chosenUser = tssi.getUser();
       chosenUserPass = tssi.getUserPass();
       chosenUserPassRepeat = chosenUserPass;
       chosenMount = tssi.getMount();
       chosenType = tssi.getType();
       chosenReimport = new Boolean(tssi.getLocaleSpecificReimportSetting());
   }

   // links for the next and cancel buttons
   String nextURL = next.getPageURL() + (tsServerId == null ? "" :
                     ("&" + TeamSiteServerConstants.SERVER_ID + "="
                     + tsServerId));
   String cancelURL = cancel.getPageURL() + "&"
                     + TeamSiteServerConstants.ACTION
                     + "=" + TeamSiteServerConstants.CANCEL_ACTION;
   String selfURL = self.getPageURL();

   // Titles
   String newTitle = bundle.getString("msg_teamsite_server_new_title1");
   String modifyTitle = bundle.getString("msg_teamsite_server_edit_title1");
   String wizardTitle = tssi == null ? newTitle : modifyTitle;
   String lbCancel = bundle.getString("lb_cancel");
   String lbNext = bundle.getString("lb_next");

%>
<HTML>
<HEAD>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%= wizardTitle %></TITLE>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = true;
var objectName = "<%= bundle.getString("lb_teamsite") %>";
var guideNode = "teamsiteServers";
var helpFile = "<%=bundle.getString("help_teamsite_server_basic_information")%>";

var names = new Array();

function checkForDuplicateName()
{
   <%=jsmsg%>
   return true;
}


function submitForm(formAction)
{
    basicServerForm.formAction.value = formAction;
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           basicServerForm.action = "<%=cancelURL%>";
           basicServerForm.submit();
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "next")
    {
       if(!checkForDuplicateName())
       {
          return false;
       }

       if (confirmForm(basicServerForm))
       {
          // Submit the form
          basicServerForm.submit();
       }
       else
       {
          return false;
       }
    }
}

function confirmForm(formSent) {
    var theName = formSent.<%=nameField%>.value;
	theName = stripBlanks (theName);

	if (isEmptyString(formSent.<%=nameField%>.value)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_name") %>");
		formSent.<%=nameField%>.value = "";
		formSent.<%=nameField%>.focus();
		return false;
	}

	if (!isNotLongerThan(formSent.<%=descriptionField%>.value, 200)) {
		alert("<%= bundle.getString("jsmsg_description") %>");
		formSent.<%=descriptionField%>.focus();
		return false;
	}

    if (!isSelectionMade(formSent.<%=osField%>)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_os") %>");
		return false;
	}

    if (!isSelectionMade(formSent.<%=typeField%>)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_user_type") %>");
		return false;
	}

    if (isEmptyString(formSent.<%=mountField%>)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_mount") %>");
		formSent.<%=mountField%>.value = "";
		formSent.<%=mountField%>.focus();
		return false;
	}

	if (isEmptyString(formSent.<%=exportField%>.value)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_export") %>");
		formSent.<%=exportField%>.value = "";
		formSent.<%=exportField%>.focus();
		return false;
	}

    if (!isAllDigits(formSent.<%=exportField%>.value)) {
        alert("<%=labelExportPort%>" + "<%=bundle.getString("jsmsg_numeric")%>");
        return false;
    }

	if (isEmptyString(formSent.<%=importField%>.value)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_import") %>");
		formSent.<%=importField%>.value = "";
		formSent.<%=importField%>.focus();
		return false;
	}

    if (!isAllDigits(formSent.<%=importField%>.value)) {
        alert("<%=labelImportPort%>" + "<%=bundle.getString("jsmsg_numeric")%>");
        return false;
    }

	if (isEmptyString(formSent.<%=proxyField%>.value)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_proxy") %>");
		formSent.<%=proxyField%>.value = "";
		formSent.<%=proxyField%>.focus();
		return false;
	}

    if (!isAllDigits(formSent.<%=proxyField%>.value)) {
        alert("<%=labelProxyPort%>" + "<%=bundle.getString("jsmsg_numeric")%>");
        return false;
    }

	if (isEmptyString(formSent.<%=homeField%>.value)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_home") %>");
		formSent.<%=homeField%>.value = "";
		formSent.<%=homeField%>.focus();
		return false;
	}

	if (isEmptyString(formSent.<%=userField%>.value)) {
		alert("<%= bundle.getString("jsmsg_teamsite_server_user") %>");
		formSent.<%=userField%>.value = "";
		formSent.<%=userField%>.focus();
		return false;
	}
	
	var thePassword = stripBlanks(formSent.<%=userPassField%>.value);
    if (isEmptyString(thePassword)) {
        alert("<%= bundle.getString("jsmsg_teamsite_server_user_pass") %>");
        formSent.<%=userPassField%>.value = "";
        formSent.<%=userPassField%>.focus();
        return false;
    }
    
    //  Make sure the repeated password matches the first
    var theRepeat = stripBlanks(formSent.<%=userPassRepeatField%>.value);
    if (theRepeat != thePassword) {
        alert("<%= bundle.getString("jsmsg_teamsite_server_user_pass_repeat") %>");
        formSent.<%=userPassRepeatField%>.value = "";
        formSent.<%=userPassField%>.value = "";
        formSent.<%=userPassField%>.focus();
        return false;
    }

    if (hasSpecialChars(formSent.<%=userField%>.value))
    {
        alert("<%= labelUser %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
	return true;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
  <TR>
    <TD COLSPAN="3" CLASS="mainHeading">&nbsp;&nbsp;<%=wizardTitle%></TD>
  </TR>
  <TR>
    <TD VALIGN="TOP">
      <TABLE CELLSPACING="8" CELLPADDING="0" BORDER="0" CLASS="standardText">
	<form name="basicServerForm" action="<%=nextURL%>" method="post">
    <INPUT TYPE="HIDDEN" NAME="<%=companyField%>" VALUE="<%=companyId%>">
	<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
	<TR>
	  <TD><%=labelServerName%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="25" NAME="<%=nameField%>" CLASS="standardText"
            <%  if (chosenName != null) { %>
                VALUE="<%= chosenName %>"
            <%  }%>
            ></INPUT>
	  </TD>
	</TR>
	<TR>
	  <TD><%=labelDescription%>:<BR>
	    <TEXTAREA NAME="<%=descriptionField%>" COLS="40" ROWS="3"
	    CLASS="standardText"><%
		if (chosenDescription != null) out.print(chosenDescription);
		%></TEXTAREA>
	  </TD>
	</TR>
	<TR>
	  <TD>
	    <%=labelOS%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <SELECT NAME="<%=osField%>"  CLASS="standardText">
	      <%
	      String ltmp = "";
	      %>
	      <OPTION VALUE="-1"><%= choose %></OPTION>
	      <%
                    if (chosenOS != null)
                    {
                        ltmp = chosenOS;
                    }
                    String osId = null;
                    String OS = null;
                    if (operatingSystems != null)
                    {
                        int osSize = operatingSystems.size();
                        for (int i=0; i<osSize; i++)
                        {
                           String selected = "";
                           OS = (String)operatingSystems.get(i);
                           if (OS.equals(ltmp))
                           {
                               selected = "SELECTED";
                           }

	      %>
	      <OPTION VALUE="<%= OS %>" <%=selected%>><%= OS %></OPTION>
	      <%  }
	      }%>
	    </SELECT>
	    <BR>
	  </TD>
	</TR>
	<TR>
	  <TD><%=labelExportPort%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="8" NAME="<%=exportField%>"
	    CLASS="standardText"
	    <%  if (chosenExport != null) { %>
	    VALUE="<%= chosenExport %>"
	    <%  }%>
	    ></INPUT>
	  </TD>
	</TR>
	<TR>
	  <TD><%=labelImportPort%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="8" NAME="<%=importField%>"
	    CLASS="standardText"
	    <%  if (chosenImport != null) { %>
	    VALUE="<%= chosenImport %>"
	    <%  }%>
	    ></INPUT>
	  </TD>
	</TR>
	<TR>
	  <TD><%=labelProxyPort%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="8" NAME="<%=proxyField%>"
	    CLASS="standardText"
	    <%  if (chosenProxy != null) { %>
	    VALUE="<%= chosenProxy%>"
	    <%  }%>
	    ></INPUT>
	  </TD>
	</TR>
	<TR>
	  <TD><%=labelHome%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="4000" NAME="<%=homeField%>"
	    CLASS="standardText"
	    <%  if (chosenHome != null) { %>
	    VALUE="<%= chosenHome %>"
	    <%  }%>
	    ></INPUT>
	  </TD>
	</TR>
	<TR>
	  <TD><%=labelUser%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="25" NAME="<%=userField%>"
	    CLASS="standardText"
	    <%  if (chosenUser != null) { %>
	    VALUE="<%= chosenUser %>"
	    <%  }%>
	    ></INPUT>
	  </TD>
	</TR>
	<TR>
      <TD><%=labelUserPass%><SPAN CLASS="asterisk">*</SPAN>:<BR>
        <INPUT TYPE="PASSWORD" SIZE="20" MAXLENGTH="25" NAME="<%=userPassField%>"
        CLASS="standardText"
        <%  if (chosenUserPass != null) { %>
        VALUE="<%= chosenUserPass %>"
        <%  }%>
        ></INPUT>
      </TD>
    </TR>
    <TR>
    <TD><%=labelUserPassRepeat%><SPAN CLASS="asterisk">*</SPAN>:<BR>
      <INPUT TYPE="PASSWORD" SIZE="20" MAXLENGTH="25" NAME="<%=userPassRepeatField%>"
      CLASS="standardText"
      <%  if (chosenUserPass != null) { %>
      VALUE="<%= chosenUserPass %>"
      <%  }%>
      ></INPUT>
    </TD>
    </TR>
	<TR>
	  <TD><%=labelMount%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="25" NAME="<%=mountField%>"
	    CLASS="standardText"
	    <%  if (chosenMount != null) { %>
	    VALUE="<%= chosenMount %>"
	    <%  }%>
	    ></INPUT>
	  </TD>
	</TR>
	<TR>
	  <TD>
	    <%=labelType%><SPAN CLASS="asterisk">*</SPAN>:<BR>
	    <SELECT NAME="<%=typeField%>"  CLASS="standardText">
	      <%
	      ltmp = "";
	      %>
	      <OPTION VALUE="-1"><%= choose %></OPTION>
	      <%
                    if (chosenType != null)
                    {
                        ltmp = chosenType;
                    }
                    String typeId = null;
                    String type = null;
                    if (userTypes != null)
                    {
                        int size = userTypes.size();
                        for (int i=0; i<size; i++)
                        {
                           String selected = "";
                           type = (String)userTypes.get(i);
                           if (type.equals(ltmp))
                           {
                               selected = "SELECTED";
                           }

	      %>
	      <OPTION VALUE="<%=type%>" <%=selected%>><%= type %></OPTION>
	      <%  }
	      }%>
	    </SELECT>
	    <BR>
	  </TD>
	</TR>
	<TR>
	  <TD>
	    <%
	    String reimport = (chosenReimport == null || chosenReimport.booleanValue()) ? "checked" : "";
	    %>
	    <input type="checkbox" name="<%=reimportField%>" value="true"
	    <%=reimport%> > <%=labelReimport%>
	  </TD>
	</TR>
	</form>
      </TABLE>
    </TD>
    <TD WIDTH="50">&nbsp;</TD>
    <TD VALIGN="TOP"></TD>
  </TR>

  <TR>
    <TD COLSPAN="3">&nbsp;</TD>
  </TR>

  <TR>
    <TD CLASS="HREFBold" COLSPAN="2">
      <INPUT TYPE="BUTTON" NAME="<%=lbCancel %>" VALUE="<%=lbCancel %>"
      onclick="submitForm('cancel')">
      <INPUT TYPE="BUTTON" NAME="<%=lbNext %>" VALUE="<%=lbNext %>"
      onclick="submitForm('next')">
    </TD>
  </TR>
</TABLE>
</DIV>
</BODY>
</HTML>
