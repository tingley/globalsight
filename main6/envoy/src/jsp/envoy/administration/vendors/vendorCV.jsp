<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.securitymgr.FieldSecurity,
                com.globalsight.everest.securitymgr.VendorSecureFields,
                com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
                com.globalsight.util.GlobalSightLocale,
                com.globalsight.util.edit.EditUtil,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>

<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="prev" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelNew" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelEdit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    String edit = (String) sessionMgr.getAttribute("edit");
   
    String doneURL = done.getPageURL() + "&action=doneCV";
    String prevURL = prev.getPageURL() + "&action=previous";
    String cancelEditURL = cancelEdit.getPageURL() + "&action=cancel";
    String cancelNewURL = cancelNew.getPageURL() + "&action=cancel";
    String nextURL = next.getPageURL() + "&action=next";
    String title = null;
    String lbCV = bundle.getString("lb_cv_resume");;
    if (edit != null)
    {
        title= bundle.getString("lb_edit") + " " +
             bundle.getString("lb_vendor") + " - " + lbCV;
    }
    else
    {
        title= bundle.getString("lb_new") + " " +
                    bundle.getString("lb_vendor") + " - " + lbCV;
    }
    
    //Labels
    String lbUpload = bundle.getString("lb_upload");
    String lbCut = bundle.getString("lb_cut_and_paste");
    String lbDone = bundle.getString("lb_done"); 
    String lbPrev = bundle.getString("lb_previous"); 
    String lbNext = bundle.getString("lb_next"); 
    String lbCancel = bundle.getString("lb_cancel");

    // Data
    Vendor vendor = (Vendor)sessionMgr.getAttribute(VendorConstants.VENDOR);
    String filename = null;
    String basename = null;
    String resumeText = "";
    if (vendor != null)
    {
        filename = vendor.getResumePath();
        basename = vendor.getResumeFilename();
        resumeText = vendor.getResume();
        if (resumeText == null) resumeText = "";
    }
    FieldSecurity hash = (FieldSecurity)
        sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String access = (String)hash.get(VendorSecureFields.RESUME);
    if (access == null) access = "shared";
        
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

var needWarning = true;
var objectName = "<%= bundle.getString("lb_vendor") %>";
var guideNode = "";
var helpFile = "<%=bundle.getString("help_vendors_cv")%>";

function submitForm(btnName) {
    if (btnName == "prev")
    {
        vendorForm.action = "<%=prevURL %>";
    }
    else if (btnName == "done")
    {
        vendorForm.action = "<%=doneURL %>";
    }
    else if (btnName == "next")
    {
        vendorForm.action = "<%=nextURL %>";
    }
    vendorForm.submit();
}

function setRadio(num)
{
    vendorForm.radioBtn[num].checked = true;
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<span class="mainHeading"> <%=title%> </span>
<form name="vendorForm" method="post" enctype="multipart/form-data">
<br>
<%
    if (access.equals("hidden"))
    {
%>
        <span class="standardText">
        <%= lbCV %>: <span class="confidential">[<%=bundle.getString("lb_confidential")%>]</span> <p>
        </span>
<%
    }
    else
    {
%>
    <% if (access.equals("shared"))  out.print(bundle.getString("helper_text_vendor_resume"));  %>
    <p>

     <table cellspacing="0" cellpadding="4" border="0" class="standardText">
        <tr>
            <td>
                <input type="radio" name="radioBtn" value="doc"><%=lbCV%>&nbsp;<%=lbUpload%>
            <% if (filename != null) { %>
                
                &nbsp;&nbsp;(<%=bundle.getString("lb_current_file")%>:
                <a class="standardHREF" target="_blank" href=<%=filename%>><%=basename%></a>)
            <% } %>
            </td>
        </tr>
        <% if (access.equals("shared")) { %>
        <tr>
            <td style="padding-left:20px">
                <input type="file" SIZE="40" name="resume" onchange='setRadio(0)' >
            </td>
        </tr>
        <% } %>
        <tr>
            <td>
                <input type="radio" name="radioBtn" value="text"><%=lbCV%>&nbsp;<%=lbCut%>
            </td>
        </tr>
        <tr>
            <td style="padding-left:20px">
                <amb:textarea rows="20" cols="80" name="resumeText"  onKeyPress='setRadio(1)' access="<%=access%>"><%=resumeText%></amb:textarea> 
            </td>
        </tr>
      </table>
    <p>
<%
    } // end if access
%>
<% if (edit == null) { %>
        <input type="button" name="<%=lbCancel%>" value="<%=lbCancel%>"
            onclick="location.replace('<%=cancelNewURL%>')">
        <input type="button" name="<%=lbPrev%>" value="<%=lbPrev%>" 
            onclick="javascript: submitForm('prev')">
        <input type="button" name="<%=lbNext%>" value="<%=lbNext%>" 
            onclick="javascript: submitForm('next')">
<% } else { %>
        <input type="button" name="<%=lbCancel%>" value="<%=lbCancel%>"
            onclick="location.replace('<%=cancelEditURL%>')">
<%
        if (access.equals("shared"))
        {
%>
        <input type="button" name="<%=lbDone%>" value="<%=lbDone%>" 
            onclick="javascript: submitForm('done')">
    <%  } // end if access %>
<% } //end if edit %>
<script>
    <% if (!access.equals("hidden"))
       {
           if (filename != null) { %>
            vendorForm.radioBtn[0].checked = true;
        <% } else if (resumeText != "") { %>
            vendorForm.radioBtn[1].checked = true;
        <% } %>
    <% } %>
</script>
</form>
</DIV>
</BODY>
</HTML>
