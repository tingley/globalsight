<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.foundation.User,
            com.globalsight.connector.eloqua.models.Email,
             com.globalsight.util.resourcebundle.SystemResourceBundle,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.connector.eloqua.models.LandingPage,

            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.company.CompanyWrapper,java.util.*,
            com.globalsight.everest.company.CompanyThreadLocal"
    session="true"%>
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	SessionManager sessionMgr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);
	User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
	String companyIdWorkingFor = CompanyThreadLocal.getInstance()
			.getValue();
	String userName = "";
	String password = "";
	if (user != null) {
		userName = user.getUserName();
		password = user.getPassword();
	}

	ResourceBundle bundle = PageHandler.getBundle(session);
	String title = bundle.getString("lb_eloqua_assets");
	String helperText = bundle
			.getString("helper_text_eloqua_create_job");
	String updateTargetLocalesUrl = self.getPageURL() + "&action=updateTargetLocales";
	String queryJobAttributesUrl = self.getPageURL() + "&action=queryJobAttributes";
	String createEloquaJobUrl = self.getPageURL() + "&action=createEloquaJob";
	Integer creatingJobsNum = (Integer)request.getAttribute("creatingJobsNum");
    if (creatingJobsNum == null)
        creatingJobsNum = 0;
	
	String uploadAttachmentUrl = self.getPageURL() + "&action=uploadAttachment";
   
%>

<html>
<head>
<link rel="stylesheet" type="text/css" href="/globalsight/includes/css/createJob.css" />
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<title>
    <%=title%>
</title>
<style type="text/css">
.sourceFile {
	position: absolute;
	height: 21px;
	filter: alpha(opacity = 0);
	opacity: 0;
	width: 86px;
	cursor: pointer;
}

.attachmentFile {
	position: absolute;
	height: 21px;
	filter: alpha(opacity = 0);
	opacity: 0;
	width: 86px;
	cursor: pointer;
}

.fileListDiv {
    width: 100%;  
    margin-top: 0px; 
    margin-bottom: 10px; 
    margin-right: 5px;
    clear:both;
}

form{margin:0px} 
</style>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="/globalsight/jquery/jQuery.md5.js"></script>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css" />
<script src="/globalsight/includes/ContextMenu.js"></script>
<script src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js" type="text/javascript"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<script type="text/javascript">
var guideNode = "createEloquaJob";
var l10Nid = 0;// the localization profile id in use for current page
var controlWindow = null; // child window of uploaded files
var attributeWindow = null; //chind window of job attributes
var attachmentUploading = false;
var hasAttribute = false;
var attributeRequired = false;
var tempFolder = "";
var isUploading = false;
var uploadedFiles = new Array();
var type = "";
var helpFile = "<%=bundle.getString("help_eloqua_assets")%>";
var initPage = false;

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
    
 // action of cleanmap button
    $("#cleanMap").click(function()
    {
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
        $(this).blur();
    });
 
    function cleanAllMap() {
    	$("option[class][class='-1']").each(function() {
    		$(this).attr("selected",true);
        });

    	updateFileProfileSelect();
    }
 
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

        if ($("option:selected[class][class='-1']").length > 0)
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
        
        var fileMapFileProfile = "";
        var os = $("[name='fileProfile']");
        for (var i = 0; i < os.length; i++)
        {
        	var fileId = os[i].id.substring(2);
            var fileProfile = os[i].value;
            fileMapFileProfile += fileId + "-" + fileProfile + ",";
        }

        alert('<%=bundle.getString("msg_job_create_successful")%>');
        $("#fileMapFileProfile").val(fileMapFileProfile);

        $("#createJobForm").attr("target", "_self");
        $("#createJobForm").attr("action", "<%=createEloquaJobUrl %>");
        $("#createJobForm").attr("enctype","application/x-www-form-urlencoded");
        $("#createJobForm").attr("encoding","application/x-www-form-urlencoded");
        document.createJobForm.submit();
    });
    // *************************************cancel button*************************************
    $("#cancel").click(function()
    {
        document.location.href="<%=self.getPageURL()%>&isCancel=true";
    });
    
    // *************************************create result*************************************
   
    
    var msg = "<c:out value='${create_result}'/>";
    if (msg != "") {
        alert(msg);
        document.location.href="/globalsight/ControlServlet?activityName=createZipJobs";
    }

    $("#selectedAttachmentFile").offset({top:$("#attachmentFileBtn").offset().top + 2});
    $("#selectedAttachmentFile").offset({left:$("#attachmentFileBtn").offset().left + 2});

    $("#selectedAttachmentFile").mouseover(function(){
        $("#attachmentFileBtn").removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $("#attachmentFileBtn").removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    });
    
    var msg = "<c:out value='${isCreate}'/>";
    if (msg != "") {
    	showCreatePate();
    }
});

function showCreatePate() {
	document.title="<%=bundle.getString("lb_eloqua_create_job")%>";
	
	$("#eloquaFiles").hide();	
	$("#createPage").show();	
    
	$("#eloquaFileHeader").hide();
	$("#eloquaCreateHeader").show();	
	
	$("#fileQueue").width($("#uploadArea").width() - 2);
    $("#targetLocales").height(425);//for Chrome
    $("#targetLocaleArea").height($("#localeArea").height() - 3);
    
    helpFile = "<%=bundle.getString("help_eloqua_create_job")%>";
    
}

function showFilePage() {
	document.title="<%=bundle.getString("lb_eloqua_assets")%>";
	
	$("#eloquaFiles").show();
	$("#createPage").hide();

	$("#eloquaFileHeader").show();
	$("#eloquaCreateHeader").hide();	
	
	helpFile = "<%=bundle.getString("help_eloqua_assets")%>";
}

function removeSelectedFile(id) {
	$("#t"+id).remove();
	$("#c"+id).attr("checked", false);
	updateFileProfileSelect();
	updateFileTotal();
}

var defaultFileProfile = "-1";

function changeFileProfile(select)
{
	defaultFileProfile = select.value;   
    updateToDefaultFileProfile();
	updateFileProfileSelect();
}

function updateToDefaultFileProfile()
{
	if (defaultFileProfile != "-1") {
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

function addFullDivElement2(id, name) {
	$("#fileQueue").append('<div id="t' + id + '" class="uploadifyQueueItem">'+
			'<div class="uploadifyProgress" style="background:rgb(79, 148, 205)">'+
			'<div class="fileInfo" >' + name + '</div>'+
			'<div class="icon" ><span class="icons"><img src="/globalsight/images/createjob/success.png" style="width:20px;height:20px"></span></div>'+
			'<div class="profile"><select name="fileProfile" id="fp'+id+'" style="width:143;z-index:50;padding-top:2px;padding-bottom:2px;" onchange="changeFileProfile(this)">'+
			'<%=request.getAttribute("fps")%> '+
			'</select></div>'+
			'<div class="fileInfo" >' + name + '</div>'+
			'<div class="cancel"><a href="javascript:removeSelectedFile(\'' + id + '\')">'+
			'<img style="padding-top:4px" src="/globalsight/images/createjob/delete.png" border="0"/></a></div>'+
			'<div class="uploadifyProgressBar" style="background-color:grey; width:642"></div>'+
			'</div>');

	 updateToDefaultFileProfile();
	 updateFileProfileSelect();
	 updateFileTotal();
}

function updateFileTotal() {
	var total = $("[name='fileProfile']").length;
	$("#fileNo").html(total);
	$("#fileNo2").html(total);
}

function updateFileState(id)
{
	var checkbox = $("#c"+id)[0];
    var table = $("#t"+id);
	if (checkbox.checked)
    {
		if (table.length < 1)
	    {		
			addFullDivElement2(checkbox.value, checkbox.title);
	    }
    }
	else
    {
		if (table.length > 0)
	    {		
			table.remove();
			updateFileProfileSelect();
			updateFileTotal();
	    }
    }		 
}

function updateAllFileState()
{
	var boxes = fileForm.selecteloquaEmailIds;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                updateFileState(checkbox.value);
            }
        } 
    }
}

function updateAllPageFileState()
{
    var boxes = landingpage_div_fileForm.selecteloquaPageIds;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                updateFileState(checkbox.value);
            }
        } 
    }
}

function updateFileProfileSelect()
{
    var os = $("option:selected[class][class !='-1']");
    if (os.length > 0)
    {
    	l10Nid = os[0].className;
    	$("option[class][class !='" + l10Nid + "']").each(function() {
            if (this.className != "-1")
            {
            	this.disabled = true;
            }
        });

    	var ls = $("#targetLocaleArea").html();
        if (ls.length < 1)
        {
        	updateLocales();
        }
    }
    else
    {
    	l10Nid = 0;
    	defaultFileProfile = "-1";
    	$("#attributeButtonDIV").hide();
    	attributeRequired = false;
    	$("option[class]").each(function() {
                this.disabled = false;
        });

    	$("#targetLocaleArea").html("");
    }
}

function updateLocales()
{
	$.get("<%=updateTargetLocalesUrl%>", 
            {"l10Nid":l10Nid,"userName":"<%=userName%>"}, 
            function(data){
                $("#targetLocaleArea").show();
                $("#targetLocaleArea").html(data);
                $("#tlControl").attr("checked", true);
                
    });

	$.get("<%=queryJobAttributesUrl%>", 
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
}

function cleanAllMap() {
	$("option[class][class='-1']").each(function() {
		$(this).attr("selected",true);
    });

	updateFileProfileSelect();
}

$(document).ready(function(){
    $("#jobAttributesTab").removeClass("tableHeadingListOff");
    $("#jobAttributesTab").addClass("tableHeadingListOn");
    $("#jobAttributesTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
    $("#jobAttributesTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})

function initLandingPage() {
	$("#idDiv").mask("<%=bundle.getString("msg_loading")%>");
    $.post('<%=self.getPageURL()%>' + "&action=getLandingPages&type=9",            
    		callBackToPageLandingPage, "text");
}

function showTab(id)
{	
	hideTab("emailTab");
	hideTab("landingPageTab");
	hideTab("formTab");
    
    $("#" + id + "Tab").removeClass("tableHeadingListOff");
    $("#" + id + "Tab").addClass("tableHeadingListOn");
    $("#" + id + "Tab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
    $("#" + id + "Tab img:last").attr("src","/globalsight/images/tab_right_blue.gif");

    $("#emailDiv").hide();
    $("#formDiv").hide();
    $("#landingPageDiv").hide();
    $("#" + id + "Div").show();

    $("#navDiv").html($("#" + id + "NavDiv").html());
    $("#navDiv2").html($("#" + id + "NavDiv2").html());
    
	if ("landingPage" == id && !initPage)
	{
		initLandingPage();
		initPage = true;
	}
}

function hideTab(id)
{
    $("#" + id).removeClass("tableHeadingListOn");
    $("#" + id).addClass("tableHeadingListOff");
    $("#" + id + " img:first").attr("src","/globalsight/images/tab_left_gray.gif");
    $("#" + id + " img:last").attr("src","/globalsight/images/tab_right_gray.gif");
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

function Trim(str)
{
    if(str=="") return str;
    var newStr = ""+str;
    RegularExp = /^\s+|\s+$/gi;
    return newStr.replace( RegularExp,"" );
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

function addAttachment() {
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

function setButtonState()
{
    var selectedIndex = new Array();
    var boxes = fileForm.selecteloquaEmailIds;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                if (checkbox.checked) 
                {
                    selectedIndex.push(i);
                    addFullDivElement2(checkbox.value, checkbox.displayName);
                }
            }
        } 
        else 
        {
            if (boxes.checked) 
            {
                selectedIndex.push(0);
            }
        }
    }
}

function handleSelectAllEmail(c) {
    if (c.checked) {
        checkAllWithName('fileForm', 'selecteloquaEmailIds'); 
    }
    else {
        $("#mail_file_tbody").find(":checkbox").each(function() {
            	this.checked = false;               
        });        
    }

    updateAllFileState();
}

function handleSelectAllPage(c) {
    if (c.checked) {
        checkAllWithName('landingpage_div_fileForm', 'selecteloquaPageIds'); 
    }
    else {
    	 $("#landingPage_file_tbody").find(":checkbox").each(function() {
         	this.checked = false;               
     }); 
    }

    updateAllPageFileState();
}

function setType(t)
{
	type = t;
}

function setInputFileDisable(t)
{
	if(t == '0')
	{
	
		$("#selectedAttachmentFile").prop('disabled', true);
	}
	else
	{

		$("#selectedAttachmentFile").prop('disabled', false);
	}
}

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
    $("#createJobForm").ajaxSubmit(
    		{
    		type: 'post',  
            url: "<%=uploadAttachmentUrl%>" , 
            dataType:'text',
            timeout:100000000,
            success: function(data){  
                 addAttachment();
            },  
            error: function(XmlHttpRequest, textStatus, errorThrown){  
                 alert(errorThrown);
            }  
    		});

	isUploading = true;

	emptyFileValue();
	$("#selectedAttachmentFile").prop('disabled', false);
}

function emptyFileValue()
{
	$("#selectedAttachmentFile").replaceWith($("#selectedAttachmentFile").clone(true));
	$("#selectedAttachmentFile").val('');
}

function callBackToPageLandingPage(data)
{
    $("#idDiv").unmask("<%=bundle.getString("msg_eloqua_wait_filter")%>");
    var ob = eval("(" + data + ")");
    $("#navDiv").html(ob.nav);
    $("#navDiv2").html(ob.nav2);
    $("#landingPageNavDiv").html(ob.nav);  
    $("#landingPageNavDiv2").html(ob.nav2);    
    updateLandingPageFile(ob.files);
}

function callBackToPageEmail(data)
{
	 $("#idDiv").unmask("<%=bundle.getString("msg_eloqua_wait_filter")%>"); 
     var ob = eval("(" + data + ")");     
     $("#navDiv").html(ob.nav);
     $("#navDiv2").html(ob.nav2);
     $("#emailNavDiv").html(ob.nav);   
     $("#emailNavDiv2").html(ob.nav2); 
     updateEmailFile(ob.emails);
}

function toPageEmailSet(page)
{
	$("#idDiv").mask("<%=bundle.getString("msg_loading")%>");
    $.post('<%=self.getPageURL()%>' + "&emailSet=" + page + "&action=getEmails&type=8", 
    		callBackToPageEmail,"text");
}


function toPageLandingPageSet(page)
{
	$("#idDiv").mask("<%=bundle.getString("msg_loading")%>");
    $.post('<%=self.getPageURL()%>' + "&landingPageSet=" + page + "&action=getLandingPages&type=8",            
    		callBackToPageLandingPage, "text");
}

function toPageEmail(page)
{
	$("#idDiv").mask("<%=bundle.getString("msg_eloqua_wait_filter")%>");
    $.post('<%=self.getPageURL()%>' + "&page=" + page + "&action=getEmails&type=5", 
    		callBackToPageEmail,"text");
}


function toPageLandingPage(page)
{
	$("#idDiv").mask("<%=bundle.getString("msg_eloqua_wait_filter")%>");
    $.post('<%=self.getPageURL()%>' + "&page=" + page + "&action=getLandingPages&type=5",            
    		callBackToPageLandingPage, "text");
}

function getEmails(type)
{
	$("#idDiv").mask("<%=bundle.getString("msg_eloqua_wait_filter")%>");
    $.post('<%=self.getPageURL()%>' + "&action=getEmails&type=" + type,              
    		callBackToPageEmail,"text");
}

function getLandingPages(type)
{
	$("#idDiv").mask("<%=bundle.getString("msg_eloqua_wait_filter")%>");
    $.post('<%=self.getPageURL()%>' + "&action=getLandingPages&type=" + type, 
    		callBackToPageLandingPage, "text");
}

function updateEmailFile(emails)
{
    var tbody = $("#mail_file_tbody");
    tbody.html("");

    for (var i = 0; i < emails.length; i++)
    {
        var email = emails[i];
        var eId = "" + email.displayId;
    	var example = $("#emailExample").clone(true);
        if (i % 2 == 0)
        {
        	example.addClass("rowOdd");
        }
        else
        {
        	example.addClass("tableRowEvenTM");
        }
        
    	example.attr("id", "");
    	var input = example.find("input");
    	input.attr("checked", false);
        
        input.attr({
                	"id": "c" + eId,
                    "title" : email.name,
                    "value": eId
                }); 

        input.bind("click",function(){
            updateFileState(this.id.substr(1));
        	});
        	        

        if ($("#t"+eId).length > 0){
        	input.attr("checked", true);
        }

        
        
        var name = example.find(".emailName");
        name.html("<a href=\"javascript:preview('" + eId + "')\"> " + email.name + "</a>");

        var createBy = example.find(".emailCreatedBy");
        createBy.html(email.createdBy);

        var createAt = example.find(".emailCreatedAt");
        createAt.html(email.createdAt);

        var status = example.find(".emailStatus");
        status.html(email.status);
        
        tbody.append(example);
    }
}

function updateLandingPageFile(files)
{
    var tbody = $("#landingPage_file_tbody");
    tbody.html("");

    for (var i = 0; i < files.length; i++)
    {
        var file = files[i];
        var eId = "" + file.displayId;
        var example = $("#pageExample").clone(true);
        if (i % 2 == 0)
        {
            example.addClass("rowOdd");
        }
        else
        {
            example.addClass("tableRowEvenTM");
        }
        
        example.attr("id", "");
        var input = example.find("input");
        input.attr("checked", false);
        input.attr({
                    "id": "c" + eId,
                    "title" : file.name,
                    "value": eId
                }); 

        input.bind("click",function(){
            updateFileState(this.id.substr(1));
            });
                    

        if ($("#t"+eId).length > 0){
            input.attr("checked", true);
        }

        var name = example.find(".pageName");
        name.html("<a href=\"javascript:preview('" + eId + "')\"> " + file.name + "</a>");

        var createBy = example.find(".pageCreatedBy");
        createBy.html(file.createdBy);
        
        var createAt = example.find(".pageCreatedAt");
        createAt.html(file.createdAt);

        var status = example.find(".pageStatus");
        status.html(file.status);
        
        tbody.append(example);
    }
}

function filterEmailItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	$("#idDiv").mask("<%=bundle.getString("msg_eloqua_wait_filter")%>");
    	$.post('<%=self.getPageURL() + "&action=getEmails&type=6"%>', 
	        	{
	    		"nameFilter":$("#nameFilter").val(),
	    		"createdAtFilter":$("#createdAtFilter").val(),
	            "createdByFilter":$("#createdByFilter").val(),
	            "statusFilter":$("#statusFilter").val()
	            },callBackToPageEmail,"text"
	    	);
	}
}

function filterLandingPageItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	$("#idDiv").mask("<%=bundle.getString("msg_eloqua_wait_filter")%>");
    	$.post('<%=self.getPageURL() + "&action=getLandingPages&type=6"%>', 
	        	{
	    		"nameFilter":$("#nameFilter2").val(),
	    		"createdAtFilter":$("#createdAtFilter2").val(),
	            "createdByFilter":$("#createdByFilter2").val(),
	            "statusFilter":$("#statusFilter2").val()
	            },callBackToPageLandingPage,"text"
	    	);
	}
}

var previewId = 0;
function preview(id)
{
	previewId = id;
	var url = '<%=self.getPageURL()%>' + "&action=preview2&id=" + id;
	$("#previewDiv").attr("src",url);
	$("#idDiv").mask('<%=bundle.getString("msg_eloqua_wait_preview")%>');
 }
 
 function doPreview()
 {
	 $("#idDiv").unmask('<%=bundle.getString("msg_eloqua_wait_preview")%>');
	 $("#previewDiv").dialog({modal:true,width:750,height:600,
	    	beforeclose: function(event, ui) {$("#previewDiv").attr("src","");},   	
	    	title:$("#c" + previewId)[0].title, resizable:true});
	$("#previewDiv").css("width", "750px");
	$("#previewDiv").css("height", "600px");
 }
 
  function chickAtt()
  {
	  $("#selectedAttachmentFile").click();
	  
  }
  
  function changePageSize(type, size)
  {
	  if (type == "LandingPage")
	  {
		  $.post('<%=self.getPageURL()%>' + "&eloquaFilePageSize=" + size + "&action=getLandingPages&type=7",            
		    		callBackToPageLandingPage, "text");
	  }
	  else if (type == "Email")
	  {
	  	 $.post('<%=self.getPageURL()%>' + "&eloquaFilePageSize=" + size + "&action=getEmails&type=7", 
	    		callBackToPageEmail,"text");
	  }
	  
  }
</script>
</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()" onunload="closePopUp()">
    <%@ include file="/envoy/common/header.jspIncl"%>
    <%@ include file="/envoy/common/navigation.jspIncl"%>
    <%@ include file="/envoy/wizards/guides.jspIncl"%>
    <div id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
        
        <div id="eloquaFileHeader">
            <span class='mainHeading'><%=bundle.getString("lb_eloqua_assets")%></span><p>
            <table cellspacing=0 cellpadding=0 border=0 class=standardText><tr><td width="100%"><%=bundle.getString("helper_text_eloqua_assets")%></td></tr></table>
        </div>
        <div id="eloquaCreateHeader" style="display: none">
            <span class='mainHeading'><%=bundle.getString("lb_eloqua_create_job")%></span><p>
            <span id="creatingJobs" style="color:red"><%=creatingJobsNum %> <c:out value="${lb_jobs_creating}"/></span><p>
            <table cellspacing=0 cellpadding=0 border=0 class=standardText><tr><td width="100%"><%=bundle.getString("helper_text_eloqua_create_job")%></td></tr></table>
        </div>
        
        <table cellspacing=0 cellpadding=0 border=0 class=standardText>
            <tr>
                <td width="100%">
                    <c:out value="${helperText}" />
                </td>
            </tr>
        </table>
        <div id="eloquaFiles" >
        <div style="width: 100%; padding-top: 0px;" >
            <div style="float: left">
            <table cellpadding="0" cellspacing="0" border="0" class="standardText">
                <tr>
                    <td id="emailTab" class="tableHeadingListOn" >
                        <img src="/globalsight/images/tab_left_blue.gif" border="0">
                        <a class="sortHREFWhite" href="javascript:showTab('email')" onclick=""><%=bundle.getString("lb_emails")%></a>
                        <img src="/globalsight/images/tab_right_blue.gif" border="0">
                    </td>
                    <td width="2"></td>
                    <td id="landingPageTab" class="tableHeadingListOff">
                        <img src="/globalsight/images/tab_left_gray.gif" border="0">
                        <a class="sortHREFWhite" href="javascript:showTab('landingPage')"><%=bundle.getString("lb_landingpages")%></a>
                        <img src="/globalsight/images/tab_right_gray.gif" border="0">
                    </td>
                    <td width="2"></td>
                    <td id="formTab" class="tableHeadingListOff" style="display: none">
                        <img src="/globalsight/images/tab_left_gray.gif" border="0">
                        <a class="sortHREFWhite" href="javascript:showTab('form')"><%=bundle.getString("lb_form")%></a>
                        <img src="/globalsight/images/tab_right_gray.gif" border="0">
                    </td>
                    
                </tr>
            </table>
            </div>
            <div id="navDiv" style="float:right; margin-right: 10px;" class="standardText">
                        <%=request.getAttribute("email_nav")%> 
            </div>
        </div>
        <div id="emailDiv" class="listborder standardText fileListDiv">
            <form action="" name="fileForm" method="post">
            <table id="email_file" style="width: 100%" class="standardText" border="0" cellspacing="0" cellpadding="4">
               <tr class="tableHeadingBasic">
                   <td style="width: 10px" align="center"><input type="checkbox" onclick="handleSelectAllEmail(this)"/></td>
                   <td ><%=bundle.getString("lb_name")%></td>
                   <td><%=bundle.getString("lb_created_by")%></td>
                   <td><%=bundle.getString("lb_create_at2")%></td>
                   <td><%=bundle.getString("lb_status")%></td>
               </tr>
               <tr class="tableHeadingFilter">
                   <td></td>
                   <td><input name="nameFilter" title="Input the filter,press Enter to filter" class="standardText" id="nameFilter" onkeydown="filterEmailItems(event);" type="text" value=""/></td>
                   <td><input name="createdByFilter" title="Input the filter,press Enter to filter" class="standardText" id="createdByFilter" onkeydown="filterEmailItems(event);" type="text" value=""/></td>
                   <td><input name="createdAtFilter" title="Input the filter,press Enter to filter" class="standardText" id="createdAtFilter" onkeydown="filterEmailItems(event);" type="text" value=""/></td>                   
                   <td><input name="statusFilter" title="Input the filter,press Enter to filter" class="standardText" id="statusFilter" onkeydown="filterEmailItems(event);" type="text" value=""/></td>
               </tr>
               <tbody id='mail_file_tbody'>
<%
            List<Email> es = (List<Email>)request.getAttribute("email_list");
            int ei = 0;
            for (Email e : es)
            {
                ei++;
               %>
               <tr <%=ei%2 == 0 ? "class=\"rowOdd\"" : "class=\"tableRowEvenTM\"" %> >
               <td align="center" >
               <input type="checkbox" id="c<%=e.getDisplayId()%>"  name="selecteloquaEmailIds" value="<%=e.getDisplayId()%>" title="<%=e.getDisplayName()%>" onclick="updateFileState('<%=e.getDisplayId()%>')">
               </td>
               <td>
               <a href="javascript:preview('<%=e.getDisplayId()%>')"> <%=e.getDisplayName()%></a>
               
                </td>
                <td>
                <%=e.getCreateBy()%>
                </td>
                <td>
                <%=e.getCreatedAt()%>
                </td>
                <td>
                <%=e.getStatus()%>
                </td>
                </tr>
               <%
            }
                %>
                </tbody>
            </table>
            
            </form>
            <div style="display: none">
            <table>
              <tr id="emailExample">
               <td align="center">
               <input type="checkbox" name="selecteloquaEmailIds" >
               </td>
                <td class="emailName">
                
                </td>
                <td class="emailCreatedBy">
                
                </td>
                <td class="emailCreatedAt">
                
                </td>
                <td class="emailStatus">
                
                </td>
                </tr>
             </table>
            </div>
        </div>
        <div id="landingPageDiv" style="display: none" class="listborder standardText fileListDiv">
            <form action="" name="landingpage_div_fileForm" method="post">

            <table id="landingPage_file" style="width: 100%" class="standardText" border="0" cellspacing="0" cellpadding="4">
               <tr class="tableHeadingBasic">
                   <td style="width: 10px" align="center"><input type="checkbox" onclick="handleSelectAllPage(this)"/></td>
                   <td ><%=bundle.getString("lb_name")%></td>
                   <td><%=bundle.getString("lb_created_by")%></td>
                   <td><%=bundle.getString("lb_create_at2")%></td>
                   <td><%=bundle.getString("lb_status")%></td>
               </tr>
               <tr class="tableHeadingFilter">
                   <td></td>
                   <td><input name="nameFilter2" title="Input the filter,press Enter to filter" class="standardText" id="nameFilter2" onkeydown="filterLandingPageItems(event);" type="text" value=""/></td>
                   <td><input name="createdByFilter2" title="Input the filter,press Enter to filter" class="standardText" id="createdByFilter2" onkeydown="filterLandingPageItems(event);" type="text" value=""/></td>
                   <td><input name="createdAtFilter2" title="Input the filter,press Enter to filter" class="standardText" id="createdAtFilter2" onkeydown="filterLandingPageItems(event);" type="text" value=""/></td>                   
                   <td><input name="statusFilter2" title="Input the filter,press Enter to filter" class="standardText" id="statusFilter2" onkeydown="filterLandingPageItems(event);" type="text" value=""/></td>
               </tr>
               <tbody id='landingPage_file_tbody'>
<%
            List<LandingPage> ps = (List<LandingPage>)request.getAttribute("page_list");
            int pi = 0;
            for (LandingPage e : ps)
            {
                pi++;
               %>
               <tr <%=pi%2 == 0 ? "class=\"rowOdd\"" : "class=\"tableRowEvenTM\"" %> >
               <td align="center">
               <input type="checkbox" id="c<%=e.getDisplayId()%>"  name="selecteloquaPageIds" value="<%=e.getDisplayId()%>" title="<%=e.getDisplayName()%>" onclick="updateFileState('<%=e.getDisplayId()%>')">
               </td>
               <td>
               <a href="javascript:preview('<%=e.getDisplayId()%>')"> <%=e.getDisplayName()%></a>
                </td>
                <td>
                <%=e.getCreateBy()%>
                </td>

                <td>
                <%=e.getCreatedAt()%>
                </td>
                <td>
                <%=e.getStatus()%>
                </td>
                </tr>
               <%
            }
                %>
                </tbody>
            </table>
            
            </form>
            <div style="display: none">
            <table>
              <tr id="pageExample">
               <td align="center">
               <input type="checkbox" name="selecteloquaPageIds" >
               </td>
                <td class="pageName">
                
                </td>
                <td class="pageCreatedBy">
                
                </td>
                <td class="pageCreatedAt">
                
                </td>
                <td class="pageStatus">
                
                </td>
                </tr>
             </table>
            </div>
        </div>
        <div style="float:left; margin-right: 10px;" class="standardText">
              <input type="button" value="<%=bundle.getString("lb_go_to_create_job")%>" onclick="showCreatePate()">
        </div>
        <div id="navDiv2" style="float:right; margin-right: 10px; " class="standardText">
              <%=request.getAttribute("email_nav2")%> 
        </div>
        <div id="formDiv" style="width: 600px; height: 490px; float: left; margin-top: 0px; padding: 10px; display:none" class="listborder standardText">
            &nbsp;
        </div>
        </div>
        <div id="createPage" style=" margin-left: 0px; margin-top: 0px; display: none "  class=" standardText">
            <form name="createJobForm" id="createJobForm" method="post" action="<%=createEloquaJobUrl %>" enctype="multipart/form-data" target="none_iframe">
                <input type="hidden" id="attributeString" name="attributeString" value="">
                <input type="hidden" id="fileMapFileProfile" name="fileMapFileProfile" value="">
                <input type="hidden" name="userName" value="<%=userName%>">
                <table class="listborder" cellspacing="0" cellpadding="0" width="979" align="left" border="1">
					<tr>
					<td>
					  <table cellSpacing="10" cellPadding="0" width="100%" align="center" border="0" table-layout="fixed">
					    <tr>
					    <td colspan="2" height="265">
					    <table class="listborder" width="100%" cellspacing="0" cellpadding="0" border="0">
					      <tr height="30">
					        <td width="59%" style="border:0"><div class="titletext" style="padding-left:10px;"><c:out value="${lb_name}"/></div></td>
					         <td width="1%" style="border:0"><div align="center">&nbsp;</div></td>
					         <td width="10%" style="border:0"><div align="center"><span class="titletext"><c:out value="${lb_status}"/></span></div></td>
					        <td width="20%" style="border:0"><div align="center"><span class="titletext"><c:out value="${lb_file_profile}"/></span></div></td>
					        <td width="10%" style="border:0">&nbsp;</td>
					      </tr>
					      <tr>
					        <td id="uploadArea" height="270" colspan="6">
					            <div id="fileQueue" class="fileQueue" ></div>
					        </td>
					      </tr>
					      <tr>
					        <td colspan="6"><div id="appl"></div><div id="attachment"></div>
					            <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
					                <tr>
					                    <td width="100px" height="32px" align="center" valign="middle" onmouseover="setInputFileDisable(0)">
					                    &nbsp;<input type="button" onclick="showFilePage()" title="<%=bundle.getString("lb_eloqua_create_job_add_file_tip")%>" id="sourceFileBtn" class="standardBtn_mouseout" value="<c:out value='${lb_add_files}'/>">
					                         </td>
					                    <td width="100px" align="center" valign="middle"></td>
					                    <td align="center" class="footertext">
					                        <c:out value="${lb_total}"/>: <span id="fileNo">0</span>
					                        - <c:out value='${lb_uploaded}'/>: <span id="fileNo2">0</span>
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
					                <div id="targetLocaleArea" style="overflow:scroll;overflow-x:hidden;padding-left:5px"></div>
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
					                <input type="button" id="attachmentFileBtn"  title="<c:out value='${lb_create_job_browse_tip}'/>" onclick="chickAtt()" class="standardBtn_mouseout" value="<c:out value='${lb_browse}'/>">
					                <input type="file"  class="attachmentFile" name="selectedAttachmentFile" id="selectedAttachmentFile" class="standardBtn_mouseout" onclick="setType(1)" onchange="checkAndUpload()" title="<c:out value='${lb_create_job_browse_tip}'/>">
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
        </div>
        <iframe name="none_iframe" width="0" height="0" scrolling="no" style="display: none"> </iframe>
    </div>

    <iframe id="previewDiv" title="" style="display:none; padding: 0px; width:100%" >
             
    </iframe>

    
    <div style="display:none;">
        <div id="emailNavDiv">
            <%=request.getAttribute("email_nav")%> 
        </div>
        
         <div id="landingPageNavDiv">
            <%=request.getAttribute("page_nav")%> 
         </div>
         
         <div id="emailNavDiv2">
            <%=request.getAttribute("email_nav2")%> 
        </div>
        
         <div id="landingPageNavDiv2">
            <%=request.getAttribute("page_nav2")%> 
         </div>
    </div>
    </div>
</body>
</html>