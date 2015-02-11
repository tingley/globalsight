<%@page import="com.globalsight.everest.webapp.WebAppConstants"%>
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

<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="newone" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String newUrl = newone.getPageURL() + "&" + WebAppConstants.USER_ACTION +
       "=" + WebAppConstants.USER_ACTION_NEW_LOCALES;
    String editUrl = modify.getPageURL()+ "&" + WebAppConstants.USER_ACTION +
       "=" + WebAppConstants.USER_ACTION_MODIFY_LOCALES;
    String removeUrl = remove.getPageURL() + "&" + WebAppConstants.USER_ACTION +
            "=" + WebAppConstants.USER_ACTION_REMOVE_ROLES;
    String doneUrl = done.getPageURL() + "&" + WebAppConstants.USER_ACTION +
       "=" + WebAppConstants.USER_ACTION_MODIFY_USER;
    String cancelUrl = cancel.getPageURL() + "&" + WebAppConstants.USER_ACTION + "=" + WebAppConstants.USER_ACTION_CANCEL_LOCALES;
    String selfUrl = self.getPageURL() + "&" + WebAppConstants.USER_ACTION + "=self";

    String subTitle = "";
    String title= bundle.getString("lb_roles");

    // Labels of the column titles
    String sourceCol = bundle.getString("lb_source_locale");
    String targetCol = bundle.getString("lb_target_locale");
    String companyName = bundle.getString("lb_company_name");

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String cancelButton = bundle.getString("lb_cancel");
    String doneButton = bundle.getString("lb_done");
    String removeButton = bundle.getString("lb_remove");

    String lbUserName = bundle.getString("lb_user_name");
    
    // Data for the page
    List pairs = (List)request.getAttribute("userLocalePairs");

    // Paging Info
    int pageNum = ((Integer)request.getAttribute("pageNum")).intValue();

    int numPages = ((Integer)request.getAttribute("numPages")).intValue();

    int listSize = pairs == null ? 0 : pairs.size();
    int totalPairs = ((Integer)request.getAttribute("listSize")).intValue();

    int pairsPerPage = ((Integer)request.getAttribute(
        "numPerPage")).intValue();
    int pairsPossibleTo = pageNum * pairsPerPage;
    int pairsTo = pairsPossibleTo > totalPairs ? totalPairs : pairsPossibleTo;
    int pairsFrom = (pairsTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionMgr.getAttribute("sorting");

    // Field level security
    FieldSecurity hash = (FieldSecurity)sessionMgr.getAttribute("securitiesHash");
    String access = (String)hash.get(UserSecureFields.ROLES);
    
    ModifyUserWrapper wrapper = (ModifyUserWrapper)sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);
    String userName = wrapper.getUserName();
%>
<HTML>
<!-- This JSP is envoy/administratin/users/modify2.jsp-->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = true;
    var objectName = "<%=bundle.getString("lb_user")%>";
    var guideNode = "users";
    var helpFile = "<%=bundle.getString("help_users_roles_list")%>";

function submitForm(selectedButton)
{
    if (selectedButton == 'New')
    {
        RolesForm.action = "<%=newUrl %>";
        RolesForm.submit();
        return;
    }
    if (selectedButton == 'Cancel')
    {
        RolesForm.action = "<%=cancelUrl %>";
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
    if (selectedButton == 'Done')
    {
        RolesForm.action = "<%=doneUrl %>";
        RolesForm.submit();
        return;
    }
    else if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_role") %>");
        return false;
    }

    if (selectedButton == 'Edit')
    {
        RolesForm.action = "<%=editUrl %>";
        values = selectedRadioBtn.split(",");
        RolesForm.sourceLocale.value = values[0];
        RolesForm.targetLocale.value = values[1];
        RolesForm.companyId.value = values[2];
    } else if (selectedButton == "Remove") {
    	RolesForm.action = "<%=removeUrl%>";
    }
    RolesForm.submit();
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
    
    <TABLE CELLPADDING=4 CELLSPACING=0 BORDER=0 CLASS="standardText">
        <TR VALIGN="TOP">
            <TD ALIGN="RIGHT">
            <form name="RolesForm" method="post">
<%
    if (access.equals("hidden"))
    {
%>
        </td><tr><td>&nbsp;
        <div class="standardText">
        <%= bundle.getString("lb_roles") %>: <span class="confidential">[<%=bundle.getString("lb_confidential")%>]
</span> <p>
        </div>
<%
    }
    else
    {
%>
        <%
        // Make the Paging widget
        if (listSize > 0)
        {
            Object[] args = {new Integer(pairsFrom), new Integer(pairsTo),
                     new Integer(totalPairs)};

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
                <a href="<%=selfUrl%>&<%="pageNum"%>=<%=pageNum - 1%>&<%="sorting"%>=<%=sortChoice%>"><%=bundle.getString("lb_previous")%></A>
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
                    <a href="<%=selfUrl%>&<%="pageNum"%>=<%=i%>&<%="sorting"%>=<%=sortChoice%>"><%=i%></A>
<%
                }
                out.print(" ");
            }
            // The "Next" link
            if (pairsTo >= totalPairs) {
                // Don't hyperlink "Next" if it's the last page
                out.print(bundle.getString("lb_next"));
            }
            else
            {
%>
                <a href="<%=selfUrl%>&<%="pageNum"%>=<%=pageNum + 1%>&<%="sorting"%>=<%=sortChoice%>"><%=bundle.getString("lb_next")%></A>

<%
            }
            out.println(" &gt;");
        }
%>
          </td>
        <tr>
          <td>
<input type="hidden" name="sourceLocale">
<input type="hidden" name="targetLocale">
<input type="hidden" name="companyId">
<div class='standardText'nowrap><b><%=lbUserName%></b>:&nbsp;<%=userName%></div>
<!-- UserRoleInfo data table -->
  <table border="0" cellspacing="0" cellpadding="5" class="list">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td>&nbsp;</td>
      <td style="padding-right: 20px;">
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=UserRoleComparator.SOURCENAME%>&doSort=true"> <%=sourceCol%></a>
      </td>
      <td style="padding-left: 20px;" >
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=UserRoleComparator.TARGETNAME%>&doSort=true"> <%=targetCol%></a>
      </td>
      <td style="padding-left: 20px;" >
      <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=UserRoleComparator.ASC_COMPANY%>&doSort=true">  <%=companyName %></a>
      </td>
    </tr>
<%
        if (listSize == 0)
        {
%>
        <tr>
          <td colspan=3 class='standardText'><%=bundle.getString("msg_no_roles")%></td>
        </tr>
<%
        }
        else
        {
              for (int i=0; i < listSize; i++)
              {
                String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                UserRoleInfo userRole = (UserRoleInfo)pairs.get(i);
                String value = userRole.getSource() + "," + userRole.getTarget() + "," + userRole.getCompanyId();
%>
                <tr style="padding-bottom:5px; padding-top:5px;"
                  valign=top bgcolor="<%=color%>">
                  <td>
<% if (access.equals("shared")) { %>
                    <input type="radio" name="radioBtn" value="<%=value%>">
<% } else { %>
                    &nbsp;
<% } %>
                  </td>
                  <td class="standardText">
                    <%=userRole.getSourceDisplayName()%>
                  </td>
                  <td style="padding-left: 20px;" class="standardText" >
                    <%=userRole.getTargetDisplayName()%>
                  </td>
                  <td style="padding-left: 20px;" class="standardText" >
                    <%=CompanyWrapper.getCompanyNameById(userRole.getCompanyId())%>
                  </td>
                </tr>
<%
              }
        }
%>
  </tbody>
  </table>
<% } //end if access %>
<!-- End Data Table -->
</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD>
<DIV ID="DownloadButtonLayer" ALIGN="RIGHT" STYLE="visibility: visible">
    <P>

    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
<% if (access.equals("shared")) { %>
    <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>" onClick="submitForm('Remove');">
    <INPUT TYPE="BUTTON" VALUE="<%=editButton%>..." onClick="submitForm('Edit');">
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." onClick="submitForm('New');">
    <INPUT TYPE="BUTTON" VALUE="<%=doneButton%>" onClick="submitForm('Done');">
<% } %>

</DIV>
</FORM>
</TD>
</TR>
</TABLE>

</BODY>
</HTML>
