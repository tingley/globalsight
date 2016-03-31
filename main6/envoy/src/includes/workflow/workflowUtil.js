
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
		Model.selectTheNode(startNode);
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
		Model.selectTheNode(endNode);
		return false;
	}
	
	tl = endNode.froms[0];
	if (typeof(tl) == "undefined"){
		alert(msg_end_no_income);
		Model.selectTheNode(endNode);
		return false;
	}
	
	for ( var i in LineData.lines) {
		var l = LineData.lines[i];
		
		if (!l.from || l.from.id == -1 || !l.to || l.to.id == -1) {
			alert(msg_line_no_node);
			LineData.selectTheLine(l);
			return false;
		}
	}
	
	// for other
	for (var b in Model.nodes){
		var node = Model.nodes[b];
		if (node.type != "endNode" && node.type != "startNode") {
			tl = node.froms[0];
			if (typeof(tl) == "undefined"){
				alert(msg_no_income);
				Model.selectTheNode(node);
				return false;
			}			
			
			tl = node.tos[0];
			if (typeof(tl) == "undefined"){
				alert(msg_no_out);
				Model.selectTheNode(node);
				return false;
			}
			
			if (node.type == "activityNode"){
				var activity = node.getAssignmentValue("activity"); 
				if (activity == "") {
					alert(msg_no_activity);
					Model.selectTheNode(node);
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
var ids = "";
function getWorkflowXml() {
	startData = {};
	endData = {};
	generatedNode = {};
	generatingNode = {};
	sequence = 0;
	ids = "";
	
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
	
	generatingNode[node.id] = "Start";
	
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
	ids += n +　"_" + node.id + ",";
	
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
	data["role_preference"] = node.getAssignmentValue("role_preference"); 
	
	data["x"] = getTemplateNumber(node.locale.x);
	data["y"] = getTemplateNumber(node.locale.y);
	
	var name = "node_" + n + "_" + node.getAssignmentValue("activity");
	generatingNode[node.id] = name;
	
	var tl = node.tos[0];
	data["transition"] = tl.data.txt;
	
	var toId = tl.to.id;
	var to = Model.nodes[toId];		
	var ob = getNextNodeXml(to);
	data["transition_to"] = ob.name;
	
	var xml = Mustache.render(taskNodeTemplate, data); 
	xml = xml + getXmlOnce(ob);
	
	var returnValue= {};
	returnValue["name"] = name
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

function getDefaultNode(node){
	var l = node.tos[0];
	
	for ( var i in node.tos) {
		var line = node.tos[i];		
		if (line.isDefault){
			l = line;
			break;
		}			
	}
	
	return Model.nodes[l.to.id];
}

function getCompletedNode(node){
	for ( var i in node.tos) {
		var line = node.tos[i];
		var toId = line.to.id;
		var to = Model.nodes[toId];
		
		if (5 == to.state || 3 == to.state){
			return to;
		}
	}
	
	return null;
}

var decisionTemplate = $("#decisionTemplate").html();
var branchTemplate = $("#branchTemplate").html();
var transitionTemplate = $("#transitionTemplate").html();
function generateConditionNode(node) {
	var n = getSequence();
	ids += n +　"_" + node.id + ",";
	
	var transitionData = {};
	var data = {};
	var name = "node_" + n + "Condition Node";
	generatingNode[node.id] = name;
	
	data["decision"] = name;
	data["x"] = getTemplateNumber(node.locale.x);
	data["y"] = getTemplateNumber(node.locale.y);
	data["sequence"] = n;
	
	// update the default path
	var dn = getDefaultNode(node);
	if (5 != dn.state && 3 != dn.state){
		var cn = getCompletedNode(node);
		if (cn != null){
			for ( var i in node.tos) {
				var line = node.tos[i];
				line.isDefault = line.to.id == cn.id;
			}
		}
	}
	
	var branchs = "";
	var transitions = "";
	var nextXmls = "";
	for ( var i in node.tos) {
		var line = node.tos[i];
		var toId = line.to.id;
		var to = Model.nodes[toId];		
		var ob = getNextNodeXml(to);
		
		var data2 = {};
		data2["i"] = i;
		data2["isDefault"] = line.isDefault;
		data2["transition"] = line.data.txt;
		var branchXml = Mustache.render(branchTemplate, data2); 
		
		data2["transition_to"] = ob.name;
		var transitionXMl = Mustache.render(transitionTemplate, data2); 
		
		branchs = branchs + branchXml;
		transitions = transitions + transitionXMl;
		nextXmls = nextXmls + getXmlOnce(ob);
	}
	
	data["branchs"] = branchs;
	data["transitions"] = transitions;
	var xml = Mustache.render(decisionTemplate, data); 
	xml = xml + nextXmls;
	
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
var generatingNode = {};

function getNextNodeXml(node) {
	
	var v = generatedNode[node.id];
	if (typeof(v) != "undefined") {
		return v;
	} 
	
	v = generatingNode[node.id];
	if (typeof(v) != "undefined") {
		return {
			name : v,
			xml : " "
		};
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