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

function addActivity(activity) {
	var node = activity["task"];
	var assignment = activity["task"]["assignment"];
	var point = assignment["point"].split(':');
	
	var roleName = assignment["role_name"];
	var activityName = assignment["activity"];
	var i = activityName.lastIndexOf("_");
	activityName = activityName.substring(0,i);
	
	var name = activity["@name"];
	var n = Model.getNodeByName(name);
	if (n == null){
		n = new ActivityNode();		
		n.init(parseFloat(point[0]), parseFloat(point[1]));
		
		n.data.name = name;		
		n.json = activity;
		
		n = Model.add(n);
	}
	
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
		addedNodes[next] = activity;
	} else {
		t = getCondition(next);
		if (t != null) {
			var condition = addCondition(t);
			LineData.addLineWithStartAndEnd(node, condition, name, isDefault);
			addedNodes[next] = condition;
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

var sequence = 0;
function getSequence() {
	sequence = sequence + 1;
	return sequence;
}

function getTemplateNumber(n) {
	return n.toFixed(1);
}

function validateWorkflow() {
	var startNode;
	var endNode;
	
	// for start node
	for (var b in Model.nodes){
		var node = Model.nodes[b];
		if (node.type == "startNode") {
			startNode = node;
			break;
		}
	}
	
	if (typeof(startNode) == "undefined"){
		alert(msg_no_start_node);
		return false;
	}
	
	var tl = startNode.tos[0];
	if (typeof(tl) == "undefined"){
		alert(msg_start_no_out);
		return false;
	}
	
	// for end node
	for (var b in Model.nodes){
		var node = Model.nodes[b];
		if (node.type == "endNode") {
			if (typeof(endNode) == "undefined"){
				endNode = node;
			} else {
				alert(msg_more_end_node);
				return false;
			}	
		}
	}
	
	if (typeof(endNode) == "undefined"){
		alert(msg_no_end_node);
		return false;
	}
	
	tl = endNode.froms[0];
	if (typeof(tl) == "undefined"){
		alert(msg_end_no_income);
		return false;
	}
	
	// for other
	for (var b in Model.nodes){
		var node = Model.nodes[b];
		if (node.type != "endNode" && node.type != "startNode") {
			tl = node.froms[0];
			if (typeof(tl) == "undefined"){
				alert(msg_no_income);
				return false;
			}
			
			tl = node.tos[0];
			if (typeof(tl) == "undefined"){
				alert(msg_no_out);
				return false;
			}
			
			if (node.type == "activityNode"){
				var activity = node.getAssignmentValue("activity"); 
				if (activity == "") {
					alert(msg_no_activity);
					return false;
				}
			}			
		}
	}
	
	return true;
}

var startData = {};
var endData = {};
var startTemplate = $("#startTemplate").html();
function getWorkflowXml() {
	startData = {};
	endData = {};
	generatedNode = {};
	sequence = 0;
	
	var startNode;
	for (var b in Model.nodes){
		var node = Model.nodes[b];
		if (node.type == "startNode") {
			startNode = node;
			break;
		}
	}
	
	var node = startNode;
	var xml = "";
	
	// start node.
	endData["x1"] = getTemplateNumber(node.locale.x);
	endData["y1"] = getTemplateNumber(node.locale.y);
	
	var tl = node.tos[0];
	startData["transition"] = tl.data.txt;
	
	var toId = tl.to.id;
	var to = Model.nodes[toId];		
	
	var ob = getNextNodeXml(to);
	startData["transition_to"] = ob.name;
	
	xml =Mustache.render(startTemplate, startData); 
	xml = xml + getXmlOnce(ob);
	
	var temp = $("#allTemplate").html();  
	var allData = {};
	allData["name"] = workflowDetailData["workflowName"];
	allData["all"] = xml;
	return Mustache.render(temp, allData); 
}

var taskNodeTemplate = $("#taskNodeTemplate").html();
function generateActivityNode(node) {
	var data = {};
	var n = getSequence();
	
	data["activity"] = node.getAssignmentValue("activity"); 
	data["sequence"] = n;
	data["workflow_pm"] = workflowDetailData["workflowPM"];
	data["workflow_manager"] = workflowDetailData["workflowManager"];
	data["report_upload_check"] = node.getAssignmentValue("report_upload_check"); 
	
	data["roles"] = node.getAssignmentValue("roles"); 
	data["accepted_time"] = node.getAssignmentValue("accepted_time"); 
	data["completed_time"] = node.getAssignmentValue("completed_time"); 
	data["overdueToPM_time"] = node.getAssignmentValue("overdueToPM_time"); 
	data["overdueToUser_time"] = node.getAssignmentValue("overdueToUser_time"); 
	
	data["role_type"] = node.getAssignmentValue("role_type"); 
	data["rate_selection_criteria"] = node.getAssignmentValue("rate_selection_criteria"); 
	data["expense_rate_id"] = node.getAssignmentValue("expense_rate_id"); 
	data["revenue_rate_id"] = node.getAssignmentValue("revenue_rate_id"); 
	data["role_name"] = node.getAssignmentValue("role_name"); 
	
	data["action_type"] = node.getAssignmentValue("action_type"); 
	
	data["x"] = getTemplateNumber(node.locale.x);
	data["y"] = getTemplateNumber(node.locale.y);
	
	var tl = node.tos[0];
	data["transition"] = tl.data.txt;
	
	var toId = tl.to.id;
	var to = Model.nodes[toId];		
	var ob = getNextNodeXml(to);
	data["transition_to"] = ob.name;
	
	var xml = Mustache.render(taskNodeTemplate, data); 
	xml = xml + getXmlOnce(ob);
	
	var returnValue= {};
	returnValue["name"] = "node_" + n + "_" + node.getAssignmentValue("activity");
	returnValue["xml"] = xml;
	return returnValue;
}

var endTemplate = $("#endTemplate").html();
function generateEndNode(node) {
	var n = getSequence();
	
	endData["sequence2"] = n;
	endData["workflow_pm"] = workflowDetailData["workflowPM"];
	endData["workflow_manager"] = workflowDetailData["workflowManager"];

	
	endData["x2"] = getTemplateNumber(node.locale.x);
	endData["y2"] = getTemplateNumber(node.locale.y);
	endData["max_node_id"] = getSequence();
	var xml = Mustache.render(endTemplate, endData); 
	
	var returnValue= {};
	returnValue["name"] = "Exit";
	returnValue["xml"] = xml;
	return returnValue;
}

var decisionTemplate1 = $("#decisionTemplate1").html();
var decisionTemplate2 = $("#decisionTemplate2").html();
function generateConditionNode(node) {
	var n = getSequence();
	
	var transitionData = {};
	var data = {};
	var name = "node_" + n + "Condition Node";
	data["decision"] = name;
	data["x"] = getTemplateNumber(node.locale.x);
	data["y"] = getTemplateNumber(node.locale.y);
	data["sequence"] = n;
	
	var l1 = node.tos[0];
	var l2 = node.tos[1];
	
	if (l2 && l2.isDefault){
		var l3 = l2;
		l2 = l1;
		l1 = l3;
	}
	
	var toId = l1.to.id;
	var to = Model.nodes[toId];		
	var ob = getNextNodeXml(to);
	data["transition1"] = l1.data.txt;	
	data["transition_to1"] = ob.name;
	var temp = decisionTemplate1;	
	
	// sometimes the condition only has one output.
	var ob2;
	if (l2){		
		toId = l2.to.id;
		to = Model.nodes[toId];		
		ob2 = getNextNodeXml(to);
		data["transition2"] = l2.data.txt;	
		data["transition_to2"] = ob2.name;
		temp = decisionTemplate2;	
	}
	
	var xml = Mustache.render(temp, data); 
	xml = xml + getXmlOnce(ob);
	if (typeof(ob2) != "undefined"){
		xml = xml + getXmlOnce(ob2);
	}
	
	var returnValue= {};
	returnValue["name"] = name;
	returnValue["xml"] = xml;
	return returnValue;
}

function getXmlOnce(node) {
	if (typeof(node.added) == "undefined") {
		node.added = true;
		return node.xml;
	} else {		
		return " ";
	}
}

var generatedNode = {};
function getNextNodeXml(node) {
	
	var v = generatedNode[node.id];
	if (typeof(v) != "undefined") {
		return v;
	}
	
	if (node.type == "activityNode") {
		log("getNextNodeXml" + node.getAssignmentValue("activity"));
		v = generateActivityNode(node);
	} else if (node.type == "endNode") {
		log("getNextNodeXml end");
		v = generateEndNode(node);
	} else if (node.type == "conditionNode") {
		log("getNextNodeXml conditionNode");
		v = generateConditionNode(node);
	}
	
	generatedNode[node.id] = v;
	log("end");
	return v;
}