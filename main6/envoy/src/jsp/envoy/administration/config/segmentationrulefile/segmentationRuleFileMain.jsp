<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.permission.Permission, 
            com.globalsight.everest.webapp.pagehandler.PageHandler, 
            com.globalsight.everest.webapp.pagehandler.administration.config.segmentationrulefile.SegmentationRuleConstant, 
            com.globalsight.everest.util.comparator.SegmentationRuleFileComparator, 
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl,
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileType,
            com.globalsight.everest.company.CompanyWrapper,
            java.util.*"
    session="true"
%>

<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="dup" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rem" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="setDefault" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<jsp:useBean id="segmentationRules" scope="request"
 class="java.util.ArrayList" />

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String newURL = new1.getPageURL() + "&action=" + SegmentationRuleConstant.NEW;
  String editURL = edit.getPageURL() + "&action=" + SegmentationRuleConstant.EDIT;
  String dupURL = dup.getPageURL() + "&action=" + SegmentationRuleConstant.DUPLICATE;
  String remURL = rem.getPageURL() + "&action=" + SegmentationRuleConstant.REMOVE;
  String exportURL = export.getPageURL() + "&action=" + SegmentationRuleConstant.EXPORT;
  String setDefaultURL = setDefault.getPageURL() + "&action=" + SegmentationRuleConstant.SET_DEFAULT;
  String uploadURL = upload.getPageURL();
  
  String title= bundle.getString("lb_segmentation_rules");
  String helperText = bundle.getString("helper_text_segmentation_rules");
  String confirmRemove = bundle.getString("msg_confirm_segmentationrule_removal");
  String confirmExport = bundle.getString("msg_confirm_segmentationrule_export");
  
  String invalid = (String)request.getAttribute("invalid");
  
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "segmentationRules";
var helpFile = "<%=bundle.getString("help_segmentation_rules_main_screen")%>";

function submitForm(button)
{
	var isOk = true;
    if (button == "New")
    {
        segmentationForm.action = "<%=newURL%>";
    }
    else if (button == "Upload")
    {
        segmentationForm.action = "<%=uploadURL%>";
    }
    else
    {
        value = getRadioValue(segmentationForm.radioBtn);

        if (button == "Edit")
        {
            segmentationForm.action = "<%=editURL%>";
        }
        else if (button == "Dup")
        {
            segmentationForm.action = "<%=dupURL%>";
        }
        else if (button == "Remove")
        {
        	if (!confirm('<%=confirmRemove%>'))
        	{
        		isOk = false;
        	}
            segmentationForm.action = "<%=remURL%>";
        }
        else if (button == "Export")
        {
        	if(!confirm('<%=confirmExport%>'))
        	{
        		isOk = false;
        	}
            segmentationForm.action = "<%=exportURL%>";
        }
        else if (button == "SetDefault")
        {
        	segmentationForm.action = "<%=setDefaultURL%>";
        }
    }

    if (isOk)
    {
    	segmentationForm.submit();
    }
}

function enableButtons()
{
    if (segmentationForm.editBtn)
        segmentationForm.editBtn.disabled = false;
    if (segmentationForm.setDefaultBtn)
        segmentationForm.setDefaultBtn.disabled = false;
    if (segmentationForm.dupBtn)
        segmentationForm.dupBtn.disabled = false;
    if (segmentationForm.remBtn)
        segmentationForm.remBtn.disabled = false;
    if (segmentationForm.expBtn)
        segmentationForm.expBtn.disabled = false;
}
function gotoTMP()
{
	document.location =  "<%=LinkHelper.getWebActivityURL(request, "tmProfiles")%>";
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
      onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<span class=errorMsg><% if (invalid != null && invalid.length() > 0) out.print(invalid); %></span>
<form name="segmentationForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="<%=SegmentationRuleConstant.SEGMENTATIONRULE_LIST%>" key="<%=SegmentationRuleConstant.SEGMENTATIONRULE_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="<%=SegmentationRuleConstant.SEGMENTATIONRULE_LIST%>" id="segmentationRuleFile"
       key="<%=SegmentationRuleConstant.SEGMENTATIONRULE_KEY%>"
       dataClass="com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl"
       pageUrl="self"
       emptyTableMsg="msg_no_segmentationrulefile" >
        <amb:column label="" width="10px">
          <input type="radio" name="radioBtn" value="<%=segmentationRuleFile.getId()%>"
           onclick="enableButtons()">
        </amb:column>
        <amb:column label="lb_name" sortBy="<%=SegmentationRuleFileComparator.NAME%>"
         width="150px">
         <%= segmentationRuleFile.getName() %>
        </amb:column>
        <amb:column label="lb_type" sortBy="<%=SegmentationRuleFileComparator.TYPE%>"
            width="100px">
             <% out.print(SegmentationRuleFileType.getTypeString(segmentationRuleFile.getType())); %>
           </amb:column>
        <amb:column label="lb_is_default" sortBy="<%=SegmentationRuleFileComparator.IS_DEFAULT%>"
            width="100px">
             <% out.print(segmentationRuleFile.getIsDefault() ? "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>" : ""); %>
           </amb:column>
        <amb:column label="lb_description" sortBy="<%=SegmentationRuleFileComparator.DESC%>"
         width="400px">
          <% out.print(segmentationRuleFile.getDescription() == null ? "" :
             segmentationRuleFile.getDescription()); %>
        </amb:column>
        <% if (isSuperAdmin) { %>
        <amb:column label="lb_company_name" sortBy="<%=SegmentationRuleFileComparator.ASC_COMPANY%>">
            <%=CompanyWrapper.getCompanyNameById(segmentationRuleFile.getCompanyId())%>
        </amb:column>
        <% } %>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.SEGMENTATIONRULE_REMOVE%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
    name="remBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.SEGMENTATIONRULE_DUP%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_duplicate")%>"
      name="dupBtn" disabled onclick="submitForm('Dup');">
    </amb:permission>
    <amb:permission name="<%=Permission.SEGMENTATIONRULE_EXPORT%>" >
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_export")%>..."
     name="expBtn" disabled onclick="submitForm('Export');">
    </amb:permission>
    <amb:permission name="<%=Permission.SEGMENTATIONRULE_EDIT%>" >
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
     name="editBtn" disabled onclick="submitForm('Edit');">
     <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_set_default")%>"
     name="setDefaultBtn" disabled onclick="submitForm('SetDefault');">
    </amb:permission>
    <amb:permission name="<%=Permission.SEGMENTATIONRULE_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_upload")%>..."
      onclick="submitForm('Upload');">
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
      onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</BODY>
</HTML>

