<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.config.UserParamNames,
         java.util.ArrayList,
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
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
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

</SCRIPT>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading"><%=title%></span>
    <p>    
<form name="notifyForm" method="post">
<input type="hidden" name="toField" >
<input type="hidden" name="<%=UserParamNames.NOTIFICATION_ENABLED%>">
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
        <select name="from" <%=disabled%> multiple class="standardText" size=15 style="width:250">
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
                		   <option value="<%=availableOption%>" ><%=bundle.getString(availableOption)%></option>
<%
                	   }
                   }		       
					else
					{
%>
                   		<option value="<%=availableOption%>" ><%=bundle.getString(availableOption)%></option>
<%
                   	}
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
            <select name="to" <%=disabled%> multiple class="standardText" size=15 style="width:250px">
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
        </td>
      </tr>
</table>
</form>
</BODY>
</HTML>
