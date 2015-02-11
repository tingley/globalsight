<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*, com.globalsight.everest.foundation.User,
                com.globalsight.everest.securitymgr.FieldSecurity,
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
                com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
                com.globalsight.util.GlobalSightLocale,
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
   
    String doneURL = done.getPageURL() + "&action=doneContact";
    String prevURL = prev.getPageURL() + "&action=previous";
    String cancelNewURL = cancelNew.getPageURL() + "&action=cancel";
    String cancelEditURL = cancelEdit.getPageURL() + "&action=cancel";
    String nextURL = next.getPageURL() + "&action=next";
    String title = null;
    if (edit != null)
    {
        title= bundle.getString("lb_edit") + " " +
                bundle.getString("lb_vendor") + " - " +
                    bundle.getString("lb_contact_information");
    }
    else
    {
        title= bundle.getString("lb_new") + " " +
                bundle.getString("lb_vendor") + " - " +
                bundle.getString("lb_contact_information");
    }
    
    //Labels
    String lbAddress = bundle.getString("lb_address");
    String lbCountry = bundle.getString("lb_country");
    String lbHomePhone = bundle.getString("lb_home_phone");
    String lbWorkPhone = bundle.getString("lb_work_phone");
    String lbCellPhone = bundle.getString("lb_cell_phone");
    String lbFax = bundle.getString("lb_fax");
    String lbEmail = bundle.getString("lb_email");
    String lbEmailLanguage = bundle.getString("lb_email_language");
    String lbChoose = bundle.getString("lb_choose");
    String lbDone = bundle.getString("lb_done"); 
    String lbPrev = bundle.getString("lb_previous"); 
    String lbNext = bundle.getString("lb_next"); 
    String lbCancel = bundle.getString("lb_cancel");
        
    //Messages
    String jsmsgAddress = bundle.getString("jsmsg_users_address");
    String jsmsgEmail = bundle.getString("jsmsg_users_email");
    String jsmsgUiLanguage = bundle.getString("jsmsg_users_ui_language");

	//available UI languages
	String[] uiLocales = UserHandlerHelper.getUILocales();

	//User UI locale 
	Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
	

    // Get the user wrapper off the session manager.
    User user = TaskHelper.getUser(session);
    String homePhone = "";
    String workPhone = "";
    String cellPhone = "";
    String fax = "";
    String address = "";
    String country = "";
    String email = "";
    String password = user.getPassword();
    String uiLanguage = "";

    Vendor vendor = (Vendor) sessionMgr.getAttribute(VendorConstants.VENDOR);

    if (vendor != null)
    {
        homePhone = vendor.getPhoneNumber(User.PhoneType.HOME);
        if (homePhone == null) homePhone = "";
        workPhone = vendor.getPhoneNumber(User.PhoneType.OFFICE);
        if (workPhone == null) workPhone = "";
        cellPhone = vendor.getPhoneNumber(User.PhoneType.CELL);
        if (cellPhone == null) cellPhone = "";
        fax = vendor.getPhoneNumber(User.PhoneType.FAX);
        if (fax == null) fax = "";
        address = vendor.getAddress();
        if (address == null) address = "";
        country = vendor.getCountry();
        if (country == null) country = "";
        email = vendor.getEmail();
        if (email == null) email = "";
        uiLanguage = vendor.getDefaultUILocale();
    }

    // field level security.  hash is empty for new vendor.
    FieldSecurity hash = (FieldSecurity)
             sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
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
var helpFile = "<%=bundle.getString("help_vendors_contact_information")%>";

function submitForm(btnName) {
    if (vendorForm.country)
    {
        if (hasSpecialChars(vendorForm.country.value))
        {
            alert("<%= lbCountry %>" + "<%= bundle.getString("msg_invalid_entry") %>");
            return false;
        }
    }
    if (btnName == "prev")
    {
        vendorForm.action = "<%=prevURL %>";
    }
    if (btnName == "done")
    {
        vendorForm.action = "<%=doneURL %>";
    }
    else if (btnName == "next")
    {
        vendorForm.action = "<%=nextURL %>";
    }
    vendorForm.submit();
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px;
     RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P></P>

     <TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="standardText">
        <FORM NAME="vendorForm" METHOD="post">
        <TR>
            <TD NOWRAP VALIGN="TOP"><%= lbAddress %>:</TD>
            <TD >
                <amb:textarea styleClass="standardText" name="address" cols="40" rows="4"
                    access='<%=(String)hash.get(VendorSecureFields.ADDRESS)%>' >
                    <%= address %>
                </amb:textarea>
            </TD>      
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbCountry%>:</TD>
            <TD>
                <amb:textfield maxlength="1000" size="40" name="country" value='<%= country %>' 
                    access='<%=(String)hash.get(VendorSecureFields.COUNTRY)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbHomePhone%>:</TD>
            <TD>
                <amb:textfield maxlength="200" size="40" name="homePhone" value='<%= homePhone %>' 
                    access='<%=(String)hash.get(VendorSecureFields.HOME_PHONE)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbWorkPhone%>:</TD>
            <TD>
                <amb:textfield maxlength="200" size="40" name="workPhone" value='<%= workPhone %>' 
                    access='<%=(String)hash.get(VendorSecureFields.WORK_PHONE)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbCellPhone%>:</TD>
            <TD>
                <amb:textfield maxlength="200" size="40" name="cellPhone" value='<%= cellPhone %>' 
                    access='<%=(String)hash.get(VendorSecureFields.CELL_PHONE)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbFax%>:</TD>
            <TD>
                <amb:textfield maxlength="200" size="40" name="fax" value='<%= fax %>' 
                    access='<%=(String)hash.get(VendorSecureFields.FAX)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbEmail %>:</TD>
            <TD>
                <amb:textfield maxlength="200" size="40" name="email" value='<%= email %>' 
                    access='<%=(String)hash.get(VendorSecureFields.EMAIL)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbEmailLanguage %>:</TD>
            <TD> 
            <amb:select name="uiLocale" access='<%=(String)hash.get(VendorSecureFields.EMAIL_LANGUAGE)%>'>
            <%                   
            if (uiLocales != null)
            {
                for (int i = 0; i < uiLocales.length; i++)
                {
                    Locale locale = PageHandler.getUILocale(uiLocales[i]);
                    String locString = locale.toString();
                    String language = locale.getDisplayLanguage(userUiLocale);
                    
                    boolean selected = false;
                    if (locString.equals(uiLanguage))
                    {
                        selected = true;
                    }
             %>
                    <amb:selectOption value="<%=locString%>"
                         selected="<%=Boolean.toString(selected)%>"
                         displayedText="<%=language%>" />
             <%
                }
            }             
            %>    
            </amb:select>
            </TD>
        </TR>

        </FORM>
        <TR>
        <TD COLSPAN="2">&nbsp;
        </TD>
        </TR>
        <TR>
            <TD COLSPAN="2">
<%
                if (edit != null)
                {
%>
                    <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
                    ONCLICK="location.replace('<%=cancelEditURL%>')">
                    <INPUT TYPE="BUTTON" NAME="<%=lbDone%>" VALUE="<%=lbDone%>" 
                        ONCLICK="javascript: submitForm('done')">
<%              } else {
%>
                    <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
                    ONCLICK="location.replace('<%=cancelNewURL%>')">
                    <INPUT TYPE="BUTTON" NAME="<%=lbPrev%>" VALUE="<%=lbPrev%>" 
                        ONCLICK="javascript: submitForm('prev')">
            
                    <INPUT TYPE="BUTTON" NAME="<%=lbNext%>" VALUE="<%=lbNext%>" 
                        ONCLICK="javascript: submitForm('next')">
<%              }
%>
            </TD>
        </TR>
        </TABLE>
</DIV>
</BODY>
</HTML>
