<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.usermgr.UserInfo,
         com.globalsight.everest.util.comparator.UserComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserStateConstants,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.projecthandler.Project,
         com.globalsight.everest.projecthandler.ProjectInfo,
         java.text.MessageFormat,
         java.util.ArrayList,
         java.util.List,
         java.util.Iterator,
         java.util.Locale,
         java.util.Set,
         java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelEdit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String doneUrl = done.getPageURL()+"&action=" + WebAppConstants.USER_ACTION_MODIFY_USER_PROJECTS;
    String cancelUrl = cancelEdit.getPageURL()+"&action=cancelEdit";
    String selfUrl = self.getPageURL();

    String title = bundle.getString("lb_edit") + " " + bundle.getString("lb_vendor") +
                    " - " + bundle.getString("lb_projects");

    // Labels of the column titles
    String nameCol = bundle.getString("lb_name");
    String descCol = bundle.getString("lb_description");
    String pmCol = bundle.getString("lb_project_manager");

    // Button names
    String okButton = bundle.getString("lb_ok");
    String cancelButton = bundle.getString("lb_cancel");
    String doneButton = bundle.getString("lb_done");

    // Data for the page
    // Paging Info
    int pageNum = 0;
    int numPages = 0;
    int listSize = 0;
    int totalProjects = 0;
    int projectsPerPage = 0;
    int projectPossibleTo = 0;
    int projectTo = 0;
    int projectFrom = 0;
    Integer sortChoice = new Integer(0);

    List projects = (List)request.getAttribute("defaultProjects");
    if (projects != null && projects.size() > 0)
    {
        pageNum = ((Integer)request.getAttribute(UserStateConstants.PROJ_PAGE_NUM)).intValue();
        numPages = ((Integer)request.getAttribute(UserStateConstants.PROJ_NUM_PAGES)).intValue();
        listSize = projects == null ? 0 : projects.size();
        totalProjects = ((Integer)request.getAttribute(UserStateConstants.PROJ_LIST_SIZE)).intValue();
        projectsPerPage = ((Integer)request.getAttribute(
            UserStateConstants.PROJ_NUM_PER_PAGE_STR)).intValue();
        projectPossibleTo = pageNum * projectsPerPage;
        projectTo = projectPossibleTo > totalProjects ? totalProjects : projectPossibleTo;
        projectFrom = (projectTo - listSize) + 1;
        sortChoice = (Integer)sessionMgr.getAttribute(UserStateConstants.PROJ_SORTING);
    }

%>
<HTML>
<!-- This JSP is envoy/administration/users/readOnlyProjects.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "projects";
    var helpFile = "<%=bundle.getString("help_vendors_projects")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           projectForm.action = "<%=cancelUrl%>";
           projectForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "saveUsers")
    {
        // From edit
        projectForm.action = "<%=doneUrl%>";
    }
    else
    {
        projectForm.action = formAction;
    }
    projectForm.submit();
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
    <span class="mainHeading">
        <%=title%>
    </span>
    <p>
<form name="projectForm" method="post">
<table border="0" bordercolor="green" cellpadding="0" cellspacing="0" class="standardText">
<%
    if (listSize > 0)
    {
%>
      <tr><td colspan="3"><%=bundle.getString("lb_project_default_list")%></td></tr>
        <tr>
            <td colspan=3>
            <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
                <TR VALIGN="TOP">
                    <TD ALIGN="RIGHT">
                <%
                // Make the Paging widget
                Object[] args = {new Integer(projectFrom), new Integer(projectTo), new Integer(totalProjects)};

                // "Displaying x to y of z"
                out.println(MessageFormat.format(
                            bundle.getString("lb_displaying_records"), args));

                out.println("<br>");
                out.println("&lt; ");

                // The "Previous" link
                if (pageNum == 1) {
                    // Don't hyperlink "Previous" if it's the first page
                    out.print(bundle.getString("lb_previous"));
                }
                else
                {
%>
                    <a href="<%=selfUrl%>&<%=UserStateConstants.PROJ_PAGE_NUM%>=<%=pageNum - 1%>&<%=UserStateConstants.PROJ_SORTING%>=<%=sortChoice%>"><%=bundle.getString("lb_previous")%></A>
<%
                }

                out.print(" ");

                // Print out the paging numbers
                for (int i = 1; i <= numPages; i++)
                {
                    // Don't hyperlink the page you're on
                    if (i == pageNum)
                    {
                        out.print("<b>" + i + "</b>");
                    }
                    // Hyperlink the other pages
                    else
                    {
%>
                        <a href="<%=selfUrl%>&<%=UserStateConstants.PROJ_PAGE_NUM%>=<%=i%>&<%=UserStateConstants.PROJ_SORTING%>=<%=sortChoice%>"><%=i%></A>
<%
                    }
                    out.print(" ");
                }
                // The "Next" link
                if (projectTo >= totalProjects) {
                    // Don't hyperlink "Next" if it's the last page
                    out.print(bundle.getString("lb_next"));
                }
                else
                {
%>
                    <a href="<%=selfUrl%>&<%=UserStateConstants.PROJ_PAGE_NUM%>=<%=pageNum + 1%>&<%=UserStateConstants.PROJ_SORTING%>=<%=sortChoice%>"><%=bundle.getString("lb_next")%></A>

<%
                }
                out.println(" &gt;");
%>
              </td>
            </tr>
<!-- results data table -->
            <tr>
              <td>
      <table border="0" cellspacing="0" cellpadding="5" class="list">
        <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px
;">
          <td style="padding-right: 10px;" nowrap class="standardText">
            <%=nameCol%>
          </td>
          <td style="padding-right: 10px;" nowrap class="standardText">
            <%=pmCol%>
          </td>
          <td style="padding-right: 150px;" nowrap class="standardText">
            <%=descCol%>
          </td>
        </tr>
<%
        for (int i=0; i < projects.size(); i++)
        {
            Project proj = (Project) projects.get(i);
            String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
            String desc = proj.getDescription();
            if (desc == null) desc = "";
            User pm = proj.getProjectManager();
%>
            <tr style="padding-bottom:5px; padding-top:5px;"
              valign=top bgcolor="<%=color%>">
              <td class="standardText">
                <%=proj.getName()%>
              </td>
              <td class="standardText">
                <%=pm.getFirstName()%>&nbsp;<%=pm.getLastName()%>
              </td>
              <td class="standardText">
                <%=desc%>
              </td>
            </tr>
<%      } %>
        </table>
        </td></tr><tr><td>&nbsp;</td></tr></table>
<%    } else { // end listsize==0
            out.println("<tr><td colspan=2>" + 
                bundle.getString("msg_no_projects") + "</td></tr>");
      }
%>

      <tr>
        <td colspan="3" style="padding-top:10px">
          <input type="button" name="<%=cancelButton %>" value="<%=cancelButton %>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=doneButton %>" value="<%=doneButton %>"
            onclick="submitForm('saveUsers')">
        </td>
      </tr>
</form>
</BODY>
</HTML>
