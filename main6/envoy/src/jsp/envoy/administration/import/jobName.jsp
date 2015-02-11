<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.imp.SelectFileHandler,
            com.globalsight.everest.webapp.pagehandler.administration.imp.MapFileProfileToFileHandler,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.everest.foundation.L10nProfile,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.SortUtil,
            com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
            java.util.*,
            java.io.File,
            java.util.regex.Matcher,
            java.util.regex.Pattern"
    session="true" %> 
<jsp:useBean id="fileImport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rssCancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cvsCancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String fileImportUrl = fileImport.getPageURL();
    String previousUrl = previous.getPageURL() + "&fromJobName=true";

    String cancelUrl = fileImportUrl + "&pageAction=" + WebAppConstants.CANCEL;

    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String jobType = (String)sessionMgr.getAttribute("jobType");
    if (jobType != null && "rssJob".equals(jobType))
    	cancelUrl = rssCancel.getPageURL();
    if (jobType != null && "cvsJob".equals(jobType))
    	cancelUrl = cvsCancel.getPageURL();

    // the list of files to import and the file profiles they are mapped to
    Hashtable fileList = 
        (Hashtable)sessionMgr.getAttribute(MapFileProfileToFileHandler.MAPPINGS);

    //Get all target locales
    GlobalSightLocale[] gslocales = null;
    Set _fileNames = fileList.keySet();
    Iterator iterator = _fileNames.iterator();
    if (iterator.hasNext())
    {
        String fileName = (String) iterator.next();
        FileProfile fp = (FileProfile) fileList.get(fileName);
        try
        {
        	long l10nProfileId = fp.getL10nProfileId();
        	L10nProfile l10nProfile = ServerProxy.getProjectHandler().getL10nProfile(l10nProfileId);
        	gslocales = l10nProfile.getTargetLocales();
        }
        catch (Exception ex) {}
    }
    List localeList = Arrays.asList(gslocales);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    GlobalSightLocaleComparator gslComp = new GlobalSightLocaleComparator(uiLocale);
    SortUtil.sort(localeList, gslComp);
    
    Boolean shouldSuggestJobName = (Boolean) request.getAttribute(SelectFileHandler.ATTR_SUGGEST_JOB_NAME);
    String suggestedJobName = "";
    if (shouldSuggestJobName.booleanValue()==true && fileList != null && fileList.size() > 0)
    {
        Set fileNames = fileList.keySet();
        Iterator iter = fileNames.iterator();
        if (iter.hasNext())
        {
            //just grab the first file in the list and try to make a suggestedJobName from it
            String fileName = (String) iter.next();
            String normalizedFileName = fileName.replace('\\','/');
            String[] tokens = normalizedFileName.split("/");
            if (tokens.length > 1)
            {
                suggestedJobName = tokens[1];
                /**
               suggestedJobName = tokens[tokens.length - 1];

               String localeTemp = tokens[0];

               String jobNameSupposed = tokens[1];
               Vector locales = ServerProxy.getLocaleManager().getAvailableLocales();
			   for(int i = 0; i < locales.size(); i ++) {
                   String localeString = locales.get(i).toString();
                   if (localeString.startsWith("iw"))
                    {
                        localeString = "he" + localeString.substring(2);
                    }
                    else if (localeString.startsWith("ji")) 
                    {
                        localeString = "yi" + localeString.substring(2);
                    }
                    else if (localeString.startsWith("in"))
                    {
                        localeString = "id" + localeString.substring(2);
                    }
                   if(localeTemp.equals(localeString)) {
                       Pattern pattern = Pattern.compile("[1-2][0-9]{3}[01][0-9][0-3][0-9]\\-[0-2][0-9][0-6][0-9]\\_.*$");
                       Matcher matcher = pattern.matcher(jobNameSupposed);
                       if(matcher.matches()) {
                           suggestedJobName = jobNameSupposed;
                       }
			       }
		       }
			   */
            }
                
        }
    }
%>
<HTML>
<!-- This JSP is envoy/administration/import/jobName.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_enter_job_name")%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "import";
var helpFile = "<%=bundle.getString("help_import_set_job_name")%>";

function submitForm()
{
   // Make sure the Job Name is not null
   if (document.jobForm.jobName.value == "")
   {
      alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_naming_job"))%>");
      return false;
   }

   // Do not allow "\",  "/", ":" and other characters in the job name
   // that are not valid in Windows (or Unix) filenames.
   var jobNameRegex = /[\\/:;\*\?\|\"<>&%]/;
   if (jobNameRegex.test(jobForm.jobName.value))
   {
      alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_invalid_job_name"))%>");
      return false;
   }

   //set flag to convert to which page
    if(jobForm.checkboxGoTo.checked) {
        document.jobForm.nextPageFlag.value = "true";
    }
    else {
        document.jobForm.nextPageFlag.value = "false";
    }

    //judge if there are target locales selected
    var selTargetLocales = document.getElementById("targetLocaleIds");
    var selectedNum = 0;
    for(var i=0; i<selTargetLocales.options.length; i++)
    {
        if(selTargetLocales.options[i].selected)
        {
        	selectedNum++;
        }
    }
    if (selectedNum <= 0)
    {
        alert("<%=bundle.getString("jsmsg_import_select_target_locale") %>");
        return false;
    }
   
    document.jobForm.submit();
}


function loadPage()
{
   loadGuides();
   jobForm.jobName.focus();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=bundle.getString("lb_enter_job_name")%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=bundle.getString("helper_text_job_name")%></TD>
  </TR>
</TABLE>
<P></P>

<FORM NAME="jobForm" ONSUBMIT="submitForm(); return false;" METHOD="POST"
 ACTION="<%=fileImportUrl%>">

<SPAN CLASS="standardTextBold"><%=bundle.getString("lb_job_name")%></SPAN><BR>
<INPUT TYPE="TEXT" SIZE=40 MAXLENGTH=170 NAME="jobName" VALUE="<%=suggestedJobName%>"><BR><br>

<% if (localeList != null && localeList.size() > 0) { %>
<SPAN CLASS="standardTextBold"><%=bundle.getString("lb_import_select_target_locale") %>:</SPAN><BR>
<span>
    <SELECT ID="targetLocaleIds" NAME="targetLocales" CLASS="standardText" size="8" MULTIPLE>
<%    for (int i=0; i<localeList.size(); i++) { 
	    GlobalSightLocale gslocale = (GlobalSightLocale) localeList.get(i);
        String localeDisplayName = gslocale.getDisplayName(uiLocale);
        long localeId = gslocale.getId();
%>
        <OPTION VALUE = <%=localeId%> SELECTED><%=localeDisplayName%>
<% } %>
     </SELECT>
</span><br/><br/>
  
<% } %>

<input type="checkbox" name="checkboxGoTo" value="checkbox"> 
  <span class='standardText'><%=bundle.getString("lb_import_continue_after_created") %></span><br><br>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="location.replace('<%=cancelUrl%>')">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_previous")%>"
 onclick="location.replace('<%=previousUrl%>')">
<INPUT TYPE="SUBMIT" VALUE="<%=bundle.getString("lb_create_job")%>">
<INPUT TYPE="HIDDEN" NAME="pageAction" VALUE="<%=WebAppConstants.IMPORT%>">
<INPUT TYPE="HIDDEN" NAME="nextPageFlag" VALUE="">
</FORM>

</DIV>
</BODY>
</HTML>