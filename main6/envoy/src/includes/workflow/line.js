var LineData = {
	lines : new Array(),
	line : null,
	lineDiv : null,
	selectedLine : null,
	tempSelectedLine : null,
	addLine : function(node, linePoint) {
		var l = new Line();
		l.id = Utils.newId();
		l.from.id = node.id;
		l.from.x = linePoint.x;
		l.from.y = linePoint.y;
		
		for(var i in node.tos){
			var tl = node.tos[i];
			if (tl.isDefault)
				l.isDefault = false;
		}

		node.tos.push(l);

		this.lines.push(l);
		this.line = l;
		var c = UI.canvasDiv;
		var div = $("<div id='" + l.id + "' class='line_box '><canvas class='shape_canvas'></canvas>" +
				"<canvas class='startPoint_canvas' width='0' height='0' style='position: absolute;'></canvas>" +
				"<canvas class='endPoint_canvas' width='0' height='0' style='position: absolute;'></canvas>" +
				"<input type='text' class='txt' id='txt" + l.id + "'/>" +
				"</div>").appendTo(c);
		
		var txt = div.find("#txt" + l.id);
		txt.focus(function() {
			l.data.txt = txt.val();
			$(this).addClass("focus");
		}).blur(function() {
			$(this).removeClass("focus");
			l.data.txt = txt.val();
			l.showTxt();
		});
		
		txt.bind('keypress', function(event) {
			if (event.keyCode == "13") {
				l.data.txt = txt.val();
				txt.blur();
			} 
		});

		return l;
	},
	selectTheLine : function(l){
		LineData.selectedLine = l;
		l.updateLocale();
	},
	deleteLine : function(l) {
		if (l.from && l.from.id != -1) {
			var node = Model.nodes[l.from.id];
			Utils.removeLine(node.tos, l);
		}
		
		if (l.to && l.to.id != -1) {
			var node = Model.nodes[l.to.id];
			Utils.removeLine(node.froms, l);
		}
		
		Utils.removeLine(LineData.lines, l);
		$("#" + l.id).remove();
	},
	deleteSelectedLine : function(line) {
		if (LineData.selectedLine) {
			var l = LineData.selectedLine;
			LineData.selectedLine = null;
			LineData.deleteLine(l);
 		}
	},
	addLineWithStartAndEnd : function(start, end, txt, isDefault) {
		
		for ( var i in start.froms) {
			var l = start.tos[i];
			
			if (l && l.from.id == start.id && l.to.id == end.id)
				return null;
		}
		
		if (typeof(isDefault) == "undefined")
			isDefault = true;
		
		var line = LineData.addLine(start, {x:1,y:1});
		line.isDefault = isDefault;
		line.from.id = start.id;
		line.to.id = end.id;
		line.updateTxt(txt);
		
		line.updateFrom();
		line.updateTo();
		line.updateFrom();
		line.updateLocale();
		
		end.froms.push(line);
		return line;
	},
	init : function() {

	},
	getLineByPoint : function(e) {
		var loc = Utils.toViewPoint(e);
		for ( var i in this.lines) {
			var l = this.lines[i];
			var r = getLength(l.from, l.to, loc);
			if (r < 5)
				return l;
		}

		return null;
	},
	endMoveLineEnd : function(e) {
		this.hideLinePoint();
		var l = this.line;
		
		// is new line
		var isNewLine = l.data.txt == "";
		if (isNewLine){
			l.updateTxt("Action" + getLineIndex());
		}
		
		var node = Model.getNodeByPoint(e);
		if (node != null && node.id != l.from.id) {
			// can not add line to start node.
			if (node.type == "startNode"){
				LineData.deleteLine(l);
				return;
			}				
			
			l.to.id = node.id;
			node.froms.push(l);
			l.updateTo(e);
		} else if (isNewLine){
			LineData.deleteLine(l);
		}
	},
	endMoveLineStart : function(e) {
		var l = this.line;
		var node = Model.getNodeByPoint(e);
		if (node != null && node.id != l.to.id) {
			l.from.id = node.id;
			node.tos.push(l);
			l.updateFrom(e);
		}
		this.hideLinePoint();
	},
	moveLineEnd : function(e) {
		var l = this.line;

		var p = this.showEndLinePoint(e);
		if (p == null) {
			var p = Utils.toViewPoint(e);
			l.to.x = p.x;
			l.to.y = p.y;
		} else {
			l.to.x = p.x;
			l.to.y = p.y;
		}

		l.updateFrom();
		l.updateLocale();
	},
	moveLineStart : function(e) {
		var l = this.line;

		var p = this.showStartLinePoint(e);
		if (p == null) {
			var p = Utils.toViewPoint(e);
			l.from.x = p.x;
			l.from.y = p.y;
		} else {
			l.from.x = p.x;
			l.from.y = p.y;
		}

		l.from.id = -1;
		l.updateTo();
		l.updateLocale();
	},
	hideLinePoint : function() {
		var div = UI.endLinePointDiv;
		if (div.length == 0) {
			return;
		}

		div.hide();
	},
	
	// return the found line point
	showEndLinePoint : function(e) {

		var div = UI.endLinePointDiv;
		if (div.length == 0) {
			var n = "<div id='endLinePointDiv' class='shape_box'><canvas class='shape_canvas' width='50' height='50'></canvas></div>";
			div = $(n);
			var i = div.appendTo(UI.viewport);
			var context = div.find(".shape_canvas")[0].getContext("2d");
			context.beginPath();
			context.strokeStyle = 'rgba(0, 0, 0, 0.3)';
			context.fillStyle = 'rgba(255, 0, 0, 0.3)';
			context.arc(14, 14, 13, 0, Math.PI * 2, false);
			context.arc(14, 14, 5, 0, Math.PI * 2, true);
			context.fill();
			
			UI.endLinePointDiv = div;
		}

		var node = Model.getNodeByPoint(e);
		if (node != null && node.id != this.line.from.id) {

			var p = node.getClosedPoint(this.line.from);
			div.css({
				left : p.x - 14 + "px",
				top : p.y - 14 + "px"
			});
			div.show();
		} else {
			div.hide();
		}

		return p;
	},
	// return the found line point
	showStartLinePoint : function(e) {

		var div = UI.endLinePointDiv;
		if (div.length == 0) {
			var n = "<div id='endLinePointDiv' class='shape_box'><canvas class='shape_canvas' width='50' height='50'></canvas></div>";
			div = $(n);
			var i = div.appendTo(UI.viewport);
			var context = div.find(".shape_canvas")[0].getContext("2d");
			context.beginPath();
			context.strokeStyle = 'rgba(0, 0, 0, 0.3)';
			context.fillStyle = 'rgba(255, 0, 0, 0.3)';
			context.arc(14, 14, 13, 0, Math.PI * 2, false);
			context.arc(14, 14, 5, 0, Math.PI * 2, true);
			context.fill();
			
			UI.endLinePointDiv = div;
		}

		var node = Model.getNodeByPoint(e);
		if (node != null && node.id != this.line.to.id) {

			var p = node.getClosedPoint(this.line.to);
			div.css({
				left : p.x - 14 + "px",
				top : p.y - 14 + "px"
			});
			div.show();
		} else {
			div.hide();
		}

		return p;
	}
}

function Line() {
	this.from = {
		id : -1,
		x : 0,
		y : 0
	};

	this.to = {
		id : -1,
		x : 0,
		y : 0
	};
	this.data = {
			txt : ""
	};
	this.isDefault = true;
	this.mixH = 20;
	this.mixW = 80;
	this.getDiv = function() {
		if (typeof(this.rootDiv) == "undefined" || this.rootDiv.length == 0){
			this.rootDiv = $("#" + this.id);
		}
		
		return this.rootDiv;
	};	
	this.getRect = function(x1, y1, x2, y2) {
		var rect = {};

		if (x1 < x2) {
			rect.x = x1;
		} else {
			rect.x = x2;
		}

		if (y1 < y2) {
			rect.y = y1;
		} else {
			rect.y = y2;
		}

		rect.h = this.mixH;
		rect.w = this.mixW;

		var w1 = Math.abs(x1 - x2);
		if (w1 > rect.w)
			rect.w = w1;

		var h1 = Math.abs(y1 - y2);
		if (h1 > rect.h)
			rect.h = h1;

		rect.x = rect.x - 30;
		rect.y = rect.y - 30;
		rect.w = rect.w + 60;
		rect.h = rect.h + 60;

		return rect;
	};
	this.updateFrom = function(e) {
		if (this.from.id != -1) {
			var node = Model.nodes[this.from.id];
			var p = node.getClosedPoint(this.to);
			this.from.x = p.x;
			this.from.y = p.y;
		} else if (typeof (e) != "undefined") {
			var p = Utils.toViewPoint(e);
			this.from.x = p.x;
			this.from.y = p.y;
		}
	};
	this.updateTo = function(e) {
		if (this.to.id != -1) {
			var node = Model.nodes[this.to.id];
			var p = node.getClosedPoint(this.from);
			this.to.x = p.x;
			this.to.y = p.y;
		} else if (typeof (e) != "undefined") {
			var p = Utils.toViewPoint(e);
			this.to.x = p.x;
			this.to.y = p.y;
		}
	};
	this.getDiv = function() {
		if (typeof(this.rootDiv) == "undefined" || this.rootDiv.length == 0){
			this.rootDiv = $("#" + this.id);
		}
		
		return this.rootDiv;
	};
	this.updateTxt = function(txt){
		this.data.txt = txt;
		var txtDiv = this.getDiv().find("#txt" + this.id);
		txtDiv.val(txt);
		this.showTxt();
	};
	this.showTxt = function() {
		var s = this.data.txt;
		var div = this.getDiv();
		var txtDiv = div.find(".txt");
		txtDiv.val(this.data.txt);
		
		var h = Math.abs(this.from.y - this.to.y);
		var w = Math.abs(this.from.x - this.to.x);
		
		var context = UI.canvas[0].getContext("2d");
		var w1 = context.measureText(s).width;
		w1 = w1 *1.2;
		if (w1 < 30)
			w1 = 30;
		
		var rect = this.getRect(this.from.x, this.from.y, this.to.x, this.to.y);
		var p1 = {
				x : this.from.x - rect.x,
				y : this.from.y - rect.y
			};
		var p2 = {
				x : this.to.x - rect.x,
				y : this.to.y - rect.y
			}
		
		var x1 = p1.x;
		if (p2.x < x1)
			x1 = p2.x;
		
		var y1 = p1.y;
		if (p2.y < y1)
			y1 = p2.y;
		
		txtDiv.css({
			left : x1 + (w - w1)/2 + "px",
			top : y1 + (h - 18)/2 + "px",
			width : w1 + "px"
		});
		
	}
	this.drawForNav = function(context, scale) {
		context.save();
		context.lineJoin = "round";
		context.lineWidth = 1 * scale;

		var p1 = {
			x : this.from.x * scale,
			y : this.from.y * scale
		};

		var p2 = {
			x : this.to.x * scale,
			y : this.to.y * scale
		}
		
		context.beginPath();
		context.moveTo(p1.x, p1.y);
		context.lineTo(p2.x, p2.y);
		context.stroke();
		context.restore();
	}
	this.drawForPrint = function(context) {
		context.lineJoin = "round";
		context.lineWidth = 1;

		var p1 = {
			x : this.from.x,
			y : this.from.y
		};

		var p2 = {
			x : this.to.x,
			y : this.to.y
		}
		
		context.beginPath();
		context.moveTo(p1.x, p1.y);
		context.lineTo(p2.x, p2.y);
		context.stroke();
		context.restore();
		this.drawArrow(context, p1, p2);
		
		var h = this.from.y - this.to.y;
		var w = this.from.x - this.to.x;
		var x = this.from.x + w/2;
		var y = this.from.y + (h - 22)/2;
		
		var s = this.data.txt;
		var w1 = context.measureText(s).width;
		w1 = w1 *1.1;
		if (w1 < 30)
			w1 = 30;
		
		context.fillText(s, x - w1/2, y );
	}
	this.updateLocale = function() {
		var rect = this.getRect(this.from.x, this.from.y, this.to.x, this.to.y);
		var div = this.getDiv();
		var canvas = div.find(".shape_canvas");
		div.css({
			left : rect.x + "px",
			top : rect.y + "px"
		});

		canvas.attr({
			width : rect.w,
			height : rect.h
		});

		var context = canvas[0].getContext("2d");
		context.clearRect(0, 0, rect.w, rect.h);

		var p1 = {
			x : this.from.x - rect.x,
			y : this.from.y - rect.y
		};

		var p2 = {
			x : this.to.x - rect.x,
			y : this.to.y - rect.y
		}

		context.save();
		
		if (this.isDefault) {
			context.strokeStyle = "rgba(0, 0,255, 1)";
			context.fillStyle = "rgba(0, 0,255, 1)";
		} else {
			context.strokeStyle = "rgb(0,0,0)";
			context.fillStyle = "rgb(50,50,50)";
		}
		
		if (this.isSelected()){
			context.shadowBlur = 4;
			context.shadowColor = "#833";
			context.strokeStyle = "rgba(188, 0,0, 1)";
		} 
		
		context.lineJoin = "round";
		context.lineWidth = 2;

		context.beginPath();
		context.moveTo(p1.x, p1.y);
		context.lineTo(p2.x, p2.y);
		context.stroke();
		context.restore();
		this.drawArrow(context, p1, p2);

		div.show();
		
		var txt = div.find("#txt" + this.id);
		this.data.txt = txt.val();			

		this.showTxt();
	};
	
	this.isOnEndPoint = function(e) {
		var loc = Utils.toViewPoint(e);		
		return getDistance(loc, this.to) < 150;
	};
	this.isOnStartPoint = function(e) {
		var loc = Utils.toViewPoint(e);		
		return getDistance(loc, this.from) < 150;
	};
	this.isSelected = function() {
		if (LineData.selectedLine && LineData.selectedLine.id == this.id){
			return true;
		} else if(LineData.tempSelectedLine && LineData.tempSelectedLine.id == this.id){
			return true;
		}
		
		return false;
	}
	this.drawArrow = function(context, p1, p2) {

		if (this.isDefault) {
			context.strokeStyle = "rgba(0, 0,188, 1)";
			context.fillStyle = "rgba(0, 0,188, 1)";
		} else {
			context.strokeStyle = "rgb(50,50,50)";
			context.fillStyle = "rgb(50,50,50)";
		}
		
		if (this.isSelected()){
			context.strokeStyle = "rgba(188, 0,0, 1)";
			context.fillStyle = "rgba(188, 0,0, 1)";
		} 
		
		var M = Utils.getAngle(p1, p2);
		
		var S = 12;
		var ac = Math.PI / 10;
		var V = S / Math.cos(ac);
		var L = p2.x - V * Math.cos(M - ac);
		var K = p2.y - V * Math.sin(M - ac);
		var O = p2.x - V * Math.sin(Math.PI / 2 - M - ac);
		var N = p2.y - V * Math.cos(Math.PI / 2 - M - ac);
		context.beginPath();
		context.moveTo(p2.x, p2.y);
		context.lineTo(L, K);
		context.lineTo(O, N);
		context.lineTo(p2.x, p2.y);
		context.closePath();
		
		
		context.fill();
		context.stroke();
	}
}

var lineIndex = 0;
function getLineIndex(){
	lineIndex++;
	return lineIndex;
}