<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.config.UserParamNames,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.util.mail.MailerLocal,
         com.globalsight.everest.localemgr.LocaleManagerLocal,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.everest.permission.PermissionSet,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         java.util.ArrayList,
         java.util.Locale,
         com.globalsight.util.mail.MailerConstants,
         java.util.List,
         java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String doneUrl = done.getPageURL()+"&action=notificationOptions";
    String cancelUrl = cancel.getPageURL()+"&action=cancel";
    
    String title= bundle.getString("lb_email_notification") + " " +
         bundle.getString("lb_options");
    
    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String doneButton = bundle.getString("lb_done");

    // Data for the page
    List availableOptions = (List)sessionMgr.getAttribute(
            WebAppConstants.AVAILABLE_NOTIFICATION_OPTIONS);     
    List addedOptions = (List)sessionMgr.getAttribute(
            PageHandler.ADDED_NOTIFICATION_OPTIONS);
    String emailNotification = (String)request.getAttribute(UserParamNames.NOTIFICATION_ENABLED);
    String emailNotificationChecked = emailNotification.equals("0") ? "" : "CHECKED";
    String disabled = emailNotification.equals("0") ? "DISABLED" : ""; 
    PermissionSet perms = (PermissionSet)session.getAttribute(WebAppConstants.PERMISSIONS);
    boolean b_editEmailTemp=true;
    if(!perms.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_EDIT_EMAIL_TEMPLATE)){
        b_editEmailTemp=false;
    }

%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<script type="text/javascript"src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "myAccount";
    var helpFile = "<%=bundle.getString("help_my_notification_options")%>";

function submitForm(formAction)
{
    saveSelectedOptions();
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           notifyForm.action = "<%=cancelUrl%>";
           notifyForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }    
    else if (formAction == "saveOptions")
    {
        form = document.notifyForm;
        var isChecked = form.emailNotification.checked;
        form.<%=UserParamNames.NOTIFICATION_ENABLED%>.value = isChecked ? "1" : "0";
        // enable the hidden field right before submit (so the values can be read)
        form.toField.disabled = false;
        notifyForm.action = "<%=doneUrl%>";
    }
    
    notifyForm.submit();
}

//
// Return true if option is already selected
//
function optionInList(id)
{
    var to = notifyForm.to;
    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].value == id)
        {
            return true;
        }
    }
    return false;
}

var first = true;
function addOption()
{
    var from = notifyForm.from;
    var to = notifyForm.to;
    if (from.selectedIndex == -1)
    {
        // put up error message
        alert("<%= bundle.getString("jsmsg_select_notification_option") %>");
        return;
    }
    for (var i = 0; i < from.length; i++)
    {       
        if (from.options[i].selected)
        {
            if (optionInList(from.options[i].value))
            {
                continue;
            }
            if (first == true)
            {
<%
                if (addedOptions == null || addedOptions.size() == 0)
                {
%>
                    to.options[0] = null;
<%
                }
%>
                first = false;
            }
            var len = to.options.length;
            to.options[len] = new Option(from.options[i].text, from.options[i].value);
            from.options[i]=null;
            i--;
        }
    }
    saveSelectedOptions();
}

function removeOption()
{
    var to = notifyForm.to;
    var from = notifyForm.from;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_notification_option") %>");
        return;
    }
    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].selected)
        {
            var len = from.options.length;
            from.options[len] =  new Option(to.options[i].text, to.options[i].value);
            
            to.options[i] = null;
            i--;
        }
    }
    saveSelectedOptions();
}

function saveSelectedOptions()
{
    if (!notifyForm.to) return;

    var to = notifyForm.to;
    var options_string = "";
    var first = true;
    // Save options in a comma separated string
    for (loop=0; loop < to.options.length; loop++)
    {
        if (first)
        {
            first = false;
        }
        else
        {
            options_string += ",";
        }
        options_string += to.options[loop].value;
    }    
    notifyForm.toField.value = options_string;
}

function checkItems(notificationCheckbox)
{
   form = document.notifyForm;
   var isChecked = notificationCheckbox.checked;
   
   for (var i = 0; i < form.elements.length; i++)
   {
      // If it's not a checkbox, or cancel/done button disable it.
      if (form.elements[i].type != "checkbox" &&
          (form.elements[i].name != "<%=cancelButton %>" && 
          form.elements[i].name != "<%=doneButton %>"))
      {      
         if (isChecked)
         {
            form.elements[i].disabled = false;
         }
         else
         {
            form.elements[i].disabled = true;
         }
      }
   }
}

$(document).ready(function() {
	
	$("#edit").click(function(){
	    $.ajax({
	    	type:'get',
	    	url:"/globalsight/ControlServlet?linkName=notification&pageName=MYACCT&&action=edit",
	        data:{
	        	'selectFromValue':document.getElementById("from").value,
	        	'selectToValue':document.getElementById("to").value,
	        },
	        dataType:"json",
	        success:function(data)
	        {
	        	$("#subjectKey").val(data.subjectKey);
	        	$("#messageKey").val(data.messageKey);
	        	$("#subjectText").val(data.subjectText);
	        	$("#messageText").val(data.messageText);
	        	document.getElementById("editEmailTable").style.display="block";
	        }
	    });
	})

	$("#save").click(function(){
		$.ajax({
		    type:'post',
			url:"/globalsight/ControlServlet?linkName=notification&pageName=MYACCT&&action=save",
			data:{
				  'subjectText':$("#subjectText").val(),
	              'messageText':$("#messageText").val(),
		          'subjectKey':$("#subjectKey").val(),
		          'messageKey':$("#messageKey").val(),
	              },              
			dataType: "text", 
			success:function(data){
				alert(data);
			},
			});	
	})
	
 	$("#from").click(function(){
 		var countTo=$("#to option").length;
 		var countFrom=$("#from option").length;
 		var i=0;
 		
 		for(var j=0;j<countTo;j++)
 		{
 			$("#to").get(0).options[j].selected = false;
 		}
 		
 		for(var j=0;j<countFrom;j++) 
 		{  
 		   if($("#from").get(0).options[j].selected)
 		   {
 			   i++;
 		   }
 		}
 		
 		if(i>1 || i==0)
 		{
 			document.getElementById("edit").disabled=true;
 		}
 		else
 		{
 			document.getElementById("edit").disabled=false;
 		}
 		
	}) 
	
 	$("#to").click(function(){
 		var countTo=$("#to option").length;
 		var countFrom=$("#from option").length;
 		var i=0;
 		
 		for(var j=0;j<countFrom;j++)
 		{
 			$("#from").get(0).options[j].selected = false;
 		}
 		
 		for(var j=0;j<countTo;j++) 
 		{  
 		   if($("#to").get(0).options[j].selected)
 		   {
 			   i++;
 		   }
 		}
 		
 		if(i>1 || i==0)
 		{
 			document.getElementById("edit").disabled=true;
 		}
 		else
 		{
 			document.getElementById("edit").disabled=false;
 		}
	}) 
	})

</SCRIPT>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides();">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading"><%=title%></span>
    <p>    
<form name="notifyForm" id="notifyForm" method="post">
<input type="hidden" name="toField" >
<input type="hidden" name="<%=UserParamNames.NOTIFICATION_ENABLED%>">
<input type="hidden" id="subjectKey">
<input type="hidden" id="messageKey">
<table border="0" bordercolor="green" cellpadding="0" cellspacing="0" >      
  <TR>
    <TD>
    <INPUT type="checkbox" name="emailNotification" <%=emailNotificationChecked%> ONCLICK="checkItems(this);">
      <SPAN class="standardText"><%=bundle.getString("lb_enable_email_notification")%></SPAN>
    </TD>    
  </TR>    
  <tr>
    <td></td>
    <td>&nbsp;</td>
    <td></td>
  </tr>
  <tr>
    <td class="standardText">
        <%=bundle.getString("lb_available_notifications")%>:
    </td>
    <td>&nbsp;</td>
    <td class="standardText">
        <%=bundle.getString("lb_send_me_emails")%>:
    </td>
  </tr>
    <tr>
        <td width="20%">
        <select name="from"  id="from" <%=disabled%> class="standardText" multiple size=15 style="width:250">
<%
            if (availableOptions != null)
            {
                for (int i = 0; i < availableOptions.size(); i++)
                {
                   String availableOption = (String)availableOptions.get(i);
                   if(addedOptions != null)
                   {
                	   boolean checkTag = false; //check if theelement exist in the "to" form
                	   for(int j=0; j<addedOptions.size(); j++)
                	   {
                		   String addedOp = (String)addedOptions.get(j);
                		   if(addedOp.equals(availableOption))
                		   {
                			   checkTag = true;
                		   }
                	   }
                	        if(!checkTag)
                	        {   
%>                           
                		       <option value="<%=availableOption%>"><%=bundle.getString(availableOption)%></option>
<%               	         }
               	    }
					else
					{
%>                       
               		   <option value="<%=availableOption%>" ><%=bundle.getString(availableOption)%></option>
<%               	}
                   }		       
                }
%>
        </select>
        </td>
        <td align="center">
          <table>
            <tr>
              <td>
                <input type="button" name="addButton" <%=disabled%> value=" >> "
                    onclick="addOption()"><br>
              </td>
            </tr>
            <tr><td>&nbsp;</td></tr>
            <tr>
                <td>
                <input type="button" name="removedButton" <%=disabled%> value=" << "
                    onclick="removeOption()">
              </td>
            </tr>
          </table>
        </td>
        <td>
            <select name="to" id="to" <%=disabled%> class="standardText" multiple size=15 style="width:250px">
<%
                if (addedOptions != null && addedOptions.size() != 0)
                {
                    for (int i = 0; i < addedOptions.size(); i++)
                    {
                       String addedOption = (String)addedOptions.get(i);

%>
                       <option value="<%=addedOption%>" ><%=bundle.getString(addedOption)%></option>
<%
                    }
                }
                else
                {
%>
                   <option>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
<%
                }
%>
            </select>
          </td>
      </tr>
      <tr>
        <td colspan="3" style="padding-top:10px">
          <input type="button" name="<%=cancelButton %>" value="<%=cancelButton%>"
            onclick="submitForm('cancel')">    
          <input type="button" name="<%=doneButton %>" value="<%=doneButton %>"
            onclick="submitForm('saveOptions')">
            <%if(b_editEmailTemp){%>
          <input type="button" id="edit" disabled value="Edit">
          <%}%>
        </td>
      </tr>
</table>
<table id="editEmailTable" style="display:none">
<tr><td>&nbsp;&nbsp;</td></tr>
      <tr><td class="standardText">Subject</td><tr>
      <tr><td><textarea rows="1" cols="80" id="subjectText"></textarea><td></tr>
      <tr><td>&nbsp;&nbsp;</td></tr>
      <tr><td class="standardText">Message</td></tr>
      <tr><td><textarea rows="10" cols="80" id="messageText"></textarea></td></tr>
      <tr><td style="padding-top:10px" colspan="3"><input type="button" value="Save" id="save"><td></tr>
</table>
</form>
</BODY>
</HTML>
