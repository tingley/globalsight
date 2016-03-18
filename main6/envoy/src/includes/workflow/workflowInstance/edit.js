var workflowDetailData;
var workflowCompanyId;

$(function() {
	
	workflowDetailData = getAjaxValue("getWorkflowDetailDataForEdit");
	workflowDetailData = eval("(" + workflowDetailData + ")");
	
	workflowCompanyId = workflowDetailData["companyId"];
	
	initUI();
	initWorkflow();
	window.onresize=function(){
		updateSize();
	}		
});

function updateSize(){

}

function initUI() {
	UI.initUI();
	updateSize();
	initMethod();
}

function initMethod(){
	$("#saveButton").click(function() {
		
		
		if (validateWorkflow()){
			var xml = getWorkflowXml();
			saveWorkflowInstance(xml);
		}
	});	
}

function addStart(n) {
	var node = new StartNode();
	node.id = n["id"];
	node.init(parseFloat(n["x"]), parseFloat(n["y"]));
	Model.add(node);
}

function addEnd(n) {
	var node = new EndNode();
	node.id = n["id"];
	node.init(parseFloat(n["x"]), parseFloat(n["y"]));
	Model.add(node);
}

function addActivity(n) {
	var node = new ActivityNode();
	node.id = n["id"];
	node.init(parseFloat(n["x"]), parseFloat(n["y"]));
	node.json = n["json"];
	node.state = n["state"];
	Model.add(node);
}

function addCondition(n) {
	var node = new ConditionNode();
	node.id = n["id"];
	node.init(parseFloat(n["x"]), parseFloat(n["y"]));
	Model.add(node);
}

function initWorkflow(){
	
	var data = getAjaxValue("getWorkflowInstanceForEdit");
	data = eval("(" + data + ")");
	
	var lines = data["lines"];
	var nodes = data["nodes"];
	
	for(var i in nodes){
		var node = nodes[i];
		if (node["type"] == 0){
			addStart(node);
		} else if (node["type"] == 1){
			addEnd(node);
		} else if (node["type"] == 2){
			addActivity(node);
		} else if (node["type"] == 5){
			addCondition(node);
		}
	}
	
	for (var i in lines){
		var line = lines[i];
		var start = Model.nodes[line["fid"]];
		var end = Model.nodes[line["tid"]];
		LineData.addLineWithStartAndEnd(start, end, line["name"], line["isDefault"]);
	}
}
