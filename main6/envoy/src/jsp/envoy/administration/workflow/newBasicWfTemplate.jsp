<%@page import="com.globalsight.everest.company.CompanyWrapper"%>
<%@page import="com.globalsight.everest.company.Company"%>
<%@page import="com.globalsight.everest.company.CompanyThreadLocal"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityManager"%>
<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
		 com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
		 com.globalsight.everest.webapp.pagehandler.PageHandler, 
		 com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.localemgr.CodeSet,
		 com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.servlet.EnvoyServletException,
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.SortUtil,
         com.globalsight.everest.foundation.LeverageLocales,
         com.globalsight.everest.util.comparator.UserComparator,
         java.util.Collections,
         java.util.Iterator,                
         java.util.List,                
         java.util.Locale,
         java.util.HashMap,
         java.util.Vector,
         java.util.Enumeration,
         java.util.ResourceBundle"
		 session="true" %>
         
<jsp:useBean id="nextPage" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self"   class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<%
   ResourceBundle bundle = PageHandler.getBundle(session);
   SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
   Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
   String actionType = (String) request.getAttribute(WorkflowTemplateConstants.ACTION);
   boolean isEdit = actionType != null && 
         actionType.equals(WorkflowTemplateConstants.EDIT_ACTION);
   String disabled = isEdit ? "DISABLED" : "";
   String nameField = WorkflowTemplateConstants.NAME_FIELD;
   String workflowTypeField = WorkflowTemplateConstants.WORKFLOW_TYPE_FIELD;
   String descriptionField = WorkflowTemplateConstants.DESCRIPTION_FIELD; 
   String projectField = WorkflowTemplateConstants.PROJECT_FIELD; 
   String notificationField = WorkflowTemplateConstants.NOTIFICATION_FIELD;
   String wfmField = WorkflowTemplateConstants.WFM_FIELD;
   String localePairField = WorkflowTemplateConstants.LOCALE_PAIR_FIELD;
   String encodingField = WorkflowTemplateConstants.ENCODING_FIELD;
   String leverageField = WorkflowTemplateConstants.LEVERAGE_FIELD;

   //labels
   String labelName = bundle.getString("lb_name");
   String labelDescription = bundle.getString("lb_description");
   String labelWorkflowType = bundle.getString("lb_workflow_type");
   String labelProject = bundle.getString("lb_project");
   String labelNotify = bundle.getString("lb_notification");
   String labelWorkflowManager = bundle.getString("lb_current_workflow_managers");
   String labelLocalePair = bundle.getString("lb_locale_pair");
   String labelTargetEncoding = bundle.getString("lb_target_encoding");
   String labelLeverage = bundle.getString("lb_target_cross_locale_leverage");
   String msgDuplicateName = bundle.getString("msg_duplicate_workflow_name");
   String choose = bundle.getString("lb_choose");

   // get the newly created or existing workflow template info
   WorkflowTemplateInfo wfti = (WorkflowTemplateInfo)
      sessionMgr.getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO);
   
   long companyId = -1;
   if (wfti == null)
   {
	   companyId = Long.parseLong(CompanyThreadLocal.getInstance().getValue());
   }
   else
   {
	   companyId = wfti.getCompanyId();
   }
   
   boolean usePerplexity = CompanyWrapper.getCompanyById(companyId).isEnablePerplexity();

   List templates = null;
   int cnt=0;
   try
   {        
        templates = new ArrayList(ServerProxy.getProjectHandler().
                                 getAllWorkflowTemplateInfos());
   }
   catch(Exception e)
   {
       throw new EnvoyServletException(e);
   }
   
   String jsmsg = "";
   long wfTemplateInfoId = -1;
   boolean isNew = (wfti == null || wfti.getId() < 0);
   if(!isNew) // edit
   {
       wfTemplateInfoId = Long.parseLong(request.getParameter("wfTemplateInfoId"));
   }
   if (templates != null) 
   {
       for(int i=0; i<templates.size(); i++)
       {
          WorkflowTemplateInfo wft = (WorkflowTemplateInfo)templates.get(i);
          if(isNew || wfTemplateInfoId != wft.getId())
          {
              jsmsg += "if(ATrim(basicTemplateForm." + nameField + ".value).toLowerCase() == \"" + wft.getName() + "\".toLowerCase())\n" +
                      "   {\n" +
                      "      alert('" + msgDuplicateName + "');\n" +
                      "      return false;\n" +
                      "   }\n";
          }
       }
   }

   List projectInfos = (List)sessionMgr.getAttribute(WorkflowTemplateConstants.PROJECTS);
   HashMap workflowManagers = (HashMap)sessionMgr.getAttribute(WorkflowTemplateConstants.WORKFLOW_MANAGERS);
   List localePairs = (List)sessionMgr.getAttribute(WorkflowTemplateConstants.LOCALE_PAIRS);
   List encodings = (List)sessionMgr.getAttribute(WorkflowTemplateConstants.ENCODINGS);
   Vector leverageDisp = (Vector)sessionMgr.getAttribute(WorkflowTemplateConstants.LEVERAGE_DISP);
   Vector leverageObjs = (Vector)sessionMgr.getAttribute(WorkflowTemplateConstants.LEVERAGE_OBJ);
   boolean leverageOn = leverageDisp != null;

   if(wfti != null)
   {
       leverageOn = true;
   }
   Long iflowTemplateId = (Long)request.getAttribute(WorkflowTemplateConstants.TEMPLATE_ID);
   
   
   String chosenName;
   String chosenDescription;
   String chosenWorkflowType = null;
   long chosenProject = -1;
   Boolean chosenNotifyFlag = null;
   List chosenWfms = null;
   String chosenLocalePair;
   String chosenTargetEncoding;
   Set<GlobalSightLocale> chosenLeverages = null;
   int scorecardShowType = -1;
   if(wfti == null)
   {
       chosenName = (String)sessionMgr.getAttribute(WorkflowTemplateConstants.CHOSEN_NAME);
       chosenDescription = (String)sessionMgr.getAttribute(WorkflowTemplateConstants.CHOSEN_DESCRIPTION);
       Long project = (Long)sessionMgr.getAttribute(WorkflowTemplateConstants.CHOSEN_PROJECT);
       if (project != null)
       {
        chosenProject = project.longValue();
       }
       chosenNotifyFlag = new Boolean((String)sessionMgr.getAttribute(WorkflowTemplateConstants.CHOSEN_NOTIFICATION));
       chosenLocalePair = (String)sessionMgr.getAttribute(WorkflowTemplateConstants.CHOSEN_LOCALE_PAIR);
       chosenTargetEncoding = (String)sessionMgr.getAttribute(WorkflowTemplateConstants.CHOSEN_TARGET_ENCODING);
   }
   else
   {
       // values to be populated in the UI fields
       chosenName = wfti.getName();
       chosenWorkflowType = wfti.getWorkflowType();
       chosenDescription = wfti.getDescription();
       chosenProject = wfti.getProject().getId();
       chosenNotifyFlag = new Boolean(wfti.notifyProjectManager());
       chosenWfms = wfti.getWorkflowManagerIds();
       chosenLocalePair = (String)sessionMgr.getAttribute(WorkflowTemplateConstants.LOCALE_PAIR);
       chosenTargetEncoding = wfti.getCodeSet();
       chosenLeverages = wfti.getLeveragingLocales();
       scorecardShowType = wfti.getScorecardShowType();
   }

   // Create a string representation of leverageObjs so we can 
   // turn it into a javascript array
   StringBuffer sbObjs = new StringBuffer();
   Enumeration vEnumObjs = leverageObjs.elements();
   while (vEnumObjs.hasMoreElements())
   {
       sbObjs.append("\"" + vEnumObjs.nextElement() + "\"");
       if (vEnumObjs.hasMoreElements())
       {
           sbObjs.append(",");
       }
   }
   String leverageObjsString = sbObjs.toString();


   // Create a string representation of leverageDisp so we can turn it 
   // into a javascript array
   StringBuffer sbDisp = new StringBuffer();
   Enumeration vEnumDisp = leverageDisp.elements();
   while (vEnumDisp.hasMoreElements())
   {
       sbDisp.append("\"" + vEnumDisp.nextElement() + "\"");
       if (vEnumDisp.hasMoreElements())
       {
           sbDisp.append(",");
       }
   }
   String leverageDispString = sbDisp.toString();
   
   // links for the next and cancel buttons
   String next2URL = nextPage.getPageURL() + (iflowTemplateId == null ? "" : 
                     ("&" + WorkflowTemplateConstants.TEMPLATE_ID + "=" 
                     + iflowTemplateId));
   String cancelURL = cancel.getPageURL() + "&" 
                     + WorkflowTemplateConstants.ACTION 
                     + "=" + WorkflowTemplateConstants.CANCEL_ACTION;
   String selfURL = self.getPageURL() + "&" 
                     + WorkflowTemplateConstants.ACTION 
                     + "=" + WorkflowTemplateConstants.LEVERAGE_ACTION;
   
   List<PerplexityService> perplexitys = PerplexityManager.getAllPerplexity();
   
   // Titles                                 
   String newTitle = bundle.getString("msg_wf_template_new_title1");
   String modifyTitle = bundle.getString("msg_wf_template_edit_title1");
   String wizardTitle = wfti == null ? newTitle : modifyTitle;
   String lbCancel = bundle.getString("lb_cancel");	
   String lbNext = bundle.getString("lb_next");	
      
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%= wizardTitle %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/jquery/jquery-1.11.3.min.js" type="text/javascript"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_workflow") %>";
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_workflow_basic_information")%>";

// create a map of projects and the workflow manager ids and names in the project
var projects = new Array()
<%
Set keys = workflowManagers.keySet();

Iterator iter = keys.iterator();

while (iter.hasNext())
{
    String projId = (String)iter.next();
    List users = (List)workflowManagers.get(projId);
%>
    var userArray = new Array();
<%
    if (users != null)
    {
        for (int i = 0; i < users.size(); i++)
        {
            User user = (User)users.get(i);
            String wfmId = user.getUserId();
            String wfmName = user.getUserName();
%>
            userArray[<%=i%>] = "<%=wfmId%>,<%=wfmName%>";
<%
        }
    }
%>

    projects[<%=projId%>] = userArray;
<%
}
List wfManagers = null;
if (chosenProject != -1 && workflowManagers != null)
{
    wfManagers = (List)workflowManagers.get(String.valueOf(chosenProject));
}
%>



var names = new Array();

function checkForDuplicateName()
{
   <%=jsmsg%>
   return true;
}

function noenter() {
  return !(window.event && window.event.keyCode == 13); }

function submitForm(formAction)
{
    basicTemplateForm.formAction.value = formAction;
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           basicTemplateForm.action = "<%=cancelURL%>";
           basicTemplateForm.submit();
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

       if (confirmForm(basicTemplateForm))
       {
          // Prepare the leveragedLocales param
          var options_string = "";
          var options_string1 = "";
          var the_select = basicTemplateForm.leverageField;
          for (loop=0; loop < the_select.options.length; loop++)
          {
              if (the_select.options[loop].selected == true)
              {
                   options_string += the_select.options[loop].text + " ";
                   options_string1 += loop + ",";
              }
          }
          basicTemplateForm.leveragedLocales.value = options_string1;

          // Prepare the workflow managers param
          var wfmgr_options_string = "";
          the_select = basicTemplateForm.wfmField;
          for (wfmloop=0; wfmloop < the_select.options.length; wfmloop++)
          {
              if (the_select.options[wfmloop].selected == true)
              {
                   wfmgr_options_string += the_select.options[wfmloop].value + ",";
              }
          }
          basicTemplateForm.<%=WorkflowTemplateConstants.CHOSEN_WORKFLOW_MANAGERS%>.value = wfmgr_options_string;

           
          // Submit the form
          basicTemplateForm.submit();
       }
       else
       {
          return false;
       }
    }
}

function confirmForm(formSent) {
    var theName = ATrim(formSent.<%=nameField%>.value);
	theName = stripBlanks (theName);
	formSent.<%=nameField%>.value = theName;
	if (isEmptyString(formSent.<%=nameField%>.value)) {
		alert("<%= bundle.getString("jsmsg_wf_template_name") %>");
		formSent.<%=nameField%>.value = "";
		formSent.<%=nameField%>.focus();
		return false;
	}
     
	if (!isNotLongerThan(formSent.<%=descriptionField%>.value, 200)) {
		alert("<%= bundle.getString("jsmsg_description") %>");
		formSent.<%=descriptionField%>.focus();
		return false;
	}

    if (hasSpecialChars(formSent.<%=nameField%>.value))
    {
        alert("<%= labelName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    
    if (!isSelectionMade(formSent.<%=projectField%>)) {
		alert("<%= bundle.getString("jsmsg_wf_template_pm") %>");
		return false;
	}

	if (!isSelectionMade(formSent.<%=localePairField%>)) {
		alert("<%= bundle.getString("jsmsg_wf_template_locale_pair") %>");
		return false;
	}
    
    if (!isSelectionMade(formSent.<%=encodingField%>)) {
		alert("<%= bundle.getString("jsmsg_wf_template_encoding") %>");
		return false;
	}	

	if (formSent.<%=leverageField%>.selectedIndex < 0)
	{
	   alert("<%= bundle.getString("jsmsg_wf_template_leveraging") %>");
	   return false;
	}
	
	if (formSent.perplexityId && formSent.perplexityId.selectedIndex > 0)
	{
	   if (formSent.perplexityKey.selectedIndex < 0)
	   {
		   alert("<%= bundle.getString("jsmsg_wf_template_perplexity_key") %>");
		   return false;
	   }
	   
	   if (!isPositiveNumber(formSent.perplexitySourceThreshold.value)) 
	   {
		   alert("<%= bundle.getString("jsmsg_wf_template_perplexity_source") %>");
		   return false;
	   }
	   
	   if (!isPositiveNumber(formSent.perplexityTargetThreshold.value)) 
	   {
		   alert("<%= bundle.getString("jsmsg_wf_template_perplexity_target") %>");
		   return false;
	   }
	}

	return true;
}

function populateLeverageFromList(localePairComboBox)
{
   $("#perplexityIds").val(-1);
   $("#perplexityKey").val(-1); 

   if (!isSelectionMade(localePairComboBox)) {
	   $("#perplexityIds").attr("disabled",true); 
	   $("#perplexityKey").attr("disabled",true); 
	   $("#perplexitySource").attr("disabled",true); 
	   $("#perplexityTarget").attr("disabled",true); 

	   return false;
   }
   
   $("#perplexityIds").attr("disabled",false); 

   var targetLang = null;
   var options_string = "";

   // The target lang that was selected
   for (var i = 0; i < localePairComboBox.options.length; i++)
   {
      if (localePairComboBox.options[i].selected == true)
      {
         targetLang = localePairComboBox.options[i].id;
         break;
      }
   }

   var possibleLevLocales = new Array(<%=leverageObjsString%>);
   var possibleLevLocalesDisplay = new Array(<%=leverageDispString%>);

   // Populate the "Leverage From:" multi-select box
   basicTemplateForm.leverageField.length = 0; // Clear the multi-select box
   var count = 0;
   for (var i = 0; i < possibleLevLocales.length; i++)
   {
	  if ((possibleLevLocales[i] == 'nb_NO' || possibleLevLocales[i] == 'no_NO'
			  ||possibleLevLocales[i] == 'nn_NO')
			  && (targetLang == 'no' || targetLang == 'nb' ||
				  targetLang == 'nn'))
	  {
		  basicTemplateForm.leverageField.options[count] = 
	            new Option(possibleLevLocalesDisplay[i], possibleLevLocales[i]);
	         count++; 
	  }
	  else
	  {
	      if (possibleLevLocales[i].indexOf(targetLang) != -1)
	      {
	         basicTemplateForm.leverageField.options[count] = 
	            new Option(possibleLevLocalesDisplay[i], possibleLevLocales[i]);
	         count++;
	      }
	  }
   }
}

//
// Update the workflow manager drop down depending on the project chosen
//
function updateWFMS(projObj)
{
    var id = projObj.options[projObj.selectedIndex].value;
    wfmArray = projects[id];

    // Remove old options
    wfList = basicTemplateForm.<%=wfmField%>;
    for (var i = 0; i < wfList.length; i++)
    {
        wfList.options[i] = null;
    }

    if (wfmArray == null)
    {
        wfList.options[0] = new Option("                            ", "");
        return;
    }

    // Add new options
    for (var i = 0; i < wfmArray.length; i++)
    {
        var item = wfmArray[i];
        var idx  = item.indexOf(',');
        var wfmId = item.substring(0, idx);
        var wfmName = item.substring(idx+1);
        wfList.options[i] = new Option(wfmName, wfmId);
    }

}

function updatePerplexity(v){
	var n = $(v).val();
	var x = n == -1;
	
	$("#perplexityKey").attr("disabled",true); 
	$("#perplexitySource").attr("disabled",x); 
	$("#perplexityTarget").attr("disabled",x); 
	
	if (x) {
		$("#perplexityKey").val(-1);
	} else {
		$("#loading").show();
		
		var data = {
				id : n,
				lpId : $("#localePairField").val()
			};
			
			$.ajax({
				type : "POST",
				url : '<%=self.getPageURL()%>&action=getPerplexity',
				cache : false,
				dataType : 'text',
				data : data,
				success : function(data) {
					var z = eval("(" + data + ")");
					$("#perplexityKey").html('<option value="-1"><%=bundle.getString("lb_choose")%></option>');
					for (var i in z){
						 var zb = z[i];
						$("#perplexityKey").append('<option value="' + zb + '">' + zb + '</option>');
					}
					$("#perplexityKey").attr("disabled",false); 
					$("#loading").hide();
				},
				error : function(request, error, status) {
					$("#loading").hide();
					result = "error";
					alert(error);
				}
			});		
	}
}

$(function(){
	$("#perplexityIds").attr("disabled",$("#localePairField").val() == -1); 
});
</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
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
    <!-- left table -->
    <TABLE CELLSPACING="8" CELLPADDING="0" BORDER="0" CLASS="standardText">
		<form name="basicTemplateForm" action="<%=next2URL%>" method="post">
        <INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
		<TR>
            <TD><%=labelName%><SPAN CLASS="asterisk">*</SPAN>:<BR>
            <INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="60" NAME="<%=nameField%>" onkeypress="return noenter()" CLASS="standardText"
            <%  if (chosenName != null) { %> 
                VALUE="<%= chosenName %>"
            <%  }%>
            ></INPUT>
            </TD>
            </TR>
            <TR>
            			<TD><%=labelDescription%>:<BR>
            <input type = "hidden" name = "<%=workflowTypeField%>" value = "<%=WorkflowTemplateInfo.TYPE_TRANSLATION%>">
            <TEXTAREA NAME="<%=descriptionField%>" COLS="40" ROWS="3" CLASS="standardText"><% if (chosenDescription != null) %><%=chosenDescription%></TEXTAREA>
            </TD>
            </TR>
            <TR>
			<TD>
                <%=labelProject%><SPAN CLASS="asterisk">*</SPAN>:<BR>
                    <SELECT NAME="<%=projectField%>" onchange="updateWFMS(this)" <%=disabled%> CLASS="standardText">
                    <%
                    long ltmp = -1;
                    %>
                    <OPTION VALUE="-1"><%= choose %></OPTION>
                    <%
                    if (chosenProject != -1)
                    {                    
                        ltmp = chosenProject;                        
                    }
                    long projectId = -1;
                    String projectName = null;
                    if (projectInfos != null)
                    {
                        int pSize = projectInfos.size();                        
                        for (int i=0; i<pSize; i++)
                        {
                           String selected = "";
                           ProjectInfo p = (ProjectInfo)projectInfos.get(i);
                           projectName = p.getName();
                           projectId = p.getProjectId();
                           if (projectId == ltmp)
                           {
                               selected = "SELECTED";
                           }
                              
                           %>
                              <OPTION VALUE="<%= projectId%>" <%=selected%>><%= projectName %></OPTION>                                                   
                    <%  }
                    }%>
                    </SELECT>
                    <BR>
            <%
            String notify = (chosenNotifyFlag == null || chosenNotifyFlag.booleanValue()) ? "checked" : "";
            %>
            <input type="checkbox" name="<%=notificationField%>" value="true"  <%=notify%> > <%=labelNotify%>
            </TD>
            </TR>
            <TR>
			<TD>
            <INPUT TYPE="HIDDEN" NAME=<%=WorkflowTemplateConstants.CHOSEN_WORKFLOW_MANAGERS%> VALUE="">
                <%=labelWorkflowManager%>:<BR>
                    <SELECT NAME="<%=wfmField%>" CLASS="standardText" MULTIPLE>
                    <%
                    String wfmId = null;
                    String wfmName = null;
                    if (wfManagers != null)
                    {
                        SortUtil.sort(wfManagers, new UserComparator(UserComparator.USERNAME, Locale.ENGLISH));
                        int wfmSize = wfManagers.size();                        
                        for (int i=0; i<wfmSize; i++)
                        {
                           String selected = "";
                           User usr = (User)wfManagers.get(i);
                           wfmId = usr.getUserId();
                           wfmName = usr.getUserName();
                           if (chosenWfms != null)
                           {
                              for (int j = 0 ; j < chosenWfms.size() ; j++)
                              {
                                 if (wfmId.equals((String)chosenWfms.get(j)))
                                 {
                                    selected = "SELECTED";
                                    break;
                                 }
                              }
                           }
                           %>
                            <OPTION VALUE="<%= wfmId %>" <%=selected%>><%= wfmName %></OPTION>
                              
                    <%
                        }
                    }
                    else 
                    {%>
                    <!-- Put a place holder in the mulitple-select list box -->
                    <OPTION VALUE="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <%}%>
                  
                    </SELECT>
            </TD>
            </TR>
            
            <TR>
			<TD>
                <INPUT TYPE="HIDDEN" NAME="ForLeverage" value="false">
                <%=labelLocalePair%><SPAN CLASS="asterisk">*</SPAN>:<BR>
                    <SELECT id="localePairField" NAME="<%=localePairField%>" <%=disabled%> 
                    CLASS="standardText" ONCHANGE="populateLeverageFromList(this)" >

                    <%
                    long ltmp1 = -1;                    
                    %>
                    <OPTION VALUE="-1"><%= choose %></OPTION>
                    <%
                    if (chosenLocalePair != null)
                    {
                        try
                        {
                           ltmp1 = Long.parseLong(chosenLocalePair);
                        }
                        catch(Exception e){}
                    }
                    long lpId = -1;
                    String localePair = null;
                    if (localePairs != null)
                    {                    
                        int lpSize = localePairs.size();
                        String selected = null;
                        for (int j=0; j<lpSize; j++)
                        {
                           selected = "";
                           LocalePair lp = (LocalePair)localePairs.get(j);
                           lpId = lp.getId();
                           localePair = lp.getSource().getDisplayName(uiLocale)+ " -> " + 
                              lp.getTarget().getDisplayName(uiLocale);
                           if (lpId == ltmp1)
                           {
                              selected = "SELECTED";
                           }
                           
                           // Put the LangCode, ie. "es", "fr", of the target Locale
                           // in the ID attribute of the OPTION tag so we can use it
                           // in the javascript function populateLeverageFromList()
                           GlobalSightLocale targetLocale = (GlobalSightLocale)lp.getTarget();
                           String targetLangCode = targetLocale.getLanguageCode();
                           %>
                           <OPTION ID="<%=targetLangCode%>" VALUE="<%= lpId %>" <%=selected%>><%= localePair %></OPTION>
                           <%
                        }                        
                    }%>
                    </SELECT>
            </TD>
            </TR>
            <TR>
			<TD>
                <%=labelTargetEncoding%><SPAN CLASS="asterisk">*</SPAN>:<BR>
                    <SELECT NAME="<%=encodingField%>" CLASS="standardText">

                    <%
                    String lpchosen2 = "";
                    lpchosen2 = bundle.getString("lb_choose");
                    %>
                    <OPTION VALUE="-1"><%= choose %></OPTION>
                    <%
                    if (chosenTargetEncoding != null)
                    {                    
                        lpchosen2 = chosenTargetEncoding;
                    } 
                    if (encodings != null)
                    {
                        String selected = null;
                        CodeSet encoding = null;
                        int encodingSize = encodings.size();
                        for (int h=0; h<encodingSize; h++)
                        {                
                           selected = "";        
                           encoding = (CodeSet)encodings.get(h);
                           if  (encoding.getCodeSet().equals(lpchosen2))
                           {
                              selected = "SELECTED";                              
                           }
                           %>
                           <OPTION VALUE="<%= encoding %>" <%=selected%>><%= encoding %></OPTION>
                           <%                           
                        }                        
                    }%>
                    </SELECT>
            </TD>
            </TR>
            <TR>
            <TD>
            <INPUT TYPE="HIDDEN" NAME="leveragedLocales" VALUE="">
            <%=labelLeverage%><SPAN CLASS="asterisk">*</SPAN>:<BR>
            <SELECT NAME="<%=leverageField%>" CLASS="standardText" MULTIPLE>
            <% if (wfti != null) { %>
             <!-- They are editing an exsiting workflow -- so populate 
                  the "Leverage From:" multi-select box. -->
                    <%
                        //GlobalSightLocale leverageLocale = null;
                        String selected = null;
                        String leverage = null;
                        if(chosenLocalePair != null)
                        {
                          lpId = -1;
                          try
                          {
                              lpId = Long.parseLong(chosenLocalePair);
                          }
                          catch (NumberFormatException e) { }

                          LocalePair lp = (LocalePair)ServerProxy.getLocaleManager().getLocalePairById(lpId);

                          GlobalSightLocale trgLocale = (GlobalSightLocale)lp.getTarget();
                          String trgLanguage = trgLocale.getLanguageCode();
                           for (int h = 0; h < leverageObjs.size(); h++)
                           {
                              selected = "";
                              GlobalSightLocale l = (GlobalSightLocale) leverageObjs.elementAt(h);
                              if(chosenLeverages != null && chosenLeverages.size()>0)
                              {
	                              leverage= (String)leverageDisp.elementAt(h);
                                  for (Iterator it=chosenLeverages.iterator(); it.hasNext();)
                                  {
                                      GlobalSightLocale e = (GlobalSightLocale)it.next();
                                      if(l.equals(e))
                                      {
                                          selected = "SELECTED";        
                                          break;
                                      }
                                  }
                              }
                          	  if (l.getLanguageCode().equals(trgLanguage) || l.getLanguageCode().equals("nb") || l.getLanguageCode().equals("no") || l.getLanguageCode().equals("nn"))
	                          {
                              %>
	                             <OPTION VALUE="<%=l%>" <%=selected %> > <%=leverage %></OPTION>
                              <%
                              }
                           }
                        } // end of if chosenLocalePair != null
                    %>
                <% } 
                   else 
                   {%>
                   <!-- New workflow -- Put a place holder in the mulitple-select list box until they
                        choose a target locale -->
                   <OPTION VALUE="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                 <%}%>
                    </SELECT>
                </TD>
            </TR>
            <TR>
			<TD>
                <%=bundle.getString("lb_workflow_scorecard_show_type")%><SPAN CLASS="asterisk">*</SPAN>:<BR>
                <select id="scorecardShowType" name="scorecardShowType">
		        	<option value="-1" <% if(scorecardShowType == -1){%> selected <%}%>><%=bundle.getString("lb_workflow_scorecard_not_showing")%></option>
		        	<option value="0" <% if(scorecardShowType == 0){%> selected <%}%>><%=bundle.getString("lb_workflow_scorecard_optional")%></option>
		        	<option value="1" <% if(scorecardShowType == 1){%> selected <%}%>><%=bundle.getString("lb_workflow_scorecard_required")%></option>	
                    <option value="2" <% if(scorecardShowType == 2){%> selected <%}%>><%=bundle.getString("lb_workflow_dqf_scorecard_optional")%></option>
                    <option value="3" <% if(scorecardShowType == 3){%> selected <%}%>><%=bundle.getString("lb_workflow_dqf_scorecard_required")%></option>   
                    <option value="4" <% if(scorecardShowType == 4){%> selected <%}%>><%=bundle.getString("lb_workflow_dqf_optional")%></option>
                    <option value="5" <% if(scorecardShowType == 5){%> selected <%}%>><%=bundle.getString("lb_workflow_dqf_required")%></option>   
	        	</select>
            </TD>
            </TR>
            
            <%if (usePerplexity){ %>
            <TR>
			<TD>
			     <fieldset>
                   <legend><%=bundle.getString("lb_perplexity_scorer")%></legend>
                      <table class="standardText">
                          <tr>
                              <td><%=bundle.getString("lb_wf_remote_service")%>:</td>
                              <td>
                                 <select id="perplexityIds" name="perplexityId" onchange="updatePerplexity(this)" >
                                     <option value="-1"><%=bundle.getString("lb_choose")%></option>
                                     <%for (PerplexityService p : perplexitys) {
                                       if (wfti != null && wfti.getPerplexityService() != null && p.getId() == wfti.getPerplexityService().getId()){
	                                     %>
	                                     <option value="<%=p.getId()%>" selected="selected"><%=p.getName()%></option>
	                                     <%} else {%>
	                                     <option value="<%=p.getId()%>"><%=p.getName()%></option>
	                                     <%}
                                       }%>
                                 </select>
                              </td>
                          </tr>
                          <tr>
                              <td><%=bundle.getString("lb_key")%>:</td>
                              <td>
                                 <select id="perplexityKey" name="perplexityKey" disabled="disabled" style="float:left;">
                                     <option value="-1"><%=bundle.getString("lb_choose")%></option>
                                     <%if (wfti != null && wfti.getPerplexityKey() != null ) {%>
                                     <option value="<%=wfti.getPerplexityKey()%>" selected="selected"><%=wfti.getPerplexityKey()%></option>
                                     <%} %>
                                 </select>
                                 <img src="images/ajax-loader.gif" id="loading" style="float:left; width:17px; display: none;"></img>
                              </td>
                          </tr>
                          <tr>
                              <td><%=bundle.getString("lb_perplexity_source_threshold")%>: </td>
                              <td>
                                 <input type="text" name="perplexitySourceThreshold" disabled="disabled" style="width:100px;" id="perplexitySource" 
                                 <%if (wfti != null && wfti.getPerplexitySourceThreshold() > 0){ %> value="<%=wfti.getPerplexitySourceThreshold() %>" <%} %>>
                              </td>
                          </tr>
                          <tr>
                              <td><%=bundle.getString("lb_perplexity_target_threshold")%>: </td>
                              <td>
                                 <input type="text" name="perplexityTargetThreshold" disabled="disabled" style="width:100px;" id="perplexityTarget"
                                 <%if (wfti != null && wfti.getPerplexityTargetThreshold() > 0){ %> value="<%=wfti.getPerplexityTargetThreshold() %>" <%} %>>
                              </td>
                          </tr>
                      </table>
                  </fieldset>               
            </TD>
            </TR>
            <%} %>
            <TR>
            </TR>
            </TABLE>
            <!-- end left table -->
        </TD>
        <TD WIDTH="50">&nbsp;</TD>
        <TD VALIGN="TOP">          
        </form>
	</TD>
    </TR>
    </TD>
    </TR>


<TR>
    <TD COLSPAN="3">&nbsp;
    </TD>
</TR>
<TR>
    <TD CLASS="HREFBold" COLSPAN="2">
        <INPUT TYPE="BUTTON" NAME="<%=lbCancel %>" VALUE="<%=lbCancel %>" 
            ONCLICK="submitForm('cancel')">  
        <INPUT TYPE="BUTTON" NAME="<%=lbNext %>" VALUE="<%=lbNext %>" 
            ONCLICK="submitForm('next')"> 
    </TD>
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
