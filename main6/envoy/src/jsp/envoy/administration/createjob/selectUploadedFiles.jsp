<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         session="true"
%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="/globalsight/includes/dtree_checkbox/dtree.css"/>
<link rel="stylesheet" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<title><c:out value="${lb_uploaded_files}"/></title>
<style type="text/css"> 
#message {
	position:absolute;
	background-color:grey;
	text-align:center;
	z-index:1;
	left:220px;
	top:150px;
	width:250px;
	height:100px;
	font:15px Verdana, Geneva, sans-serif;
	line-height: 50px;
	display:none;
}

.standardText {
    font-family: Arial,Helvetica,sans-serif;
    font-size: 9pt;
}
</style>
<script type="text/javascript" src="/globalsight/includes/dtree_checkbox/dtree.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript">

$(document).ready(function() {
	$(".standardBtn_mouseout").mouseover(function(){
		$(this).removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
	}).mouseout(function(){
		$(this).removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
	}).css("width","100px");
	
	$(".button_out").mouseover(function(){
		$(this).removeClass("button_out").addClass("button_over");
	}).mouseout(function(){
		$(this).removeClass("button_over").addClass("button_out");
	});
	
	$("#closeWin").click(function() {
		window.close();
	});
	
	$("#control").click(function(){
		if ($("#control").attr("checked") == "checked") {
			$("[name='fileId']").attr("checked", true);
		} else {
			$("[name='fileId']").attr("checked", false);
		}
	});
	
	$("#switchFile").click(function(){
		//var base = $("#basePath").html();
		var selectedFiles = new Array();
		$("input[name='fileId']:checked").each(function(){
			var fileId = $(this).attr("value");
			selectedFiles.push(fileId);
		});
		if (selectedFiles.length > 0) {
			var msg = "";
			$("#message").fadeIn(function(){
				var size = selectedFiles.length;
				var times = size % 100 == 0 ? parseInt(size / 100) : parseInt(size / 100) + 1;
				
				for (var j = 0; j < times; j++)
		        {
		            var n = size - 100 * j > 100 ? 100 : size % 100;
		            var sliceOfArray = selectedFiles.slice(j * 100, j * 100 + n);

		            var param = sliceOfArray.join("#@#");
		            
					$.get("/globalsight/ControlServlet?linkName=selectFile&pageName=CJ", 
							{"action":"getSwitchFiles","files":param,"no":Math.random()}, 
							function(paramArray){
								var objs = eval(paramArray);
								
								for(var i = 0; i < objs.length; i++) {
									var fileId = objs[i].id;
									var filePath = objs[i].path;
									var fileName = objs[i].name;
									var fileSize = parseInt(objs[i].size);
						            if (window.opener.switchedFileArray.index(fileId) == -1) {
						            	window.opener.switchedFileArray.push(fileId);
						            	window.opener.addDivElement(fileId, filePath, fileName, fileSize, true);// true is a flag shows it's switched
										$("#ProgressBar" + fileId, window.opener.document).width(window.opener.progressBarWidth);
								        window.opener.queryFileProfile(fileId);
						            } else {
						            	msg += filePath + " is added, ignored. \r\n";
						            }
								}
								$("#message").fadeOut("slow", function(){
									if (msg != "") {
										alert(msg);
									}
								});
							}
						);
		        }
			});
		}
		$("#uploadedDirectory").focus();
	});
	
	$("#expandBtn").click(function(){
		tree.openAll();
		$("#uploadedDirectory").focus();
	});
	
	$("#collapseBtn").click(function(){
		tree.closeAll();
		$("#uploadedDirectory").focus();
	});
	
	$("#removeFile").click(function(){
		var selects = $("[name='fileId']:checked");
		if (selects.length > 0) {
			if (confirm("<c:out value='${msg_job_delete_confirm}'/>")) {
				//var selectedFiles = new Array();
				var msg = "";
				$("input[name='fileId']:checked").each(function(){
					var fileId = $(this).attr("value");
					if (window.opener.switchedFileArray.index(fileId) != -1){
						$(this).attr("checked", false);
						msg = "Some added files cannot be deleted.";
					}
				});
				if (msg != "") {
					alert(msg);
				}
				
				document.forms[0].action += "&action=deleteFile&currentFolderName=<c:out value='${currentFolderName}'/>";
				document.forms[0].submit();
			}
		}
	});
	
	$("[name='fileId']").click(function(){
		$("#control").attr("checked", false);
	});
});

</script>
</head>
<body>
<table CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"><tr><td width="100%"><c:out value="${helper_text_recent_files}"/></td></tr></table>
<br>
<form name="selectFileForm" method="post" action="/globalsight/ControlServlet?pageName=CJ&linkName=selectFile">
<table CELLSPACING="0" CELLPADDING="0" style="border:0px solid black">
<tr VALIGN="middle">
	<td style="border:1px solid black;width:50px;height:20px;background-color:#738eb5;" align="center">
		<input type="checkbox" id="control" title="Select/Deselect All">
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
		<input type="button" id="removeFile" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/trash.png')" title="Delete">
	</td>
</tr>
<tr><td style="height:3px" colspan="7"></td></tr>
</table>
<table CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText" STYLE="height:560px">
    <COL WIDTH=21>   <!-- Checkbox -->
    <COL WIDTH=22>   <!-- Folder/File icon -->
    <COL WIDTH=620>  <!-- File -->
    <COL WIDTH=18>   <!-- Up Folder (for header) -->
    <TR VALIGN="TOP">
		<TD colspan="4" style="border:1px solid black;">
			<div id="message">Adding files... <br>Please don't close this window.</div>
			<div id="uploadedDirectory" style="height:515px;width:681px;overflow:scroll;overflow-x:scroll;">
			<script type="text/javascript">
			var tree = new dTree('tree','<c:out value="${path}"/>','tf');
			tree.add("0","-1","");
			<c:forEach items="${recentFiles}" var="ele">
				tree.add("<c:out value='${ele.fileId}'/>","<c:out value='${ele.parentFileId}'/>","<c:out value='${ele.fileName}'/>","<c:out value='${ele.filePath}'/>");
			</c:forEach>
			document.write(tree);
			</script>
			</div>
		</TD>
	</TR>
	<TR VALIGN="TOP">
		<td colspan="4" height="25px" align="center">
			<br>
			<input id="switchFile" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_create_job_add_to_job}'/>" title="<c:out value='${lb_create_job_add_to_job}'/>">&nbsp;&nbsp;&nbsp;
			<input id="closeWin" type="button" class="standardBtn_mouseout" value="<c:out value='${lb_close}'/>" title="<c:out value='${lb_close}'/>">
		</td>
	</TR>
</table>
</form>
</body>
</html>