var Toolbar = {
	init : function() {
		this.initTitle();
		this.initToolBar1();
		this.initToolBar2();
	},
	initTitle : function(){
		$("[title],[original-title]")
	    .live(
		    "mouseover",
		    function() {
			if ($(this).attr("disableTitle")) {
			    return false
			}
			var k = $(this);
			if (k.attr("title")) {
			    k.attr("original-title", k.attr("title"));
			    k.removeAttr("title")
			}
			if (!k.attr("original-title")) {
			    return
			}
			var l = k.attr("original-title");
			var j = UI.hover_tip;
			if (j.length == 0) {
			    j = $(
				    "<div id='hover_tip'><div class='tip_arrow'></div><div class='tip_content radius3'></div></div>")
				    .appendTo("body");
			    UI.hover_tip = $("#hover_tip");
			}
			$(".tip_content").html(l);
			UI.hover_tip.show();
			$(".tip_arrow").removeClass("tip_right").removeClass(
				"tip_top").css("top", "");
			if (k.attr("title_pos") == "right") {
			    j.css({
				left : k.offset().left + k.outerWidth() + 7,
				top : k.offset().top + k.outerHeight() / 2
					- j.outerHeight() / 2
			    });
			    $(".tip_arrow")
				    .attr("class", "tip_arrow tip_right").css(
					    "top", j.outerHeight() / 2 - 7)
			} else {
			    if (k.attr("title_pos") == "top") {
				j.css({
				    left : k.offset().left + k.outerWidth() / 2
					    - j.outerWidth() / 2,
				    top : k.offset().top - j.outerHeight()
				});
				$(".tip_arrow").attr("class",
					"tip_arrow tip_top")
			    } else {
				if (k.attr("title_pos") == "left") {
				    j.css({
					left : k.offset().left - j.outerWidth()
						- 7,
					top : k.offset().top + k.outerHeight()
						/ 2 - j.outerHeight() / 2
				    });
				    $(".tip_arrow").attr("class",
					    "tip_arrow tip_left")
				} else {
				    j.css({
					left : k.offset().left + k.outerWidth()
						/ 2 - j.outerWidth() / 2,
					top : k.offset().top + k.outerHeight()
				    });
				    $(".tip_arrow").attr("class", "tip_arrow")
				}
			    }
			}
		    }).live("mouseout", function() {
			    UI.hover_tip.hide()
		    });
	},
    // for save, cancel, print and previous
	initToolBar2 : function() {
		$("#print").click(function(){
			Print.print();
		});
	},
	// for start, end, line and so on...
	initToolBar1 : function() {

		var node = new EndNode();
		var div = $("#button_end");
		var canvas = div.children()[0];
		node.drawShape(canvas.getContext("2d"), 30, 25, 25);
		this.initToolBarButtonMethod(div, node, UI.button.end);

		node = new ActivityNode();
		div = $("#button_activity");
		canvas = div.children()[0];
		node.drawShape(canvas.getContext("2d"), 30, 25, 25);
		this.initToolBarButtonMethod(div, node, UI.button.activity);

		node = new ConditionNode();
		div = $("#button_condition");
		canvas = div.children()[0];
		node.drawShape(canvas.getContext("2d"), 30, 25, 25);
		this.initToolBarButtonMethod(div, node, UI.button.condition);

		div = $("#button_line");
		canvas = div.children()[0];
		var context = canvas.getContext("2d");
		context.beginPath();
		context.moveTo(17, 17);
		context.lineTo(33, 33);
		context.stroke();

		var line = new Line();
		var p1 = {
			x : 15,
			y : 15
		};
		var p2 = {
			x : 35,
			y : 35
		};
		line.isDefault = false;
		line.drawArrow(context, p1, p2);
		this.initToolBarButtonMethod(div, null, UI.button.line);

		div = $("#button_point");
		canvas = div.children()[0];
		var context = canvas.getContext("2d");
		context.beginPath();
		context.moveTo(20, 15);
		context.lineTo(20, 35);
		context.lineTo(26, 31);
		context.lineTo(31, 40);
		context.lineTo(35, 38);
		context.lineTo(30, 30);
		context.lineTo(37, 28);
		context.lineTo(20, 15);
		context.stroke();
		this.initToolBarButtonMethod(div, null, UI.button.point);
	},
	initToolBarButtonMethod : function(div, node, button) {

		div.click(function() {
			UI.toolbar.find(".selected").removeClass("selected");
			$(this).toggleClass("selected");
			UI.button.value = button;
			Toolbar.unbindAllForChangeButton();

			if (node != null) {
				var context2 = UI.creatingCanvas[0].getContext("2d");
				context2.clearRect(0, 0, 200, 200);
				node.drawShape(context2);

				UI.bodyDiv.unbind().bind("mousemove.create", function(t) {

					var creatingDiv = UI.creatingDiv;
					creatingDiv.css({
						left : "-100px",
						top : "-100px"
					});
					creatingDiv.show();
				});

				UI.bodyDiv.bind("mousemove.creating", function(e) {
					e.preventDefault();
					Utils.optimizeAndMoveToViewport(node, UI.creatingDiv, e);
					Model.showSnapLine(e, node);
				});

				UI.viewport.unbind("mouseup").bind("mouseup", function(e) {
					if (node) {
						Model.add(node, e);
					}
				});
			}

		});
	},
	unbindAllForChangeButton : function() {
		UI.bodyDiv.unbind();
		UI.viewport.unbind("mouseup");
		UI.shapeCanvasDiv.hide();
		UI.creatingDiv.hide();
		Model.hideSnapLine();
	},
}