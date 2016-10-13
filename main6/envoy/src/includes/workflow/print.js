var Print = {
	print : function() {
		
		this.draw();
		
		var dataUrl = UI.printCanvas[0].toDataURL();
		UI.printImg[0].src = dataUrl;
	},
	draw : function() {
		if (!UI.init)
			return;
		var x = 0;
		var y = 0;
		
		for ( var b in Model.nodes) {
			var node = Model.nodes[b];
			if (node.locale.x > x)
				x = node.locale.x;
			if (node.locale.y > y)
				y = node.locale.y;
		}
		
		x = x + 100;
		y = y + 100;
		
		UI.printCanvas.attr({
			width : x,
			height : y
		});
		
		var navCtx = UI.printCanvas[0].getContext("2d");
		navCtx.clearRect(0, 0, 2000, 2000);
		
		for ( var b in Model.nodes) {
			var node = Model.nodes[b];
			var n = Utils.copy(node);
			n.drawShape(navCtx, n.defaultWidth, n.locale.x, n.locale.y);
			n.showTxt(navCtx, n.locale.x - 50, n.locale.y - 35);
			
			for (var i in n.tos) {
				var l = n.tos[i];
				l.drawForPrint(navCtx);
			}
		}
	}
}