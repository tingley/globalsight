var UI = {
	isDrag : false,
	init : false,
	editable : true,
    button : {
    	value : 4,
    	activity : 0,
    	end : 1,
    	condition : 2,
    	line : 3,
    	point : 4
    },
	initUI : function() {
		this.intArg();

		UI.canvas.attr({
			width : UI.canvasDiv.width(),
			height : UI.canvasDiv.height()
		});

		UI.snapLineCanvas.attr({
			width : UI.canvasDiv.width(),
			height : UI.canvasDiv.height()
		});

		this.addMethod();
		this.showGrid();

		LineData.init();
		Menu.init();
		Toolbar.init();
		this.initBodyMethod();
		Dialog.init();
		Navigation.init();
	},
	
	updateCursorForPoint : function(e){
		var selectedLine = LineData.tempSelectedLine;
		LineData.tempSelectedLine = null;
		if (selectedLine != null){
			selectedLine.updateLocale();
		}
		
		var l = LineData.getLineByPoint(e);
		if (l != null) {
			LineData.tempSelectedLine = l;					
			if (l.isOnEndPoint(e) || l.isOnStartPoint(e)) {
				UI.bodyDiv.css("cursor", "move");
			} else {
				UI.bodyDiv.css("cursor", "pointer");
			} 
		} 
		else {
			var node = Model.getNodeByPoint(e);
			if (node != null && node.isMoveRect(e)){
				UI.bodyDiv.css("cursor", "move");
			} else {
				UI.bodyDiv.css("cursor", "default");
			}
		}
	},
	mouseX : 0,
	mouseY : 0,
	initBodyMethod : function() {
		UI.body.bind("mousedown", function(e) {
			
			if (Menu.showProperty)
				return;
			
			if (1 == e.which) {
				onLeftDown(e);
			}
		});

		UI.body.bind("mousemove.updateCursor", function(e) {
			UI.mouseX = e.clientX; 
			UI.mouseY = e.clientY;
			
			if (UI.isDrag)
				return;

			if (UI.button.value == UI.button.point) {
				UI.updateCursorForPoint(e);
			} else {
				var node = Model.getNodeByPoint(e);
				if (node != null && node.type != "endNode"){
					UI.bodyDiv.css("cursor", "crosshair");
				} else {
					UI.bodyDiv.css("cursor", "default");
				}
			}
		});

		UI.body.bind("mouseup.create", function(e) {
			if (UI.button.value == UI.button.point) {
				UI.bodyDiv.unbind("mousemove.creating");
				Model.hideSnapLine();
				Navigation.draw();
			}
		});
	},
	intArg : function() {
		UI.body = $("body");
		UI.canvas = $("#canvas");
		UI.viewport = $("#viewport");
		UI.shapePanel = $("#shapePanel");
		UI.canvasDiv = $("#canvasDiv");
		UI.snapLineCanvas = $("#snapLineCanvas");
		UI.bodyDiv = $("#bodyDiv");
		UI.endLinePointDiv = $("#endLinePointDiv");
		UI.node_menu = $("#node_menu");
		UI.condition_menu = $("#condition_menu");
		UI.propertiesDiv = $("#propertiesDiv");
		UI.toolbar = $("#toolbar");
		UI.shapeCanvas =  $("#shapeCanvas");
		UI.creatingCanvas = $("#creatingCanvas");
		UI.shapeCanvasDiv = $("#shapeCanvasDiv");
		UI.creatingDiv = $("#creatingDiv");
		UI.dialogUserTable = $("#dialogUserTable");
		UI.dialogUserSelect = $("#dialogUserSelect");
		UI.selectAllUserCheckbox = $("#selectAllUserCheckbox");
		UI.navDiv = $("#navDiv");
		UI.navButton = $("#navButton");
		UI.navCanvas = $("#navCanvas");
		UI.navRectDiv = $("#navRectDiv");
		UI.hover_tip = $("#hover_tip");
		UI.printCanvas = $("#printCanvas");
		UI.printImg = $("#printImg");
	},
	addMethod : function() {
		var body = UI.body;
		body.mousedown(function() {
			UI.isDrag = true;
		});

		body.mouseup(function() {
			UI.isDrag = false;
		});
	},
	showGrid : function() {
		var context = UI.canvas[0].getContext("2d");
		context.clearRect(0, 0, UI.canvasDiv.width(), UI.canvasDiv.height());
		context.translate(0, 0);
		context.lineWidth = 1;
		context.save();
		var i = 0.5;
		var g = 0;
		var d = 15;
		var color1 = "rgb(229,229,229)";
		var color2 = "rgb(242,242,242)";
		var m = UI.canvas.height();
		var c = UI.canvas.width();
		while (i <= m) {
			context.restore();
			if (g % 4 == 0) {
				context.strokeStyle = color1;
			} else {
				context.strokeStyle = color2;
			}
			context.beginPath();
			context.moveTo(0, i);
			context.lineTo(c, i);
			i += d;
			g++;
			context.stroke()
		}
		i = 0.5;
		g = 0;
		while (i <= c) {
			context.restore();
			if (g % 4 == 0) {
				context.strokeStyle = color1;
			} else {
				context.strokeStyle = color2;
			}
			context.beginPath();
			context.moveTo(i, 0);
			context.lineTo(i, m);
			i += d;
			g++;
			context.stroke();
		}
	}
}

function onLeftDownForPoint(e) {
	var selectedLine = LineData.selectedLine;
	LineData.selectedLine = null;
	if (selectedLine != null){
		selectedLine.updateLocale();
	}
	
	var selectedNode = Model.selectedNode;
	Model.selectedNode = null;
	if (selectedNode != null){
		selectedNode.hidePoints();
	}
	
	var l = LineData.getLineByPoint(e);
	if (l != null) {
		LineData.selectTheLine(l);
		
		if (l.isOnEndPoint(e)) {
			e.preventDefault();

			UI.bodyDiv.bind("mousemove.moveLine", function(e) {
				LineData.line = l;
				LineData.moveLineEnd(e);
			});

			UI.body.bind("mouseup.endLine", function(e) {
				LineData.endMoveLineEnd(e);
				UI.body.unbind("mouseup.endLine");
				UI.bodyDiv.unbind("mousemove.moveLine");
			});
		} else if (l.isOnStartPoint(e)) {
			e.preventDefault();

			UI.bodyDiv.bind("mousemove.moveLine", function(e) {
				LineData.line = l;
				LineData.moveLineStart(e);
			});

			UI.body.bind("mouseup.endLine", function(e) {
				LineData.endMoveLineStart(e);
				UI.body.unbind("mouseup.endLine");
				UI.bodyDiv.unbind("mousemove.moveLine");
			});
		}
	} else {
		var node = Model.getNodeByPoint(e);
		if (node != null) {
			Model.selectTheNode(node);
			
			if (node.isMoveRect(e)) {
				e.preventDefault();

				UI.bodyDiv.unbind("mousemove.creating").bind("mousemove.creating", function(e) {
					Model.moveNode(node, e);
				});
			}
		}
		
	}
}

function onLeftDownForLine(e) {
	var node = Model.getNodeByPoint(e);
	if (node != null) {
		
		var type = node.type;
		if (type == "endNode") 
			return;
		
		if ((type == "activityNode" || type == "startNode") && node.tos.length > 0){
			alert("You cannot have two outgoing arrows\n from the start and activity nodes.");
			return;
		}
		
		var linePoint = node.getClosedPoint(e);
		if (linePoint != null) {// add line
			e.preventDefault();

			var line = LineData.addLine(node, linePoint);

			UI.bodyDiv.unbind("mousemove.line").bind("mousemove.line", function(e) {
				LineData.moveLineEnd(e);
			});

			UI.bodyDiv.unbind("mouseup.line").bind("mouseup.line", function(e) {
				UI.bodyDiv.unbind("mousemove.line");
				UI.bodyDiv.unbind("mouseup.line");
				LineData.endMoveLineEnd(e);
			});
	    }
	}
}

function onLeftDown(e) {
	
	if (UI.button.value == UI.button.point) {
		onLeftDownForPoint(e);
	} else if (UI.button.value == UI.button.line) {
		onLeftDownForLine(e);
	} else if (UI.button.value == UI.button.end) {
		
	}
}