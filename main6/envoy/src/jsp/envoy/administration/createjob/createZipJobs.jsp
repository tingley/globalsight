<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.company.CompanyThreadLocal"
         session="true"
%>
<%
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
    String companyIdWorkingFor = CompanyThreadLocal.getInstance().getValue();
    String userName = "";
    String password = "";
    if (user != null)
    {
        userName = user.getUserName();
        password = user.getPassword();
    }
    Integer creatingJobsNum = (Integer)request.getAttribute("creatingJobsNum");
%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<title><c:out value="${lb_create_job_without_java}"/></title>
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
<script type="text/javascript">
var guideNode = "createZipJob";
var helpFile = "<c:out value='${help_create_job}'/>";
var mapped = false;// mark whether the target locales are mapped in the page
var l10Nid = 0;// the localization profile id in use for current page
var totalFileNo = 0;// total files 
var uploadedFileNo = 0;// file number that have been uploaded
var totalSize = 0;// total size of uploaded files
var uploadFailedNo = 0;// number of upload failed files
var progressBarWidth = 642;// width of progress bar
var switchedFileArray = new Array();// selected files from uploaded window
var controlWindow = null; // child window of uploaded files
var attributeWindow = null; //chind window of job attributes
var attachmentUploading = false;
var hasAttribute = false;
var attributeRequired = false;
var tempFolder = "";
var isUploading = false;
var uploadedFiles = new Array();
var zipFiles = new Array();
var type = "";

function confirmJump()
{
    return true;
}

Date.prototype.format = function(format)
{ 
	var o = { 
		"M+" : this.getMonth()+1, //month 
		"d+" : this.getDate(), //day 
		"h+" : this.getHours(), //hour 
		"m+" : this.getMinutes(), //minute 
		"s+" : this.getSeconds(), //second 
		"q+" : Math.floor((this.getMonth()+3)/3), //quarter 
		"S" : this.getMilliseconds() //millisecond 
	} 

	if(/(y+)/.test(format)) 
	{ 
		format = format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
	} 

	for(var k in o) 
	{ 
		if(new RegExp("("+ k +")").test(format)) 
		{ 
			format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length)); 
		} 
	} 
	return format; 
} 

$(document).ready(function() {
	var date = new Date();
    tempFolder = date.format("yyyyMMddhhmm")+"-"+Math.floor(Math.random() * 1000000000);
    var action = $("#uploadSelectedFile").attr("action");
    $("#uploadSelectedFile").attr("action", action+"&tempFolder="+tempFolder);

    $(".standardBtn_mouseout").mouseover(function(){
        $(this).removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $(this).removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    }).css("width","90px");
    
    $("#fileQueue").width($("#uploadArea").width() - 2);
    $("#targetLocaleArea").height($("#localeArea").height() - $("#localeButtonArea").height() - 3);
    // action of job name text
    $("#jobName").focus(function() {
        if ($("#jobName").attr("value") == "<c:out value='${jsmsg_customer_job_name}'/>") {
            $("#jobName").attr("value","");
            $("#jobName").css("color","black");
        }
    }).blur(function() {
        if ($("#jobName").attr("value") == "") {
            $("#jobName").attr("value","<c:out value='${jsmsg_customer_job_name}'/>");
            $("#jobName").css("color","#cccccc");
        }
    });
    // action of textarea
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
    // action of checkall button
    $("#checkAll").click(function()
    {
        $("[name='targetLocale']").attr("checked",true);
    });
    // action of uncheckall button
    $("#uncheckAll").click(function()
    {
        $("[name='targetLocale']").attr("checked",false);
    });
    // action of cleanmap button
    $("#cleanMap").click(function()
    {
        $("[name='fileProfile']").each(function(){
            addEmptyOption(this);
        });
        $("[name='fileProfile']").attr("value", "");
        $("[name='fileProfile'] option").attr("disabled", false);
        $("#targetLocaleArea").show();
        $("#targetLocaleArea").html("");
        mapped = false;
        l10Nid = 0;
        $("#attributeButtonDIV").hide();
        attributeRequired = false;
        $(this).blur();
    });
    // action of uploaded files button
    $("#uploadedFiles").click(function() {
        var ctlStr = "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,width=700,height=650,left=700,top=" + (screen.height)/9;
        var winUrl = "/globalsight/ControlServlet?linkName=selectFile&pageName=CZJ&currentFolderName="+tempFolder;
        controlWindow = window.open(winUrl,"sf",ctlStr);
        $(this).blur();
        controlWindow.focus();
    });
    // action of target locale checkbox
    $("#targetLocaleArea").delegate("input:checkbox","click", function(){
        $("#tlControl").attr("checked", false);
    });
    // action of job attribute button
    $("#attributeButton").click(function(){
        var ctlStr = "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=450,left=700,top=" + (screen.height)/7;
        var winUrl = "/globalsight/ControlServlet?linkName=jobAttributes&pageName=CZJ&l10Nid="+l10Nid;
        attributeWindow = window.open(winUrl,"ja",ctlStr);
        $(this).blur();
        attributeWindow.focus();
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
            alert("<c:out value='${msg_job_add_files}'/>");
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
            alert("<c:out value='${jsmsg_choose_file_profiles_for_all_files}'/>");
            creating = false;
            return;
        }
        // validation of job name
        var jobName = Trim($("#jobName").attr("value"));
        if (jobName == "" || jobName == "<c:out value='${jsmsg_customer_job_name}'/>")
        {
            alert("<c:out value='${jsmsg_customer_job_name}'/>");// Please enter a Job Name.
            $("#jobName").focus();
            creating = false;
            return;
        }
        var jobNameRegex = /[\\/:;\*\?\|\"<>&%]/;
        if (jobNameRegex.test(jobName))
        {
            alert("<c:out value='${jsmsg_invalid_job_name_1}' escapeXml='false'/>");
            $("#jobName").focus();
            creating = false;
            return;
        }
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
        // validation of target locales
        var count = $("input[name='targetLocale']:checked").length;  
        if (count == 0 || $("#targetLocaleArea").css("display") == "none")
        {
            alert("<c:out value='${lb_import_select_target_locale}'/>");
            creating = false;
            return;
        }
        if (attachmentUploading) {
            alert("<c:out value='${msg_job_attachment_uploading}'/>");
            creating = false;
            return;
        }
        if (attributeRequired && $("#attributeString").val() == "") {
            alert("You must set required job attributes first.");
            creating = false;
            return;
        }
        $("#tmpFolderName").val(tempFolder);
        $("#createJobForm").attr("target", "_self");
        $("#createJobForm").attr("enctype","application/x-www-form-urlencoded");
        $("#createJobForm").attr("encoding","application/x-www-form-urlencoded");
        document.createJobForm.action += "&uploadAction=createJob&userName=<%=userName%>";
        document.createJobForm.submit();
    });
    // *************************************cancel button*************************************
    $("#cancel").click(function()
    {
        document.location.href="/globalsight/ControlServlet?activityName=createZipJobs";
    });
    
    // *************************************create result*************************************
    $("#targetLocales").height(425);//for Chrome
    var msg = "<c:out value='${create_result}'/>";
    if (msg != "") {
        alert(msg);
        document.location.href="/globalsight/ControlServlet?activityName=createZipJobs";
    }

    $("#selectedSourceFile").offset({top:$("#sourceFileBtn").offset().top + 2});
    $("#selectedSourceFile").offset({left:$("#sourceFileBtn").offset().left + 2});
    $("#selectedAttachmentFile").offset({top:$("#attachmentFileBtn").offset().top + 2});
    $("#selectedAttachmentFile").offset({left:$("#attachmentFileBtn").offset().left + 2});

    $("#selectedSourceFile").mouseover(function(){
        $("#sourceFileBtn").removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $("#sourceFileBtn").removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    });

    $("#selectedAttachmentFile").mouseover(function(){
        $("#attachmentFileBtn").removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $("#attachmentFileBtn").removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    });
});

function addDivForNewFile(paramArray) {
    var objs = eval(paramArray);
    var tempId = "";
    for (var i = 0; i < objs.length; i++) {
        var id = objs[i].id;
        var zipName = objs[i].zipName;
        var filePath = objs[i].path;
        var fileName = objs[i].name;
        var fileSize = parseInt(objs[i].size);

        if(objs.length > 1)
        {
        	tempId = $.md5(zipName.replace(/\\/g, "\\\\").replace(/\'/g, "\\'"));
        	runProgress(tempId,100,"normal",false);   
        	setTimeout(function(){
	        		$("#bp" + tempId).remove();
	        		for (var i = 0; i < objs.length; i++) {
	        	        var id = objs[i].id;
	        	        var zipName = objs[i].zipName;
	        	        var filePath = objs[i].path;
	        	        var fileName = objs[i].name;
	        	        var fileSize = parseInt(objs[i].size);
	        	        zipFiles.push(zipName);
	
	        	        addFullDivElement(id, zipName, filePath, fileName, fileSize, false);
	        	   		runProgress(id,100,"fast",false);
	        	  	}},2000
        	  	);
        	break;
        }
        else
        {
        	tempId = $.md5(fileName.replace(/\\/g, "\\\\").replace(/\'/g, "\\'"));
        	tempZipId = $.md5(zipName.replace(/\\/g, "\\\\").replace(/\'/g, "\\'"));
	       	runProgress(tempId,100,"normal",false);      
	       	setTimeout(function(){
	       		$("#bp" + tempId).remove();
	       		$("#bp" + tempZipId).remove();
	       		addFullDivElement(id, zipName, filePath, fileName, fileSize, false);
	       		runProgress(id,100,"fast",false);},2000
	       	);
        }      
    }
    isUploading = false;
   	$("#tmpFolderName").val(tempFolder);
}

function addDivElement(fileId, filePath, fileName, fileSize, isSwitched)
{
	addFullDivElement(fileId, fileName, filePath, fileName, fileSize, isSwitched);
	runProgress(fileId,15,"normal",isSwitched);
    runProgress(fileId,30,"normal",isSwitched);
    runProgress(fileId,45,"normal",isSwitched);
    runProgress(fileId,60,"normal",isSwitched);
    runProgress(fileId,100,"normal",isSwitched);
}

function addFullDivElement(id, zipName, filePath, fileName, fileSize, isSwitched) {
    fileSize = parseInt(fileSize);
    var size = parseNo(fileSize);
    $("#fileQueue").append('<div id="bp' + id + '" class="uploadifyQueueItem">' 
            // div of progress bar container
            + '<div id="ProgressDiv' + id + '" class="uploadifyProgress">'
            // div of file name
            + '<div class="fileInfo" id="FileInfo' + id + '" onclick="mapTargetLocales(ProgressDiv' + id + ')">' 
            + fileName + '<input type="hidden" id="Hidden' + id + '" name="jobWholeName" value="' + fileName + '">'
            + '<input type="hidden" name="jobFilePath" value="' + filePath + '">' 
            + '<input type="hidden" name="isSwitched" value="' + isSwitched + '"></div>'
            // detail link
            + '<div class="detail"><a href="javascript:showTip(\'' + id + '\')">(more)</a></div>'
            // div of file size
            + '<div class="filesize" onclick="mapTargetLocales(ProgressDiv' + id + ')">' + size + '</div>'
            // div of complete icon
            + '<div class="icon" onclick="mapTargetLocales(ProgressDiv' + id + ')"><span class="icons"></span></div>'
            // div of file profile select
            + '<div class="profile" onclick="mapTargetLocales(ProgressDiv' + id + ')"><span class="profileArea"></span></div>'
            // div of cancel button
            + '<div class="cancel"><a href="javascript:removeFile(\''+ id + '\',\'' + zipName.replace(/\\/g, "\\\\").replace(/\'/g, "\\'") + '\',' + fileSize + ',\'' + filePath.replace(/\\/g, "\\\\").replace(/\'/g, "\\'") + '\')">' 
            + '<img style="padding-top:4px" src="/globalsight/images/createjob/delete.png" border="0"/></a></div>'
            // div of progress bar
            + '<div class="uploadifyProgressBar" id="ProgressBar' + id + '"></div>'
            + '</div></div>'
            + '<div id="bptip' + id + '" class="tip_cj" style="display:none">' + filePath + '</div>');
    totalFileNo++;
    totalSize += fileSize;
    $("#fileNo").html(totalFileNo);
    $("#totalFileSize").html(parseNo(totalSize));
}

function addTempDivElement(fileName) {
	var id = $.md5(fileName);
	$("#fileQueue").append('<div id="bp' + id + '" class="uploadifyQueueItem">' 
            // div of progress bar container
            + '<div id="ProgressDiv' + id + '" class="uploadifyProgress">'
            // div of file name
            + '<div class="fileInfo" id="FileInfo' + id + '" onclick="mapTargetLocales(ProgressDiv' + id + ')">' 
            + fileName + '<input type="hidden" id="Hidden' + id + '" name="jobWholeName" value="' + id + '">'
            + '<input type="hidden" name="jobFilePath" value="">' 
            + '<input type="hidden" name="isSwitched" value=""></div>'
            // detail link
            + '<div class="detail">(more)</div>'
            // div of file size
            + '<div class="filesize" onclick="mapTargetLocales(ProgressDiv' + id + ')"></div>'
            // div of complete icon
            + '<div class="icon" onclick="mapTargetLocales(ProgressDiv' + id + ')"><span class="icons"></span></div>'
            // div of file profile select
            + '<div class="profile" onclick="mapTargetLocales(ProgressDiv' + id + ')"><span class="profileArea"></span></div>'
            // div of cancel button
            + '<div class="cancel"><img style="padding-top:3px" src="/globalsight/images/createjob/delete.png" border="0"/></div>'
            // div of progress bar
            + '<div class="uploadifyProgressBar" id="ProgressBar' + id + '"></div>'
            + '</div></div>'
            + '<div id="bptip' + id + '" class="tip_cj" style="display:none">' + fileName + '</div>');
	runProgress(id, 15, "normal",false);
	runProgress(id, 30, "normal",false);
	runProgress(id, 45, "normal",false);
    runProgress(id, 60, "normal",false);
}

function showTip(id)
{
    if ($("#bptip" + id).css("display") == "none") {
        $("#bptip" + id).css("display", "block");
        $("#bp" + id).find(".detail").html('<a href="javascript:hideTip(\'' + id + '\')">(hide)</a>');
    }
}
function hideTip(id)
{
    if ($("#bptip" + id).css("display") == "block") {
        $("#bptip" + id).css("display", "none");
        $("#bp" + id).find(".detail").html('<a href="javascript:showTip(\'' + id + '\')">(more)</a>');
    }
}

// This is not used any more.
function showWarningMessage(emptyFiles, largeFiles, existFiles)
{
    var msg = "";
    for (var i = 0; i < emptyFiles.length; i++) {
        msg += emptyFiles[i] + " <c:out value='${msg_job_create_empty_file}'/>\r\n";
    }
    for (var i = 0; i < largeFiles.length; i++) {
        msg += largeFiles[i] + " <c:out value='${msg_job_create_large_file}'/>\r\n";
    }
    for (var i = 0; i < existFiles.length; i++) {
        msg += existFiles[i] + " <c:out value='${msg_job_create_exist}'/>\r\n";
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

function uploadError(id) {
    $("#bp" + id).addClass("uploadifyError");
    $("#bp" + id).find(".icons").html("<img src='/globalsight/images/createjob/error.png' style='width:20px;height:20px'>");
    $("#ProgressBar" + id).stop(true);// true means stop all animations of this progress bar
    $("#ProgressBar" + id)
        .fadeOut(250, function() {
                $("#ProgressBar" + id).remove();
                uploadFailedNo++;
                $("#failedUpload").show().html("| <c:out value='${msg_failed}'/>: " + uploadFailedNo);
            });
}

function removeFile(id, zipName, fileSize, filePath) {
    $("#bp" + id).fadeOut(250, function() {
        var icon = $("#bp" + id).find(".icons").html();
        if (icon.indexOf("success.png") != -1) {
            uploadedFileNo--;
            $("#uploadedFileNo").html("- <c:out value='${lb_uploaded}'/>: " + uploadedFileNo);
        } else if(icon.indexOf("error.png") != -1) {
            uploadFailedNo--;
            $("#failedUpload").show().html("| <c:out value='${msg_failed}'/>: " + uploadFailedNo);
        }
        
        $("#bp" + id).remove(); // remove the file
        $("#bptip" + id).remove();// remove the tip
        switchedFileArray.remove(id); // remove the fileid from page tmp array
        if(zipName.substr(zipName.length - 4 ,zipName.length) == ".zip")
        {
        	for(i=0; i<zipFiles.length; i++)
    		{
    			if(zipFiles[i] == zipName)
    			{
    				zipFiles.splice(i,1);
    				break;
    			}
    		}
			var zip = false;
        	for(i=0; i<zipFiles.length; i++)
    		{
    			if(zipFiles[i] == zipName)
    			{
    				zip = true;
    				break;
    			}
    		}
    		if(!zip)
    		{
    			for(i=0; i<uploadedFiles.length; i++)
        		{
        			if(uploadedFiles[i] == zipName)
        			{
        				uploadedFiles.splice(i,1);
        				break;
        			}
        		}
    		}
        }
        else
        {
        	for(i=0; i<uploadedFiles.length; i++)
    		{
    			if(uploadedFiles[i] == zipName)
    			{
    				uploadedFiles.splice(i,1);
    				break;
    			}
    		}
        }
        totalFileNo--;
        totalSize -= fileSize;
        $("#fileNo").html(totalFileNo);
        $("#totalFileSize").html(parseNo(totalSize));
        // remove the file from system
        $.post("/globalsight/ControlServlet?activityName=createZipJobs", 
                {"uploadAction":"deleteFile","filePath":filePath,
                "folder":tempFolder,"no":Math.random()});
        var fileCount = $(".uploadifyQueueItem").length;
        if (fileCount == 0)
        {
            $.post("/globalsight/ControlServlet?activityName=createZipJobs", 
                    {"uploadAction":"deleteFile",
                    "folder":tempFolder,"no":Math.random()});
            $("#targetLocaleArea").show();
            $("#targetLocaleArea").html("");
            mapped = false;
            $("#tlControl").attr("checked", false);
            l10Nid = 0;
            $("#attributeButtonDIV").hide();
            attributeRequired = false;
        }
    });
}

function mapTargetLocales(o)
{
    var divId = "bp" + o.id.substring(11);
    var icon = $("#"+ divId).find(".icons").html();
    if (icon.indexOf("success.png") != -1) {
        $(".uploadifyProgress").css("background","#E5E5E5");
        o.style.background="#4F94CD";
        
        var selectId = "fp" + o.id.substring(11);
        var selectedValue = $("#"+selectId).children('option:selected').val();
        if (selectedValue == "")
        {
            $("#targetLocaleArea").hide();
            return;
        }
        if (l10Nid == 0)
        {
            l10Nid = getL10NFromSelectValue(selectedValue);
        }
        if (l10Nid != 0)
        {
            if (!mapped)
            {
                $.get("/globalsight/ControlServlet?activityName=createZipJobs", 
                        {"uploadAction":"queryTargetLocales","l10Nid":l10Nid,"no":Math.random(),"userName":"<%=userName%>"}, 
                        function(data){
                            $("#targetLocaleArea").show();
                            $("#targetLocaleArea").html(data);
                            $("#tlControl").attr("checked", true);
                            mapped = true;
                });
            }
            else
            {
                $("#targetLocaleArea").show();
            }
        }
    }
}

function runProgress(id, percentage, speed, isSwitched) {
    $("#uploadedFileNo").show();
    var wii = parseInt(percentage) / 100 * progressBarWidth;
    if(speed == "normal")
    {
	    $("#ProgressBar" + id).animate({width : wii}, "normal", function() {
	        var displayData = percentage + "%";
	        $("#bp"+ id).find(".icons").text(displayData);
	        
	        if (percentage == 100 && isSwitched) {
	            queryFileProfile(id);
	            uploadedFileNo++;
	            $("#uploadedFileNo").html("- <c:out value='${lb_uploaded}'/>: " + uploadedFileNo);
	        }
	    });
    }
    else
    {
    	$("#ProgressBar" + id).animate({width : wii}, 10, function() {
	        var displayData = percentage + "%";
	        $("#bp"+ id).find(".icons").text(displayData);
	        
	        if (percentage == 100) {
	            queryFileProfile(id);
	            uploadedFileNo++;
	            $("#uploadedFileNo").html("- <c:out value='${lb_uploaded}'/>: " + uploadedFileNo);
	        }
	    });
    }
}

function queryFileProfile(id)
{
    $("#ProgressBar" + id).css("background-color", "grey");
    $("#bp" + id).find(".icons").html("<img src='/globalsight/images/createjob/success.png' style='width:20px;height:20px'>");
    var profile = $("#bp" + id).find(".profileArea");
    var theFileName = $("#Hidden" + id).attr("value");
    $.get("/globalsight/ControlServlet?activityName=createZipJobs", 
            {"uploadAction":"queryFileProfile","fileName":theFileName,"l10Nid":l10Nid,"no":Math.random(), "userName":"<%=userName%>"}, 
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

function Trim(str)
{
    if(str=="") return str;
    var newStr = ""+str;
    RegularExp = /^\s+|\s+$/gi;
    return newStr.replace( RegularExp,"" );
}

function disableUnavailableFileProfiles(o)
{
    var selectValue = o.value;
    if (l10Nid == 0)
    {
        l10Nid = getL10NFromSelectValue(selectValue);
    }
    if (l10Nid != 0)
    {
        $.get("/globalsight/ControlServlet?activityName=createZipJobs", 
                {"uploadAction":"getAttributes","l10Nid":l10Nid,"no":Math.random()}, 
                function(ret){
                    if (ret == "true" || ret == "required") {
                        $("#attributeButtonDIV").show();
                        if (ret == "required") {
                            attributeRequired = true;
                        }
                    } else {
                        $("#attributeButtonDIV").hide();
                    }
                });
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
    
    $("#targetLocaleArea").height(371);//for Chrome
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
    if (str == "")
    {
        return 0;
    }
    var tmp = str.split(",");
    var l10id = tmp[0];
    return l10id;
}

function setBaseFolder(path)
{
    $("[name='baseFolder']").attr("value", path);
}

function closePopUp() {
    if (controlWindow != null && !controlWindow.closed)
    {
        controlWindow.close();
    }
    controlWindow = null;
    
    if (attributeWindow != null && !attributeWindow.closed)
    {
        attributeWindow.close();
    }
    attributeWindow = null;
}

function addAttachment(fileName) {
	$("#delAtt").show();
    attachmentUploading = true;
    runAttachProgress(100);
}

function addTempAttachment(fileName) {
    $("#attachmentArea").html("<div style='line-height:25px;position:absolute;background-color:#0099FF;' id='ProgressBarAttach'></div>" 
                            + "<div id='attName' style='width:336px' class='attachment_div'>" + fileName + "</div>" 
                            + "<input type='hidden' name='attachment' value=\"" + fileName + "\">");
    $("#ProgressBarAttach").height($("#attachmentArea").height());
    runAttachProgress(10);
    runAttachProgress(20);
    runAttachProgress(30);
    runAttachProgress(50);
    runAttachProgress(60);
    $("#baseStorageFolder").val(tempFolder);
}

function delAttc() {
    $("#attachmentArea").html("");
    $("#delAtt").hide();
    $("#attachmentArea").removeClass("uploadifyError");
    attachmentUploading = false;
}

function runAttachProgress(percentage) {
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

function controlBoxes() {
    var tl = document.getElementById("tlControl");
    if (tl.checked == true) {
        $("[name='targetLocale']").attr("checked", true);
    } else {
        $("[name='targetLocale']").attr("checked", false);
    }
}

function getCreatingJobsNum()
{
	$.get("/globalsight/ControlServlet?pageName=CZJ&linkName=createZipJob", 
	{"uploadAction":"getCreatingJobsNum","no":Math.random()}, 
	function(data)
	{
		$("#creatingJobs").html("<c:out value='${lb_job_creating}'/>: " + data + " <c:out value='${lb_jobs_creating}'/>");
	},
	"text");
}

function setType(t)
{
	type = t;
}

function setInputFileDisable(t)
{
	if (t == '0')
	{
		$("#selectedSourceFile").prop('disabled', false);
		$("#selectedAttachmentFile").prop('disabled', true);
	}
	else
	{
		$("#selectedSourceFile").prop('disabled', true);
		$("#selectedAttachmentFile").prop('disabled', false);
	}
}

function checkAndUpload()
{
	if (isUploading)
	{
		alert("Please wait for the last file upload.");
		emptyFileValue();
		return false;
	}

	if (type == "0")
	{
		var tempFiles = document.getElementById( "selectedSourceFile" );
		var tempFileName;
		var dupFiles = new Array();
		var temp;
		for (var j = 0; j < tempFiles.files.length; j++)
		{
			temp = tempFiles.files[j];
			tempFileName = temp.name;
			var flag = true;
			if (uploadedFiles.length > 0)
			{
				for (i = 0; i < uploadedFiles.length; i++)
				{
					if (uploadedFiles[i] == tempFileName)
					{
						dupFiles.push(tempFileName);
						flag = false;
					}
				}
			}
			if (flag)
			{
				addTempDivElement(tempFileName.replace(/\\/g, "\\\\").replace(/\'/g, "\\'"));
				uploadedFiles.push(tempFileName);
			}
		}
		if (dupFiles.length > 0)
		{
			var result = dupFiles.join("\n");
			if (dupFiles.length > 1)
			{
				alert("Below files are already in the uploaded list, ignored: \n\n" + result);
			}
			else
			{
				alert("Below file is already in the uploaded list, ignored: \n\n" + result);
			}
			if (dupFiles.length == tempFiles.files.length)
			{
				emptyFileValue();
				return false;
			}
		}
	}
	else if (type == "1")
	{
		tempFileName = $("#selectedAttachmentFile").val();
		if(tempFileName.lastIndexOf("\\") > 0)
		{
			tempFileName = tempFileName.substr(tempFileName.lastIndexOf("\\") + 1,tempFileName.length);
		}
		addTempAttachment(tempFileName.replace(/\\/g, "\\\\").replace(/\'/g, "\\'"));	
	}

    var action = $("#createJobForm").attr("action");
    $("#createJobForm").attr("action", action+"&uploadAction=uploadSelectedFile&type="+type+"&tempFolder="+tempFolder);
	$("#createJobForm").submit();
	$("#createJobForm").attr("action", action);
	isUploading = true;
	emptyFileValue();
	$("#selectedSourceFile").prop('disabled', false);
	$("#selectedAttachmentFile").prop('disabled', false);
}

function emptyFileValue()
{
	if(isIE())
	{
		$("#selectedSourceFile").replaceWith($("#selectedSourceFile").clone(true));
		$("#selectedAttachmentFile").replaceWith($("#selectedAttachmentFile").clone(true));
	}
	$("#selectedSourceFile").val('');
	$("#selectedAttachmentFile").val('');
}

function isIE() { //ie?  
    if (!!window.ActiveXObject || "ActiveXObject" in window)  
        return true;  
    else  
        return false;  
}
</script>
</head>

<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()" onunload="closePopUp()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class='mainHeading'><c:out value="${lb_create_job_without_java}"/></span><p>
<span id="creatingJobs" style="color:red"><c:out value="${lb_job_creating}"/>: <%=creatingJobsNum %> <c:out value="${lb_jobs_creating}"/></span><p>
<table cellspacing=0 cellpadding=0 border=0 class=standardText><tr><td width="100%"><c:out value="${helper_text_create_job}"/> Click "Add Files" button to add source files one by one.</td></tr></table>
<form name="createJobForm" id="createJobForm" method="post" action="/globalsight/ControlServlet?pageName=CZJ&linkName=createZipJob" enctype="multipart/form-data" target="none_iframe">
<input type="hidden" id="tmpFolderName" name="tmpFolderName" value="">
<input type="hidden" id="baseStorageFolder" name="baseStorageFolder" value="">
<input type="hidden" name="baseFolder" value="">
<input type="hidden" id="attributeString" name="attributeString" value="">
<input type="hidden" name="userName" value="<%=userName%>">

<table class="listborder" cellspacing="0" cellpadding="0" width="979" align="left" border="1">
<tr>
<td>
  <table cellSpacing="10" cellPadding="0" width="100%" align="center" border="0" table-layout="fixed">
    <tr>
    <td colspan="2" height="265">
    <table class="listborder" width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr height="30">
        <td width="50%" style="border:0"><div class="titletext" style="padding-left:10px;"><c:out value="${lb_name}"/></div></td>
        <td width="10%" style="border:0"><div align="center"><span class="titletext"><c:out value="${lb_size}"/></span></div></td>
        <td width="10%" style="border:0"><div align="center"><span class="titletext"><c:out value="${lb_status}"/></span></div></td>
        <td width="20%" style="border:0"><div align="center"><span class="titletext"><c:out value="${lb_file_profile}"/></span></div></td>
        <td width="10%" style="border:0">&nbsp;</td>
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
                    <td width="100px" height="30px" align="center" valign="middle" onmouseover="setInputFileDisable(0)">
                    <input type="button" id="sourceFileBtn" class="standardBtn_mouseout" value="<c:out value='${lb_add_files}'/>">
                    <input type="file" class="sourceFile" multiple value="Add File" name="selectedSourceFile" id="selectedSourceFile" onclick="setType(0)" onchange="checkAndUpload()" title="<c:out value='${lb_create_job_add_file_tip}'/>">
                    </td>
                    <td width="100px" align="center" valign="middle"><input id="uploadedFiles" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_uploaded_files}'/>" title="<c:out value='${lb_create_job_uploaded_files_tip}'/>"></td>
                    <td align="center" class="footertext">
                        <c:out value="${lb_total}"/>: <span id="fileNo">0</span>
                        (<span id="totalFileSize">0.00KB</span>)
                        <span id="uploadedFileNo" style="display:none">- <c:out value='${lb_uploaded}'/>: 0</span>
                        <span id="failedUpload" style="display:none">| <c:out value="${msg_failed}"/>: 0</span>
                    </td>
                    <td width="110px" align="center"><input id="cleanMap" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_clear_profile}'/>" title="<c:out value='${lb_create_job_clean_map_tip}'/>"></td>
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
                &nbsp;&nbsp;&nbsp;<input type="checkbox" id="tlControl" onclick="javascript:controlBoxes()" title="Select/Deselect All">&nbsp;<c:out value="${lb_target_locales}"/>
            </div>
            <div id="localeArea" style="background-color:white;height:88%;width:89%;left:15px;position:relative;">
                <div id="targetLocaleArea" style="overflow:scroll;overflow-x:hidden;padding-left:5px">
                </div>
            </div>
            <div style="height:5px"></div>
        </div>
        <div style="padding-left:30px;padding-top:15px;background-color:white">
            <input id="create" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_create_job}'/>" title="<c:out value='${lb_create_job}'/>">&nbsp;&nbsp;&nbsp;
            <input id="cancel" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_cancel}'/>" title="<c:out value='${lb_cancel}'/>">
        </div>
    </td>
  </tr>
  <tr>
    <td width="50%" height="25">
        <input class="text" maxlength="100" id="jobName" name="jobName" value="<c:out value='${jsmsg_customer_job_name}'/>" style="color:#cccccc"/>
    </td>
    <td width="10%">
      <select name="priority" class="select">
          <option value="1">1 - <c:out value='${highest}'/></option>
          <option value="2">2 - <c:out value='${major}'/></option>
          <option value="3" selected="true">3 - <c:out value='${normal}'/></option>
          <option value="4">4 - <c:out value='${lower}'/></option>
          <option value="5">5 - <c:out value='${lowest}'/></option>
      </select>
    </td>
  </tr>
  <tr>
    <td height="50" colspan="2">
      <textarea class="textarea" name="comment" style="color:#cccccc"><c:out value="${jsmsg_customer_comment}"/></textarea>
    </td>
  </tr>
  <tr>
    <td colspan="2">
        <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
            <tr>
                <td width="15%" height="25" valign="middle" style="font-family:Arial,Helvetica,sans-serif;font-size:10pt;">
                    <c:out value="${lb_attachment}"/> / <c:out value="${lb_reference_file}"/>: 
                </td>
                <td width="51%" valign="middle"><div id="attachmentArea" style="border:1px solid #0C1476;padding-left:0;height:25px;line-height:25px"></div></td>
                <td width="4%" valign="middle" align="center"><div id="delAtt" style="display:none;"><img src="/globalsight/images/createjob/delete.png" style="cursor:pointer;padding-top:4px;" onclick="delAttc()"></div></td>
                <td width="15%" onmouseover="setInputFileDisable(1)">
                <input type="button" id="attachmentFileBtn" class="standardBtn_mouseout" value="<c:out value='${lb_browse}'/>">
                <input type="file" class="attachmentFile" name="selectedAttachmentFile" id="selectedAttachmentFile" class="standardBtn_mouseout" onclick="setType(1)" onchange="checkAndUpload()" title="<c:out value='${lb_create_job_browse_tip}'/>">
                </td>
                <td width="15%"><div id="attributeButtonDIV" style="display:none"><input id="attributeButton" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_job_attributes}'/>" title="<c:out value='${lb_job_attributes}'/>"></div>
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