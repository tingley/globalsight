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
            com.globalsight.cxe.entity.gitconnector.GitConnector,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="showFileMapping" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    // Lables
    ResourceBundle bundle = PageHandler.getBundle(session);
    String treeTitle = bundle.getString("lb_git_files");
    String createJobTitle = bundle.getString("lb_git_connector_create_job");
    String treeHelperText = bundle.getString("helper_text_git_connector_tree");
    String createHelperText = bundle.getString("helper_text_git_connector_create_job");

    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
    String companyIdWorkingFor = CompanyThreadLocal.getInstance()
            .getValue();
    String userName = "";
    String password = "";
    if (user != null)
    {
        userName = user.getUserName();
        password = user.getPassword();
    }

    // URLs
    GitConnector gc = (GitConnector) request.getAttribute("gitConnector");
    String initURL = self.getPageURL() + "&action=initTree&gcId=" + gc.getId();
    String pageContentUrl = self.getPageURL() + "&action=getPages";
    String uploadAttachmentUrl = self.getPageURL() + "&action=uploadAttachment";
    String updateTargetLocalesUrl = self.getPageURL() + "&action=updateTargetLocales";
    String createGitConnectorJobUrl = self.getPageURL() + "&action=createGitConnectorJob";
    String checkFileMappingUrl = self.getPageURL() + "&action=checkFileMapping";
    String showFileMappingUrl = showFileMapping.getPageURL();

    Integer creatingJobsNum = (Integer)request.getAttribute("creatingJobsNum");
    if (creatingJobsNum == null)
        creatingJobsNum = 0;
%>
<html>
<head>
<title><%=treeTitle%></title>
<style type="text/css">
table td,table td * {
    vertical-align: top;
}

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
<link rel="stylesheet" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<link href="/globalsight/jquery/dynatree-1.2.4/skin-vista/ui.dynatree.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.cookie.js"></script>
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.dynatree.min.js"></script>
<script language="JavaScript" src="/globalsight/includes/modalDialog.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<script language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></script>
<script language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></script>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<script src="/globalsight/includes/ContextMenu.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<script type="text/javascript">
var needWarning = false;
var guideNode = "GitConnector";
var objectName = "";
var helpFile = "<%=bundle.getString("help_git_files")%>";

var mapped = false;
var l10Nid = 0;// the localization profile id in use for current page
var controlWindow = null; // child window of uploaded files
var attributeWindow = null; //chind window of job attributes
var attachmentUploading = false;
var hasAttribute = false;
var attributeRequired = false;
var isUploading = false;
var uploadedFiles = new Array();
var defaultFileProfile = "-1";
var checkout = "no";

function confirmJump() {
    return true;
}

// Adds JS endsWith function.
String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

// Init the dialog for page info displaying.
var pageInfoDialog = null;
$(
	function () {
    	pageInfoDialog = $("#currentNodeDiv").dialog({
            modal: false,
            autoOpen: false,
            closeOnEscape: true,
            minHeight: 125,
            minWidth:500
        });
    }
)

function initTree()
{
	// Initialize MindTouch Pages tree
    $("#treeDIV").dynatree({
        title: "Loading Git Connector tree",
        persist: false,
        checkbox: true,
        selectMode: 3,
        initAjax: {
            url: "<%=initURL%>",
            data: { checkout: checkout}
        },
        onSelect: function(select, node) {
            // Get a list of all selected nodes, and convert to a key array:
            var selKeys = $.map(node.tree.getSelectedNodes(), function(node){
              return node.data.title;
            });

            // Get a list of all selected TOP nodes
            var selRootNodes = node.tree.getSelectedNodes(true);
            // ... and convert to a key array:
            var selRootKeys = $.map(selRootNodes, function(node){
              return node.data.title;
            });
        },
        onDblClick: function(node, event) {
            node.toggleSelect();
        },
        onKeydown: function(node, event) {
            if( event.which == 32 ) {
              node.toggleSelect();
              return false;
            }
        }
    });
}

$(document).ready(function ()
{
    // Set CSS Value
	var defaultWidth = "100%";
	var defaultHeight = $(window).height() * 0.65;
    $("#treeDIV").width(defaultWidth);
    $("#treeDIV").height(defaultHeight);

    initTree();

    // Expand All Button
    $("#expandBtn").click(function () {
        $("#treeDIV").dynatree("getRoot").visit(function (node) {
            node.expand(true);
        });
    });

    // Collapse All Button
    $("#collapseBtn").click(function () {
        $("#treeDIV").dynatree("getRoot").visit(function (node) {
            node.expand(false);
        });
    });

    // Buttons style
    $(".standardBtn_mouseout").mouseover(function(){
        $(this).removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $(this).removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    }).css("width","90px");
    
    $("#checkFileMapping").css("width","130px");

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

    // action of job attribute button
    $("#attributeButton").click(function(){
        var ctlStr = "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=450,left=700,top=200";// + (screen.height)/7;
        var winUrl = "/globalsight/ControlServlet?linkName=jobAttributes&pageName=CZJ&l10Nid="+l10Nid;
        attributeWindow = window.open(winUrl,"ja",ctlStr);
        $(this).blur();
        attributeWindow.focus();
    });

    // ensure this will not impact attribute button
    $("#selectedAttachmentFile").offset({top:$("#attachmentFileBtn").offset().top + 2});
    $("#selectedAttachmentFile").offset({left:$("#attachmentFileBtn").offset().left + 2});

    $("#selectedAttachmentFile").mouseover(function(){
        $("#attachmentFileBtn").removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
    }).mouseout(function(){
        $("#attachmentFileBtn").removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
    });

    // create job
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
            alert("<c:out value='${msg_set_job_attributes}'/>");
            creating = false;
            return;
        }

        var fileMapFileProfile = "";
        var os = $("[name='fileProfile']");
        for (var i = 0; i < os.length; i++)
        {
            var fileId = os[i].id.substring(2);
            var fileProfile = os[i].value;
            fileMapFileProfile += fileId + "-" + fileProfile + " ";
        }
        $("#fileMapFileProfile").val(fileMapFileProfile);

        fnSelectAll2(false);
        
        var allMapping = true;
    	var fileProfileId = "";	
    	$("select[name=fileProfile]").each(function(){
    		fileProfileId = $(this).val();
    		return false
    	})
    	
    	var targetLocaleIds = "";
    	$("input[name=targetLocale]:checked").each(function(){
    		targetLocaleIds = targetLocaleIds + " " + $(this).val();
    	})
    	targetLocaleIds = targetLocaleIds.substr(1);
		$.ajaxSettings.async = false;
        $("input[name=jobFilePath]").each(function(){
	    	var random = Math.random();
    		$.get("<%=checkFileMappingUrl%>", 
            {action:"action",filePath:$(this).val(),fileProfileId:fileProfileId,gcId:"<%=gc.getId()%>",targetLocaleIds:targetLocaleIds,"no":Math.random()}, 
            function(data)
            {
            	if(data != "")
            	{
            		allMapping = false;
            	}
            });
        })
        if(!allMapping)
        {
        	var msg = "Not all files are mapped on all target locales, you can click ‘Check File Mapping’ button to see detailed information. Do you want to continue to create job?";
        	if(!confirm(msg))
        	{
        		creating = false;
        		return;
        	}
        }
        
        alert('<%=bundle.getString("msg_job_create_successful")%>');
        creating = false;
        $("#createJobForm").attr("target", "_self");
        $("#createJobForm").attr("action", "<%=createGitConnectorJobUrl%>");
        $("#createJobForm").attr("enctype","application/x-www-form-urlencoded");
        $("#createJobForm").attr("encoding","application/x-www-form-urlencoded");
        $("#createJobForm").ajaxSubmit(
        		{
        			type: 'post',
                    url: "<%=createGitConnectorJobUrl%>",
                    dataType:'text',
                    timeout:100000000,
                    success: function(data) {
                	    // Clear every field values
                	    clearFieldValues();
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
                }
        });
    });

    $("#cancel").click(function()
    {
    	document.location.href="/globalsight/ControlServlet?linkName=connect&pageName=GCM&action=connect&gcId=" + <%= gc.getId()%>;
    });

});

function showFileMapping()
{
	var jobFilePaths =document.getElementsByName("jobFilePath");
	if(jobFilePaths.length == 0)
	{
		alert("Please add files and select target locales.");
		return;
	}
	
	var fileProfiles = document.getElementsByName("fileProfile");
	var fileProfileId = "";	
	for(var i=0;i<fileProfiles.length;i++)
	{
		fileProfileId = fileProfiles[i].value;
	}
	if(fileProfileId == "")
	{
		alert("Select target locale(s) for this job");
		return;
	}
	
	var targetLocales = document.getElementsByName("targetLocale");
	var targetLocaleIds = "";	
	for(var i=0;i<targetLocales.length;i++)
	{
		if(targetLocales[i].checked)
		{
			targetLocaleIds = targetLocaleIds + " " + targetLocales[i].value;
		}
	}
	if(targetLocaleIds == "")
	{
		alert("Select target locale(s) for this job");
		return;
	}
	
	var url = "<%=showFileMappingUrl%>";
		window.open(url, null,
	    'width = 700,height = 400,status = no,resizable = yes,center = yes,left = 300,top = 100');
	
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

// After submit job, clear all fields on create job UI
function clearFieldValues() {
    $("#fileQueue").html("");
    $("#targetLocaleArea").html("");
    $("#targetLocaleArea").hide();
    $("#tlControl").attr("checked", false);
    $("textarea").val("<c:out value='${jsmsg_customer_comment}'/>")
    $("textarea").css("color","#cccccc");
    $("#jobName").attr("value","<c:out value='${jsmsg_customer_job_name}'/>");
    $("#jobName").css("color","#cccccc");
    defaultFileProfile = "-1";
    $("#priority option[value='3']").attr("selected", "selected");
    $("#selectedAttachmentFile").val("");
    $("#attributeString").val("");
    $("#fileMapFileProfile").val("");
    delAttc();
}

function goToCreateJob() {
    $("#currentNodeDiv").dialog("close");
    $("#currentNodeDiv").hide();

    var selNodes = $("#treeDIV").dynatree("getSelectedNodes");
    if(selNodes.length == 0)
    {
        alert("<%=bundle.getString("msg_select_file")%>");
        return;
    }

    showCreatePage();
    //$("#fileQueue").html("");
    for (var i = 0; i < selNodes.length; i++) {
    	var path = selNodes[i].data.path;
    	var name = selNodes[i].data.title;
    	var id = selNodes[i].data.id;

		if(name.indexOf(".") > 0)
		{
	    	addFullDivElement(id, name, path, name, true);
		}
    }
}

function back()
{
	document.location.href="/globalsight/ControlServlet?activityName=gitconnector";	
}

function addFullDivElement(id, zipName, filePath, fileName, isSwitched) {
	if ($("#bp"+id).length > 0)
	{
	}
	else
	{
	    $("#fileQueue").append('<div id="bp' + id + '" class="uploadifyQueueItem">' 
	            // div of progress bar container
	            + '<div id="ProgressDiv' + id + '" class="uploadifyProgress" style="background:rgb(103, 138, 166)">'
	            // div of file name
	            + '<div class="fileInfo" id="FileInfo' + id + '" onclick="mapTargetLocales(ProgressDiv' + id + ')">' 
	            + fileName + '<input type="hidden" id="Hidden' + id + '" name="jobWholeName" value="' + fileName + '">'
	            + '<input type="hidden" name="jobFilePath" value="' + filePath + '">' 
	            + '<input type="hidden" name="isSwitched" value="' + isSwitched + '"></div>'
	            // detail link
	            + '<div class="detail"><a href="javascript:showTip(\'' + id + '\')">(more)</a></div>'
	            // div of complete icon
	            + '<div class="icon" ><span class="icons"><img src="/globalsight/images/createjob/success.png" style="width:20px;height:20px"></span></div>'
	            // div of file profile select
	            + '<div class="profile" onclick="mapTargetLocales(ProgressDiv' + id + ')"><span class="profileArea"></span></div>'
	            // div of cancel button
	            + '<div class="cancel"><a href="javascript:removeSelectedFile(\'' + id + '\')">' 
	            + '<img style="padding-top:4px" src="/globalsight/images/createjob/delete.png" border="0"/></a></div>'
	            // div of progress bar
	            + '<div class="uploadifyProgressBar" id="ProgressBar' + id + '"></div>'
	            + '</div></div>'
	            + '<div id="bptip' + id + '" class="tip_cj" style="display:none">' + filePath + '</div>');
	    queryFileProfile(id);
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
                        + "onchange='changeFileProfile(this);disableUnavailableFileProfiles(this)'><option value=''></option>"
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
                updateToDefaultFileProfile();
        	    //updateFileProfileSelect();
        	    updateFileTotal();
        });
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

function mapTargetLocales(o)
{
    var divId = "bp" + o.id.substring(11);
    var icon = $("#"+ divId).find(".icons").html();
    if (icon.indexOf("success.png") != -1) {
        
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

function changeFileProfile(select)
{
    defaultFileProfile = select.value;

    updateToDefaultFileProfile();

    //updateFileProfileSelect();
}

// If one file is mapped with file profile, the other files with same extension 
// should use the same file profile default.
function updateToDefaultFileProfile()
{
    if (defaultFileProfile != "-1") {
        var selects = $("[name='fileProfile']");
        for (var j = 0; j < selects.length; j++)
        {
            var aSelect = selects[j];
            if (aSelect.value == "")
            {
                $(aSelect).val(defaultFileProfile); 
            }
        }
    }
}

function updateFileTotal() {
    var total = $("[name='fileProfile']").length;
    $("#fileNo").html(total);
    $("#fileNo2").html(total);
}

function fnSelectAll() {
    var isChecked = $("#control").is(":checked");
    fnSelectAll2(isChecked);
}

// Select/Unselect ALL
function fnSelectAll2(isChecked) {
    $("#treeDIV").dynatree("getRoot").visit(function(node){
        node.select(isChecked);
    });
}

function fnReload() {
	checkout = "yes";
    // Close page information dialog first
	$("#currentNodeDiv").dialog("close");
    $("#currentNodeDiv").hide();

    $("#treeDIV").dynatree("destroy");
    initTree();
}

function showCreatePage() {
    document.title="<%=createJobTitle%>";

    $("#gitconnectorPagesDiv").hide();
    $("#createJobDiv").show();

    $("#mtFileTreeHeader").hide();
    $("#mtCreateJobHeader").show();

    $("#fileQueue").width($("#uploadArea").width() - 2);
    $("#targetLocales").height(425);//for Chrome
    $("#targetLocaleArea").height($("#localeArea").height() - 3);

    helpFile = "<%=bundle.getString("help_git_create_job")%>";
}

function showFilePage() {
    document.title="<%=treeTitle%>";

    $("#gitconnectorPagesDiv").show();
    $("#createJobDiv").hide();

    $("#mtFileTreeHeader").show();
    $("#mtCreateJobHeader").hide();

    helpFile = "<%=bundle.getString("help_git_files")%>";
}

function setInputFileDisable(t) {
    if(t == '0') {
        $("#selectedAttachmentFile").prop('disabled', true);
    } else {
        $("#selectedAttachmentFile").prop('disabled', false);
    }
}

// Control target locales select/unselect all.
function controlBoxes() {
    var tl = document.getElementById("tlControl");
    if (tl.checked == true) {
        $("[name='targetLocale']").attr("checked", true);
    } else {
        $("[name='targetLocale']").attr("checked", false);
    }
}

// Delete attachment file
function delAttc() {
    $("#attachmentArea").html("");
    $("#delAtt").hide();
    $("#attachmentArea").removeClass("uploadifyError");
    attachmentUploading = false;
}

function chickAtt() {
    $("#selectedAttachmentFile").click();
}

// Upload attachment to server in a TMP file in session.
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
            error: function(XmlHttpRequest, textStatus, errorThrown){  
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
                            + "<div id='attName' style='width:336px' class='attachment_div'>" + fileName + "</div>" 
                            + "<input type='hidden' name='attachment' value=\"" + fileName + "\">");
    $("#ProgressBarAttach").height($("#attachmentArea").height());
    runAttachProgress(10);
    runAttachProgress(20);
    runAttachProgress(30);
    runAttachProgress(50);
    runAttachProgress(60);
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

function addAttachment() {
    $("#delAtt").show();
    attachmentUploading = true;
    runAttachProgress(100);
}

// update file profile map options
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
}

function removeSelectedFile(id) {
    $("#bp"+id).remove();
    $("#bptip"+id).remove();
    $("#c"+id).attr("checked", false);
    //updateFileProfileSelect();
    updateFileTotal();
}

</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides();">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

    <div id="mtFileTreeHeader">
        <span class="mainHeading"><%=treeTitle%></span>
        <table>
            <tr><td height="10">&nbsp;</td></tr>
            <tr><TD class="standardText"><%=treeHelperText%><A class='standardHREF' href="<%=gc.getUrl()%>" target="_blank"><b><%=gc.getUrl()%></b></A>.</TD></tr>
        </table><br/>
    </div>
    <div id="mtCreateJobHeader" style="display: none">
        <span class='mainHeading'><%=createJobTitle%></span><p>
        <span id="creatingJobs" style="color:red"><%=creatingJobsNum %> <%=bundle.getString("lb_jobs_creating")%></span><p>
        <table cellspacing=0 cellpadding=0 border=0 class=standardText><tr><td width="100%"><%=createHelperText%></td></tr></table>
    </div>

    <!-- Start of Page Tree -->
    <div id="gitconnectorPagesDiv">
        <table cellspacing="0" cellpadding="0" style="border:0px solid black">
        <tr VALIGN="bottom">
            <td style="border:1px solid black;width:50px;height:20px;background-color:#738eb5;" align="center">
                <input type="checkbox" id="control" title="Select/Deselect All" onClick="fnSelectAll();">
            </td>
            <td style="width:1px"></td>
            <td style="border:1px solid black;background-color:#738eb5;">
                <input type="button" id="expandBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/expand_all.gif')" title="Expand All">
            </td>
            <td style="width:1px"></td>
            <td style="border:1px solid black;background-color:#738eb5;">
                <input type="button" id="collapseBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/collapse_all.gif')" title="Collapse All">
            </td>
            <td style="width:1px"></td>
            <td style="border:1px solid black;background-color:#738eb5;">
                <input type="button" id="refreshFile" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/refresh.png')" title="Reload" onClick="fnReload();">
            </td>
        </tr>
        <tr><td style="height:3px" colspan="7"></td></tr>
        </table>

        <div id="treeDIV"><!-- Page tree is in this DIV. --></div>
        <!-- this Div has the "page information" dialog -->
        <div id="currentNodeDiv" title="Page Information" style="display:none" class="standardText"></div>
        <div>&nbsp;</div>
        <div align="left">
        <input type="BUTTON" VALUE="<%=bundle.getString("lb_go_to_create_job")%>" onClick="goToCreateJob();"/>
        <input type="BUTTON" VALUE="<%=bundle.getString("lb_back")%>" onClick="back();"/>
        </div>
    </div>
    <!-- End of Page Tree -->

    <!-- Start of Create Job Section -->
    <div id="createJobDiv" style="margin-left:0px; margin-top:0px; display:none;" class="standardText">
        <form name="createJobForm" id="createJobForm" method="post" action="" enctype="multipart/form-data" target="none_iframe">
        	<input type="hidden" id="gcId" value="<%=gc.getId()%>">
			<input type="hidden" id="checkFileMappingUrl" value="<%=checkFileMappingUrl%>">
            <input type="hidden" id="attributeString" name="attributeString" value="" />
            <input type="hidden" id="fileMapFileProfile" name="fileMapFileProfile" value="" />
            <input type="hidden" name="userName" value="<%=userName%>" />
            <input type="hidden" name="gitConnectorId" value="<%=gc.getId()%>" />

            <table class="listborder" cellspacing="0" cellpadding="0" width="979" align="left" border="1">
            <tr><td>
                <table cellSpacing="10" cellPadding="0" width="100%" align="center" border="0" table-layout="fixed">
                    <tr>
                        <td colspan="2" height="265">
                            <table class="listborder" width="100%" cellspacing="0" cellpadding="0" border="0">
                                <tr height="30">
                                    <td width="59%" style="border:0"><div class="titletext" style="padding-left:10px;padding-top:5px"><c:out value='${lb_name}'/></div></td>
                                    <td width="1%" style="border:0">&nbsp;</td>
                                    <td width="10%" style="border:0"><div align="center" class="titletext" style="padding-left:10px;padding-top:5px"><c:out value="${lb_status}"/></div></td>
                                    <td width="20%" style="border:0"><div align="center" class="titletext" style="padding-left:10px;padding-top:5px"><c:out value="${lb_file_profile}"/></div></td>
                                    <td width="10%" style="border:0">&nbsp;</td>
                                </tr>
                                <tr>
                                    <td id="uploadArea" height="270" colspan="5"><div id="fileQueue" class="fileQueue"></div></td>
                                </tr>
                                <tr>
                                    <td colspan="6"><div id="appl"></div><div id="attachment"></div>
                                        <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
                                            <tr>
                                                <td width="100px" height="32px" align="center" valign="middle" onmouseover="setInputFileDisable(0)">
                                                    &nbsp;<input type="button" onclick="showFilePage();" title="<c:out value='${lb_git_connector_create_job_add_file_tip}'/>" class="standardBtn_mouseout" value="<c:out value='${lb_add_files}'/>">
                                                </td>
                                                <td width="150px" height="32px" align="center" valign="middle">
                                                    &nbsp;<input id="checkFileMapping" type="button"  width="150px" onclick="showFileMapping();" title="Check File Mapping" class="standardBtn_mouseout" value="Check File Mapping">
                                                </td>
                                                <td width="100px" align="center" valign="middle"></td>
                                                <td align="center" class="footertext"><c:out value="${lb_total}"/>: <span id="fileNo">0</span> - <c:out value='${lb_uploaded}'/>: <span id="fileNo2">0</span></td>
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
                                    &nbsp;&nbsp;&nbsp;<input type="checkbox" id="tlControl" onclick="javascript:controlBoxes();" title="Select/Deselect All">&nbsp;<c:out value="${lb_target_locales}"/>
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
                            <select id="priority" name="priority" class="select">
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
                                        <input type="file"  class="attachmentFile" name="selectedAttachmentFile" id="selectedAttachmentFile" class="standardBtn_mouseout" onchange="checkAndUpload();" title="<c:out value='${lb_create_job_browse_tip}'/>">
                                    </td>
                                    <td width="15%"><div id="attributeButtonDIV" style="display:none"><input id="attributeButton" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_job_attributes}'/>" title="<c:out value='${lb_job_attributes}'/>"></div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td></tr>
            </table>    
        </form>
    </div>
    <iframe name="none_iframe" width="0" height="0" scrolling="no" style="display: none"> </iframe>
    <!-- End of Create Job Section -->

</DIV>
</DIV>
<body>
</HTML>
