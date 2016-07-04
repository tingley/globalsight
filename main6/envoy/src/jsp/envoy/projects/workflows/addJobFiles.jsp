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
            java.text.SimpleDateFormat,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.page.SourcePage,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    // Lables
    ResourceBundle bundle = PageHandler.getBundle(session);
    String createJobTitle = bundle.getString("title_add_source_files");
    String title = bundle.getString("title_add_source_files");
    Job jobImpl = (Job) request.getAttribute("Job");
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
    String companyIdWorkingFor = CompanyThreadLocal.getInstance().getValue();
    String selfURL = self.getPageURL();
    String userName = "";
    if (user != null) {
        userName = user.getUserName();
    }
%>
<html>
<head>
<title><%=title%></title>
<style type="text/css">
form{margin:0px}
.sourceFile{ position:absolute;height:25px;filter:alpha(opacity=0); opacity:0; width:86px;cursor:pointer;}
.fileQueue5 {
	height:262px;
	width:auto;
	position:absolute;
	top:75px;
	overflow:scroll;
	overflow-x:hidden;
	border-top:1px groove #CCCCCC;
	border-bottom:1px groove #CCCCCC;
}
</style>

<link rel="stylesheet" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>

<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jQuery.md5.js"></script>

<script language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></script>
<script language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></script>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<script src="/globalsight/includes/ContextMenu.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<script type="text/javascript">
var isUploading = false;
var uploadedFiles = new Array();
var mapped = false;// mark whether the target locales are mapped in the page
var l10Nid = 0;// the localization profile id in use for current page
var totalFileNo = 0;// total files 
var uploadedFileNo = 0;// file number that have been uploaded
var totalSize = 0;// total size of uploaded files
var uploadFailedNo = 0;// number of upload failed files
var progressBarWidth = 705;// width of progress bar
var switchedFileArray = new Array();// selected files from uploaded window
var zipFiles = new Array();
var tempFolder = "";

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

$(document).ready(function(){
	var date = new Date();
    tempFolder = date.format("yyyyMMddhhmm")+"-"+Math.floor(Math.random() * 1000000000);
	$("#fileQueue").width($("#uploadArea").width() - 2);
	$("#selectedSourceFile").offset({top:$("#sourceFileBtn").offset().top + 2});
    $("#selectedSourceFile").offset({left:$("#sourceFileBtn").offset().left + 2});
    $(".standardBtn_mouseout").mouseover(function(){
        $(this).removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $(this).removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    }).css("width","90px");
    // action of cleanmap button
    $("#cleanMap").click(function()
    {
        $("[name='fileProfile']").each(function(){
            addEmptyOption(this);
        });
        $("[name='fileProfile']").attr("value", "");
        $("[name='fileProfile'] option").attr("disabled", false);
        mapped = false;
        l10Nid = <%=jobImpl.getL10nProfileId()%>;
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
            alert("<%=bundle.getString("msg_job_add_files")%>");
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
            alert("<%=bundle.getString("jsmsg_choose_file_profiles_for_all_files")%>");
            creating = false;
            return;
        }
        $("#tmpFolderName").val(tempFolder);
        $("#createJobForm").attr("target", "_self");
        $("#createJobForm").attr("enctype","application/x-www-form-urlencoded");
        $("#createJobForm").attr("encoding","application/x-www-form-urlencoded");
        document.createJobForm.action = "<%=selfURL%>&uploadAction=addFile&userName=<%=userName%>";
        document.createJobForm.submit();
    });
    // *************************************close button*************************************
    $("#close").click(function()
    {
    	window.opener.location.reload();
    	window.close();
    });
    
});

function showDuplicate(pageArray)
{
	alert("<%=bundle.getString("msg_upload_dup_files")%>\n\n" + pageArray);
}

function addDivForNewFile(paramArray) 
{
	var objs = eval(paramArray);
    var tempId = "";
    if(objs=="")
    {
    	emptyFileValue();
    	isUploading = false;
    	$("#tmpFolderName").val(tempFolder);
		return false;
    }
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

function checkAndUpload(){
	if (isUploading)
	{
		alert("Please wait for the last file upload.");
		emptyFileValue();
		return false;
	}
    $("#createJobForm").attr("action", "<%=selfURL%>&uploadAction=uploadSelectedFile&tempFolder="+tempFolder);
	$("#createJobForm").submit();
	
	isUploading = true;
	emptyFileValue();
	$("#selectedSourceFile").prop('disabled', false);
}

function emptyFileValue()
{
	if(isIE())
	{
		$("#selectedSourceFile").replaceWith($("#selectedSourceFile").clone(true));
	}
	$("#selectedSourceFile").val('');
}

function isIE() { //ie?  
    if (!!window.ActiveXObject || "ActiveXObject" in window)  
        return true;  
    else  
        return false;  
}

function runProgress(id, percentage, speed, isSwitched) {
    $("#uploadedFileNo").show();
    var wii = parseInt(percentage) / 100 * progressBarWidth;
    if(speed == "normal")
    {
	    $("#ProgressBar" + id).animate({width : wii}, "normal", function() {
	        if (percentage == 100 && isSwitched) {
	            queryFileProfile(id);
	            uploadedFileNo++;
	            $("#uploadedFileNo").html("- <%=bundle.getString("lb_uploaded")%>: " + uploadedFileNo);
	        }
	    });
    }
    else
    {
    	$("#ProgressBar" + id).animate({width : wii}, 10, function() {
	        if (percentage == 100) {
	            queryFileProfile(id);
	            uploadedFileNo++;
	            $("#uploadedFileNo").html("- <%=bundle.getString("lb_uploaded")%>: " + uploadedFileNo);
	        }
	    });
    }
}

function queryFileProfile(id)
{
    $("#ProgressBar" + id).css("background-color", "grey");
    var profile = $("#bp" + id).find(".profileArea");
    profile.html("<select id='fp" + id
            + "' name='fileProfile' style='width:143;z-index:50;padding-top:2px;padding-bottom:2px;' " 
            + "onchange='disableUnavailableFileProfiles(this)'><option value=''></option>"
            + "</select>");
    var theFileName = $("#Hidden" + id).attr("value");
    $.get("<%=selfURL%>", 
            {"uploadAction":"queryFileProfile","fileName":theFileName,"l10Nid":"<%=jobImpl.getL10nProfileId()%>","no":Math.random(), "userName":"<%=userName%>"}, 
            function(data){
                profile.html("<select id='fp" + id
                        + "' name='fileProfile' style='width:143;z-index:50;padding-top:2px;padding-bottom:2px;' " 
                        + "onchange='disableUnavailableFileProfiles(this)'><option value=''></option>"
                        + data
                        + "</select>");
                if (!mapped && $("#fp" + id).val() != "") {
                    mapTargetLocales(document.getElementById("ProgressDiv" + id));
                }
        });
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

function removeFile(id, zipName, fileSize, filePath) {
    $("#bp" + id).fadeOut(250, function() {
            uploadedFileNo--;
            $("#uploadedFileNo").html("- <%=bundle.getString("lb_uploaded")%>: " + uploadedFileNo);
        
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
        $.post("<%=selfURL%>", 
                {"uploadAction":"deleteFile","filePath":filePath,
                "folder":tempFolder,"no":Math.random()});
        var fileCount = $(".uploadifyQueueItem").length;
        if (fileCount == 0)
        {
            $.post("<%=selfURL%>", 
                    {"uploadAction":"deleteFile",
                    "folder":tempFolder,"no":Math.random()});
            mapped = false;
            l10Nid = <%=jobImpl.getL10nProfileId()%>;
        }
    });
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

function mapTargetLocales(o)
{
    $(".uploadifyProgress").css("background","#E5E5E5");
    o.style.background="#4F94CD";
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<DIV id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 20px; LEFT: 20px; RIGHT: 20px;">
    <div id="blaiseCreateJobHeader">
        <span class='mainHeading'><%=createJobTitle%></span><p>
    </div>

    <div id="createJobDiv" style="margin-left:0px; margin-top:0px; class="standardText">
        <form name="createJobForm" id="createJobForm" method="post" action="" enctype="multipart/form-data" target="none_iframe">
           <input type="hidden" id="tmpFolderName" name="tmpFolderName" value="">
            <input type="hidden" id="fileMapFileProfile" name="fileMapFileProfile" value="" />
            <input type="hidden" id="userName" name="userName" value="<%=userName%>" />
            <input type="hidden" id="jobId" name= "jobId" value="<%=jobImpl.getJobId()%>"/>
            <input type="hidden" id="jobName" name= "jobName" value="<%=jobImpl.getJobName()%>"/>
            
        <table class="listborder" cellspacing="0" cellpadding="0" width="760" align="left" border="1" table-layout="fixed">
            <tr>
            	<td>
	                <table cellSpacing="10" cellPadding="0" width="100%" align="center" border="0" table-layout="fixed">
                    	<tr>
	                        <td colspan="2" height="265px">
	                            <table class="listborder" width="100%" height="100%" cellspacing="0" cellpadding="0" border="0">
	                             <tr height="30">
							        <td width="70%" style="border:0"><div class="titletext" style="padding-left:10px;"><%=bundle.getString("lb_name")%></div></td>
							        <td width="20%" style="border:0"><div align="center"><span class="titletext"><%=bundle.getString("lb_file_profile")%></span></div></td>
							        <td width="10%" style="border:0">&nbsp;</td>
							      </tr>
	                                <tr>
	                                    <td id="uploadArea" height="265px" colspan="3">
	                                    	<div id="fileQueue" class="fileQueue5"  style =""></div>
	                                    </td>
	                                </tr>
                                      <tr>
								        <td colspan="5">
								            <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
								                <tr>
								                    <td width="100px" height="30px" align="center" valign="middle">
								                    <input type="button" id="sourceFileBtn" class="standardBtn_mouseout" value="<%=bundle.getString("lb_add_files")%>" style="width:90px">
								                    <input type="file" class="sourceFile" multiple value="Add File" name="selectedSourceFile" id="selectedSourceFile" onclick="" onchange="checkAndUpload()" title="<%=bundle.getString("lb_create_job_add_file_tip")%>" style="left: 20px;">
								                    </td>
								                    <td align="center" class="footertext">
								                        <%=bundle.getString("lb_total")%>: <span id="fileNo">0</span>
								                        <span id="uploadedFileNo" style="display:none">- <%=bundle.getString("lb_uploaded")%>: 0</span>
                        								<span id="failedUpload" style="display:none">| <%=bundle.getString("msg_failed")%>: 0</span>
								                    </td>
								                    <td width="110px" align="center"><input id="cleanMap" type="button" class="standardBtn_mouseout" value="<%=bundle.getString("lb_clear_profile")%>" title="<%=bundle.getString("lb_create_job_clean_map_tip")%>"></td>
								                    <td width="70px" style="border:0;">&nbsp;</td>
								                </tr>
								            </table>
								        </td>
								      </tr>
	                            </table>
	                        </td>
	                    </tr>
	                    <tr>
	                        <td colspan="3">
	                            <table cellSpacing="0" cellPadding="0" width="100%" border="0">
	                                <tr>
	                                    <td width="25%" align="right">
                               				<input id="create" type="button" class="standardBtn_mouseout" style="width:90px;" value="<%=bundle.getString("lb_add")%>" title="<%=bundle.getString("lb_add")%>">
                               				&nbsp;&nbsp;&nbsp;
                           					<input id="close" type="button" class="standardBtn_mouseout" style="width:90px;" value="<%=bundle.getString("lb_close")%>" title="<%=bundle.getString("lb_close")%>">
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
    </div>
</DIV>
</DIV>
</body>
</HTML>
