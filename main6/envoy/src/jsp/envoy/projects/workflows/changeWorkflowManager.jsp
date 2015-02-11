<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.ChangeWorkflowManagerHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.webapp.pagehandler.PageHandler, 
            com.globalsight.everest.servlet.EnvoyServletException,
            java.util.Hashtable,
            java.util.ResourceBundle"
            session="true" %>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="ready" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="inprogress" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    // get session manager from the http session
    SessionManager sessionMgr = (SessionManager)session.
      getAttribute(WebAppConstants.SESSION_MANAGER);
    
    String userId = ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();
    String srcPage = request.getParameter(JobManagementHandler.MY_JOBS_TAB);
    String destinationUrl = srcPage == null ? ready.getPageURL() : 
           (srcPage.equalsIgnoreCase("WF3") ? inprogress.getPageURL() :
           ready.getPageURL());
    String selfUrl = self.getPageURL() + 
            "&" + JobManagementHandler.MY_JOBS_TAB +"="+ srcPage;
    
    String lbRevert = bundle.getString("lb_revert");    
    String lbRevertAll = bundle.getString("lb_revertAll");    
    String lbCancel = bundle.getString("lb_cancel");
    String lbSave = bundle.getString("lb_saveAll");
    String lbDone = bundle.getString("lb_done");
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String paramJobId = JobManagementHandler.JOB_ID;
    String paramAssignments = ChangeWorkflowManagerHandler.WORKFLOW_MANAGER_ASSIGNMENTS;

    //Get session data
    Hashtable workflowData = (Hashtable)sessionMgr.getAttribute(ChangeWorkflowManagerHandler.WORKFLOW_DATA);
    Hashtable managerData = (Hashtable)sessionMgr.getAttribute(ChangeWorkflowManagerHandler.MANAGER_DATA);   
    String jobName = (String)sessionMgr.getAttribute(ChangeWorkflowManagerHandler.JOB_NAME);   
    String jobId = (String)request.getParameter(paramJobId);   

    // get workflow data lists (NOTE: these are all sorted and aligned by the page handler)
    ArrayList allWorkflows = (ArrayList)workflowData.get(ChangeWorkflowManagerHandler.ALL_WORKFLOW_NAMES);
    ArrayList allWorkflowIds = (ArrayList)workflowData.get(ChangeWorkflowManagerHandler.ALL_WORKFLOW_IDS);
    ArrayList allCurWorkflowMgrIds = (ArrayList)workflowData.get(ChangeWorkflowManagerHandler.ALL_CURRENT_WORKFLOW_MGR_IDS);

    boolean haveWorkflowData = false;
    if(  allWorkflows != null && allWorkflows.size() > 0 && 
        allWorkflowIds != null && allWorkflowIds.size() > 0 &&
        allCurWorkflowMgrIds != null && allCurWorkflowMgrIds.size() > 0 &&
        (allWorkflows.size() == allWorkflowIds.size()) && (allWorkflows.size() == allCurWorkflowMgrIds.size()))
    {
        haveWorkflowData = true;
    }
    
    // get workflow Manager data lists (NOTE: these are all sorted and aligned by the page handler)
    ArrayList allMgrs = (ArrayList)managerData.get(ChangeWorkflowManagerHandler.ALL_WORKFLOW_MGR_NAMES);
    ArrayList allMgrIds = (ArrayList)managerData.get(ChangeWorkflowManagerHandler.ALL_WORKFLOW_MGR_IDS);

    boolean haveManagerData = false;
    if(allMgrs != null && allMgrs.size() > 0 && 
       allMgrIds != null && allMgrIds.size() > 0 &&
       allMgrs.size() == allMgrIds.size() )
    {
        haveManagerData = true;
    }
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= bundle.getString("lb_change_workflow_manager") %></TITLE>
<%@ include file="/envoy/common/header.jspIncl" %>

<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>

<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var guideNode = "";
    var helpFile = "<%=bundle.getString("help_change_workflow_manager")%>";
    var clientSideSelectionsDefault = new Array();
    var clientSideSelections = new Array();
    var trackChanges = new Array();
    var currSelectedWorkflowIdx;

    function init()
    {
        // Load the Guides
        loadGuides();
        
        selectWorkflowManagers();
    }
   
    function submitForm(selectedButton) 
    {    
       if (selectedButton == 'save' ) 
       {   
           var warnRemove = false;

           // make sure we captured the last changes
           recordWorkflowManagers();

           WfTemplateForm.action = "<%=selfUrl%>";
           WfTemplateForm.action += "&" + "<%=paramJobId%>" + "=" + <%=jobId%>;

           // -all selections are sent under the same param name
           // -all selections are sent as [space] delimted strings
           // -the first token is the workflow id, the remaining tokens are user ids
           for(workflowId in clientSideSelections)
           {
                var separator = " ";
                var selectionsDefaults = clientSideSelectionsDefault[workflowId];
                var selections = clientSideSelections[workflowId];
                
                // for ie5.0 we have to avoid funtion names that get returned with "for/in" .
                if(typeof(selections) != "function" && typeof(selections) != "undefined" )
                {
                    var s = separator + selections.join(separator);
                    
                    // check if user is removing himself or herself
                    // temp search strings must end with a space
                    var d = separator + selectionsDefaults.join(separator) + " ";
                    var _s = separator + selections.join(separator) + " ";
                    var inD = d.indexOf(" " + "<%=userId%>" + " ");
                    var inS = _s.indexOf(" " + "<%=userId%>" + " "); 
                    if(inD != -1 && inS == -1)
                    {
                        warnRemove = true; 
                    }

                    addFormElement("<%=paramAssignments%>", workflowId + s);
                }
           }

           if(warnRemove && !confirm("You are removing yourself as a manager of some workfows. Ok ?"))
           {
              return true;    
           }           
           
           WfTemplateForm.submit();           
       }       
    } 

    function selectWorkflowManagers()
    {
<% 
        if(!haveWorkflowData)
        { %>
            return;
<%      } %>

        theForm ="";
        if (document.layers)
        {
            theForm = document.contentLayer.document.WfTemplateForm;
        }
        else 
        {
            theForm = document.all.WfTemplateForm;
        }
       
        // remember curr selection
        currSelectedWorkflowIdx = theForm.selectWorkflow.selectedIndex;

<% 
        if(haveManagerData)
        { %>
            selectClear(theForm.selectWorkflowManagers);
            var userSelections = clientSideSelections[theForm.selectWorkflow.value];
            for(var i=0; i < userSelections.length; i++)
            { 
                selectValue(theForm.selectWorkflowManagers, userSelections[i]);
            }
<%      } %>

        return;
    }  

    function selectClear(select)
    {
      for (var i = 0; i < select.options.length; ++i)
      {
          select.options[i].selected = false;
      }
    }

    function selectValue(select, value)
    {
      for (i = 0; i < select.options.length; ++i)
      {
        if (select.options[i].value == value)
        {
          select.options[i].selected = true;
          return;
        }
      }
    }
    
    function recordWorkflowManagers()
    {
<% 
        if(!haveManagerData)
        { %>
            return;
<%      } %>

        theForm ="";
        if (document.layers)
        {
            theForm = document.contentLayer.document.WfTemplateForm;
        }
        else 
        {
            theForm = document.all.WfTemplateForm;
		}
       var selectedManagers = new Array();
       var j=0;
       for(var i=0; i<theForm.selectWorkflowManagers.options.length; i++)
       {
         if(theForm.selectWorkflowManagers.options[i].selected)
         {
            selectedManagers[j++] = theForm.selectWorkflowManagers.options[i].value;
         }
       }
       var workflowId = theForm.selectWorkflow.options[currSelectedWorkflowIdx].value;
       clientSideSelections[workflowId]=selectedManagers;
       currSelectedWorkflowIdx = theForm.selectWorkflow.selectedIndex;
       selectWorkflowManagers();
    }

    function done()
    {
       if (isDirty())
       {
          if(confirm("<%=bundle.getString("jsmsg_change_workflow_manager_save")%>"))
          {
            submitForm("save");
          }
       }
       else
       {
          WfTemplateForm.action = "<%=destinationUrl%>";
          WfTemplateForm.submit();          
       }
    }

    function cancel()
    {
       // cancel should simply cancel - do not ask for confirmation!
       WfTemplateForm.action = "<%=destinationUrl%>";
       WfTemplateForm.submit();          
    }

    function revert()
    {
        revertOne(theForm.selectWorkflow.value);
    }

    function revertOne(workflowId)
    {
        // NOTE: THE KEY TO EACH ARRAY IS THE WORKFLOW ID (see also page handler)   
        //var workflowId = theForm.selectWorkflow.value;
        var newSelections = new Array();
        var defaults = clientSideSelectionsDefault[workflowId];
        for(var i=0; i < defaults.length; i++)
        {           
           newSelections[i] = defaults[i];
        }
        clientSideSelections[workflowId] = newSelections;
        trackChanges[workflowId] = false;
        selectWorkflowManagers();
        updateStatusMessage();
        return;
    }

    function revertAll()
    {
        // NOTE: THE KEY TO EACH ARRAY IS THE WORKFLOW ID (see also page handler)   
        for(workflowId in clientSideSelectionsDefault)
        {
            // for ie5.0 we have to avoid funtion names that get returned with "for/in" .
            if(typeof(clientSideSelectionsDefault[workflowId]) != "function" )
            {
                revertOne(workflowId);
                trackChanges[workflowId] = false;
            }
        }
        selectWorkflowManagers();
        updateStatusMessage();
        return;
    }

    function dirty()
    {
<% 
        if(haveWorkflowData && haveManagerData)
        { %>
            trackChanges[theForm.selectWorkflow.value] = true;
            updateStatusMessage();
<%      } %>
    }
    
    function isDirty()
    {
        for(workflowId in trackChanges )
        {
            // for ie5.0 we have to avoid funtion names that get returned with "for/in" .
            if(typeof(trackChanges[workflowId]) != "function" )
            {
                if(trackChanges[workflowId])
                {
                    return true;
                }
            }
        } 
        return false;
    }
             
    // add hidden inputs with the given name and value for WFM to be set.              
    function addFormElement(name, value) 
    { 
        var e = document.createElement('input'); 
        e.setAttribute('type', 'hidden'); 
        e.setAttribute('name', name); 
        e.setAttribute('value', value); 
        var f = document.getElementById('WfTemplateForm'); 
        f.appendChild(e);
    } 

    function updateStatusMessage()
    {
        var msg = "";

        if(isDirty())
        {
            msg = "<%=bundle.getString("jsmsg_change_workflow_manager_unsaved")%>";
        }       

        // set message
        if (document.layers)
        {
            document.menu.document.statusMessage.innerHTML = msg;
        }
        else
        {
           statusMessage.innerHTML = msg;
        }
    } 

    </SCRIPT>
<STYLE type="text/css">
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
</STYLE>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="init();"  CLASS="standardText">


<%    
    // Create a multidementional client-side javascript array to track selections *****
    //  - create a string representation of CurWorkflowMgrIds for each workflow
    //  - use string to create a javascript array of selections
    //  - add the array of selections to the multidementional array [wkflowID][array of selections]
    StringBuffer sbSeletions = null;
    for(int alignedIdx=0; alignedIdx < allCurWorkflowMgrIds.size(); alignedIdx++)
    {
        sbSeletions = new StringBuffer();
        ArrayList aWorkflowCurrMgrIds = (ArrayList)allCurWorkflowMgrIds.get(alignedIdx);
        for(int j=0; j < aWorkflowCurrMgrIds.size(); )
        {
           sbSeletions.append("\"" + aWorkflowCurrMgrIds.get(j) + "\"");
           if (++j<aWorkflowCurrMgrIds.size())
           {
               sbSeletions.append(",");
           }
        }
%>
<SCRIPT LANGUAGE="JavaScript">
       clientSideSelections[<%=allWorkflowIds.get(alignedIdx)%>]= new Array(<%=sbSeletions.toString()%>);
       clientSideSelectionsDefault[<%=allWorkflowIds.get(alignedIdx)%>]= new Array(<%=sbSeletions.toString()%>);
       trackChanges[<%=allWorkflowIds.get(alignedIdx)%>]= false;
</SCRIPT>
<%
   }
%>



<FORM NAME="WfTemplateForm" ID="WfTemplateForm" METHOD="POST">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading">
<%=bundle.getString("lb_change_workflow_manager")%>
</SPAN>
<P>

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER=0 CLASS="standardText">
<TR>
<TD WIDTH=600>
<%= bundle.getString("helper_text_change_workflow_manager") %>
</TD>
</TR>
</TABLE>
<P>
<!-- let's populate the target locale combo box -->
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
    <TR>
        <TD COLSPAN=2 >
            <SPAN CLASS="standardTextBold"><%=bundle.getString("lb_job")%>:</SPAN> <%=jobName%>
            &nbsp;&nbsp;&nbsp;&nbsp;<SPAN id="statusMessage" CLASS="standardText" style="color: red;" >&nbsp;</SPAN>
        </TD>
    </TR>
</TABLE>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
    <TD COLSPAN=3 height='10'>
        &nbsp;
    </TD>
</TR>
<TR>
    <TD> 
       <SPAN CLASS="standardTextBold"><%=bundle.getString("lb_workflows")%>:</SPAN>
    </TD>
    <TD width='30'>&nbsp;</TD>
    <TD> 
       <SPAN CLASS="standardTextBold"><%=bundle.getString("lb_current_workflow_managers")%>:</SPAN>       
    </TD>
</TR>
<TR>
    <TD>
        <!-- Workflow selector -->
<%
        if(haveWorkflowData)
        {
%>
        <SELECT NAME='selectWorkflow' ONCHANGE='recordWorkflowManagers()' SIZE='15'> 
<%
            for(int i=0; i<allWorkflows.size(); i++)
            {
%>
        <OPTION VALUE='<%=allWorkflowIds.get(i)%>' <%= i==0 ? "SELECTED" : "" %> ><%=allWorkflows.get(i)%></OPTION>;
<%          }
        }
        else
        {
%>
        <%=bundle.getString("msg_no_workflows_assigned_to_you")%>
<%
        }
%>

    </TD>
    <TD width='30'>&nbsp;</TD>
    <TD>    
        <!-- Workflow Manager User selector -->
<%
        if(haveManagerData)
        {
%>
            <SELECT NAME='selectWorkflowManagers' ONCHANGE='dirty()' SIZE='15' WIDTH='30' MULTIPLE> 
<%
            for(int i=0; i<allMgrs.size(); i++)
            {
%>
            <OPTION VALUE='<%=allMgrIds.get(i)%>' ><%=allMgrs.get(i)%></OPTION>;
<%          }
        }
        else
        {
%>
        <%=bundle.getString("msg_no_workflow_managers")%>
<%
        }
%>


    </TD>
</TR><TD COLSPAN= '3'>&nbsp;</TD><TR>
</TR>
<TR>
    <TD>
<% 
     if(haveWorkflowData && haveManagerData)
     { %>
       <INPUT TYPE="BUTTON" NAME="<%=lbRevert%>" VALUE="<%=lbRevert%>" 
                    ONCLICK="revert()">
       <INPUT TYPE="BUTTON" NAME="<%=lbRevertAll%>" VALUE="<%=lbRevertAll%>" 
                    ONCLICK="revertAll()">                        
<%   } %>
    </TD>
    <TD>&nbsp;</TD>
    <TD ALIGN="RIGHT">
        <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
            ONCLICK="cancel()">   
<% 
     if(haveWorkflowData  && haveManagerData )
     { %>
        <INPUT TYPE="BUTTON" NAME="<%=lbSave%>" VALUE="<%=lbSave%>"
            ONCLICK="submitForm('save')">   
        <INPUT TYPE="BUTTON" NAME="<%=lbDone%>" VALUE="<%=lbDone%>"
            ONCLICK="done()">   
<%   } %>
    </TD> 
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>

