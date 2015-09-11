<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
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

    // For sla report issue
    boolean isTranslate = false;
    boolean isReviewEditable = false;
    boolean isReviewNotEditable = false;
	String qaChecks = "";
	Company company = null;
	boolean enableQAChecks = false;
	boolean isEnableDitaChecks = false;
	boolean isAutoCompleteActivity = false;
	
	boolean isAfterJobCreation = false;
	boolean isAfterJobDispatch = false;
	boolean isAfterActivityStart = false;
	
	String []strArr;
	
	String afterJobCreation = "";
	String afterJobDispatch = "";
	String afterActivityStart = "";
	
	String afterJobCreationD = "";
	String afterJobCreationH = "";
	String afterJobCreationM = "";
	
	String afterJobDispatchD = "";
	String afterJobDispatchH = "";
	String afterJobDispatchM = "";
	
	String afterActivityStartD = "";
	String afterActivityStartH = "";
	String afterActivityStartM = "";
	
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

        isAutoCompleteActivity = activity.getAutoCompleteActivity();
        afterJobCreation = activity.getAfterJobCreation();
        afterJobDispatch = activity.getAfterJobDispatch();
        afterActivityStart = activity.getAfterActivityStart();
        
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
        else
        {
            isTranslate = true;
        }
        qaChecks = activity.getQaChecks() ? "checked" : "";

        if (activity.getRunDitaQAChecks())
        {
            isDitaCheckChecked = "checked";
        }
        if(isAutoCompleteActivity)
        {
        	if(afterJobCreation !="" && afterJobCreation != null)
            {
            	isAfterJobCreation = true;
            	strArr = afterJobCreation.split("-");
            	afterJobCreationD = strArr[0];
            	afterJobCreationH = strArr[1];
            	afterJobCreationM = strArr[2];
            }
            else if(afterJobDispatch !="" && afterJobDispatch != null)
            {
            	isAfterJobDispatch = true;
            	strArr = afterJobDispatch.split("-");
            	afterJobDispatchD = strArr[0];
            	afterJobDispatchH = strArr[1];
            	afterJobDispatchM = strArr[2];
            }
            else if(afterActivityStart !="" && afterActivityStart != null)
            {
            	isAfterActivityStart = true;
            	strArr = afterActivityStart.split("-");
            	afterActivityStartD = strArr[0];
            	afterActivityStartH = strArr[1];
            	afterActivityStartM = strArr[2];
            }
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
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script type="text/javascript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_activity_type")%>";
var guideNode="activityType";
var helpFile = "<%=bundle.getString("help_activity_types_basic_screen")%>";
var saveNewURL = "<%=saveURL%>";

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
            activityForm.action = saveNewURL;
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
        return checkACA();
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

    return checkACA();
}

function checkEmpty(d,h,m)
{
	d=ATrim(d);
	h=ATrim(h);
	m=ATrim(m);

	if (h.length==0)
	{
		h="0";
	}
	if (d.length==0)
	{
		d="0";
	}
	if (m.length==0)
	{
		m="0";
	}
	return [d,h,m]
}

function checkACA()
{
	var checkbox = document.getElementById("autoCompleteActivity");
	if(checkbox.checked)
	{
		var radioValue = document.getElementsByName("autoCompleteActivity");
		var isSelectOne = false;
		for(var j=0;j<radioValue.length;j++)
		{
			if(radioValue[j].checked)
			{
				isSelectOne = true;
				var testD;
				var testH;
				var testM;
				if("afterJobCreation" == radioValue[j].value)
				{
					testD = document.getElementById("afterJobCreationD");
					testH = document.getElementById("afterJobCreationH");
					testM = document.getElementById("afterJobCreationM");
				}
				else if("afterJobDispatch" == radioValue[j].value)
				{
					testD = document.getElementById("afterJobDispatchD");
					testH = document.getElementById("afterJobDispatchH");
					testM = document.getElementById("afterJobDispatchM");
				}
				else
				{
					testD = document.getElementById("afterActivityStartD");
					testH = document.getElementById("afterActivityStartH");
					testM = document.getElementById("afterActivityStartM");
				}

				var checkE = checkEmpty(testD.value,testH.value,testM.value);
				testD = checkE[0];
				testH = checkE[1];
				testM = checkE[2];

				if(testD=="0" && testH=="0" && testM=="0" && "afterActivityStart" != radioValue[j].value)
				{
					alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_activity_time_all_zero_check"))%>");
					return false;
				}
				else if(testD=="0" && testH=="0" && testM=="0" && "afterActivityStart" == radioValue[j].value)
				{
					if(!confirm("<%=EditUtil.toJavascript(bundle.getString("jsmsg_activity_time_all_zero"))%>"))
					{
						return false;
					}
				}
				if(!isAllDigits(testD)||!isAllDigits(testH)||!isAllDigits(testM)||parseInt(testH)>23||parseInt(testM)>59||parseInt(testD)>365)
				{
					alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_activity_time_invalid"))%>");
					return false; 
				}
				
				saveNewURL+="&isAutoCompleteActivity=" + checkbox.checked + "&" + radioValue[j].value + "=" + testD+"-"+testH+"-"+testM;
			}
		}
		if(!isSelectOne)
		{
			alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_activity_time_not_check"))%>");
			return false;
		}
	}
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

$(document).ready(function(){

	if(("true" == "<%=isAutoCompleteActivity%>"))
	{
		setDisableTR('ACA1', false);
		setDisableTR('ACA2', false);
		setDisableTR('ACA3', false);
	}
	else
	{
		setDisableTR('ACA1', true);
		setDisableTR('ACA2', true);
		setDisableTR('ACA3', true);
	}
	$("#autoCompleteActivity").click(function(){
		if(this.checked)
		{
			setDisableTR('ACA1', false);
			setDisableTR('ACA2', false);
			setDisableTR('ACA3', false);
		}
		else
		{
			setDisableTR('ACA1', true);
			setDisableTR('ACA2', true);
			setDisableTR('ACA3', true);
		}
	});
});

/**
 * Disable/Enable TR element
 * 
 * @param trId
 *            The id of TR item
 * @param isDisabled
 *            Disable/Enable flag
 */
function setDisableTR(trId, isDisabled) 
{
	var trElem = document.getElementById(trId);
	var color;
	if (isDisabled) 
	{
		color = "gray";
	} 
	else 
	{
		color = "black";
	}
	trElem.style.color = color;
	
	// Operate text elements
	elems = trElem.getElementsByTagName("input");
	for ( var i = 0; i < elems.length; i++) 
	{
		if ("radio" == elems[i].type || "test" == elems[i].type) 
		{
			elems[i].disabled = isDisabled;
			elems[i].style.color = color;
		}
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

<script>
function radioClick()
{
	var radio = document.all.radios;

	if(!document.getElementById("afterJobCreation").checked)
	{
		 document.getElementById("afterJobCreationD").value="";
		 document.getElementById("afterJobCreationH").value="";
		 document.getElementById("afterJobCreationM").value="";
	}
	if(!document.getElementById("afterJobDispatch").checked)
	{
		 document.getElementById("afterJobDispatchD").value="";
		 document.getElementById("afterJobDispatchH").value="";
		 document.getElementById("afterJobDispatchM").value="";
	}
	if(!document.getElementById("afterActivityStart").checked)
	{
		 document.getElementById("afterActivityStartD").value="";
		 document.getElementById("afterActivityStartH").value="";
		 document.getElementById("afterActivityStartM").value="";
	}
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
    <TR>
	  <TD><%=bundle.getString("lb_type")%>:</TD>
	  <TD colspan="2">
	    <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.TRANSLATE%>" <%=isTranslate ? "checked" : ""%> onclick="radioClick()">
        <%=bundle.getString("lb_translate")%>&nbsp;&nbsp;&nbsp;&nbsp;

        <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.REVIEW_EDITABLE%>" <%=isReviewEditable ? "checked" : ""%> onclick="radioClick()">
        <%=bundle.getString("lb_review_editable")%>&nbsp;&nbsp;&nbsp;&nbsp;

        <input type="radio" name="<%=ActivityConstants.TYPE%>" id ="radios" value="<%=ActivityConstants.REVIEW_NOT_EDITABLE%>" <%=isReviewNotEditable ? "checked" : ""%> onclick="radioClick()">
        <%=bundle.getString("lb_review_notEditable")%>&nbsp;&nbsp;&nbsp;&nbsp;
	  </TD>
	</TR>
	<tr><td colspan="3">&nbsp;</td></tr>
	<TR>
	<TD colspan="3"><%=bundle.getString("lb_activity_auto_complete")%>:&nbsp;&nbsp;
	<input type="checkbox" name="autoCompleteActivityName" id ="autoCompleteActivity" value="true" <%=isAutoCompleteActivity ? "checked" : ""%> >
	</TD></TR>
	<TR id="ACA1"><TD>&nbsp;&nbsp;&nbsp;</TD><TD>
	<input type="radio" name="autoCompleteActivity" id ="afterJobCreation" value="afterJobCreation" <%=isAfterJobCreation ? "checked" : ""%> onclick="radioClick()">
     <%=bundle.getString("lb_activity_after_job_creation")%>&nbsp;&nbsp;</TD><TD>
     <input type="text" name="afterJobCreationD" id ="afterJobCreationD" size="3" value="<%=afterJobCreationD%>">&nbsp;d&nbsp;
     <input type="text" name="afterJobCreationH" id ="afterJobCreationH" size="3" value="<%=afterJobCreationH%>">&nbsp;h&nbsp;
     <input type="text" name="afterJobCreationM" id ="afterJobCreationM" size="3" value="<%=afterJobCreationM%>">&nbsp;m&nbsp;
	</TD></TR>
	<TR id="ACA2"><TD>&nbsp;&nbsp;&nbsp;</TD><TD>
	<input type="radio" name="autoCompleteActivity" id ="afterJobDispatch" value="afterJobDispatch" <%=isAfterJobDispatch ? "checked" : ""%> onclick="radioClick()">
     <%=bundle.getString("lb_activity_after_job_dispatch")%></TD><TD>
     <input type="text" name="afterJobDispatchD" id ="afterJobDispatchD" size="3" value="<%=afterJobDispatchD%>">&nbsp;d&nbsp;
     <input type="text" name="afterJobDispatchH" id ="afterJobDispatchH" size="3" value="<%=afterJobDispatchH%>">&nbsp;h&nbsp;
     <input type="text" name="afterJobDispatchM" id ="afterJobDispatchM" size="3" value="<%=afterJobDispatchM%>">&nbsp;m&nbsp;
	</TD></TR>
	<TR id="ACA3"><TD>&nbsp;&nbsp;&nbsp;</TD><TD>
	<input type="radio" name="autoCompleteActivity" id ="afterActivityStart" value="afterActivityStart" <%=isAfterActivityStart ? "checked" : ""%> onclick="radioClick()">
     <%=bundle.getString("lb_activity_after_activity_start")%></TD><TD>
     <input type="text" name="afterActivityStartD" id ="afterActivityStartD" size="3" value="<%=afterActivityStartD%>">&nbsp;d&nbsp;
     <input type="text" name="afterActivityStartH" id ="afterActivityStartH" size="3" value="<%=afterActivityStartH%>">&nbsp;h&nbsp;
     <input type="text" name="afterActivityStartM" id ="afterActivityStartM" size="3" value="<%=afterActivityStartM%>">&nbsp;m&nbsp;
	</TD></TR>
	
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
