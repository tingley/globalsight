$(function() {
	loadWorkflow();
});

var workflowDetailData;
var workflowCompanyId;

function loadWorkflow() {
	var data = getWorkflowData();
	workflowDetailData = getWorkflowDetailData();
	workflowCompanyId = workflowDetailData["companyId"];
		
	initUI();		
	addWorkflow(data);
	finishInit();
}

function initUI() {
	UI.initUI();
	
	//update the workflow info
	$("#workflowName").html(workflowDetailData.workflowName);
	$("#workflowLocale").html(workflowDetailData.templateSource.displayName + " -> " + workflowDetailData.templateTarget.displayName);

	$(".standardBtn_mouseout").mouseover(function() {
		$(this).removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
	}).mouseout(function() {
		$(this).removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
	});
	
	$(".toolbar_button").mouseover(function() {
		$(this).addClass("toolbar_button_over");
	}).mouseout(function() {
		$(this).removeClass("toolbar_button_over");
	});
	
	$(".menu").find("li").mouseover(function() {
		$(this).addClass("selectedLi");
	}).mouseout(function() {
		$(this).removeClass("selectedLi");
	});	

	$("#saveButton").click(function() {
		if (validateWorkflow()){
			var xml = getWorkflowXml();
			saveWorkflow(xml);
		}
	});	
}

function finishInit() {
	UI.init = true;
	Navigation.draw();
}