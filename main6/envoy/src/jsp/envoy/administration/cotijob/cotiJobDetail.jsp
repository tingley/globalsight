<%@page import="com.globalsight.everest.coti.util.COTIConstants"%>
<%@page import="com.globalsight.everest.coti.util.COTIUtilEnvoy"%>
<%@page import="java.io.File"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
    com.globalsight.everest.servlet.util.SessionManager,
     com.globalsight.everest.permission.PermissionGroup, 
     com.globalsight.everest.util.comparator.StringComparator,
     com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
     com.globalsight.util.GlobalSightLocale,
     com.globalsight.everest.company.CompanyThreadLocal,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserComparator,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,         
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.everest.comment.Comment,
         com.globalsight.everest.jobhandler.JobImpl,
         com.globalsight.everest.coti.COTIDocument,
         com.globalsight.everest.coti.COTIPackage,
         com.globalsight.everest.coti.COTIProject,
         java.text.MessageFormat,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle, java.util.Vector"
         session="true"
%>
<%
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
	ResourceBundle bundle = PageHandler.getBundle(session);
    User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
    Locale uilocale = (Locale) session
            .getAttribute(WebAppConstants.UILOCALE);
    String companyIdWorkingFor = CompanyThreadLocal.getInstance()
            .getValue();
    String userName = "";
    String password = "";
    if (user != null)
    {
        userName = user.getUserName();
        password = user.getPassword();
    }
    Integer creatingJobsNum = (Integer) request
            .getAttribute("creatingJobsNum");
    
    String projectId = (String) request.getAttribute(WebAppConstants.JOB_ID);
    COTIProject cproject = (COTIProject) request.getAttribute("cotiProject");
    COTIPackage cpackage = (COTIPackage) request.getAttribute("cotiPackage");
    List<COTIDocument> documents = (List<COTIDocument>) request.getAttribute("cotiDocuments");
    GlobalSightLocale sourceLocale = (GlobalSightLocale) request.getAttribute("sourceLocale");
    GlobalSightLocale targetLocale = (GlobalSightLocale) request.getAttribute("targetLocale");
    JobImpl gsjob = (JobImpl) request.getAttribute("gsjob");
    boolean jobCreated = false;
    if (gsjob != null)
    {
        jobCreated = true;
    }
    boolean isOkForCreate = false;
    if (cproject != null && cproject.getStatus() != null 
            && cproject.getStatus().equals(COTIConstants.project_status_started))
    {
        isOkForCreate = true;
    }
    
    
    String getUrl = "/globalsight/ControlServlet?linkName=cotiJobDetail&pageName=cojs&jobId=" + projectId + "&fromJobs=true";
%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<title><%= bundle.getString("lb_coti_job") %> - <%=cproject.getCotiProjectName() %></title>
<style type="text/css">
  .sourceFile{ position:absolute;height:21px;filter:alpha(opacity=0); opacity:0; width:86px;cursor:pointer;}
  .attachmentFile{ position:absolute;height:21px;filter:alpha(opacity=0); opacity:0; width:86px;cursor:pointer;}
</style>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jQuery.md5.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "";
var helpFile = "<%= bundle.getString("help_coti_job") %>";

var mapped = false;// mark whether the target locales are mapped in the page
var l10Nid = 0;// the localization profile id in use for current page
var totalFileNo = 0;// total files 
var uploadedFileNo = 0;// file number that have been uploaded
var totalSize = 0;// total size of uploaded files
var uploadFailedNo = 0;// number of upload failed files
var progressBarWidth = 642;// width of progress bar
var switchedFileArray = new Array();// selected files from uploaded window
var controlWindow = null; // child window of uploaded files

var jobCreated = <%=jobCreated ? "true" : "false" %>;
var type = "";

function confirmJump()
{
    return true;
}

$(document).ready(function() {
	
    $(".standardBtn_mouseout").mouseover(function(){
        $(this).removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $(this).removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    }).css("width","90px");
    
    $("#fileQueue").width($("#uploadArea").width() - 2);
    $("#targetLocaleArea").height($("#localeArea").height() - 3);
    $("#SourceLocaleArea").height($("#srclocaleArea").height() - 3);
    // action of job name text
    <%  if (jobCreated || !isOkForCreate) {%>
	    $("#create").prop('disabled', true);
	    $("#cleanMap").prop('disabled', true);
	    $("#jobName").prop('disabled', true);
	    $("#jobName").attr("value","<%=gsjob != null ? gsjob.getJobName() : "" %>");
	    $("#jobName").css("color","black");
	    $("textarea").prop('disabled', true);
	    <%
	    Comment comment = null;
	    if (gsjob != null)
	    {
		    List comments = gsjob.getJobComments();
		    
		    if (comments != null && comments.size() > 0)
		    {
		        comment = (Comment) comments.get(0);
		    }
	    }
	    %>
	    $("textarea").val("<%=comment == null ? "" : comment.getComment() %>");
	    $("textarea").css("color","black");
	    
	    $("#priority").prop('disabled', true);
	    $("#priority").val("<%=gsjob != null ? gsjob.getPriority() : "3"%>");
	    
	    <% if (jobCreated) {%>
	    	$("#creatingJobs").html("The GlobalSight job has been created. ");
	    <% } else if (!isOkForCreate) { %>
	    	$("#creatingJobs").html("Create GlobalSight job for COTI project when it is \"started\"");
	    <% } %>
    <% } else { %>
    $("#jobName").focus(function() {
        if ($("#jobName").attr("value") == "<%= bundle.getString("jsmsg_customer_job_name") %>") {
            $("#jobName").attr("value","<%=cproject.getCotiProjectName() %>");
            $("#jobName").css("color","black");
        }
    }).blur(function() {
        if ($("#jobName").attr("value") == "") {
            $("#jobName").attr("value","<%= bundle.getString("jsmsg_customer_job_name") %>");
            $("#jobName").css("color","#cccccc");
        }
    });
    // action of textarea
    $("textarea").focus(function() {
        if ($("textarea").val() == "<%= bundle.getString("jsmsg_customer_comment") %>") {
            $("textarea").val("");
            $("textarea").css("color","black");
        }
    }).blur(function() {
        if ($("textarea").val() == "") {
            $("textarea").val("<%= bundle.getString("jsmsg_customer_comment") %>")
            $("textarea").css("color","#cccccc");
        }
    });
    // action of cleanmap button
    $("#cleanMap").click(function()
    {
        $("[name='fileProfile']").each(function(){
            addEmptyOption(this);
        });
        $("[name='fileProfile']").attr("value", "");
        $("[name='fileProfile'] option").attr("disabled", false);
        
        mapped = false;
        l10Nid = 0;
        $("#attributeButtonDIV").hide();
        attributeRequired = false;
        $(this).blur();
    });
        
    // *************************************action of create button*************************************
    var creating = false;
    $("#create").click(function()
    {
        $(this).blur();
        if (creating) {
            return;
        }
        creating = true;
        // validation of file profiles
        if ($(".uploadifyQueueItem").length == 0) {
            alert("<%= bundle.getString("msg_job_add_files") %>");
            creating = false;
            return;
        }
        var flag = true;
        $("[name='fileProfile']").each(function() {
            if ($(this).children('option:selected').val() == "")
            {
                flag = false;
                return;
            }
        })
        var no1 = $(".uploadifyQueueItem").length;
        var no2 = $("[name='fileProfile']").length;
        if (!flag || no1 != no2)
        {
            alert("<%= bundle.getString("jsmsg_choose_file_profiles_for_all_files") %>");
            creating = false;
            return;
        }
        // validation of job name
        var jobName = Trim($("#jobName").attr("value"));
        if (jobName == "" || jobName == "<%= bundle.getString("jsmsg_customer_job_name") %>")
        {
            alert("<%= bundle.getString("jsmsg_customer_job_name") %>");// Please enter a Job Name.
            $("#jobName").focus();
            creating = false;
            return;
        }
        var jobNameRegex = /[\\/:;\*\?\|\"<>&%]/;
        if (jobNameRegex.test(jobName))
        {
            alert("<%= bundle.getString("jsmsg_invalid_job_name_1") %>");
            $("#jobName").focus();
            creating = false;
            return;
        }
        if ($("textarea").val() == "<%= bundle.getString("jsmsg_customer_comment") %>") {
            $("textarea").val("");
        } else {
            if ($("textarea").val().length > 2000) {
                alert("<%= bundle.getString("jsmsg_comment_must_be_less") %>");
                $("textarea").focus();
                creating = false;
                return;
            }
        }
        
        $("#createJobForm").attr("target", "_self");
        document.createJobForm.action = "<%= getUrl%>&theAction=createJob&userName=<%=userName%>";
        document.createJobForm.submit();
    });
    <% } %>
    // *************************************cancel button*************************************
    $("#cancel").click(function()
    {
        document.location.href="/globalsight/ControlServlet?activityName=cotiJobs";
    });
    
    // *************************************create result*************************************
    $("#targetLocales").height(425);//for Chrome
    var msg = "<c:out value='${create_result}'/>";
    if (msg != "") {
        alert(msg);
        document.location.href="/globalsight/ControlServlet?activityName=cotiJobs";
    }
});

function addFullDivElement(id, filePath, fileName, fileSize) {
    fileSize = parseInt(fileSize);
    var size = parseNo(fileSize);
    
    var iiihtml = '<div id="bp' + id + '" class="uploadifyQueueItem">' 
    // div of progress bar container
    + '<div id="ProgressDiv' + id + '" class="uploadifyProgress">'
 	// div of document id
 	+ '<div class="cotidocumentid" onclick="mapTargetLocales(ProgressDiv' + id + ')">' + id + '</div>'
    // div of file name
    + '<div class="cotifileInfo" id="FileInfo' + id + '" onclick="mapTargetLocales(ProgressDiv' + id + ')">' 
    + fileName + '<input type="hidden" id="Hidden' + id + '" name="jobWholeName" value="' + fileName + '">'
    + '<input type="hidden" name="jobFilePath" value="' + filePath + '"></div>' 
    // detail link
    + '<div class="cotidetail"><a href="javascript:showTip(\'' + id + '\')">(more)</a></div>'
    // div of file size
    + '<div class="cotifilesize" onclick="mapTargetLocales(ProgressDiv' + id + ')">' + size + '</div>'
    // div of complete icon
    + '<div class="cotiicon" onclick="mapTargetLocales(ProgressDiv' + id + ')"><span class="icons"></span></div>'
    // div of file profile select
    + '<div class="cotiprofile" onclick="mapTargetLocales(ProgressDiv' + id + ')"><span class="profileArea"></span></div>'
    + '</div></div>'
    + '<div id="bptip' + id + '" class="tip_cj" style="display:none">' + filePath + '</div>';
    
    $("#fileQueue").append(iiihtml);
    totalFileNo++;
    totalSize += fileSize;
    $("#fileNo").html(totalFileNo);
    $("#totalFileSize").html(parseNo(totalSize));
}

function addReferenceFileElement(id, filePath) {
    
    var iiihtml = '<input type="checkbox" name="referenceFilePath" value="' + filePath + '" checked />' + filePath + '<br>';
    
    $("#referenceFilesDiv").append(iiihtml);
}

function queryFileProfile(id)
{
    $("#ProgressBar" + id).css("background-color", "grey");
    $("#bp" + id).find(".icons").html("<img src='/globalsight/images/createjob/success.png' style='width:20px;height:20px'>");
    var profile = $("#bp" + id).find(".profileArea");
    var theFileName = $("#Hidden" + id).attr("value");
    $.get("<%= getUrl %>", 
            {"theAction":"queryFileProfile","fileName":theFileName,"l10Nid":l10Nid,"no":Math.random(), "userName":"<%=userName%>"}, 
            function(data){
                profile.html("<select id='fp" + id
                        + "' name='fileProfile' style='width:143;z-index:50;padding-top:2px;padding-bottom:2px;' " 
                        + "onchange='disableUnavailableFileProfiles(this)'><option value=''></option>"
                        + data
                        + "</select>");
                if (l10Nid == 0)
                {
                    l10Nid = getL10NFromSelectValue($("#fp"+id).children('option:selected').val());
                }
                disableUnavailableFileProfiles(document.getElementById("fp" + id));
                if (!mapped && $("#fp" + id).val() != "") {
                    mapTargetLocales(document.getElementById("ProgressDiv" + id));
                }
        });
}

function disableUnavailableFileProfiles(o)
{
	if (o == null)
	{
		return;
	}
	
    var selectValue = o.value;
    if (l10Nid == 0)
    {
        l10Nid = getL10NFromSelectValue(selectValue);
    }
    if (l10Nid != 0)
    {
        var selects = $("[name='fileProfile']");
        for (var j = 0; j < selects.length; j++)
        {
            var aSelect = selects[j];
            var options = aSelect.options;
            var enable = new Array();
            for (var i = 1; i < options.length; i++)
            {
                if (l10Nid == getL10NFromSelectValue(options[i].value))
                {
                    options[i].disabled = false;// enable this option
                    enable.push(options[i]);// save the option in the array
                }
                else
                {
                    options[i].selected = false;
                    options[i].disabled = true;
                }
            }
            if (enable.length == 1)
            {
                enable[0].selected = true;
                removeEmptyOption(aSelect);
            }
            else if (enable.length == 0)
            {
                options[0].selected = true;
            }
            else if (enable.length > 1)
            {
                for (var z = 0; z < enable.length; z++)
                {
                    if (selectValue == enable[z].value && aSelect.value == "")
                    {
                        enable[z].selected = true;
                        break;
                    }
                }
            }
        }
    }
}

function showTip(id)
{
    if ($("#bptip" + id).css("display") == "none") {
        $("#bptip" + id).css("display", "block");
        $("#bp" + id).find(".cotidetail").html('<a href="javascript:hideTip(\'' + id + '\')">(hide)</a>');
    }
}
function hideTip(id)
{
    if ($("#bptip" + id).css("display") == "block") {
        $("#bptip" + id).css("display", "none");
        $("#bp" + id).find(".cotidetail").html('<a href="javascript:showTip(\'' + id + '\')">(more)</a>');
    }
}

// This is not used any more.
function showWarningMessage(emptyFiles, largeFiles, existFiles)
{
    var msg = "";
    for (var i = 0; i < emptyFiles.length; i++) {
        msg += emptyFiles[i] + " <%= bundle.getString("msg_job_create_empty_file") %>\r\n";
    }
    for (var i = 0; i < largeFiles.length; i++) {
        msg += largeFiles[i] + " <%= bundle.getString("msg_job_create_large_file") %>\r\n";
    }
    for (var i = 0; i < existFiles.length; i++) {
        msg += existFiles[i] + " <%= bundle.getString("msg_job_create_exist") %>\r\n";
    }
    alert(msg);
}

function parseNo(no) {
    var size = Math.round(parseInt(no) / 1024 * 100) * 0.01;
    var m = "KB";
    if (size > 1000) {
        size = Math.round(size * 0.001 * 100) * 0.01;
        m = "MB";
    }
    size = size.toFixed(2);
    return size + m;
}

function mapTargetLocales(o)
{
	// do nothing now
}

function Trim(str)
{
    if(str=="") return str;
    var newStr = ""+str;
    RegularExp = /^\s+|\s+$/gi;
    return newStr.replace( RegularExp,"" );
}

function addEmptyOption(o)
{
    var ops = o.options;
    for (var i = 0; i < ops.length;i++)
    {
        if (ops[i].value == "")
        {
            return;
        }
    }
    var no = new Option();
    no.value = "";
    no.text = "";
    no.title = "";
    o.options[ops.length] = no;
    
    SortD(o);
}

function SortD(aSelect){
    var temp_opts = new Array();
    var temp = new Object();
    for(var i=0; i<aSelect.options.length; i++){
        temp_opts[i] = aSelect.options[i];
    }

    for(var x=0; x<temp_opts.length-1; x++){
        for(var y=(x+1); y<temp_opts.length; y++){
            if(temp_opts[x].text.toLowerCase() > temp_opts[y].text.toLowerCase()){
                temp = temp_opts[x].text;
                temp_opts[x].text = temp_opts[y].text;
                temp_opts[y].text = temp;
                
                temp = temp_opts[x].value;
                temp_opts[x].value = temp_opts[y].value;
                temp_opts[y].value = temp;

                temp = temp_opts[x].title;
                temp_opts[x].title = temp_opts[y].title;
                temp_opts[y].title = temp;
            }
        }
    }

    for(var j=0; j<aSelect.options.length; j++){
        aSelect.options[j].value = temp_opts[j].value;
        aSelect.options[j].text = temp_opts[j].text;
        aSelect.options[j].title = temp_opts[j].title;
    }
}

function removeEmptyOption(o)
{
    var options = o.options;
    for (var i = 0; i < options.length;i++)
    {
        if (options[i].value == "")
        {
            o.remove(options[i]);
        }
    }
}

function getL10NFromSelectValue(str)
{
    if (typeof(str) == "undefined" || str == "")
    {
        return 0;
    }
    var tmp = str.split(",");
    var l10id = tmp[0];
    return l10id;
}

function loadPage()
{
	loadGuides();

	<% for(int i =0; i < documents.size(); i++) 
	{
	    COTIDocument cd = documents.get(i);
	    String fileRef = cd.getFileRef();
	    String cdId = "" + cd.getId();
	    String filePath = COTIUtilEnvoy.getCotiDocumentPath(cpackage.getCompanyId(), cpackage, cproject, cd);
	    filePath = filePath.replace("\\", "/");
	    if (!cd.getIsTranslation())
	    {
	        %>
	        addReferenceFileElement("<%= cdId%>", "<%= filePath %>");
	        <%
	    }
	    else
	    {
	    File file = new File(filePath);
	    Long fileSize = file.length();
	    %>
	addFullDivElement("<%= cdId%>", "<%= filePath %>", "<%= fileRef %>", "<%= fileSize %>");
	queryFileProfile("<%= cdId%>");

	<% }} %>
}

</script>
</head>

<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">

<span class='mainHeading'><%= bundle.getString("lb_coti_job")%> - <%=cproject.getCotiProjectName() %></span><p>
<span id="creatingJobs">&nbsp;</span><p>

<table cellspacing=0 cellpadding=0 border=0 class=standardText>
<tr><td width="100%"><%= bundle.getString("helper_text_create_cotijobs") %></td></tr>
</table>

<form name="createJobForm" id="createJobForm" method="post" target="none_iframe">
<input type="hidden" id="attributeString" name="attributeString" value="">
<input type="hidden" name="userName" value="<%=userName%>">
<input type="hidden" name="projectId" value="<%=projectId%>">

<table class="listborder" cellspacing="0" cellpadding="0" width="979" align="left" border="1">
<tr>
<td>
  <table cellSpacing="10" cellPadding="0" width="100%" align="center" border="0" table-layout="fixed">
    <tr>
    <td colspan="2" height="265">
    <table class="listborder" width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr height="30">
        <td width="5%" style="border:0"><div class="titletext" style="padding-left:10px;"><%= bundle.getString("lb_id") %></div></td>
        <td width="48%" style="border:0"><div class="titletext" style="padding-left:10px;"><%= bundle.getString("lb_name") %></div></td>
        <td width="10%" style="border:0"><div align="center"><span class="titletext"><%= bundle.getString("lb_size") %></span></div></td>
        <td width="7%" style="border:0"><div align="center"><span class="titletext"><%= bundle.getString("lb_status") %></span></div></td>
        <td width="30%" style="border:0"><div align="center"><span class="titletext" style="text-align: left;"><%= bundle.getString("lb_file_profile") %></span></div></td>
      </tr>
      <tr>
        <td id="uploadArea" height="270" colspan="6">
            <div id="fileQueue" class="fileQueue"></div>
        </td>
      </tr>
      <tr>
        <td colspan="6"><div id="appl"></div><div id="attachment"></div>
            <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
                <tr>
                    <td width="100px" height="32px" align="center" valign="middle">
                    &nbsp;
                    </td>
                    <td width="100px" align="center" valign="middle"></td>
                    <td align="center" class="footertext">
                        <%= bundle.getString("lb_total") %>: <span id="fileNo">0</span>
                        (<span id="totalFileSize">0.00KB</span>)
                    </td>
                    <td width="110px" align="center"><input id="cleanMap" type="button" class="standardBtn_mouseout" value="<%= bundle.getString("lb_clear_profile") %>" title="<%= bundle.getString("lb_create_job_clean_map_tip") %>"></td>
                    <td width="70px" style="border:0">&nbsp;</td>
                </tr>
            </table>
        </td>
      </tr>
    </table>
    </td>
    <td rowspan="4" style="width:28%;height:100%">
        <div style="background-color:#738EB5;height:91%" id="targetLocales">
            <div style="font: 11pt Verdana, Geneva, sans-serif;color:white;height:20px;padding-left:5px;padding-top:4px;padding-bottom:3px">
                &nbsp;&nbsp;&nbsp;&nbsp;<%= bundle.getString("lb_source_locale") %>
            </div>
            <div id="srclocaleArea" style="background-color:white;height:40%;width:89%;left:15px;position:relative;">
                <div id="sourceLocaleArea" style="overflow:scroll;overflow-x:hidden;padding-left:5px;font-family:Arial,Helvetica,sans-serif;font-size:10pt">
                <%=sourceLocale.toString()+"("+sourceLocale.getDisplayLanguage(uilocale)+"_"+sourceLocale.getDisplayCountry(uilocale)+")" %>
                </div>
            </div>
            <div style="height:5px"></div>
            <div style="font: 11pt Verdana, Geneva, sans-serif;color:white;height:20px;padding-left:5px;padding-top:4px;padding-bottom:3px">
                &nbsp;&nbsp;&nbsp;&nbsp;<%= bundle.getString("lb_target_locale") %>
            </div>
            <div id="localeArea" style="background-color:white;height:40%;width:89%;left:15px;position:relative;">
                <div id="targetLocaleArea" style="height:40px;overflow:scroll;overflow-x:hidden;padding-left:5px;font-family:Arial,Helvetica,sans-serif;font-size:10pt">
                <%=targetLocale.toString()+"("+targetLocale.getDisplayLanguage(uilocale)+"_"+targetLocale.getDisplayCountry(uilocale)+")" %>
                </div>
            </div>
        </div>
        <div style="padding-left:30px;padding-top:15px;background-color:white">
        &nbsp;
        </div>
    </td>
  </tr>
  <tr>
    <td width="50%" height="25">
        <input class="text" maxlength="100" id="jobName" name="jobName" value="<%= bundle.getString("jsmsg_customer_job_name") %>" style="color:#cccccc"/>
    </td>
    <td width="10%">
      <select name="priority" id="priority" class="select">
          <option value="1">1 - <%= bundle.getString("highest") %></option>
          <option value="2">2 - <%= bundle.getString("major") %></option>
          <option value="3" selected="true">3 - <%= bundle.getString("normal") %></option>
          <option value="4">4 - <%= bundle.getString("lower") %></option>
          <option value="5">5 - <%= bundle.getString("lowest") %></option>
      </select>
    </td>
  </tr>
  <tr>
    <td height="50" colspan="2">
      <textarea class="textarea" name="comment" style="color:#cccccc"><%= bundle.getString("jsmsg_customer_comment") %></textarea>
    </td>
  </tr>
  <tr>
        <td colspan="2">
        <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
            <tr>
            	<td width="15%"><div id="attributeButtonDIV" style="display:none">&nbsp;</div>
                </td>
                <td width="15%"><div id="attributeButtonDIV" style="display:none">
                &nbsp;
                </td>
                <td width="15%">
                <input id="create" type="button" class="standardBtn_mouseout" value="<%= bundle.getString("lb_create_job") %>" title="<%= bundle.getString("lb_create_job") %>">
                </td>
                <td width="15%">
                <input id="cancel" type="button" class="standardBtn_mouseout" value="<%= bundle.getString("lb_cancel") %>" title="<%= bundle.getString("lb_cancel") %>">
                </td>
                <td><div id="attributeButtonDIV" style="display:none">&nbsp;</div>
                </td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <td colspan="2">
        <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
            <tr>
                <td width="100%" height="25" valign="middle" style="font-family:Arial,Helvetica,sans-serif;font-size:10pt;">
                    Check to add reference files as Job Attachment: 
                </td>
            </tr>
            <tr>
                <td width="100%" valign="middle" style="font-family:Arial,Helvetica,sans-serif;font-size:10pt;">
                <div id="referenceFilesDiv"></div>
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
<iframe name="none_iframe" width="0" height="0" scrolling="no" style="display:none">
</iframe>
</div>
</body>
</html>