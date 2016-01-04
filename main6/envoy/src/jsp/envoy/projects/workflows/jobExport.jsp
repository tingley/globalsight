<%@page import="com.globalsight.everest.page.pageexport.ExportConstants"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            org.apache.log4j.Logger,
            com.globalsight.everest.page.SourcePage,
            com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
            com.globalsight.everest.page.PrimaryFile,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobExportHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            java.util.*,
com.globalsight.cxe.entity.exportlocation.ExportLocation,
com.globalsight.cxe.entity.fileprofile.FileProfile,
com.globalsight.cxe.entity.knownformattype.KnownFormatType,
com.globalsight.everest.foundation.L10nProfile,
com.globalsight.everest.jobhandler.Job,
com.globalsight.everest.localemgr.CodeSetImpl,
com.globalsight.everest.localemgr.CodeSet,
com.globalsight.everest.page.DataSourceType,
com.globalsight.everest.page.TargetPage,
com.globalsight.everest.projecthandler.Project,
com.globalsight.everest.projecthandler.WorkflowTypeConstants,
com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
com.globalsight.everest.servlet.EnvoyServletException,
com.globalsight.everest.servlet.util.ServerProxy,
com.globalsight.everest.util.system.SystemConfigParamNames,
com.globalsight.everest.util.system.SystemConfiguration,
com.globalsight.everest.webapp.WebAppConstants,
com.globalsight.everest.webapp.pagehandler.ControlFlowHelper,
com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper,
com.globalsight.everest.webapp.webnavigation.WebPageDescriptor,
com.globalsight.everest.workflowmanager.Workflow,
com.globalsight.util.GlobalSightLocale"
    session="true" %>
<%
    NavigationBean export = (NavigationBean) request.getAttribute("export");
    NavigationBean exported = (NavigationBean) request.getAttribute("exported");
    ResourceBundle bundle = PageHandler.getBundle(session);
    String taskId = (String)request.getAttribute(WebAppConstants.TASK_ID);
    String taskState = (String)request.getAttribute(WebAppConstants.TASK_STATE);
    StringBuffer exportBuffer = new StringBuffer(export.getPageURL());
    exportBuffer.append("&");
    exportBuffer.append(WebAppConstants.TASK_ID);
    exportBuffer.append("=");
    exportBuffer.append(taskId);
    exportBuffer.append("&");
    exportBuffer.append(WebAppConstants.TASK_STATE);
    exportBuffer.append("=");
    exportBuffer.append(taskState);
    
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String subTitle = bundle.getString("lb_my_jobs");
    String title = bundle.getString("action_export");
    String lbCancel = bundle.getString("lb_cancel");
    String lbExport = bundle.getString("lb_export");
    String moveToDtp = bundle.getString("lb_move_to_dtp");
    String primaryPrefix = JobExportHandler.PRIMARY_PREFIX;
    String secondaryPrefix = JobExportHandler.SECONDARY_PREFIX;

    boolean b_exportForUpdate = false;
    boolean b_exportMultipleActivities = false;
    Collection sourcePages = null;
    String extractedImage = null;
    String extractedToolTip = null;
    String unExtractedImage = null;
    String unExtractedToolTip = null;

    if ("true".equals(request.getParameter(JobManagementHandler.EXPORT_FOR_UPDATE_PARAM)))
    {
        b_exportForUpdate = true;
        lbExport = bundle.getString("lb_export_source");
        title = bundle.getString("lb_export_for_update");
        sourcePages = (Collection) request.getAttribute(JobExportHandler.ATTR_SOURCE_PAGES);
       extractedImage = bundle.getString("img_file_extracted");
       extractedToolTip = bundle.getString("lb_file_extracted");
       unExtractedImage = bundle.getString("img_file_unextracted");
       unExtractedToolTip = bundle.getString("lb_file_unextracted");
    }
    if ("true".equals(request.getParameter(JobManagementHandler.EXPORT_MULTIPLE_ACTIVITIES_PARAM)))
        b_exportMultipleActivities = true;
    Job job = (Job) request.getAttribute(JobExportHandler.ATTR_JOB);
    int numOfSourcePages = 0;
    if (job == null)
    {
        numOfSourcePages = 0;    
    } else 
    {
        numOfSourcePages = job.getSourcePages().size();    
    }
    int tableSize = 200;
    if (numOfSourcePages < 10)
        tableSize = numOfSourcePages * 20;

%>
<%!
private String getMainFileName(String p_filename)
{
  int index = p_filename.indexOf(")");
  if (index > 0 && p_filename.startsWith("("))
  {
    index++;
    while (Character.isSpace(p_filename.charAt(index)))
    {
      index++;
    }

    return p_filename.substring(index, p_filename.length());
  }

  return p_filename;
}

private String getSubFileName(String p_filename)
{
  int index = p_filename.indexOf(")");
  if (index > 0 && p_filename.startsWith("("))
  {
    return p_filename.substring(0, p_filename.indexOf(")") + 1);
  }

  return null;
}
%>
<HTML>
<HEAD>
    <META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
    <TITLE><%=title%></TITLE>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
    
    <style>
    div.tableContainer {
        height: <%=tableSize%>px;  /* must be greater than tbody*/
        overflow: auto;
        }
    
    table#scroll {
        width: 99%;     /*100% of container produces horiz. scroll in Mozilla*/
        border: solid 1px slategray;
        }
    
    table#scroll>tbody  {  /* child selector syntax which IE6 and older do not support*/
        overflow: auto;
        height: 268px;
        }
    
    thead#scroll td#scroll  {
        position:relative;
        top: expression(document.getElementById("data").scrollTop-2); /*IE5+ only*/
        }
    </style>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
    <SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "myJobs";
    <% if (b_exportForUpdate) { %>
    var helpFile = "<%=bundle.getString("help_job_export_source")%>";
    <% } else { %>
    var helpFile = "<%=bundle.getString("help_job_export")%>";
    <% } %>

    function loadPage()
    {
       <% if (!b_exportForUpdate) { %>
       updateButtonStatus();
       <% } %>   
       loadGuides();
    }
    
    function submitFormNoCheck(exportAction)
    {
        if (document.exportForm.delay)
        {
                var value = document.exportForm.delay.value;
                if (isEmptyString(value))
            {
            alert("<%=bundle.getString("jsmsg_delay_in_minutes")%>");
                    document.exportForm.delay.focus();
            return;
            }
    
            if (!isAllDigits(value))
            {
            alert("<%=bundle.getString("jsmsg_delay_in_minutes_numeric")%>");
                    document.exportForm.delay.focus();
            return;
            }
        }

        exportForm.exportAction.value = exportAction;
        exportForm.submit();
    }

    function submitForm(exportAction)
    {
        exportForm.exportAction.value = exportAction;
        if(exportAction == "cancel")
        {
            exportForm.submit();
        }
        else
        {
            if (validateSelection(document.exportForm))
            {
                exportForm.submit();
            }
            else
            {
                alert("<%=bundle.getString("jsmsg_select_a_page")%>");
            }
        }
    }

    function validateSelection(form)
    {
       var pageChecked = false;

       for (var i = 0; i < form.elements.length; i++)
       {
          if (form.elements[i].type == "checkbox" &&
              form.elements[i].name == "page")
          {
             if (form.elements[i].checked == true)
             {
                pageChecked = true;
                break;
             }
          }
       }

       return pageChecked;
    }

    function updateButtonStatus()
    {
       form = document.exportForm;
       var buttons = new Array();
       for (var i = 0; i < form.elements.length; i++)
       {
          if (form.elements[i].type == "button")
          {
             buttons.push(form.elements[i]);
          }
       }
       var dtpCheckboxes = form.dtpCheckbox;
       var dtpSelectedIndex = new Array();
       var transCheckboxes = form.transCheckbox;
       if (dtpCheckboxes != null)
       {
          if (dtpCheckboxes.length)
          {
             for (var i = 0; i < dtpCheckboxes.length; i++)
             {
               var dtpCheckbox = dtpCheckboxes[i];
               if (dtpCheckbox.checked)
               {
                  buttons[1].value = "<%= moveToDtp %>";
                  buttons[3].value = "<%= moveToDtp %>";
                  dtpSelectedIndex.push(i);
               }
             }
          }
          else
          {
             if (dtpCheckboxes.checked)
             {
                buttons[1].value = "<%= moveToDtp %>";
                buttons[3].value = "<%= moveToDtp %>";
                dtpSelectedIndex.push(0);
             }
          }
       }
       
       if (transCheckboxes != null)
       {
          if (transCheckboxes.length)
          {
             for (var i = 0; i < transCheckboxes.length; i++)
             {
               var transCheckbox = transCheckboxes[i];
               if (transCheckbox.checked && dtpSelectedIndex.length == 0)
               {
                  buttons[1].value = "<%= lbExport %>";
                  buttons[3].value = "<%= lbExport %>";
               }
             }
          }
          else
          {
             if (transCheckboxes.checked && dtpSelectedIndex.length == 0)
             {
                buttons[1].value = "<%= lbExport %>";
                buttons[3].value = "<%= lbExport %>";
             }
          }
       }
    }
    /**
     * This function checks the primary target file radio button
     * and all the pages under it for that particular workflow
     *
     * @param workflowCheckbox The checkbox object of
     *                        checkbox that was checked.
     */
    function checkWorkflowPages(workflowCheckbox)
    {
       updateButtonStatus();
       // "workflowCheckbox" is the checkbox object
       form = document.exportForm;
       var primary = "<%=primaryPrefix%>" + workflowCheckbox.value;
       var secondary = "<%=secondaryPrefix%>" + workflowCheckbox.value;
       for (var i = 0; i < form.elements.length; i++)
       {
          // If it's a checkbox or radio button
          if (form.elements[i].type == "checkbox" ||
              form.elements[i].type == "radio")
          {
             // If it's a page under the primary radio of the selected workflow
             if (form.elements[i].value.indexOf(primary) != -1)
             {
                // Set the checkbox equal to the parent workflow
                // checkbox state
                form.elements[i].checked = workflowCheckbox.checked;
                form.elements[i].disabled = false;
             }
             else if (form.elements[i].value.indexOf(secondary) != -1)
             {
                form.elements[i].checked = false;
                if (form.elements[i].type == "checkbox")
                {
                  form.elements[i].disabled = true;
                }
             }
          }
       }
    }

    /**
     * This function checks all the pages under the
     * selected radio button.  It'll also uncheck the
     * pages under the other radio button and disable
     * them.
     *
     * @param worflowRadioBtn The radio object of
     *                        radio button that was checked.
     */
    function checkPagesUnderRadioBtn(workflowRadioBtn)
    {
       // "workflowRadioBtn" is the radio button object
       form = document.exportForm;
       var wfId = workflowRadioBtn.value.substring(
          1, workflowRadioBtn.value.length);

       var alternativePrefix = workflowRadioBtn.value.substring(0, 1);

       wfId = alternativePrefix == "<%=secondaryPrefix%>" ?
          "<%=primaryPrefix%>"+wfId :
          "<%=secondaryPrefix%>"+wfId;

       for (var i = 0; i < form.elements.length; i++)
       {
          // If it's a checkbox
          if (form.elements[i].type == "checkbox")
          {
             // If it's a page under the selected workflow
             if (form.elements[i].value.indexOf(workflowRadioBtn.value) != -1)
             {
                // Set the checkbox equal to the parent workflow
                // checkbox state
                form.elements[i].checked = workflowRadioBtn.checked;
                form.elements[i].disabled = false;
             }
             else if (form.elements[i].value.indexOf(wfId) != -1 && workflowRadioBtn.checked)
             {
                form.elements[i].checked = !workflowRadioBtn.checked;
                form.elements[i].disabled = workflowRadioBtn.checked;
             }
          }
       }
    }

    function selectBOM(obj, id, isEnabled) {
	    var bomTypeElement;
	    for (var i=0;i<exportForm.elements.length;i++) {
	      if (exportForm.elements[i].name.indexOf("bomType_" + id) == 0) {
	        bomTypeElement = exportForm.elements[i];
	      }
	    }
    	      
    	if (obj.options.selectedIndex == 0 && isEnabled) {
    	  bomTypeElement.disabled = false;
    	} else
    	  bomTypeElement.disabled = true;
    }

	//for GBS-2599
	function handleSelectAll() {
		if (exportForm && exportForm.selectAll) {
			if (exportForm.selectAll.checked) {
				checkAll('exportForm');
			}
			else {
				clearAll('exportForm'); 
			}
		}
	}
    </SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

    <SPAN CLASS="mainHeading">
    <%=title%>
    </SPAN>
    <br>
    <br>
    <table cellspacing="0" cellpadding="0" border=0 class="standardText" >
      <tr>
        <td width=500>
          <%=bundle.getString("helper_text_export")%>
        </td>
      </tr>
    </table>
    <br>
<% if (!b_exportMultipleActivities) {%>
    <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
        <TR CLASS="tableHeadingBasic">
            <TD COLSPAN="3" NOWRAP>&nbsp;&nbsp;&nbsp;Details</TD>
        </TR>
        <TR CLASS="standardText">
            <TD NOWRAP><B><%=bundle.getString("lb_job")%>:</B></TD>
            <TD ROWSPAN="5" WIDTH="10">&nbsp;</TD>
            <TD><%=request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET)%></TD>
        </TR>

        <TR CLASS="standardText">
            <TD NOWRAP><B><%=bundle.getString("lb_initiator")%>:</B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.JOB_INITIATOR_SCRIPTLET)%></TD>
        </TR>

        <TR CLASS="standardText">
            <TD NOWRAP><B><%=bundle.getString("lb_source_locale")%>:</B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.SRC_LOCALE_SCRIPTLET)%></TD>
        </TR>

        <TR CLASS="standardText">
            <TD NOWRAP><B><%=bundle.getString("lb_project")%>:</B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.PROJECT_NAME_SCRIPTLET)%></TD>
        </TR>

        <TR CLASS="standardText">
            <TD NOWRAP><B><%=bundle.getString("lb_loc_profile")%>:</B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.L10NPROFILE_NAME_SCRIPTLET)%></TD>
        </TR>

    </TABLE>

    <P>
<%}%>
    <FORM ACTION="<%=exportBuffer.toString()%>" METHOD="POST" NAME="exportForm">
        <% if (b_exportForUpdate == false && b_exportMultipleActivities==false) { %>
        <SPAN CLASS="standardTextBold">
        <%=bundle.getString("lb_workflows")%>
        </SPAN>
        <% } %>

        <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText">
            <TR>
                <TD>
                <!-- Job Export Utility Bar -->
                <%@ include file="/envoy/projects/workflows/jobExportUtilityBar.jspIncl" %>
                <!-- End Job Export Utility Bar -->
                </TD>
            </TR>
            <TR>
                <TD>
                    <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText" STYLE="border: 1px solid #0C1476">
                        <TR>
                            <TD>
                                <TABLE CELLPADDING="4" CELLSPACING="0" BORDER="0"> 
                                    <% if (b_exportMultipleActivities == false) { %>
                                    <TR CLASS="tableHeadingBasic">
                                        <% if (b_exportForUpdate==false) {%>
                                        <TD NOWRAP width="5px">
											<input type="checkbox" onclick="handleSelectAll()" name="selectAll" checked="true" style="position:relative;left:-3px;"/>
											<IMG SRC="/globalsight/images/spacer.gif" WIDTH="20" HEIGHT="1"/>
										</TD>
                                        <TD NOWRAP>
                                            <SPAN CLASS="whiteBold"><%=bundle.getString("lb_target_locales")%></SPAN>
                                        </TD>
                                        <% } else { %>
                                        <TD NOWRAP>
                                            <SPAN CLASS="whiteBold"><%=bundle.getString("lb_source_locale")%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</SPAN>
                                        </TD>
                                        <%}%>
                                        <amb:permission name="<%=Permission.JOB_WORKFLOWS_EDITEXPORTLOC%>" >
                                        <TD NOWRAP>
                                            <SPAN CLASS="whiteBold"><%=bundle.getString("lb_export_locale_subdir")%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</SPAN>
                                        </TD>
                                        </amb:permission>
                                        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_export_location")%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</SPAN></TD>
                                        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_localized")%></SPAN>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</TD>
                                        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_export_charencoding")%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</SPAN></TD>
                                        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_utf_bom")%></SPAN>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</TD>
                                        <TD NOWRAP><SPAN CLASS="whiteBold">Populate Source Into<br>Un-Translated Target (XLF/XLZ only)</SPAN></TD>
                                        <% if (b_exportForUpdate==true) { %>
                                        <TD ALIGN="LEFT" NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_export_delay")%></SPAN>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</TD>
                                        <% } %>
                                    <% } %>
                                    </TR>
<% if (b_exportForUpdate) {
    String exportText =  getExportForUpdateText(
    	exportBuffer.toString(),
        bundle,
        uiLocale,
        job,
        request); %>
        <%=exportText%>
<% } else if (b_exportMultipleActivities) {
    %>
<!-- export multiple activity stuff here -->
<%
    //first get all the workflows and group them by locale
    HashMap localeMap = new HashMap();
    String v[] = (String[]) request.getParameterValues(JobManagementHandler.WF_ID);
    for (int i=0; i < v.length; i++)
    {
        long wfId = Long.parseLong(v[i]);
        Workflow w = WorkflowHandlerHelper.getWorkflowById(
            wfId);
        ArrayList workflows = (ArrayList)localeMap.get(w.getTargetLocale());
        
        if (workflows == null)
        {
            workflows = new ArrayList();
            localeMap.put(w.getTargetLocale(), workflows);
        }
        workflows.add(w);
    }
    //handle each locale separately
    Iterator iter = localeMap.keySet().iterator();
    while (iter.hasNext())
    {
        GlobalSightLocale gsl = (GlobalSightLocale) iter.next();
        ArrayList a = (ArrayList) localeMap.get(gsl);
        String targetLocale = gsl.getDisplayName(uiLocale);
        
        String prefixTargetLocale = targetLocale.substring(0,targetLocale.indexOf('[')-1);
        String suffixTargetLocale = targetLocale.substring(targetLocale.indexOf('['));
        %>        
        <TR CLASS="tableHeadingBasic"><TD COLSPAN=5><%=bundle.getString("lb_target_locale")%>: <%=targetLocale%></TD>
        <TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>
        <TR CLASS="tableHeadingBasic">
        <TD NOWRAP width="5px"><input type="checkbox" onclick="handleSelectAll()" name="selectAll" checked="true" style="position:relative;left:-3px;"/><IMG SRC="/globalsight/images/spacer.gif" WIDTH="20" HEIGHT="1"></TD>
        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_job")%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</SPAN></TD>
        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_source_locale")%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</SPAN></TD>
        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_localized")%></SPAN>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD NOWRAP>
        <amb:permission name="<%=Permission.JOB_WORKFLOWS_EDITEXPORTLOC%>" >
        <SPAN CLASS="whiteBold"><%=bundle.getString("lb_export_locale_subdir")%></SPAN>
        </amb:permission>
        </TD>
        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_export_location")%></SPAN></TD>
        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_export_charencoding")%></SPAN></TD>
        <TD NOWRAP><SPAN CLASS="whiteBold"><%=bundle.getString("lb_utf_bom")%></SPAN></TD>
        <TD NOWRAP><SPAN CLASS="whiteBold">Populate Source Into Un-Translated Target</SPAN></TD>
        </TR>
        
        <%
        for (int i=0; i < a.size(); i++)
        {
        	StringBuilder sb = new StringBuilder();
            Workflow w = (Workflow) a.get(i);
            L10nProfile l10nProfile = LocProfileHandlerHelper.getL10nProfile(
                w.getJob().getL10nProfileId());
            String workflowText = getWorkflowText(bundle,
                                           uiLocale,
                                           w,
                                           request,
                                           l10nProfile,
                                           b_exportMultipleActivities);
            sb.append(workflowText);
            if (i == a.size() - 1)
            {
            	sb.append("<INPUT TYPE=\"hidden\" NAME=\"");
                sb.append(JobManagementHandler.JOB_ID);
                sb.append("\" VALUE=\"");
                sb.append(w.getJob().getId());
                sb.append("\">");
            }
            %><%=sb.toString()%><%
        }    
    }
%>
<!-- end of export multiple activity stuff -->
<% } else { 
    String exportText = getWorkflowExportText(
        bundle,
        uiLocale,
        job,
        request, b_exportMultipleActivities);%>
        <%=exportText%>
<% } %>
                                </TABLE>
                                </div>
    </TD>
    </TR>
    <TR>
        <TD>
            <!-- Export Source -->
            <% if (b_exportForUpdate==true) { %>
                <SPAN CLASS="standardText"><%=bundle.getString("lb_srcpages_exported")%></SPAN>
                <% if (tableSize >= 200) { %>
                    <div id="data">
                <% } %>
            <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText" width="100%">
                <COL WIDTH="20"> <!-- Spacer -->
                <COL WIDTH="10"> <!-- Bullet -->
                <COL WIDTH="600"> <!-- Page path -->
            <%
            Iterator iter = sourcePages.iterator();
            while (iter.hasNext())
            {
                SourcePage sp = (SourcePage) iter.next();
                boolean isExtracted = sp.getPrimaryFileType()== PrimaryFile.EXTRACTED_FILE;
                String pageName = getMainFileName(sp.getExternalPageId());
                String subName = getSubFileName(sp.getExternalPageId());
                if (subName != null)
                {
                    pageName = pageName + " " + subName;
                }
            %>
                <TR VALIGN="TOP">
                    <TD>&nbsp;</TD>
                    <TD>&bull;</TD>
                    <TD>
                        <IMG SRC="<%=isExtracted ? extractedImage : unExtractedImage%>"
                        ALT="<%=isExtracted ? extractedToolTip : unExtractedToolTip%>"
                        WIDTH=13 HEIGHT=15>&nbsp; <%=pageName%>
                    </TD>
                </TR>
            <% } %>

            </TABLE>
            <!-- End Export Source -->
            <% } %>

        </TD>
    </TR>
    </TABLE>
    </TD>
    </TR>
    <TR>
        <TD>
        <!-- Job Export Utility Bar -->
        <%@ include file="/envoy/projects/workflows/jobExportUtilityBar.jspIncl" %>
        <!-- End Job Export Utility Bar -->
        </TD>
    </TR>
    </TD>
    </TABLE>
    <INPUT TYPE="HIDDEN" NAME="exportAction" VALUE="">
    </FORM>
</DIV>
</BODY>
</HTML>
<%!
    private String getExportForUpdateText(String p_baseURL,
                                          ResourceBundle p_bundle,
                                          Locale p_uiLocale,
                                          Job p_job,
                                          HttpServletRequest p_request)
    throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session.getAttribute(
                    WebAppConstants.PERMISSIONS);
        boolean hasPerm = userPerms.getPermissionFor(Permission.JOB_WORKFLOWS_EDITEXPORTLOC);
        List workflows = JobExportHandler.activeWorkflows(p_job);
        L10nProfile l10nProfile = LocProfileHandlerHelper.getL10nProfile(p_job.getL10nProfileId());
        StringBuffer sb = new StringBuffer();
        SystemConfiguration config = SystemConfiguration.getInstance();

        Workflow curWF = (Workflow)workflows.get(0);
        // get datasource type for displaying export info (only for filesystem)
        List targetPages = curWF.getTargetPages();
        String ds = "";
        if (WorkflowTypeConstants.TYPE_DTP.equals(curWF.getWorkflowType()))
        {
            ds = WorkflowTypeConstants.TYPE_DTP;    
        } else
        {
            ds = ((TargetPage)targetPages.get(0)).getDataSourceType();       
        }     
        boolean isFileSystem = ds.startsWith(DataSourceType.FILE_SYSTEM);
        boolean isDatabase = DataSourceType.DATABASE.equals(ds);
        boolean isMicrosoftOffice = false;
        long wfId = curWF.getId();

        if (!isDatabase && !WorkflowTypeConstants.TYPE_DTP.equals(curWF.getWorkflowType()))
        {
            isMicrosoftOffice = JobExportHandler.isMicrosoftOffice((TargetPage)targetPages.get(0));
        }

        GlobalSightLocale sourceLocale = p_job.getSourceLocale();
        if (!hasPerm) {
            sb.append("<INPUT TYPE=\"hidden\" ");
            sb.append("NAME=\"");
            sb.append(JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM);
            sb.append("_");
            sb.append(p_job.getId());
            sb.append("\" VALUE=\"\\");
            if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("export"))
               sb.append("export");
            else
               sb.append(sourceLocale.toString());
            sb.append("\">");
        }
        sb.append("<INPUT TYPE=\"hidden\" NAME=\"");
        sb.append(JobManagementHandler.JOB_ID);
        sb.append("\" VALUE=\"");
        sb.append(p_job.getId());
        sb.append("\">");
        sb.append("<INPUT TYPE=\"hidden\" NAME=\"");
        sb.append(JobManagementHandler.EXPORT_FOR_UPDATE_PARAM);
        sb.append("\" VALUE=\"true\">");

        sb.append("<TR CLASS=\"standardText\" BGCOLOR=\"");
        sb.append(JobManagementHandler.WHITE_BG);
        sb.append("\">\r\n");

        // source locale
        sb.append("<TD>");
        sb.append(sourceLocale.getDisplayName(p_uiLocale));
        sb.append("</TD>\r\n");

        // export directory
        if (isFileSystem)
        {
            if (hasPerm) {
                sb.append("<TD><SPAN CLASS=");
                sb.append("\"formFields\">");
                sb.append("<INPUT TYPE=\"text\" MAXLENGTH=\"1000\" SIZE=\"30\" ");
                sb.append("NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM);
                sb.append("_");
                sb.append(p_job.getId());
                sb.append("\" VALUE=\"\\");
                if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("export"))
                   sb.append("export");
                else
                   sb.append(sourceLocale.toString());
                sb.append("\" CLASS=\"formFields\"></SPAN>\r\n");
                sb.append("</TD>\r\n");
            }

            try
            {
                sb.append("<TD><SPAN CLASS=\"formFields\">\r\n");
                sb.append("<SELECT NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_LOCATION_PARAM);
                sb.append("_");
                sb.append(p_job.getId());
                sb.append("\">\r\n");
                addExportLocationOptions(sb,p_bundle);
                sb.append("</SELECT></TD>\r\n");
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
        else
        {
            sb.append("<TD><SPAN CLASS=");
            sb.append("\"standardText\">");
            sb.append(p_bundle.getString("lb_na"));
        }
        sb.append("</TD>\r\n");

        // Is Workflow Localized column
        sb.append("<TD>N/A</TD>");
        
        // data encoding
        String codeset = l10nProfile.getCodeSet(sourceLocale);
        boolean needBOMProcessing = needBOMProcessing(p_job.getAllFileProfiles());
        boolean isXlfSrcAsTrgEnabled = isXlfSrcAsTrgEnabled(p_job.getAllFileProfiles());

        sb.append("<TD><SPAN CLASS=");
        if (!isDatabase)
        {
            sb.append("\"formFields\">");
            sb.append("<SELECT NAME=\"");
            sb.append(JobManagementHandler.EXPORT_WF_CODE_PARAM);
            sb.append("_");
            sb.append(p_job.getId());
            sb.append("\" CLASS=\"formFields\" onchange=\"selectBOM(this, " + p_job.getId() + ", " + needBOMProcessing + ")\">\r\n");

            if (isMicrosoftOffice)
            {
                sb.append("<OPTION SELECTED>UTF-8</OPTION>\r\n");
            }
            else
            {
                boolean hasJsp = false;
            	boolean hasJavaProperties = false;
                for (int i = 0; i < targetPages.size(); i++)
                {
                    TargetPage page = (TargetPage)targetPages.get(i);
                    String name = page.getExternalPageId();
                    if (name.toLowerCase().endsWith(".properties"))
                    {
                        hasJavaProperties = true;
                        //break;
                    }
                    if (name.toLowerCase().endsWith(".jsp"))
                    {
                    	hasJsp = true;
                    }
                }
                addCodeSetOptions(codeset, sourceLocale,sb,hasJavaProperties,hasJsp);
            }

            sb.append("</SELECT>");
        }
        else
        {
            sb.append("\"standardText\">");
            sb.append(codeset);
        }
        sb.append("</SPAN></TD>\r\n");
        
        //BOM Processing
        int BOMType = p_job.getFileProfile().getBOMType();
        sb.append("<TD><SPAN class=\"formField\">");
        sb.append("<SELECT id=\"bomType_" + p_job.getId() + "\" name=\"bomType_");
        sb.append(p_job.getId());
        sb.append("\" class=\"standardText\" " + (needBOMProcessing ? "" : "disabled") + ">");
        sb.append("<option value='-1'>" + p_bundle.getString("lb_utf_bom_file_profile_setting") + "</option>");
        sb.append("<option value='1'>" + p_bundle.getString("lb_utf_bom_preserve") + "</option>");
        sb.append("<option value='2'>" + p_bundle.getString("lb_utf_bom_add") + "</option>");
        sb.append("<option value='3'>" + p_bundle.getString("lb_utf_bom_remove") + "</option>");
        sb.append("</SELECT>\r\n</SPAN></TD>\r\n");

        // XLF Source As Target
        sb.append("<TD><SPAN class='formField'>");
        sb.append("  <SELECT id='xlfSrcAsTrg_" + p_job.getId() + "' name='xlfSrcAsTrg_" + p_job.getId() + "' class='standardText' " + (isXlfSrcAsTrgEnabled?"":"disabled") + ">");
        sb.append("    <option value='0'>No&nbsp;&nbsp;&nbsp;&nbsp;</option>");
        sb.append("    <option value='1'>Yes&nbsp;&nbsp;&nbsp;&nbsp;</option>");
        sb.append("  </SELECT>\r\n</SPAN></TD>\r\n");

        //job cancellation delay
        sb.append("<TD><input type=\"text\" name=\"");
        sb.append(JobExportHandler.PARAM_DELAY);
        sb.append("\" value=\"30\" MAXLENGTH=\"5\" SIZE=\"5\"></td>");

        sb.append("</TR>\r\n");
        return sb.toString();
    }

    // add the initial table settings for the pages under a workflow section
    private void addInitialTableSettings(StringBuffer p_sb)
    {
        p_sb.append("<TR>\r\n");
        p_sb.append("  <TD COLSPAN=\"8\">\r\n");
        p_sb.append("  <div id=\"data\">\r\n");
        p_sb.append("    <TABLE BORDER='0' CELLPADDING='0' CELLSPACING='0' CLASS='standardText' WIDTH='100%'>\r\n");
        p_sb.append("      <COL WIDTH=20>   <!-- Spacer column -->");
        p_sb.append("      <COL WIDTH=20>   <!-- Checkbox -->");
        p_sb.append("      <COL WIDTH='90%'>  <!-- Page path -->");
    }

    // add the table headings for primary/secondary target files
    private void addTableHeadings(StringBuffer p_sb, long p_wfId,
                                  String p_radioValue, String p_title,
                                  String p_isChecked)
    {
        p_sb.append("<TR>\r\n");
        p_sb.append("  <TD COLSPAN='6'>\r\n");
        p_sb.append("    <TABLE BORDER='0' WIDTH='80%' CELLPADDING=0");
        p_sb.append("      <TR>\r\n");
        p_sb.append("        <TD>&nbsp;</TD>");
        p_sb.append("        <TD CLASS='tableHeadingBasic'>");
        p_sb.append("          <INPUT TYPE='RADIO' NAME='RadioBtn" + p_wfId + "' VALUE='" + p_radioValue + "' " + p_isChecked);
        p_sb.append("          ONCLICK='checkPagesUnderRadioBtn(this);'>\r\n");
        p_sb.append(           p_title);
        p_sb.append("        </TD>\r\n");
        p_sb.append("      </TR>\r\n");
        p_sb.append("    </TABLE>\r\n");
        p_sb.append("  </TD>\r\n");
        p_sb.append("</TR>\r\n");
    }
    
    // build the html table with pages
    private void preparePageRows(StringBuffer p_sb, long p_id,
                                 String p_wfValue,
                                 String p_checkedOrDisabled,
                                 String p_icon, String p_toolTip,
                                 String p_displayName)
    {
        String pageName = getMainFileName(p_displayName);
        String subName = getSubFileName(p_displayName);
        if (subName != null)
        {
            pageName = pageName + " " + subName;
        }
        p_sb.append("<TR VALIGN=TOP>\r\n");
        p_sb.append("  <TD>&nbsp;</TD>");
        p_sb.append("  <TD>");
        p_sb.append("    <INPUT TYPE='checkbox' NAME='page' VALUE='pageId_" + p_id + "_wfId_" + p_wfValue + "' " + p_checkedOrDisabled + ">");
        p_sb.append("  </TD>\r\n");
        p_sb.append("  <TD STYLE='word-wrap: break-word' WIDTH='98%'>");
        p_sb.append("    <IMG SRC='" + p_icon + "' ALT='" + p_toolTip + "' WIDTH=13 HEIGHT=17>&nbsp;" + pageName);
        p_sb.append("  </TD>\r\n");
        p_sb.append("</TR>\r\n");
    }

    // closing the inner table
    private void closeInnerTable(StringBuffer p_sb)
    {
        p_sb.append("</TABLE>\r\n");
        p_sb.append("</TD>\r\n");
        p_sb.append("</TR>\r\n");
    }
    /**
     * Get the workflows to be displayed on the Export
     * page.
     *
     * @param p_bundle
     * @param p_uiLocale
     * @param p_job
     * @param p_request
     * @return
     * @exception EnvoyServletException
     */
    private String getWorkflowExportText(ResourceBundle p_bundle,
                                         Locale p_uiLocale,
                                         Job p_job,
                                         HttpServletRequest p_request,
                                         boolean b_exportMultipleActivities)
    throws EnvoyServletException
    {
        List workflows = JobExportHandler.activeWorkflows(p_job);
        L10nProfile l10nProfile =
        LocProfileHandlerHelper.getL10nProfile(p_job.getL10nProfileId());

        // Check if they only want selected workflows, instead of all the
        // workflows in a job
        boolean exportSelectedWFOnly = false;
        String s =
        p_request.getParameter(JobManagementHandler.EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM);
        if (s != null && s.equalsIgnoreCase("true"))
            exportSelectedWFOnly = true;

        StringTokenizer tokenizer = null;
        StringBuffer sb = new StringBuffer();
        String workflowText = null;
        if (exportSelectedWFOnly == true)
        {
            // Display only the selected workflows
            // The selected workflows are a space delimited string, so
            // use StringTokenizer
            tokenizer = new StringTokenizer(p_request.getParameter(JobManagementHandler.WF_ID));
            while (tokenizer.hasMoreTokens())
            {
                long selectedWfId = Long.parseLong(tokenizer.nextToken());
                for (int i=0; i < workflows.size(); i++)
                {
                    Workflow curWF = (Workflow)workflows.get(i);
                    if (curWF.getId() == selectedWfId)
                    {
                        workflowText = getWorkflowText(p_bundle,
                                                       p_uiLocale,
                                                       curWF,
                                                       p_request,
                                                       l10nProfile,
                                                       b_exportMultipleActivities);
                        sb.append(workflowText);
                        break;
                    }
                }
            }
        }
        else
        {
            // Display ALL the workflows in the job
            for (int i=0; i < workflows.size(); i++)
            {
                Workflow curWF = (Workflow)workflows.get(i);
                // skip DTP Workflow here
                if (WorkflowTypeConstants.TYPE_DTP.equals(curWF.getWorkflowType()))
                {
                    continue;
                }
                workflowText = getWorkflowText(p_bundle,
                                               p_uiLocale,
                                               curWF,
                                               p_request,
                                               l10nProfile,
                                               b_exportMultipleActivities);
                sb.append(workflowText);
            }
        }
        sb.append("<INPUT TYPE=\"hidden\" NAME=\"");
        sb.append(JobManagementHandler.JOB_ID);
        sb.append("\" VALUE=\"");
        sb.append(p_job.getId());
        sb.append("\">");
        
        return sb.toString();
    }

    private String getWorkflowText(ResourceBundle p_bundle,
                                   Locale p_uiLocale,
                                   Workflow curWF,
                                   HttpServletRequest p_request,
                                   L10nProfile l10nProfile,
                                   boolean b_exportMultipleActivities)
    throws EnvoyServletException
    {
        try
        {
            HttpSession session = p_request.getSession(false);
            PermissionSet userPerms = (PermissionSet) session.getAttribute(
                        WebAppConstants.PERMISSIONS);
            boolean hasPerm = userPerms.getPermissionFor(Permission.JOB_WORKFLOWS_EDITEXPORTLOC);
            List targetPages = curWF.getTargetPages();
            
            String ds = "";
            if (WorkflowTypeConstants.TYPE_DTP.equals(curWF.getWorkflowType()))
            {
                ds = WorkflowTypeConstants.TYPE_DTP;
            } else
            {
                ds = ((TargetPage)targetPages.get(0)).getDataSourceType(); 
            }             
            boolean isFileSystem = ds.startsWith(DataSourceType.FILE_SYSTEM); //could be fs or fsAutoImport
            boolean isDatabase = DataSourceType.DATABASE.equals(ds);
            boolean isMicrosoftOffice = false;
            boolean isNativeRtf = false;
            
            SystemConfiguration config = SystemConfiguration.getInstance();

            if (!isDatabase && !WorkflowTypeConstants.TYPE_DTP.equals(curWF.getWorkflowType()))
            {
                isMicrosoftOffice = JobExportHandler.isMicrosoftOffice((TargetPage)targetPages.get(0));
                isNativeRtf = JobExportHandler.isNativeRtf((TargetPage)targetPages.get(0));
            }

            long wfId = curWF.getId();
            StringBuffer sb = new StringBuffer();
            GlobalSightLocale targetLocale = curWF.getTargetLocale();
            if (!hasPerm) {
                sb.append("<INPUT TYPE=\"hidden\" ");
                sb.append("NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM);
                sb.append("_");
                sb.append(wfId);
                sb.append("\" VALUE=\"\\");
                if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("export"))
                   sb.append("export");
                else if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("language"))
                   sb.append(targetLocale.getLanguageCode());
                else
                   sb.append(targetLocale.toString());
                sb.append("\"");
            }

            sb.append("<TR CLASS=\"standardText\" BGCOLOR=\"#EEEEEE\">");
            // Checkbox
            sb.append("<TD WIDTH=\"20\">");
            if (JobExportHandler.isValidState(curWF.getState()))
            {
                String checkboxName = "";
                WorkflowTemplateInfo dtpWfti = (WorkflowTemplateInfo) curWF.getJob().getL10nProfile().getDtpWorkflowTemplateInfo(targetLocale);
                
                if (Workflow.LOCALIZED.equals(curWF.getState()) && dtpWfti != null)
                {
                    checkboxName = "dtpCheckbox";
                }
                else
                {
                    checkboxName = "transCheckbox";
                }

                sb.append("<INPUT TYPE=CHECKBOX ");
                sb.append("NAME=" + checkboxName + " ");
                sb.append("VALUE=" + wfId + " ");
                sb.append("CHECKED ONCLICK=\"checkWorkflowPages(this);\">"); 
            }
            sb.append("</TD>\r\n");

            // target locale
            sb.append("<TD><B>");            
            if (b_exportMultipleActivities)
              sb.append(curWF.getJob().getJobName());
            else
              sb.append(targetLocale.getDisplayName(p_uiLocale));
            sb.append("</B></TD>\r\n");            

            
            if (b_exportMultipleActivities == false)
            { //if EXPORTLOCANDCODESET 
            // export directory
            if (isFileSystem)
            {
                if (hasPerm) {
                    sb.append("<TD><SPAN CLASS=");
                    sb.append("\"formFields\">");
                    sb.append("<INPUT TYPE=\"text\" MAXLENGTH=\"1000\" SIZE=\"30\" ");
                    sb.append("NAME=\"");
                    sb.append(JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM);
                    sb.append("_");
                    sb.append(wfId);
                    sb.append("\" VALUE=\"\\");
                    if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("export"))
                       sb.append("export");
                    else if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("language"))
                       sb.append(targetLocale.getLanguageCode());
                    else
                      sb.append(targetLocale.toString());

                    sb.append("\" CLASS=\"formFields\"");

                    //must have export location edit to modify this value
                    if (userPerms.getPermissionFor(Permission.EXPORT_LOC_EDIT)==false)
                        sb.append(" READONLY");
                    sb.append("></SPAN>\r\n");
                }

				String companyId = String.valueOf(curWF.getJob().getCompanyId());
                Collection c = ServerProxy.getExportLocationPersistenceManager().getAllExportLocations(companyId);
                ExportLocation defaultExportLocation = ServerProxy.getExportLocationPersistenceManager().getDefaultExportLocation();

                Object[] exportLocs = null;
                if (c != null)
                    exportLocs = c.toArray();

                sb.append("</TD>\n<TD><SPAN CLASS=\"formFields\">\r\n");
                sb.append("<SELECT NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_LOCATION_PARAM);
                sb.append("_");
                sb.append(wfId);
                sb.append("\">\r\n");
                if (exportLocs==null || exportLocs.length == 0)
                {
                    //if there are no export locations at all, then at least show the CXE docs Directory
                    sb.append("<OPTION VALUE=\"");
                    sb.append(config.getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR));
                    sb.append("\">CXE Docs Dir</OPTION>");
                }
                else
                {
                    for (int j=0; j<exportLocs.length; j++)
                    {
                        ExportLocation el = (ExportLocation) exportLocs[j];
                        sb.append("<OPTION VALUE=\"");
                        sb.append(el.getLocation());

                        if (el.getId() == defaultExportLocation.getId())
                            sb.append("\" SELECTED>");
                        else
                            sb.append("\">");

                        sb.append(el.getName());
                        sb.append("</OPTION>\r\n");
                    }
                }
                sb.append("</SELECT></TD>\r\n");
            }
            else
            {
                sb.append("<TD><SPAN CLASS=");
                sb.append("\"standardText\">");
                sb.append(p_bundle.getString("lb_na"));
                sb.append("</TD>\r\n");

                //export location does not apply either
                sb.append("<TD><SPAN CLASS=");
                sb.append("\"standardText\">");
                sb.append(p_bundle.getString("lb_na"));
            }
            sb.append("</TD>\r\n");

            // Is Workflow Localized column
            sb.append("<TD>");
            if (curWF.getState().equals(Workflow.LOCALIZED) ||
                curWF.getState().equals(Workflow.EXPORTED) ||
                curWF.getState().equals(Workflow.ARCHIVED))
            {
                sb.append(p_bundle.getString("lb_yes"));
            }
            else
            {
                sb.append(p_bundle.getString("lb_no"));
            }
            sb.append("</TD>\r\n");

            // data encoding
            String codeset = l10nProfile.getCodeSet(targetLocale);
            //BOM Processing
            int BOMType = curWF.getJob().getFileProfile().getBOMType();
            ArrayList<FileProfile> fps = curWF.getJob().getAllFileProfiles();
            boolean needBOMProcessing = needBOMProcessing(fps);
            boolean isXlfSrcAsTrgEnabled = isXlfSrcAsTrgEnabled(fps);

            sb.append("<TD><SPAN CLASS=");
            if (!isDatabase)
            {
                sb.append("\"formFields\">");
                sb.append("<SELECT NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_CODE_PARAM);
                sb.append("_");
                sb.append(wfId);
                sb.append("\" CLASS=\"formFields\" onchange=\"selectBOM(this, " + wfId + ", " + needBOMProcessing + ")\">\r\n");

                if (isMicrosoftOffice)
                {
                    //only allow UTF8 for Office docs
                    sb.append("<OPTION SELECTED>UTF-8</OPTION>\r\n");
                }
                else if (isNativeRtf)
                {
                    //only allow ASCII for RTF docs
                    sb.append("<OPTION SELECTED>ASCII</OPTION>\r\n");
                }
                else
                {
                	boolean hasJsp = false;
                	boolean hasJavaProperties = false;
                    for (int i = 0; i < targetPages.size(); i++)
                    {
                        TargetPage page = (TargetPage)targetPages.get(i);
                        String name = page.getExternalPageId();
                        if (name.endsWith(".properties"))
                        {
                            hasJavaProperties = true;
                            //break;
                        }
                        if (name.endsWith(".jsp"))
                        {
                        	hasJsp = true;
                        }
                    }
                    addCodeSetOptions(codeset, targetLocale,sb,hasJavaProperties,hasJsp);
                }

                sb.append("</SELECT>");
            }
            else
            {
                sb.append("\"standardText\">");
                sb.append(codeset);
            }
            sb.append("</SPAN></TD>\r\n");
            if (!WorkflowTypeConstants.TYPE_DTP.equals(curWF.getWorkflowType()) &&
                    JobExportHandler.isUnextracted((TargetPage)targetPages.get(0)))
            {
                // if unextracted, disable the character encoding dropdown
                sb.append("<script>exportForm.");
                sb.append(JobManagementHandler.EXPORT_WF_CODE_PARAM);
                sb.append("_");
                sb.append(wfId);
                sb.append(".disabled=true;</script>");
            }
            
            sb.append("<TD><SPAN class=\"formField\">");
            sb.append("<SELECT id=\"bomType_" + wfId + "\" name=\"bomType_");
            sb.append(wfId);
            sb.append("\" class=\"standardText\" " + (needBOMProcessing ? "" : "disabled") + ">");
            sb.append("<option value='-1'>" + p_bundle.getString("lb_utf_bom_file_profile_setting") + "</option>");
            sb.append("<option value='1'>" + p_bundle.getString("lb_utf_bom_preserve") + "</option>");
            sb.append("<option value='2'>" + p_bundle.getString("lb_utf_bom_add") + "</option>");
            sb.append("<option value='3'>" + p_bundle.getString("lb_utf_bom_remove") + "</option>");
            sb.append("</SELECT>\r\n</SPAN></TD>\r\n");

            sb.append("<TD><SPAN class=\"formField\">");
            sb.append("  <SELECT id='xlfSrcAsTrg_" + wfId + "' name='xlfSrcAsTrg_" + wfId + "' class='standardText '" + (isXlfSrcAsTrgEnabled?"":"disabled") + ">");
            sb.append("    <option value='0'>No&nbsp;&nbsp;&nbsp;&nbsp;</option>");
            sb.append("    <option value='1'>Yes&nbsp;&nbsp;&nbsp;&nbsp;</option>");
            sb.append("  </SELECT>\r\n</SPAN></TD>\r\n");
            }
            else
            {
                sb.append("<TD>");
                sb.append(curWF.getJob().getSourceLocale().getDisplayName(p_uiLocale));
                sb.append("</TD>\r\n");

                // Is Workflow Localized column
                sb.append("<TD>");
                if (curWF.getState().equals(Workflow.LOCALIZED) ||
                    curWF.getState().equals(Workflow.EXPORTED) ||
                    curWF.getState().equals(Workflow.ARCHIVED))
                {
                    sb.append(p_bundle.getString("lb_yes"));
                }
                else
                {
                    sb.append(p_bundle.getString("lb_no"));
                }
                sb.append("</TD>\r\n");
            }

            if (b_exportMultipleActivities)
            {
                sb.append("<TD><SPAN CLASS=\"formFields\"><INPUT NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM);
                sb.append("_");
                sb.append(targetLocale.toString());
                sb.append("\" TYPE=\"TEXT\" VALUE=\"");
                sb.append("\\");
                if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("export"))
                  sb.append("export");
                else if (config.getStringParameter(SystemConfigParamNames.EXPORT_DIR_NAME_STYLE).equalsIgnoreCase("locale"))
                  sb.append(targetLocale.toString());
                else
                  sb.append(targetLocale.getLanguageCode());
                sb.append("\">");
                sb.append("</SPAN></TD>");
                
                sb.append("<TD><SPAN CLASS=\"formFields\"><SELECT NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_LOCATION_PARAM);
                sb.append("_");
                sb.append(targetLocale.toString());
                sb.append("\">");
                addExportLocationOptions(sb, p_bundle);
                sb.append("</SELECT></SPAN></TD>");

                int BOMType = curWF.getJob().getFileProfile().getBOMType();
                ArrayList<FileProfile> fps = curWF.getJob().getAllFileProfiles();
                boolean needBOMProcessing = needBOMProcessing(fps);
                boolean isXlfSrcAsTrgEnabled = isXlfSrcAsTrgEnabled(fps);
                
                sb.append("<TD><SPAN CLASS=\"formFields\"><SELECT NAME=\"");
                sb.append(JobManagementHandler.EXPORT_WF_CODE_PARAM);
                sb.append("_");
                sb.append(targetLocale.toString());
                sb.append("\" onchange=\"selectBOM(this, " + wfId + ", " + needBOMProcessing + ")\">");
                String codeSet = l10nProfile.getCodeSet(targetLocale);
                boolean hasJsp = false;
                boolean hasJavaProperties = false;
                for (int i = 0; i < targetPages.size(); i++)
                {
                    TargetPage page = (TargetPage)targetPages.get(i);
                    String name = page.getExternalPageId();
                    if (name.endsWith(".properties"))
                    {
                        hasJavaProperties = true;
                    }
                    if (name.endsWith(".jsp"))
                    {
                    	hasJsp = true;
                    }
                }
                addCodeSetOptions(codeSet, targetLocale,sb,hasJavaProperties,hasJsp);
                sb.append("</SELECT></SPAN></TD>");
                //BOM Processing
                sb.append("<TD><SPAN class=\"formField\">");
                sb.append("<SELECT id=\"bomType_" + wfId + "\" name=\"bomType_");
                sb.append(wfId);
                sb.append("\" class=\"standardText\" " + (needBOMProcessing ? "" : "disabled") + ">");
                sb.append("<option value='-1' selected>" + p_bundle.getString("lb_utf_bom_file_profile_setting") + "</option>");
                sb.append("<option value='1'>" + p_bundle.getString("lb_utf_bom_preserve") + "</option>");
                sb.append("<option value='2'>" + p_bundle.getString("lb_utf_bom_add") + "</option>");
                sb.append("<option value='3'>" + p_bundle.getString("lb_utf_bom_remove") + "</option>");
                sb.append("</SELECT>\r\n</SPAN></TD>\r\n");
                // 
		        sb.append("<TD><SPAN class=\"formField\">");
                sb.append("<SELECT id='xlfSrcAsTrg_" + wfId + "' name='xlfSrcAsTrg_" + wfId + "' class='standardText'>");
         		sb.append("<option value='0'>No&nbsp;&nbsp;&nbsp;&nbsp;</option>");
    	        sb.append("<option value='1'>Yes&nbsp;&nbsp;&nbsp;&nbsp;</option>");
                sb.append("</SELECT>\r\n</SPAN></TD>\r\n");
            }
            sb.append("</TR>\r\n");
            
            // Display the pages in the workflow
            sb.append(getContentItemText(curWF, p_bundle));

            return sb.toString();
        }
        catch (Exception e)
        {
            c_logger.error("Problem generating workflow text.",e);
            throw new EnvoyServletException(e);
        }

    }

    /**
     * Display the pages in the workflow and check them
     * all by default
     *
     * @param p_wf Workflow ID
     */
    private String getContentItemText(Workflow p_wf, ResourceBundle p_bundle)
    {
        if (WorkflowTypeConstants.TYPE_DTP.equals(p_wf.getWorkflowType()))
        {
            return "";
        }
        long wfId = p_wf.getId();
        StringBuffer sb = new StringBuffer();

        // display the Primary Target Pages (both extracted and unextracted)
        List pages = p_wf.getTargetPages();
        Set<SecondaryTargetFile> stfs = p_wf.getSecondaryTargetFiles();
        int stfSize = stfs.size();               
        String wfValue = null;

        if (pages.size() > 0)
        {
            wfValue = JobExportHandler.PRIMARY_PREFIX + wfId;

            // heading info
            if (stfSize > 0) {
                addTableHeadings(sb, wfId, wfValue,
                             p_bundle.getString("lb_primary_target_files"),
                             "CHECKED");
            }

            addInitialTableSettings(sb);
            
            sb.append("<thead id=scroll>");
            sb.append("<tbody>");
            for (int i = 0 ; i < pages.size() ; i++)
            {
                TargetPage curPage = (TargetPage)pages.get(i);
                boolean isUnextracted = curPage.getPrimaryFileType() ==
                                        PrimaryFile.UNEXTRACTED_FILE;
                String icon = isUnextracted ?
                              p_bundle.getString("img_file_unextracted") :
                              p_bundle.getString("img_file_extracted");
                String toolTip = isUnextracted ?
                                 p_bundle.getString("lb_file_unextracted") :
                                 p_bundle.getString("lb_file_extracted");

                preparePageRows(sb, curPage.getId(), wfValue,
                                "CHECKED", icon, toolTip,
                                curPage.getExternalPageId());
            }

            closeInnerTable(sb);
        }

        // Display secondary target files

        if (stfSize > 0)
        {
            wfValue = JobExportHandler.SECONDARY_PREFIX + wfId;
            // the length of wf id plus one is used for displaying
            // stf storage path without jobId and wf id prefix
            int wfValueLength = wfValue.length();

            sb.append("<TR></TR>\r\n");
            
            addTableHeadings(sb, wfId, wfValue,
                             p_bundle.getString("lb_secondary_target_files"),
                             "");
            
            addInitialTableSettings(sb);                             

            for (SecondaryTargetFile stf : stfs)
            {
                long stfId = stf.getId();

                String storagePath = stf.getStoragePath();
                int startIndex = storagePath.lastIndexOf(
                    String.valueOf(wfId)) + wfValueLength;

                preparePageRows(sb, stfId, wfValue, "DISABLED",
                                p_bundle.getString("img_file_unextracted"),
                                p_bundle.getString("lb_file_unextracted"),
                                storagePath.substring(startIndex));
            }

            sb.append("</tbody>");
            sb.append("</thead>");
            closeInnerTable(sb);
        }

        return sb.toString();
    }
    
    private void addCodeSetOptions(String p_codeSet,
        GlobalSightLocale p_locale, StringBuffer codeSetOptions, boolean hasJavaProperties, boolean hasJsp)
    throws EnvoyServletException
    {
        List allCodeSets = new ArrayList();
        try
        {
        	CodeSetImpl sameCode = new CodeSetImpl();
        	sameCode.setCodeSet(JobManagementHandler.SAME_AS_SOURCE);
             allCodeSets.add(sameCode);
           	 List codeSets = ServerProxy.getLocaleManager().getAllCodeSets();
           	 allCodeSets.addAll(codeSets);
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }

        Iterator it = allCodeSets.iterator();
        while (it.hasNext())
        {
            CodeSet codeSet = (CodeSet)it.next();
            codeSetOptions.append("<OPTION"
                                  + (p_codeSet.equals(codeSet.getCodeSet())
                                     ? " " + WebAppConstants.SELECTED
                                     : "")
                                  + ">" + codeSet.getCodeSet()
                                  + "</OPTION>\r\n");
        }
        if (hasJavaProperties)
        {
            codeSetOptions.append("<OPTION>Unicode Escape</OPTION>\r\n");
        }
        //
        if (hasJsp)   
        {
        	codeSetOptions.append("<OPTION>Entity Escape</OPTION>\r\n");
        }
    }
    
    //just adds the export location options to the string buffer for use with <SELECT>
    private void addExportLocationOptions(StringBuffer sb, ResourceBundle bundle) throws Exception
    {
        Collection c = ServerProxy.getExportLocationPersistenceManager().getAllExportLocations();
        ExportLocation defaultExportLocation = ServerProxy.getExportLocationPersistenceManager().getDefaultExportLocation();
        Object[] exportLocs = null;
                if (c != null)
                    exportLocs = c.toArray();
                
                if (exportLocs==null || exportLocs.length == 0)
                {
                    //if there are no export locations at all, then at least show the CXE docs Directory
                    SystemConfiguration config = SystemConfiguration.getInstance();
                    sb.append("<OPTION VALUE=\"");
                    sb.append(config.getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR));
                    sb.append("\">").append(bundle.getString("lb_cxe_docs_dir")).append("</OPTION>");
                }
                else
                {
                    for (int j=0; j<exportLocs.length; j++)
                    {
                        ExportLocation el = (ExportLocation) exportLocs[j];
                        sb.append("<OPTION VALUE=\"");
                        sb.append(el.getLocation());

                        if (el.getId() == defaultExportLocation.getId())
                            sb.append("\" SELECTED>");
                        else
                            sb.append("\">");

                        sb.append(el.getName());
                        sb.append("</OPTION>\r\n");
                    }
                }
    }
    
    private static final Logger c_logger = Logger
    .getLogger(JobExportHandler.class.getName());

    // "UTF-8 Byte Order Mark (BOM)" option only be available for "html","xml","resx". 
    private boolean needBOMProcessing(ArrayList<FileProfile> fps)
    {
        boolean needBOMProcessing = false;
        long knownFormatTypeId = -1;
        for (FileProfile fp : fps)
        {
            knownFormatTypeId = fp.getKnownFormatTypeId();
            needBOMProcessing = (knownFormatTypeId == 1 || knownFormatTypeId == 7 || knownFormatTypeId == 45);
            if (needBOMProcessing)
                break;
        }

        return needBOMProcessing;
    }

    // "Populate Source Into Un-Translated Target (XLF/XLZ only)
    private boolean isXlfSrcAsTrgEnabled(ArrayList<FileProfile> fps)
    {
        boolean isXlfSrcAsTrgEnabled = false;
        long knownFormatTypeId = -1;
        for (FileProfile fp : fps)
        {
            knownFormatTypeId = fp.getKnownFormatTypeId();
            isXlfSrcAsTrgEnabled = (knownFormatTypeId == 39 || knownFormatTypeId == 48 || knownFormatTypeId == 56);
            if (isXlfSrcAsTrgEnabled)
                break;
        }

        return isXlfSrcAsTrgEnabled;
    }
%>
