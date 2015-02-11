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
    SessionManager sessionMgr = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
 
    String confirmRemove = bundle.getString("msg_confirm_activity_removal");
    String newURL = new1.getPageURL() + "&action=" + ActivityConstants.CREATE;
    String modifyURL = modify.getPageURL() + "&action=" + ActivityConstants.EDIT;
    String removeURL = remove.getPageURL() + "&action=" + ActivityConstants.REMOVE;
    String selfURL = self.getPageURL();
    String title = bundle.getString("lb_activity_types");
    String helperText= bundle.getString("helper_text_activity_types");

    String deps = (String)sessionMgr.getAttribute(ActivityConstants.DEPENDENCIES);
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
 
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

    String actNameFilterValue = (String) sessionMgr.getAttribute(ActivityConstants.FILTER_ACTIVITY_NAME);
    if (actNameFilterValue == null || actNameFilterValue.trim().length() == 0) {
        actNameFilterValue = "";
    }
    String actCompanyNameFilterValue = (String) sessionMgr.getAttribute(ActivityConstants.FILTER_COMPANY_NAME);
    if (actCompanyNameFilterValue == null || actCompanyNameFilterValue.trim().length() == 0) {
        actCompanyNameFilterValue = "";
    }
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "activityType";
var helpFile = "<%=bundle.getString("help_activity_types_main_screen")%>";

function newActivity()
{
    activityForm.action = "<%=newURL%>";
    activityForm.submit();
}

function modifyActivity(activityName)
{
    activityForm.action = "<%=modifyURL%>" + "&name=" + activityName;
    activityForm.submit();
}

function removeActivity()
{
    var value = getSelectedActivity();
    if (!confirm('<%=confirmRemove%>'))
    {
        return false;
    }
    activityForm.action = "<%=removeURL%>" + "&name=" + value;
    activityForm.submit();
}

// Find selected activities
// Actually there is only one in current implementation.
function getSelectedActivity()
{
    var selectedActNames = "";
    $("input[name='checkboxBtn']:checked").each(function ()
    {
        selectedActNames += $(this).val() + ",";
    });
    if (selectedActNames != "")
        selectedActNames = selectedActNames.substring(0, selectedActNames.length - 1);

    return selectedActNames;
}

function buttonManagement()
{
    var count = $("input[name='checkboxBtn']:checked").length;
    if (count == 1)
    {
        $("#removeBtn").attr("disabled", false);
    }
    else
    {
        $("#removeBtn").attr("disabled", true);
    }
}

function handleSelectAll() {
    var ch = $("#selectAll").attr("checked");
    if (ch == "checked")
    {
        $("[name='checkboxBtn']").attr("checked", true);
    }
    else
    {
        $("[name='checkboxBtn']").attr("checked", false);
    }

    buttonManagement();
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
        activityForm.action = "<%=selfURL%>";
        activityForm.submit();
    }
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
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

<form name="activityForm" id="activityForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="min-width:1024px;" >
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="activities" key="<%=ActivityConstants.ACTIVITY_KEY%>" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="activities" id="activity"
       key="<%=ActivityConstants.ACTIVITY_KEY%>"
       dataClass="com.globalsight.everest.workflow.Activity" pageUrl="self"
       hasFilter="true" emptyTableMsg="msg_no_activity_types" >
      <amb:column label="checkbox" width="2%">
        <input type="checkbox" name="checkboxBtn" id="checkboxBtn" value="<%=activity.getName()%>" onclick="buttonManagement()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=ActivityComparator.NAME%>" width="15%" filter="<%=ActivityConstants.FILTER_ACTIVITY_NAME%>" filterValue="<%=actNameFilterValue%>">
      <%  if (userPermissions.getPermissionFor(Permission.ACTIVITY_TYPES_EDIT)) { %>
          <a href="javascript:void(0);" onclick="modifyActivity('<%=activity.getName()%>');"><%=activity.getDisplayName()%></a>
      <%  } else { %>
          <%=activity.getDisplayName()%>
      <%
          }
      %>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=ActivityComparator.DESC%>" width="40%">
      <% out.print(activity.getDescription() == null ?
       "" : activity.getDescription()); %>
      </amb:column>

      <amb:column label="lb_review_editable" width="15%">
      <%
      if (activity.isType(Activity.TYPE_REVIEW) && activity.getIsEditable())
       out.println("<img src=/globalsight/images/checkmark.gif height=9 width=13 hspace=10 vspace=3>");
      %>
      </amb:column>
      <amb:column label="lb_review_notEditable" width="15%">
      <%
      if (activity.isType(Activity.TYPE_REVIEW) && !activity.getIsEditable())
       out.println("<img src=/globalsight/images/checkmark.gif height=9 width=13 hspace=10 vspace=3>");
      %>
      </amb:column>
      <amb:column label="lb_use_type" sortBy="<%=ActivityComparator.USE_TYPE%>" width="">
      <%String useType = bundle.getString(activity.getUseType());
      out.print(useType);
      %>
      </amb:column>
      <% if (isSuperAdmin) { %>
      <amb:column label="lb_company_name" sortBy="<%=ActivityComparator.ASC_COMPANY%>" width="140" 
          filter="<%=ActivityConstants.FILTER_COMPANY_NAME%>" filterValue="<%=actCompanyNameFilterValue%>" >
          <%=CompanyWrapper.getCompanyNameById(activity.getCompanyId())%>
      </amb:column>
      <% } %>
      </amb:table>
    </td>
  </tr>
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="activities" key="<%=ActivityConstants.ACTIVITY_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="left">
    <amb:permission name="<%=Permission.ACTIVITY_TYPES_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>" name="removeBtn" id="removeBtn" disabled onclick="removeActivity();">
    </amb:permission>
    <amb:permission name="<%=Permission.ACTIVITY_TYPES_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." name="newBtn" id="newBtn" onclick="newActivity();">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
</BODY>
</HTML>

