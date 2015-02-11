<%@page import="com.globalsight.everest.webapp.pagehandler.administration.users.UserDefaultRole"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.util.comparator.UserRoleComparator,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.securitymgr.UserSecureFields,
         com.globalsight.everest.usermgr.UserRoleInfo,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.ModifyUserWrapper,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         java.text.MessageFormat,java.util.ResourceBundle,
         com.globalsight.everest.company.*,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="add" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    
    String addUrl = add.getPageURL();
    String modifyUrl = modify.getPageURL() + "&action=edit";
    String removeUrl = remove.getPageURL() + "&action=remove";
    String cancelUrl = cancel.getPageURL() + "&action=cancelDefaultRole";
    String doneUrl = done.getPageURL() + "&action=doneDefaultRole";

    String title= bundle.getString("lb_default_role");

    // Labels of the column titles
    String sourceCol = bundle.getString("lb_source_locale");
    String targetCol = bundle.getString("lb_target_locale");

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String cancelButton = bundle.getString("lb_cancel");
    String removeButton = bundle.getString("lb_remove");
    String doneButton = bundle.getString("lb_done");

    String lbUserName = bundle.getString("lb_user_name");
    
    ModifyUserWrapper wrapper = (ModifyUserWrapper)sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);
    String userName = wrapper.getUserName();

    ArrayList<UserDefaultRole> defaultRoles = (ArrayList)sessionMgr.getAttribute("defaultRoles");
%>
<HTML>
<!-- This JSP is envoy/administratin/users/modify2.jsp-->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = true;
    var objectName = "<%=bundle.getString("lb_user")%>";
    var guideNode = "users";
    var helpFile = "<%=bundle.getString("help_users_defaultroles_list")%>";

function submitForm(selectedButton)
{
    if (selectedButton == 'Cancel')
    {
        RolesForm.action = "<%=cancelUrl %>";
        RolesForm.submit();
        return;
    }
    if (selectedButton == "Done") {
        RolesForm.action = "<%=doneUrl%>";
        RolesForm.submit();
        return;
    }
    var checked = false;
    var selectedRadioBtn = null;
    if (RolesForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (RolesForm.radioBtn.length)
        {
            for (i = 0; !checked && i < RolesForm.radioBtn.length; i++)
            {
                if (RolesForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selectedRadioBtn = RolesForm.radioBtn[i].value;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (RolesForm.radioBtn.checked == true)
            {
                checked = true;
                selectedRadioBtn = RolesForm.radioBtn.value;
            }
        }
    }
    // otherwise do the following
    if (selectedButton == 'New')
    {
        RolesForm.action = "<%=addUrl %>";
        RolesForm.submit();
        return;
    } else if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_role") %>");
        return false;
    } else {
        if (selectedButton == "Edit") {
            RolesForm.action = "<%=modifyUrl%>&id=" + selectedRadioBtn;
        } else if (selectedButton == "Remove") {
        	RolesForm.action = "<%=removeUrl%>&id=" + selectedRadioBtn;
        }
    }

    RolesForm.submit();
}

function setButton() {
	RolesForm.removeBtn.disabled = false;
	RolesForm.editBtn.disabled = false;
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
    <SPAN CLASS="mainHeading">
    <%=title%>
    </SPAN>
    <p></p>
    
    <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
        <TR VALIGN="TOP">
            <TD ALIGN="left">
            <form name="RolesForm" method="post">
<input type="hidden" name="sourceLocale">
<input type="hidden" name="targetLocale">
<div class='standardText'nowrap><b><%=lbUserName%></b>:&nbsp;<%=userName%></div>
<!-- UserRoleInfo data table -->
  <table border="0" cellspacing="0" cellpadding="5" class="list">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td>&nbsp;</td>
      <td style="padding-right: 20px;">
        <%=sourceCol%>
      </td>
      <td style="padding-left: 20px;" >
        <%=targetCol%>
      </td>
    </tr>
<%
		int rolesSize = defaultRoles.size();
        if (rolesSize == 0)
        {
%>
        <tr>
          <td colspan=3 class='standardText'><%=bundle.getString("msg_no_roles")%></td>
        </tr>
<%
        }
        else
        {
        	  UserDefaultRole role = null;
        	  String key = "";
              for (int i=0; i < rolesSize; i++)
              {
                String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                role = defaultRoles.get(i);
                key = role.getSourceLocaleId() + "_" + role.getTargetLocaleId();
%>
                <tr style="padding-bottom:5px; padding-top:5px;"
                  valign=top bgcolor="<%=color%>">
                  <td>
                    <input type="radio" name="radioBtn" value="<%=key%>" onclick="setButton();">
                  </td>
                  <td class="standardText">
                    <%=role.getSourceLocaleObject().getDisplayName()%>
                  </td>
                  <td style="padding-left: 20px;" class="standardText" >
                    <%=role.getTargetLocaleObject().getDisplayName()%>
                  </td>
                </tr>
<%
              }
        }
%>
  </tbody>
  </table>
<!-- End Data Table -->
</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD>
<DIV ID="DownloadButtonLayer" ALIGN="RIGHT" STYLE="visibility: visible">
    <P>

    <INPUT TYPE="BUTTON" name="cancelBtn" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
    <INPUT TYPE="BUTTON" name="editBtn" VALUE="<%=editButton%>..." onClick="submitForm('Edit');" disabled>
    <INPUT TYPE="BUTTON" name="removeBtn" VALUE="<%=removeButton%>" onClick="submitForm('Remove');" disabled>
    <INPUT TYPE="BUTTON" name="newBtn" VALUE="<%=newButton%>..." onClick="submitForm('New');">
    <INPUT TYPE="BUTTON" name="doneBtn" VALUE="<%=doneButton%>" onClick="submitForm('Done');">

</DIV>
</FORM>
</TD>
</TR>
</TABLE>

</BODY>
</HTML>
