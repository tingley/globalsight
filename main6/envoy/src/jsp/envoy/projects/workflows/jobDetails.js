
function getObjWidthAndHeight(obj)
{
    var st = document.body.scrollTop;
    var sl = document.documentElement.scrollLeft;
    var ch = document.documentElement.clientHeight;
    var cw = document.documentElement.clientWidth;
    var objH = $("#" + obj).height();
    var objW = $("#" + obj).width();
    var objT = Number(st) + (Number(ch) - Number(objH))/2;
    var objL = Number(sl) + (Number(cw) - Number(objW))/2;
    
    return objT + "|" + objL;
}

function URLencode(sStr) {
	return escape(sStr).replace(/\+/g, '%2B').replace(/\"/g,'&quote;').replace(/\'/g, '%27').replace(/\//g,'%2F');
}

//Get the date which has be formated
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

function hideContextMenu()
{
    document.getElementById("idBody").focus();
}

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

// Click "Send Mail" 
function send_email(msg_quote_ready_confirm, selfURL)
{
	if(confirm(msg_quote_ready_confirm)) 
	{
		setQuoteReadyDate();
		submitEmail(selfURL);
	}
}
function setQuoteReadyDate()
{
	var date = new Date();
	var myDate = getMyDate(date);
	document.getElementById("quoteDate").value = myDate;
}
function submitEmail(selfURL)
{
	var quoteForm = document.getElementById("quoteForm");
	quoteForm.action = selfURL;
	quoteForm.submit();
}

// Click "Save" to save quote PO number
function saveQuotePoNumber(PoNumberIsChanged, msg_save_po_number_confirm, selfURL, QUOTE_PO_NUMBER,
    msg_validate_po_number)
{
	var quotePoNumber = document.getElementById("POnumber").value;
	var quoteForm = document.getElementById("quoteForm");   
	if (PoNumberIsChanged)
	{
		if(confirm(msg_save_po_number_confirm))
		{
			quoteForm.action = selfURL + "&" + QUOTE_PO_NUMBER + "=" + URLencode(quotePoNumber);
			quoteForm.submit();
		}
	}
	else
	{
		alert(msg_validate_po_number);
		return false;
	}
}

// Set the Confirm Approved Quote Date (For "Quote process webEx" issue)
function confirmApproveQuote(msg_quote_approve_confirm, QUOTE_APPROVED_DATE, 
	QUOTE_APPROVED_DATE_MODIFY_FLAG, dispatchWorkflow, msg_cost_center_empty, 
	hasReadyWorkflow, msg_dispatch_all_workflow_confirm, wfFormActionUrl, selfURL)
{
	var workflowForm = document.getElementById("workflowForm");

	if(confirm(msg_quote_approve_confirm))
	{
		setApproveQuoteDate(QUOTE_APPROVED_DATE, QUOTE_APPROVED_DATE_MODIFY_FLAG);
		//submitEmail();

		var hasSetCostCenter = document.getElementById("hasSetCostCenter").value;
		if (dispatchWorkflow == 'true')
		{
			if ("false" == hasSetCostCenter) 
			{
				alert(msg_cost_center_empty);
			}
			else if (hasReadyWorkflow == 'true' && confirm(msg_dispatch_all_workflow_confirm))
			{
	 			workflowForm.action = wfFormActionUrl;
			}
		}

		var quoteForm = document.getElementById("quoteForm");
		quoteForm.action = selfURL;
		//When user selects dispatch all workflows in the same time,add the action for "workflowForm" to current form,
		//then handler will handle them both once.
		if (workflowForm.action.length > 0) 
		{
		   quoteForm.action += "&workflowFormAction=" + workflowForm.action;
		}

		quoteForm.submit();
   }
}

function setApproveQuoteDate(QUOTE_APPROVED_DATE, QUOTE_APPROVED_DATE_MODIFY_FLAG)
{
	var date = new Date();
	var myDate = getMyDate(date);
	document.getElementById(QUOTE_APPROVED_DATE).value = myDate;
	document.getElementById(QUOTE_APPROVED_DATE_MODIFY_FLAG).value = true;
}

// This is not used for now.
function dispatchAllWorkflow(detailsURL, DISPATCH_ALL_WF_PARAM, 
		JOB_ID, ALL_READY_WORKFLOW_IDS, allReadyWorkflowsIds)
{
	var workflowForm = document.getElementById("workflowForm");
	
	workflowForm.action = detailsURL +
		"&" + DISPATCH_ALL_WF_PARAM + "=true" +
		"&" + JOB_ID + "=" + workflowForm.jobId.value +
		"&" + ALL_READY_WORKFLOW_IDS + "=" + allReadyWorkflowsIds;
    workflowForm.submit();
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

var windownum = 1;
function popup(url, target)
{
   target = target + parent.windx + windownum++;
   parent.windx++;
   var newurl = url+'&target=' + target;
   window.open(newurl,target,config='height=500,width=700,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
};

function loadPage()
{
   ContextMenu.intializeContextMenu();
   // Load the Guide
   loadGuides();
   // Load the currency stuff
   doOnLoad();
}

function openGxmlEditor(url,sourceEditorUrl)
{
    hideContextMenu();
    if (w_viewer != null && !w_viewer.closed)
    {
        w_viewer.close();
    }
    window.location.href = (sourceEditorUrl + url);
}

function getJobCookie(cookieName)
{
    var lastjobs = "";
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

function setJobCookie(lastJobs,cookieName)
{
    var today = new Date();
    var expires = new Date(today.getTime() + (90 * 86400000));  //90 days
    document.cookie = cookieName + lastJobs + ";EXPIRES=" + expires.toGMTString() + ";PATH=" + escape("/");
}

function getSelectPageIds()
{
	var pIds = document.getElementsByName("pageIds");
	var selectIds = "";
	for (var i = 0; i < pIds.length; i++) {
        if (pIds[i].checked){
           if (selectIds.length > 0){
           	selectIds = selectIds.concat(",");
           }
           
           selectIds = selectIds.concat(pIds[i].value);
        }
    }

    return selectIds;
}

function showPriorityDiv(wfId)
{
   //select "Check All"
   if (wfId == 'workflowForm_checkAll')
   {
	   var form = eval("document.workflowForm");
	   for (var i = 0; i < form.elements.length; i++)
	   {
		if (form.elements[i].type == "checkbox" && form.elements[i].name != "selectAll_1")
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
			if (form.elements[i].type == "checkbox" && form.elements[i].name != "selectAll_1")
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

function setButtonState(isVendorManagementInstalled)
{
   var workflowState;
   
   j = 0;
   if (workflowForm.wfId.length)
   {
      for (i = 0; i < workflowForm.wfId.length; i++)
      {
      	 var checkBoxObj = workflowForm.wfId[i];
         if (checkBoxObj.checked == true)
         {
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
      if (isVendorManagementInstalled) { 
          if (document.workflowForm.Rate)
              document.workflowForm.Rate.disabled = true;
      } 
      if (document.workflowForm.ReAssign)
          document.workflowForm.ReAssign.disabled = true;
      if (document.workflowForm.Download)
          document.workflowForm.Download.disabled = true;
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
      if (isVendorManagementInstalled) { 
          if (document.workflowForm.Rate)
              document.workflowForm.Rate.disabled = false;
      }
      if (document.workflowForm.ReAssign)
         document.workflowForm.ReAssign.disabled = false;
      if (document.workflowForm.Download)
          document.workflowForm.Download.disabled = false;
      if(document.workflowForm.skip){
		  document.workflowForm.skip.disabled = false;
	  }
   }
}