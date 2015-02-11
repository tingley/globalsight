<%@page import="com.globalsight.everest.workflow.Activity"%>
<%@page import="com.globalsight.util.GlobalSightLocale"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.users.UserDefaultRole"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.users.UserDefaultActivity"%>
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

<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    
    String saveUrl = save.getPageURL() + "&action=save";
    String cancelUrl = cancel.getPageURL() + "&action=cancel";
    String selfUrl = self.getPageURL();

    String title= bundle.getString("lb_default_role");

    // Labels of the column titles
    String sourceCol = bundle.getString("lb_source_locale");
    String targetCol = bundle.getString("lb_target_locale");

    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String saveButton = bundle.getString("lb_done");

    String lbUserName = bundle.getString("lb_user_name");
    
    ModifyUserWrapper wrapper = (ModifyUserWrapper)sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);
    String userName = wrapper.getUserName();

    String editMark = (String)sessionMgr.getAttribute("isEdit");
    boolean isEdit = false;
    if (editMark != null && editMark.equals("1")) {
        isEdit = true;
        title += " - " + bundle.getString("lb_edit");
    } else 
    	title += " - " + bundle.getString("lb_new");
    
    //init data
    Vector sourceLocales = (Vector)sessionMgr.getAttribute("sourceLocales");
    Vector targetLocales = new Vector();
    long sourceLocaleId = 0l, targetLocaleId = 0l;
    String sourceLocale = "", targetLocale = "";
    ArrayList<String> activities = new ArrayList<String>();
    UserDefaultRole curRole = new UserDefaultRole();
    ArrayList<String> selectedActivities = new ArrayList<String>();
    
    if (isEdit) {
        curRole = (UserDefaultRole)sessionMgr.getAttribute("defaultRole");
        sourceLocaleId = curRole.getSourceLocaleId();
        targetLocaleId = curRole.getTargetLocaleId();
        sourceLocale = curRole.getSourceLocaleObject().getDisplayName();
        targetLocale = curRole.getTargetLocaleObject().getDisplayName(); 
        selectedActivities = (ArrayList<String>)sessionMgr.getAttribute("curActivities");
    } else {
        sourceLocale = (String)sessionMgr.getAttribute("sourceLocale");
        if (sourceLocale == null)
            sourceLocale = "0"; 
        sourceLocaleId = Long.parseLong(sourceLocale);
    }
    targetLocales = (Vector)sessionMgr.getAttribute("targetLocales") == null ? new Vector() : (Vector)sessionMgr.getAttribute("targetLocales");
    activities = (ArrayList<String>)sessionMgr.getAttribute("activities") == null ? new ArrayList() : (ArrayList<String>)sessionMgr.getAttribute("activities");
    
%>
<HTML>
<!-- This JSP is envoy/administratin/users/modify2.jsp-->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = true;
    var objectName = "<%=bundle.getString("lb_user")%>";
    var guideNode = "users";
    var helpFile = "<%=bundle.getString("help_users_defaultroles_new")%>";

function submitForm(selectedButton)
{
    if (selectedButton == 'Cancel')
    {
        RolesForm.action = "<%=cancelUrl %>";
        RolesForm.submit();
        return;
    } else if (selectedButton == "Save") {
        <%
        if (!isEdit) {
        %>
        if (RolesForm.sourceLocale.selectedIndex == 0) {
            alert("<%=bundle.getString("jsmsg_customer_src_locale")%>");
            return false;
        }
        var canRun = false;
        for (var i=0;i<RolesForm.targetLocale.options.length;i++) {
            if (RolesForm.targetLocale.options[i].selected) {
                canRun = true;
                break;
            }
        }
        if (!canRun) {
            alert("<%=bundle.getString("jsmsg_customer_targ_locale")%>");
            return;
        }
        <%
        }
        %>
        canRun = false;
        var name = "";
        for (var i=0;i<document.RolesForm.elements.length;i++) {
            name = document.RolesForm.elements[i].name;
            //alert("name == " + name);
            if (name.indexOf("activity") == 0 && name != "activityCount") {
               if (document.getElementById(name).checked)
                  canRun = true;
            }
        }
        if (!canRun) {
            alert("<%=bundle.getString("jsmsg_users_activities") %>");
            return;
        } else {
            document.RolesForm.action = "<%=saveUrl%>";
            document.RolesForm.submit();
        }
    }
}

function setTarget() {
    if (RolesForm.sourceLocale.selectedIndex == 0) {
        alert("Please select a source locale first");
        return;
    }
    RolesForm.action = "<%=selfUrl%>&action=setTarget";
    RolesForm.submit();
}
function setActivity() {
    if (RolesForm.sourceLocale.selectedIndex == 0) {
        alert("Please select a source locale first");
        return;
    }
    var canRun = false;
    for (var i=0;i<RolesForm.targetLocale.options.length;i++) {
        if (RolesForm.targetLocale.options[i].selected)
            canRun = true;
    }    
    if (!canRun) {
        alert("Please select at least one target locale");
        return;
    }
    RolesForm.action = "<%=selfUrl%>&action=setActivity";
    RolesForm.submit();
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
    ONLOAD="loadGuides()"><%@ include file="/envoy/common/header.jspIncl" %>
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
            <div class='standardText' nowrap><b><%=lbUserName%></b>:&nbsp;<%=userName%></div>
  <table border="0" cellspacing="0" cellpadding="5">
    <tr>
      <td class='standardText'><%=sourceCol %>:</td>
      <td class='standardText'>
      <%
      GlobalSightLocale locale = null;
      if (!isEdit) {
       %>
        <select name="sourceLocale" onchange="setTarget();">
          <option value="-1"><%=bundle.getString("lb_choose") %>...</option>
          <%
          for (int i=0;i<sourceLocales.size();i++) {
        	  locale = (GlobalSightLocale)sourceLocales.get(i);
        	  if (sourceLocaleId == locale.getId())
        	       out.println("<option value='" + locale.getId() + "' selected>" + locale.getDisplayName() + "</option>");
        	  else
        	       out.println("<option value='" + locale.getId() + "'>" + locale.getDisplayName() + "</option>");
          }
          %>
        </select>
      <%
      } else {
      %>
      <b><%=sourceLocale %></b>
      <%
      }
       %>
      </td>
      </tr>
      <tr>
      <td class='standardText' valign="top"><%=targetCol %>:</td>
      <td class='standardText'>
      <%
      if (!isEdit) {
       %>
        <select name="targetLocale" multiple="multiple" style="width:280px;height:100px;">
          <%
          for (int i=0;i<targetLocales.size();i++) {
        	  locale = (GlobalSightLocale)targetLocales.get(i);
      	      out.println("<option value='" + locale.getId() + "'>" + locale.getDisplayName() + "</option>");
          }
          %>
        </select>
        <%
        } else {
         %>
         <b><%=targetLocale %></b>
         <%
        }
        %>
      </td>
    </tr>
    <tr>
      <td class='standardText' style="vertical-align:top;padding-top:13px;"><%=bundle.getString("lb_activity_types") %><input type="checkbox" id="checkAll" onclick="check_all()"/>:</td>
      <td class='standardText'>
        <table border="0">
          <tr>
          <%
          String activityName = "";
          String selected = "";
          int size = activities.size();
          for (int i = 0; i < size; i++) {
            activityName = activities.get(i);
            if (selectedActivities.contains(activityName))
              selected = "checked";
            else
              selected = "";
            if (i % 4 == 0) {
                out.println("</tr><tr>");
            }
        	  %>
        	    <td class='standardText'>
        	      <input type="checkbox" id="activity_<%=i %>" name="activity_<%=i %>" value="<%=activityName %>" <%=selected %>><%=activityName %>
        	    </td>
        	  <%
          }
          %>
          </tr>
          <input type="hidden" name="activityCount" value="<%=size %>"/>
        </table>
      </td>
    </tr>
  </table>
<!-- End Data Table -->
</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD>
    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
    <INPUT TYPE="BUTTON" VALUE="<%=saveButton%>" onClick="submitForm('Save');">
    </FORM>
</TD>
</TR>
</TABLE>

</BODY>
</HTML>
