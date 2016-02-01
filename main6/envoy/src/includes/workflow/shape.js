var Data = {
	shapes : {},
	init : function() {
		this.add(new EndNode());
		this.add(new ActivityNode());
		this.add(new ConditionNode());
		this.add(new Line());
	},
	add : function(node) {
		this.shapes[node.type] = node;
	}
}

// There are two coordinates.
// 1. based on the #viewport.
// 2. based on the small canvas.
// Only the locale is based on the #viewport.
function Node() {
	this.id = -1;
	this.name = "";
	this.defaultWidth = 100;
	this.scale = 1;

	// The center point based on #viewport.
	this.locale = {
		x : 150,
		y : 150
	}
	
	this.data = {
			
	}
	
	this.froms = new Array();
	this.tos = new Array();

	this.init = function(x,y, w) {
		
		this.locale.x = x;
		this.locale.y = y;
		
		if (typeof (w) == "undefined") {
			w = this.defaultWidth;
		}
		this.w = w;
		this.scale = w / this.prop.size;

		var w2 = this.prop.size * this.scale / 2;
		var h = this.prop.h * this.scale / 2;

		this.prop.x = w2;
		this.prop.y = h;
	};
	
	// The center point
	this.getOffset = function(w) {
		if (typeof (w) == "undefined") {
			w = this.defaultWidth;
		}

		var scale = w / this.prop.size;
		var x = this.prop.w * scale / 2;
		var y = this.prop.h * scale / 2;
		var offset = {
			x : x,
			y : y
		}
		return offset;
	};

	this.getDiv = function() {
		if (typeof(this.rootDiv) == "undefined" || this.rootDiv.length == 0){
			this.rootDiv = $("#" + this.id);
		}
		
		return this.rootDiv;
	};
	this.getPointDiv = function() {
		if (typeof(this.pointDiv) == "undefined" || this.pointDiv.length == 0){
			this.pointDiv = $("#point" + this.id);
		}
		
		return this.pointDiv;
	};
	this.getRectDiv = function() {
		if (typeof(this.rectDiv) == "undefined" || this.rectDiv.length == 0){
			this.rectDiv = $("#rect" + this.id);
		}
		
		return this.rectDiv;
	};
	this.addShadow = function(context, scale) {
		context.shadowColor = 'rgba(0, 0, 0, 0.7)';
		context.shadowOffsetX = 5 * scale;
		context.shadowOffsetY = 5 * scale;
		context.shadowBlur = 6;
	};
	this.addFillShadow = function(context, scale) {
		context.shadowColor = 'rgba(0, 0, 0, 0.2)';
		context.shadowOffsetX = 5 * scale;
		context.shadowOffsetY = 5 * scale;
		context.shadowBlur = 6;
	};
	
	// Gets all line points. The line point is used to add line.
	// The method should be rewrite.
	this.getPoints = function() {
	};

	// Returns the line point if the locale(e) is close to it or return null.
	this.getLinePoint = function(e) {
		var root = this.getDiv();
		var loc = Utils.windowToCanvas(root, e.clientX, e.clientY);

		var ps = this.getPoints();
		for ( var i in ps) {
			var p = ps[i];

			var dx = loc.x - p.x;
			var dy = loc.y - p.y
			var d = dx * dx + dy * dy;

			if (d < 100) {
				var a = root.offset();
				var result = {
					x : a.left + p.x,
					y : a.top + p.y
				};
				return Utils.toViewPoint(result);
			}
		}

		return null;
	};
	this.isMoveRect = function(e) {
		var p = Utils.toViewPoint(e);

		var d2 = p.x - this.locale.x;
		var h2 = p.y - this.locale.y;

		var d = Math.sqrt(d2 * d2 + h2 * h2);
		return d < 40;
	};

	this.getSnapLineY = function(x, y) {

		if (Math.abs(y - this.locale.y) < 10)
			return this.locale.y;

		var w = this.prop.w * this.scale / 2;
		var h = this.prop.h * this.scale / 2;

		var y1 = this.locale.y - h;
		var y2 = this.locale.y + h;

		if (Math.abs(y - y1) < 10)
			return y1;

		if (Math.abs(y - y2) < 10)
			return y2;

		return -1;
	};

	this.getSnapLineX = function(x, y) {
		if (Math.abs(x - this.locale.x) < 10)
			return this.locale.x;

		var w = this.prop.w * this.scale / 2;
		var h = this.prop.h * this.scale / 2;

		var x1 = this.locale.x - w;
		var x2 = this.locale.x + w;

		if (Math.abs(x - x1) < 10)
			return x1;

		if (Math.abs(x - x2) < 10)
			return x2;

		return -1;
	};

	// the x, y is used in print.
	this.showTxt = function() {
		if (this.type == "activityNode"){
			var name = this.getAssignmentValue("role_name");
			var user = this.getDisplayActivity();
			$('#name' + this.id).html(name);
			$('#user' + this.id).html(user);			
		}	
	};
	this.showLocale = function(context) {
		context.font = "10px Arial";
		var s = this.locale.x + "," + this.locale.y;
		var w = context.measureText(s).width;
		var x = (this.defaultWidth - w) / 2;
		context.fillText(s, x, 40)
	};
	this.drawShape = function(context, w, x1, y1) {

		if (typeof (w) == "undefined") {
			w = this.defaultWidth;
		}

		context.save();
		this.draw(context, w, x1, y1);
		context.restore();
	};
	this.drawNav = function(context, scale) {
		
		var x1 = this.locale.x * scale;
		var y1 = this.locale.y * scale;
		var w = this.defaultWidth * scale;

		context.save();
		context.lineWidth = 5*scale;
		this.draw(context, w, x1, y1, true);
		
		context.restore();
	};
	this.drawRect = function(context, w, x1, y1) {
		if (typeof (w) == "undefined") {
			w = this.defaultWidth;
		}

		context.save();
		this.drawShapRect(context, w, x1, y1);
		context.restore();
	};
	// the loc(x, y) is based on the windows.
	this.getClosedPoint = function(loc) {

		var arc = Utils.getAngle(this.locale, loc);
		var root = this.getDiv();
		var a = root.offset();

		var result = {
			x : 0,
			y : 0
		};
		var ps = this.getPoints();
		for ( var i in ps) {
			var p = ps[i];

			var x = arc;
			if (x >= p.arc.from && x < p.arc.to) {
				result.x = p.x + a.left;
				result.y = p.y + a.top;
				break;
			}

			if (p.arc.from < 0) {
				x = x - 2 * Math.PI;

				if (x >= p.arc.from && x < p.arc.to) {
					result.x = p.x + a.left;
					result.y = p.y + a.top;
					break;
				}
			}
		}

		return Utils.toViewPoint(result);
	}

	this.drawPoints = function(context) {
		context.save();

		var ps = this.getPoints();
		for ( var i in this.getPoints()) {
			var p = ps[i];
			context.strokeStyle = 'rgba(255, 0, 0, 0.8)';
			context.beginPath();
			context.arc(p.x + 5, p.y + 5, 5, 0, Math.PI * 2, true);
			context.stroke();
		}
		context.restore();
	}
	this.showPoints = function(context) {
		var div = this.getRectDiv();
		div.show();
	};

	this.hidePoints = function(context) {
		var div = this.getRectDiv();
		div.hide();
	};
	this.bindMethod = function(div, node) {
		div.bind("mouseenter", function() {
			var q = $("#shape_thumb");
			q.children("div").html(node.desc);
			var context = q.children("canvas")[0].getContext("2d");
			q.attr("current", node.type);
			var t = 160;
			var s = 160;
			context.clearRect(0, 0, t, s);
			node.drawShape(context, 130, 80,80);
			q.show();
		});
		div.bind("mouseleave", function() {
			$("#shape_thumb").hide()
		});
		div.click(function(){
		    UI.shapePanel.find(".selected").removeClass("selected");
			$(this).toggleClass("selected");
		}); 
	};
}

function Ring() {

	this.defaultWidth = 50;
	
	this.prop = {
		size : 100,
		w : 100,
		h : 100,
		r : 50,
		x : 55,
		y : 55
	};
	this.showPoints = function(context) {
		var div = this.getPointDiv();
		div.show();
	};

	this.hidePoints = function(context) {
		var div = this.getPointDiv();
		div.hide();
	};
	this.drawShapRect = function(context, w, x1, y1) {
		this.scale = w / (this.prop.r * 2);
		var r = this.prop.r * this.scale;

		if (typeof (x1) == "undefined") {
			x1 = r + 5;
			y1 = r + 5;
		}

		context.beginPath();
		this.addShadow(context, this.scale);

		// out ring
		context.strokeStyle = 'rgba(255, 0, 0, 0.8)';
		context.arc(x1, y1, r, 0, Math.PI * 2, false);
		context.stroke();
	};
	this.draw = function(context, w, x1, y1, isNav) {
		this.scale = w / (this.prop.r * 2);
		var r = this.prop.r * this.scale;

		if (typeof (x1) == "undefined") {
			x1 = r + 1;
			y1 = r + 1;
		}

		this.prop.x = x1;
		this.prop.y = y1;

		context.beginPath();
		
		var isNav = typeof (isNav) != "undefined";
		if (isNav)
			context.strokeStyle = 'rgba(100, 140, 230, 1)';
		else{
			this.addFillShadow(context, this.scale);
			context.strokeStyle = 'rgba(100, 140, 230, 0.5)';
		}
		
		// out ring
		context.arc(x1, y1, r, 0, Math.PI * 2, false);
		context.stroke();

		// in ring
		context.strokeStyle = 'rgba(0, 0, 0, 0.1)';
		context.arc(x1, y1, r * 0.8, 0, Math.PI * 2, true);
		context.fillStyle = 'rgba(100, 140, 230, 0.1)';
	
		context.fill();
		context.stroke();

		// center ring
		context.beginPath();
		context.strokeStyle = 'rgba(0, 0, 0, 0.5)';
		if (isNav)
			context.strokeStyle = 'rgba(0, 0, 0, 0.5)';
		context.fillStyle = this.centerColor;
		context.arc(x1, y1, r * 0.5, 0, Math.PI * 2, false);
		context.fill();
	};
	this.getLinePoint = function(e) {

		var r = this.prop.r * this.scale;
		var p = Utils.toViewPoint(e);

		var d2 = p.x - this.locale.x;
		var h2 = p.y - this.locale.y;

		var d = Math.sqrt(d2 * d2 + h2 * h2);
		if (Math.abs(d - r) < 10) {
			return this.getClosedPoint(e);
		}

		return null;
	};
	this.drawPoints = function(context) {
		context.save();
		var r = this.prop.r * this.scale;
		
		context.beginPath();

		var x = this.prop.x + 6;
		var y = this.prop.y + 6;
		
		// out ring
		context.strokeStyle = 'rgba(100, 140, 230, 0.5)';
		context.arc(x, y, r, 0, Math.PI * 2, false);
		context.stroke();

		// in ring
		context.strokeStyle = 'rgba(0, 0, 0, 0.1)';
		context.arc(x, y, r * 0.8, 0, Math.PI * 2, true);
		context.fillStyle = 'rgba(255, 0, 0, 0.1)';
		context.fill();
		context.stroke();
		
		context.restore();
	};
	this.getClosedPoint = function(loc) {

		var d2 = loc.x - this.locale.x;
		var h2 = loc.y - this.locale.y;

		var z2 = Math.sqrt(d2 * d2 + h2 * h2);
		var r = this.prop.r * this.scale;

		var x1 = r * d2 / z2 + this.locale.x;
		var y1 = r * h2 / z2 + this.locale.y;

		var result = {
			x : x1,
			y : y1
		};

		return result;
	}
	this.getPoints = function() {

		if (typeof (this.points) == "undefined") {

			var r = this.prop.r * this.scale;

			var x1 = this.prop.x - r;
			var y1 = this.prop.y - r;

			var x2 = this.prop.x + r;
			var y2 = this.prop.y + r;

			var n = Math.PI / 4;

			this.points = new Array();

			this.points.push({
				x : x2,
				y : (y1 + y2) / 2,
				arc : {
					from : -1 * n,
					to : 1 * n
				}
			});

			this.points.push({
				x : (x1 + x2) / 2,
				y : y2,
				arc : {
					from : 1 * n,
					to : 3 * n
				}
			});

			this.points.push({
				x : x1,
				y : (y1 + y2) / 2,
				arc : {
					from : 3 * n,
					to : 5 * n
				}
			});

			this.points.push({
				x : (x1 + x2) / 2,
				y : y1,
				arc : {
					from : 5 * n,
					to : 7 * n
				}
			});
		}

		return this.points;
	};
}

Ring.prototype = new Node();

function StartNode() {
	this.type = "startNode";
	this.desc = lb_add_start;
	this.centerColor = 'rgba(0, 255, 0, 0.6)';
}

StartNode.prototype = new Ring();

function EndNode() {
	this.type = "endNode";
	this.desc = lb_add_end;
	this.centerColor = 'rgba(255, 0, 0, 0.6)';
}

EndNode.prototype = new Ring();

function ActivityNode() {
	this.prop = {
		size : 70,
		w : 70,
		h : 50,
		x : 55,
		y : 55
	},

	this.desc = lb_add_activity;
	this.type = "activityNode";

	this.getPoints = function() {

		if (typeof (this.points) == "undefined") {

			var w = this.prop.size * this.scale / 2;
			var h = this.prop.h * this.scale / 2;

			var x1 = this.prop.x - w;
			var y1 = this.prop.y - h;
			var x2 = this.prop.x + w;
			var y2 = this.prop.y + h;

			var n = Math.PI / 8;

			this.points = new Array();

			this.points.push({
				x : x2,
				y : (y1 + y2) / 2,
				arc : {
					from : -1 * n,
					to : 1 * n
				}
			});
			this.points.push({
				x : x2,
				y : y2,
				arc : {
					from : 1 * n,
					to : 3 * n
				}
			});

			this.points.push({
				x : (x1 + x2) / 2,
				y : y2,
				arc : {
					from : 3 * n,
					to : 5 * n
				}
			});
			this.points.push({
				x : x1,
				y : y2,
				arc : {
					from : 5 * n,
					to : 7 * n
				}
			});
			this.points.push({
				x : x1,
				y : (y1 + y2) / 2,
				arc : {
					from : 7 * n,
					to : 9 * n
				}
			});
			this.points.push({
				x : x1,
				y : y1,
				arc : {
					from : 9 * n,
					to : 11 * n
				}
			});
			this.points.push({
				x : (x1 + x2) / 2,
				y : y1,
				arc : {
					from : 11 * n,
					to : 13 * n
				}
			});

			this.points.push({
				x : x2,
				y : y1,
				arc : {
					from : 13 * n,
					to : 15 * n
				}
			});
		}

		return this.points;
	};
	this.drawShapRect = function(context, w, x1, y1) {
		this.scale = w / this.prop.size;
		var w = this.prop.size * this.scale / 2;
		var h = this.prop.h * this.scale / 2;

		var x1 = this.prop.x - w + 4;
		var y1 = this.prop.y - h + 4;
		var x2 = this.prop.x + w + 6;
		var y2 = this.prop.y + h + 6;

		context.strokeStyle = 'rgba(255, 0, 0, 1)';
		context.beginPath();
		context.moveTo(x1, y1);
		context.lineTo(x2, y1);
		context.lineTo(x2, y2);
		context.lineTo(x1, y2);
		context.lineTo(x1, y1);
		context.stroke();
	};
	this.draw = function(context, w, x1, y1, isNav) {
		this.scale = w / this.prop.size;
		var w = this.prop.size * this.scale / 2;
		var h = this.prop.h * this.scale / 2;

		if (typeof (x1) == "undefined") {
			x1 = w;
			y1 = h;
		}

		this.prop.x = x1;
		this.prop.y = y1;

		var x1 = this.prop.x - w;
		var y1 = this.prop.y - h;
		var x2 = this.prop.x + w;
		var y2 = this.prop.y + h;

		var space = this.scale * 5;

		var x3 = x1 + space;
		var y3 = y1 + space;
		var x4 = x2 - space;
		var y4 = (y2 + y1) / 2 - 1 * this.scale;

		var isNav = typeof (isNav) != "undefined";		
		if (isNav)
			context.strokeStyle = 'rgba(100, 140, 230, 1)';
		else{
			this.addFillShadow(context, this.scale);
			context.strokeStyle = 'rgba(100, 140, 230, 0.3)';
		}
		
		context.beginPath();
		context.moveTo(x3, y3);
		context.lineTo(x3, y4);
		context.lineTo(x4, y4);
		context.lineTo(x4, y3);
		context.lineTo(x3, y3);
		context.stroke();

		x3 = x1 + space;
		y3 = (y2 + y1) / 2 + 1 * this.scale;
		x4 = x2 - space;
		y4 = y2 - space;

		context.moveTo(x3, y3);
		context.lineTo(x3, y4);
		context.lineTo(x4, y4);
		context.lineTo(x4, y3);
		context.lineTo(x3, y3);
		context.stroke();

		context.fillStyle = 'rgba(218, 255, 255, 0.5)';
		context.fill();

		// out border
		context.moveTo(x1, y1);
		context.lineTo(x2, y1);
		context.lineTo(x2, y2);
		context.lineTo(x1, y2);
		context.lineTo(x1, y1);
		context.stroke();
		context.fillStyle = 'rgba(153, 204, 204, 0.8)';
		context.fill();

		// color out border again.
		context.strokeStyle = 'rgba(100, 140, 230, 1)';
		context.beginPath();
		context.moveTo(x1, y1);
		context.lineTo(x2, y1);
		context.lineTo(x2, y2);
		context.lineTo(x1, y2);
		context.lineTo(x1, y1);
		context.stroke();
	};
	
	this.json = {
			   "@name": "",
			   "task":    {
				    "@name": "",
				    "assignment": {			     
					     "activity": "",
					     "report_upload_check": "0",
					     "roles": "",
					     "accepted_time": "86400000",
					     "completed_time": "86400000",
					     "overdueToPM_time": "86400000",
					     "overdueToUser_time": "3600000",
					     "role_type": "false",
					     "sequence": "",
					     "structural_state": "-1",
					     "rate_selection_criteria": "1",
					     "expense_rate_id": "-1",
					     "revenue_rate_id": "-1",
					     "role_name": "All qualified users",
					     "role_id": "0",
					     "action_type": "lb_no_action",
					     "role_preference": [],
					     "point": "0:0"
			         },
	           }
		};
	
	// for data
	this.getAssignmentValue = function(key)
	{
		return this.json["task"]["assignment"][key];
	};
	
	this.updateAssignmentValue = function(key, value){
		this.json["task"]["assignment"][key] = value;
	}
	
	this.getDisplayActivity = function()
	{
		var activityName = this.getAssignmentValue("activity");
		var i = activityName.lastIndexOf("_");
		
		activityName = activityName.substring(0,i);
		
		return activityName;
	}
}

ActivityNode.prototype = new Node();

function ConditionNode() {
	this.defaultWidth = 60;
	this.prop = {
		size : 70,
		w : 70,
		h : 50,
		x : 45,
		y : 26
	},

	this.desc = lb_add_condition;
	this.type = "conditionNode";

	this.getPoints = function() {

		if (typeof (this.points) == "undefined") {

			var w = this.prop.size * this.scale / 2;
			var h = this.prop.h * this.scale / 2;

			var x1 = this.prop.x - w;
			var y1 = this.prop.y - h;
			var x2 = this.prop.x + w;
			var y2 = this.prop.y + h;

			var x3 = 3 * x1 / 4 + x2 / 4;
			var y3 = 3 * y1 / 4 + y2 / 4;

			var x4 = x1 / 4 + 3 * x2 / 4;
			var y4 = y1 / 4 + 3 * y2 / 4;

			var n = Math.PI / 8;

			this.points = new Array();

			this.points.push({
				x : x2,
				y : (y1 + y2) / 2,
				arc : {
					from : -1 * n,
					to : 1 * n
				}
			});
			this.points.push({
				x : x4,
				y : y4,
				arc : {
					from : 1 * n,
					to : 3 * n
				}
			});

			this.points.push({
				x : (x1 + x2) / 2,
				y : y2,
				arc : {
					from : 3 * n,
					to : 5 * n
				}
			});
			this.points.push({
				x : x3,
				y : y4,
				arc : {
					from : 5 * n,
					to : 7 * n
				}
			});
			this.points.push({
				x : x1,
				y : (y1 + y2) / 2,
				arc : {
					from : 7 * n,
					to : 9 * n
				}
			});
			this.points.push({
				x : x3,
				y : y3,
				arc : {
					from : 9 * n,
					to : 11 * n
				}
			});
			this.points.push({
				x : (x1 + x2) / 2,
				y : y1,
				arc : {
					from : 11 * n,
					to : 13 * n
				}
			});

			this.points.push({
				x : x4,
				y : y3,
				arc : {
					from : 13 * n,
					to : 15 * n
				}
			});
		}

		return this.points;
	};

	this.drawShapRect = function(context, w, x1, y1) {
		
		var w = this.prop.size * this.scale / 2 + 2;
		var h = this.prop.h * this.scale / 2 + 1;
		
		context.beginPath();
		context.lineJoin = "round";
		context.strokeStyle = 'rgba(255, 0, 0, 1)';

		var x1 = this.prop.x - w + 5;
		var y1 = this.prop.y - h + 5;
		var x2 = this.prop.x + w + 5;
		var y2 = this.prop.y + h + 5;

		context.moveTo(x1, this.prop.y + 5);
		context.lineTo(this.prop.x + 5, y1);
		context.lineTo(x2, this.prop.y + 5);
		context.lineTo(this.prop.x + 5, y2);
		context.closePath();
		context.stroke();
	}
	this.draw = function(context, w, x1, y1, isNav) {

		this.scale = w / this.prop.size;
		var w = this.prop.size * this.scale / 2;
		var h = this.prop.h * this.scale / 2;

		if (typeof (x1) == "undefined") {
			x1 = w;
			y1 = h;
		}

		this.prop.x = x1;
		this.prop.y = y1;

		var isNav = typeof (isNav) != "undefined";		
		if (!isNav)
		    this.addFillShadow(context, this.scale);

		context.beginPath();
		context.lineJoin = "round";
		context.strokeStyle = 'rgba(100, 140, 230, 0.7)';
		if (isNav)
			context.strokeStyle = 'rgba(100, 140, 230, 1)';

		var x1 = this.prop.x - w;
		var y1 = this.prop.y - h;
		var x2 = this.prop.x + w;
		var y2 = this.prop.y + h;

		context.moveTo(x1, this.prop.y);
		context.lineTo(this.prop.x, y1);
		context.lineTo(x2, this.prop.y);
		context.lineTo(this.prop.x, y2);
		context.closePath();

		context.fillStyle = 'rgba(218, 255, 255, 0.5)';
		if (isNav)
			context.fillStyle = 'rgba(218, 255, 255,, 1)';
		context.fill();
		context.stroke();

		context.fillStyle = 'rgba(153, 204, 204, 0.8)';
		if (isNav)
			context.fillStyle = 'rgba(153, 204, 204, 1)';
		context.fill();

		context.stroke();
	};
}

ConditionNode.prototype = new Node();