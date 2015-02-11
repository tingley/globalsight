<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.diplomat.javabeans.SortedNameValuePairsBean,
                com.globalsight.cxe.entity.fileprofile.FileProfile,
                com.globalsight.util.resourcebundle.ResourceBundleConstants,
                com.globalsight.util.resourcebundle.SystemResourceBundle,
                java.util.Locale, java.util.ResourceBundle,
	            com.globalsight.everest.webapp.pagehandler.PageHandler,
	            com.globalsight.everest.company.CompanyWrapper,
				java.util.ArrayList,
				java.util.Iterator,
                java.util.HashMap"
                session="true" %>

<jsp:useBean id="fileProfiles" class="java.util.Vector" scope="request" />
<jsp:useBean id="hiddenFields" class="com.globalsight.diplomat.javabeans.SortedNameValuePairsBean" scope="request" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);
    response.setHeader("Pragma", "No-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-cache");
    response.setContentType("text/html; charset=UTF-8");

	String title = "TeamSite Import";
	boolean isSingleCompany = false;
	String singleCompanyId = null;
%>

<jsp:useBean id="jobNamePrompt" class="java.util.Vector" scope="request" />
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

<%
    HashMap companyFpMap = new HashMap();
    Enumeration e = fileProfiles.elements();
    while(e.hasMoreElements())
    {
        FileProfile fileProfile = (FileProfile) e.nextElement();
        long companyId = fileProfile.getCompanyId();
        
        ArrayList companyFps = (ArrayList)companyFpMap.get(companyId);
        if (companyFps == null) companyFps = new ArrayList();
        companyFps.add(fileProfile);
        companyFpMap.put(companyId, companyFps);
    }

    if (companyFpMap.size() == 1)
    {
        isSingleCompany = true;
    }
    
    int c = 0;
    Iterator mapIt = companyFpMap.keySet().iterator();
    while (mapIt.hasNext()) 
    {
        String companyId = (String)mapIt.next();
        if (isSingleCompany) singleCompanyId = companyId;
%>
var fpId<%=companyId%> = new Array();
var fpName<%=companyId%> = new Array();
<%
        ArrayList fps = (ArrayList)companyFpMap.get(companyId);
        for (int i = 0; i < fps.size(); i++)
        {
            FileProfile fp = (FileProfile)fps.get(i);
%>
fpId<%=companyId%>[<%= i %>] = "<%= fp.getId() %>";
fpName<%=companyId%>[<%= i %>] = "<%= fp.getName() %>";
<%
        }
    }
%>

function updateFileProfiles(companyId)
{
    var fpSelectBox = document.mainForm.fileProfile;
    fpSelectBox.options.length = 0;
    
    var fpOption = new Option("<%= bundle.getString("lb_file_profiles") %>", "-1", false, false);
    fpSelectBox.options[0] = fpOption;
    
    if (companyId != "-1")
    {
        var idArray = eval("fpId" + companyId);
        var nameArray = eval("fpName" + companyId);

        for (var idi = 0; idi < idArray.length; idi++)
        {
            fpOption = new Option(nameArray[idi], idArray[idi], false, false);
            fpSelectBox.options[idi + 1] = fpOption;
        }
    }

    needNameCheck(-1);
}

// needNameArray to be filled in dynamically from localization profile
// with jobsize being batch (true) or determined by wordcount (false)
var needNameArray = new Array();
<%
    for(int i = 0; i < jobNamePrompt.size(); i++)
    {
%>
needNameArray[<%= i %>] = <%= jobNamePrompt.elementAt(i) %>;
<%
    }
%>

function needNameCheck(selectedOption) {
	if ((selectedOption != -1) && (needNameArray[selectedOption])) {
		if (document.layers)
document.contentLayer.document.variableLayer.visibility = "show";
		else variableLayer.style.visibility = "visible";
	}
	else {
		if (document.layers)
document.contentLayer.document.variableLayer.visibility = "hide";
		else variableLayer.style.visibility = "hidden";
	}
}

function isReady(form) {
// check for a job name
	if ("" == form.jobName.value) {
		form.jobName.value = "-1";
		alert ("A job name is required.");
		form.jobName.focus();
		return false;
	}

// check for a file profile
	if
(form.fileProfile.options[form.fileProfile.selectedIndex].value
== -1) {
		alert ("A file profile selection is required.");
		return false;
	}
	form.submit();

}
function submitForm() {
// Move Job Name if necessary
	if (document.layers) {
		if
(document.contentLayer.document.variableLayer.visibility == "show")
document.contentLayer.document.mainForm.jobName.value =
document.contentLayer.document.variableLayer.document.nameForm.jobName.value;
		isReady(document.contentLayer.document.mainForm);
	}
	else {
		if (variableLayer.style.visibility == "visible")
mainForm.jobName.value = nameForm.jobName.value;
		isReady(mainForm);
	}
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" <%    
    if (isSingleCompany)
    {
%>onload="updateFileProfiles(<%=singleCompanyId%>);"<%
    }
%>>
<DIV ID="header" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
        <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
            <TR>
                <TD><IMG SRC="/globalsight/images/logo_header.gif" HEIGHT="68" WIDTH="253"><BR>
                    <HR NOSHADE SIZE=1> 
                 </TD>
            </TR>
        </TABLE>
</DIV>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 88px; LEFT: 20px;">
<SPAN CLASS="mainHeading">Enter GlobalSight Job Information</SPAN><P>
<FORM method=POST onSubmit="return isReady(this)" NAME="mainForm" CLASS=standardText>

<%
    if (companyFpMap.size() > 1)
    {
%>
<P>
<SPAN CLASS="standardText">Please select a company:</SPAN><BR>
<SELECT NAME=companySlct onChange="updateFileProfiles(value);">
<OPTION VALUE=-1><%= bundle.getString("lb_company_name") %></OPTION>
<%
        Iterator mapIt2 = companyFpMap.keySet().iterator();
        while (mapIt2.hasNext())
        {
            String companyId = (String)mapIt2.next();
%>
<OPTION VALUE="<%=companyId%>" > <%=CompanyWrapper.getCompanyNameById(companyId)%> </OPTION>
<%
        }
%>
</SELECT>
<P>
<%
    }
%>

<P>
<SPAN CLASS="standardText">Please select a File Profile:</SPAN><BR>
<SELECT NAME=fileProfile onChange="needNameCheck(this.selectedIndex-1);">
<OPTION VALUE=-1><%= bundle.getString("lb_file_profiles") %></OPTION>
</SELECT>
<P>

	<%
	// set the hidden fields from the "hiddenFields" bean
	Set hiddenFieldKeys = hiddenFields.keySet();
    	Iterator hiddenFieldIterator = hiddenFieldKeys.iterator();
	while(hiddenFieldIterator.hasNext())
    	{
		String optionKey = (String) hiddenFieldIterator.next();
		String optionValue = (String) hiddenFields.getValue(optionKey);
	%>
		<INPUT TYPE=Hidden NAME="<%= optionKey %>" VALUE="<%= optionValue %>" >
	<%
	}
	%>
    <INPUT TYPE=Hidden NAME="jobName" VALUE="-1" >
</FORM>
<P>

<DIV ID="variableLayer" STYLE=" POSITION: RELATIVE; Z-INDEX: 9; TOP: 5px; LEFT: 0px; VISIBILITY: HIDDEN">
<FORM NAME="nameForm" ACTION="javascript:submitForm();" CLASS=standardText>
<SPAN CLASS="standardText">Please provide a Job Name:</SPAN><BR>
<INPUT TYPE="text" SIZE="30"
NAME="jobName" VALUE="" MAXLENGTH="320">
</FORM>
</DIV>
<P>
<SPAN CLASS="HREFBold">
<INPUT TYPE="BUTTON" NAME="Cancel" VALUE="Cancel" ONCLICK="window.close();">
<INPUT TYPE="BUTTON" NAME="OK" VALUE="OK" ONCLICK="submitForm();">
</SPAN>
</DIV>
</BODY>
</HTML>
