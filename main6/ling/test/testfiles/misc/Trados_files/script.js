var ns4 = (document.layers) ? true : false;
var ie4 = (document.all) ? true : false;
var help1=document.location.href.split('/');
var gruppe=help1[help1.length-2];
var LastShown = "";
var LastShownDot = "";

about2 = new Image();
about2.src = "../images/nav2/about2.gif";

function DisSub(ID) {
	HideSub();
	if (ns4) {
		css = document.layers[ID];
		}
	if (ie4) {
		css = document.all[ID].style;
		}
	css.visibility = "visible";
	LastShown = ID;
}

function HideSub() {
	HideDot();
	if (LastShown != "") {
		if (ns4) {
			css = document.layers[LastShown];
			}
		if (ie4) {
			css = document.all[LastShown].style;
		}
	css.visibility = "hidden";
	LastShown = "";
	}
}

function ShowSub(ID) {
	if (ns4) {
		css = document.layers[ID];
		}
	if (ie4) {
		css = document.all[ID].style;
		}
	css.visibility = "visible";
	LastShown = ID;
}

function DisDot(ID) {
	HideDot();
	if (ns4) {
		document.layers[LastShown].document.images[ID].src = ("pics/rot.gif");
		}
	if (ie4) {
		document[ID].src = ("pics/rot.gif");
		}
	LastShownDot = ID;
}

function HideDot() {
	if (LastShownDot != "") {
		if (ns4) {
			document.layers[LastShown].document.images[LastShownDot].src = ("images/transparent.gif");
			}
		if (ie4) {
			document[LastShownDot].src = ("images/transparent.gif");
			}
	LastShownDot = "";
	}
}
