<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
        java.util.Locale,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.util.system.SystemConfigParamNames,
        com.globalsight.everest.util.system.SystemConfiguration,
        com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper,
        com.globalsight.everest.webapp.pagehandler.administration.users.UserStateConstants,
        com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
        com.globalsight.everest.webapp.javabean.NavigationBean"
    session="true"
%>
<jsp:useBean id="addAnother" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="setRate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="setSource" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String nextURL = next.getPageURL() + "&" +
  WebAppConstants.USER_ACTION + "=next";
String addAnotherURL = addAnother.getPageURL() + "&" +
  WebAppConstants.USER_ACTION + "=" +
  WebAppConstants.USER_ACTION_ADD_ANOTHER_LOCALES;
String setRateURL = setRate.getPageURL() + "&" + WebAppConstants.USER_ACTION + "=" +
  WebAppConstants.USER_ACTION_SET_RATE;
//set Source
String setSourceURL = setSource.getPageURL() + "&" + WebAppConstants.USER_ACTION + "=" +
  WebAppConstants.USER_ACTION_SET_SOURCE;
String prevURL = prev.getPageURL() + "&" + WebAppConstants.USER_ACTION + "=" +
  WebAppConstants.USER_ACTION_PREVIOUS;
String cancelURL = cancel.getPageURL() + "&" + WebAppConstants.USER_ACTION + "=" +
  WebAppConstants.CANCEL;

//Labels read from bundle
String title= bundle.getString("lb_new_roles");
String lbRoleCompanyName = bundle.getString("lb_role_companyname");
String lbSourceLocale = bundle.getString("lb_source_locale");
String lbChoose = bundle.getString("lb_choose");
String lbTargetLocale = bundle.getString("lb_target_locale");
String lbActivityTypes = bundle.getString("lb_activity_types");
String lbUser = bundle.getString("lb_user");
String lbPrevious = bundle.getString("lb_previous");
String lbCancel = bundle.getString("lb_cancel");
String lbNext = bundle.getString("lb_next");
String lbAdd = bundle.getString("lb_add");
String lbUserName = bundle.getString("lb_user_name");

boolean isJobCosting = ((Boolean)request.getAttribute(
  SystemConfigParamNames.COSTING_ENABLED)).booleanValue();

//Messges read from bundle
String jsmsgActivities = bundle.getString("jsmsg_users_activities") ;
String jsmsgSourceLocale = bundle.getString("jsmsg_users_source_locale");
String jsmsgTargetLocale = bundle.getString("jsmsg_users_target_locale");

SessionManager sessionMgr =
  (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
// Get the user wrapper off the session manager.
String userGroup = (String)sessionMgr.getAttribute(UserStateConstants.USER_GROUPS);
StringBuffer another = (StringBuffer)sessionMgr.getAttribute(UserConstants.ADD_ANOTHER);
boolean isAddAnother = another.toString().equals("true");
boolean isRoleOptional = true;
String roleAdded = (String) sessionMgr.getAttribute("roleAdded");

CreateUserWrapper wrapper = (CreateUserWrapper)sessionMgr.getAttribute(
  UserConstants.CREATE_USER_WRAPPER);
boolean promptIsActive = wrapper.promptIsActive();
String userName = wrapper.getUserName();
%>
<HTML>
<!-- This JSP is envoy/administratin/users/new2.jsp-->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = true;
var objectName = "<%= lbUser %>";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_users_roles_new")%>";

var theForm;

function submitForm()
{
   if (document.layers)
   {
      theForm = document.contentLayer.document.userForm;
   }
   else
   {
      theForm = document.all.userForm;
   }

   if (checkAccess(theForm, false))
   {
      theForm.submit();
   }
}

function setSources()
{
   if (document.layers)
   {
      theForm = document.contentLayer.document.userForm;
   }
   else
   {
      theForm = document.all.userForm;
   }

   theForm.action = "<%= setSourceURL %>";
   theForm.submit();
}

function checkTargetLocale(theForm)
{     
    if (theForm.selectTargetLocale.options[theForm.selectTargetLocale.selectedIndex].value == "-1" ||
            theForm.selectTargetLocale.options[theForm.selectTargetLocale.selectedIndex].value == "" || 
            theForm.selectTargetLocale.options[theForm.selectTargetLocale.selectedIndex].value == null)
    {
        alert("<%= jsmsgTargetLocale %>");
        return false;
    }
    else 
    {
        return true;
    }
}

function setRate()
{
   
   if (document.layers)
   {
      theForm = document.contentLayer.document.userForm;
   }
   else
   {
      theForm = document.all.userForm;
   }

   if (checkTargetLocale(theForm))
   {
      theForm.action = "<%= setRateURL %>";
      theForm.submit();    
   }
   
}

function addAnotherPanel()
{
   if (document.layers)
   {
      theForm = document.contentLayer.document.userForm;
   }
   else
   {
      theForm = document.all.userForm;
   }

   if (confirmForm(theForm, true))
   {
      theForm.action = "<%= addAnotherURL %>";
      theForm.submit();
   }
}

function checkAccess(formSent, addPanel)
{
   activityChecked = false;
   for (i = 2; i < formSent.length; i++)
   {
      if ((formSent.elements[i].type == "checkbox") &&
          (formSent.elements[i].checked == true))
      {
         activityChecked = true;
         break;
      }
   }
   if (activityChecked && !isSelectionMade(formSent.selectSourceLocale))
   {
       alert("<%= jsmsgSourceLocale %>");
       return false;
   }
   if (activityChecked && !isSelectionMade(formSent.selectTargetLocale))
   {
       alert("<%= jsmsgTargetLocale %>");
       return false;
   }
   if (!activityChecked && addPanel)
   {
      alert("<%= jsmsgActivities %>");
      return false;
   }
   
<% if (!isRoleOptional && promptIsActive) { %>
   if ((!activityChecked || !isSelectionMade(formSent.selectSourceLocale)) &&
        <%=roleAdded%> == null)
   {
      if (!confirm("<%=bundle.getString("jsmsg_users_active3")%>"))
      {
          return false;
      }
   }
<% } %>

   return true;
}

function confirmForm(formSent, addPanel)
{
   if (!checkTargetLocale(formSent))
   {
       return false;
   }
   else
   {
       return true;
   }
   
   activityChecked = false;

   for (i = 2; i < formSent.length; i++)
   {
      if ((formSent.elements[i].type == "checkbox") &&
          (formSent.elements[i].checked == true))
      {
         activityChecked = true;
         break;
      }
   }
<% if (!isRoleOptional) { %>
   if (!activityChecked && <%=roleAdded%> == null)
   {
      alert("<%= jsmsgActivities %>");
      return false;
   }
   if (!isSelectionMade(formSent.selectSourceLocale) && <%=roleAdded%> == null)
   {
      alert("<%= jsmsgSourceLocale %>");
      return false;
   }

   if (!isSelectionMade(formSent.selectTargetLocale) && <%=roleAdded%> == null)
   {
      alert("<%= jsmsgTargetLocale %>");
      return false;
   }
<% } else { %>
   if (activityChecked && !isSelectionMade(formSent.selectSourceLocale))
   {
       alert("<%= jsmsgSourceLocale %>");
       return false;
   }
   if (activityChecked && !isSelectionMade(formSent.selectTargetLocale))
   {
       alert("<%= jsmsgTargetLocale %>");
       return false;
   }
   if (!activityChecked && addPanel)
   {
      alert("<%= jsmsgActivities %>");
      return false;
   }
<% } %>

   return true;
}


var targetArrayText0 = new Array("<--Select Source First"," ");
var targetArrayValue0 = new Array("-1","-1");

<%= (String) request.getAttribute("jsArrays") %>

function setTargets(selectedIndexSent)
{
   if (document.layers)
   {
      for (i = 0; i < (eval("targetArrayText" + selectedIndexSent)).length; i++)
      {
         document.contentLayer.document.userForm.selectTargetLocale.options[i].text = (eval("targetArrayText" + selectedIndexSent))[i];
         document.contentLayer.document.userForm.selectTargetLocale.options[i].value = (eval("targetArrayValue" + selectedIndexSent))[i];
      }
      for (i = (eval("targetArrayText" + selectedIndexSent)).length; i < document.contentLayer.document.userForm.selectTargetLocale.length; i++)
      {
         document.contentLayer.document.userForm.selectTargetLocale.options[i].text = "";
         document.contentLayer.document.userForm.selectTargetLocale.options[i].value = "-1";
      }
   }
   else
   {
      for (i = 0; i < (eval("targetArrayText" + selectedIndexSent)).length; i++)
      {
         document.userForm.selectTargetLocale.options[i].text = (eval("targetArrayText" + selectedIndexSent))[i];
         document.userForm.selectTargetLocale.options[i].value = (eval("targetArrayValue" + selectedIndexSent))[i];
      }
      for (i = (eval("targetArrayText" + selectedIndexSent)).length; i < document.userForm.selectTargetLocale.length; i++)
      {
         document.userForm.selectTargetLocale.options[i].text = "";
         document.userForm.selectTargetLocale.options[i].value = "-1";
      }
   }
}

function check_all(){
	if($("#checkAll").is(":checked")){
		$(":checkbox").attr("checked","true");
	} else {
		$(":checkbox").removeAttr("checked");
	}
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 0; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=bundle.getString("helper_text_users_roles")%>
      <%if (userGroup == null || userGroup.equals("")) { %>
      <BR><BR><B>This step can be skipped for Vendor Management users.</B>
      <%}%>
      <HR NOSHADE SIZE=1>
    </TD>
  </TR>
</TABLE>

<P></P>

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
  <TR>
    <TD>
      <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" CLASS="standardText">
	<FORM NAME="userForm" ACTION="<%= nextURL %>" METHOD="post">
	<TR>
	      <TD>
	      <%= lbUserName %><SPAN CLASS="asterisk">*</SPAN>:
	      </TD>
	      <TD>
	      <%= userName %>
	      </TD>
	      <TD>&nbsp;</TD>
       </TR>
	<TR>
	      <TD>
	      <%= lbRoleCompanyName %><SPAN CLASS="asterisk">*</SPAN>:
	      </TD>
	      <TD>
	      <%= (String) request.getAttribute("allRoleCompanyNames") %>
	      </TD>
	      <TD>&nbsp;</TD>
       </TR>
       <tr><TD><BR></TD></tr>
       <TR>
	    <TD>
	    <%= lbSourceLocale %><SPAN CLASS="asterisk">*</SPAN>:
	    </TD>
            <TD>
	    <SELECT NAME="selectSourceLocale" SIZE="1" onChange="setTargets(selectedIndex);">
	      <OPTION VALUE="-1" SELECTED><%= lbChoose %></OPTION>
		<%= (String) request.getAttribute("allSourceLocales") %>
	    </SELECT>
	  </TD>
	  <TD>
	    <%= lbTargetLocale %><SPAN CLASS="asterisk">*</SPAN>:
	    <SELECT NAME="selectTargetLocale" SIZE="1" ONCHANGE="javascript: setRate();">
	      <!-- One note: the number of options under this select tag must be equal to the largest target array set in the javascript above -->
	      <%= (String) request.getAttribute("optionPadding") %>
	    </SELECT>
	  </TD>
	</TR>
      </TABLE>
    </TD>
  </TR>
  <TR>
    <TD COLSPAN="2">&nbsp;</TD>
  </TR>
  <TR>
    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" CLASS="standardText">
      <TR>
	<TD>
	  <input type="checkbox" id="checkAll" onclick="check_all()"/><%= lbActivityTypes %><SPAN CLASS="asterisk">*</SPAN>:&nbsp;&nbsp;&nbsp;&nbsp;
	</TD>
	<% if (isJobCosting) { %>
	<TD>
	  <%=bundle.getString("lb_expense")%> <SPAN CLASS="asterisk">*</SPAN>:&nbsp;&nbsp;&nbsp;&nbsp;
	</TD>
	<% } %>
      </TR>
      <%= (String) request.getAttribute("activities") %>
    </TABLE>
  </TR>
  <tr><BR></tr>
  <TR>
    <TD COLSPAN="2" ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
      onclick="location.replace('<%=cancelURL%>')">
      <INPUT TYPE="BUTTON" NAME="<%=lbPrevious%>" VALUE="<%=lbPrevious%>"
      onclick="location.replace('<%=prevURL%>')">
      
      <INPUT TYPE="BUTTON" NAME="<%=lbAdd%>" VALUE="<%=lbAdd%>"
      onclick="addAnotherPanel()">
      
      <INPUT TYPE="BUTTON" NAME="<%=lbNext%>" VALUE="<%=lbNext%>"
      onclick="submitForm()">
    </TD>
  </TR>
</TABLE>
</DIV>
</FORM>
</BODY>
</HTML>
