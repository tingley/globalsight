<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.securitymgr.FieldSecurity,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
                com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.foundation.User,
                com.globalsight.everest.securitymgr.VendorSecureFields,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle,
                java.util.Vector" 
        session="true" 
%>

<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="roles" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cv" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="contact" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="projects" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="custom" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="perms" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="security" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="setUser" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="removeUser" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<% 
    SessionManager sessionManager = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    String edit = (String) sessionManager.getAttribute("edit");
   
    String nextURL = next.getPageURL() + "&action=next";
    String rolesURL = roles.getPageURL() + "&action=roles";
    String cvURL = cv.getPageURL() + "&action=cv";
    String contactURL = contact.getPageURL() + "&action=contact";
    String projectsURL = projects.getPageURL() + "&action=projects";
    String securityURL = security.getPageURL() + "&action=security";
    String customURL = custom.getPageURL() + "&action=custom";
    String permsURL = perms.getPageURL() + "&action=perms";
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String saveURL = save.getPageURL() + "&action=save";
    String userURL = setUser.getPageURL() + "&action=setUser";
    String removeUserURL = removeUser.getPageURL() + "&action=removeUser";
    String pagetitle;
    if (edit == null)
        pagetitle= bundle.getString("lb_new");
    else
        pagetitle= bundle.getString("lb_edit");

    pagetitle += " " + bundle.getString("lb_vendor") + " - " +
                    bundle.getString("lb_basic_information");
    
    //Labels
    String lbVendorId = bundle.getString("lb_vendor_id");
    String lbExistingUser = bundle.getString("lb_is_existing_user");
    String lbFirstName = bundle.getString("lb_first_name");
    String lbLastName = bundle.getString("lb_last_name");
    String lbAlias = bundle.getString("lb_alias");
	String lbTitle = bundle.getString("lb_title");
	String lbCompanyName = bundle.getString("lb_company_name");
	String lbNotes = bundle.getString("lb_notes");
    String lbAmbassadorAccess = bundle.getString("lb_ambassador_access");
    String lbStatus = bundle.getString("lb_status");
    String lbUsername = bundle.getString("lb_user_name");
    String lbPassword = bundle.getString("lb_password");
    String lbPasswordRepeat = bundle.getString("lb_password_repeat");
    String lbPermissions = bundle.getString("lb_permissions");
    String lbInternal = bundle.getString("lb_internal");    
    String lbExternal = bundle.getString("lb_external");    
    String lbType = bundle.getString("lb_type");    
	String lbCountries = bundle.getString("lb_countries");
	String lbDateOfBirth = bundle.getString("lb_date_of_birth");
    String lbAllowed = bundle.getString("lb_allowed");    
    String lbNotAllowed = bundle.getString("lb_not_allowed");    
    String lbChoose = bundle.getString("lb_choose");    
    String lbCancel = bundle.getString("lb_cancel");    
    String lbNext = bundle.getString("lb_next");    
    String lbRoles = bundle.getString("lb_roles");
    String lbContact = bundle.getString("lb_contact_info");
    String lbCV = bundle.getString("lb_cv_resume");
    String lbProjects = bundle.getString("lb_projects");
    String lbSecurity = bundle.getString("lb_security");
    String lbSave = bundle.getString("lb_save");
    
        
    //Messages
    String jsmsgUserName = bundle.getString("jsmsg_users_user_name");
    String jsmsgAlias = bundle.getString("jsmsg_users_alias");
    String jsmsgPassword = bundle.getString("jsmsg_users_password");
    String jsmsgRepeatPassword = bundle.getString("jsmsg_users_repeat_password");
    String jsmsgFirstName = bundle.getString("jsmsg_users_first_name");
    String jsmsgLastName = bundle.getString("jsmsg_users_last_name");
    String jsmsgOverride = bundle.getString("jsmsg_override_user");

	//User UI locale 
	Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE); 	
	
    SessionManager sessionMgr =
         (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Custom button
    String lbCustom = (String) sessionMgr.getAttribute("customPageTitle");


    // Get the data
    String firstName = "";
    String lastName = "";
    String alias = "";
    String userTitle = "";
    String isInternal = "-1";
    String companyName = "";
    String notes = "";
    boolean hasAccess = false;
    String status = "";
    String userId = "";
    String countries = "";
    String dateOfBirth = "";
    User user = null;
    String password = "";
    String repeat = "";
    Vector allUsernames = (Vector)sessionMgr.getAttribute("allUsernames");
    Vector usernames = (Vector)sessionMgr.getAttribute("allUsernames");
    ArrayList allAliases = (ArrayList)sessionMgr.getAttribute("allAliases");
    ArrayList allVendorIds = (ArrayList)sessionMgr.getAttribute("allVendorIds");
    Vendor vendor = (Vendor) sessionMgr.getAttribute(VendorConstants.VENDOR);

    if (vendor != null)
    {
        firstName = vendor.getFirstName();
        lastName = vendor.getLastName();
        userTitle = vendor.getTitle();
        if (userTitle == null) userTitle = "";

        if (vendor.isInternalVendor()) 
            isInternal = "1";
        else
            isInternal = "0";

        companyName = vendor.getCompanyName();
        if (companyName == null) companyName = "";

        notes = vendor.getNotes();
        if (notes == null) notes = "";

        hasAccess = vendor.useInAmbassador();
        status = vendor.getStatus();
        userId  = vendor.getUserId();
        if (userId == null) userId = "";
        if (vendor.getPassword() != null)
        {
            password = "***************************";
            repeat = password;
        }
        user = vendor.getUser();

        countries = vendor.getNationalities();
        if (countries == null) countries = "";
        dateOfBirth = vendor.getDateOfBirth();
        if (dateOfBirth == null) dateOfBirth = "";
        alias = vendor.getPseudonym();
        if (alias == null) alias = "";
    }

    String[] statuses = (String[])sessionMgr.getAttribute("statuses");
    String[] companies = (String[])sessionMgr.getAttribute("companyNames");

    // field level security.  hash is empty for new vendor.
    FieldSecurity hash = (FieldSecurity)
         sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String companyAccess = (String)hash.get(VendorSecureFields.COMPANY);

    if (companyAccess == null) companyAccess = "shared";
    String vidAccess = (String)hash.get(VendorSecureFields.CUSTOM_ID);
    String userAccess = (String)hash.get(VendorSecureFields.USERNAME);
    if (userAccess == null) userAccess = "shared";
    String neverShowUserInfo = (String)sessionMgr.getAttribute("neverShowUserInfo");
    String securityPermission = (String)hash.get("security");
%>

<html>
<!-- This JSP is envoy/administration/vendors/vendorBasic.jsp -->
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= pagetitle %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<script language="JavaScript">

var needWarning = true;
var objectName = "<%= bundle.getString("lb_vendor") %>";
var helpFile = "<%=bundle.getString("help_vendors_basic_information")%>";
var guideNode = "";
var company;
var userInfoShowing = false;

var existingUsers = new Array();
<%
    ArrayList users = null;
    if (allUsernames != null)
    {
        for (int i =0; i < allUsernames.size(); i++)
        {
            User existing = (User) allUsernames.get(i);
%>
            existingUsers[<%=i%>] = "<%=existing.getUserId()%>";
<%
        }
    }
%>

var existingAliases = new Array();
<%
    ArrayList aliases = null;
    if (allAliases != null)
    {
        for (int i =0; i < allAliases.size(); i++)
        {
            String existing = (String) allAliases.get(i);
%>
            existingAliases[<%=i%>] = "<%=existing%>";
<%
        }
    }
%>

var existingVendorIds = new Array();
<%
    ArrayList vendorIds = null;
    if (allVendorIds != null)
    {
        for (int i =0; i < allVendorIds.size(); i++)
        {
            String existing = (String) allVendorIds.get(i);
%>
            existingVendorIds[<%=i%>] = "<%=existing%>";
<%
        }
    }
%>

function submitForm(btnName) {
    if (btnName == "next")
    {
        vendorForm.action = "<%=nextURL %>";
    }
    else if (btnName == "contact")
    {
        vendorForm.action = "<%=contactURL %>";
    }
    else if (btnName == "cv")
    {
        vendorForm.action = "<%=cvURL %>";
    }
    else if (btnName == "roles")
    {
        vendorForm.action = "<%=rolesURL %>";
    }
    else if (btnName == "projects")
    {
        vendorForm.action = "<%=projectsURL %>";
    }
    else if (btnName == "custom")
    {
        vendorForm.action = "<%=customURL %>";
    }
    else if (btnName == "security")
    {
        vendorForm.action = "<%=securityURL %>";
    }
    else if (btnName == "perms")
    {
        vendorForm.action = "<%=permsURL %>";
    }
    else if (btnName == "save")
    {
        vendorForm.action = "<%=saveURL %>";
    }
    if (confirmForm())
    {
        if (vendorForm.company)
        {
            if (company)
                vendorForm.company.value="true";
            else
                vendorForm.company.value="false";
        }
        if (isLongName())
        {
            alert("<%= bundle.getString("jsmsg_vendor_len") %>");
            vendorForm.userName.focus();
            return;
        }
        if (isDupUsername())
        {
            alert('<%=bundle.getString("jsmsg_duplicate_users")%>');
            vendorForm.userName.focus();
            return;
        }
        if (!isValidAlias())
        {
            alert('<%=bundle.getString("jsmsg_alias_quotes")%>');
            return;
        }
        if (isDupAlias())
        {
            alert('<%=bundle.getString("jsmsg_duplicate_alias")%>');
            return;
        }
        if (isDupVendorId())
        {
            alert('<%=bundle.getString("jsmsg_duplicate_vendor_id")%>');
            return;
        }
        vendorForm.submit();
    }
}

function isLongName()
{
    if (!vendorForm.userName)
        return false;
    if (!isNotLongerThan(vendorForm.userName.value, 40)) {
        return true;
    }
    return false;
}

function isDupUsername()
{
    if (!vendorForm.userName) // hidden or locked field
        return false

    if (vendorForm.user[0].checked) // existing amb user
        return false;

    if (document.getElementById("username2").style.display == "none")
        return false;
    username = vendorForm.userName.value;
    if (username == "")
        return false;
    for (i=0; i < existingUsers.length; i++)
    {
        if (existingUsers[i] == username)
            return true;
    }
    return false;
}

function isValidAlias()
{
    alias = vendorForm.alias.value;
    for (var i = 0; i < alias.length; i++)
    {
        if (alias.charAt(i) == '"')
        {
            return false;
        }
    }
    return true;
}

function isDupAlias()
{
    alias = vendorForm.alias.value;
    if ("<%=alias%>" == alias)
    {
        return false;  // the user didn't change it
    }
    for (i=0; i < existingAliases.length; i++)
    {
        if (existingAliases[i] == alias)
            return true;
    }
    return false;
}

function isDupVendorId()
{
    if (!vendorForm.customVendorId) // hidden or locked field
        return false
    value = vendorForm.customVendorId.value;
    for (i=0; i < existingVendorIds.length; i++)
    {
        if (existingVendorIds[i] == value)
            return true;
    }
    return false;
}

//
// This verifies that required fields are set and the password is the
// same as repeat password.  But it gets ugly with field level security
// and the fact that password, et al are not even displayed if GlobalSight
// access is not true AND status is not approved.
// This also checks for special chars in textfields.
function confirmForm()
{
    if (vendorForm.firstName)
    {
        var theFirst = vendorForm.firstName.value;
        theFirst = stripBlanks(theFirst);

        if (isEmptyString(theFirst)) {
            alert(" <%= jsmsgFirstName %>");
            vendorForm.firstName.value = "";
            vendorForm.firstName.focus();
            return false;
        }
        if (hasSpecialChars(theFirst))
        {
            alert("<%= lbFirstName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
            return false;
        }
    }
    if (vendorForm.lastName)
    {
        var theLast = vendorForm.lastName.value;
        theLast = stripBlanks(theLast);

        if (isEmptyString(theLast)) {
            alert(" <%= jsmsgLastName %>");
            vendorForm.lastName.value = "";
            vendorForm.lastName.focus();
            return false;
        }
        if (hasSpecialChars(theLast))
        {
            alert("<%= lbLastName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
            return false;
        }
    }

    if (vendorForm.vendorType)
    {
        if (vendorForm.vendorType.value == "-1") {
            alert(" <%= bundle.getString("jsmsg_users_type") %>");
            return false;
        }
    }

    var theAlias = vendorForm.alias.value;
    theAlias = stripBlanks(theAlias);

    if (isEmptyString(theAlias)) {
        alert(" <%= jsmsgAlias %>");
        vendorForm.alias.value = "";
        vendorForm.alias.focus();
        return false;
    }
    if (hasSpecialChars(theAlias))
    {
        alert("<%= lbAlias %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    if (vendorForm.userTitle)
    {
        if (hasSpecialChars(vendorForm.userTitle.value))
        {
            alert("<%= lbTitle %>" + "<%= bundle.getString("msg_invalid_entry") %>");
            return false;
        }
    }

<%
    if (neverShowUserInfo != null && neverShowUserInfo.equals("true"))
    {
%>
        return true;
<%
    }
%>
    if (userInfoShowing)
    {
        if (vendorForm.password)
        {
            var thePassword = vendorForm.password.value;
            thePassword = stripBlanks(thePassword);

            if (isEmptyString(thePassword)) {
                alert(" <%= jsmsgPassword %>");
                vendorForm.password.value = "";
                vendorForm.password.focus();
                return false;
            }
     
            var theRepeat = vendorForm.passwordConfirm.value;
            theRepeat = stripBlanks(theRepeat);

            if (theRepeat != thePassword) {
                alert(" <%= jsmsgRepeatPassword%>");
                vendorForm.passwordConfirm.value = "";
                vendorForm.password.value = "";
                vendorForm.password.focus();
                return false;
            }
            // Make sure the repeated password matches the first
            if (theRepeat != thePassword) {
                alert("<%= bundle.getString("jsmsg_users_repeat_password") %>");
                vendorForm.passwordConfirm.value = "";
                vendorForm.password.value = "";
                vendorForm.password.focus();
                return false;
            }


            // Removing the dummypassword
            if(thePassword == "***************************"){
               vendorForm.password.value = "";
               vendorForm.passwordConfirm.value = "";
            }
        }

        if (vendorForm.userName)
        {
            var theUserName = vendorForm.userName.value;
            theUserName = stripBlanks(theUserName);

            if (isEmptyString(theUserName)) {
                alert(" <%= jsmsgUserName %>");
                vendorForm.userName.value = "";
                vendorForm.userName.focus();
                return false;
            }
            if (hasSomeSpecialChars(theUserName))
            {
                alert("<%= lbUsername %>" + "<%= bundle.getString("msg_invalid_entry3") %>");
                return false;
            }
        }
    }

    return true;
}

function companiesRadioOn()
{
    company = false;
    if (vendorForm.radioBtn)
    {
        vendorForm.radioBtn[0].checked = true;
    }
}

function companiesRadioOff()
{
    company = true;
    if (vendorForm.radioBtn)
    {
        vendorForm.radioBtn[1].checked = true;
    }
}

function checkUsernameOff()
{
    <% if (!userId.equals("")) { %>
        // submit form to remove the userId and password
        // This can only happen if in one edit or new session,
        // they select a user, then change their mind.
        vendorForm.action = "<%=removeUserURL %>";
        vendorForm.submit();
    <% } %>
}

function checkUsername()
{
    if (confirm("<%=jsmsgOverride%>"))
    {
        vendorForm.user[0].checked = true;
        vendorForm.accessAllowed.options[0].selected = true;
        showUserInfo();
        vendorForm.action = "<%=userURL %>";
        vendorForm.submit();
    } 
    else
    { 
        vendorForm.user[0].checked = false;
        vendorForm.usernameSelect.options[0].selected = true;
    } 
}

//
// Determine whether to hide or show user info
// This is disgusting and hard to determine because some fields may have
// access of locked or hidden
//
// Basically if useInAmbassador is true, and status is APPROVED, and not an existing
// user, then show username, password, and repeat password. 
//
function showUserInfo()
{
<%
    if (neverShowUserInfo != null && neverShowUserInfo.equals("true"))
    {
%>
        return;
<%
    }
%>
    var existingUser = vendorForm.user[0].checked;
    var allowed = 1;
    if (vendorForm.accessAllowed)
        allowed = vendorForm.accessAllowed[vendorForm.accessAllowed.selectedIndex].value;
    var status = "APPROVED";
    if (vendorForm.status)
        status = vendorForm.status[vendorForm.status.selectedIndex].value;

    if (allowed == 1 && status == "APPROVED" && !existingUser)
    {
        document.getElementById("username2").style.display = "block";
        document.getElementById("pwd").style.display = "block";
        document.getElementById("rpwd").style.display = "block";
        <% if (edit != null) { %>
        document.getElementById("permButton").style.display = "block";
        <% } %>
        userInfoShowing = true;
    }
    else
    {
        document.getElementById("username2").style.display = "none";
        document.getElementById("pwd").style.display = "none";
        document.getElementById("rpwd").style.display = "none";
        <% if (edit != null) { %>
        document.getElementById("permButton").style.display = "none";
        <% } %>
        userInfoShowing = false;
    }

    //determine whether to show the permButton
    <% if (edit != null) { %>
    if (existingUser) {
       document.getElementById("permButton").style.display = "block";
    }
    <% } %>
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; top: 108px; left: 20px; right: 20px;">

<span class="mainHeading">
<%=pagetitle%>
</span>
<p>
    <form name="vendorForm" method="post">
    <input type="hidden" name="company">
    <table cellspacing="0" cellpadding="4" border="0" class="standardText">
        <tr class="standardText">
            <td colspan=2>
                <%= lbExistingUser%>
            </td>
        </tr>
        <tr class="standardText">
            <td colspan=2 style="padding-left:20px">
                <input type="radio" name="user" onclick="showUserInfo()"
<%              if (!userId.equals("")) out.print(" checked");
                if (!userAccess.equals("shared")) out.println(" disabled");
%>
                >
                 &nbsp;<%=bundle.getString("lb_yes")%>,
                &nbsp;<%=lbUsername%><span class="asterisk">*</span>:
                <amb:select name="usernameSelect" onChange="checkUsername()"
                     blanks="15" access='<%=userAccess%>'>
<%
                    for (int i = 0; i < usernames.size(); i++)
                    {
                        User userInList = (User)usernames.get(i);
                        boolean selected = !userId.equals("") &&
                                             userId.equals(userInList.getUserId());
                        String fullname = userInList.getFirstName() + " " +
                                          userInList.getLastName() + " (" +
                                          userInList.getUserName() + ")";
%>
                        <amb:selectOption value="<%=userInList.getUserId()%>"
                             selected="<%=Boolean.toString(selected)%>"
                             displayedText="<%=fullname%>" />
<%
                    }
%>
                </amb:select>
            </td>
        </tr>
        <tr class="standardText">
            <td colspan=2 style="padding-left:20px">
                <input type="radio" name="user" onclick="checkUsernameOff();showUserInfo();"
<%              if (userId.equals("")) out.print(" checked");
                if (!userAccess.equals("shared")) out.println(" disabled");
%>
                >
                &nbsp;<%=bundle.getString("lb_no")%>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr class="standardText">
            <td>
                <%= lbAmbassadorAccess%><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:select name="accessAllowed" onChange="showUserInfo()"
                 access='<%=(String)hash.get(VendorSecureFields.AMBASSADOR_ACCESS)%>'>
                    <amb:selectOption value="1"
                         selected="<%=Boolean.toString(hasAccess)%>"
                         displayedText="lb_allowed" />
                    <amb:selectOption value="0"
                         selected="<%=Boolean.toString(!hasAccess)%>"
                         displayedText="lb_not_allowed" />
                </amb:select>
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbStatus%><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:select name="status" onChange="showUserInfo()" access='<%=(String)hash.get(VendorSecureFields.STATUS)%>'>
<%
                    for (int i = 0; i < statuses.length; i++)
                    {
                        String stat = (String)statuses[i];
%>
                        <amb:selectOption value="<%=stat%>"
                             selected="<%=Boolean.toString(stat.equals(status))%>"
                             displayedText="<%=stat%>" />
<%
                    }
%>
                </amb:select>
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbVendorId%></span>:
            </td>
            <td>
                <% if (edit != null && !"hidden".equals(vidAccess)) { %>
                        <%=vendor.getCustomVendorId()%>
                <% } else if (edit != null && "hidden".equals(vidAccess)) { %>
                       <span class='confidential'>[<%=bundle.getString("lb_confidential")%>]</span>
                <% } else { %>
                    <amb:textfield size='40' name='customVendorId' access='<%=(String)hash.get(VendorSecureFields.CUSTOM_ID)%>' />
                <% } %>
            </td>
        </tr>
        <tr class="standardText" id="username2">
            <td>
                <%= lbUsername%><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:textfield  maxlength="40" size="40" name="userName"
                     value="<%= userId %>" access='<%=userAccess%>' />
            </td>
        </tr>
        <tr class="standardText" id="pwd">
            <td>
                <%= lbPassword %><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:password size="20" name="password" value="<%=password%>" access='<%=userAccess%>'/>
            </td>
        </tr>
        <tr class="standardText" id="rpwd">
            <td>
                <%= lbPasswordRepeat %><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:password size="20" name="passwordConfirm" access="<%=userAccess%>" />
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbFirstName %><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:textfield maxlength="40" size="40" name="firstName"
                     value="<%= firstName %>"
                     access='<%=(String)hash.get(VendorSecureFields.FIRST_NAME)%>' />
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbLastName%><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:textfield maxlength="40" size="40" name="lastName"
                     value="<%= lastName %>" access='<%=(String)hash.get(VendorSecureFields.LAST_NAME)%>' />
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbAlias%><span class="asterisk">*</span>:
            </td>
            <td>
                <input type="text" maxlength="40" size="40" name="alias" value="<%= alias %>">
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbTitle%>:
            </td>
            <td>
                <amb:textfield maxlength="40" size="40" name="userTitle"
                     value="<%= userTitle %>" access='<%=(String)hash.get(VendorSecureFields.TITLE)%>' />
            </td>
        </tr>
        <tr class="standardText">
            <td><%= lbCompanyName %>:</td>
<%
            if (companies != null)
            {
                out.println("<td>");
                if ("shared".equals(companyAccess))
                {
                    out.println("<input type='radio' name='radioBtn' >");
                }
%>
                <amb:select name="companies" onChange="companiesRadioOn()" blanks="15"
                     access="<%=companyAccess%>">
<%
                for (int i = 0; i < companies.length; i++)
                {
%>
                    <amb:selectOption value='<%=companies[i]%>'
                       selected='<%=Boolean.toString(companies[i].equals(companyName))%>'
                       displayedText='<%=companies[i]%>' />
<%
                }
%>
                </amb:select>
                </td>
                </tr>
<%
                if ("shared".equals(companyAccess))
                {
%>
                    <tr>
                    <td>&nbsp;</td>
                    <td><input type='radio' name='radioBtn' >
                    <amb:textfield size='40' name='companyName' 
                        value='<%=companyName%>' onKeyPress='companiesRadioOff()' />
<%
                }
%>
<%
            } else {
%>
                <td>
                <amb:textfield size='40' name='companyName' value='<%=companyName%>'
                     access='<%=companyAccess%>' />
<%
            }
%>
            </td>
		</tr>
        <tr class="standardText">
            <td>
                <%= lbType%><span class="asterisk">*</span>:
            </td>
            <td>
                <amb:select name="vendorType" access='<%=(String)hash.get(VendorSecureFields.IS_INTERNAL)%>' >
                    <amb:selectOption value="-1"
                         selected='<%=Boolean.toString(isInternal.equals("-1"))%>'
                         displayedText="lb_choose" />
                    <amb:selectOption value="0"
                         selected='<%=Boolean.toString(isInternal.equals("0"))%>'
                         displayedText="lb_external" />
                    <amb:selectOption value="1"
                         selected='<%=Boolean.toString(isInternal.equals("1"))%>'
                         displayedText="lb_internal" />
                </amb:select>
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbCountries%>:
            </td>
            <td>
                <amb:textfield maxlength="2000" size="40" name="countries" value="<%=countries%>"
                     access='<%=(String)hash.get(VendorSecureFields.CITIZENSHIP)%>' />
            </td>
        </tr>
        <tr class="standardText">
            <td>
                <%= lbDateOfBirth%>:
            </td>
            <td>
                <amb:textfield maxlength="20" size="40" name="dateOfBirth" value="<%=dateOfBirth%>"
                     access='<%=(String)hash.get(VendorSecureFields.DOB)%>' />
            </td>
        </tr>
        <tr class="standardText">
            <td valign="top">
                <%= lbNotes%>:
            </td>
            <td>
                <amb:textarea rows="5" cols="50" name="notes"
                     access='<%=(String)hash.get(VendorSecureFields.NOTES)%>' ><%=notes%></amb:textarea> 
            </td>
        </tr>
        <tr>
        <td colspan="2">&nbsp;
        </td>
        </tr>
        <% if (edit != null) { %>
            <tr>
            <td colspan="2" align="left">
                <input type="button" name="<%=lbRoles%>" value="<%=lbRoles%>..."
                    onclick="javascript: submitForm('roles')">
                <input type="button" name="<%=lbContact%>" value="<%=lbContact%>..." 
                    onclick="javascript: submitForm('contact')">
                <input type="button" name="<%=lbCV%>" value="<%=lbCV%>..."
                    onclick="javascript: submitForm('cv')">
                <input type="button" name="<%=lbProjects%>" value="<%=lbProjects%>..."                        
                    onclick="javascript: submitForm('projects')">
            <% if ("shared".equals(securityPermission)) { %>
                <input type="button" name="<%=lbSecurity%>" value="<%=lbSecurity%>..."
                    onclick="javascript: submitForm('security')">
            <% } %>
            <% if (lbCustom != null) { %>
                <input type="button" name="<%=lbCustom%>" value="<%=lbCustom%>..." 
                    onclick="javascript: submitForm('custom')">
            <% } %>

                <input type="button" id="permButton" name="<%=lbPermissions%>" value="<%=lbPermissions%>..." 
                    onclick="javascript: submitForm('perms')">
            </td>
            </tr>
            <tr>
            <td colspan="2" align="left">
                <input type="button" name="<%=lbCancel%>" value="<%=lbCancel%>" 
                    onclick="location.replace('<%=cancelURL%>')">
                <input type="button" name="<%=lbSave%>" value="<%=lbSave%>" 
                    onclick="javascript: submitForm('save')">
            </td>
            </tr>
        <% } else { %>
            <tr>
            <td colspan="2" align="left">
                <input type="button" name="<%=lbCancel%>" value="<%=lbCancel%>" 
                    onclick="location.replace('<%=cancelURL%>')">
                <input type="button" name="<%=lbNext%>" value="<%=lbNext%>"
                    onclick="javascript: submitForm('next')">
            </td>
            </tr>

        <% } %>
            </td>
        </tr>
    </table>
    </form>
</div>
<script language="javascript">
showUserInfo();
<%
    if ("shared".equals(companyAccess))
    {
        boolean found = false;
        if (companies != null)
        {
            for (int i = 0; i < companies.length; i++)
            {
                if (companies[i].equals(companyName))
                {
                    found = true;
%>
                    companiesRadioOn();
                    vendorForm.companyName.value = "";
<%
                }
            }
            if (found == false)
            {
%>
                companiesRadioOff();
<%
            }
        }
    }
%>
</script>
</body>
</html>
