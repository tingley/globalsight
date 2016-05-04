<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
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
  session="true"%>
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String validateURL = self.getPageURL() + "&action=validate";
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
	
    if (activity == null)
    {
    	company = CompanyWrapper.getCurrentCompany();
        isTranslate = true;
        activity = new Activity();
    }
    else
    {
    	company = CompanyWrapper.getCompanyById(activity.getCompanyId());
        activityName = activity.getDisplayName();
        activityUseType = activity.getUseType();
        desc = activity.getDescription();

        isAutoCompleteActivity = activity.getAutoCompleteActivity();
        
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
    }
    enableQAChecks = company.getEnableQAChecks();
    isEnableDitaChecks = company.getEnableDitaChecks();
%>
<html>
<head>
<title><%=title%></title>
<link rel="stylesheet" href="/globalsight/jquery/jQueryUI.redmond.css">
<link rel="stylesheet" href="/globalsight/jquery/chosen.css">
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.10.2.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.11.2.js"></script>
<script SRC="/globalsight/includes/vue/vue.js"></script>
<script SRC="/globalsight/jquery/chosen.jquery.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>

<style type="text/css">
.number{ime-mode:disabled;}
.scheduleLab{width:80px;float:left;padding-top: 5px;}
.weekDiv{width:90px;float:left;padding: 1px;}
</style>
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

function checkSchedule()
{
    if (!vueDate.autoComplete)
        return true;
    
    var result = true;
    $.ajax({
        type : "POST",
        url : '<%=validateURL%>',
        async : false,
        cache : false,
        dataType : 'text',
        data : $('#activityForm').serialize(),
        success : function(data) {
        	result = data;
        },
        error : function(request, error, status) {
        	result = "error";
            alert(error);
        }
    });
    
    if (result == "")
    	return true;
    else{
    	alert(result);
    	return false;
    }
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
	 if (!checkSchedule())
	        return false;
	 
	var checkbox = document.getElementById("autoCompleteActivity");
	saveNewURL+="&isAutoCompleteActivity=" + checkbox.checked;
	
	if(checkbox.checked)
	{
		var radioValue = document.getElementsByName("autoCompleteActivity");
		var isSelectOne = false;
		for(var j=0;j<radioValue.length;j++)
		{
			if(radioValue[j].checked)
			{
				isSelectOne = true;
				
				if ("4" == radioValue[j].value)
				{
					
				}
				else
				{
					var testD;
					var testH;
					var testM;
					if("1" == radioValue[j].value)
					{
						testD = document.getElementById("afterJobCreationD");
						testH = document.getElementById("afterJobCreationH");
						testM = document.getElementById("afterJobCreationM");
					}
					else if("2" == radioValue[j].value)
					{
						testD = document.getElementById("afterJobDispatchD");
						testH = document.getElementById("afterJobDispatchH");
						testM = document.getElementById("afterJobDispatchM");
					}
					else if("3" == radioValue[j].value)
					{
						testD = document.getElementById("afterActivityStartD");
						testH = document.getElementById("afterActivityStartH");
						testM = document.getElementById("afterActivityStartM");
					} 
					else
					{
						//alert($("#monthlyMonths").val());
						//return false;
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
						alert("<%=EditUtil.toJavascript(bundle
						.getString("jsmsg_activity_time_invalid"))%>");
						return false; 
					}
					
					saveNewURL+= "&" + radioValue[j].value + "=" + testD+"-"+testH+"-"+testM;
				}	
			}
		}
		if(!isSelectOne)
		{
			alert("<%=EditUtil.toJavascript(bundle
					.getString("msg_activity_time_not_check"))%>");
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
		if (edit) {
			activityForm.<%=ActivityConstants.DESC%>.focus();
		} else {
			activityForm.<%=ActivityConstants.NAME%>.focus();
		}
	}

	Vue.directive('disable', function(value) {
		this.el.disabled = !!value;
		this.el.style.color = value ? 'gray' : 'black';
	});

	Vue.directive('disable2', function(value) {
		this.el.disabled = !!value;
		this.el.style.color = value ? 'gray' : 'black';
		
		$(this.el).attr("disabled",!!value).trigger("chosen:updated");
	});
	
	Vue.directive('clean', function(value) {
		if (value)
			this.el.value = '';
	});

	Vue.directive('showDiv', function(value) {
		var d = (value != this.arg);
		this.el.display = d ? "none" : "";
	});
	
	Vue.filter('timeNumber', function (value) {
		if (value < 10)
			return "0" + value;
		
		return value;
	})

	var vueDate
	$(function() {
		$(".datepicker").datepicker({
			dateFormat : "mm/dd/yy"
		});
		vueDate = new Vue({
			el : '#app',
			data :<%=activity.toJson()%>
	    });
		
		$('.multipleSelector').chosen({
			width : "100%"
		});
		
		$('.number').onlyNum();
	});
	
	$.fn.onlyNum = function () {
	     $(this).keypress(function (event) {
	         var eventObj = event || e;
	         var keyCode = eventObj.keyCode || eventObj.which;
	         if ((keyCode >= 48 && keyCode <= 57))
	             return true;
	         else
	             return false;
	     });
	 };
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="doOnload()"  class="standardText" >
  <%@ include file="/envoy/common/header.jspIncl"%>
  <%@ include file="/envoy/common/navigation.jspIncl"%>
  <%@ include file="/envoy/wizards/guides.jspIncl"%>
  <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading"><%=title%></span>
    <form id="activityForm" name="activityForm" method="post" action="">
      <table border="0" cellspacing="2" cellpadding="2" class="standardText" id="app">
        <tr valign="top">
          <td>
            <table border="0" class="standardText" cellpadding="2">
              <tr>
                <td><amb:lb key="lb_name"/><span class="asterisk">*</span>:
                </td>
                <td>
                  <%
                  	if (edit) {
                  %>
                  <%=activityName%>
                  <%
                  	} else {
                  %>
                  <input type="textfield" name="<%=ActivityConstants.NAME%>" maxlength="24" size="24" value="<%=activityName%>" class="standardText">
                  <%
                  	}
                  %>
                </td>
                <td valign="center">
                  <%
                  	if (!edit) {
                  %>
                  <%=bundle.getString("lb_valid_name")%>
                  <%
                  	}
                  %>
                </td>
              </tr>
              <tr>
                <td valign="top"><amb:lb key="lb_description"/>:
                </td>
                <td colspan="2">
                  <input type="hidden" name="useTypeField" value="TRANS">
                  <textarea rows="6" cols="40" name="<%=ActivityConstants.DESC%>" class="standardText" v-model="desc"></textarea>
                </td>
              </tr>
              <TR>
                <TD><amb:lb key="lb_type"/>:
                </TD>
                <TD colspan="2">
                  <input type="radio" name="<%=ActivityConstants.TYPE%>" id="radios" value="<%=Activity.TYPE_TRANSLATE%>" v-model="type">
                  <amb:lb key="lb_translate"/>&nbsp;&nbsp;&nbsp;&nbsp;
                  <input type="radio" name="<%=ActivityConstants.TYPE%>" id="radios" value="<%=Activity.TYPE_REVIEW_EDITABLE%>" v-model="type">
                  <amb:lb key="lb_review_editable"/>&nbsp;&nbsp;&nbsp;&nbsp;
                  <input type="radio" name="<%=ActivityConstants.TYPE%>" id="radios" value="<%=Activity.TYPE_REVIEW%>" v-model="type">
                  <amb:lb key="lb_review_notEditable"/>&nbsp;&nbsp;&nbsp;&nbsp;
                </TD>
              </TR>
              <TR>
                <TD colspan="3"><amb:lb key="lb_activity_auto_complete"/>:&nbsp;&nbsp;
                  <input type="checkbox" name="autoCompleteActivityName" id="autoCompleteActivity" value="true" v-model="autoComplete">
                </TD>
              </TR>
              <TR v-disable="!autoComplete">
                <TD>&nbsp;&nbsp;&nbsp;</TD>
                <TD>
                  <input type="radio" name="autoCompleteActivity" id="afterJobCreation" value="1" v-model="completeType" v-disable="!autoComplete">
                  <amb:lb key="lb_activity_after_job_creation"/>&nbsp;&nbsp;
                </TD>
                <TD>
                  <input type="text" name="afterJobCreationD" id="afterJobCreationD" v-model="afterJobCreationD" size="3" v-clean="completeType!=1" v-disable="!autoComplete || completeType!=1">
                  &nbsp;d&nbsp;
                  <input type="text" name="afterJobCreationH" id="afterJobCreationH" v-model="afterJobCreationH" size="3" v-clean="completeType!=1" v-disable="!autoComplete || completeType!=1">
                  &nbsp;h&nbsp;
                  <input type="text" name="afterJobCreationM" id="afterJobCreationM" v-model="afterJobCreationM" size="3" v-clean="completeType!=1" v-disable="!autoComplete || completeType!=1">
                  &nbsp;m&nbsp;
                </TD>
              </TR>
              <TR v-disable="!autoComplete">
                <TD>&nbsp;&nbsp;&nbsp;</TD>
                <TD>
                  <input type="radio" name="autoCompleteActivity" id="afterJobDispatch" value="2" v-model="completeType" v-disable="!autoComplete">
                  <amb:lb key="lb_activity_after_job_dispatch"/></TD>
                <TD>
                  <input type="text" name="afterJobDispatchD" id="afterJobDispatchD" v-model="afterJobDispatchD" size="3" v-clean="completeType!=2" v-disable="!autoComplete ||completeType!=2">
                  &nbsp;d&nbsp;
                  <input type="text" name="afterJobDispatchH" id="afterJobDispatchH" v-model="afterJobDispatchH" size="3" v-clean="completeType!=2" v-disable="!autoComplete ||completeType!=2">
                  &nbsp;h&nbsp;
                  <input type="text" name="afterJobDispatchM" id="afterJobDispatchM" v-model="afterJobDispatchM" size="3" v-clean="completeType!=2" v-disable="!autoComplete ||completeType!=2">
                  &nbsp;m&nbsp;
                </TD>
              </TR>
              <TR v-disable="!autoComplete">
                <TD>&nbsp;&nbsp;&nbsp;</TD>
                <TD>
                  <input type="radio" name="autoCompleteActivity" id="afterActivityStart" value="3" v-model="completeType" v-disable="!autoComplete">
                  <amb:lb key="lb_activity_after_activity_start"/></TD>
                <TD>
                  <input type="text" name="afterActivityStartD" id="afterActivityStartD" v-model="afterActivityStartD" size="3" v-clean="completeType!=3" v-disable="!autoComplete ||completeType!=3">
                  &nbsp;d&nbsp;
                  <input type="text" name="afterActivityStartH" id="afterActivityStartH" v-model="afterActivityStartH" size="3" v-clean="completeType!=3" v-disable="!autoComplete ||completeType!=3">
                  &nbsp;h&nbsp;
                  <input type="text" name="afterActivityStartM" id="afterActivityStartM" v-model="afterActivityStartM" size="3" v-clean="completeType!=3" v-disable="!autoComplete ||completeType!=3">
                  &nbsp;m&nbsp;
                </TD>
              </TR>
              <TR v-disable="!autoComplete">
                <TD>&nbsp;&nbsp;&nbsp;</TD>
                <TD valign="top">
                  <input type="radio" name="autoCompleteActivity" id="schedule" value="4" v-model="completeType" v-disable="!autoComplete">
                  <amb:lb key="lb_schedule"/>
                </TD>
                <TD>
                  <table v-show="autoComplete&&completeType==4" class="standardText">
                    <tr>
                      <td valign="top">
                        <input type="radio" name="scheduleType" v-model="scheduleType" value="5" v-scheduleDisable="completeType" v-disable="completeType!=4">
                        <amb:lb key="lb_daily"/><br>
                        <input type="radio" name="scheduleType" v-model="scheduleType" value="6" v-scheduleDisable="completeType" v-disable="completeType!=4">
                        <amb:lb key="lb_weekly"/><br>
                        <input type="radio" name="scheduleType" v-model="scheduleType" value="7" v-scheduleDisable="completeType" v-disable="completeType!=4">
                        <amb:lb key="lb_monthly"/><br>
                        <input type="radio" name="scheduleType" v-model="scheduleType" value="8" v-scheduleDisable="completeType" v-disable="completeType!=4">
                        <amb:lb key="lb_one_time"/><br>
                      </td>
                      <td valign="top" style="padding-left: 50px">
                        <div v-show="scheduleType" style="padding-bottom: 10px;"> 
                            <div class="scheduleLab"><amb:lb key="lb_start"/>:</div>
                            <input type="text" readonly="readonly" name="startDate" v-model="startDate" id="startDate" class="datepicker" placeholder="MM/dd/yyyy">
                            <select name="startH" v-model="startH" data-placeholder="HH" >
                              <option v-for="option in 24" :value="option">{{ option | timeNumber }}</option>
                            </select>&nbsp; <b>:</b>
                          
                            <select name="startM" v-model="startM" data-placeholder="MM" >
                              <option v-for="option in 60" :value="option">{{ option | timeNumber}}</option>
                            </select>
                        </div>
                        <div v-show="scheduleType==5">
                            <div class="scheduleLab"><amb:lb key="lb_recur_every"/>:</div>
                            <input type="number" class="number" v-model="dailyRecur" name="dailyRecur" id="dailyRecur" size="3"><amb:lb key="lb_days"/>                       
                        </div>
                        <div v-show="scheduleType==6">
                            <div class="scheduleLab"><amb:lb key="lb_recur_every"/>:</div>
                            <div style="float: left">
                               <div style="padding-bottom: 10px;">
	                               <input type="number" class="number" name="weeklyRecur" v-model="weeklyRecur" id="weeklyRecur" size="3">
	                                 <amb:lb key="lb_weeks_on"/>:
	                           </div>
	                                 
	                           <div style="width:300px;">
	                              <div class="weekDiv">
	                                <input type="checkbox" name="weeklyWeeks" v-model="weeklyWeeks" value="0" >
                                    <amb:lb key="lb_sunday"/>
                                  </div>
                                  <div class="weekDiv">
                                     <input type="checkbox" name="weeklyWeeks" v-model="weeklyWeeks" value="1" >
                                     <amb:lb key="lb_monday"/>
                                  </div>
                                  <div class="weekDiv">
                                     <input type="checkbox" name="weeklyWeeks" v-model="weeklyWeeks" value="2">
                                     <amb:lb key="lb_tuesday"/>
                                  </div>
                                  <div class="weekDiv">
                                     <input type="checkbox" name="weeklyWeeks" v-model="weeklyWeeks" value="3">
                                     <amb:lb key="lb_wednesday"/>
                                  </div>
                                 <div class="weekDiv">
                                     <input type="checkbox" name="weeklyWeeks" v-model="weeklyWeeks" value="4">
                                     <amb:lb key="lb_thursday"/>
                                  </div>
                                  <div class="weekDiv">
                                     <input type="checkbox" name="weeklyWeeks" v-model="weeklyWeeks" value="5">
                                     <amb:lb key="lb_friday"/>
                                  </div>
                                   <div class="weekDiv">
                                     <input type="checkbox" name="weeklyWeeks" v-model="weeklyWeeks" value="6">
                                     <amb:lb key="lb_saturday"/>
                                  </div>
                            </div>
                            </div>
                        </div>
                        <div  v-show="scheduleType==7">
                          <div class="scheduleLab"><amb:lb key="lb_months"/>:</div>
                          <table style="width:300px;" class="standardText">
                            <tr valign="top">
                              <td colspan="3">
                                <select name="monthlyMonths" v-model="monthlyMonths" id="monthlyMonths" class="multipleSelector" multiple data-placeholder="MM" v-clean="scheduleType!=7">
                                  <option value='1'><amb:lb key="lb_jan"/></option>
                                  <option value='2'><amb:lb key="lb_feb"/></option>
                                  <option value='3'><amb:lb key="lb_mar"/></option>
                                  <option value='4'><amb:lb key="lb_apr"/></option>
                                  <option value='5'><amb:lb key="lb_may"/></option>
                                  <option value='6'><amb:lb key="lb_june"/></option>
                                  <option value='7'><amb:lb key="lb_july"/></option>
                                  <option value='8'><amb:lb key="lb_aug"/></option>
                                  <option value='9'><amb:lb key="lb_sep"/></option>
                                  <option value='10'><amb:lb key="lb_oct"/></option>
                                  <option value='11'><amb:lb key="lb_nov"/></option>
                                  <option value='12'><amb:lb key="lb_dec"/></option>
                                </select>
                              </td>
                            </tr>
                            <tr valign="top">
                              <td nowrap="nowrap" style="padding-top: 5px; padding-right: 5px;">
                                  <input type="radio" name="monthCondition" v-model="monthCondition" value="1" ><amb:lb key="lb_schedule_days"/>:
                              </td>
                              <td colspan="2">
                                <select name="monthlyDays" v-model="monthlyDays" class="multipleSelector"  multiple data-placeholder="DD" v-disable2="monthCondition!=1" class="chosen-select" v-clean="scheduleType!=7">
                                  <option v-for="option in 31" :value="option + 1">{{ option + 1}}</option>
                                  <option value='32'><amb:lb key="lb_last_day"/></option>
                                </select>
                              </td>
                            </tr>
                            <tr valign="top" >
                              <td style="padding-top: 5px"><input type="radio" name="monthCondition" v-model="monthCondition" value="2" ><amb:lb key="lb_schedule_on"/>:</td>
                              <td valign="top" width="50%">
                              <div >
                                  <select name="monthlyOnPrefix" v-model="monthlyOnPrefix" class="multipleSelector"  multiple data-placeholder="Your Selection" v-clean="scheduleType!=7" v-disable2="monthCondition!=2">
                                    <option value='1'><amb:lb key="lb_first"/></option>
                                    <option value='2'><amb:lb key="lb_second"/></option>
                                    <option value='3'><amb:lb key="lb_third"/></option>
                                    <option value='4'><amb:lb key="lb_fourth"/></option>
                                    <option value='5'><amb:lb key="lb_last"/></option>
                                  </select> 
                                  </div>
                               </td>
                                <td width="50%">
                                <div>
                                  <select name="monthlyOnWeek" v-model="monthlyOnWeek" class="multipleSelector"  multiple data-placeholder="Your Selection" v-clean="scheduleType!=7" v-disable2="monthCondition!=2">
                                    <option value='0'><amb:lb key="lb_sunday"/></option>
                                    <option value='1'><amb:lb key="lb_monday"/></option>
                                    <option value='2'><amb:lb key="lb_tuesday"/></option>
                                    <option value='3'><amb:lb key="lb_wednesday"/></option>
                                    <option value='4'><amb:lb key="lb_thursday"/></option>
                                    <option value='5'><amb:lb key="lb_friday"/></option>
                                    <option value='6'><amb:lb key="lb_saturday"/></option>
                                  </select>
                                </div>
                              </td>
                            </tr>
                          </table>
                        </div>
                      </td>
                    </tr>
                  </table>
                </td>
              </TR>
              <br>
              <%
              	if (enableQAChecks) {
              %>
              <tr>
                <td><amb:lb key="lb_activity_qa_checks"/>:
                </td>
                <td>
                  <INPUT TYPE=checkbox id="qaChecks" name="qaChecks" <%=qaChecks%>>
                </td>
              </tr>
              <%
              	}
              %>
              <%
              	if (isEnableDitaChecks) {
              %>
              <tr>
                <td><amb:lb key="lb_is_dita_activity"/>:
                </td>
                <td colspan="2">
                  <input type="checkbox" name="<%=ActivityConstants.IS_DITA_QA_CHECK_ACTIVITY%>" <%=isDitaCheckChecked%> />
                </td>
              </tr>
              <%
              	}
              %>
              <tr>
                <td colspan=3>&nbsp;</td>
              </tr>
              <tr>
                <td>&nbsp;</td>
                <td colspan="2">
                  <input type="button" class="standardText" name="<%=lbcancel%>" value="<%=lbcancel%>" onclick="submitForm('cancel')">
                  <input type="button" class="standardText" name="<%=lbsave%>" value="<%=lbsave%>" onclick="submitForm('save')">
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      <%
      	String tokenName = FormUtil
      			.getTokenName(FormUtil.Forms.NEW_ACTIVITY_TYPE);
      %>
      <input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
    </form>
  </div>
</body>
</html>
