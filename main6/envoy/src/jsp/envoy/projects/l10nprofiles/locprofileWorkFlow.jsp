<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.projecthandler.WorkflowTemplateInfo, 
            com.globalsight.everest.foundation.BasicL10nProfile,
            com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
            com.globalsight.everest.webapp.tags.TableConstants,
            com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.WorkflowInfos,
            com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper,
            com.globalsight.util.FormUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.pagehandler.PageHandler, 
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.util.GeneralException,
            java.util.Date,
            java.util.List,
            java.util.Locale,
            java.util.Hashtable,
            java.util.ResourceBundle,
            java.util.Vector"
            session="true" %>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<jsp:useBean id="target" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel4" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="pre4" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="preList" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="addAnother" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="removeCurrent" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    // bring in "state" from session
    SessionManager sessionMgr = (SessionManager) request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfUrl = self.getPageURL();
    String cancel4URL = cancel4.getPageURL();
    String pre4URL = pre4.getPageURL();
    String addAnotherURL = addAnother.getPageURL() + "&"
                    + WorkflowTemplateConstants.ACTION 
                    + "=" + WorkflowTemplateConstants.SAVE_WORKFLOW_TEMPLATE 
                    +"&templatePageNum="
                    + request.getParameter(WorkflowTemplateConstants.KEY + TableConstants.PAGE_NUM);
    String removeCurrentURL = removeCurrent.getPageURL() + "&action=removeCurrent"
				    +"&templatePageNum="
				    + request.getParameter(WorkflowTemplateConstants.KEY + TableConstants.PAGE_NUM);
    String saveURL = save.getPageURL() + "&action=save";
    String title = bundle.getString("lb_new_workflow_template");
    String wizardTitle = bundle.getString("lb_attach_workflows_to_locales");
    String lbNext = bundle.getString("lb_next");
    String lbPrevious = bundle.getString("lb_previous");  
    String lbCancel = bundle.getString("lb_cancel");  
    String lbSave = bundle.getString("lb_save");  
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String targetUrl =  target.getPageURL() + "&" 
                      + WorkflowTemplateConstants.ACTION 
                      + "=" + WorkflowTemplateConstants.POPULATE_WORKFLOWS_ACTION;
    String labelTargetLocale =  bundle.getString("lb_target_locale");
    BasicL10nProfile ModLocProfile = (BasicL10nProfile)sessionMgr.getAttribute("locprofile");
    if (ModLocProfile != null) 
    {
        pre4URL = preList.getPageURL();
    }
    
    //Labels of the column titles
    String attachWorkflow = bundle.getString("lb_attach_workflow");
    String attachedWorkflow = bundle.getString("lb_attached_workflow");
    String saveMsg = bundle.getString("jsmsg_loc_profiles_association");
    String nameCol = bundle.getString("lb_name");
    String descCol = bundle.getString("lb_description");
    String localePairCol = bundle.getString("lb_locale_pair");
    String pmCol = bundle.getString("lb_project_manager");
    String workflowTypeCol = bundle.getString("lb_workflow_type");
    String selectTargetLocales = (String)sessionMgr.getAttribute(LocProfileStateConstants.TARGET_LOCALES);    
    List selectedIds = (List)sessionMgr.getAttribute(WorkflowTemplateConstants.SELECTED_WORKFLOW);
    
    Hashtable wftLocaleHash = new Hashtable();
    String selectedTarget = (String)sessionMgr.getAttribute(WorkflowTemplateConstants.TARGET_LOCALE); 
    if(selectedTarget!= null && !selectedTarget.equals("0"))
    {
        long targetId = Long.parseLong(selectedTarget);
        GlobalSightLocale tgt = (GlobalSightLocale)LocProfileHandlerHelper.getLocaleById(targetId);
        
        wftLocaleHash = (Hashtable)sessionMgr.getAttribute(
               WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH); 
        
        if(wftLocaleHash != null)
        {
            selectedIds = (List)wftLocaleHash.get(tgt);
        }
    }

    // get templates, state and selected column passed by PageHandler
    List templates = (List)sessionMgr.getAttribute(WorkflowTemplateConstants.TEMPLATES);
    int pageNum = ((Integer)request.getAttribute(WorkflowTemplateConstants.KEY + TableConstants.PAGE_NUM)).intValue();
    int numOfPages = ((Integer)request.getAttribute("numOfPages")).intValue();
    int totalTemplates = ((Integer)request.getAttribute("totalTemplates")).intValue();
    
    // total number of displayable objects.                           
    int listSize = templates == null ? 0 : templates.size();
    
    //paging info
    int possibleTo = pageNum * 10;
    int to = possibleTo > totalTemplates ? totalTemplates : possibleTo;
    int from = (to - listSize) + 1;
    
    // table width setting
    int tableWidth = 740;
    
    // messages                           
    String removeWarning = bundle.getString("jsmsg_wf_template_remove");    
%>
<%!
	private boolean isActive(long workflowId, HttpServletRequest request)
	{
		if(WorkflowTemplateConstants.SAVE_WORKFLOW_TEMPLATE.equals(request.getParameter("action")))
		{
			//Should not modify the "Attatch workflow" process.
			return true;
		}

		boolean flag = false;
		Vector<WorkflowInfos> workflowInfos = (Vector<WorkflowInfos>)request.getAttribute(LocProfileStateConstants.WORKFLOW_INFOS);
		for(int i = 0; i < workflowInfos.size(); i++)
		{
			WorkflowInfos workflowInfo = workflowInfos.get(i);
			if(workflowInfo.getWfId() == workflowId)
			{
				flag = workflowInfo.isActive();
			}
		}
		return flag;
	}
%> 

<%@page import="java.text.MessageFormat"%><HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = true;
    var cancelName = "<%= bundle.getString("lb_loc_profile") %>";
    var previousName = "<%= bundle.getString("lb_workflows") %>";
    var objectName = cancelName;
    var guideNode = "locProfiles";
    var anotherUrls = new Array();
    var helpFile = "<%=bundle.getString("help_localized_workflow_attach")%>";

    function cancel()
    {
       //  Bug Fix: 5577
       //  Warn before really cancelling
       //  (Warning turned on/off based on needWarning flag above)
       //
       if (confirmJump())
       {
          if (document.layers) document.contentLayer.document.profileCancel.submit();
          else profileCancel.submit();
       }
    }
    
    function previous()
    {       
        objectName = previousName;
        if (document.layers) document.contentLayer.document.profilePrev.submit();
        else profilePrev.submit();
    }

    function submitForm(selectedButton, obj) 
    {
       var dtpIndexes = dtpSelectedIndex();
       var transIndexes = translationSelectedIndex();  
       // otherwise do the following
       if (transIndexes.length != 1 || dtpIndexes.length > 1) 
       {
           alert("<%= bundle.getString("jsmsg_wf_template_select") %>");
           return false;
       }
       else 
       {
       	  if(obj.value == '<%=attachWorkflow%>'){
       		 selectedButton = 'addAnother'
          }
          else if(obj.value == "UnAttatch Workflow")
          {
          
          	 selectedButton = 'removeCurrent';
          }
		  if (selectedButton=='addAnother')
          {
             WfTemplateForm.action = "<%=addAnotherURL%>" + "&" + anotherUrls[parseInt(at(WfTemplateForm.transCheckbox, transIndexes[0]).value)];
             if (dtpIndexes.length > 0) {
             	WfTemplateForm.action += "&" + anotherUrls[parseInt(at(WfTemplateForm.dtpCheckbox, dtpIndexes[0]).value)];
             }
             WfTemplateForm.submit();
          }
		  if (selectedButton=='save')
          {          
			 if( <%=wftLocaleHash.size()%> < 1)
			 {
				alert("<%= bundle.getString("jsmsg_attach_workflow") %>");
				return false;
			 }
             WfTemplateForm.action = '<%=saveURL%>';
             WfTemplateForm.submit();
          }
          if(selectedButton == 'removeCurrent')
          {
          	 WfTemplateForm.action = "<%=removeCurrentURL%>" + "&" + anotherUrls[parseInt(at(WfTemplateForm.transCheckbox, transIndexes[0]).value)]; 
             if (dtpIndexes.length > 0) {
             	WfTemplateForm.action += "&" + anotherUrls[parseInt(at(WfTemplateForm.dtpCheckbox, dtpIndexes[0]).value)];
             }
             WfTemplateForm.submit();
          }
       }               
    }
    
    function at(array, i) {
		if (array.length) {
			return array[i];
		} else if (i == 0) {
			return array;
		} else {
			return null;
		}
	}

    function populateWorkflows()
    {
       theForm ="";
       if (document.layers){
		   theForm = document.contentLayer.document.WfTemplateForm;
	   }
       else {
		  theForm = document.all.WfTemplateForm;
		  }
       theForm.action = "<%=targetUrl%>";
	   WfTemplateForm.submit();
       //theForm.submit();
    }
    
    function updateWorkflowSelect(obj) {
		setMessage(translationSelectedIndex(), dtpSelectedIndex());
		setButtonState();
		<%
			for(int i = 0; i < listSize; i++){
		%>
		document.getElementById("checkbox_"+"<%=i%>").checked = false;
		<%
			}
		%>
		obj.checked = true;
	}
	
	function dtpSelectedIndex() {
		var dtpSelectedIndex = new Array();
		
		var dtpCheckboxes = WfTemplateForm.dtpCheckbox;
		if (dtpCheckboxes != null) {
			if (dtpCheckboxes.length) {
				for (var i=0; i < dtpCheckboxes.length; i++) {
					var checkbox = dtpCheckboxes[i];
					if (checkbox.checked) {
						dtpSelectedIndex.push(i);
					}
				}
			} else {
				if (dtpCheckboxes.checked) {
					dtpSelectedIndex.push(0);
				}
			}
		}
		return dtpSelectedIndex;
	}
	
	function translationSelectedIndex() {
		var translationSelectedIndex = new Array();
		
		var transCheckboxes = WfTemplateForm.transCheckbox;
		if (transCheckboxes != null) {
			if (transCheckboxes.length) {
				for (var i=0; i < transCheckboxes.length; i++) {
					var checkbox = transCheckboxes[i];
					if (checkbox.checked) {
						translationSelectedIndex.push(i);
					}
				}
			} else {
				if (transCheckboxes.checked) {
					translationSelectedIndex.push(0);
				}
			}
		}
		return translationSelectedIndex;
	}
	
	function setMessage(translationSelected, dtpSelected) 
	{
		var insertMessage = "";
		if (translationSelected.length == 0 && dtpSelected.length == 0) {
			insertMessage = "<%= bundle.getString("jsmsg_noneSelection_attach_workflow") %>";
			document.getElementById("add_remove").disabled = true;
			document.getElementById("<%=lbSave%>").disabled = true;
		} else if (translationSelected.length > 1 && dtpSelected.length <= 1) {
			insertMessage = "<%= bundle.getString("jsmsg_oneTransSelection_attach_workflow") %>";
			document.getElementById("add_remove").disabled = true;
			document.getElementById("<%=lbSave%>").disabled = true;
		} else if (translationSelected.length == 0 && dtpSelected.length == 1) {
			insertMessage = "<%= bundle.getString("jsmsg_noneTransSelection_attach_workflow") %>";
			document.getElementById("add_remove").disabled = true;
			document.getElementById("<%=lbSave%>").disabled = true;
		} else if (dtpSelected.length > 1) {
			insertMessage = "<%= bundle.getString("jsmsg_oneDtpSelection_attach_workflow") %>";
			document.getElementById("add_remove").disabled = true;
			document.getElementById("<%=lbSave%>").disabled = true;
		} else {
			var nw = document.getElementById("add_remove").disabled = false;
			document.getElementById("<%=lbSave%>").disabled = false;
		}
		
		document.getElementById("promptMessage").innerHTML=insertMessage;
	}
	
function setButtonState()
{
	var flag = false;
	var allTCheckBox = WfTemplateForm.transCheckbox;
	if(allTCheckBox == null)
	{
		document.getElementById("add_remove").value = "<%=attachWorkflow%>";
		return;
	}
	var model = -1;
	if(allTCheckBox.length)
	{
		for(var i = 0; i < allTCheckBox.length; i++)
		{
			if(allTCheckBox[i].checked)
			{
				model = allTCheckBox[i].getAttribute('model');
			}
		}
	}
	else
	{
		//only one checkbox
		if(allTCheckBox.checked)
		{
			model = allTCheckBox.getAttribute("model");
		}
	}
	
	var attatchImg = document.getElementById("associated_" + model);
	document.getElementById("add_remove").value = "<%=attachWorkflow%>";
	if (attatchImg && attatchImg.childNodes.length > 0)
	{
		document.getElementById("add_remove").value = "UnAttatch Workflow";
	}
}
function submitPage(link)
{
	WfTemplateForm.action = link;
    WfTemplateForm.submit();
}
    </SCRIPT>
<STYLE type="text/css">
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
</STYLE>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides();setButtonState()" CLASS="standardText">
<FORM NAME="WfTemplateForm" METHOD="POST">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading">
<%=wizardTitle%>  
</SPAN>
<P>

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER=0 CLASS="standardText">
<TR>
<TD WIDTH=600>
<%= bundle.getString("helper_text_attach_workflow") %>
</TD>
</TR>
</TABLE>
<P>
<!-- let's populate the target locale combo box -->
<%=labelTargetLocale%><SPAN CLASS="asterisk">*</SPAN>:<BR>
<SELECT NAME='<%=WorkflowTemplateConstants.TARGET_LOCALE%>' 
    ONCHANGE='populateWorkflows()' SIZE='1'> 
<%=selectTargetLocales%>    

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD ALIGN="RIGHT"> 
        <%
        // Make the Paging widget
        if (listSize > 0) 
        {
            out.println(MessageFormat.format(bundle.getString("lb_displaying_records"), from, to, totalTemplates) + "<BR>");
        
            out.println("&lt; ");

            // The "Previous" link
            if (pageNum == 1) {
                // Don't hyperlink "Previous" if it's the first page
                out.print(lbPrevious);
            }
            else 
            {
                out.print("<A HREF=\"javascript:submitPage('" + targetUrl + 
                          "&templatePageNum=" +
                          (pageNum - 1) + 
                          "')\">" + lbPrevious + "</A>");
            }

            out.print(" ");

            // Print out the paging numbers
            for (int i = 1; i <= numOfPages; i++) 
            {
                // Don't hyperlink the page you're on
                if (i == pageNum) 
                {
                    out.print("<B>" + i + "</B>");
                }
                // Hyperlink the other pages
                else
                {
                    out.print("<A HREF=\"javascript:submitPage('" + targetUrl + "&templatePageNum=" +
                              i + "')\">" + i + "</A>");
                }
                out.print(" ");
            }        

            // The "Next" link
            if (to >= totalTemplates) {
                // Don't hyperlink "Next" if it's the last page
                out.print(lbNext);
            }
            else 
            {
                out.print("<A HREF=\"javascript:submitPage('"+targetUrl + 
                          "&templatePageNum=" +
                          (pageNum + 1) + "')\">" + lbNext + "</A>");
            }
            out.println(" &gt;");
        }
        else
        {
            out.print(bundle.getString("lb_displaying_zero") + "<BR>&nbsp;");
        }
        %>
</TD>
</TR>
<TR>
<TD>             
<!-- Data Table -->
<div id="promptMessage">&nbsp;</div><p>         
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="5" CLASS="list">
        <TR CLASS="tableHeadingBasic" VALIGN="BOTTOM" STYLE="padding-bottom: 3px;">
            <TD>&nbsp;</TD>
            <%
            out.println("<TD>"+nameCol+"</TD>");
            out.println("<TD>"+descCol+"</TD>");
            out.println("<TD>"+localePairCol+"</TD>");
            out.println("<TD>"+pmCol+"</TD>");
            out.println("<TD>"+attachedWorkflow+"</TD>");
            out.println("<TD>"+workflowTypeCol+"</TD>");
            %>
        </TR>
            
            <%
               int i;
               int javascript_array_index = 0;
               String iflowTemplateId = WorkflowTemplateConstants.TEMPLATE_ID;
               String templateId = WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID;
               String checked = "";
               for (i=0; i < listSize; i++, javascript_array_index++)
               {
                  String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                  WorkflowTemplateInfo wft = (WorkflowTemplateInfo)templates.get(i);
                  String wfName = wft.getName();
                  String desc = (wft.getDescription() == null) ? "" : wft.getDescription();
                  String checkboxName = "dtpCheckbox";
                  String workflowType = wft.getWorkflowType();
                  
                  if (!wft.isDtpWorkflow(workflowType)) {
                  	workflowType = "Translation";
                  	checkboxName = "transCheckbox";
                  }
                  String localePair = wft.getSourceLocale().getDisplayName(uiLocale)+ " -> " + 
                              wft.getTargetLocale().getDisplayName(uiLocale);
                  String projectManager = wft.getProjectManagerId();
                  String anotherURL = templateId + "=" + wft.getId();
                  String associated = "";
                  if(selectedIds != null && selectedIds.size() >0){
                  	  Long wfid = new Long(wft.getId());
                  	  for(int m =0; m < selectedIds.size(); m++){
                  	  	  if(selectedIds.get(m).equals(wfid.toString()) && isActive(wfid, request)){   
	                      	  associated ="<IMG SRC=\"/globalsight/images/checkmark.gif\" " + 
	                         	  "HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3>";
	                      	  checked = "CHECKED";
	                      	  break;
                      	  }else{
                      	  	  associated = "";
	                      	  checked = "";      
                     	  }
                  	  }
                  }
                  out.println("<TR CLASS=standardText STYLE=\"padding-bottom: 5px; padding-top: 5px;\" VALIGN=TOP BGCOLOR="+color+">");
                  out.println("<TD><INPUT id='checkbox_"+i+"' model='" + i + "' TYPE='radio' onclick='updateWorkflowSelect(this)' " + checked + " NAME=" + checkboxName + " VALUE=\""+javascript_array_index+"\"></TD>");
             %>
              <SCRIPT LANGUAGE="JavaScript1.2">
                anotherUrls[<%=javascript_array_index%>] = "<%=anotherURL%>";
              </SCRIPT>
             <%
                  out.println("<TD>"+ wfName + "</TD>");
                  out.println("<TD>"+ desc + "</TD>");
                  out.println("<TD>"+ localePair + "</TD>");
                  out.println("<TD>"+ projectManager + "</TD>"); 
                  out.println("<TD id='associated_" + i + "'>"+ associated + "</TD>");
                  out.println("<TD>"+ workflowType + "</TD>");
                  out.println("</TR>");
               }
             %>
                </TABLE>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_LOCALIZATION_PROFILE); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />

</FORM>

                    <FORM NAME="profileCancel" ACTION="<%=cancel4URL%>" METHOD="POST">
                        <INPUT TYPE="HIDDEN" NAME="Cancel" VALUE="Cancel">
                    </FORM>
                    <FORM NAME="profilePrev" ACTION="<%=pre4URL%>" METHOD="POST">
                        <INPUT TYPE="HIDDEN" NAME="Previous" VALUE="Previous">
                    </FORM>
                <!-- End Data table -->
            </TD>
        </TR>
        <TR>
            <TD>&nbsp;</TD>
        </TR>
        <TR>
            <TD ALIGN="RIGHT">
                <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
                    ONCLICK="cancel()">
                <INPUT TYPE="BUTTON" NAME="<%=lbPrevious%>" VALUE="<%=lbPrevious%>" 
                    ONCLICK="previous()">
                <INPUT id="add_remove" TYPE="BUTTON" NAME="New Workflow" VALUE="<%=attachWorkflow%>" 
                    ONCLICK="submitForm('addAnother',this)">
                <INPUT TYPE="BUTTON" ID="<%=lbSave%>" NAME="<%=lbSave%>" VALUE="<%=lbSave%>" 
                    ONCLICK="submitForm('save', this)">
                 <INPUT TYPE="HIDDEN" NAME="postIt" VALUE="true"></INPUT>
            </TD>
        </TR>
    </TABLE>
</DIV>
</BODY>
</HTML>

