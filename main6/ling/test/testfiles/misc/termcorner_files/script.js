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
		document.layers["menu"].document.images[ID + "Head"].src = ("../images/nav/sub_" + ID + "2.gif");
		}
	if (ie4) {
		css = document.all[ID].style;
		document[ID + "Head"].src = ("../images/nav/sub_" + ID + "2.gif");
		}
	css.visibility = "visible";
	LastShown = ID;
}

function HideSub() {
	HideDot();
	if (LastShown != "") {
		if (ns4) {
			css = document.layers[LastShown];
			if (LastShown != gruppe)
				document.layers["menu"].document.images[LastShown + "Head"].src = ("../images/nav/sub_" + LastShown + ".gif");
			}
		if (ie4) {
			css = document.all[LastShown].style;
			if (LastShown != gruppe)
				document[LastShown + "Head"].src = ("../images/nav/sub_" + LastShown + ".gif");
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
		document.layers[LastShown].document.images[ID].src = ("../pics/rot.gif");
		}
	if (ie4) {
		document[ID].src = ("../pics/rot.gif");
		}
	LastShownDot = ID;
}

function HideDot() {
	if (LastShownDot != "") {
		if (ns4) {
			document.layers[LastShown].document.images[LastShownDot].src = ("../images/transparent.gif");
			}
		if (ie4) {
			document[LastShownDot].src = ("../images/transparent.gif");
			}
	LastShownDot = "";
	}
}

function inizialiate() {
	if (ns4) {
		document.layers["menu"].document.images[gruppe + "Head"].src = ("../images/nav/sub_" + gruppe + "2.gif");
		document.layers["menu"].document.images["header"].src = bild;
		}
	if (ie4) {
		document[gruppe + "Head"].src = ("../images/nav/sub_" + gruppe + "2.gif");
		document["header"].src = bild;
		}
}

function doresize() {
    inizialiate();
}

function setresize() {
    window.onresize=doresize;
}

if (ns4) window.onload=setresize;
