<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
			com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.foundation.User,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.everest.company.CompanyThreadLocal,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.cxe.entity.blaise.BlaiseConnector,
            com.globalsight.connector.blaise.vo.TranslationInboxEntryVo,
            com.globalsight.everest.util.comparator.BlaiseInboxEntryComparator,
            java.text.SimpleDateFormat,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    // Lables
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_blaise_inbox_entries");
    String createJobTitle = bundle.getString("lb_blaise_create_job");
    String entryListHelperText = bundle.getString("helper_text_blaise");
    String createHelperText = bundle.getString("helper_text_blaise_create_job");
    String noteFNameAsJobName = bundle.getString("lb_blaise_entry_name_as_job_name");

    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
    String companyIdWorkingFor = CompanyThreadLocal.getInstance().getValue();
    String userName = "";
    if (user != null) {
        userName = user.getUserName();
    }
    String fps = (String) request.getAttribute("fps");

    BlaiseConnector blc = (BlaiseConnector) sessionMgr.getAttribute("blaiseConnector");
    HashMap<Long, String> id2FileNameMap = (HashMap<Long, String>) sessionMgr.getAttribute("id2FileNameMap");
    HashMap<Long, String> id2LocaleMap = (HashMap<Long, String>) sessionMgr.getAttribute("id2LocaleMap");
    List<java.util.Locale> allBlaiseLocales = (List<java.util.Locale>) sessionMgr.getAttribute("allBlaiseLocales");

    // URLs
	String filterURL = self.getPageURL() + "&action=filter";
	String claimURL = self.getPageURL() + "&action=claim";
    String uploadAttachmentUrl = self.getPageURL() + "&action=uploadAttachment";
    String checkTargetLocalesUrl = self.getPageURL() + "&action=checkTargetLocalesUrl";
    String checkAttrRequiredUrl = self.getPageURL() + "&action=checkAttributeRequired";
    String createBlaiseJobUrl = self.getPageURL() + "&action=createBlaiseJob";

    // Filters
    String relatedObjectIdFilter = (String) sessionMgr.getAttribute("relatedObjectIdFilter");
    relatedObjectIdFilter = relatedObjectIdFilter == null ? "" : relatedObjectIdFilter;
	String sourceLocaleFilter = (String) sessionMgr.getAttribute("sourceLocaleFilter");
	sourceLocaleFilter = sourceLocaleFilter == null ? "" : sourceLocaleFilter;
	String targetLocaleFilter = (String) sessionMgr.getAttribute("targetLocaleFilter");
	targetLocaleFilter = targetLocaleFilter == null ? "" : targetLocaleFilter;
	String descriptionFilter = (String) sessionMgr.getAttribute("descriptionFilter");
	descriptionFilter = descriptionFilter == null ? "" : descriptionFilter;
	String jobIdFilter = (String) sessionMgr.getAttribute("jobIdFilter");
	jobIdFilter = jobIdFilter == null ? "" : jobIdFilter;

    Integer creatingJobsNum = (Integer)request.getAttribute("creatingJobsNum");
    if (creatingJobsNum == null)
        creatingJobsNum = 0;

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
%>
<html>
<head>
<title><%=title%></title>
<style type="text/css">
form{margin:0px}
</style>

<link rel="stylesheet" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<script language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></script>
<script language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></script>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<script src="/globalsight/includes/ContextMenu.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<script type="text/javascript">
var needWarning = false;
var guideNode = "Blaise";
var objectName = "";
var helpFile = "<%=bundle.getString("help_blaise_entries")%>";

var l10Nid = 0;// the localization profile id in use for current page
var attributeWindow = null; //child window of job attributes
var attachmentUploading = false;
var attributeRequired = false;
var isUploading = false;
var defaultFileProfile = "-1";
var creating = false;

var entryFileNameMap = {};
<%
for (Map.Entry<Long, String> entry : id2FileNameMap.entrySet())
{%>
	entryFileNameMap['<%=entry.getKey()%>'] = '<%=entry.getValue()%>';
<%
}
%>

var id2LocaleMap = {};
<%
for (Map.Entry<Long, String> entry : id2LocaleMap.entrySet())
{%>
	id2LocaleMap['<%=entry.getKey()%>'] = '<%=entry.getValue()%>';
<%
}
%>

var srcLocaleOptions = "<option>Choose...</option>";
<%
String lCode = null;
String displayName = null;
String selected = "";
for (java.util.Locale locale : allBlaiseLocales)
{
    lCode = locale.getLanguage() + "_" + locale.getCountry();
    displayName = lCode + " (" + locale.getDisplayLanguage() + "_" + locale.getDisplayCountry() + ")";
    selected = lCode.equalsIgnoreCase(sourceLocaleFilter) ? "SELECTED" : "";
%>
    srcLocaleOptions += "<option value='<%=lCode%>' <%=selected%>>" + "<%=displayName%>" + "</option>";
<%
}
%>

var trgLocaleOptions = "<option>Choose...</option>";
<%
for (java.util.Locale locale : allBlaiseLocales)
{
    lCode = locale.getLanguage() + "_" + locale.getCountry();
    displayName = lCode + " (" + locale.getDisplayLanguage() + "_" + locale.getDisplayCountry() + ")";
    selected = lCode.equalsIgnoreCase(targetLocaleFilter) ? "SELECTED" : "";
%>
	trgLocaleOptions += "<option value='<%=lCode%>' <%=selected%>>" + "<%=displayName%>" + "</option>";
<%
}
%>

function claim()
{
	var selectedIds = findSelectedCheckboxes();
	var url = "<%=claimURL%>&entryIds=" + selectedIds;
    $("#blaiseEntriesForm").attr("action", url).submit();
}

function goToCreateJob()
{
    showCreatePage();

    $("#fileQueue").html("");

    var ids = findSelectedCheckboxes();
    var idsArr = ids.split(",");
    for (var i = 0; i < idsArr.length; i++)
    {
    	var id = idsArr[i];
    	if (i % 2 == 0) {
        	addFullDivElement(id, 'tableRowOddTM');
    	} else {
    		addFullDivElement(id, 'tableRowEvenTM');
   		}
    }
}

function addFullDivElement(id, lineClass)
{
	var script = '<tr width="100%" height="25" id="tr' + id + '" class="' + lineClass + '">'
		+ '<td width="50%" class="titletext">&nbsp;' + entryFileNameMap[id] + '</td>'
		+ '<td width="20%" class="profile">'
		+ '    <select name="fileProfile" id="fp'+id+'" style="width:143;z-index:50;padding-top:2px;padding-bottom:2px;" onchange="changeFileProfile(this)">'
		+ '        <%=fps%>'+
		+ '    </select>'
		+ '</td>'
		+ '<td width="25%" class="titletext">' + id2LocaleMap[id] + '</td>'
		+ '<td width="5%"><a href="javascript:removeSelectedFile(\'' + id + '\')"><img style="padding-top:4px" src="/globalsight/images/createjob/delete.png" border="0"/></a></td>'
		+ '</tr>';

		$("#fileQueue").append(script);
	
	updateToDefaultFileProfile();
    updateFileProfileSelect();
    updateFileTotal();
}

function changeFileProfile(select)
{
    defaultFileProfile = select.value;

    updateToDefaultFileProfile();

    updateFileProfileSelect();
}

//If one file is mapped with file profile, the other files with same extension 
//should use the same file profile default.
function updateToDefaultFileProfile()
{
	if (defaultFileProfile != "-1")
	{
		var selects = $("[name='fileProfile']");
		for (var j = 0; j < selects.length; j++)
		{
			var aSelect = selects[j];
         	if (aSelect.value == "-1")
         	{
             	$(aSelect).val(defaultFileProfile);
		    }
     	}
	}
}

//update file profile map options
function updateFileProfileSelect()
{
    var os = $("option:selected[class][class !='-1']");
    if (os.length > 0)
    {
    	// Use "class" to store "l10Nid" in "select>>option".
        l10Nid = os[0].className;
        $("option[class][class !='" + l10Nid + "']").each(function() {
            if (this.className != "-1")
            {
                this.disabled = true;
            }
        });

        checkAttrRequired();
    }
    else
    {
        l10Nid = 0;
        defaultFileProfile = "-1";
        $("#attributeButton").hide();
        attributeRequired = false;
        $("option[class]").each(function() {
                this.disabled = false;
        });
    }
}

function checkAttrRequired()
{
    $.get('<%=checkAttrRequiredUrl%>', 
    	{"l10Nid":l10Nid,"no":Math.random()}, 
        function(ret) {
            if (ret == "true" || ret == "required") {
                $("#attributeButton").show();
                if (ret == "required") {
                    attributeRequired = true;
                }
            } else {
                $("#attributeButton").hide();
            }
    });
}

function removeSelectedFile(id)
{
    $("#tr"+id).remove();

    updateFileProfileSelect();
    updateFileTotal();
    refreshEntryClass();
}

// When remove entry from selected entries, reset their class
function refreshEntryClass()
{
    var i = 0;
    $("tr[id^=tr]").each(function () {
		var trIid = $(this).attr("id");
		if (i % 2 == 0) {
			$(this).attr("class", "tableRowOddTM");
    	} else {
    		$(this).attr("class", "tableRowEvenTM");
   		}
		i++;
	});
}

function updateFileTotal()
{
    var total = $("[name='fileProfile']").length;
    $("#fileNo").html(total);
    $("#fileNo2").html(total);
}

function showCreatePage()
{
    document.title="<%=createJobTitle%>";

    $("#blaiseEntryListDiv").hide();
    $("#createJobDiv").show();

    $("#blaiseFileListHeader").hide();
    $("#blaiseCreateJobHeader").show();

    $("#fileQueue").width($("#uploadArea").width() - 2);

    helpFile = "<%=bundle.getString("help_blaise_create_job")%>";
}

function showEntryList()
{
    document.title="<%=title%>";

    $("#blaiseEntryListDiv").show();
    $("#createJobDiv").hide();

    $("#blaiseFileListHeader").show();
    $("#blaiseCreateJobHeader").hide();

    helpFile = "<%=bundle.getString("help_blaise_entries")%>";
}

function setButtonState()
{
	var ids = findSelectedCheckboxes();
    if (ids == "")
    {
    	$("#goToCreateJobBtn").attr("disabled", true);
//    	$("#claimBtn").attr("disabled", true);
    }
    else
    {
    	$("#goToCreateJobBtn").attr("disabled", false);
//    	$("#claimBtn").attr("disabled", false);
    }
}

function findSelectedCheckboxes()
{
    var ids = "";
    $('input[type="checkbox"][name="blaiseInboxEntryIds"]:checked').each(function () {
        ids += $(this).val() + ",";
    });
    if (ids != "")
      ids = ids.substring(0, ids.length - 1);
    return ids;
}

function handleSelectAll()
{
    if (blaiseEntriesForm && blaiseEntriesForm.selectAll) {
        if (blaiseEntriesForm.selectAll.checked) {
            checkAllWithName('blaiseEntriesForm', 'blaiseInboxEntryIds');
            setButtonState();
        }
        else
        {
            clearAll('blaiseEntriesForm');
            setButtonState();
        }
    }
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13 && checkFilters())
    {
    	blaiseEntriesForm.action = "<%=filterURL%>";
    	blaiseEntriesForm.submit();
    }
}

function filterSelectItems(e)
{
	filterItems(e);
}

function checkFilters()
{
	var tmp = "";
	tmp = ATrim($("#relatedObjectIdFilter").val());
	if (tmp != "" && !isAllDigits(tmp)) {
		alert("Invalid Blaise ID, only integer number is allowed.");
		return false;
	}

	tmp = ATrim($("#jobIdFilter").val());
	var ids = tmp.split(",");
	var id = "";
    for (var i = 0; i < ids.length; i++) {
    	id = ATrim(ids[i]);
    	if (id != "" && !isAllDigits(id)) {
    		alert("Invalid job ID, only integer number is allowed.");
    		return false;
    	}
	}
    
    return true;
}

//Delete attachment file
function delAttc()
{
    $("#attachmentArea").html("");
    $("#delAtt").hide();
    $("#attachment").val("");
    $("#attachmentArea").removeClass("uploadifyError");
    attachmentUploading = false;
}

function clickAtt()
{
    $("#selectedAttachmentFile").click();
}

//Upload attachment to server in a TMP file in session.
function checkAndUpload()
{
    if(isUploading)
    {
        alert("Please wait for the last file upload.");
        emptyFileValue();
        return false;
    }

    var tempFileName = $("#selectedAttachmentFile").val();
    if(tempFileName.lastIndexOf("\\") > 0)
    {
        tempFileName = tempFileName.substr(tempFileName.lastIndexOf("\\") + 1,tempFileName.length);
    }
    addTempAttachment(tempFileName.replace(/\\/g, "\\\\").replace(/\'/g, "\\'"));

    $("#createJobForm").attr("enctype","multipart/form-data");
    $("#createJobForm").ajaxSubmit(
    {
		type: 'post',
        url: "<%=uploadAttachmentUrl%>",
        dataType:'text',
        timeout:100000000,
        success: function(data){
             addAttachment();
        },
        error: function(XmlHttpRequest, textStatus, errorThrown) {  
             alert(errorThrown);
        }
    });

    isUploading = true;

    $("#selectedAttachmentFile").prop('disabled', false);
}

function emptyFileValue() {
    $("#selectedAttachmentFile").replaceWith($("#selectedAttachmentFile").clone(true));
    $("#selectedAttachmentFile").val('');
}

function addTempAttachment(fileName) {
    $("#attachmentArea").html("<div style='line-height:25px;position:absolute;background-color:#0099FF;' id='ProgressBarAttach'></div>" 
                            + "<div id='attName' style='width:336px' class='attachment_div'>" + fileName + "</div>" );
    $("#ProgressBarAttach").height($("#attachmentArea").height());
    $("#attachment").val(fileName);
    runAttachProgress(10);
    runAttachProgress(20);
    runAttachProgress(30);
    runAttachProgress(50);
    runAttachProgress(60);
}

function runAttachProgress(percentage)
{
    percentage = parseInt(percentage);
    if (percentage >= 0) {
        var wii = percentage / 100 * $("#attachmentArea").width() - 1;
        $("#ProgressBarAttach").animate({width : wii}, "normal", function(){
            if (percentage == 100) {
                $("#ProgressBarAttach").css("background-color", "white");
                attachmentUploading = false;
                isUploading = false;
            }
        });
    } else {
        $("#attachmentArea").addClass("uploadifyError");
        $("#ProgressBarAttach").stop(true);
        $("#ProgressBarAttach")
            .fadeOut(250, function() {
                $(this).remove();
                attachmentUploading = false;
                isUploading = false;
            });
    }
}

function addAttachment() {
    $("#delAtt").show();
    attachmentUploading = true;
    runAttachProgress(100);
}

// After submit job, clear all fields on create job UI
function clearFieldValues() {
    $("#fileQueue").html("");
    $("textarea").val("<c:out value='${jsmsg_customer_comment}'/>")
    $("textarea").css("color","#cccccc");
//    $("#jobName").attr("value","<c:out value='${jsmsg_customer_job_name}'/>");
//    $("#jobName").css("color","#cccccc");
    defaultFileProfile = "-1";
    $("#priority option[value='3']").attr("selected", "selected");
    $("#selectedAttachmentFile").val("");
    $("#attributeString").val("");
    $("#fileMapFileProfile").val("");
    delAttc();
}

$(document).ready(function ()
{
	// hide "Job Attributes" button in the beginning
	$("#attributeButton").hide();
	// "Browser" file button should be hidden always
	$("#selectedAttachmentFile").hide();

	$("#sourceLocaleFilter").html(srcLocaleOptions);
	$("#targetLocaleFilter").html(trgLocaleOptions);

// action of job name text
//    $("#jobName").focus(function() {
//        if ($("#jobName").attr("value") == "<c:out value='${jsmsg_customer_job_name}'/>") {
//            $("#jobName").attr("value","");
//            $("#jobName").css("color","black");
//        }
//    }).blur(function() {
//        if ($("#jobName").attr("value") == "") {
//            $("#jobName").attr("value","<c:out value='${jsmsg_customer_job_name}'/>");
//            $("#jobName").css("color","#cccccc");
//        }
//    });

    // action of textarea (job comment area)
    $("textarea").focus(function() {
        if ($("textarea").val() == "<c:out value='${jsmsg_customer_comment}'/>") {
            $("textarea").val("");
            $("textarea").css("color","black");
        }
    }).blur(function() {
        if ($("textarea").val() == "") {
            $("textarea").val("<c:out value='${jsmsg_customer_comment}'/>")
            $("textarea").css("color","#cccccc");
        }
    });

    // "Clear Profile" button
    $("#cleanMap").click(function() {
        $("[name='fileProfile']").each(function(){
            var emptyOptions = $(this).find("option[class][class='-1']");
            if (emptyOptions.length == 0)
            {
                $(this).prepend('<option class="-1\" value="-1"></option>');
            }
        });
        $("[name='fileProfile']").attr("value", "-1");
        $("[name='fileProfile'] option").attr("disabled", false);

        updateFileProfileSelect();

        $("tr[id^=tr]").each(function () {
    		$(this).removeAttr("style");
    	});

		refreshEntryClass();

        $(this).blur();
    });

    // action of job attribute button
    $("#attributeButton").click(function() {
        var ctlStr = "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=450,left=700,top=200";
        var winUrl = "/globalsight/ControlServlet?linkName=jobAttributes&pageName=CZJ&l10Nid="+l10Nid;
        attributeWindow = window.open(winUrl,"ja", ctlStr);
        $(this).blur();
        attributeWindow.focus();
    });

    $("#cancel").click(function() {
    	showEntryList();
    });

    // create job
    $("#create").click(function()
    {
        $(this).blur();
        if (creating) {
            return;
        }
        creating = true;

        // there should be at least one source file
        if ($("#fileNo").html() == "0") {
            alert("<c:out value='${msg_job_add_files}'/>");
            creating = false;
            return;
        }

        // all files should be mapped file profile
        if ($("option:selected[class][class='-1']").length > 0)
        {
            alert("<c:out value='${jsmsg_choose_file_profiles_for_all_files}'/>");
            creating = false;
            return;
        }

        // validation of job name
//        var jobName = Trim($("#jobName").attr("value"));
//        if (jobName == "" || jobName == "<c:out value='${jsmsg_customer_job_name}'/>")
//        {
//            alert("<c:out value='${jsmsg_customer_job_name}'/>");
//            $("#jobName").focus();
//            creating = false;
//            return;
//        }

//        var jobNameRegex = /[\\/:;\*\?\|\"<>&%]/;
//        if (jobNameRegex.test(jobName))
//        {
//            alert("<c:out value='${jsmsg_invalid_job_name_1}' escapeXml='false'/>");
//            $("#jobName").focus();
//            creating = false;
//            return;
//        }

        // validation of job comment
        if ($("textarea").val() == "<c:out value='${jsmsg_customer_comment}'/>") {
            $("textarea").val("");
        } else {
            if ($("textarea").val().length > 2000) {
                alert("<c:out value='${jsmsg_comment_must_be_less}'/>");
                $("textarea").focus();
                creating = false;
                return;
            }
        }

		if (attachmentUploading) {
            alert("<c:out value='${msg_job_attachment_uploading}'/>");
            creating = false;
            return;
        }

        if (attributeRequired && $("#attributeString").val() == "") {
            alert("<c:out value='${msg_set_job_attributes}'/>");
            creating = false;
            return;
        }

        var fileMapFileProfile = "";
        var entryIds = "";
        var os = $("[name='fileProfile']");
        for (var i = 0; i < os.length; i++)
        {
            var fileId = os[i].id.substring(2);
            var fileProfile = os[i].value;
            fileMapFileProfile += fileId + "-" + fileProfile + ",";
            entryIds += fileId + ",";
        }
        $("#fileMapFileProfile").val(fileMapFileProfile);

        // validation of target locales
        var checkTrgLocalUrl = "<%=checkTargetLocalesUrl%>&entryIds=" + entryIds + "&l10Nid=" + l10Nid;
        $("#createJobForm").attr("enctype","application/x-www-form-urlencoded");
        $("#createJobForm").ajaxSubmit({
   			type: 'post',
            url: checkTrgLocalUrl,
            dataType:'text',
            timeout:100000000,
            success: function(data) {
            	if (data != "") {
            		creating = false;
            		var ids = data.split(",");
				    for (var i = 0; i < ids.length; i++) {
				    	$("#tr"+ids[i]).css("color", "red");
			    	}
				    alert("<c:out value='${msg_blaise_no_workflow}'/>");
            		return;
            	} else {
                    alert('<%=bundle.getString("msg_job_create_successful")%>');
                    creating = false;

                    $("#createJobForm").attr("target", "_self");
                    $("#createJobForm").attr("action", "<%=createBlaiseJobUrl%>");
                    $("#createJobForm").attr("enctype","application/x-www-form-urlencoded");
                    $("#createJobForm").attr("encoding","application/x-www-form-urlencoded");

                    var blaiseConnectorId = $("#blaiseConnectorId").val();
                    var attributeString = $("#attributeString").val();
                    var fileMapFileProfile = $("#fileMapFileProfile").val();
                    var userName = $("#userName").val();
                    var priority = $("#priority").val();
                    var comment = $("#comment").val();
					var attachment =  $("#attachment").val();
					
                    var createJobUrl = "<%=createBlaiseJobUrl%>" 
                    	+ "&blaiseConnectorId=" + blaiseConnectorId
                    	+ "&attributeString=" + attributeString
                    	+ "&fileMapFileProfile=" + fileMapFileProfile
                    	+ "&userName=" + userName
                    	+ "&priority=" + priority
                    	+ "&comment=" + comment
                    	+ "&attachment=" + attachment;

                    $("#createJobForm").ajaxSubmit({
               			type: 'post',
                        url: createJobUrl,
                        dataType:'text',
                        timeout:100000000,
                        success: function(data) {
                      	    clearFieldValues();
                        }
                    });
            	}
            }
        });

    });

});
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides();">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

    <div id="blaiseFileListHeader">
        <span class="mainHeading"><%=title%></span>
        <table>
            <tr><td height="10">&nbsp;</td></tr>
            <tr><TD class="standardText"><%=entryListHelperText%> Blaise server: <A class='standardHREF' href="<%=blc.getUrl()%>" target="_blank"><b><%=blc.getUrl()%></b></A></TD></tr>
        </table><br/>
    </div>

    <!-- Start of Inbox Entries List -->
    <div id="blaiseEntryListDiv">
	<form name="blaiseEntriesForm" id="blaiseEntriesForm" method="post">
		<input type="hidden" name="blcId" value="<%=blc.getId()%>" />
		<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="min-width:1024px;" >
		    <tr valign="top">
		        <td align="right">
		            <amb:tableNav bean="blaiseEntryList" key="blaiseEntryKey" pageUrl="self" />
		        </td>
		    </tr>
		    <tr>
		        <td>
			        <amb:table bean="blaiseEntryList" id="blaiseInboxEntry" key="blaiseEntryKey"
			        	dataClass="com.globalsight.connector.blaise.vo.TranslationInboxEntryVo"
			        	pageUrl="self" hasFilter="true" 
			        	emptyTableMsg="msg_blaise_inbox_entry_none">

		            <amb:column label="checkbox" width="2%">
		                <input type="checkbox" name="blaiseInboxEntryIds" value="<%=blaiseInboxEntry.getId()%>" onclick="setButtonState();">
		            </amb:column>

		            <amb:column label="lb_blaise_id" sortBy="<%=BlaiseInboxEntryComparator.RELATED_OBJECT_ID%>" filter="relatedObjectIdFilter" filterValue="<%=relatedObjectIdFilter%>" width="3%">
						<%=blaiseInboxEntry.getRelatedObjectId()%>
		            </amb:column>

		            <amb:column label="" width="5px">&nbsp;</amb:column>

		            <amb:column label="lb_source_locale" sortBy="<%=BlaiseInboxEntryComparator.SOURCE_LOCALE%>" filterSelect="sourceLocaleFilter" filterValue="<%=sourceLocaleFilter%>" width="15%">
		                <%=blaiseInboxEntry.getDisplaySourceLocale() == null ? "" : blaiseInboxEntry.getDisplaySourceLocale()%>
		            </amb:column>

		            <amb:column label="lb_target_locale" sortBy="<%=BlaiseInboxEntryComparator.TARGET_LOCALE%>" filterSelect="targetLocaleFilter" filterValue="<%=targetLocaleFilter%>" width="15%">
		                <%=blaiseInboxEntry.getDisplayTargetLocale() == null ? "" : blaiseInboxEntry.getDisplayTargetLocale()%>
		            </amb:column>

		            <amb:column label="lb_description" sortBy="<%=BlaiseInboxEntryComparator.DESCRIPTION%>" filter="descriptionFilter" filterValue="<%=descriptionFilter%>" width="10%">
		                <%=blaiseInboxEntry.getDescription() == null ? "" : blaiseInboxEntry.getDescription()%>
		            </amb:column>

		            <amb:column label="lb_source_revision" width="10%">
		                <%=blaiseInboxEntry.getSourceRevision()%>
		            </amb:column>

		            <amb:column label="lb_start_date" sortBy="<%=BlaiseInboxEntryComparator.WORKFLOW_START_DATE%>" width="10%">
		                <%=dateFormat.format(blaiseInboxEntry.getWorkflowStartDate())%>
		            </amb:column>

		            <amb:column label="lb_due_date" sortBy="<%=BlaiseInboxEntryComparator.DUE_DATE%>" width="10%">
		                <%=dateFormat.format(blaiseInboxEntry.getDueDate())%>
		            </amb:column>

		            <amb:column label="lb_job_id" filter="jobIdFilter" filterValue="<%=jobIdFilter%>" width="25%">
			            <%=blaiseInboxEntry.getJobIdsForDisplay() == null ? "" : blaiseInboxEntry.getJobIdsForDisplay()%>
		            </amb:column>

		        	</amb:table>
	        	</td>
		    </tr>
		    <tr style="padding-top: 5px;">
		    	<td><amb:tableNav  bean="blaiseEntryList" key="blaiseEntryKey" pageUrl="self" scope="25,50,100" showTotalCount="false"/></td>
		    </tr>
		    <tr>
		        <td style="padding-top:5px" align="left">
		            <!-- 
		            <INPUT type="BUTTON" name="claimBtn" id="claimBtn" VALUE="Claim" disabled onclick="claim();">
		             -->
		            <INPUT type="BUTTON" name="goToCreateJobBtn" id="goToCreateJobBtn" VALUE="<%=bundle.getString("lb_go_to_create_job")%>" onClick="goToCreateJob();" disabled/>
		        </td>
		    </tr>
		</table>
	</form>
	</div>
    <!-- End of Inbox Entries List -->

    <div id="blaiseCreateJobHeader" style="display: none">
        <span class='mainHeading'><%=createJobTitle%></span><p>
        <span id="creatingJobs" style="color:red"><%=creatingJobsNum %> <%=bundle.getString("lb_jobs_creating")%></span><p>
        <table cellspacing=0 cellpadding=0 border=0 class=standardText>
        	<tr><td width="100%"><%=createHelperText%></td></tr>
        	<tr><td>&nbsp;</td></tr>
        	<tr><td width="100%"><%=noteFNameAsJobName%></td></tr>
        </table>
    </div>


    <!-- Start of Create Job Section -->
    <div id="createJobDiv" style="margin-left:0px; margin-top:0px; display:none;" class="standardText">
        <form name="createJobForm" id="createJobForm" method="post" action="" enctype="multipart/form-data">
            <input type="hidden" id="attributeString" name="attributeString" value="" />
            <input type="hidden" id="fileMapFileProfile" name="fileMapFileProfile" value="" />
            <input type="hidden" id="userName" name="userName" value="<%=userName%>" />
            <input type="hidden" id="blaiseConnectorId" name="blaiseConnectorId" value="<%=blc.getId()%>" />

        <table class="listborder" cellspacing="0" cellpadding="0" width="979" align="left" border="0">
            <tr>
            	<td>
	                <table cellSpacing="10" cellPadding="0" width="100%" align="center" border="0" table-layout="fixed">
                    	<tr>
	                        <td colspan="3" height="265px">
	                            <table class="listborder" width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
	                                <tr class="listborder" height="30">
	                                    <td width="490px" class="titletext" style="padding-left:3px; border-bottom:1px solid #0C1476;"><c:out value='${lb_name}'/></td>
	                                    <td width="196px" class="titletext" style="border-bottom:1px solid #0C1476;"><c:out value="${lb_file_profile}"/></td>
	                                    <td width="245px" class="titletext" style="border-bottom:1px solid #0C1476;">Target Locale</td>
	                                    <td width="49px" class="titletext" style="border-bottom:1px solid #0C1476;">&nbsp;</td>
	                                </tr>
	                                <tr>
	                                    <td id="uploadArea" height="265px" colspan="4">
	                                    	<div style="width:100%; height:265px; overflow-x:hidden; overflow-y:auto;">
	                                    		<table id="fileQueue" style="border-collapse:collapse;"></table>
	                                    	</div>
	                                    </td>
	                                </tr>
	                                <tr height="40">
	                                    <td align="left" valign="middle" style="border-top:1px solid #0C1476;">
                                            &nbsp;<input type="button" onclick="showEntryList();" title="<c:out value='${lb_blaise_create_job_add_file_tip}'/>" class="standardBtn_mouseout" style="width:90px;" value="<c:out value='${lb_add_files}'/>">
                                            <span class="footertext"><c:out value="${lb_total}"/>: <span id="fileNo">0</span> - <c:out value='${lb_uploaded}'/>: <span id="fileNo2">0</span></span>
                                            &nbsp;&nbsp;&nbsp;&nbsp;
                                            <select id="priority" name="priority" class="select" style="font-size:9pt;padding-top:3px;padding-bottom:3px;">
				                                <option value="1">1 - <c:out value='${highest}'/></option>
				                                <option value="2">2 - <c:out value='${major}'/></option>
				                                <option value="3" selected="true">3 - <c:out value='${normal}'/></option>
				                                <option value="4">4 - <c:out value='${lower}'/></option>
				                                <option value="5">5 - <c:out value='${lowest}'/></option>
				                            </select>
                                        </td>
                                        <td align="left" style="border-top:1px solid #0C1476;">
                                        	<input id="cleanMap" type="button" class="standardBtn_mouseout" style="width:90px;" value="<c:out value='${lb_clear_profile}'/>" title="<c:out value='${lb_create_job_clean_map_tip}'/>">
                                        </td>
                                        <td colspan="2" style="border-top:1px solid #0C1476;" align="left" valign="middle">
                                        	<input id="attributeButton" type="button" class="standardBtn_mouseout" style="width:90px;" value="<c:out value='${lb_job_attributes}'/>" title="<c:out value='${lb_job_attributes}'/>">
                                        </td>
	                                </tr>
	                            </table>
	                        </td>
	                    </tr>
	                    <tr>
	                        <td height="50" colspan="3"><textarea class="textarea" id="comment" name="comment" style="color:#cccccc"><c:out value="${jsmsg_customer_comment}"/></textarea></td>
	                    </tr>
	                    <tr>
	                        <td colspan="3">
	                            <table cellSpacing="0" cellPadding="0" width="100%" border="0">
	                                <tr>
	                                    <td width="18%" height="25" valign="middle" style="font-family:Arial,Helvetica,sans-serif;font-size:10pt;">
	                                        <c:out value="${lb_attachment}"/> / <c:out value="${lb_reference_file}"/>:
	                                    </td>
	                                    <td width="40%" valign="middle"><div id="attachmentArea" style="border:1px solid #0C1476;padding-left:0;height:25px;line-height:25px"></div>
	                                    <input type='hidden' name='attachment' id='attachment' value="">
	                                    </td>
	                                    <td width="5%" valign="middle" align="left"><div id="delAtt" style="display:none;"><img src="/globalsight/images/createjob/delete.png" style="cursor:pointer;padding-top:4px;" onclick="delAttc();"></div></td>
	                                    <td width="10%">
	                                        <input type="button" id="attachmentFileBtn"  title="<c:out value='${lb_create_job_browse_tip}'/>" onclick="clickAtt()" class="standardBtn_mouseout" style="width:90px;" value="<c:out value='${lb_browse}'/>" />
	                                        <input type="file" id="selectedAttachmentFile" name="selectedAttachmentFile" onchange="checkAndUpload();"/>
	                                    </td>
	                                    <td width="25%" align="right">
                               				<input id="create" type="button" class="standardBtn_mouseout" style="width:90px;" value="<c:out value='${lb_create_job}'/>" title="<c:out value='${lb_create_job}'/>">
                               				&nbsp;&nbsp;&nbsp;
                           					<input id="cancel" type="button" class="standardBtn_mouseout" style="width:90px;" value="<c:out value='${lb_cancel}'/>" title="<c:out value='${lb_cancel}'/>">
	                                    </td>
	                                </tr>
	                            </table>
	                        </td>
	                    </tr>
	                </table>
            	</td>
            </tr>
        </table>
        </form>
    </div>
    <iframe name="none_iframe" width="0" height="0" scrolling="no" style="display: none"> </iframe>
    <!-- End of Create Job Section -->

</DIV>
</DIV>
<body>
</HTML>
