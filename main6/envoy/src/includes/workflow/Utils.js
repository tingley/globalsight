var Utils = {
	i : 1,
	copy : function(a) {
		return $.extend(true, {}, a)
	},
	windowToCanvas : function(canvas, x, y) {
		var a = canvas.offset();
		if (a == null) {
			a = {
				left : 0,
				top : 0
			}
		}
		return {
			x : x - a.left + canvas.scrollLeft(),
			y : y - a.top + canvas.scrollTop()
		}
	},
	toViewPoint : function(p) {
		
		var x;
		var y;
		
		if (typeof (p.clientX) != "undefined") {
			x = p.clientX;
			y = p.clientY;
		} else {
			x = p.x;
			y = p.y;
		}
		
		return this.windowToCanvas(UI.viewport, x, y);
	},

	newId : function() {
		Utils.i++;
		var b = Math.random();
		var a = (b + new Date().getTime());
		return a.toString(16).replace(".", "") + Utils.i;
	},
	newZIndex : function() {
		this.i++;
		return this.i;
	},
	getAngle : function(c, a) {
		var b = Math.atan(Math.abs(c.y - a.y) / Math.abs(c.x - a.x));
		if (a.x <= c.x && a.y > c.y) {
			b = Math.PI - b
		} else {
			if (a.x < c.x && a.y <= c.y) {
				b = Math.PI + b
			} else {
				if (a.x >= c.x && a.y < c.y) {
					b = Math.PI * 2 - b
				}
			}
		}
		return b
	},
	optimizeAndMoveToViewport : function(node, nodeDiv, e, w) {
		var loc;
		if (typeof(e) == "undefined"){
			loc = {
					x : node.locale.x,
					y : node.locale.y
			};
		} else {
			loc = Model.getOptimizedLocale(e.clientX, e.clientY, node);
		}
		
		var offset = node.getOffset(w);
		var x = loc.x - offset.x;
		var y = loc.y - offset.y;
		
		if (x < 0){
			x = 0;
			loc.x = offset.x;
		}
		
		if (y < 0){
			y = 0;
			loc.y = offset.y;
		}
		
		nodeDiv.css({
			left : x + "px",
			top : y + "px"
		});

		node.locale.x = loc.x;
		node.locale.y = loc.y;
	},

	moveDivToCanvas : function(node, nodeDiv, root, e, w) {
		var loc = Utils.windowToCanvas(root, e.clientX, e.clientY);
		if (loc.x < 0) {
			loc.x = 0;
		}

		if (loc.y < 0) {
			loc.y = 0;
		}

		var offset = node.getOffset(w);
		nodeDiv.css({
			left : loc.x - offset.x + "px",
			top : loc.y - offset.y + "px"
		});

		node.locale.x = loc.x;
		node.locale.y = loc.y;
	},
	
	removeLine : function(list, l) {
		var index = -1;
		for ( var i in list) {
			var l1  = list[i];
			if (l.id == l1.id) {
				index = i;
				break;
			}
		}
		
		if (index > -1) {
			list.splice(index, 1);
		}
	},
	removeNode : function(list, l) {
		delete list[l.id];
	}
}

function log(msg) {
	console.log(msg);
}

function timeToNumber(d, h, m) {
	return (86400 * d + 3600*h + 60 * m) * 1000;
}

function getTime(time){
	var s = Math.floor(time / 1000);
	var d = Math.floor(s / 86400);//24 * 3600;
	s = s % 86400;
	var h = Math.floor(s / 3600);
	s = s % 3600;
	var m = Math.floor(s / 60);
	
	return {
		d : d,
		h : h,
		m : m
	};
}

function numberIsValid(number, low, high) {
	return number >= low && number <= high;
}

function timesAreValid(d, h, m)
{
    return numberIsValid(d, 0, 365)
            && numberIsValid(h, 0, 24 * 7)
            && numberIsValid(m, 0, 12 * 60);
}

function getDistance(p1, p2){
	return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
}

// Gets the distance for the p3 to a line. The p1,p2 is the start and end for the line.
function getLength(p1, p2, p3) {
	
	var x1;
	var x2;
	var y1;
	var y2;
	
	if (p2.x < p1.x){
		x1 = p2.x;
		x2 = p1.x;
	} else {
		x1 = p1.x;
		x2 = p2.x;		
	}
	
	if (p2.y < p1.y){
		y1 = p2.y;
		y2 = p1.y;
	} else {
		y1 = p1.y;
		y2 = p2.y;		
	}
	
	if (p3.x < x1 || p3.x > x2){
		if (x2 - x1 > 30)
			return;
	}
		
	
	if (p3.y < y1 || p3.y > y2) {
		if (y2 - y1 > 30)
			return
	}
	
	if (x2 - x1 < 3) {
		return Math.abs(p3.x - p1.x);
	} else {
		var k=(p1.y-p2.y)/(p1.x-p2.x);
	    var L=Math.abs(k * (p3.x-p1.x)-(p3.y-p1.y))/Math.sqrt((1+k*k));
	    return L;
	}
}