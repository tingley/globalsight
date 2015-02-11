<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.util.comparator.DBProfileComparator, 
         com.globalsight.everest.webapp.pagehandler.administration.dbprofile.DBProfileMainHandler, 
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         java.util.Locale, java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="mod1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="details" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="profiles" class="java.util.ArrayList" scope="request"/>

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String newURL = new1.getPageURL();
  String modifyURL = mod1.getPageURL() + "&action=edit";
  String detailsURL = details.getPageURL();
  String title = bundle.getString("lb_db_profiles");
  String helperText = bundle.getString("helper_text_db_profile_main");
  String preReqData = (String)request.getAttribute("preReqData");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_db_profiles_main_screen")%>";

function submitForm(button)
{
    if (button == "New")
    {
<%
        if (preReqData != null)
        {
%>
            alert("<%=preReqData%>");
            return;
<%
        }
%>
        dbForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(dbForm.radioBtn);
        if (button == "Edit")
        {
            dbForm.action = "<%=modifyURL%>" + "&id=" + value;
        } else if (button == "Details") {
            dbForm.action = "<%=detailsURL%>" + "&id=" + value;
        }
    }
    dbForm.submit();
    return;

}

function enableButtons()
{
    dbForm.editBtn.disabled = false;
    dbForm.detailsBtn.disabled = false;
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="dbForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="profiles"
                 key="<%=DBProfileMainHandler.PROFILE_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="profiles" id="profile"
                     key="<%=DBProfileMainHandler.PROFILE_KEY%>"
                     dataClass="com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl"
                     pageUrl="self"
                     emptyTableMsg="msg_no_db_profiles" >
                <amb:column label="">
                    <input type="radio" name="radioBtn" value="<%=profile.getId()%>"
                        onclick="enableButtons()" >
                </amb:column>
                <amb:column label="lb_id" sortBy="<%=DBProfileComparator.ID%>"
                    width="50px">
                    <%= profile.getId() %>
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=DBProfileComparator.NAME%>"
                    width="150px">
                    <%= profile.getName() %>
                </amb:column>
                <amb:column label="lb_description" sortBy="<%=DBProfileComparator.DESC%>"
                    width="150px">
                     <% out.print(profile.getDescription() == null ?
                         "" : profile.getDescription()); %>
                </amb:column>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_details")%>"
            name="detailsBtn" disabled onClick="submitForm('Details');">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
            name="editBtn" disabled onClick="submitForm('Edit');">
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
            onClick="submitForm('New');">
    </td>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>


