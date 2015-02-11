<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
      com.globalsight.everest.workflow.Activity,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.pagehandler.administration.activity.ActivityConstants, 
      com.globalsight.everest.util.comparator.ActivityComparator, 
      com.globalsight.everest.company.CompanyWrapper,
      java.util.ArrayList,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="activities" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
 
    String confirmRemove = bundle.getString("msg_confirm_activity_removal");
    String newURL = new1.getPageURL() + "&action=" + ActivityConstants.CREATE;
    String modifyURL = modify.getPageURL() + "&action=" + ActivityConstants.EDIT;
    String removeURL = remove.getPageURL() + "&action=" + ActivityConstants.REMOVE;
    String title = bundle.getString("lb_activity_types");
    String helperText= bundle.getString("helper_text_activity_types");

    String deps = (String)sessionMgr.getAttribute(ActivityConstants.DEPENDENCIES);
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "activityType";
var helpFile = "<%=bundle.getString("help_activity_types_main_screen")%>";

function submitForm(button)
{
    if (button == "New")
    {
        activityForm.action = "<%=newURL%>";
    }
    else 
    {
        value = getRadioValue(activityForm.radioBtn);
        if (button == "Edit")
        {
            activityForm.action = "<%=modifyURL%>" + "&name=" + value;
        }
        else if (button == "Remove")
        {
            if (!confirm('<%=confirmRemove%>')) return false;
            activityForm.action = "<%=removeURL%>" + "&name=" + value;
        }
    }
    activityForm.submit();
    return;
}

function enableButtons()
{
    if (activityForm.removeBtn)
        activityForm.removeBtn.disabled = false;
    if (activityForm.editBtn)
        activityForm.editBtn.disabled = false;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<% if (deps != null) {
    sessionMgr.removeElement(ActivityConstants.DEPENDENCIES);
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>

<form name="activityForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="activities" key="<%=ActivityConstants.ACTIVITY_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="activities" id="activity"
       key="<%=ActivityConstants.ACTIVITY_KEY%>"
       dataClass="com.globalsight.everest.workflow.Activity" pageUrl="self"
       emptyTableMsg="msg_no_activity_types" >
      <amb:column label="">
      <input type="radio" name="radioBtn" value="<%=activity.getName()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=ActivityComparator.NAME%>"
       width="150px">
      <%= activity.getDisplayName() %>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=ActivityComparator.DESC%>"
       width="250px">
      <% out.print(activity.getDescription() == null ?
       "" : activity.getDescription()); %>
      </amb:column>

      <amb:column label="lb_review_editable" width="100px">
      <%
      if (activity.isType(Activity.TYPE_REVIEW) && activity.getIsEditable())
       out.println("<img src=/globalsight/images/checkmark.gif height=9 width=13 hspace=10 vspace=3>");
      %>
      </amb:column>
      <amb:column label="lb_review_notEditable" width="120px">
      <%
      if (activity.isType(Activity.TYPE_REVIEW) && !activity.getIsEditable())
       out.println("<img src=/globalsight/images/checkmark.gif height=9 width=13 hspace=10 vspace=3>");
      %>
      </amb:column>
      <amb:column label="lb_use_type" sortBy="<%=ActivityComparator.USE_TYPE%>" width="80px">
      <%String useType = bundle.getString(activity.getUseType());
      out.print(useType);
      %>
      </amb:column>
      <% if (isSuperAdmin) { %>
      <amb:column label="lb_company_name" sortBy="<%=ActivityComparator.ASC_COMPANY%>" width="120">
          <%=CompanyWrapper.getCompanyNameById(activity.getCompanyId())%>
      </amb:column>
      <% } %>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.ACTIVITY_TYPES_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
       name="removeBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.ACTIVITY_TYPES_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.ACTIVITY_TYPES_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
</BODY>
</HTML>

