var Model = {
	nodes : {},
	selectedNode : null,
	add : function(oldNode, e) {
		var node = Utils.copy(oldNode);
		
		if (node.id == -1) {
			node.id = Utils.newId();
		}

		this.nodes[node.id] = node;
		var modelDiv = node.getDiv();
		var pointsDiv = node.getPointDiv();
		if (modelDiv.length == 0) {
			var c = UI.canvasDiv;
			modelDiv = $("<div id='" + node.id + "' class='shape_box'><canvas class='shape_canvas'></canvas><canvas class='shape_canvas'></canvas></div>").appendTo(c);
			
			// for show rect
			var rectDiv = $("<div id='rect" + node.id + "' class='shape_box rectDiv' style='display: none;'><canvas class='shape_canvas'></canvas></div>").appendTo(modelDiv);
			var rectContext = rectDiv.find(".shape_canvas")[0].getContext("2d");
			node.drawRect(rectContext);
			
		    // for txt
			if (node.type == "activityNode") {
				$("<div class='shape_box' style='padding:7px;'>" +
						"<div class='txtDiv' id='name" + node.id + "'></div>" +
						"<div class='txtDiv' style='margin-top:5px;' id='user" + node.id + "'></div>" +
						"</div>").appendTo(modelDiv);
			}

			// for show point
			pointsDiv = $("<div id='point" + node.id + "' class='shape_box pointDiv' style='display: none;'><canvas class='shape_canvas'></canvas></div>").appendTo(modelDiv)
			var context = pointsDiv.find(".shape_canvas")[0].getContext("2d");

			var offset = node.getOffset();

			// update size
			$(modelDiv.find(".shape_canvas")).attr({
				width : node.prop.x * 2 + 15,
				height : node.prop.y * 2 + 15
			});
			
			$(pointsDiv.find(".shape_canvas")).attr({
				width : node.prop.x * 2 + 15,
				height : node.prop.y * 2 + 15
			});
			
			$(rectDiv.find(".shape_canvas")).attr({
				width : node.prop.x * 2 + 15,
				height : node.prop.y * 2 + 15
			});


			node.drawPoints(context);
			// pointsDiv.hide();
		}

		var modelCanvas = modelDiv.find(".shape_canvas");
		var context = modelCanvas[0].getContext("2d");

		node.drawShape(context);
		node.showTxt();
		modelDiv.show();

		this.addMethod(modelDiv, node);		
		Utils.optimizeAndMoveToViewport(node, modelDiv, e);
		node.hidePoints();
		
		Model.nodeChanged();
		
		return node;
	},
	nodeChanged : function() {
		Navigation.draw();
	},
	deleteSelectedNode : function() {
		if (Model.selectedNode) {
			var node = Model.selectedNode;
			
			// can not delete start node
			if (node.type == "startNode"){
				return;
			}
			
			for (var i in node.froms) {
				var l = node.froms[i];
				l.to.id = -1;
			}
			for (var i in node.tos) {
				var l = node.tos[i];
				l.from.id = -1;
			}
			$("#" + node.id).remove();
			
			Utils.removeNode(Model.nodes, node);
			Model.nodeChanged();
 		}
	},
	selectTheNode : function (node) {
		Model.selectedNode = node;
		node.showPoints();
	},
	deleteNode : function(node) {
		for (var i in node.froms) {
			var l = node.froms[i];
			l.to.id = -1;
		}
		for (var i in node.tos) {
			var l = node.tos[i];
			l.from.id = -1;
		}
		$("#" + node.id).remove();
		
		Utils.removeNode(Model.nodes, node);
		Model.nodeChanged();
	},
	moveNode : function(node, e) {
		Utils.optimizeAndMoveToViewport(node, node.getDiv(), e);
		Model.showSnapLine(e, node);

		// update line
		for ( var i in node.froms) {
			var line = node.froms[i];
			line.updateFrom();
			line.updateTo(e);
			line.updateLocale();
		}

		for ( var i in node.tos) {
			var line = node.tos[i];
			line.updateFrom(e);
			line.updateTo();
			line.updateLocale();
		}

	},
	addMethod : function(div, node) {
		div.bind("mouseover.move", function(e) {
			e.preventDefault();			
			node.showPoints();
		});
		div.bind("mouseout", function(e) {
			e.preventDefault();	
			if (Model.selectedNode && Model.selectedNode.id == this.id){
				node.showPoints();
			} else {
				node.hidePoints();
			}
		});
	},
	getOptimizedLocale : function(x, y, node) {
		var root = UI.viewport;
		var loc = Utils.windowToCanvas(root, x, y);
		x = loc.x;
		y = loc.y;

		for ( var b in Model.nodes) {
			j = Model.nodes[b];
			if (j.id != node.id) {
				var x1 = j.getSnapLineX(x, y)
				if (x1 > 0) {
					x = x1;
					break;
				}
			}
		}

		for ( var b in Model.nodes) {
			j = Model.nodes[b];
			if (j.id != node.id) {
				var y1 = j.getSnapLineY(x, y)
				if (y1 > 0) {
					y = y1;
					break;
				}
			}
		}

		return {
			x : x,
			y : y,
			clientX : x,
			clientY : y
		}
	},
	getNodeByPoint : function(e) {
		for ( var i in Model.nodes) {
			var node = Model.nodes[i];
			
			var div = node.getRectDiv();
			var context = div.find(".shape_canvas")[0].getContext("2d");
			var p = Utils.windowToCanvas(node.getDiv(), e.clientX, e.clientY);
			
			if (typeof (node.isDrawRect) == "undefined") {
				node.drawRect(context);
				node.isDrawRect = true;
			}
			
			if ( context.isPointInPath(p.x,p.y)) {
				return node;
			}
		}
		
		return null;
	},
	getNodeByName : function(name) {
		for ( var i in Model.nodes) {
			var node = Model.nodes[i];
			
			if (node.data.name == name)
				return node;
		}
		
		return null;
	},
	hideSnapLine : function() {
		var context = UI.snapLineCanvas[0].getContext("2d");
		context.clearRect(0, 0, UI.canvasDiv.width(), UI.canvasDiv.height());
	},
	showSnapLine : function(e, node) {
		var loc = Utils.toViewPoint(e);
		var x = loc.x;
		var y = loc.y;
		var showX = false;
		for ( var b in Model.nodes) {
			j = Model.nodes[b];
			if (j.id != node.id) {
				var y1 = j.getSnapLineY(x, y)
				if (y1 > 0) {
					showX = true;
					y = y1;
					break;
				}
			}
		}

		var showY = false;
		for ( var b in Model.nodes) {
			j = Model.nodes[b];
			if (j.id != node.id) {
				var x1 = j.getSnapLineX(x, y)
				if (x1 > 0) {
					showY = true;
					x = x1;
					break;
				}
			}
		}

		var context = UI.snapLineCanvas[0].getContext("2d");
		context.clearRect(0, 0, UI.canvasDiv.width(), UI.canvasDiv.height())

		if (showX) {
			context.save();
			context.strokeStyle = 'rgba(100, 140, 230, 1)';
			context.beginPath();
			context.moveTo(0, y);
			context.lineTo(UI.canvasDiv.width(), y);
			context.stroke();
			context.restore();
		}

		if (showY) {
			context.save();
			context.strokeStyle = 'rgba(100, 140, 230, 1)';
			context.beginPath();
			context.moveTo(x, 0);
			context.lineTo(x, UI.canvasDiv.height());
			context.stroke();
			context.restore();
		}
	}
}