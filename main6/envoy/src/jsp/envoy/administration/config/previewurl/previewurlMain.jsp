<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.cxe.entity.previewurl.PreviewUrlImpl,
				 com.globalsight.util.resourcebundle.ResourceBundleConstants,
				 com.globalsight.util.resourcebundle.SystemResourceBundle,
		 		 com.globalsight.everest.webapp.pagehandler.PageHandler, 
		 		 com.globalsight.everest.webapp.pagehandler.administration.config.previewurl.PreviewUrlMainHandler, 
		 		 com.globalsight.everest.util.comparator.PreviewUrlComparator, 
				 java.util.Locale, java.util.ResourceBundle"
				 session="true" %>

<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="edit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="previewUrls" class="java.util.ArrayList" scope="request"/>

<%
	ResourceBundle bundle = PageHandler.getBundle(session);
   	String newURL = new1.getPageURL() + "&action=new";
	String editURL = edit.getPageURL() + "&action=edit";
	String title= bundle.getString("lb_db_preview_rules");
    String helperText = bundle.getString("helper_text_preview");
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
var guideNode = "dbPreviewRules";
var helpFile = "<%=bundle.getString("help_db_preview_rules_main_screen")%>";

function submitForm(button)
{
    if (button == "New")
    {
        previewForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(previewForm.radioBtn);
        previewForm.action = "<%=editURL%>" + "&id=" + value;
    }
    previewForm.submit();
    return;

}

function enableButtons()
{
    previewForm.editBtn.disabled = false;
    previewForm.testBtn.disabled = false;
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
<form name="previewForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="previewUrls"
                 key="<%=PreviewUrlMainHandler.PREVIEW_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="previewUrls" id="previewUrl"
                     key="<%=PreviewUrlMainHandler.PREVIEW_KEY%>"
                     dataClass="com.globalsight.cxe.entity.previewurl.PreviewUrlImpl"
                     pageUrl="self"
                     emptyTableMsg="msg_no_db_connections" >
                <amb:column label="">
                    <input type="radio" name="radioBtn" value="<%=previewUrl.getId()%>"
                        onclick="enableButtons()" >
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=PreviewUrlComparator.NAME%>"
                    width="150px">
                    <%= previewUrl.getName() %>
                </amb:column>
                <amb:column label="lb_description"
                     sortBy="<%=PreviewUrlComparator.DESC%>"
                     width="400">
                     <% out.print(previewUrl.getDescription() == null ?
                         "" : previewUrl.getDescription()); %>
                </amb:column>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
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


