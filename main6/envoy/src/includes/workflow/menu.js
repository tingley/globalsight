var Menu = {
	showProperty : false,
	init : function() {
		UI.body.bind("click.menu", function(e) {
			UI.node_menu.hide();
			UI.condition_menu.hide();
		});

		UI.viewport.unbind("contextmenu").bind("contextmenu", function(e) {
			e.preventDefault();
			onRightDown(e);
		});


		$(document).unbind("keydown.hotkey").bind("keydown.hotkey", function(e) {
			if (Menu.showProperty)
				return;

			if (e.keyCode == 32) {
				var node = Model.selectedNode;
				if (node != null) {
					e.preventDefault();
					showProperties(node);
				} else {
					var line = LineData.selectedLine;
					if (line != null && isFromConditionNode(line)){
						e.preventDefault();
						setLineToDefault(line);
					}
				}
			} else if (e.keyCode == 46) {
				log("delete");
				
				var l = LineData.selectedLine;
				log(1 + l);
				if (l){
					LineData.deleteSelectedLine();
				} else {
					var node = Model.selectedNode;
					log(2 + node);
					if (node) {
						Model.deleteSelectedNode();
					}
				}
			}
		});

		$("#edit_ok").click(function(e) {
			hideProperties();
		});

	}
}

function setLineToDefault(line) {
	var n = Model.nodes[line.from.id];
	
	var sl = LineData.selectedLine;
	if (sl && sl.id == line.id)
		LineData.selectedLine = null;
	
	for (var i in n.tos){
		var l = n.tos[i];
		l.isDefault = l.id == line.id;
		l.updateLocale();
	}
}

function isFromConditionNode(line) {
	if (line != null && line.from && line.from.id != -1) {
		var n = Model.nodes[line.from.id];
		if (n.type == "conditionNode"){
			return true;
		}
	}
	
	return false;
}

function onRightDown(e) {
	if (UI.button.value != UI.button.point) {
		return;
	}
	
	var node = Model.getNodeByPoint(e);
	if (node != null && node.type != "startNode") {
		e.preventDefault();

		UI.node_menu.show();

		UI.node_menu.css({
			display : "block",
			"z-index" : 800,
			left : e.pageX,
			top : e.pageY
		});

		if (node.type == "activityNode"){
			var li = UI.node_menu.find("li:eq(0)");
			li.show();
			li.unbind("click").bind("click", function() {
				showProperties(node);
			});
		} else {
			UI.node_menu.find("li:eq(0)").hide();
		}
		
		
		UI.node_menu.find("li:eq(1)").unbind("click").bind("click", function() {
			Model.deleteNode(node);
		});
	} else {
		var line = LineData.getLineByPoint(e);
		if (line != null) {
			e.preventDefault();
			UI.condition_menu.show();

			UI.condition_menu.css({
				display : "block",
				"z-index" : 800,
				left : UI.mouseX,
				top : UI.mouseY
			});

			
			if (isFromConditionNode(line)){
				var li = UI.condition_menu.find("li:eq(0)");
				li.show();
				li.unbind("click").bind("click", function() {
					setLineToDefault(line);
				});
			}
			else{
				UI.condition_menu.find("li:eq(0)").hide();
			}
			
			UI.condition_menu.find("li:eq(1)").unbind("click").bind("click", function() {
				LineData.deleteLine(line);
			});
		}
		
	}
}