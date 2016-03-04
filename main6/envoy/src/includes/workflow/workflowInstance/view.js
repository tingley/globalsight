$(function() {
	UI.editable = false;
	initUI();
	initWorkflow();
	window.onresize=function(){
		updateSize();
	}		
});


function updateSize(){
    var w = $(window).width() - 30;
	$("#viewport").css("width", w);
}

function initUI() {
	UI.intArg();	 
	
	UI.canvas.attr({
		width : 2000,
		height : 2000
	});
	
	UI.snapLineCanvas.attr({
		width : 2000,
		height : 2000
	});
	
	UI.showGrid();
	updateSize();
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
	node.showTxt = function() {
		$('#name' + this.id).html(n["activityName"]);
		$('#user' + this.id).html(n["roleName"]);			
	};
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
	var data = getAjaxValue("getWorkflowInstance");
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
