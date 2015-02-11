<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
				         com.globalsight.everest.autoactions.AutoAction,
				         com.globalsight.everest.gsedition.GSEditionActivity,
				         com.globalsight.everest.autoactions.AutoActionManagerLocal,
				         com.globalsight.everest.gsedition.GSEditionActivityManagerLocal,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.activity.ActivityConstants,
                 com.globalsight.everest.workflow.Activity,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.FormUtil,
                 com.globalsight.util.GeneralException,
                 com.globalsight.everest.company.CompanyThreadLocal,
                 java.text.MessageFormat,
                 java.util.*,
                 com.globalsight.everest.permission.Permission,
                 com.globalsight.everest.permission.PermissionSet,
                 com.globalsight.everest.company.CompanyWrapper,
                 com.globalsight.everest.company.Company"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    AutoActionManagerLocal actionManager = new AutoActionManagerLocal();
    GSEditionActivityManagerLocal gsManager = new GSEditionActivityManagerLocal();

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=" + ActivityConstants.EDIT;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_activity_type");
    }
    else
    {
        saveURL +=  "&action=" + ActivityConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_activity_type");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + ActivityConstants.CANCEL;

    // Data
    ArrayList names = (ArrayList)request.getAttribute(ActivityConstants.NAMES);
    Activity activity = (Activity)sessionMgr.getAttribute(ActivityConstants.ACTIVITY);
    String activityName = "";
    String desc = "";
    String activityUseType = "";
    String isDitaCheckChecked = "";
	int autoActionID = 0;
	int gsEditionActionID = 0;

    // For sla report issue
    boolean isTranslate = false;
    boolean isReviewEditable = false;
    boolean isReviewNotEditable = false;
	boolean isAutoAction = false;
	boolean isGSEdition = false;
	String qaChecks = "";
	Company company = null;
	boolean enableQAChecks = false;
	boolean isEnableDitaChecks = false;

    if (activity == null)
    {
    	company = CompanyWrapper.getCurrentCompany();
        isTranslate = true;
    }
    else
    {
    	company = CompanyWrapper.getCompanyById(activity.getCompanyId());
        activityName = activity.getDisplayName();
        activityUseType = activity.getUseType();
        desc = activity.getDescription();
        if (desc == null) desc = "";
        
        if (activity.isType(Activity.TYPE_REVIEW))
        {
            if (activity.getIsEditable()) 
            {
                isReviewEditable = true;
            }
            else
            {
                isReviewNotEditable = true;
            }
        }
		else if(activity.isType(Activity.TYPE_AUTOACTION))
		{
		    isAutoAction = true;
		    if(activity.getAutoActionID() != null)
		    {
		        autoActionID = Integer.parseInt(activity.getAutoActionID());
		    }
        }
        else if(activity.isType(Activity.TYPE_GSEDITION))
        {
            isGSEdition = true;
            if(activity.getEditionActionID() != null)
            {
                gsEditionActionID = Integer.parseInt(activity.getEditionActionID());
            }
        }
        else
        {
            isTranslate = true;
        }
        qaChecks = activity.getQaChecks() ? "checked" : "";

        if (activity.getRunDitaQAChecks())
        {
            isDitaCheckChecked = "checked";
        }
    }
    enableQAChecks = company.getEnableQAChecks();
    isEnableDitaChecks = company.getEnableDitaChecks();
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_activity_type")%>";
var guideNode="activityType";
var helpFile = "<%=bundle.getString("help_activity_types_basic_screen")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        activityForm.action = "<%=cancelURL%>";
        activityForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            activityForm.action = "<%=saveURL%>";
            activityForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate activity name.
//
function confirmForm()
{
    if (!activityForm.nameField) 
    {
        // can't change name on edit
        return true;
    }
    activityForm.nameField.value = ATrim(activityForm.nameField.value);
    if (isEmptyString(activityForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_activity_name"))%>");
        activityForm.nameField.value = "";
        activityForm.nameField.focus();
        return false;
    }        
    if (hasSpecialChars(activityForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
          "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;
    }
    if (hasSpace(activityForm.nameField.value)) {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
          "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;        
    }

    // check for dups 
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String actname = (String)names.get(i);
            actname = actname.toLowerCase();
%>
            if ("<%=actname%>" == activityForm.nameField.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_activity_type"))%>");
                return false;
            }
<%
        }
    }
%>

    return true;
}

function hasSpace(theField)
{
    var iChars = ' ';
    for (var i = 0; i < theField.length; i++)
    {
        if (iChars == theField.charAt(i))
        {
            return true;
        }
    }
    return false;
}

function doOnload()
{
    loadGuides();

    var edit = eval("<%=edit%>");
    if (edit)
    {
        activityForm.<%=ActivityConstants.DESC%>.focus();
    }
    else
    {
        activityForm.<%=ActivityConstants.NAME%>.focus();
    }
}

function radioClick() {
	var radio = document.all.radios;

    if (radio[3].checked) {
		autoActionDiv.style.display='';
	}
	else {
        autoActionDiv.style.display='none';
	}
}
</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

  <%
  String autoAbleFlag = "";
  String GSEditionAbleFlag = "";
  boolean  autoActionShowFlag = false;
  boolean GSEditionShowFlag = false;
  if (!userPerms.getPermissionFor(Permission.AUTOMATIC_ACTIONS_VIEW)) {
	    if(edit && isAutoAction) {
          autoActionShowFlag = true;
      }
    
     autoAbleFlag = "disabled";
  }
  else {
     autoActionShowFlag = true;
  }

  if (!userPerms.getPermissionFor(Permission.GSEDITION_ACTIONS_VIEW)) {
	    if(edit && isGSEdition) {
         GSEditionShowFlag = true;
      }
    
    GSEditionAbleFlag = "disabled";
  }
  else {
     GSEditionShowFlag = true;
  }
	%>
	
<script>
 function radioClick() {
	var radio = document.all.radios;

<%if(GSEditionShowFlag && autoActionShowFlag) {%>
  if (radio[3].checked) {
      autoActionDiv.style.display='';
      GSEditionDiv.style.display='none';
	}
	else if (radio[4].checked) {
      GSEditionDiv.style.display='';
      autoActionDiv.style.display='none';
	}
	else {
      autoActionDiv.style.display='none';
      GSEditionDiv.style.display='none';
	}
<%}else if(!GSEditionShowFlag && autoActionShowFlag) {%>

  if (radio[3].checked) {
      autoActionDiv.style.display='';
	}
	else {
      autoActionDiv.style.display='none';
	}
<%}else if(GSEditionShowFlag && !autoActionShowFlag) {%>
  if (radio[3].checked) {
      GSEditionDiv.style.display='';
	}
	else {
      GSEditionDiv.style.display='none';
	}
<%}%>
    
    
}

</script>   
    	

<form name="activityForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <% if (edit) { %>
	    <%=activityName%>
            <% } else { %>
	    <input type="textfield" name="<%=ActivityConstants.NAME%>"
	    maxlength="24" size="24" value="<%=activityName%>" class="standardText">
            <% } %>
          </td>
          <td valign="center">
            <% if (!edit) { %>
	    <%=bundle.getString("lb_valid_name")%>
            <% } %>
          </td>
        </tr>
        <tr>
          <td valign="top">
	    <%=bundle.getString("lb_description")%>:
          </td>
          <td colspan="2">
            <input type="hidden" name = "useTypeField" value = "TRANS">
            <textarea rows="6" cols="40" name="<%=ActivityConstants.DESC%>" class="standardText"><%=desc%></textarea>
          </td>
        </tr>
        <tr><td colspan="3">&nbsp;</td></tr>
  <%
  String ableFlag = "";
  boolean showFlag = false;
	if (!userPerms.getPermissionFor(Permission.AUTOMATIC_ACTIONS_VIEW)) {
	    if(edit && isAutoAction) {
        showFlag = true;
      }
    
    ableFlag = "disabled";
  }
  else {
    showFlag = true;
  }
	%>
<TR>
	  <TD><%=bundle.getString("lb_type")%>:</TD>
	  <TD colspan="2">
	    <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.TRANSLATE%>" <%=isTranslate ? "checked" : ""%> onclick="radioClick()">
        <%=bundle.getString("lb_translate")%>&nbsp;&nbsp;&nbsp;&nbsp;

        <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.REVIEW_EDITABLE%>" <%=isReviewEditable ? "checked" : ""%> onclick="radioClick()">
        <%=bundle.getString("lb_review_editable")%>&nbsp;&nbsp;&nbsp;&nbsp;

        <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.REVIEW_NOT_EDITABLE%>" <%=isReviewNotEditable ? "checked" : ""%> onclick="radioClick()">
        <%=bundle.getString("lb_review_notEditable")%>&nbsp;&nbsp;&nbsp;&nbsp;
        
        <%if(autoActionShowFlag) {%>
		    <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.AUTO_ACTION%>" <%=isAutoAction ? "checked" : ""%> onclick="radioClick()" <%=autoAbleFlag%>>
        <%=bundle.getString("lb_automatic_actions")%>&nbsp;&nbsp;
        <%}%>
        
        <%if(GSEditionShowFlag) {%>
		    <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.GSEDITION%>" <%=isGSEdition ? "checked" : ""%> onclick="radioClick()" <%=GSEditionAbleFlag%>>
        <%=bundle.getString("lb_gsedition_actions")%>&nbsp;&nbsp;
        <%}%>
		</TD>
	</TR>
	<!----------Automatic Action---------------->
	<%if(autoActionShowFlag) {%>
	<TR id="autoActionDiv" style="display<%=isAutoAction ? "" : ":none"%>;">
	    <TD><%=bundle.getString("lb_select_autoactions")%>:</TD>
	    <TD colspan="2">

		<SELECT ID="SelectAutoAction" NAME="SelectAutoAction" <%=autoAbleFlag%>>
		<%
    ArrayList autoactions = (ArrayList) actionManager.getAllActions();

		for(int i = 0; i < autoactions.size(); i++) {
		    AutoAction autoaction = (AutoAction)autoactions.get(i);
			  String selected = "";

			  if(autoaction.getId() == autoActionID) {
            selected = "SELECTED";
			  }
	    %>
		<OPTION VALUE="<%=autoaction.getId()%>" <%=selected%>><%=autoaction.getName()%></OPTION>
		<%
		}
		%>
        </SELECT>

	  </TD>
	</TR>
	<%}%>
	<!----------GS Edition Action---------------->
  <%if(GSEditionShowFlag) {%>
	<TR id="GSEditionDiv" style="display<%=isGSEdition ? "" : ":none"%>;">
	    <TD><%=bundle.getString("lb_select_gsedition_action")%>:</TD>
	    <TD colspan="2">

		  <SELECT ID="SelectEditionAction" NAME="SelectEditionAction" <%=GSEditionAbleFlag%>>
		  <%
      ArrayList gsactions = (ArrayList) gsManager.getAllActions();

		  for(int i = 0; i < gsactions.size(); i++) {
		      GSEditionActivity gsEditionActivity = (GSEditionActivity)gsactions.get(i);
			    String selected = "";

			    if(gsEditionActivity.getId() == gsEditionActionID) {
              selected = "SELECTED";
			}
	    %>
		      <OPTION VALUE="<%=gsEditionActivity.getId()%>" <%=selected%>><%=gsEditionActivity.getName()%></OPTION>
		  <%
		  }
		  %>
      </SELECT>
	  </TD>
	</TR>
	<%}%>
	
	<% if (enableQAChecks) {%>
    <tr>
        <td><%=bundle.getString("lb_activity_qa_checks")%>:</td>
        <td><INPUT TYPE=checkbox id="qaChecks" name="qaChecks" <%=qaChecks%> >
        </td>
    </tr>
    <%} %>

    <% if (isEnableDitaChecks) { %>
    <tr>
        <td><%=bundle.getString("lb_is_dita_activity")%>:</td>
        <td colspan="2"><input type="checkbox" name="<%=ActivityConstants.IS_DITA_QA_CHECK_ACTIVITY%>" <%=isDitaCheckChecked%> /></td>
    </tr>
    <% } %>
	<tr><td colspan=3>&nbsp;</td></tr>
	<tr>
	  <td>&nbsp;</td>
	  <td colspan="2">
	    <input type="button" class="standardText" name="<%=lbcancel%>" value="<%=lbcancel%>"
	    onclick="submitForm('cancel')">
	    <input type="button" class="standardText" name="<%=lbsave%>" value="<%=lbsave%>"
	    onclick="submitForm('save')">
	  </td>
	</tr>
      </table>
    </td>
  </tr>
</table>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_ACTIVITY_TYPE); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />

</form>
</div>
</body>
</html>
