<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.servlet.EnvoyServletException,
         com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
         com.globalsight.util.GlobalSightLocale,
         java.util.Locale,
         java.util.Vector,
         java.util.Enumeration,
         java.util.ResourceBundle"
		 session="true" %>
         
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save"   class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    //labels
    String labelName = bundle.getString("lb_name");
    String labelSource = bundle.getString("lb_source_locale");
    String labelTarget = bundle.getString("lb_target_locale");
    String labelLocales = bundle.getString("lb_locale_pairs_dup");

    // errors
    String badPair = bundle.getString("jsmsg_wf_invalid_localepair");
    String msgDuplicateName = bundle.getString("msg_duplicate_workflow_name");

    List localePairs = (List)request.getAttribute(WorkflowTemplateConstants.ALL_LOCALES);
    List sourceLocales = (List)request.getAttribute(WorkflowTemplateConstants.SOURCE_LOCALES);
    List targetLocales = (List)request.getAttribute(WorkflowTemplateConstants.TARGET_LOCALES);
    List projects = (List)request.getAttribute("projects");
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


   // links for the save and cancel buttons
   String cancelURL = cancel.getPageURL() + "&" 
                     + WorkflowTemplateConstants.ACTION 
                     + "=" + WorkflowTemplateConstants.CANCEL_ACTION;
   String saveURL = save.getPageURL() + "&" 
                     + WorkflowTemplateConstants.ACTION 
                     + "=" + WorkflowTemplateConstants.IMPORT_ACTION;
   
   // Titles                                 
   String dupTitle = bundle.getString("msg_wf_template_imp_title");	
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
var objectName = "<%= bundle.getString("lb_workflow") %>";
var guideNode = "workflows";
var needWarning = true;
var helpFile = "<%=bundle.getString("help_workflow_duplication")%>";
var wfNamesArray = new Array();
var dupNamesArray = new Array();
var localePairArray = new Array();
var localePairIdArray = new Array();

<%
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
    var pairs = impTemplateForm.resultLocales;
    
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
    var src = impTemplateForm.sourceLocale;
    var targ = impTemplateForm.targetLocale;
    if (src.selectedIndex == -1)
    {
        // put up error message
	    alert("<%= bundle.getString("jsmsg_wf_template_dup_src_locales") %>");
        return;
    }
    if (targ.selectedIndex == -1)
    {
        // put up error message
	    alert("<%= bundle.getString("jsmsg_wf_template_dup_targ_locales") %>");
        return;
    }
    // loop through selected target locales, creating pairs
    for (var i = 0; i < targ.length; i++)
    {
        if (targ.options[i].selected)
        {
            var pair = src.options[src.selectedIndex].text + " -> " +
                       targ.options[i].text;
            var resIndex = validPair(pair);
            if (resIndex != -1) 
            {
                if (first == true)
                {
                    impTemplateForm.resultLocales.options[0] = null;
                    first = false;
                }
                var id = localePairIdArray[resIndex];
                var len = impTemplateForm.resultLocales.options.length;
                impTemplateForm.resultLocales.options[len] = new Option(pair, id);
            }
            else
            {
                alert("<%=badPair%>"+ "\n"+pair); 
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

function checkForDuplicateName()
{
    if (hasSpecialChars(impTemplateForm.nameTF.value))
    {
        alert("<%=bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    
    // loop through selected locale pairs and compare with
    // all workflow names.  But first must get the dupName
    // from looking up the localepair id in the array to 
    // get an index to use in the dupNames array.
    var pairs = impTemplateForm.resultLocales;
    for (i=0; i < pairs.options.length; i++)
    {
        id = pairs.options[i].value;
        for (j=0; j < localePairIdArray.length; j++)
        {
            if (id == localePairIdArray[j])
            {
                dupName = impTemplateForm.nameTF.value + "_" + dupNamesArray[j];
                for (k=0; k < wfNamesArray.length; k++)
                {
                    if (wfNamesArray[k].toLowerCase() == dupName.toLowerCase()) 
                    {
                        alert('<%=msgDuplicateName%>');
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
    impTemplateForm.formAction.value = formAction;
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           impTemplateForm.action = "<%=cancelURL%>";
           impTemplateForm.submit();
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
        var pairs = impTemplateForm.resultLocales;
        for (loop=0; loop < pairs.options.length; loop++)
        {
            options_string += pairs.options[loop].value + ",";
        }
        impTemplateForm.localePairs.value = options_string;
	
        if (confirmForm())
        {
            // Submit the form
            impTemplateForm.action = "<%=saveURL%>";
            impTemplateForm.submit();
        }
        else
        {
           return false;
        }
    }
}

function confirmForm() {
    var theName = impTemplateForm.nameTF.value;
	theName = stripBlanks (theName);

	if (isEmptyString(impTemplateForm.nameTF.value)) {
	    alert("<%= bundle.getString("jsmsg_wf_template_name") %>");
	    impTemplateForm.nameTF.value = "";
	    impTemplateForm.nameTF.focus();
	    return false;
	}
	if (isEmptyString(impTemplateForm.filename.value)) {
	    alert("<%= bundle.getString("jsmsg_wf_template_file") %>");
	    return false;	
	}
     
	if (impTemplateForm.project.value == "-1") {
	    alert("<%= bundle.getString("jsmsg_wf_template_project") %>");
	    return false;	
	}
     
    var results = impTemplateForm.resultLocales;
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
<TD COLSPAN="3" CLASS="mainHeading">&nbsp;&nbsp;<%= dupTitle %></TD>
</TR>
<TR>
<TD VALIGN="TOP">
    <!-- left table -->
    <TABLE CELLSPACING="8" CELLPADDING="0" BORDER="0" CLASS="standardText">
	<form name="impTemplateForm" ENCTYPE="multipart/form-data" METHOD="post">
        <INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
        <TR>
            <TD colspan="3">
                <%=labelName%><SPAN CLASS="asterisk">*</SPAN>:<BR>
                <INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="48" NAME="nameTF" CLASS="standardText" /> (<%=bundle.getString("msg_wf_dup_note")%>)
            </TD>
        </TR>
        <TR>
            <TD colspan="3">
                <%=bundle.getString("lb_select_import_file_n") %><SPAN CLASS="asterisk">*</SPAN>:<BR>
                <INPUT TYPE="file" NAME="filename" id="idFilename" SIZE=40></INPUT>
            </TD>
        </TR>
        <TR>
            <TD colspan="3">
                <%=bundle.getString("lb_select_the_project_n") %><SPAN CLASS="asterisk">*</SPAN>:<BR>
            <select name="project">
               <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i = 0; i < projects.size(); i++)
            {
                ProjectInfo pi = (ProjectInfo) projects.get(i);
                out.println("<option value=" + pi.getProjectId() + ">" + pi.getName() +
                                "</option>");

            }
%>
            </select>
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
            <SELECT NAME="targetLocale" CLASS="standardText" size=15 multiple=true>
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
