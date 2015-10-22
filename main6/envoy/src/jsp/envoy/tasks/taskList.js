var taskItem = ["<tr class=\"row\"><td class=\"taskIdField selectAll\"><input type=\"checkbox\" name=\"taskId\" value=\"", 
                "",   //Task ID, 1
                "\"/></td><td class=\"priorityField standardText \">", 
                "",   //Priority, 3
                "</td><td class=\"overdueField\">",
                "",   //Overdue, 5
                "</td><td class=\"jobIdField standardText center\" width=7%>",
                "",   //Job ID, 7
                "</td><td class=\"jobNameField standardText left\">",
                "",   //Job Name, 9
                "</td><td class=\"isUploadingField standardText center\" width=7%>",
                "",   //isUploading, 11
                "</td><td class=\"activityField standardText left\" width=7%>",
                "",   //Activity Name, 13
                "</td><td class=\"assigneesField standardText left\" width=7%>",
                "",   //Assignees Name, 15
                "</td><td class=\"sourceWordCountField standardText center\" width=7%>",
                "",   //Source Word Count, 17
                "</td><td class=\"translatedText standardText center\" width=7%>",
                "",   //Translated Text, 19
                "</td><td class=\"sourceLocaleField standardText\" width=7%>",
                "",   //Source Locale, 21
                "</td><td class=\"targetLocaleField standardText\" width=7%>",
                "",   //Target Locale, 23
                "</td><td class=\"taskDate standardText\" width=10%>",
                "",   //Accept By or Due By or Completed Date, 25
                "</td><td class=\"ecdDate standardText\" width=7%>",
                "",   //Estimated Completion Date, 27
                "</td><td class=\"taskStatus standardText center\" width=7%>",
                "",   //Task Status, 29
                "</td><td class=\"company standardText center\" width=7%>",
                "",   //Company, 31
                "</td></tr>"];
var recordInfo = ["Displaying <b>",
                  "",	//Start record
                  "</b> - <b>",
                  "",	//End record
                  "</b> of <b>",
                  "",	//Total record
                  "</b>"];

var helpFile = "";
var currentTaskState = "-10";   // "All Status" is the default
var pageNumber = 1;
var rowsPerPage = 20;
var selfUrl = "";
var detailUrl = "";
var wordCountListUrl = "";
var exportUrl = "";
var hasSelected = false;
var needWarning = false;
var getFilterFromRequest = false;
var downloadCheck;
var startExportDate;
var exportEnd = false;
var exportDownloadRandom;
var exportFrom = "task";
var exportPercent = 0;

$(function () {
	//Init task state select
    currentTaskState = $("#state").val();
	rowsPerPage = $("#perPageCount").val();
	pageNumber = $("#pageNumber").val();
    if (currentTaskState == null || currentTaskState == "")
        currentTaskState = "-10";
    selfUrl = $("#selfUrl").val();
    detailUrl = $("#detailUrl").val();
    wordCountListUrl = $("#wordCountListUrl").val();
	exportUrl = $("#exportUrl").val();
	loadGuides();
    ContextMenu.intializeContextMenu();

    initTaskStateSelect();
	initPerPageSetSelect();
	initFilterOptions();

    initSortActions();
    initButtonActions();
    
    getHelpInfo();  //This option will also get task list too.
    checkForDownloadRequest();
});

function initSortActions() {
	$("#jobIdItem").bind("click", function() {
		sortSearch("jobId",false);
	});
	
	$("#jobNameItem").bind("click", function() {
		sortSearch("jobName",false);
	});
	
	$("#activityNameItem").bind("click", function() {
		sortSearch("activityName",false);
	});
	
	$("#sourceWordCountItem").bind("click", function() {
		sortSearch("sourceWordCount",false);
	});
	
	$("#sourceLocaleItem").bind("click", function() {
		sortSearch("sourceLocale",false);
	});
	
	$("#targetLocaleItem").bind("click", function() {
		sortSearch("targetLocale",false);
	});
	
	$("#taskDateItem").bind("click", function() {
		if (currentTaskState == "3")
			sortSearch("ecaDate",true);
		else if (currentTaskState == "8")
			sortSearch("acceptedDate",true);
		else if (currentTaskState == "-1")
			sortSearch("completedDate",true);
		else if (currentTaskState == "6")
			sortSearch("ecdDate",true);
		else if (currentTaskState == "-10")
			sortSearch("ecaDate",true);
	});
}

function sortSearch(sortColumnString, isDate)
{
	rmoveSortImg();
	sortColumn = sortColumnString;
	if(isDate)
		sortColumnString = "taskDate";
	if (sortType == "asc") {
		sortType = "desc";
		$("#"+sortColumnString+"Sort").html($descImg);
	} else {
		sortType = "asc";
		$("#"+sortColumnString+"Sort").html($ascImg);
	}
	submitSearch();
}

function rmoveSortImg()
{	
	$("#prioritySort").html("");
	$("#jobIdSort").html("");
	$("#jobNameSort").html("");
	$("#activityNameSort").html("");
	$("#sourceWordCountSort").html("");	
	$("#taskDateSort").html("");
	$("#sourceLocaleSort").html("");
	$("#targetLocaleSort").html("");
}

function initFilterOptions() {
    //Set up source locale and target locale according with locale pairs
	var tmp = "";
	$("#prioityFilter").bind("keydown", function(e) {
		if (e.which == 13) {
			pageNumber = 1;
			submitSearch();
		}
	});
	
	$("#jobIdFilter").bind("keydown", function(e) {
		if (e.which == 13) {
			pageNumber = 1;
			submitSearch();
		}
	});
	
	$("#jobNameFilter").bind("keydown", function(e) {
		if (e.which == 13) {
			pageNumber = 1;
			submitSearch();
		}
	});
	
	$("#activityNameFilter").bind("keydown", function(e) {
		if (e.which == 13) {
			pageNumber = 1;
			submitSearch();
		}
	});
	
	$("#assigneesNameFilter").bind("keydown", function(e) {
		if (e.which == 13) {
			pageNumber = 1;
			submitSearch();
		}
	});
	$.ajaxSettings.async = false; 
	var random = Math.random();
    $.getJSON("/globalsight/TaskListServlet", {
        action:"getFilterOptions",
        random:random
    }, function(data) {
        var tmp = "";
        if (data.sourceLocales) {
            tmp = "<option value=\"0\">Choose...</option>";
            $.each(data.sourceLocales, function(i, item) {
            	if(item.id == sourceLocale)
            	{          		
            		tmp += "<option value=\"" + item.id + "\" selected>" + item.name + "</option>";
            	}
            	else
            	{
            		tmp += "<option value=\"" + item.id + "\">" + item.name + "</option>";
            	}
            });
            $("#sourceLocaleFilter").append(tmp);
        }
        if (data.targetLocales) {
            tmp = "<option value=\"0\">Choose...</option>";
            $.each(data.targetLocales, function(i, item) {
            	if(item.id == targetLocale)
            	{          		
            		tmp += "<option value=\"" + item.id + "\" selected>" + item.name + "</option>";
            	}
            	else
            	{
            		tmp += "<option value=\"" + item.id + "\">" + item.name + "</option>";
            	}
            });
            $("#targetLocaleFilter").append(tmp);
        }
        if (data.companies) {
            tmp = "<option value=\"0\">Choose...</option>";
            $.each(data.companies, function(i, item) {
            	if(item.id == company)
            	{          		
            		tmp += "<option value=\"" + item.id + "\" selected>" + item.name + "</option>";
            	}
            	else
            	{
            		tmp += "<option value=\"" + item.id + "\">" + item.name + "</option>";
            	}
            });
            $("#companyFilter").append(tmp);
        }
		
		$(".filterSelect").bind("change", function() {
			pageNumber = 1;
			submitSearch();
		});
    });
    $.ajaxSettings.async = true; 
}

function submitSearch()
{
	if(checkFilters())
	{
		getFilterFromRequest = true;
		getTaskList(currentTaskState, pageNumber, rowsPerPage, sortColumn, sortType);
	}
}


function checkFilters()
{
	var tmp = "";
	tmp = ATrim($("#jobIdFilter").val());
	if (tmp != "" && !isAllDigits(tmp)) {
		alert("Invalid job ID, only integer numbers are allowed.");
		return false;
	}
	
	tmp = ATrim($("#jobNameFilter").val()); 
	if (hasSpecialChars(tmp)) {
		alert("Invalid text for job name, wildcard characters and reguler experssions are not supported.");
		return false;
	} else		
		$("#jobNameFilter").val(tmp);
	
	tmp = ATrim($("#activityNameFilter").val()); 
	if (hasSpecialChars(tmp)) {
		alert("Invalid text for activity name, wildcard characters and reguler experssions are not supported.");
		return false;
	} else 
		$("#activityNameFilter").val(tmp);
	
	tmp = ATrim($("#assigneesNameFilter").val()); 
	if (hasSomeSpecialChars(tmp)) {
		alert("Invalid text for assignee name, wildcard characters and reguler experssions are not supported.");
		return false;
	} else 
		$("#assigneesNameFilter").val(tmp);
	
	if(showDateForm)
	{
    	tmp = ATrim($("#acceptanceStartFilter").val());
    	if (tmp != "") 
        {
        	if(!isAllDigits(tmp))
        	{
	    		alert("The Date Range Starts and Ends values must be integers.");
	            return false;
        	}
        	
        	if($("#acceptanceStartOptionsFilter").val() == "")
        	{
        		alert('If you enter a value for a Date Range, you must also select the duration, such as "hours ago"');
	            return false;
        	}
        	
        	if(ATrim($("#acceptanceEndFilter").val()) == "" &&
        			$("#acceptanceEndOptionsFilter").val() != "NOW")
        	{
        		alert("If you enter a start value for a Date Range, you must also enter an end value and vice versa.");
	            return false;
        	}
    	}

    	tmp = ATrim($("#acceptanceEndFilter").val());
    	if (tmp != "") 
        {
        	if(!isAllDigits(tmp))
        	{
	    		alert("The Date Range Starts and Ends values must be integers.");
	            return false;
        	}
        	
        	if($("#acceptanceEndOptionsFilter").val() == "")
        	{
        		alert('If you enter a value for a Date Range, you must also select the duration, such as "hours ago"');
	            return false;
        	}
        	
        	if(ATrim($("#acceptanceStartFilter").val()) == "")
        	{
        		alert("If you enter a start value for a Date Range, you must also enter an end value and vice versa.");
	            return false;
        	}
    	}

    	tmp = ATrim($("#completionStartFilter").val());
    	if (tmp != "") 
        {
        	if(!isAllDigits(tmp))
        	{
	    		alert("The Date Range Starts and Ends values must be integers.");
	            return false;
        	}
        	
        	if($("#completionStartOptionsFilter").val() == "")
        	{
        		alert('If you enter a value for a Date Range, you must also select the duration, such as "hours ago"');
	            return false;
        	}
        	
        	if(ATrim($("#completionEndFilter").val()) == "" &&
        			$("#completionEndOptionsFilter").val() != "NOW")
        	{
        		alert("If you enter a start value for a Date Range, you must also enter an end value and vice versa.");
	            return false;
        	}
    	}

    	tmp = ATrim($("#completionEndFilter").val());
    	if (tmp != "") 
        {
        	if(!isAllDigits(tmp))
        	{
	    		alert("The Date Range Starts and Ends values must be integers.");
	            return false;
        	}
        	
        	if($("#completionEndOptionsFilter").val() == "")
        	{
        		alert('If you enter a value for a Date Range, you must also select the duration, such as "hours ago"');
	            return false;
        	}
        	
        	if(ATrim($("#completionStartFilter").val()) == "")
        	{
        		alert("If you enter a start value for a Date Range, you must also enter an end value and vice versa.");
	            return false;
        	}
    	}
	}
	return true;
}

/**
 * Init the select component of task state
 */
function initTaskStateSelect() {
    var taskStates = ["Available", "In Progress", "Finished", "Rejected", "All Status"];
    var taskStatesValue = ["3", "8", "-1", "6", "-10"];
    for (var index = 0; index < taskStates.length; index++) {
        if (taskStatesValue[index] == currentTaskState) {
            $("#taskStates").append("<option value='" + taskStatesValue[index] + "' selected>" + taskStates[index] + "</option>");
        } else {
            $("#taskStates").append("<option value='" + taskStatesValue[index] + "'>" + taskStates[index] + "</option>");
        }
    }
    $(".searchTask").click(function () {
    	getFilterFromRequest = true;
    	if(currentTaskState != $("#taskStates").val())
    	{  	
    		getFilterFromRequest = false;
    		$("#piorityFilter").val("");
	    	$("#jobIdFilter").val("");
	    	$("#jobNameFilter").val("");
	    	$("#activityNameFilter").val("");
	    	$("#assigneesNameFilter").val("");
	    	$("#sourceLocaleFilter").find("option[value='0']").attr("selected",true);
	    	$("#targetLocaleFilter").find("option[value='0']").attr("selected",true);
	    	$("#companyFilter").find("option[value='0']").attr("selected",true);
	    	$("#jobIdSort").html("");
			$("#jobNameSort").html("");
			$("#activityNameSort").html("");
			$("#taskDateSort").html("");
			$("#jobIdSort").html($descImg);
			pageNumber = 1;
	    	sortColumn = "jobId";
	    	sortType="desc";
    	}
    	currentTaskState = $("#taskStates").val();
    	getHelpInfo();
    });
}

/**
 * Init per page count select component
 */
function initPerPageSetSelect() {
	$("#perPageSet").bind("change", function() {
		rowsPerPage = $(this).val();
		pageNumber = 1;
		submitSearch();
	});
}

/**
 * Init action of each button
 */
function initButtonActions() {
	//updateButtonState();
	
    //Bind a global event to check if has row selected
    $("#listForm :button").bind("click", function() {
        var taskIds = getSelectedTasks();
        if (taskIds == "") {
            alert("Please select at least one record first");
            hasSelected = false;
        } else
            hasSelected = true;
    });

    //Accept
    $("#acceptBtn").bind("click", function () {
        if (!hasSelected)
            return;

        var taskIds = getSelectedTasks();
        $.post(selfUrl, {
            state:currentTaskState,
            taskAction:"acceptAllTasks",
            taskParam:taskIds
        }, function() {
            alert("All selected tasks have been accepted.");
            submitSearch();
        });
    });

    //Translated Text
    $("#translatedTextBtn").bind("click", function() {
        if (!hasSelected)
            return;
        var taskIds = getSelectedTasks();
        var random = Math.random();
        taskIds = taskIds.split(",");
        var j=0;
        var count = setInterval(function translatedTextc()
    	{
        	$.getJSON(selfUrl, {
        		taskAction:"retrieveTranslatedText",
        		state:currentTaskState,
        		taskParam:taskIds[j],
        		random:random
        	}, function(data) {
        		$(data).each(function (i, item) {
        			var taskId = item.taskId;
            		var percent = item.percent;
            		var $row = $("#listForm :checkbox[value='" + taskId + "']");
            		var translatedTextItem =$row.parent().siblings().filter(".translatedText");
            		if (percent < 100)
            		{
            			translatedTextItem.removeClass("completed").addClass("uncompleted");
            		}
            		else
            			translatedTextItem.removeClass("uncompleted").addClass("completed");
            		translatedTextItem.html("("+percent+"%)");
        		});
        	});
        	j++;
    		if(j>=taskIds.length)
        		clearInterval(count);
    	},200);
    });

    //Complete Activity
    $("#completeActivityBtn").bind("click", function() {
        if (!hasSelected)
            return;

        var taskIds = getSelectedTasks();
        var random = Math.random();
        $.getJSON(selfUrl, {
            state:currentTaskState,
            taskAction:"selectedTasksStatus",
            taskParam:taskIds,
            random:random
        }, function(data) {
            if (data.isUploadingJobName) {
                alert("Below activities of jobs are uploading, and you can't complete them! Others will be completed and go to the next one immediately!\n" + data.isUploadingJobName);
            }
            if (data.isNeedScoreTaskId)
            {
            	alert("Below activities of jobs need be scored, and you can't complete them! Others will be completed and go to the next one immediately!\n" + data.isNeedScoreTaskId);
            }
            
            var reportUploadCheckConfirmInfo = "One or more activities you selected require upload Translation Edit/ Reviewer Comments Report before complete the activities. Click OK to complete them all anyway, or Click Cancel to omit those activities.";
            if(taskIds.indexOf(",") < 0)
            	reportUploadCheckConfirmInfo = "The activity requires upload Translation Edit/ Reviewer Comments Report before complete it. Are you sure to continue?";
            var isFinishUnUploadReportTask = true;
            if(data.isNeedReportUploadCheckTaskId)
            {
            	if(!confirm(reportUploadCheckConfirmInfo))
            	{
            		isFinishUnUploadReportTask = false;
            	}
            }
            
            if(isFinishUnUploadReportTask)
            {
            	if (data.isFinishedTaskId) {
                	var confirmInfo = "The activities you selected will be completed and go to the exit of the workflow directly. Are you sure to continue?"; 
                    for(var i=0;i<rowsPerPage;i++)
                    {
                    	taskIds = taskIds.replace(","," ");
                    }
                    if (data.unTranslatedTaskId) {
                    	confirmInfo = "Tasks that are not 100% translated can't be completed, system will ignore them and complete the rest. Are you sure to continue?";
                    }

                    
                	if (confirm(confirmInfo)) {
                		$.post(selfUrl, {
                			state:currentTaskState,
                			taskAction:"completeActivity",
                			taskParam:data.isFinishedTaskId
                		}, function(data) {
                			submitSearch();
                		});
                	}
                }
                else
    			{
                	if (data.unTranslatedTaskId) 
                	{
                		alert ("The selected activities are not 100% translated or not scored, can not be completed.");
                	}
    			}
            }
            if(!isFinishUnUploadReportTask)
            {
            	if (data.isFinishedReportUploadTaskId) {
                	var confirmInfo = "The activities you selected will be completed and go to the next one. Are you sure to continue?"; 
                    for(var i=0;i<rowsPerPage;i++)
                    {
                    	taskIds = taskIds.replace(","," ");
                    }
                    if (data.unTranslatedTaskId) {
                    	confirmInfo = "Tasks that are not 100% translated can't be completed, system will ignore them and complete the rest. Are you sure to continue?";
                    }
	            	if (confirm(confirmInfo) && data.isFinishedReportUploadTaskId) 
	            	{
	            		$.post(selfUrl, {
	            			state:currentTaskState,
	            			taskAction:"completeActivity",
	            			taskParam:data.isFinishedReportUploadTaskId
	            		}, function(data) {
	            			submitSearch();
	            		});
	            	}
	            	else
	            	{
	            		submitSearch();
	            	}   
                }
                else
    			{
    				if (data.unTranslatedTaskId) 
                	{
                		alert ("The selected activities are not 100% translated or not scored, can not be completed.");
                	}
    			}
            }
        });
    });

    //Complete Workflow
    $("#completeWorkflowBtn").bind("click", function() {
        if (!hasSelected)
            return;

        var taskIds = getSelectedTasks();
        var random = Math.random();
        $.getJSON(selfUrl, {
            state:currentTaskState,
            taskAction:"selectedTasksStatus",
            taskParam:taskIds,
            random:random
        }, function(data) {
            if (data.isUploadingJobName) {
                alert("Below activities of jobs are uploading, and you can't complete them! Others will be completed immediately!\n" + data.isUploadingJobName);
            }
            if (data.isNeedScoreTaskId)
            {
            	alert("Below activities of jobs need be scored, and you can't complete them! Others will be completed and go to the next one immediately!\n" + data.isNeedScoreTaskId);
            }
            
            var reportUploadCheckConfirmInfo = "One or more activities you selected require upload Translation Edit/ Reviewer Comments Report before complete the activities. Click OK to complete them all anyway, or Click Cancel to omit those activities.";
            if(taskIds.indexOf(",") < 0)
            	reportUploadCheckConfirmInfo = "The activity requires upload Translation Edit/ Reviewer Comments Report before complete it. Are you sure to continue?";
            var isFinishUnUploadReportTask = true;
            if(data.isNeedReportUploadCheckTaskId)
            {
            	if(!confirm(reportUploadCheckConfirmInfo))
            	{
            		isFinishUnUploadReportTask = false;
            	}
            }
            
            if(isFinishUnUploadReportTask)
            {
            	if (data.isFinishedTaskId) {
                	var confirmInfo = "The activities you selected will be completed and go to the exit of the workflow directly. Are you sure to continue?"; 
                    for(var i=0;i<rowsPerPage;i++)
                    {
                    	taskIds = taskIds.replace(","," ");
                    }
                    if (data.unTranslatedTaskId) {
                    	confirmInfo = "Tasks that are not 100% translated can't be completed, system will ignore them and complete the rest. Are you sure to continue?";
                    }

                    
                	if (confirm(confirmInfo)) {
                		$.post(selfUrl, {
                			state:currentTaskState,
                			taskAction:"completeWorkflow",
                			taskParam:data.isFinishedTaskId
                		}, function(data) {
                			submitSearch();
                		});
                	}
                }
                else
    			{
                	if (data.unTranslatedTaskId) 
                	{
                		alert ("The selected activities are not 100% translated or not scored, can not be completed.");
                	}
    			}
            }
            if(!isFinishUnUploadReportTask)
            {
            	if (data.isFinishedReportUploadTaskId) {
                	var confirmInfo = "The activities you selected will be completed and go to the exit of the workflow directly. Are you sure to continue?"; 
                    for(var i=0;i<rowsPerPage;i++)
                    {
                    	taskIds = taskIds.replace(","," ");
                    }
                    if (data.unTranslatedTaskId) {
                    	confirmInfo = "Tasks that are not 100% translated can't be completed, system will ignore them and complete the rest. Are you sure to continue?";
                    }
	            	if (confirm(confirmInfo) && data.isFinishedReportUploadTaskId) 
	            	{
	            		$.post(selfUrl, {
	            			state:currentTaskState,
	            			taskAction:"completeWorkflow",
	            			taskParam:data.isFinishedReportUploadTaskId
	            		}, function(data) {
	            			submitSearch();
	            		});
	            	}
	            	else
	            	{
	            		submitSearch();
	            	}   
                }
                else
    			{
    				if (data.unTranslatedTaskId) 
                	{
                		alert ("The selected activities are not 100% translated or not scored, can not be completed.");
                	}
    			}
            }
        });
    });

    //Detail word count
    $("#detailWordCountBtn").bind("click", function() {
        if (!hasSelected)
            return;

        var taskIds = getSelectedTasks();
		var action = wordCountListUrl + "&action=one&taskid=" + taskIds;
        $("#listForm").attr("action", action);
        $("#listForm").submit();
    });

    //Download
    $("#downloadBtn").bind("click", function() {
        if (!hasSelected)
            return;

        var taskIds = getSelectedTasks();
        var downloadAction = "download";
        var random = Math.random();
        $.getJSON(selfUrl, {
            taskAction:"filterZeroWCTasksForOfflineDownload",
            taskParam:taskIds,
            state:currentTaskState,
            random:random
        }, function(data) {
            var selectedTasks = data.selectedTaskIds;
            if (selectedTasks == "0") {
                alert("Zero wordcount task will not be offline downloaded.");
                return;
            }
            var downloadUrl = selfUrl + "&taskAction=";
            if (currentTaskState == "3") {
                //Available
                $("#listForm").attr("action", downloadUrl + "acceptTaskAndDownload&taskParam=" + selectedTasks).submit();
            } else if (currentTaskState == "8") {
                //In Progress
                showProgressDiv();
                $("#listForm").attr("action", downloadUrl + "downloadALLOfflineFiles&taskParam=" + selectedTasks).submit();
            }
        });
    });

    //Download
    $("#downloadCombinedBtn").bind("click", function() {
        if (!hasSelected)
            return;

        var taskIds = getSelectedTasks();
        var random = Math.random();
        $.getJSON(selfUrl, {
            taskAction:"filterZeroWCTasksForOfflineDownload",
            taskParam:taskIds,
            state:currentTaskState,
            random:random
        }, function(data) {
            var selectedTasks = data.selectedTaskIds;
            if (selectedTasks == "0") {
                alert("Zero wordcount task will not be offline downloaded.");
                return;
            }
            var downloadUrl = selfUrl + "&taskAction=";
            if (currentTaskState == "3") {
                //Available
                $("#listForm").attr("action", downloadUrl + "DownloadCombined&taskParam=" + selectedTasks).submit();
            } else if (currentTaskState == "8") {
                //In Progress
                showProgressDiv();
                $("#listForm").attr("action", downloadUrl + "downloadALLOfflineFilesCombined&taskParam=" + selectedTasks).submit();
            }
        });
    });
    
    //Search
    $("#searchReplaceBtn").bind("click", function() {
    	if (!hasSelected)
			return;
    	
    	var searchUrl = $("#searchUrl").val() + "&search=";
    	var jobId = "";
    	var $selectedRows = getSelectedRows();
    	$selectedRows.each(function(i, item) {
    		jobId = $(this).parent().parent().find(".jobIdField").text();
	    });
    	
    	$("#listForm").attr("action", searchUrl + jobId).submit();
    });

    //Export
	$("#exportBtn").bind("click", function() {
		if (!hasSelected)
			return;
		
		var taskIds = getSelectedTasks();
        var wfId = "-1";
        var random = Math.random();
        $.getJSON("/globalsight/TaskListServlet", {
            action:"checkUploadingStatus",
            state:currentTaskState,
            taskId:taskIds,
            exportFrom:exportFrom,
            random:random
        }, function(data) {
        	wfId = data.workflowId;
        	var action = exportUrl + "&exportSelectedWorkflowsOnly=true&wfId=" + wfId + "&taskId=" + taskIds + "&state=" + currentTaskState;
        	if(data.isUploading)
        	{
        		alert("The activity is uploading. Please wait.");
        	}
        	else
        	{
        		$("#listForm").attr("action", action);
                $("#listForm").submit();
        	}
        });
	});
	
	$("#exportDownloadBtn").bind("click", function() {
		if (!hasSelected)
			return;
		
		var taskIds = getSelectedTasks();
        var random = Math.random();
        exportDownloadRandom = Math.random();
        $.getJSON("/globalsight/TaskListServlet", {
            action:"checkUploadingStatus",
            state:currentTaskState,
            taskId:taskIds,
            exportFrom:exportFrom,
            random:random
        }, function(data) {
        	if(data.isUploading)
        	{
        		alert("The activity is uploading. Please wait.");
        	}
        	else
        	{
        		$.getJSON("/globalsight/TaskListServlet", {
                    action:"export",
                    state:currentTaskState,
                    taskId:taskIds,
                    exportFrom:exportFrom,
                    random:random
                }, function(data) {
                	startExportDate = data.startExportDate;
                	exportEnd = false;
                	exportPercent = 0;
            		if(downloadCheck != null)
            		{
            			clearInterval(downloadCheck);
            			downloadCheck = null;
            		}
                	showExportDownloadProgressDiv();
                });
        	}
        });
    });

    //Offline upload
	$("#offlineUploadBtn").unbind("click");
	$("#offlineUploadBtn").bind("click", function() {
		window.location.href = "/globalsight/ControlServlet?activityName=simpleofflineupload";
	});
}

function download()
{
	var taskIds = getSelectedTasks();
    var random = Math.random();
    var exportDownloadMessage = "";
	$.getJSON("/globalsight/TaskListServlet", {
        action:"download",
        state:currentTaskState,
        taskId:taskIds,
        startExportDate:startExportDate,
        exportDownloadRandom:exportDownloadRandom,
        exportFrom:exportFrom,
        random:random
    }, function(data) {
    	if(!exportEnd)
    	{
    		if (data.selectFiles != "")
    		{
    			exportEnd = true;
    			window.clearInterval(downloadCheck);
    			downloadCheck = null;
    			var selectedFiles = "";
    			$.each(data.selectFiles, function(i, item) {
    				item = encodeURIComponent(item.replace(/%C2%A0/g, "%20"));
    				selectedFiles += ("," + item);
    			});
    			selectedFiles = selectedFiles.substring(1,selectedFiles.length);
    			$("#selectedFileList").val(selectedFiles);
    			downloadFilesForm.action = "/globalsight/ControlServlet?linkName=downloadApplet&pageName=CUST_FILE_Download&action=download&taskId="+taskIds+"&state=8&isChecked="+false;
    			downloadFilesForm.submit();
    		}
    		if(data.percent == 100 && data.selectFiles != "")
    		{
    			exportDownloadMessage = "Finish export. Start download."
    			showExportDownloadProgress("", data.percent, exportDownloadMessage);
    			exportPercent = 0;
    		}
    		if(exportPercent < data.percent && data.percent < 100 )
    		{
    			exportPercent = data.percent;
    			showExportDownloadProgress("", data.percent, exportDownloadMessage);
    		}
    	}
    });
}

function wordCountLink(taskId)
{
	var action = wordCountListUrl + "&action=one&taskid=" + taskId;
    $("#listForm").attr("action", action);
    $("#listForm").submit();
}
/**
 * Check if there is any records are selected.
 * @returns {boolean} If true, have some records selected.
 */
function selectedCheck() {
    if (getSelectedRows().length == 0) {
        alert("Please select at least one record first");
        return false;
    } else
        return true;
}

function getSelectedRows() {
	return $("#listForm :checkbox:not(#selectAllCbx):checked");
}

function getSelectedTasks() {
    var $selectedRows = getSelectedRows();
    var tmp = "";
    if ($selectedRows.length == 0)
    	return "";
    
    $selectedRows.each(function(i, item) {
    	if (tmp != "")
    		tmp += "," + $(this).val();
    	else
    		tmp = $(this).val();
    });
    return tmp;
}

function setListStyle() {
    $("#list tbody tr:odd").addClass("rowOdd");
    
    $("#list tbody > tr").click(function() {
        if ($(this).hasClass("rowSelected")) {
            $(this).removeClass("rowSelected").find(":checkbox").attr("checked", false);
			if ($("#selectAllCbx").is(":checked"))
				$("#selectAllCbx").attr("checked", false);
		}
        else {
            $(this).addClass("rowSelected").find(":checkbox").attr("checked", true);
		}
		disableButtons();
    });
	$("#selectAllCbx").click(function() {
		$(":checkbox:not(#selectAllCbx)").attr("checked", this.checked);
		if (this.checked) {
			$("#list tbody > tr").addClass("rowSelected");
		} else 
			$("#list tbody > tr").removeClass("rowSelected");
		disableButtons();
	});
}

function disableButtons() {
	var $selectedRows = getSelectedRows();
	var length = $selectedRows.length;
	if(length == 0)
	{
		$("#detailWordCountBtn").attr("disabled", false);
		$("#exportBtn").attr("disabled", false);
		$("#exportDownloadBtn").attr("disabled", false);
		$("#downloadCombinedBtn").attr("disabled", false);
		$("#searchReplaceBtn").attr("disabled", false);
	}
	else if (length == 1) {
		$("#detailWordCountBtn").attr("disabled", false);
		$("#exportBtn").attr("disabled", false);
		$("#exportDownloadBtn").attr("disabled", false);
		$("#searchReplaceBtn").attr("disabled", false);
		if($("#isCombinedFormat").val() == "false")
		{		
			$("#downloadCombinedBtn").attr("disabled", true);
		}
		else
		{
			$("#downloadCombinedBtn").attr("disabled", false);
		}
	} 
	else if (length > 1) {
		$("#detailWordCountBtn").attr("disabled", true);
		$("#exportBtn").attr("disabled", true);
		$("#exportDownloadBtn").attr("disabled", true);
		$("#searchReplaceBtn").attr("disabled", true);
		
		var sourceLocale = "";
		var targetLocale = "";
		var company = "";
		var localeEqual = true;
		$selectedRows.each(function(i, item) {
			var thisSourceLocale = $(this).parent().parent().find(".sourceLocaleField").text();
			var thisTargetLocale = $(this).parent().parent().find(".targetLocaleField").text();
			var thisCompany = $(this).parent().parent().find(".company").text();
			if(sourceLocale == "")
			{	
				sourceLocale = thisSourceLocale;
				targetLocale = thisTargetLocale;
				company = thisCompany;
			}
			if(sourceLocale != thisSourceLocale || targetLocale != thisTargetLocale
					|| company != thisCompany)
			{
				localeEqual = false;
			}
	    });
		if(localeEqual && $("#isCombinedFormat").val() != "false")
		{
			$("#downloadCombinedBtn").attr("disabled", false);
		}
		else
		{
			$("#downloadCombinedBtn").attr("disabled", true);
		}
	}
}

function getTaskList(state, pagenum, rowsperpage, sortcolumn, sorttype) {
	$("#list tbody").empty();
	$("#list tbody").append("<tr><td colspan=20 align='center'><img src='/globalsight/includes/tasksloading.gif'/></td></tr>");
	$("#taskStates").attr("disabled", true);
	var acceptanceStartFilter = $("#acceptanceStartFilter").val();
	var acceptanceStartOptionsFilter = $("#acceptanceStartOptionsFilter").val();
	var acceptanceEndFilter = $("#acceptanceEndFilter").val();
	var acceptanceEndOptionsFilter = $("#acceptanceEndOptionsFilter").val();
	var completionStartFilter = $("#completionStartFilter").val();
	var completionStartOptionsFilter = $("#completionStartOptionsFilter").val();
	var completionEndFilter = $("#completionEndFilter").val();
	var completionEndOptionsFilter = $("#completionEndOptionsFilter").val();
	var priorityFilter = $("#priorityFilter").val();
	var jobIdOption = $("#jobIdOption").val();
    var jobIdFilter = $("#jobIdFilter").val();
    var jobNameFilter = $("#jobNameFilter").val();
    var activityNameFilter = $("#activityNameFilter").val();
    var assigneesNameFilter = $("#assigneesNameFilter").val();
    var sourceLocaleFilter = $("#sourceLocaleFilter").val();
    var targetLocaleFilter = $("#targetLocaleFilter").val();
    var companyFilter = $("#companyFilter").val();
    var random = Math.random();
	if(sortcolumn == "")
	{
		sortcolumn = "jobId";
		sorttype = "desc";
		
		var $descImg = $("<img border=0 width=7 hspace=1 height=4 src=\"/globalsight/images/sort-down.gif\">");
		$("#jobIdSort").html($descImg);
		sortType = "desc";
	}
    $.getJSON("/globalsight/TaskListServlet", 
		{
			action:"getTaskList",
			state:state,
			pageNumber:pagenum,
            rowsPerPage:rowsperpage,
			sortColumn:sortcolumn,
			sortType:sorttype,
			advancedSearch:advancedSearch,
			acceptanceStartFilter:acceptanceStartFilter,
			acceptanceStartOptionsFilter:acceptanceStartOptionsFilter,
			acceptanceEndFilter:acceptanceEndFilter,
			acceptanceEndOptionsFilter:acceptanceEndOptionsFilter,
			completionStartFilter:completionStartFilter,
			completionStartOptionsFilter:completionStartOptionsFilter,
			completionEndFilter:completionEndFilter,
			completionEndOptionsFilter:completionEndOptionsFilter,
			priorityFilter:priorityFilter,
			jobIdOption:jobIdOption,
            jobIdFilter:jobIdFilter,
            jobNameFilter:jobNameFilter,
            activityNameFilter:activityNameFilter,
            assigneesNameFilter:assigneesNameFilter,
            sourceLocaleFilter:sourceLocaleFilter,
            targetLocaleFilter:targetLocaleFilter,
            companyFilter:companyFilter,
            getFilterFromRequest:getFilterFromRequest,
            random:random
		}, function(data) {
			if(data == "sessionTimeout")
			{
				window.location = "ControlServlet?activityName=login";
				return;
			}
			else
			{
				$("#list tbody").empty();
				var tls = [], hash = {};
				$.each(data.tasks, function(i, item) {
					var tmp = taskItem;
					tmp[1] = item.taskId;
					tmp[3] = item.priority;
					if (item.overdue) {
						tmp[5] = "<img width=\"8\" alt=\"Overdue\" src=\"/globalsight/images/dot_red.gif\"";
					} else 
						tmp[5] = "";
					tmp[7] = item.jobId;
					var tmpJobName = item.jobName;
					if (item.state == -1 || item.state==6 || item.state==10)
						tmp[9] = item.jobName;
					else {
						var contextForTab = "oncontextmenu=\"contextForTab('" + item.state + "','" + item.taskId + "',event)\"";
						tmp[9] = "<a href=\"" + detailUrl + "&state=" + currentTaskState + "&taskId=" + item.taskId + "&jobname=" + item.jobName + "\" " + contextForTab + ">" + item.jobName + "</a>";
					}
					if(item.isUploading == "Yes")
					{
						tmp[11] = "<span style=\"color:red\">Yes</span>";
					}
					else
					{
						tmp[11] = "No";
					}
					tmp[13] = item.activityName;
					if(item.state == -1 || item.state==6)
					{
						tmp[15] = "";
					}
					else
					{
						tmp[15] = item.assignees;
					}
					if (item.state == 6)
						tmp[17] = item.wordCount;
					else
						tmp[17] = "<a href=\"#\" class=\"wordCountLink\" onclick='wordCountLink(" + item.taskId + ")'>" + item.wordCount + "</a>";
					tmp[19] = "";
					tmp[21] = item.sourceLocaleName;
					tmp[23] = item.targetLocaleName;
					if (!hash[item.targetLocaleName])
					{
						tls.push(item.targetLocaleName);
						hash[item.targetLocaleName] = true;
					}
					tmp[25] = item.taskDateString;
					tmp[27] = item.estimatedCompletionDateString;
					tmp[29] = item.stateString;
					tmp[31] = item.companyName;
					
					$("#list tbody").append(tmp.join(""));
				});
				
				var recordInfoItem = recordInfo;
				recordInfoItem[1] = data.begin;
				recordInfoItem[3] = data.end;
				recordInfoItem[5] = data.total;
				if(data.total != 0)
				{		
					$(".recordInfo").html(recordInfoItem.join(""));
					var pageNavInfo = generatePageNav(data.pageNumber, data.totalPages);
					$(".pageNav").html(pageNavInfo);
					var perPageSet = data.perPageCount;
					$("#perPageSet").val(perPageSet);
				}
				else
				{
					$(".recordInfo").html("Displaying 0 records");
					$(".pageNav").html("");
					$("#perPageSet").val(perPageSet);
				}
				
				$(".targetLocaleSet").remove();
				for(i=0;i<tls.length;i++)
				{
					var tl = tls[i];
					tl = tl.substring(tl.indexOf("[")+1,tl.indexOf("]"));
					$("#listForm").append($("<input TYPE='hidden' class='targetLocaleSet' NAME='languageSet' value='"+tl+"'/>"));
				}
				
				setListStyle();
				updateButtonState(state,pagenum,rowsperpage,sortcolumn,sorttype,jobIdFilter,jobNameFilter,activityNameFilter,assigneesNameFilter,
								sourceLocaleFilter,targetLocaleFilter,companyFilter,jobIdOption,priorityFilter,
								acceptanceStartFilter,acceptanceStartOptionsFilter,acceptanceEndFilter,acceptanceEndOptionsFilter,
								completionStartFilter,completionStartOptionsFilter,completionEndFilter,completionEndOptionsFilter,advancedSearch);
				
				$("#taskStates").attr("disabled", false);
			}
		});
};

function goto(pagenum) {
	 pageNumber = pagenum;
	 submitSearch();
}

function generatePageNav(pagenum, totalpage) {
	var pageNav = "";
	if (pagenum == 1) {
		pageNav += "<span class='gray'>First | Previous</span>";
	} else {
		pageNav += "<a href='#' onclick='goto(1);'>First</a> | ";
		pageNav += "<a href='#' onclick='goto(" + (pagenum-1) + ");'>Previous</a>";
	}
	pageNav += " | ";
	var start = 1;
	var end = 5;
	if (pagenum > end) {
		start = pagenum - end + 1;
		end = start + 5;
	}
	if (end > totalpage)
		end = totalpage;
	for (var i = start; i<=end; i++) {
		if (i == pagenum) {
			pageNav += "<span class='gray'>" + i + "</span> ";
		} else {
			pageNav += "<a href='#' onclick='goto(" + i + ");'>" + i + "</a> ";
		}
	}
	if (pagenum == totalpage) {
		pageNav += "<span class='gray'>| Next | Last</span>";
	} else {
		if (pagenum < totalpage) {
			pageNav += "| <a href='#' onclick='goto(" + (pagenum+1) + ");'>Next</a>";
		}
		pageNav += " | <a href='#' onclick='goto(" + totalpage + ");'>Last</a>";
	}
	
	return pageNav;
}

function getHelpInfo() {
	$("#helpText").val("");
	helpFile = "";
	$.ajaxSettings.async = false; 
	var random = Math.random();
    $.getJSON("/globalsight/TaskListServlet", 
	{
		action:"getHelpInfo",
		state:currentTaskState,
		random:random
	}, function(data) {
		$("#stateString").html(data.stateString);
		$("#taskDateLabel").html(data.taskDateLabel);
		$("#helpText").html(data.helpText);
		helpFile = data.helpFile;
        submitSearch();
    });
    $.ajaxSettings.async = true;
};

function showButton(button, isShow) {
    if (isShow)
        $("#" + button).show();
    else
        $("#" + button).hide();
}

function updateButtonState(state,pagenum,rowsperpage,sortcolumn,sorttype,jobIdFilter,jobNameFilter,activityNameFilter,assigneesNameFilter,
						sourceLocaleFilter,targetLocaleFilter,companyFilter,jobIdOption,priorityFilter,
						acceptanceStartFilter,acceptanceStartOptionsFilter,acceptanceEndFilter,acceptanceEndOptionsFilter,
						completionStartFilter,completionStartOptionsFilter,completionEndFilter,completionEndOptionsFilter,advancedSearch) {
		var random = Math.random();
		$.getJSON("/globalsight/TaskListServlet?random="+Math.random(), {
            action:"getButtonStatus",
            state:currentTaskState,
            random:random
        }, function(data) {
            showButton("acceptBtn", data.accept);
            showButton("translatedTextBtn", data.translatedText);
            showButton("completeActivityBtn", data.completeActivity);
            showButton("completeWorkflowBtn", data.completeWorkflow);
            showButton("detailWordCountBtn", data.detailWordCount);
            showButton("searchReplaceBtn", data.searchReplace);
            showButton("exportBtn", data.isExport);
            showButton("downloadBtn", data.download);
            showButton("downloadCombinedBtn", data.downloadCombined);
            showButton("offlineUploadBtn", data.offlineUpload);
            showButton("exportDownloadBtn", data.exportDownload);

            showAndHide(currentTaskState);
            
            if(!data.showAssignees)
            {
            	$(".assigneeItem").hide();
                $(".assigneesField").hide();
            }
        });
}

function showAndHide(currentTaskState)
{	
	$(".taskStatus").hide();
    $(".activityItem").show();
    $(".activityField").show();
    $(".assigneeItem").show();
    $(".assigneesField").show();
    $(".isUploadingItem").hide();
    $(".isUploadingField").hide();
    if ($("#isSuperUser").val() == "true")
    	$(".company").show();
    else
    	$(".company").hide();          
    
    switch (parseInt(currentTaskState)) {
        case 3: //Available
            $(".translatedText").show();
            $(".taskDate").show();
            $(".ecdDate").show();
            break;
        case 8: //In Progress
            $(".translatedText").show();
            $(".taskDate").show();
            $(".ecdDate").hide();
            $(".isUploadingItem").show();
            $(".isUploadingField").show();
            break;
        case -1: //Finish
            $(".translatedText").hide();
            $(".assigneeItem").hide();
            $(".assigneesField").hide();
            $(".taskDate").show();
            $(".ecdDate").hide();
            break;
        case 6: //Reject
            $(".translatedText").hide();
            $(".assigneeItem").hide();
            $(".assigneesField").hide();
            $(".taskDate").show();
            $(".ecdDate").hide();
            break;
        default: //All
            $(".translatedText").hide();
            $(".taskDate").show();
            $(".ecdDate").hide();
            $(".taskStatus").show();
            break;
    }
}

function contextForTab(taskState, taskId, e)
{
	if(e instanceof Object)
	{
		e.preventDefault();
		e.stopPropagation();
	}
	var popupoptions;
	var random = Math.random();
	$.ajaxSettings.async = false; 
	$.getJSON("/globalsight/TaskListServlet", {
		state:currentTaskState,
		action:"getContextForTab",
		taskId:taskId,
		random:random
	}, function(data) {
		var workOfflineUrl = data.workOfflineUrl;
		var secondaryTargetFilesUrl = data.secondaryTargetFilesUrl;
		var commentUrl = data.commentUrl;
		var scorecardUrl = data.scorecardUrl;
		var isShowComment = data.isShowComment;
		var isShowScorecard = data.isShowScorecard;
		
		var targetFilesItem = new ContextItem(data.targetFilesLabel, function(){ location.href="/globalsight/ControlServlet?linkName=detail&pageName=TK1&taskAction=getTask&taskType=TRANSZ&state=" + taskState + "&taskId=" + taskId;});
		popupoptions = [targetFilesItem];
		var secondaryTargetFilesItem;
		if (secondaryTargetFilesUrl != "")
		{
			secondaryTargetFilesItem = new ContextItem(data.secondaryTargetFilesLabel, function(){ location.href=secondaryTargetFilesUrl;});
			popupoptions.push(secondaryTargetFilesItem);
		}
		var workOfflineItem;
		if (workOfflineUrl != "")
		{
			workOfflineItem = new ContextItem(data.workOfflineLabel, function(){ location.href=workOfflineUrl;});
			popupoptions.push(workOfflineItem);
		}
		var commentItem;
		if (isShowComment)
		{
			commentItem = new ContextItem(data.commentLabel, function(){ location.href=commentUrl;});
			popupoptions.push(commentItem);
		}
		var scorecardItem;
		if(isShowScorecard)
		{
			scorecardItem = new ContextItem(data.scorecardLabel, function(){ location.href=scorecardUrl;});
			popupoptions.push(scorecardItem);
		}
		var qaChecksItem;
		if(data.isShowQaChecks)
		{
			qaChecksItem = new ContextItem(data.qaChecksLabel, function(){ location.href=data.qaChecksUrl;});
			popupoptions.push(qaChecksItem);
		}
	});
	$.ajaxSettings.async = true; 
	ContextMenu.display(popupoptions, e);
}

function checkForDownloadRequest()
{
    if($("#acceptDownloadRequested").val() != "false")
    {
        if ($("#taskParam").val() != "")
        {
        	var actionValue = "downloadALLOfflineFiles";
        	if ($("#isDownloadCombined").val() != "false")
        	{
        		actionValue = "downloadALLOfflineFilesCombined";
        	}
        	
            var action = selfUrl + "&taskAction=" + actionValue;
            action += "&taskParam=" + $("#taskParam").val();
            showProgressDiv();
            $("#listForm").attr("action", action).submit();
        }      
    }
    
    if ($("#errorMsg").val() != "") {
    	showProgressDivError();
    }      
}

function showProgressDiv()
{
    idMessagesDownload.innerHTML = "";
    document.getElementById("idProgressDownload").innerHTML = "0%"
    document.getElementById("idProgressBarDownload").style.width = 0;
    document.getElementById("idProgressDivDownload").style.display = "";
    o_intervalRefresh = window.setInterval("doProgressRefresh()", 300);
}

function showExportDownloadProgressDiv()
{
	if(downloadCheck == null)
	{
		idExportDownloadMessagesDownload.innerHTML = "";
		document.getElementById("idExportDownloadProgressDownload").innerHTML = "0%"
		document.getElementById("idExportDownloadProgressBarDownload").style.width = 0;
		document.getElementById("idExportDownloadProgressDivDownload").style.display = "";
		showExportDownloadProgress("", 0 , "Start Export...");
		downloadCheck = window.setInterval("download()", 2000);
	}
}

function showProgressDivError()
{
    document.getElementById("idProgressDivDownload").style.display = "";
    doProgressRefresh();

    if (o_intervalRefresh)
        window.clearInterval(o_intervalRefresh);
}
