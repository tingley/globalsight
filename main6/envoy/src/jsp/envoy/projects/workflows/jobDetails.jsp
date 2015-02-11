<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.costing.Cost,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.jobhandler.JobImpl,
            com.globalsight.everest.costing.Money,
            com.globalsight.everest.costing.Currency,
            com.globalsight.everest.costing.CurrencyFormat,
            com.globalsight.everest.costing.FlatSurcharge,
            com.globalsight.everest.costing.PercentageSurcharge,
            com.globalsight.everest.costing.Surcharge,
            com.globalsight.everest.costing.BigDecimalHelper,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.PageComparator,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            com.globalsight.everest.workflowmanager.Workflow,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.AddSourceHandler,
            com.globalsight.everest.company.CompanyThreadLocal,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.modules.Modules,
            java.util.Date,
            java.util.Set,
            java.util.*,
            java.text.MessageFormat,
            com.globalsight.everest.foundation.User,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pending" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobComments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobAttributes" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetails" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="workflowActivities" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="workflowComments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="changeCurr" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="exportError" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="workflowImportError" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editPages" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editTotalSourcePageWc" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editFinalCost" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="surcharges" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="addWF" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rateVendor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="sourceEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="wordcountList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="pageList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="assign" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="skip" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="addSourceFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="allStatus" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 
<%
    String thisFileSearch = (String) request.getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);
   if (thisFileSearch == null)
   {
       thisFileSearch = "";
   }

   ResourceBundle bundle = PageHandler.getBundle(session);
   String selfURL = self.getPageURL();
   String pendingURL = pending.getPageURL();
   String detailsURL = jobDetails.getPageURL();
   String addWfURL = addWF.getPageURL();
   String exportErrorURL = exportError.getPageURL();
   String wfImportErrorURL = workflowImportError.getPageURL();
   String exportURL = export.getPageURL();
   String changeCurrencyURL = changeCurr.getPageURL();
   String editPagesURL = editPages.getPageURL();
   String downloadURL = download.getPageURL() + "&fromJobDetail=true";
   String editTotalSourcePageWcURL = editTotalSourcePageWc.getPageURL();
   String editExpensesFinalCostURL = editFinalCost.getPageURL()
                                    + "&"
                                    + JobManagementHandler.SURCHARGES_FOR
                                    + "=" + WebAppConstants.EXPENSES;
   String editRevenueFinalCostURL = editFinalCost.getPageURL()
                                    + "&"
                                    + JobManagementHandler.SURCHARGES_FOR
                                    + "=" + WebAppConstants.REVENUE;
   String expensesSurchargesURL = surcharges.getPageURL()
                                    + "&"
                                    + JobManagementHandler.SURCHARGES_FOR
                                    + "=" + WebAppConstants.EXPENSES;
   String revenueSurchargesURL = surcharges.getPageURL()
                                    + "&"
                                    + JobManagementHandler.SURCHARGES_FOR
                                    + "=" + WebAppConstants.REVENUE;
   String jobCommentsURL = jobComments.getPageURL()
                                    + "&" + JobManagementHandler.JOB_ID
                                    + "=" + request.getAttribute(JobManagementHandler.JOB_ID);
   String jobAttributesURL = jobAttributes.getPageURL() + "&" + JobManagementHandler.JOB_ID + "=" + request.getAttribute(JobManagementHandler.JOB_ID);
   String detailsTabURL = detailsURL + "&" + JobManagementHandler.JOB_ID
                                    + "=" + request.getAttribute(JobManagementHandler.JOB_ID);
   String rateVendorURL = rateVendor.getPageURL();
   String wordCountURL = wordcountList.getPageURL() + "&action=list";
   String modifyURL = modify.getPageURL();
   String editorUrl = editor.getPageURL();
   String sourceEditorUrl = sourceEditor.getPageURL();
   String pageListUrl = pageList.getPageURL() + "&" + JobManagementHandler.PAGE_SEARCH_PARAM + "=" + thisFileSearch;
   String assignURL = assign.getPageURL();
   String skipURL = skip.getPageURL();
   String addSourceFilesURL = addSourceFiles.getPageURL()+ "&jobId=" + request.getAttribute(JobManagementHandler.JOB_ID);
   String beforeAddDeleteSourceURL = addSourceFilesURL + "&action=" + AddSourceHandler.CAN_ADD_DELETE_SOURCE_FILES;
   String beforeDeleteSourceURL = addSourceFilesURL + "&action=" + AddSourceHandler.BEFORE_DELETE_SOURCE_FILES;
   String deleteSourceURL = addSourceFilesURL + "&action=" + AddSourceHandler.DELETE_SOURCE_FILES;
   String downloadSourceURL = addSourceFilesURL + "&action=" + AddSourceHandler.DOWNLOAD_SOURCE_FILES;
   String uploadSourceURL = addSourceFilesURL + "&action=" + AddSourceHandler.UPLOAD_SOURCE_FILES;
   String beforeAccessWorkflowURL = addSourceFilesURL + "&action=" + AddSourceHandler.CAN_UPDATE_WORKFLOW;
   String showDeleteProgressURL = addSourceFilesURL + "&action=" + AddSourceHandler.SHOW_DELETE_PROGRESS;
   String showUpdateProgressURL = addSourceFilesURL + "&action=" + AddSourceHandler.SHOW_UPDATE_PROGRESS;
   String checkPageExistURL = addSourceFilesURL + "&action=" + AddSourceHandler.CHECK_PAGE_EXIST;

   SessionManager sessionMgr = (SessionManager)session.getAttribute(
     WebAppConstants.SESSION_MANAGER);

   String currentCurrency = (String)session.getAttribute(JobManagementHandler.CURRENCY);
   String lbChangeCurrency = bundle.getString("lb_change_currency");
   String lbDetails = bundle.getString("lb_details");
   String lbJob = bundle.getString("lb_job");
   String lbPrevious = bundle.getString("lb_previous");
   String title = bundle.getString("lb_job_details");
   String workflowActivitiesURL = workflowActivities.getPageURL();
   String workflowCommentsURL = workflowComments.getPageURL();
   String xmlCurrency = (String)session.getAttribute(JobManagementHandler.CURRENCY_XML);
   String openSegmentComments = (String)sessionMgr.getAttribute(JobManagementHandler.OPEN_AND_QUERY_SEGMENT_COMMENTS);
   String closedSegmentComments = (String)sessionMgr.getAttribute(JobManagementHandler.CLOSED_SEGMENT_COMMENTS);
   
   // get the quotation email date
   String quoteDate = (String)sessionMgr.getAttribute(JobManagementHandler.QUOTE_DATE);

   // For "Quote proess webEx" isseue, get the Quotation Approved Date
   String quoteApprovedDate = (String)sessionMgr.getAttribute(JobManagementHandler.QUOTE_APPROVED_DATE);
   String quotePOnumber = (String)sessionMgr.getAttribute(JobManagementHandler.QUOTE_PO_NUMBER);
          quotePOnumber = quotePOnumber.replaceAll("\"","&quot;");
   User anthoriserUser = (User) sessionMgr.getAttribute(JobManagementHandler.AUTHORISER_USER);
   boolean hasReadyWorkflow = (Boolean)sessionMgr.getAttribute(JobManagementHandler.HAS_READY_WORKFLOW);
   String allReadyWorkflowsIds = (String)sessionMgr.getAttribute(JobManagementHandler.ALL_READY_WORKFLOW_IDS);
   
   // The quoteDate will be set default value "0000" when the quote has been edited by somebody.The quoteDate will be set system time when it has been confirmed by somebody.
   if(quoteApprovedDate != null && quoteApprovedDate.equals("0000")){
       quoteApprovedDate = null;
       anthoriserUser = null;
   }
   
   String labelDetails = bundle.getString("lb_details");
   String labelComments = bundle.getString("lb_comments");

   // used by the pageSearch include
   String lb_filter_text = bundle.getString("lb_source_file_filter");

   boolean jobCosting = ((Boolean)request.getAttribute(
      SystemConfigParamNames.COSTING_ENABLED)).booleanValue();
   boolean jobRevenue = ((Boolean)request.getAttribute(
      SystemConfigParamNames.REVENUE_ENABLED)).booleanValue();
   boolean displayStartDate = ((Boolean)request.getAttribute(
      SystemConfigParamNames.IS_DELL)).booleanValue();
   
   // For expenses
   Cost cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.COST_OBJECT);
   boolean isCostOverriden = false;
   int colspan = 8;
   PermissionSet perms = (PermissionSet) session.getAttribute(
                    WebAppConstants.PERMISSIONS);
   boolean costEditAllowed = perms.getPermissionFor(Permission.JOB_COSTING_EDIT);
   boolean costReEditAllowed = perms.getPermissionFor(Permission.JOB_COSTING_REEDIT);
   boolean dispatchWorkflow = perms.getPermissionFor(Permission.JOB_WORKFLOWS_DISPATCH);

   // Note that when all workflows of a job are discarded, cost would be null.
   if (jobCosting && cost != null)
   {
        isCostOverriden = cost.isOverriden();
        String removeCostOverrideURL = selfURL + "&" + JobManagementHandler.JOB_ID + "="  +
            request.getAttribute(JobManagementHandler.JOB_ID) +
            "&" + JobManagementHandler.REMOVE_OVERRIDE + "=true";
   }

   // for revenue
   Cost revenue = (Cost)sessionMgr.getAttribute(JobManagementHandler.REVENUE_OBJECT);
   boolean isRevenueOverriden = false;
   colspan = 11;

   // Note that when all workflows of a job are discarded, revenue would be null.
   if (jobCosting && revenue != null)
   {
        isRevenueOverriden = revenue.isOverriden();
        String removeRevenueOverrideURL = selfURL + "&" + JobManagementHandler.JOB_ID + "="  +
            request.getAttribute(JobManagementHandler.JOB_ID) +
            "&" + JobManagementHandler.REMOVE_OVERRIDE + "=true";
   }

   Job job = null;
   String jobState = "";
   String jobName = "";
   boolean b_jobIsFinished = false;
   boolean hasSetCostCenter = true;
   Long jobId = null;
   Long projectId = null;
   try
   {
       jobId =
           new Long((String)request.getAttribute(JobManagementHandler.JOB_ID));
       job = ServerProxy.getJobHandler().getJobById(jobId.longValue());
       jobName = job.getJobName();
       jobState = job.getState();
       projectId = job.getProjectId();

       //determine whether to lock down or not
       if (Job.EXPORTED.equals(jobState) ||
           Job.EXPORT_FAIL.equals(jobState) ||
           Job.LOCALIZED.equals(jobState) ||
           Job.ARCHIVED.equals(jobState))
       {
           b_jobIsFinished = true;
       }
       
       hasSetCostCenter = job.hasSetCostCenter();
   }
   catch(Exception e)
   {
       System.out.println("Error while getting Job Comments");
       e.printStackTrace();
   }

   int reimportOption = 0;
   try
   {
       reimportOption = Integer.parseInt(ServerProxy.getSystemParameterPersistenceManager().getSystemParameter(SystemConfigParamNames.REIMPORT_OPTION).getValue());
   }
   catch (Exception ge)
   {
       // assumes disabled.
   }

   boolean allowEditSourcePage = ((Boolean)request.getAttribute(
      WebAppConstants.GXML_EDITOR)).booleanValue();
   boolean canEditSourcePage = false;

   if (allowEditSourcePage)
   {
       canEditSourcePage = perms.getPermissionFor(Permission.SOURCE_PAGE_EDIT);

       // Can only edit pending or dispatched jobs...
       if (!Job.READY_TO_BE_DISPATCHED.equals(jobState) &&
           !Job.DISPATCHED.equals(jobState))
       {
           canEditSourcePage = false;
       }
       // ... where none of the workflows are localizaed already
       else
       {
           ArrayList workflows = new ArrayList(job.getWorkflows());

           for (int i = 0, max = workflows.size(); i < max; i++)
           {
               Workflow wf = (Workflow)workflows.get(i);

               if (wf.getState().equals(Workflow.LOCALIZED))
               {
                   canEditSourcePage = false;
                   break;
               }
           }
       }
   }
   
   String currentId = CompanyThreadLocal.getInstance().getValue();

   // Set which page to back to after drilling down into segment comments
   session.setAttribute("segmentCommentsBackPage","jobComments");
   
   //It's a very ugly implementation for multicompany prototype. Modify it in real version.
   boolean isSuperAdmin = UserUtil.isSuperAdmin(
        (String) request.getSession().getAttribute(WebAppConstants.USER_NAME));

   User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
   String userId = user.getUserId();   
   Hashtable delayTimeTable = (Hashtable)sessionMgr.getAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE);
   
   boolean isIE = request.getHeader("User-Agent").indexOf("MSIE")!=-1;
   boolean isFirefox = request.getHeader("User-Agent").indexOf("Firefox")!=-1;
   
   SystemConfiguration sysConfig = SystemConfiguration.getInstance();
   boolean useSSL = sysConfig.getBooleanParameter(SystemConfigParamNames.USE_SSL);
   String httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
   if (useSSL == true)
   {
       httpProtocolToUse = WebAppConstants.PROTOCOL_HTTPS;
   }
   else
   {
       httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
   }
   
   StringBuffer appletcontent = new StringBuffer();
   if(isIE){
       appletcontent.append("<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" width=\"920\" height=\"500\" ");
       appletcontent.append("NAME = \"FSV\" codebase=\"");
       appletcontent.append(httpProtocolToUse);
       appletcontent.append("://java.sun.com/update/1.6.0/jinstall-6-windows-i586.cab#Version=1,6\"> ");
   }
   else
   {
       appletcontent.append("<APPLET style=\"display:inline\" type=\"application/x-java-applet;jpi-version=1.6\" width=\"920\" height=\"500\" ");
       appletcontent.append("pluginspage=\"http://java.sun.com/products/plugin/index.html#download\"> ");
   }

   appletcontent.append("<PARAM NAME = \"code\" VALUE = \"com.globalsight.EditSourceApplet\" > ");
   appletcontent.append("<PARAM NAME = \"cache_option\" VALUE = \"Plugin\" > ");
   appletcontent.append("<PARAM NAME = \"cache_archive\" VALUE = \"/globalsight/applet/lib/SelectFilesApplet.jar,/globalsight/applet/lib/commons-codec-1.3.jar,/globalsight/applet/lib/commons-httpclient-3.0-rc2.jar,/globalsight/applet/lib/commons-logging.jar\" >");
   appletcontent.append("<PARAM NAME = NAME VALUE = \"FSV\"> ");
   appletcontent.append("<PARAM NAME = \"type\" VALUE=\"application/x-java-applet;version=1.6\"> ");
   appletcontent.append("<PARAM NAME = \"scriptable\" VALUE=\"true\"> ");
   appletcontent.append("<PARAM NAME = \"jobId\" value=\"" + jobId + "\"> ");
   appletcontent.append("<PARAM NAME = \"companyId\" value=\"" + currentId + "\"> ");
   appletcontent.append("<PARAM NAME = \"pageLocale\" value=\"" + bundle.getLocale() + "\"> ");
   appletcontent.append("<PARAM NAME = \"projectId\" value=\"" + projectId + "\"> ");
   appletcontent.append("<PARAM NAME = \"addToApplet\" value=\"MainAppletWillAddThis\"> ");
   
   if(isIE){
       appletcontent.append(" </OBJECT>");
   } else {
       appletcontent.append(" </APPLET>");
   }%>
<HTML>
<HEAD>
<!-- This JSP is envoy/projects/workflows/jobDetails.jsp -->
<TITLE><%= title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/ContextMenu.js"></script>
<script src="/globalsight/includes/ieemu.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/projects/workflows/changeCurrencyJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/constants.jspIncl" %>
<TITLE><%=title%></TITLE>
<style>
@import url(/globalsight/dijit/themes/tundra/attribute.css);
@import url(/globalsight/dojox/form/resources/FileUploader.css);
@import url(/globalsight/dijit/themes/tundra/ProgressBar2.css);

h1 {
    font:   menu;
	font-size: 1.5em; 
	font-weight: normal;
	line-height: 1em; 
	margin-top: 1em;
	margin-bottom:0;
}

h1 {
	font-size: 1.5em; 
	font-weight: normal;
	line-height: 1em; 
	margin-top: 1em;
	margin-bottom:0;
}
h2 { 
	font-size: 1.1667em; 
	font-weight: bold; 
	line-height: 1.286em; 
	margin-top: 1.929em; 
	margin-bottom:0.643em;
}
h3, h4, h5, h6 {
	font-size: 1em; 
	font-weight: bold; 
	line-height: 1.5em; 
	margin-top: 1.5em; 
	margin-bottom: 0;
}

.header2 {
    font-weight: normal; !important;
}

.dijitDialogTitle {
font-family:Arial, Helvetica, sans-serif;
}

.nomalFont {
font-family:Arial, Helvetica, sans-serif;
}

.tundra .dijitButtonText {
    width:100%;
    height:20px;
	text-align: center;
	padding: 0 0.3em;
}

.tundra .dijitDialog .dijitDialogPaneContent {
  background: #fff;
  border: none;
  border-top: 1px solid #d3d3d3;
  padding: 0px;
}

div.tableContainer {
<% 
    if (perms.getPermissionFor(Permission.JOB_COSTING_VIEW)) {
%>
	height:expression(this.scrollHeight>350?"330":"100%");
	max-height:330px; /* must be greater than tbody*/
<% } else { %> 
	height:expression(this.scrollHeight>185?"185":"100%");
    max-height:185px;  /* must be greater than tbody*/
<% }  %> 
    overflow: auto;
    }

table.scroll {
    width: 99%;     /*100% of container produces horiz. scroll in Mozilla*/
    border: solid 1px slategray;
    }

table.scroll>tbody  {  /* child selector syntax which IE6 and older do not support*/
    overflow: auto;
    height: auto;
    }

thead.scroll td.scroll  {
    position:relative;
    top: expression(document.getElementById("data").scrollTop-2); /*IE5+ only*/
    }
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/dojo/dojo.js" djConfig="parseOnLoad: true"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var w_viewer = null;
var w_addSourceFileWindow = null;
var rateVendorWindow = null;
var helpFile = "<%=bundle.getString("help_job_details")%>";

dojo.require("dijit.Dialog");
dojo.require("dijit.Dialog");
dojo.require("dijit.form.Button");
dojo.require("dojo.io.iframe");
dojo.require("dijit.ProgressBar");

function loadPage()
{
   ContextMenu.intializeContextMenu();
   // Load the Guide
   loadGuides();
   // Load the currency stuff
   doOnLoad();
 }

function openViewerWindow(url)
{	
	hideContextMenu();
    dojo.xhrPost(
    {
        url:"<%=checkPageExistURL%>" + url,
        handleAs: "text", 
        load:function(data)
        {
            if (data=="")
            {
            	if (w_viewer != null && !w_viewer.closed)
                {
                    w_viewer.focus();
                    return;
                }

                var style = "resizable=yes,top=0,left=0,height=" + (screen.availHeight - 60) + ",width=" + (screen.availWidth - 20);
                w_viewer = window.open('<%=editorUrl%>' + url, 'Viewer', style);
            }
            else
            {
            	showMsg3(data);
            }
        },
        error:function(error)
        {
        	showMsg(error.message);
        }
    });
}

function openGxmlEditor(url)
{
    hideContextMenu();
    if (w_viewer != null && !w_viewer.closed)
    {
        w_viewer.close();
    }
    window.location.href = ("<%=sourceEditorUrl%>" + url);
}


function doUnload()
{
    if (w_viewer != null && !w_viewer.closed)
    {
        w_viewer.close();
    }
    w_viewer = null;

    if (rateVendorWindow != null && !rateVendorWindow.closed)
    {
        rateVendorWindow.close();
    }
    rateVendorWindow = null;

    if (w_addSourceFileWindow != null && !w_addSourceFileWindow.closed)
    {
    	w_addSourceFileWindow.close();
    }
    w_addSourceFileWindow = null;
}

function openActivitiesWindow(url)
{
    activitiesWindow = window.open(url, 'activitiesWindow',
       'resizable,scrollbars=yes,top=0,left=0,height=700,width=800');
}


function openRateVendorWindow(url)
{
    rateVendorWindow = window.open(url, 'rateVendorWindow',
        'resizable,scrollbars=yes,top=0,left=0,height=500,width=1000');
}

function contextForPage(url, e)
{
    if(navigator.userAgent.indexOf("MSIE")==-1)
    {
    e.preventDefault();
    e.stopPropagation();
    }

    var popupoptions;

    var allowEditSource = eval('<%=allowEditSourcePage%>');
    var canEditSource = eval('<%=canEditSourcePage%>');

    if (allowEditSource)
    {
       popupoptions = [
         new ContextItem("<B><%=bundle.getString("lb_context_item_view_trans_status") %></B>",
           function(){ openViewerWindow(url);}),
         new ContextItem("<%=bundle.getString("lb_context_item_edit_src_page") %>",
           function(){ openGxmlEditor(url);}, !canEditSource)
       ];
    }
    else
    {
       popupoptions = [
         new ContextItem("<B><%=bundle.getString("lb_context_item_view_trans_status") %></B>",
           function(){ openViewerWindow(url);})
       ];
    }
    
    ContextMenu.display(popupoptions, e);
}

function hideContextMenu()
{
    document.getElementById("idBody").focus();
}

// According a check box to
function updateButtonStateByCheckBox(buttonId,objBox)
{
    var objButton =  document.getElementById(buttonId);
    if (objBox.checked)
	{
		objButton.disabled = false;
	}
	else
	{
		objButton.disabled = true;
	}
}

function send_email()
{
   if(confirm('<%=bundle.getString("msg_quote_ready_confirm")%>'))
   {
	setQuoteReadyDate();
	submitEmail();
   }
}

function submitEmail() 
{

  var quoteForm = document.getElementById("quoteForm");
	quoteForm.action = "<%=selfURL%>";
	quoteForm.submit();
}

// for ready quote date
function setQuoteReadyDate()
{
	var date = new Date();
	var myDate = getMyDate(date);
	document.getElementById("<%= JobManagementHandler.QUOTE_DATE %>").value = myDate;
}

// Get the date which has be formated
function getMyDate(dateObj)
{ 
	    var day = dateObj.getDate();
		var month = dateObj.getMonth() + 1;
		var year = dateObj.getYear();
		var hour = dateObj.getHours();
		var minute = dateObj.getMinutes();
		if (day < 10) 
		{
			day = "0" + day;
		}
		if (month < 10) 
		{
			month = "0" + month;
		}
		if (hour < 10) 
		{
			hour = "0" + hour;
		}
		if (minute < 10) 
		{
			minute = "0" + minute;
		}
		var time = hour + ":" + minute;
		var date = month + "/" + day + "/" + year + " " + time;
		return date;
}

// For "Quote process webEx" issue
// Set the Confirm Approved Quote Date
function confirmApproveQuote()
{
   var workflowForm = document.getElementById("workflowForm"); 
   
   if(confirm('<%=bundle.getString("msg_quote_approve_confirm")%>'))
   {
	   setApproveQuoteDate();
	   //submitEmail();
	   
	   var hasSetCostCenter = document.getElementById("hasSetCostCenter").value;
	   if (<%=dispatchWorkflow%>)
	   {
		   if ("false" == hasSetCostCenter)
		   {
			   alert("<%=bundle.getString("msg_cost_center_empty")%>");		   
		   }else if (<%=hasReadyWorkflow%> && confirm('<%=bundle.getString("msg_dispatch_all_workflow_confirm")%>'))
		   {
	 		   workflowForm.action = "<%=detailsURL%>";
			   workflowForm.action += "&" +
			       "<%=JobManagementHandler.DISPATCH_ALL_WF_PARAM%>=true&" +
			       "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value + 
			       "&<%=JobManagementHandler.ALL_READY_WORKFLOW_IDS%>=<%=allReadyWorkflowsIds%>";
		   }
	   }

	   
	   var quoteForm = document.getElementById("quoteForm");
   	 quoteForm.action = "<%=selfURL%>";
       //When user selects dispatch all workflows in the same time,add the action for "workflowForm" to current form,
       //then handler will handle them both once.
	   if (workflowForm.action.length > 0)
	   {
		   quoteForm.action += "&workflowFormAction=" + workflowForm.action;
	   }
	   
	   quoteForm.submit();
   }
}

function dispatchAllWorkflow()
{
	workflowForm.action = "<%=detailsURL%>";
    workflowForm.action += "&" +
    "<%=JobManagementHandler.DISPATCH_ALL_WF_PARAM%>=true&" +
    "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value + 
    "&<%=JobManagementHandler.ALL_READY_WORKFLOW_IDS%>=<%=allReadyWorkflowsIds%>";
    alert("workflowForm.action :: " + workflowForm.action);
    workflowForm.submit();
}

function setApproveQuoteDate()
{
   var date = new Date();
   var myDate = getMyDate(date);
   document.getElementById('<%= JobManagementHandler.QUOTE_APPROVED_DATE %>').value = myDate;
   document.getElementById('<%= JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG %>').value = true;
}
 
// Save the Quote Po Number
var PoNumberIsChanged = false;
function saveQuotePoNumber()
{
   var quotePoNumber = document.getElementById("POnumber").value;
   var quoteForm = document.getElementById("quoteForm");   
	   if (PoNumberIsChanged)
       {
	         if(confirm('<%=bundle.getString("msg_save_po_number_confirm")%>'))
	         {
		         quoteForm.action = "<%=selfURL%>"+"&<%= JobManagementHandler.QUOTE_PO_NUMBER %>=" + URLencode(quotePoNumber);
		         quoteForm.submit();
		     }
       }
       else
       {
          alert('<%=bundle.getString("msg_validate_po_number")%>');
          return false;
       }
   
} 

function URLencode(sStr) 
{
	return escape(sStr).replace(/\+/g, '%2B').replace(/\"/g,'&quote;').replace(/\'/g, '%27').replace(/\//g,'%2F');
}

function submitForm(buttonClicked)
{
	var pagesForm = document.forms["pagesForm"];
	var workflowForm = document.forms["workflowForm"];
    dojo.xhrPost(
    	    {
    	        url:"<%=beforeAccessWorkflowURL%>",
    	        handleAs: "text", 
    	        load:function(data)
    	        {
    	            if (data=="")
    	            {
    	            	realSubmitForm(buttonClicked, workflowForm, pagesForm);
    	            }
    	            else
    	            {
    	            	showMsg(data);
    	            }
    	        },
    	        error:function(error)
    	        {
    	        	showMsg(error.message);
    	        }
    	    });
}

function realSubmitForm(buttonClicked,workflowForm, pagesForm){
    if (buttonClicked == "PageError"){
       pagesForm.action = "/globalsight/ControlServlet?linkName=error&pageName=WF1&jobId=<%=request.getAttribute(JobManagementHandler.JOB_ID)%>&fromDetails=true";
       pagesForm.submit();
       return true;
    }else if (buttonClicked == "Pending"){
       workflowForm.action = "<%=pendingURL%>";
       workflowForm.submit();
       return true
    }else if (buttonClicked == "AddWF"){
    	<%if(job.hasPassoloFiles()){%>
        alert("<%=bundle.getString("jsmsg_cannot_add_passolo_workflow")%>");
        return;
        <%} else if(jobState.equals(Job.PENDING) || jobState.equals("IMPORT_FAILED")){%>
            // a pending workflow cannot be modified
            alert("<%=bundle.getString("jsmsg_cannot_add_pending_workflow")%>");
            return;
        <%}%>
        workflowForm.action = "<%=addWfURL%>&<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
    }
    else
    {
       // Note that when all workflows of a job are discarded, there'll be no more 'wfId'.
       if (workflowForm.wfId == null ||
           !isRadioChecked(workflowForm.wfId) )
       {
          return false;
       }
    }

   var valuesArray;
   var wfId = "";
   var wfState = "";
   var wfIsEditable = "";
   var exportErrorWorkflowSelected;
   var importErrorWorkflowSelected;

   // If more than one checkbox is displayed, loop
   // through the array to find the one checked
   if (workflowForm.wfId.length)
   {
      for (i = 0; i < workflowForm.wfId.length; i++)
      {
         if (workflowForm.wfId[i].checked == true)
         {
            if( wfId != "" )
            {
               wfId += " "; // must add a [white space] delimiter
               wfState += " "; // add a whitespace for debugging readability
               wfIsEditable += " "; // add a whitespace for debugging readability
            }
            valuesArray = getRadioValuesWf(workflowForm.wfId[i].value);
            wfId += valuesArray[0];
            wfState += valuesArray[1];
            wfIsEditable += valuesArray[2];

            if (wfState.indexOf("EXPORT_FAILED") != -1)
            {
                exportErrorWorkflowSelected = true;
            }
            else if (wfState.indexOf("IMPORT_FAILED") != -1)
            {
               importErrorWorkflowSelected = true;
            }
         }
      }
   }
   // If only one checkbox is displayed, there is no checkbox array, so
   // just check if the single checkbox is checked
   else
   {
      if (workflowForm.wfId.checked == true)
      {
         valuesArray = getRadioValuesWf(workflowForm.wfId.value);
         wfId += valuesArray[0];
         // Add a space so we when we're looking for workflows of state
         // DISPATCHED, we don't also pick up READY_TO_BE_DISPATCHED
         wfState += " " + valuesArray[1];
         wfIsEditable += valuesArray[2];

         if (wfState.indexOf("EXPORT_FAILED") != -1)
         {
             exportErrorWorkflowSelected = true;
         }
         else if (wfState.indexOf("IMPORT_FAILED") != -1)
         {
            importErrorWorkflowSelected = true;
         }
      }
   }

   if (buttonClicked == "Details")
   {
      workflowForm.action = "<%=workflowActivitiesURL%>";
      var url = workflowForm.action + "&" +
         "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
         "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
      openActivitiesWindow(url);

      // Don't submit form since we'll display the response
      // in a pop-up window
      return false;
   }
   else if (buttonClicked == "Rate")
   {
      openRateVendorWindow("<%=rateVendorURL%>&action=rate&wfId=" + wfId);
      // Don't submit form since we'll display the response
      // in a pop-up window
      return false;
   }
   else if (buttonClicked == "UpdateWordCounts")
   {
	   workflowForm.action = "<%=selfURL %>" + "&" +
	       "<%=JobManagementHandler.UPDATE_WORD_COUNTS%>=yes" + "&" +
	       "<%=JobManagementHandler.WF_ID%>=" + wfId;
   }
   else if (buttonClicked == "WordCount")
   {
      workflowForm.action = "<%=wordCountURL%>" + "&" +
         "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
         "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
   }
   else if (buttonClicked == "ReAssign")
   {
     if(wfState.indexOf("READY_TO_BE_DISPATCHED") == -1 && wfState.indexOf("DISPATCHED") == -1)
     {
         alert("<%=bundle.getString("jsmsg_cannot_reassign_workflow")%>");         
         return false;
     }
     
     workflowForm.action = "<%=assignURL%>" + "&" +
         "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
         "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
     
   }
   else if (buttonClicked == "skip")
   {
      if(wfState.indexOf("READY_TO_BE_DISPATCHED") != -1 || wfState.indexOf("DISPATCHED") != -1 
	) 
      {
	workflowForm.action = "<%=skipURL%>" + "&" +
	   "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
	   "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
      

	} else {

	  alert("<%=bundle.getString("jsmsg_cannot_skip")%>");
	  return;
	}
   }
   else if (buttonClicked == "Edit")
   {
      if (wfIsEditable == "false")
      {

         if (wfState.indexOf("PENDING") != -1 ||
             wfState.indexOf("IMPORT_FAILED") != -1)
         {
            // a pending workflow cannot be modified
            alert("<%=bundle.getString("jsmsg_cannot_edit_pending_workflow")%>");
         }
         else
         {
            // You cannot edit this workflow because you are not a PM or
            // the wf is completed
            alert("<%=bundle.getString("jsmsg_cannot_edit_workflow")%>");
         }
         return false;
      }

      workflowForm.action = "<%=modifyURL%>";
      workflowForm.action += "&" +
         "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
         "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
   }
   else if (buttonClicked == "Discard")
   {

      // You can only discard workflows that are in the DISPATCHED,
      // IMPORT_FAIL or READY_TO_BE_DISPATCHED states.
      if (wfState.indexOf("ARCHIVED") != -1 ||
          wfState.indexOf("BATCH_RESERVED") != -1 ||
          wfState.indexOf("EXPORTED") != -1 ||
          wfState.indexOf("EXPORT_FAILED") != -1 ||
          wfState.indexOf("LOCALIZED") != -1 ||
          wfState.indexOf("PENDING") != -1)
      {
         // You can only discard workflows that are...DISPATCHED, READY_TO_BE_DISPATCHED
         alert("<%=bundle.getString("jsmsg_cannot_discard_workflow")%>");
         return false;
      }


      // Warning!! This will discard the wf from the system...
      if (confirm("<%=bundle.getString("jsmsg_warning")%>" +
                  "\n\n" +
                  "<%=bundle.getString("jsmsg_discard_workflow")%>"))
      {
            workflowForm.action = "<%=detailsURL%>" + "&" +
                "<%=JobManagementHandler.DISCARD_WF_PARAM%>=true&" +
                "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
                "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;

            // Add flag if it's the last workflow.  The controlhelper will
            // want to go to a different destination page in that case.
            if (!workflowForm.wfId.length)
            {
                workflowForm.action += "&lastWF=true";
            }
      }
      else
      {
         return false;
      }
   }
   else if (buttonClicked == "Dispatch")
   {
	   var hasSetCostCenter = document.getElementById("hasSetCostCenter").value;
	   if ("false" == hasSetCostCenter)
	   {
		   alert("<%=bundle.getString("msg_cost_center_empty")%>");
		   return;
	   }
	   
      workflowForm.action = "<%=detailsURL%>";
      // You can only dispatch workflows that are in the READY_TO_BE_DISPATCHED,
      // state.
      // If user just select one workflow and its state is READY_TO_BE_DISPATCHED,
      // there needs add a leading space 
      var tmpState = " " + wfState;
      if (tmpState.indexOf("ARCHIVED") != -1 ||
          tmpState.indexOf("BATCH_RESERVED") != -1 ||

          // Add a leading space so we don't pick up READY_TO_BE_DISPATCHED
          tmpState.indexOf(" DISPATCHED") != -1 ||

          tmpState.indexOf("EXPORTED") != -1 ||
          tmpState.indexOf("EXPORT_FAILED") != -1 ||
          tmpState.indexOf("IMPORT_FAILED") != -1 ||
          tmpState.indexOf("LOCALIZED") != -1 ||
          tmpState.indexOf("PENDING") != -1)
      {
         // You can only dispatch workflows that are...READY_TO_BE_DISPATCHED
         alert("<%=bundle.getString("jsmsg_cannot_dispatch_workflow")%>");
         return false;
      }

      workflowForm.action += "&" +
         "<%=JobManagementHandler.DISPATCH_WF_PARAM%>=true&" +
         "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
         "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
   }
   else if (buttonClicked == "sendingbackEditionJob")
   {
       workflowForm.action = "<%=selfURL%>" + "&" + "action=sendingbackEditionJob&" + 
           "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
           "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
   }
   else if (buttonClicked == "Archive")
   {
      workflowForm.action = "<%=detailsURL%>";

      // You can only archive workflows that are in the EXPORTED
      // state.
      if (wfState.indexOf("ARCHIVED") != -1 ||
          wfState.indexOf("BATCH_RESERVED") != -1 ||
          wfState.indexOf("DISPATCHED") != -1 ||
          wfState.indexOf("EXPORT_FAILED") != -1 ||
          wfState.indexOf("IMPORT_FAILED") != -1 ||
          wfState.indexOf("LOCALIZED") != -1 ||
          wfState.indexOf("PENDING") != -1 ||
          wfState.indexOf("READY_TO_BE_DISPATCHED") != -1)
      {
         // You can only archive workflows that are...EXPORTED
         alert("<%=bundle.getString("jsmsg_cannot_archive_workflow")%>");
         return false;
      }

      workflowForm.action += "&" +
         "<%=JobManagementHandler.ARCHIVE_WF_PARAM%>=true&" +
         "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
         "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
   }
   else if (buttonClicked == "ViewError")
   {
      if (!exportErrorWorkflowSelected &&
          !importErrorWorkflowSelected)
      {
         alert("<%=bundle.getString("jsmsg_workflow_has_no_errors")%>");
         return false;
      }
      else if (exportErrorWorkflowSelected)
      {
         workflowForm.action = "<%=exportErrorURL%>" + "&" +
            "<%=JobManagementHandler.ERROR_WF_PARAM%>=" + wfId + "&" +
            "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
      }
      else  // import error selected
      {
         workflowForm.action= "<%=wfImportErrorURL%>" + "&" +
            "<%=JobManagementHandler.ERROR_WF_PARAM%>=" + wfId + "&" +
            "<%=JobManagementHandler.JOB_ID%>=" + workflowForm.jobId.value;
      }
   }
   else if (buttonClicked == "Export")
   {
      // You cannot export workflows in the following state
      // because no work has been done on the pages, the workflow
      // has not started.
      if (wfState.indexOf("BATCH_RESERVED") != -1 ||
          wfState.indexOf("PENDING") != -1 ||
          wfState.indexOf("READY_TO_BE_DISPATCHED") != -1 ||
          wfState.indexOf("IMPORT_FAILED") != -1)
      {
         // You can only archive workflows that are...EXPORTED
         alert("<%=bundle.getString("jsmsg_cannot_export_workflow")%>");
         return false;
      }

      workflowForm.action = "<%=exportURL%>";
      workflowForm.action += "&" +
         "<%=JobManagementHandler.WF_ID%>=" + wfId + "&" +
         "<%=JobManagementHandler.EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM%>=true";
   }
   else if (buttonClicked == "Download")
   {
      var user_id = "<%=userId%>";
      var job_id = "<%=jobId%>";
      var delayTimeTableKey = user_id + job_id + wfId;
      checkDelayTime(delayTimeTableKey, wfId);
      return false;
   }
   else if (buttonClicked == "priority") {
       workflowForm.action = "<%=detailsURL%>&" +
           "changePriority=true&" + "wfId=" + wfId;
   }

   workflowForm.submit();
}

var jsStartTimesArray = new Array();
<%
    if(delayTimeTable != null)
    {
         Enumeration elements = delayTimeTable.keys();                 
                   while(elements.hasMoreElements())
                   {
                      String key = (String)elements.nextElement();
                      Date DateValue = (Date)delayTimeTable.get(key);
                      long workflowExportStartTime = DateValue.getTime(); 
%>
                      jsStartTimesArray["<%=key%>"] = <%=workflowExportStartTime%>;
<%
                   }
    }
%>
function checkDelayTime(key, wfId)
{   
	var start_time = 0;
    if(key in jsStartTimesArray)
    {
        start_time = jsStartTimesArray[key];
    } 
    //send request to get current time from server.
    dojo.xhrPost(
    {
    	url:"<%=selfURL%>&<%=JobManagementHandler.OBTAIN_TIME%>=true",
    	handleAs: "text", 
    	load:function(dateString)
    	{
    	    if (dateString != "")
    	    {
        	    var currentTime = parseInt(dateString);
        	    var usedTime = (currentTime - start_time)/1000;
        	    var delayTime = <%=sessionMgr
        									.getAttribute(SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME)%>;
        	    var leftTime = parseInt(delayTime - usedTime);
        	    if(leftTime > 0)
        	    {
        	        alert("<%=bundle.getString("msg_task_download_time")%>".replace("%1", delayTime).replace("%2", leftTime));
        	    }
        	    else
        	    {
        	    	workflowForm.action = "<%=downloadURL%>&firstEntry=true&<%=DownloadFileHandler.PARAM_JOB_ID%>" +
                    "=<%=jobId%>&<%=DownloadFileHandler.PARAM_WORKFLOW_ID%>=" + wfId;
        	    	workflowForm.submit();
        	    }
    	    }     
    	 },
    	 error:function(error)
    	 {
    	    alert(error.message);
    	 }
    });
}

function showPriorityDiv(wfId)
{
<% if (!perms.getPermissionFor(Permission.JOB_WORKFLOWS_PRIORITY)) {%>
	return;
<% } %>
   //select "Check All"
   if (wfId == 'workflowForm_checkAll')
   {
	   var form = eval("document.workflowForm");
	   for (var i = 0; i < form.elements.length; i++)
	   {
		if (form.elements[i].type == "checkbox")
	        {
	            var wf_id = form.elements[i].id.substring(5);
	            var prioritySelect = document.getElementById("prioritySelect" + wf_id);
	            var priorityLabel = document.getElementById("priorityLabel" + wf_id);
	            prioritySelect.style.display = "block";
	            priorityLabel.style.display = "none";
	        }
	   }
   }
   //select "Clear All"
   else if (wfId == 'workflowForm_clearAll')
   {
	   var form = eval("document.workflowForm");
	   for (var i = 0; i < form.elements.length; i++)
	   {
			if (form.elements[i].type == "checkbox")
	        {
	            var wf_id = form.elements[i].id.substring(5);
	            var prioritySelect = document.getElementById("prioritySelect" + wf_id);
	            var priorityLabel = document.getElementById("priorityLabel" + wf_id);
	            prioritySelect.style.display = "none";
	            priorityLabel.style.display = "block";
	        }
	   }
   }
   //select one by one
   else 
   {
	   var currentCheckbox = document.getElementById("wfId_" + wfId);
	   
	   var prioritySelect = document.getElementById("prioritySelect" + wfId);
	   var priorityLabel = document.getElementById("priorityLabel" + wfId);

	   if (currentCheckbox.type == "checkbox")
	   {
		   if ( currentCheckbox.checked == true)
		   {
	           prioritySelect.style.display = "block";
	           priorityLabel.style.display = "none";
		   }
		   else 
		   {
               prioritySelect.style.display = "none";
	           priorityLabel.style.display = "block";
		   }
	   }
   }
}

function setButtonState()
{
   var workflowState;
   var isSkipDisable = false;
   
   j = 0;
   if (workflowForm.wfId.length)
   {
      for (i = 0; i < workflowForm.wfId.length; i++)
      {
      	 var checkBoxObj = workflowForm.wfId[i];
         if (checkBoxObj.checked == true)
         {
         	var workflowId = checkBoxObj.id.substring(checkBoxObj.id.lastIndexOf("_") + 1);
         	workflowState = document.getElementById("currentWorkflowState_" + workflowId).innerHTML.replace(/(^\s*)|(\s*$)/g,"");
         	if(workflowState != 'DISPATCHED' && workflowState != 'READY_TO_BE_DISPATCHED')
         	{
         		isSkipDisable = true;
         	}
            j++;
         }
      }
   }

   if (j > 1)
   {
      if (document.workflowForm.Details)
          document.workflowForm.Details.disabled = true;
      if (document.workflowForm.Edit)
          document.workflowForm.Edit.disabled = true;
      if (document.workflowForm.ViewError)
          document.workflowForm.ViewError.disabled = true;
      if (document.workflowForm.changePriority)
          document.workflowForm.changePriority.disabled = true;
          
      <% if (Modules.isVendorManagementInstalled()) { %>
          if (document.workflowForm.Rate)
              document.workflowForm.Rate.disabled = true;
      <% } %>
      if (document.workflowForm.ReAssign)
          document.workflowForm.ReAssign.disabled = true;
      
      if (document.workflowForm.Download)
          document.workflowForm.Download.disabled = true;
	   if(isSkipDisable)
	   {
	   		if(document.workflowForm.skip){
	   			document.workflowForm.skip.disabled = true;
	   		}
	   }
	   else
	   {
	     	if(document.workflowForm.skip){
	   			document.workflowForm.skip.disabled = false;
	   		}
	   }
          
   }
   else
   {
      if (document.workflowForm.Details)
          document.workflowForm.Details.disabled = false;
      if (document.workflowForm.Edit)
          document.workflowForm.Edit.disabled = false;
      if (document.workflowForm.ViewError)
          document.workflowForm.ViewError.disabled = false;
      if (document.workflowForm.changePriority)
          document.workflowForm.changePriority.disabled = false;
          
      <% if (Modules.isVendorManagementInstalled()) { %>
          if (document.workflowForm.Rate)
              document.workflowForm.Rate.disabled = false;
      <% } %>
      if (document.workflowForm.ReAssign)
         document.workflowForm.ReAssign.disabled = false;
      if (document.workflowForm.Download)
          document.workflowForm.Download.disabled = false;
      if(document.workflowForm.skip){
		  document.workflowForm.skip.disabled = false;
	  }
   }
}

function getJobCookie()
{
    var lastjobs = "";
    var cookieName = "<%=JobSearchConstants.MRU_JOBS_COOKIE%>" +
                     "<%=session.getAttribute(WebAppConstants.USER_NAME).hashCode()%>";
    if (document.cookie.length > 0)
    {
        offset = document.cookie.indexOf(cookieName);
        if (offset != -1)
        {
            offset += cookieName.length + 1;
            end = document.cookie.indexOf(";", offset);
            if (end == -1)
            {
                end = document.cookie.length;
            }
            var lastjobs = unescape(document.cookie.substring(offset, end));
        }
    }
    return lastjobs;
}

function setJobCookie(lastJobs)
{
    var today = new Date();
    var expires = new Date(today.getTime() + (90 * 86400000));  //90 days
    var cookieName = "<%=JobSearchConstants.MRU_JOBS_COOKIE%>" +
                     "<%=session.getAttribute(WebAppConstants.USER_NAME).hashCode()%>" + "=";
    document.cookie = cookieName + lastJobs + ";EXPIRES=" + expires.toGMTString() + ";PATH=" + escape("/");
}

var windownum = 1;
function popup(url, target)
{
   target = target + parent.windx + windownum++;
   parent.windx++;
   var newurl = url+'&target=' + target;
   window.open(newurl,target,config='height=500,width=700,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
};

function confirmCostChange()
{
   <% if (b_jobIsFinished) { %>
   if (confirm("<%=bundle.getString("jsmsg_costing_lockdown")%>"))
   {
      return true;
   }
   else
   {
      return false;
   }
   <% } %>
}

function addSourceFiles()
{
    dojo.xhrPost(
    {
        url:"<%=beforeAddDeleteSourceURL%>",
        handleAs: "text", 
        load:function(data)
        {
            if (data=="")
            {
            	openAddSourceFilesWindow();
            }
            else
            {
            	showMsg(data);
            }
        },
        error:function(error)
        {
        	showMsg(error.message);
        }
    });
}

function openAddSourceFilesWindow()
{

	var div = dijit.byId('addSourceDiv');
	div.show();
	dojo.byId('appletDiv').innerHTML = '<%=appletcontent.toString()%>';
}

function removeSourceFiles()
{	
	var pIds = getSelectPageIds();
	
	if (pIds.length == 0)
	{
		showMsg('<%=bundle.getString("msg_no_file_remove")%>');
		return;
    }

	var obj = {
		pIds : pIds
	}
	
    dojo.xhrPost(
    {
        url:"<%=beforeDeleteSourceURL%>",
        handleAs: "text", 
        content:obj,
        load:function(data)
        {
            if (data=="")
            {
                if (confirm('<%=bundle.getString("msg_confirm_remove")%>'))
                {
                	doRemoveFiles(pIds);
                }
            }
            else
            {               
            	var returnData = eval(data);
                if (returnData.error)
                {
                	showMsg(returnData.error);
                }
                else if (returnData.confirm && confirm(returnData.confirm))
                {
                	doRemoveFiles(pIds);
                }
            }
        },
        error:function(error)
        {
        	showMsg(error.message);
        }
    });
}

function getSelectPageIds()
{
	var pIds = document.getElementsByName("pageIds");
	var selectIds = "";
	for (var i = 0; i < pIds.length; i++)
    {
        if (pIds[i].checked)
        {
           if (selectIds.length > 0)
           {
           	selectIds = selectIds.concat(",");
           }
           
           selectIds = selectIds.concat(pIds[i].value);
        }
    }

    return selectIds;
}

function doRemoveFiles(pIds)
{

	var  randomNum = new Date().getTime() + Math.floor(Math.random()*10000+1);
    dojo.xhrPost(
    	    {
    	        url:"<%=deleteSourceURL%>" + "&pageIds=" + pIds + "&randomNum=" + randomNum,
    	        handleAs: "text", 
    	        load:function(data)
    	        {
        	        closeMsg2();
        	        
    	            if (data=="")
    	            {
    	            	refreshJobPage();
    	            }
    	            else
    	            {
    	            	var returnData = eval(data);
    	            	if (returnData.discard != null)
    	            	{
    	            		location.replace('<%=allStatus.getPageURL()%>');
            	        }
    	            	else
    	            	{
    	            		showMsg(data);
            	        }
    	            }
    	        },
    	        error:function(error)
    	        {
    	        	closeMsg2();
    	        	showMsg(error.message);
    	        }
    	    });

    dojo.byId('progressMsg').innerHTML='<%=bundle.getString("msg_wait_delete_finish")%>';
    showDeleteProgress(0, randomNum);
}

function downloadFiles()
{
	var pIds = getSelectPageIds();
	if (pIds.length == 0)
	{
		showMsg('<%=bundle.getString("msg_no_file_remove")%>');
		return;
    }
    
	downLoadForm.action = '<%=downloadSourceURL%>' + "&pageIds=" + pIds;	
	downLoadForm.submit();
}

function openUploadFile()
{
    dojo.xhrPost(
    	    {
    	        url:"<%=beforeAddDeleteSourceURL%>",
    	        handleAs: "text", 
    	        load:function(data)
    	        {
    	            if (data=="")
    	            {
    	            	dijit.byId('uploadFormDiv').show();
    	            }
    	            else
    	            {
    	            	showMsg(data);
    	            }
    	        },
    	        error:function(error)
    	        {
    	        	showMsg(error.message);
    	        }
    	    });
}

function showUpdateProgress(num, randomNum)
{
	var obj = {
		number : num,
		randomNum : randomNum
	}
	
	dojo.xhrPost(
    {
 	        url:"<%=showUpdateProgressURL%>",
 	        content:obj,
 	        handleAs: "text", 
 	        load:function(data)
 	        {
 	            if (data=="")
 	            {
 	            	
 	            }
 	            else
 	            {
 	            	var returnData = eval(data);
 	            	dijit.byId('theBar').update({ maximum: returnData.total, progress:returnData.number });
 	            	dijit.byId('progressDialog').show();
 	            	showUpdateProgress(returnData.number, randomNum);
 	            }
 	        },
 	        error:function(error)
 	        {
 	        	
 	        }
    });
}

function showDeleteProgress(num, randomNum)
{
	var obj = {
		number : num,
		randomNum : randomNum
	}
	
	dojo.xhrPost(
    {
 	        url:"<%=showDeleteProgressURL%>",
 	        content:obj,
 	        handleAs: "text", 
 	        load:function(data)
 	        {
 	            if (data=="")
 	            {
 	            	
 	            }
 	            else
 	            {
 	            	var returnData = eval(data);
 	            	dijit.byId('theBar').update({ maximum: returnData.total, progress:returnData.number });
 	            	dijit.byId('progressDialog').show();
 	            	showDeleteProgress(returnData.number, randomNum);
 	            }
 	        },
 	        error:function(error)
 	        {
 	        	
 	        }
    });
}

function uploadFile() 
{
	var  randomNum = new Date().getTime() + Math.floor(Math.random()*10000+1);
	
    dojo.io.iframe.send({
		form: dojo.byId("uploadForm"),
		url:  "<%=uploadSourceURL%>" + "&randomNum=" + randomNum,
       method: 'POST', 
       contentType: "multipart/form-data",
		handleAs: "text",
		handle: function(response, ioArgs){
			if(response instanceof Error){
				alert("Failed to upload file, please try later.");
			}
			else{
				dijit.byId('uploadFormDiv').hide();
   				var returnData = eval(response);
   				if (returnData)
   				{
   					var msg = returnData;
   					if (returnData.error)
   	                {
   	                	msg = returnData.error;
   	                }

   					dijit.byId('progressDialog').hide();
   					showMsg(msg);
   	   			}
   				else
   				{
   					refreshJobPage();
   	   	   		}
   			}
		}
	});

    dijit.byId('uploadFormDiv').hide();
    dojo.byId('progressMsg').innerHTML='<%=bundle.getString("lb_upldate_applet_msg")%>';
    showUpdateProgress(0, randomNum);
}

function showMsg(msg)
{
	 dojo.byId('msgDiv').innerHTML=msg;
	 dijit.byId('msgDialog').show();
}

function showMsg3(msg)
{
	 dojo.byId('msgDiv3').innerHTML=msg;
	 dijit.byId('msgDialog3').show();
}


function showMsg2(msg)
{
    var dialog = dijit.byId('msgDialog2');
    dojo.style(dialog.closeButtonNode,"display","none"); 
	dojo.byId('msgDiv2').innerHTML=msg;
	dijit.byId('msgDialog2').show();
}

function closeMsg2()
{
	dijit.byId('msgDialog2').hide();
}

function refreshJobPage()
{
	try
	{
		document.getElementById("detailTab").click();
	}
	catch(ex)
	{ 
		location.reload(true)
	}
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
 ID="idBody" onload="loadPage()" onunload="doUnload()" class="tundra">
<XML id="oCurrency"><%=xmlCurrency%></XML>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/projects/workflows/pageSort.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=lbJob%>: <%=request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET)%>
</SPAN>
<P>
<input type="hidden" id="hasSetCostCenter" name="hasSetCostCenter" value="<%=hasSetCostCenter%>">

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_job_details")%>
</TD>
</TR>
</TABLE>
<P>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<TD>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>

        <TD CLASS="tableHeadingListOn">
        <IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0">
        <A CLASS="sortHREFWhite" id="detailTab" HREF="<%=detailsTabURL%>"><%=labelDetails%></A>
        <IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0">
        </TD>
        
        <amb:permission  name="<%=Permission.JOB_COMMENTS_VIEW%>" >
        <TD WIDTH="2"></TD>       
        <TD CLASS="tableHeadingListOff">        
        <IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0">
        <A CLASS="sortHREFWhite" HREF="<%=jobCommentsURL%>"><%=labelComments%></A>
        <IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0">
        </TD>
        </amb:permission>
        
        <TD WIDTH="2"></TD>
       
        <TD CLASS="tableHeadingListOff">
        <amb:permission  name="<%=Permission.JOB_ATTRIBUTE_VIEW%>" >
        <IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0">
        <A CLASS="sortHREFWhite" HREF="<%=jobAttributesURL%>"><%=bundle.getString("lb_job_attributes") %></A>
        <IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0">
        </amb:permission>
        </TD>
        
</TR>
<TR>
    <TD COLSPAN="4" HEIGHT="1"><IMG SRC="/globalsight/images/spacer.gif" HEIGHT="1" WIDTH="1"></TD>
</TR>
</TABLE>

<FORM style="display:none" NAME="oForm" ACTION="<%=request.getAttribute(JobManagementHandler.CHANGE_CURRENCY_URL)%>" METHOD="post">
<INPUT type="hidden" NAME="idCurrency" VALUE=""></INPUT>
<INPUT type="hidden" NAME="dateChanged" VALUE=""></INPUT>
<INPUT TYPE="HIDDEN" id ="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" NAME="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" VALUE="">
</FORM>

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR VALIGN=TOP>
<TD>
<!-- Details table -->
    <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" WIDTH="350" CLASS="detailText" >
        <TR CLASS="tableHeadingBasic">
            <TD COLSPAN="3" NOWRAP>&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_details")%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_job_id")%>:<B></TD>
            <TD
            <%
                if( jobCosting)
                {
                    out.println("ROWSPAN=11");
                }
                else
                {
                    out.println("ROWSPAN=10");
                }
            %>
            WIDTH="10">&nbsp;</TD>
            <TD><%=request.getAttribute(JobManagementHandler.JOB_ID)%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD style="width:100px"><B><%=bundle.getString("lb_job_name")%>:<B></TD>
            <TD style="width:200px;word-wrap:break-word;word-break:break-all">
			<%out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:200px\'>\");}</SCRIPT>"); %>
            <%=request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET)%>
            <%out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>"); %>
            </TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_initiator")%>:<B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.JOB_INITIATOR_SCRIPTLET)%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_source_locale")%>:<B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.SRC_LOCALE_SCRIPTLET)%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_project")%>:<B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.PROJECT_NAME_SCRIPTLET)%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_loc_profile")%>:<B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.L10NPROFILE_NAME_SCRIPTLET)%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_date_created")%>:<B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.JOB_DATE_CREATED_SCRIPTLET)%></TD>
        </TR>
        <%if(jobCosting) { %>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_pages")%>
                <% if (costEditAllowed) { %>
                (<A HREF="<%=editPagesURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                <%}%>
             :<B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.PAGES_IN_JOB)%></TD>
        </TR>
        <%}%>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_priority")%>:<B></TD>
            <TD><%=request.getAttribute(JobManagementHandler.JOB_PRIORITY_SCRIPTLET)%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD COLSPAN="3">&nbsp;&nbsp;&nbsp;</TD>
        </TR>
        <amb:permission  name="<%=Permission.JOB_COMMENTS_VIEW%>" >
        <TR VALIGN="TOP">
        <TD COLSPAN="3" CLASS="detailText">
        <%
            String openSegments = "<B><A HREF='" + jobCommentsURL + "'>" + openSegmentComments + "</A></B>";
            String closedSegments = "<B><A HREF='" + jobCommentsURL + "'>" + closedSegmentComments + "</A></B>";
            Object[] args = {openSegments, closedSegments}; 
            out.println(MessageFormat.format(bundle.getString("lb_segment_comments"), 
                                                                                args)); 
            out.println("<BR>");
        %>
        </TD>
        </TR>
        </amb:permission>

        <!-- Job Costing -->
        <%if(jobCosting) { %>
        <amb:permission  name="<%=Permission.JOB_COSTING_VIEW%>" >
        <TR VALIGN="TOP">
            <TD COLSPAN=3>&nbsp;</TD>
        </TR>
         <TR VALIGN="TOP">
            <TD COLSPAN=3>
                <TABLE CELLPADDING=1 CELLSPACING=0 BORDER=0 CLASS=detailText WIDTH="100%">
                    <TR>
                        <TD BGCOLOR="#D6CFB2">
                            <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0
                                CLASS=detailText BGCOLOR="WHITE" WIDTH="100%">
                                <TR VALIGN="TOP">
                                    <TD BGCOLOR="#D6CFB2" COLSPAN=2><B><%=bundle.getString("lb_job_cost")%></B></TD>
                                </TR>
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_currency")%>
                                <%
                                    if (costReEditAllowed)
                                    {
                                %>
                                     	(<A HREF="#" CLASS="standardHREFDetail" TABINDEX="0" onclick="changeCurrency('<%=currentCurrency%>'); return false;"><%=bundle.getString("lb_edit")%></A>):<B>
                                <%
                                	}
                                	else if (costEditAllowed)
                                	{ 
                                		 if(quoteApprovedDate == null)
                                		 {
                                %>
 											(<A HREF="#" CLASS="standardHREFDetail" TABINDEX="0" onclick="changeCurrency('<%=currentCurrency%>'); return false;"><%=bundle.getString("lb_edit")%></A>):<B>
   								<% 
   										}else
   										{
   								%>
											(<A  href="#" CLASS="standardHREFDetail" TABINDEX="0" onclick="return false;"><%=bundle.getString("lb_edit")%></A>):<B>
                                <%
                                 		}
                                 	} 
                                %>
                                    </TD>
                                    <TD ALIGN="RIGHT"><%=currentCurrency%></TD>
                                </TR>

                                <TR VALIGN="TOP">
                                    <TD BGCOLOR="#D6CFB2" COLSPAN=2></B></TD>
                                </TR>
                                
                                <%
                                    ArrayList surchargesAll = null;
                                    Currency currencyObj = null;
                                    int sz = 0;
                                %>

                                <amb:permission  name="<%=Permission.COSTING_EXPENSE_VIEW%>" >
                                <!-- This section is for Expense Details -->
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_estimated_internal_costs")%>:</B></TD>
                                    <TD ALIGN="RIGHT"><%=request.getAttribute(JobManagementHandler.ESTIMATED_COST)%></TD>
                                </TR>
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_actual_internal_costs")%>:</B></TD>
                                    <TD ALIGN="RIGHT"><%=request.getAttribute(JobManagementHandler.ACTUAL_COST)%></TD>
                                </TR>
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_surcharges")%>
                                    <%
                                      if (costReEditAllowed)
                                    	{
                                    %>
                                    	(<A HREF="<%=expensesSurchargesURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                    <%
                                    	}
                                    	else if (costEditAllowed)
                                    	{
	                                       	 if(quoteApprovedDate == null)
	                                       	 {
                                    %>
                                   			 	(<A HREF="<%=expensesSurchargesURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                	<%
											}else
											{
                                	%>
                                				(<A  href="#" CLASS=standardHREFDetail onclick="return false;"><%=bundle.getString("lb_edit")%></A>)
                                    <%
                                    		}
                                    	}
                                    %>
                                    :</B></TD>
                                    <TD ALIGN="RIGHT"></TD>
                                </TR>
                                <!-- Surcharges -->
                                <%
                                    surchargesAll =
                                        (ArrayList)request.getAttribute(JobManagementHandler.SURCHARGES_ALL);
                                    currencyObj =
                                        (Currency)sessionMgr.getAttribute(JobManagementHandler.CURRENCY_OBJECT);
                                    sz = surchargesAll == null ? 0 : surchargesAll.size();
                                    
                                    // tempCost includes actualCost & all flat surcharges.
                                    // tempCost will used to calculate the percentage surcharges.
                                    Money tempCost = cost.getActualCost();

                                    // 1st, calculate the flat surchage
                                    for (int i=0; i<sz; i++)
                                    {
                                        out.println("<TR VALIGN=TOP>");
                                        Surcharge surcharge = (Surcharge)surchargesAll.get(i);

                                        if (surcharge.getType().equals("FlatSurcharge"))
                                        {
                                            FlatSurcharge flatSurcharge = (FlatSurcharge)surcharge;
                                            float surchargeAmount = flatSurcharge.getAmount().getAmount();
                                            tempCost = tempCost.add(flatSurcharge.surchargeAmount(flatSurcharge.getAmount()));

                                            out.println("<TD NOWRAP STYLE=\"padding-left: 10px\">" +
                                                        flatSurcharge.getName() + "</TD>" +
                                                        "<TD ALIGN=RIGHT>" +
                                                        CurrencyFormat.getCurrencyFormat(currencyObj).format(surchargeAmount) +
                                                        "</TD>");
                                        }

                                        out.println("</TR>");
                                    }        

                                    // 2nd, calculate the percentage surchage base on totalcost
                                    for (int i=0; i<sz; i++)
                                    {
                                        out.println("<TR VALIGN=TOP>");
                                        Surcharge surcharge = (Surcharge)surchargesAll.get(i);
                                        if (surcharge.getType().equals("PercentageSurcharge"))
                                        {
                                            PercentageSurcharge percentageSurcharge = (PercentageSurcharge)surcharge;
                                            float percentage = Money.roundOff(percentageSurcharge.getPercentage() * 100);
                                            float percentageAmount =
                                                percentageSurcharge.surchargeAmount(tempCost).getAmount();
                                            out.println("<TD NOWRAP STYLE=\"padding-left: 10px\">" +
                                                        percentageSurcharge.getName() +
                                                        " (" + percentage + "%)" +  "</TD>" +
                                                        "<TD ALIGN=RIGHT>" +
                                                        CurrencyFormat.getCurrencyFormat(currencyObj).format(percentageAmount) +
                                                        "</TD>");
                                        }
                                        out.println("</TR>");
                                    }
                                %>
                                <!-- End Surcharges -->
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_final_internal_costs")%>
                                <% 
	                                if (costReEditAllowed)
	                                {
	                            %>
	                                   (<A HREF="<%=editExpensesFinalCostURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                <%
	                                }
	                                else if (costEditAllowed) 
	                                {
	                                   if(quoteApprovedDate == null)
	                                   {
                                %>
                                    		(<A HREF="<%=editExpensesFinalCostURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                <%
	                                  	}else
	                                  	{
	                            %>
	                                     	(<A href="#" CLASS=standardHREFDetail onclick="return false;"><%=bundle.getString("lb_edit")%></A>)
                                <%
	                                    }
	                                }
                                %>
                                    :<B></TD>
                                    <TD ALIGN="RIGHT"><%=request.getAttribute(JobManagementHandler.FINAL_COST)%>
                                    <%if(isCostOverriden) { %>
                                    <BR><SPAN CLASS="smallTextGray">(<%=bundle.getString("lb_final_expenses_override")%>)</SPAN>
                                    <%}%>
                                    </TD>
                                </TR>
                                <!-- End of Expense Details -->
                                </amb:permission>

                                <%if(jobRevenue) { %>
                                <amb:permission  name="<%=Permission.COSTING_REVENUE_VIEW%>" >
                                <TR VALIGN="TOP">
                                    <TD BGCOLOR="#D6CFB2" COLSPAN=2></B></TD>
                                </TR>
                                <!-- This section is for Revenue Details -->
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_actual_billing_charges")%>:</B></TD>
                                    <TD ALIGN="RIGHT"><%=request.getAttribute(JobManagementHandler.ESTIMATED_REVENUE)%></TD>
                                </TR>
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_surcharges")%>
                                    <%
	                                    if (costReEditAllowed)
	                                    {
	                                %>
	                                     (<A HREF="<%=revenueSurchargesURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                    <%
	                                    }
	                                    else if (costEditAllowed)
	                                    {
	                                        if(quoteApprovedDate == null)
	                                        {
                                    %>
                                    	 		 (<A HREF="<%=revenueSurchargesURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                    <%
	                                    	}else
	                                    	{
	                                %>
	                                      		(<A href="#" CLASS=standardHREFDetail onclick="return false;"><%=bundle.getString("lb_edit")%></A>)
                                    <%
	                                    	}
	                                    }
                                    %>
                                    :</B></TD>
                                    <TD ALIGN="RIGHT"></TD>
                                </TR>
                                <!-- Surcharges -->
                                <%
                                    surchargesAll =
                                        (ArrayList)request.getAttribute(JobManagementHandler.REVENUE_SURCHARGES_ALL);
                                    currencyObj =
                                        (Currency)sessionMgr.getAttribute(JobManagementHandler.CURRENCY_OBJECT);
                                    sz = surchargesAll == null ? 0 : surchargesAll.size();

                                    // tempCost includes actualCost & all flat surcharges.
                                    // tempCost will used to calculate the percentage surcharges.
                                    Money tempCost = revenue.getActualCost();

                                    // 1st, calculate the flat surchage
                                    for (int i=0; i<sz; i++)
                                    {
                                        out.println("<TR VALIGN=TOP>");
                                        Surcharge surcharge = (Surcharge)surchargesAll.get(i);
                                        if (surcharge.getType().equals("FlatSurcharge"))
                                        {
                                            FlatSurcharge flatSurcharge = (FlatSurcharge)surcharge;
                                            float surchargeAmount = flatSurcharge.getAmount().getAmount();
                                            tempCost = tempCost.add(flatSurcharge.surchargeAmount(flatSurcharge.getAmount()));
                                            
                                            String flatSurchargeLocalName =  flatSurcharge.getName();
                                            float perSurcharge = 0.0f;
                                            float fileCounts = job.getSourcePages().size();
                                            if(flatSurchargeLocalName.equals(SystemConfigParamNames.PER_FILE_CHARGE01_KEY))
                                            {
                                               perSurcharge =  BigDecimalHelper.divide (surchargeAmount,fileCounts);
                                               flatSurchargeLocalName = bundle.getString("lb_per_file_charge_01_detail") + "<font color='BLUE'>&nbsp;&nbsp;$"+perSurcharge+"</font>";
                                            }
                                            else if(flatSurchargeLocalName.equals(SystemConfigParamNames.PER_FILE_CHARGE02_KEY))
                                            {
                                               perSurcharge =  BigDecimalHelper.divide (surchargeAmount,fileCounts);
                                               flatSurchargeLocalName = bundle.getString("lb_per_file_charge_02_detail") + "<font color='BLUE'>&nbsp;&nbsp;$"+perSurcharge+"</font>";
                                            }
                                            else if(flatSurchargeLocalName.equals(SystemConfigParamNames.PER_JOB_CHARGE_KEY))
                                            {
                                               flatSurchargeLocalName = bundle.getString("lb_per_job_charge_detail");
                                            }
                                            out.println("<TD NOWRAP STYLE=\"padding-left: 10px\">" +
                                                        flatSurchargeLocalName + "</TD>" +
                                                        "<TD ALIGN=RIGHT>" +
                                                        CurrencyFormat.getCurrencyFormat(currencyObj).format(surchargeAmount) +
                                                        "</TD>");
                                        }
                                        out.println("</TR>");
                                    }
                                    
                                    // 2nd, calculate the percentage surchage base on totalcost
                                    for (int i=0; i<sz; i++)
                                    {
                                        out.println("<TR VALIGN=TOP>");
                                        Surcharge surcharge = (Surcharge)surchargesAll.get(i);
                                        if (surcharge.getType().equals("PercentageSurcharge"))
                                        {
                                            PercentageSurcharge percentageSurcharge = (PercentageSurcharge)surcharge;
                                            float percentage = Money.roundOff(percentageSurcharge.getPercentage() * 100);
                                            float percentageAmount =
                                                percentageSurcharge.surchargeAmount(tempCost).getAmount();
                                            out.println("<TD NOWRAP STYLE=\"padding-left: 10px\">" +
                                                        percentageSurcharge.getName() +
                                                        " (" + percentage + "%)" +  "</TD>" +
                                                        "<TD ALIGN=RIGHT>" +
                                                        CurrencyFormat.getCurrencyFormat(currencyObj).format(percentageAmount) +
                                                        "</TD>");
                                        }
                                        out.println("</TR>");
                                    }
                                %>
                               
                                <!-- End Surcharges -->
                                <TR VALIGN="TOP">
                                    <TD NOWRAP><B><%=bundle.getString("lb_final_billing_charges")%>
                                    <%
	                                    if (costReEditAllowed)
	                                    {
                                    %>
                                     		(<A HREF="<%=editRevenueFinalCostURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                    <%
	                                    }
	                                    else if(costEditAllowed)
	                                    {
	                                       if(quoteApprovedDate == null)
	                                       {
                                    %>
                                    		(<A HREF="<%=editRevenueFinalCostURL%>" CLASS=standardHREFDetail onclick="return confirmCostChange()"><%=bundle.getString("lb_edit")%></A>)
                                    <%
	                                	   }else
	                                       {
                                    %>
                                    		(<A href="#" CLASS=standardHREFDetail onclick="return false;"><%=bundle.getString("lb_edit")%></A>)
                                    <%
	                                       }
	                                    }
                                    %>
                                    :<B></TD>
                                    <TD ALIGN="RIGHT"><%=request.getAttribute(JobManagementHandler.FINAL_REVENUE)%>
                                    <%if(isRevenueOverriden) { %>
                                    <BR><SPAN CLASS="smallTextGray">(<%=bundle.getString("lb_final_revenue_override")%>)</SPAN>
                                    <%}%>
                                    </TD>
                                </TR>
                                <!-- End of Revenue Details -->
                                </amb:permission>
                                <%}%>


                            <amb:permission  name="<%=Permission.JOB_COSTING_REPORT%>" >
                                <TR VALIGN="TOP">
                                    <TD COLSPAN=2 ALIGN="RIGHT"><INPUT TYPE="BUTTON"
                                        VALUE="<%=bundle.getString("lb_cost_report")%>" NAME="costReport" CLASS="detailText"
                                        onclick="popup('/globalsight/TranswareReports?reportPageName=CostingReport&act=Create&jobid=<%=request.getAttribute(JobManagementHandler.JOB_ID)%>','<%=bundle.getString("lb_costing")%>')"></TD>
                                </TR>
                            </amb:permission>
                            </TABLE>
                         </TD>
                     </TR>
                 </TABLE>
             </TD>
         </TR>
        </amb:permission>
         <%}%>
         <!-- End Job Costing -->
     </TABLE>
<!-- End Details table -->
</TD>
<TD WIDTH="30">
&nbsp;
</TD>
<amb:permission  name="<%=Permission.JOB_FILES_VIEW%>" >
<TD VALIGN="TOP">
<%@ include file="/envoy/projects/workflows/pageSearch.jspIncl" %>

    <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%" style="border: solid 1px slategray;">
		<tbody>
		<TR width=100%>
		<TD colspan=4 width=100%>
            <div class="tableContainer" id="data">
            <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%">
				<thead class="scroll">
					<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM" width="100%">
					<TD class=scroll align=left style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px;width:70%;height:30px">
						<A CLASS="sortHREFWhite" HREF="<%=pageListUrl + "&" + JobManagementHandler.PAGE_SORT_PARAM + "=" + PageComparator.EXTERNAL_PAGE_ID%>">
						<%=bundle.getString("lb_primary_source_files")%></A><%=pageNameSortArrow%></TD>
					<TD CLASS=scroll ALIGN=left style="padding-center: 8px; padding-top: 2px; padding-bottom: 2px;" nowrap >
						<%=bundle.getString("lb_file_profile")%></TD>
					<TD class=scroll align=right style="padding-right: 8px; padding-top: 2px; padding-bottom: 2px;" nowrap>
						<A CLASS="sortHREFWhite" HREF="<%=pageListUrl + "&" + JobManagementHandler.PAGE_SORT_PARAM + "=" + PageComparator.WORD_COUNT%>">
						<%=bundle.getString("lb_source_word_count")%></A><%=wordCountSortArrow%></TD>
					<TD class=scroll align=center style="padding-right: 8px; padding-top: 2px; padding-bottom: 2px;" nowrap>
						<%=bundle.getString("lb_source")%></TD>
					</TR>
			    </thead>
    		<%=request.getAttribute(JobManagementHandler.JOB_CONTENT_SCRIPTLET)%>
		<TR>
			<TD colspan=4>
			<amb:permission  name="<%=Permission.ADD_SOURCE_FILES%>" >
			<input CLASS="standardText" type="button" name="Add Files" value="<%=bundle.getString("lb_add_files") %>" onclick="addSourceFiles()">
			</amb:permission>
			<amb:permission  name="<%=Permission.DELETE_SOURCE_FILES%>" >
			<input CLASS="standardText" type="button" name="remove Files" value="<%=bundle.getString("lb_remove_files") %>" onclick="removeSourceFiles()">
			</amb:permission>
			<amb:permission  name="<%=Permission.EDIT_SOURCE_FILES%>" >
			<input CLASS="standardText" type="button" name="download Files" value="<%=bundle.getString("lb_download_edit") %>" onclick="downloadFiles()">
			<input CLASS="standardText" type="button" name="upload Files" value="<%=bundle.getString("lb_upload_edit") %>" onclick="openUploadFile();">
			</amb:permission> 
			</TD>
	   </TR>
	</tbody>
	</TABLE>
    <!-- End Pages table -->
</TD>
</amb:permission>
</TR>
</TABLE>
<!--begin quoteForm-->
<FORM id = "quoteForm" NAME="quoteForm" METHOD="POST">
<TABLE CELLPADDING=0 CELLSPACING=2 BORDER=0 CLASS=detailText BGCOLOR="WHITE" WIDTH=100%>
<TR>
	<TD WIDTH=24%>
    	   <amb:permission name="<%=Permission.JOB_QUOTE_SEND%>" >
                <div>
                 <% if(quoteApprovedDate != null)
                    {
                 %>
                 <INPUT TYPE="CHECKBOX" NAME="quoteOK" disabled = "true" ONCLICK="updateButtonStateByCheckBox('sendEmailId',this)"/>
                    &nbsp;<B><%=bundle.getString("lb_quote_ready")%></B>
                 <%
                    }
                    else
                    {
    
                 %>
                    <INPUT TYPE="CHECKBOX" NAME="quoteOK" ONCLICK="updateButtonStateByCheckBox('sendEmailId',this)"/>
                    &nbsp;<B><%=bundle.getString("lb_quote_ready")%></B>
    
                 <%
                    }
                 %>
                </div>
               <div height="50">&nbsp;</div>
               <div>
                   <INPUT CLASS="standardText" TYPE="BUTTON" NAME="sendEmail" id = "sendEmailId" value="<%=bundle.getString("lb_send_email")%>" DISABLED="TRUE" ONCLICK="send_email()">
               </div>
               <div height="15">&nbsp;</div>
            </amb:permission>
            <!-- For Quote process webEx-->
            <amb:permission name="<%=Permission.JOB_QUOTE_PONUMBER_VIEW%>" >
              <span>
                <%=bundle.getString("lb_po_number")%><input type = "text" name = "POnumber" id = "POnumber" disabled = "true" size = 15 value = "<%=quotePOnumber%>" onChange= "PoNumberIsChanged=true;"/>
              </span>
              <amb:permission name="<%=Permission.JOB_QUOTE_PONUMBER_EDIT%>" >
                     <div ALIGN="RIGHT"><input class="standardText" type = "button" name = "PONumberSave" id = "PONumberSave" value = "<%=bundle.getString("lb_po_number_save")%>" onClick = "saveQuotePoNumber()"/></div>
                      <script>
                            var objPONumberText = document.getElementById('POnumber');
                            objPONumberText.disabled = false;                           
                      </script>
              </amb:permission>
            </amb:permission>

    </TD>
    <TD WIDTH=4%></TD>
    <%	String src_green = "";
    	String src_red = "";
    	if (quoteApprovedDate != null)
    	{
    		src_green = "/globalsight/images/traffic_green.gif";
    		src_red = "/globalsight/images/traffic_off.gif";
    	}
    	else 
    	{
    		src_red = "/globalsight/images/traffic_red.gif";
    		src_green = "/globalsight/images/traffic_off.gif";
    	}
    %>
    <TD WIDTH=70% ALIGN="left">
        <amb:permission name="<%=Permission.JOB_QUOTE_STATUS_VIEW%>" >
            <IMG SRC=<%= src_green %> BORDER="0">
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <IMG SRC=<%= src_red %> BORDER="0">
        </amb:permission>
    </TD>
</TR>
<tr height = "40">&nbsp;</tr>
<TR>
    <TD WIDTH=17%>
        <!-- For Quote process webEx issue-->
        <table >
         <amb:permission name="<%=Permission.JOB_QUOTE_APPROVE%>" >
            <tr>
                    <INPUT TYPE="CHECKBOX" NAME="ApproveBox" id = "ApproveBox" disabled = "true" onClick="updateButtonStateByCheckBox('confirmApproveId',this)"/>
                      &nbsp;<B><%=bundle.getString("lb_quote_approve")%></B>
            </tr>
            <script>
                     <%
                        if(sessionMgr.getAttribute("poRequired") != null) {
                            int poRequired = (Integer)sessionMgr.getAttribute("poRequired");
                            if(poRequired == 1) {
                      %>
                               var objPONumberText = document.getElementById('POnumber');
                               
                               if(objPONumberText != null && typeof(objPONumberText)!= "undefine")
                               {
                                   var poNumberValue =  objPONumberText.value;
                                   if(poNumberValue != null && poNumberValue!= '')
                                   {
                                       document.getElementById("ApproveBox").disabled = false;
                                   }
                                }
                       <%}else if(poRequired == 0){%>
                           document.getElementById("ApproveBox").disabled = false;
                       <%}}%>

            </script>
            <tr height = "30">&nbsp;</tr>
            <tr>
                <INPUT CLASS="standardText" TYPE="BUTTON" NAME="confirmApprove" id="confirmApproveId" value="<%=bundle.getString("lb_quote_approve_confirm")%>" DISABLED="TRUE" ONCLICK="confirmApproveQuote();"/>
            </tr>
         </amb:permission>
       </table>
    </TD>
    <TD WIDTH=4%></TD>
    <%	
    	String quoteReadyDateMessage = "";
    	String quoteApprovedDateMessage = "";
    	String quoteApprovedAnthoriserMessage = "";
    	if(quoteDate != null && !quoteDate.equals(""))
    	{
                quoteReadyDateMessage = "<B>" + bundle.getString("lb_quote_ready_email_sent") + "</B><br><B>" + quoteDate + "</B>";
        }

    	if (quoteApprovedDate != null && !quoteApprovedDate.equals(""))
    	{
   		quoteApprovedDateMessage = "<B>" + bundle.getString("lb_quote_approved_email_sent") + "</B><br><B>" + quoteApprovedDate + "</B>";
    	}
    	if(anthoriserUser != null && !anthoriserUser.equals(""))
    	{
    		quoteApprovedAnthoriserMessage = "<B>" + bundle.getString("lb_send_authoriser") + "</B><br><B>" + anthoriserUser.getFirstName() + "&nbsp;" + anthoriserUser.getLastName() + "</B>";
    	}
    %>
    <TD WIDTH=70% ALIGN="left">
      <amb:permission name="<%=Permission.JOB_QUOTE_STATUS_VIEW%>" >
      	      <div style="float:left;"><%= quoteApprovedAnthoriserMessage %></div>
      	      <div style="float:left;color: #FFFFFF;width:20px;">&nbsp;</div>
      	      <div style="float:left;"><%= quoteReadyDateMessage %></div>
      	      <div style="float:left;color: #FFFFFF;width:20px;">&nbsp;</div>
      	      <div style="float:left;"><%= quoteApprovedDateMessage %></div>
      </amb:permission>
    </TD>
</TR>
</TABLE>
<INPUT TYPE="HIDDEN" NAME="<%= JobManagementHandler.QUOTE_DATE %>" id="<%= JobManagementHandler.QUOTE_DATE %>" VALUE="">
<!-- For Quote process webEx issue-->
<INPUT TYPE="Hidden" id ="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" NAME="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" VALUE="">
<input type="hidden" id="<%= JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG %>" name="<%= JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG %>" value="false">
</FORM>
<!--end quoteForm-->
<P>
<FORM NAME="workflowForm" id="workflowForm" METHOD="POST">
<INPUT TYPE="HIDDEN" NAME="jobId"
    VALUE=<%=request.getAttribute(JobManagementHandler.JOB_ID)%>>
<!-- Workflows table -->
<amb:permission  name="<%=Permission.JOB_WORKFLOWS_VIEW%>" >
     <% if (request.getAttribute("wError") != null)
             out.println("<div style='color:red'>" +  request.getAttribute("wError") + "</div>");          
      %>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
    <TR>
        <TD>
            <SPAN CLASS="standardTextBold">
            <%=bundle.getString("lb_workflows")%>
            </SPAN>
        </TD>
    </TR>
    <TR>
        <TD>
            <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
                    <TR>
                        <TD CLASS="tableHeadingBasic" COLSPAN=<%=colspan%>
                          HEIGHT="2"><IMG SRC="/globalsight/images/spacer.gif" WIDTH="1" HEIGHT="2"></TD>
                    </TR>
                    <TR>
                    <TD CLASS="tableHeadingBasic" COLSPAN=<%=colspan%> HEIGHT="2"><IMG SRC="/globalsight/images/spacer.gif" WIDTH="1" HEIGHT="2"></TD>
                    </TR>
                    <TR>
                        <TD CLASS="tableHeadingBasic"></TD>
                        <TD CLASS="tableHeadingBasic"><SPAN CLASS="whiteBold"><%=bundle.getString("lb_target_locale")%>&nbsp;&nbsp;&nbsp;&nbsp;</SPAN></TD>
                        <TD CLASS="wordCountHeadingWhite" ALIGN="CENTER"><%=bundle.getString("lb_word_count")%>&nbsp;&nbsp;&nbsp;</TD>
                        <TD CLASS="tableHeadingBasic" ALIGN="CENTER"><SPAN CLASS="whiteBold">&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_percent_complete")%>&nbsp;&nbsp;&nbsp;</SPAN></TD>
                        <TD CLASS="tableHeadingBasic"><SPAN CLASS="whiteBold"><%=bundle.getString("lb_state")%>&nbsp;&nbsp;&nbsp;</SPAN></TD>
                        <TD CLASS="tableHeadingBasic"><SPAN CLASS="whiteBold"><%=bundle.getString("lb_current_activity")%>&nbsp;&nbsp;&nbsp;</SPAN></TD>
                        <% if (displayStartDate){%>
                            <amb:permission  name="<%=Permission.JOB_WORKFLOWS_ESTREVIEWSTART%>" >
                            <TD CLASS="tableHeadingBasic"><SPAN CLASS="whiteBold"><%=bundle.getString("lb_estimated_review_start")%>&nbsp;&nbsp;&nbsp;</SPAN></TD>
                            </amb:permission>
                        <%}%>
                        <TD CLASS="tableHeadingBasic"><SPAN CLASS="whiteBold"><%=bundle.getString("lb_estimated_translate_completion_date")%>&nbsp;&nbsp;&nbsp;</SPAN></TD>
                        <TD CLASS="tableHeadingBasic"><SPAN CLASS="whiteBold"><%=bundle.getString("lb_estimated_completion_date")%>&nbsp;&nbsp;&nbsp;</SPAN></TD>
                        <TD CLASS="tableHeadingBasic"><SPAN CLASS="whiteBold"><%=bundle.getString("lb_priority")%>&nbsp;&nbsp;&nbsp;</SPAN></TD>
                    </TR>
                    <%=request.getAttribute(JobManagementHandler.WORKFLOW_SCRIPTLET)%>
				     <TR>
				        <TD COLSPAN=<%=colspan%>>
				            <DIV ID="CheckAllLayer">
				                <A CLASS="standardHREF"
				                   HREF="javascript:checkAll('workflowForm'); setButtonState(); showPriorityDiv('workflowForm_checkAll');"><%=bundle.getString("lb_check_all")%></A> |
				                <A CLASS="standardHREF"
				                   HREF="javascript:clearAll('workflowForm');setButtonState(); showPriorityDiv('workflowForm_clearAll');"><%=bundle.getString("lb_clear_all")%></A>
				            </DIV>
				         </TD>
				     </TR>
                    <TR>
                    <TD COLSPAN=<%=colspan%> ALIGN=RIGHT>
<%
  if (!isSuperAdmin)
  {
%>
<% if (Modules.isCustomerAccessGroupInstalled()) { %>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_REASSIGN%>" >
		    <INPUT CLASS="standardText" TYPE="BUTTON" NAME="ReAssign" VALUE="<%=bundle.getString("lb_reassign")%>" onclick="submitForm('ReAssign');"/>
                    </amb:permission>
<% } %>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_DISCARD%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Discard" VALUE="<%=bundle.getString("lb_discard")%>" onclick="submitForm('Discard');"/>
                    </amb:permission>

                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_VIEW_ERROR%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="ViewError" VALUE="<%=bundle.getString("action_view_error")%>..." onclick="submitForm('ViewError');">
                    </amb:permission>
                    <amb:permission  name="<%=Permission.UPDATE_WORD_COUNTS%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="UpdateWordCounts" VALUE="<%=bundle.getString("lb_update_word_counts")%>" onclick="submitForm('UpdateWordCounts');">
                    </amb:permission>                    
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_WORDCOUNT%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="WordCount" VALUE="<%=bundle.getString("lb_detailed_word_counts")%>..." onclick="submitForm('WordCount');">
                    </amb:permission>
<% if (Modules.isVendorManagementInstalled()) { %>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_RATEVENDOR%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Rate" VALUE="<%=bundle.getString("lb_rate_vendor")%>" onclick="submitForm('Rate');">
                    </amb:permission>
<% } %>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_ARCHIVE%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Archive" VALUE="<%=bundle.getString("lb_archive")%>" onclick="submitForm('Archive');">
                    </amb:permission>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_DETAILS%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Details" VALUE="<%=bundle.getString("lb_details")%>" onclick="submitForm('Details');">
                    </amb:permission>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_EXPORT%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Export" VALUE="<%=bundle.getString("lb_export")%>..." onclick="submitForm('Export');">
                    </amb:permission>
                        <% if (reimportOption == 0 || reimportOption == 1){ %>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_ADD%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Add" VALUE="<%=bundle.getString("lb_add")%>..." onclick="submitForm('AddWF');">
                    </amb:permission>
                        <% } %>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_EDIT%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Edit" VALUE="<%=bundle.getString("lb_edit")%>..." onclick="submitForm('Edit');"/>
                    </amb:permission>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_DISPATCH%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="Dispatch" VALUE="<%=bundle.getString("lb_dispatch")%>" onclick="submitForm('Dispatch');">
                    </amb:permission>
                    <amb:permission  name="<%=Permission.JOB_WORKFLOWS_SKIP%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="skip" VALUE="<%=bundle.getString("lb_skip_activity")%>" onClick="submitForm('skip');">
                    </amb:permission>
                    <amb:permission name="<%=Permission.JOBS_DOWNLOAD%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME=Download VALUE="<%=bundle.getString("lb_download")%>..." onClick="submitForm('Download');">
                    </amb:permission>
                    <amb:permission name="<%=Permission.JOB_WORKFLOWS_PRIORITY%>" >
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME=changePriority VALUE="<%=bundle.getString("lb_modify_priority")%>" onClick="submitForm('priority');">
                    </amb:permission>
                    
                    <%
                    String sendingBackStatus = (String)request.getAttribute("editionJobSendingbackStatus");
                    if(job.getState().equals(Job.EXPORTED) || 
                        job.getState().equals(Job.LOCALIZED)) {
                        if(sendingBackStatus != null) {
                            if(!sendingBackStatus.equals("sending_back_edition_finished")) {
                    %>
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="ReSendingBack" VALUE="<%=bundle.getString("lb_resendingback_edition_job")%>" onclick="submitForm('sendingbackEditionJob');">
                    <%
                            }
                       }
                    }
                    %>
<%
  }
%>                    
                    </TD>
                    </TR>
                    <TR>
                    <TD COLSPAN=<%=colspan%> ALIGN=RIGHT>
                        <!-- Spacer -->
                        &nbsp;
                    </TD>
                    </TR>
                    <TR>
                    <TD COLSPAN=<%=colspan%> ALIGN=RIGHT>
                        <INPUT CLASS="standardText" TYPE="BUTTON" NAME="<%=lbPrevious%>" VALUE="<%=lbPrevious%>"
<% if (request.getParameter("fromImpErr") != null) { %>
                            onclick="submitForm('Pending')">
<% } else if (request.getParameter("fromJobs") != null) { %>
                            onclick="history.go(-1)">
<% } else { %>
        DISABLED >
<% } %>
                    </TD>
                    </TR>
            </TABLE>
        </TD>
    </TR>
</TABLE>
</amb:permission>
<!-- End Workflows table -->
</FORM>
</DIV>

<div dojoType="dijit.Dialog" id="addSourceDiv" title="<%=bundle.getString("title_add_source_file") %>"
    execute="" style="display:none;padding: 0px; margin: 0px;" class="nomalFont">
  <div id="appletDiv" style="padding: 0px; margin: 0px; width:920px; height:500px;"></div>
</div>


<div dojoType="dijit.Dialog" id="msgDialog" title="<%=bundle.getString("lb_message") %>"
    execute="" style="display:none;padding: 0px; margin: 0px;" class="nomalFont">
   <div style="padding: 20px 50px;" >
   <div id="msgDiv" style="padding: 0px; font-family:Arial, Helvetica, sans-serif; font-size: 11pt" ></div>
   </div>
   
   <div style="margin:5px; padding: 5px 15px; text-align:right; border-top:1px solid grey">
     <button dojoType="dijit.form.Button" type="button" onclick="dijit.byId('msgDialog').hide();"><%=bundle.getString("lb_close")%></button>
   </div>
</div>

<div dojoType="dijit.Dialog" id="msgDialog2" title="<%=bundle.getString("lb_message") %>" 
    execute="" style="display:none;padding: 0px; margin: 0px;" class="nomalFont" >
   <div style="padding: 20px 50px;" >
   <div id="msgDiv2" style="padding: 0px; font-family:Arial, Helvetica, sans-serif; font-size: 11pt" ></div>
   </div>
</div>

<div dojoType="dijit.Dialog" id="msgDialog3" title="<%=bundle.getString("lb_message") %>"
    execute="" style="display:none;padding: 0px; margin: 0px;" class="nomalFont">
   <div style="padding: 20px 50px;" >
   <div id="msgDiv3" style="padding: 0px; font-family:Arial, Helvetica, sans-serif; font-size: 11pt" ></div>
   </div>
   
   <div style="margin:5px; padding: 5px 15px; text-align:right; border-top:1px solid grey">
     <button dojoType="dijit.form.Button" type="button" onclick="refreshJobPage();"><%=bundle.getString("lb_close")%></button>
   </div>
</div>

<div dojoType="dijit.Dialog" id="progressDialog" title="<%=bundle.getString("lb_message") %>" 
    execute="" style="display:none;padding: 0px; margin: 0px; " class="nomalFont" >
   <div style="padding: 20px 50px;" >
   <div id="progressMsg" style="padding: 0px; font-family:Arial, Helvetica, sans-serif; font-size: 11pt" ></div>
   <div annotate="true" id="theBar" 
    dojoType="dijit.ProgressBar"
    style="height:15px; width:275px; font-size: 12px;" 
    >
   </div>
   </div>
</div>

<div dojoType="dijit.Dialog" id="uploadFormDiv" title="<%=bundle.getString("title_upload_source_file") %>" 
    execute="" style="display:none">
  
  <FORM NAME="uploadForm" METHOD="POST" ACTION="<%=uploadSourceURL%>"
        ENCTYPE="multipart/form-data" id="uploadForm">
  <input type="hidden" id="jobId" name="jobId" value="<%=jobId%>">
  <table style="width: 650px; ">
    <tr>
      <td colspan="2">&nbsp;</td>     
    </tr>
    <tr>
      <td colspan="2"  align="center" valign="middle" style="width: 600px; " class="nomalFont">
          <table>
          <tr>
          <td>
          <%=bundle.getString("lb_file")%>:
          </td>
           <td valign="middle">
          <input type="file" name="uploadFile" style="width: 400px; height:27px;" size="45" id="fileUploadDialog">
          </td>
          <td valign="middle">
          <button dojoType="dijit.form.Button" type="button" onclick="uploadFile()"><%=bundle.getString("lb_upload")%></button>
          <button dojoType="dijit.form.Button" type="button" onclick="dijit.byId('uploadFormDiv').hide();"><%=bundle.getString("lb_close")%></button>
          </td>
          </tr>
          </table>
       </td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>     
    </tr>
  </table>
  </FORM>
</div>


<Form name="downLoadForm" method="post" action="<%=downloadSourceURL%>">

</Form>

<script>
// Set cookie for most recently used job list
var thisjob = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>" + ":" + "<%=jobName%>";
var cookie = getJobCookie();
if (cookie.length != 0)
{
    // only save last 3.  make sure this one isn't already on the list.
    var lastjobs = thisjob;
    var jobs = cookie.split(",");
    for (i = 0; i < jobs.length && i < 3; i++)
    {
        if (jobs[i] != thisjob)
        {
            lastjobs += "," + jobs[i];
        }
    }
    setJobCookie(lastjobs);
}
else
{
    setJobCookie(thisjob);
}
</script>
</BODY>
</HTML>

