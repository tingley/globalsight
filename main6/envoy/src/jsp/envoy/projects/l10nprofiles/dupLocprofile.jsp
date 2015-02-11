<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.localemgr.CodeSet,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.servlet.EnvoyServletException,
         com.globalsight.util.GlobalSightLocale,
         java.util.Collection,                
         java.util.Iterator,                
         java.util.List,                
         java.util.Locale,
         java.util.HashMap,
         java.util.Enumeration,
         java.util.ResourceBundle"
		 session="true" %>
         
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="saveDup"   class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String actionType = (String)sessionMgr.getAttribute(LocProfileStateConstants.ACTION);
    //labels
    String labelName = bundle.getString("lb_name");
    String labelSource = bundle.getString("lb_source_locale");
    String labelTarget = bundle.getString("lb_target_locale");
    String labelLocales = bundle.getString("lb_locale_pairs_dup");

    // errors
    String badPair = bundle.getString("jsmsg_wf_invalid_localepair");

    List localePairs = (List)request.getAttribute(LocProfileStateConstants.ALL_LOCALES);
    List sourceLocales = (List)request.getAttribute(LocProfileStateConstants.SOURCE_LOCALE);
    List targetLocales = (List)request.getAttribute(LocProfileStateConstants.TARGET_LOCALE);
    List templates = null;
    try
    {
        templates = new ArrayList(ServerProxy.getProjectHandler().
                                 getAllWorkflowTemplateInfos());
    }
    catch(Exception e)
    {
        throw new EnvoyServletException(e);
    }
    Hashtable l10nprofiles = null;
    try
    {
        l10nprofiles = ServerProxy.getProjectHandler().getAllL10nProfileNames();
    }
    catch(Exception e)
    {
        throw new EnvoyServletException(e);
    }


   // links for the save and cancel buttons
   String cancelURL = cancel.getPageURL() + "&" 
                     + LocProfileStateConstants.ACTION 
                     + "=" + LocProfileStateConstants.CANCEL_ACTION;
   String saveURL = saveDup.getPageURL() + "&" 
                     + LocProfileStateConstants.ACTION 
                     + "=" + LocProfileStateConstants.SAVEDUP_ACTION;
   
   // Titles                                 
   String dupTitle = bundle.getString("msg_loc_profiles_dup_title");
   String lbSave = bundle.getString("lb_save");	
   String lbCancel = bundle.getString("lb_cancel");	
      
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%= dupTitle %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var guideNode = "locProfiles";
var needWarning = true;
var objectName = "<%= bundle.getString("lb_loc_profile") %>"

var helpFile = "<%=bundle.getString("help_localization_profiles_duplication")%>";
var wfNamesArray = new Array();
var l10nNamesArray = new Array();
var dupNamesArray = new Array();
var localePairArray = new Array();
var localePairIdArray = new Array();

<%
    if (l10nprofiles != null) 
    {
        Collection names = l10nprofiles.values();
        Iterator iter = names.iterator();
        int i = 0;
        while (iter.hasNext())
        {
%>
            l10nNamesArray[<%=i++%>] = "<%=iter.next()%>";
<%
        }
    }
    if (templates != null)
    {
        int size = templates.size();
        for (int i=0; i<size; i++)
        {
            WorkflowTemplateInfo wft = (WorkflowTemplateInfo)templates.get(i);
%>
            wfNamesArray[<%=i%>] = "<%=wft.getName()%>";
<%
        }
    }
    if (localePairs != null)
    {
        int size = localePairs.size();
                
        for (int i=0; i<size; i++)
        {
           LocalePair localePair = (LocalePair)localePairs.get(i);
           String dispPair = localePair.getSource().getDisplayName(uiLocale) + " -> " + localePair.getTarget().getDisplayName(uiLocale);
           long lpId = localePair.getId();
           // this should mimic ProjectHandlerLocal.generateAutoName
           String dupName = localePair.getSource().toString() + "_" + localePair.getTarget().toString();
%>
           localePairArray[<%=i%>] = "<%=dispPair%>";
           localePairIdArray[<%=i%>] = "<%=lpId%>";
           dupNamesArray[<%=i%>] = "<%=dupName%>";
<%
        }
    }
%>

function removePair()
{
    var pairs = dupTemplateForm.resultLocales;

    if (pairs.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_wf_template_dup_remove") %>");
        return;
    }
    else
    {
        pairs.options[pairs.selectedIndex] = null;
    }
}

var first = true;

function addPair()
{
    var src = dupTemplateForm.sourceLocale;
    var targ = dupTemplateForm.targetLocale;
    var result = dupTemplateForm.resultLocales;
    if (src.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_wf_template_dup_src_locales") %>");
        return;
    }
    if (targ.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_wf_template_dup_targ_locales") %>");
        return;
    }
    
    var wariingStr = "The pairs have been added:";
    var waringFlag = false;
    
    for (i=0; i < targ.length; i++) {
        if(targ.options[i].selected == true) {
            var pair = src.options[src.selectedIndex].text + " -> " +
                   targ.options[i].text;
            var resIndex = validPair(pair);

            if (resIndex != -1) {
                if (first == true){
                    dupTemplateForm.resultLocales.options[0] = null;
                    first = false;
                }

                var id = localePairIdArray[resIndex];
                var len = dupTemplateForm.resultLocales.options.length;
                var flag = true;
					
                for (j=0; j < result.length; j++) {
                    if(id == result.options[j].value) {
                        flag = false;
                        break;
                    }
                }

                if(flag == true) {
                    dupTemplateForm.resultLocales.options[len] = new Option(pair, id);
                }
            }
            else {
                alert("<%=badPair%>");
            }
        }
    }
}


//
// Is this a valid locale pair?  No, return -1.
//
function validPair(pair)
{
    var i;
    for (i=0; i < localePairArray.length; i++)
    {
        if (localePairArray[i] == pair) {
            return i;
            break;
        }
    }
    return -1;
}

// Need to check for duplicate l10n names and workflow names. 
function checkForDuplicateName()
{
    // First check l10n names
    var pairs = dupTemplateForm.resultLocales;
    for (i=0; i < pairs.options.length; i++)
    {
        id = pairs.options[i].value;
        for (j=0; j < localePairIdArray.length; j++)
        {
            if (id == localePairIdArray[j])
            {
                dupName = ATrim(dupTemplateForm.nameTF.value) + "_" + dupNamesArray[j];
                for (k=0; k < l10nNamesArray.length; k++)
                {
                    if (l10nNamesArray[k].toLowerCase() == dupName.toLowerCase())
                    {
                        alert('<%= bundle.getString("jsmsg_duplicate_loc_profile")%>');
                        return false;
                    }
                }
            }
        }
    }
    // Now check workflow names
    for (i=0; i < pairs.options.length; i++)
    {
        id = pairs.options[i].value;
        for (j=0; j < localePairIdArray.length; j++)
        {
            if (id == localePairIdArray[j])
            {
                dupName = ATrim(dupTemplateForm.nameTF.value) + "_" + dupNamesArray[j];
                
                for (k=0; k < wfNamesArray.length; k++)
                {
                    if (wfNamesArray[k].toLowerCase() == dupName.toLowerCase())
                    {
                        alert('<%= bundle.getString("msg_duplicate_workflow_name")%>');
                        return false;
                    }
                }
            }
        }
    }
    return true;
}


function submitForm(formAction)
{
    dupTemplateForm.formAction.value = formAction;
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           dupTemplateForm.action = "<%=cancelURL%>";
           dupTemplateForm.submit();
       }
       else 
       {
          return false;
       }
    }
    else if (formAction == "save")
    {
        if(!checkForDuplicateName())
        {
           return false;
        }
        var options_string = "";
        var pairs = dupTemplateForm.resultLocales;
        for (loop=0; loop < pairs.options.length; loop++)
        {
            options_string += pairs.options[loop].value + ",";
        }
        dupTemplateForm.localePairs.value = options_string;

        if (confirmForm())
        {
            // Submit the form
            dupTemplateForm.action = "<%=saveURL%>";
            dupTemplateForm.submit();
        }
        else
        {
           return false;
        }
    }
}

function confirmForm() {
    var theName = dupTemplateForm.nameTF.value;
    theName = stripBlanks (theName);

    if (isEmptyString(dupTemplateForm.nameTF.value)) {
        alert("<%= bundle.getString("jsmsg_wf_template_name") %>");
        dupTemplateForm.nameTF.value = "";
        dupTemplateForm.nameTF.focus();
        return false;
    }

    var results = dupTemplateForm.resultLocales;
    if ((results.length == 1 && first == true) || results.length == 0)
    {
       alert("<%= bundle.getString("jsmsg_wf_template_dup_locales") %>");
       return false;
    }

    return true;
}


</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">


<TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
<TR>
<TD COLSPAN="3" CLASS="mainHeading">&nbsp;&nbsp;<%=dupTitle%></TD>
</TR>
<TR>
<TD VALIGN="TOP">
    <!-- left table -->
    <TABLE CELLSPACING="8" CELLPADDING="0" BORDER="0" CLASS="standardText">
	<form name="dupTemplateForm" method="post">
		<input type="hidden" name="DupLocProfile" value='<%=request.getAttribute("DupLocProfile")%>'/>
        <INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
        <TR>
            <TD>
            <%=labelName%><SPAN CLASS="asterisk">*</SPAN>:<BR>
            <INPUT TYPE="TEXT" SIZE="20" MAXLENGTH="25" NAME="nameTF" CLASS="standardText" />
            </TD>
        </TR>
        <TR>
            <TD>
            <%=labelSource%></SPAN>:<BR>
            <SELECT NAME="sourceLocale" CLASS="standardText" size=15>
<%
                if (sourceLocales != null)
                {
                    int size = sourceLocales.size();

                    for (int i=0; i<size; i++)
                    {
                       GlobalSightLocale locale = (GlobalSightLocale)sourceLocales.get(i);
                       String disp = locale.getDisplayName(uiLocale);
                       long lpId = locale.getId();

%>
                       <OPTION VALUE="<%=lpId%>" ><%=disp%></OPTION>
<%
                    }
                }
%>
            </SELECT>
            </TD>
            <TD>
            <%=labelTarget%>:<BR>
            <SELECT NAME="targetLocale" CLASS="standardText" multiple=true size=15 >
<%
                if (targetLocales != null)
                {
                    int size = targetLocales.size();

                    for (int i=0; i<size; i++)
                    {
                       GlobalSightLocale locale = (GlobalSightLocale)targetLocales.get(i);
                       String disp = locale.getDisplayName(uiLocale);
                       long lpId = locale.getId();

%>
                       <OPTION VALUE="<%=lpId%>" ><%=disp%></OPTION>
<%
                    }
                }
%>
            </SELECT>
            </TD>
            <TD>
              <TABLE>
                <TR>
                  <TD>
                    <INPUT TYPE="BUTTON" NAME="addButton" value=" >> "
                        ONCLICK="addPair()"><br>
                  </TD>
                </TR>
                <TR><TD>&nbsp;</TD></TR>
                <TR>
                  <TD>
                    <INPUT TYPE="BUTTON" NAME="removedButton" value=" << "
                        ONCLICK="removePair()">
                  </TD>
                </TR>
              </TABLE>
            </TD>
            <TD>
            <%=labelLocales%><SPAN CLASS="asterisk">*</SPAN>:<BR>
            <INPUT TYPE="HIDDEN" NAME="localePairs" VALUE="">
            <SELECT NAME="resultLocales" CLASS="standardText" size=15>
               <OPTION>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</OPTION>
            </SELECT>
            </TD>
        </TR>
            <TR>
            </TR>
            </TABLE>
            <!-- end left table -->
        </TD>
        <TD WIDTH="50">&nbsp;</TD>
        <TD VALIGN="TOP">          
        </form>
	</TD>
    </TR>
    </TD>
    </TR>


<TR>
    <TD CLASS="HREFBold" COLSPAN="2">
        <INPUT TYPE="BUTTON" NAME="<%=lbSave %>" VALUE="<%=lbSave %>" 
            ONCLICK="submitForm('save')">  
        <INPUT TYPE="BUTTON" NAME="<%=lbCancel %>" VALUE="<%=lbCancel %>" 
            ONCLICK="submitForm('cancel')">  
    </TD>
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
