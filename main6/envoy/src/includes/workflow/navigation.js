var rectX = 0;
var rectY = 0;

var Navigation = {
	init : function() {
		UI.navDiv.draggable();
		var p = UI.navButton.offset();
		UI.navDiv.css({
			left : 30 + p.left  + "px",
			top :"5px"
		});
		
		UI.navButton.click(function(){			
			$(this).toggleClass("selected2");
			if (UI.navDiv.is(":hidden")) {
				UI.navDiv.show();
			} else {
				UI.navDiv.hide();
			}
		});
		
		UI.viewport.scroll(function() {
			rectX = UI.viewport.scrollLeft() / 10;
			rectY = UI.viewport.scrollTop() / 10;
			
			UI.navRectDiv.css({
				left : rectX + "px",
				top : rectY + "px"
			});
		});

		
		// size of navDiv: 200*200 
		// size of select div: 120*50
		// size of show div: 1200*500
		UI.navDiv.bind("mousemove.moveRect", function(e) {
			
			e.preventDefault();	
			log(e.offsetX + ":" + e.offsetY + "----------" + e.target.id);
			
			
			var p = Utils.windowToCanvas(UI.navDiv, e.clientX, e.clientY);
			
			var x = p.x - 60;
			var y = p.y - 25;
			
			if (x < 0)
				x = 0;
			
			if (x > 80)
				x = 80;
			
			if (y < 0)
				y = 0;
			
			if (y > 150)
				y = 150;			
			
			rectX = x;
			rectY = y;
			var div = UI.navRectDiv;
			
			div.css({
				left : x + "px",
				top : y + "px"
			});
		});
		UI.navDiv.click(function(e){
			
			e.preventDefault();	
			
			var x = rectX;
			var y = rectY;

            x = x * 10;
            y = y * 10;
			
			UI.viewport.scrollLeft(x);
			UI.viewport.scrollTop(y);
		});
	},
	draw : function() {
		if (!UI.init)
			return;
		
		var navCtx = UI.navCanvas[0].getContext("2d");
		navCtx.clearRect(0, 0, 200, 200);
		for ( var b in Model.nodes) {
			var node = Model.nodes[b];
			var n = Utils.copy(node);
			n.drawNav(navCtx, 0.1);
			
			for (var i in n.tos) {
				var l = n.tos[i];
				l.drawForNav(navCtx, 0.1);
			}
		}
	}
}
