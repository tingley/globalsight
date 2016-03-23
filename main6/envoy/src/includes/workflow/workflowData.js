var workflow;
function addWorkflow(data) {
	if (data != ""){
		workflow = eval("(" + data + ")");
		addStart(workflow);
	} else {
		var start = new StartNode();		
		start.init(50, 50);
		start = Model.add(start);
	}
}

function addStart(workflow) {
	var startNode = workflow["end-state"]["action"]["start_node"];
	var point = startNode["point"].split(':');
	
	var start = new StartNode();		
	start.init(parseFloat(point[0]), parseFloat(point[1]));
	start = Model.add(start);
	start.json = workflow["start-state"];
	start.startJson = startNode;
	
	var startState = workflow["start-state"];
	var next = startState["transition"]["@to"];
	var name = startState["transition"]["@name"];
	
	addNext(start, next, name);
}

function addEnd() {
	var node = workflow["end-state"];
	var point = node["action"]["point"].split(':');
	
	var name = "Exit";
	var n = Model.getNodeByName(name);
	if (n == null){
		n = new EndNode();
		n.data.name = name;
		n.init(parseFloat(point[0]), parseFloat(point[1]));		
		n = Model.add(n);		
		
		n.josn = node;
	}	
	
	return n;
}

function isActivityExist(activity){
	var activities = workflowDetailData["activities"];
	for (var i in activities){
		if (activity == activities[i]["name"]){
			return true;
		}
	}
	return false;
}

function addActivity(activity) {
	var node = activity["task"];
	var assignment = activity["task"]["assignment"];
	var point = assignment["point"].split(':');
	var activityName = assignment["activity"];
	
	var name = activity["@name"];
	var n = Model.getNodeByName(name);
	if (n == null){
		n = new ActivityNode();		
		n.init(parseFloat(point[0]), parseFloat(point[1]));
		
		if (isActivityExist(activityName)){
			n.data.name = name;		
			n.json = activity;
		}
		
		n = Model.add(n);
	}
	
	addedNodes[name] = n;
	
    var next = activity["transition"]["@to"];
    var name = activity["transition"]["@name"];
    addNext(n, next, name);
	
	return n;
}

function addCondition(condition) {
	var handler = condition["handler"];
	var point = handler["point"].split(':');	
	
	var name = condition["@name"];
	var n = Model.getNodeByName(name);
	if (n == null){
		n = new ConditionNode();
		n.data.name = name;
		n.init(parseFloat(point[0]) + 15, parseFloat(point[1]) + 15);		
		n = Model.add(n);	
		
		n.josn = condition;
	}	
	
	addedNodes[name] = n;
	
	//get default
	var spec= handler["workflow_condition_spec"];		
    var nexts = condition["transition"];
	
    if (nexts.length) {
    	for (var i = 0; i < nexts.length; i++) {
    		var next = nexts[i]["@to"];
    		var name = nexts[i]["@name"];
    		
    		var isDefault = spec["workflow_branch_spec_" + i]["is_default"];
    		addNext(n, next, name, isDefault == "true");
    	}	
    } else {
    	// only one condition
    	var next = nexts["@to"];
		var name = nexts["@name"];
		addNext(n, next, name, true);
    }
	
	return n;
}

var addedNodes = {};
function addNext(node, next, name, isDefault) {
	// has added.
	var n = addedNodes[next];
	if (typeof(n) != "undefined") {
		LineData.addLineWithStartAndEnd(node, n, name, isDefault);
		return;
	}
	
	if (next == "Exit") {
		var end = addEnd();
		var l = LineData.addLineWithStartAndEnd(node, end, name, isDefault);
		l.data.txt = name;
		addedNodes[next] = end;
		return;
	}
	
	var t = getTask(next);
	if (t != null) {
		var activity = addActivity(t);
		LineData.addLineWithStartAndEnd(node, activity, name, isDefault);
	} else {
		t = getCondition(next);
		if (t != null) {
			var condition = addCondition(t);
			LineData.addLineWithStartAndEnd(node, condition, name, isDefault);			
		} 		
	}
}

function getTask(name) {
	var ts = workflow["task-node"];
	if (ts && ts["@name"] == name)
		return ts;
	
	for ( var i in ts) {
		var t = ts[i];
		
		if (t["@name"] == name)
			return t;
	}
	
	return null;
}

function getCondition(name) {
	var ts = workflow["decision"];
	if (ts && ts["@name"] == name)
		return ts;
	
	for ( var i in ts) {
		var t = ts[i];
		
		if (t["@name"] == name)
			return t;
	}
	
	return null;
}
