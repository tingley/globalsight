/*
Copyright (c) 2003 - XDE
-------------------------------------------------------------
$Id: xderichedit.js,v 1.1 2009/04/14 15:42:52 yorkjin Exp $
-------------------------------------------------------------
$RCSfile: xderichedit.js,v $
$Author: yorkjin $
$Revision: 1.1 $
$Date: 2009/04/14 15:42:52 $
$Locker:  $
$Name:  $
------------------------------------------------------------
*/
var ActiveSelection=null;displayEditBox
var wroteOutStyleSheet=false;
var commonTermArray="Common Term #1,Sample #2 XDE&copy;, XDE&copy; Spell Checker,Common Term #4, Common <b>Term</b> #5, XQASP&reg;";
var specialchars= "&bull;,&copy;,&reg;,&trade;,&cent;,&euro;,&pound;,&yen;,&deg;,&plusmn;,&frac12;,&frac14;,&frac34;,&brvbar;,&lsquo;,&rsquo;,&ldquo;,&rdquo;".split(",");
var fontNamesArray="Arial,Arial Black,Arial Narrow,Comic Sans MS,Courier New,Times New Roman,Verdana";
var fontSizeArray="1,2,3,4,5,6,7";
var activeControl=null;
var htmlEditorMode=false;
var browserName=navigator.appName; 

var browserVer=parseInt(navigator.appVersion);

if ((browserName=="Netscape" && browserVer>=5)){
	htmlEditorMode=true; 
}else if (browserName=="Microsoft Internet Explorer"){
	workStr=navigator.appVersion;
	startIdx=workStr.indexOf("MSIE ");
	endIdx=workStr.indexOf(";",startIdx);
	browserVer=parseFloat(workStr.substring(startIdx+5,endIdx));
	htmlEditorMode=browserVer>=5.5;
} 
  
   


if(xdeeditorBasePath==null) //set default in case the developer forgot.
	var xdeeditorBasePath="/globalsight/xdespellchecker/editor/";
//Write out Stylesheet
tmp="<link rel=\"stylesheet\" href=\""+xdeeditorBasePath+"xdeStyleSheet.css\" type=\"text/css\">";
document.write(tmp);


function setCommonTerms(iFrameName,pcommon){
	commonTermArray=pcommon;
}
function setFontNames(iFrameName,pcommon){
	fontNamesArray=pcommon;
}
function setFontSizes(iFrameName,pcommon){
	fontSizeArray=pcommon;
}

function setText(iFrameName,html){
	var tmp="";
	var docElem=document.getElementById(iFrameName);
	if (!htmlEditorMode) {
		tmp = unescape(html);
		docElem.value=tmp;
		return;
	} else {
		tmp = '<link rel="stylesheet" href="xdeStyleSheet.css" type="text/css"><body>' + unescape(html) + '</body>';
	}
	if (isNS) {
		document.getElementById(iFrameName).frameWindow.document.body.innerHTML =tmp;
		document.getElementById(iFrameName).value = tmp;
	} else {
		// loop to wait on
		while(typeof(document.getElementById(iFrameName).frameWindow.document)=='undefined')
		{
		}
	
		document.getElementById(iFrameName).frameWindow.document.open();
		document.getElementById(iFrameName).frameWindow.document.write(tmp);
		document.getElementById(iFrameName).frameWindow.document.close();
	}
}
/** Normally used to retrieve data in html encoded form, and send to a database */
function xde_getXHTML(iFrameName){
	if(!htmlEditorMode)
		return escape(document.getElementById(iFrameName).value);
	return escape(document.getElementById(iFrameName).frameWindow.document.body.innerHTML);
}
/** Returns raw HTML, for spell checking etc. */
function xde_spellHTML(iFrameName){
	if(!htmlEditorMode)
		return document.getElementById(iFrameName).value;

	return document.getElementById(iFrameName).frameWindow.document.body.innerHTML;
}

function selectEdit() {
	if (document.all.edit.frameWindow)
		document.all.edit.frameWindow.focus();
}

function xde_resizeBody() {
	if (document.body.offsetHeight < 100)
		return;
}

function xde_CloseMenus() {
	hide("dFontName");
	hide("dFontSize");
	hide("dcommonTerm");
}
function show(id) {
	if(document.getElementById(id).style.display != "block"){
		document.getElementById(id).style.display = "block";
		if(isNS)
			document.getElementById(id).style.visibility= "visible";
	}
}

function hide(id) {
	if(document.getElementById(id)){
		if(document.getElementById(id).style.display != "none"){
			document.getElementById(id).style.display = "none";
			if(isNS)
				document.getElementById(id).style.visibility= "hidden";
		}
	}
}

function xde_closeDropdown(dropdown){

	var activeEvent=window.event || arguments.callee.caller.arguments[0];
	var x = activeEvent.clientX;
	var y = activeEvent.clientY;
	if (x < dropdown.offsetLeft || x > dropdown.offsetLeft + dropdown.offsetWidth ||y+10 < dropdown.offsetTop || y > dropdown.offsetTop + dropdown.offsetHeight)
		dropdown.style.display = "none";
}
function findPosY(obj){//http://www.xs4all.nl/~ppk/js/findpos.html
	var curtop = 0;
	if (obj.offsetParent)
	{
		while (obj.offsetParent)
		{
			curtop += obj.offsetTop
			obj = obj.offsetParent;
		}
	}
	else if (obj.y)
		curtop += obj.y;
	return curtop;
}	
function findPosX(obj){//http://www.xs4all.nl/~ppk/js/findpos.html
	var curtop = 0;
	if (obj.offsetParent)
	{
		while (obj.offsetParent)
		{
			curtop += obj.offsetLeft
			obj = obj.offsetParent;
		}
	}
	else if (obj.x)
		curtop += obj.x;
	return curtop;
}	
function xdeopenMenu(menuID,belowID){
	var dMenu=document.getElementById(menuID);
	if(dMenu.style.display=="none"){
		xde_CloseMenus();
		dMenu.style.left =findPosX(document.getElementById(belowID));
		dMenu.style.top =findPosY(document.getElementById(belowID))+document.getElementById(belowID).offsetHeight-1;
		show(menuID);
	}else{
		hide(menuID);
	}
}


function xde_editFontSize(iFrameName,s) {
	hide("dFontSize");
	document.getElementById(iFrameName).frameWindow.focus();
	document.getElementById(iFrameName).execCommand('FontSize', s);

}
function xde_editFontName(iFrameName,s) {
	hide("dFontName");
	document.getElementById(iFrameName).frameWindow.focus();
	document.getElementById(iFrameName).execCommand('FontName', s);
}

var sColorType;

function xde_openColorPicker(iFrameName,sType) {
	var iHeight = 280;
	var iWidth = 340;
	var leftPos = window.screenLeft + ((window.document.body.offsetWidth - iWidth) / 2);
	var topPos = window.screenTop + ((window.document.body.offsetHeight - iHeight) / 2);
	if (topPos + iHeight + 60 > window.screen.height)
		topPos = window.screen.height - (iHeight + 60);
	xde_CloseMenus();
	document.getElementById(iFrameName).frameWindow.focus();
	sColorType = sType;
	window.open(xdeeditorBasePath+"colorpicker.htm?iFrameName="+iFrameName+"&", "colorpicker", "left="+leftPos+",top="+topPos+",width="+iWidth+",height="+iHeight+",center=yes,toolbar=no,resizable=no");
}

function editSetColor(iFrameName,sColor) {
	xde_CloseMenus();
	document.getElementById(iFrameName).frameWindow.focus();
	document.getElementById(iFrameName).execCommand(sColorType, sColor);
}

function xde_openPopUp(iFrameName,popupHTMLFileName,title,width,height){
	var leftPos = window.screenLeft + ((window.document.body.offsetWidth - width) / 2);
	var topPos = window.screenTop + ((window.document.body.offsetHeight - height) / 2);
	xde_CloseMenus();
	document.getElementById(iFrameName).frameWindow.focus();
	window.open(xdeeditorBasePath+popupHTMLFileName+"?iFrameName="+iFrameName+"&", title, "left="+leftPos+",top="+topPos+",width="+width+",height="+height+",center=yes,toolbar=no,resizable=no");

}
function editSetEmoticon(iFrameName,sEmoticon) {
	document.getElementById(iFrameName).frameWindow.focus();
	document.getElementById(iFrameName).execCommand('InsertImage', sEmoticon);
}

function editSetFormatting(iFrameName,sValue) {

	xde_CloseMenus();
	if(htmlEditorMode)
		document.getElementById(iFrameName).frameWindow.focus();
	if(sValue=="insertlink"){
		
		if(isNS){
			var URLTitle = prompt("Enter a Title for the URL:", "");
			var urlLink= prompt("Enter a URL e.g. http://xde.net/pathToImage.gif", "http://");
			tmpHTML="<a href=\""+urlLink+"\">"+URLTitle+"</a>";
			insertHTML(iFrameName,tmpHTML);
		}else{
			document.getElementById(iFrameName).execCommand("CreateLink");
		}
	}else if(sValue=="insertURLImage"){
			var imagePath = prompt('Enter Image URL:', 'http://');				
			if ((imagePath != null) && (imagePath != "")) {
				editSetEmoticon(iFrameName,imagePath);
			}
	}else if(sValue=="pagebreak"){
		insertHTML(iFrameName,"<P STYLE=\"page-break-before: always;\"></p>"); 
	}else if(sValue=="preview"){

		var tmpHTML=xde_spellHTML(iFrameName);
		msgWindow=window.open("","displayWindow","toolbar=no,width=375,height=480,directories=no,status=no,scrollbars=yes,resize=no,menubar=yes,location=no");
		msgWindow.document.write(tmpHTML);
	}else if(sValue=="save"){
		saveIt();
	}else{
		
		document.getElementById(iFrameName).execCommand(sValue);
	}
}
var spellingWindow=null;
function spellcheckIt(iFrameName) {
	if(htmlEditorMode){
		document.getElementById(iFrameName).frameWindow.focus();
		doSpell( this,'myFormName.language', iFrameName+'&typectrl=xdeedit', true);
	}else{
		doSpell( this,'', iFrameName, true);
	}
}

function spellCheckAndSave(iFrameName){
	if(htmlEditorMode){
		document.getElementById(iFrameName).frameWindow.focus();
		doSpell( this,'', iFrameName+'&typectrl=xdeedit&exec=saveIt()', true);
	}else{
		doSpell( this,'', iFrameName+'&exec=saveIt()', false);

	}

//	if (spellingWindow.opener == null) spellingWindow.opener = self;
	
}

function closeLoadingDiv() {
	if (parent.window.loading)
		parent.window.loading.style.display="none";
}
function undoText(i) {
	objTo = eval('document.all.' + i + '.frameWindow.document.body');
	objHold = eval('document.frmAddEdit.' + i + 'Hold');
	objHold.value = objTo.innerHTML;
	objFrom = eval('document.frmAddEdit.' + i + 'Original');
	objTo.innerHTML = objFrom.value;
	eval('document.all.' + i + '.frameWindow.focus()');
}

function redoText(i) {
	objTo = eval('document.all.' + i + '.frameWindow.document.body');
	objFrom = eval('document.frmAddEdit.' + i + 'Hold');
	objTo.innerHTML = objFrom.value;
	eval('document.all.' + i + '.frameWindow.focus()');
}

function initRichEdits() { //initialize all rich text editors
	var allframes=null;
	if(isNS){
		allframes = document.getElementsByTagName("IFRAME");
	}else{
		allframes=document.all.tags("IFRAME");
	}
	for (var i=0; i<allframes.length; i++) {
		if (allframes[i].className == "richEdit")
			initRichEdit(allframes[i]);
	}
	if(!isNS){
		// Make sure everything is not selectable
		var all = document.all;
		var l = all.length;
		for (var i = 0; i < l; i++) {
			if (all[i].tagName != "INPUT" && all[i].tagName != "TEXTAREA" && all[i].tagName != "IFRAME")
				all[i].unselectable = "on";
		}
	}

}
function geckoGetRv()
{
  if (navigator.product != 'Gecko')
  {
    return -1;
  }
  var rvValue = 0;
  var ua      = navigator.userAgent.toLowerCase();
  var rvStart = ua.indexOf('rv:');
  var rvEnd   = ua.indexOf(')', rvStart);
  var rv      = ua.substring(rvStart+3, rvEnd);
  var rvParts = rv.split('.');
  var exp     = 1;

  for (var i = 0; i < rvParts.length; i++)
  {
    var val = parseInt(rvParts[i]);
    rvValue += val / exp;
    exp *= 100;
  }

  return rvValue;
}

function insertHTML(iFrameName,html) {
	xde_CloseMenus();
	document.getElementById(iFrameName).frameWindow.focus();
	if(!isNS){
	 	tmpRange = document.getElementById(iFrameName).document.selection.createRange();
		tmpRange.pasteHTML(html);
	}else{
		if(geckoGetRv()>"1.04"){
			document.getElementById(iFrameName).execCommand('insertHTML', html);
		}else{
			alert("Your Gecko browser version("+geckoGetRv()+") does not support insertion of HTML, please upgrade to 1.05 or later http://www.mozilla.org/");
		}
		return;
	}
	
	
}

function initRichEdit(editorDocument) {
	//Is there a document ID yet?
	if (editorDocument.id) {
		if(isNS){
			editorDocument.frameWindow = frames[editorDocument.id];
		}else{
			editorDocument.frameWindow = document.frames[editorDocument.id];
		}
		if (editorDocument.value == null)
			editorDocument.value = editorDocument.innerHTML;
//		editorDocument.src ="about:blank";
		//contruct the new document, set design mode on.
		var newDocument = null;
		if(isNS){
			newDocument = editorDocument.contentDocument;
		}else{
			editorDocument.value='<link rel="stylesheet" href="xdeStyleSheet.css" type="text/css"><body></body>';		
			newDocument = editorDocument.frameWindow.document;
		}
		newDocument.designMode = "On";
		try{
			newDocument.open();
			newDocument.write(editorDocument.value);
			newDocument.close();
		}catch(e){}
		editorDocument.execCommand = function (pExecProp, pExecVal, pUserInterface) {
			var lDoc = editorDocument.contentWindow.document;
			var lSelectionType = null;
			var oTarget=lDoc; //default to document
			if(!isNS){
				ActiveSelection = lDoc.selection;
				lSelectionType = lDoc.selection.type; 
			}
			
			
			if(ActiveSelection != null&!isNS){  //Something is selected, get the range.
				if(lSelectionType != "None")  //Something is selected, get the range.
					oTarget=lDoc.selection.createRange();
			}
			var returnValue = oTarget.execCommand(pExecProp, pUserInterface, pExecVal);

			ActiveSelection=null;
			if (lSelectionType == "Text")
				oTarget.select();
			return returnValue;
		}
	}else{
		alert("editorDocument.id failed!");
	}
	
}
function displayEditBox(useIframeName,toolbarDisplay){

	writeRTEMenu(useIframeName);
	tableOpenTag="<table border=0 cellspacing=\"1\" id=\"editToolbar\" name=\"editToolbar\"><tr>";

	if(toolbarDisplay==""||toolbarDisplay==null){
		if(!isNS){
			toolbarDisplay="Save,spellandsave,|,Preview,|,Undo,Redo,|,copypaste,|,fontname,fontsize,|,Bold,Italic,Underline,StrikeThrough,SuperScript,SubScript,|,justifyleft,justifyCenter,justifyright,justifyfull,|,insertunorderedlist,insertorderedlist,|,spellcheck,!,commonterms,|,indent,outdent,|,forecolor,backcolor,|,specialchar,horizontalrule,PageBreak,inserttable,insertlink,insertimage,emoticon,insertimageurl";	
		}else{
			toolbarDisplay="Save,spellandsave,|,Preview,|,Undo,Redo,|,fontname,fontsize,|,Bold,Italic,Underline,StrikeThrough,SuperScript,SubScript,|,justifyleft,justifyCenter,justifyright,justifyfull,|,insertunorderedlist,insertorderedlist,|,spellcheck,!,commonterms,|,indent,outdent,|,forecolor,backcolor,|,specialchar,horizontalrule,PageBreak,inserttable,insertlink,insertimage,emoticon,insertimageurl";
		}
	}
	document.write("<div id=\"editToolbarDiv\" style=\"background: buttonface;\" width=\"100%\">"+tableOpenTag);
	if(!htmlEditorMode){
		toolbarDisplay="Save,spellandsave,|,Preview,|,spellcheck";
//!,commonterms,|,indent,outdent,|,forecolor,backcolor,|,specialchar,horizontalrule,PageBreak,inserttable,insertlink,insertimage,emoticon,insertimageurl";	
	}

	var arr = toolbarDisplay.split(",");
	for(var i = 0; i < arr.length; i++){
		var tmpvalUpperLower=arr[i];
		var tmpval=tmpvalUpperLower.toLowerCase();
			
	
	if(tmpval=="strikethrough"||tmpval=="superscript"||tmpval=="subscript"||tmpval=="save"||tmpval=="bold"||tmpval=="italic"||tmpval=="underline"||tmpval=="redo"||tmpval=="undo"||tmpval=="pagebreak"||tmpval=="preview"){ 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','"+tmpval+"')\"><img src=\""+xdeeditorBasePath+"images/edit/"+tmpval+".gif\" alt=\""+tmpvalUpperLower+"\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="!"){ //New toolbar row
		document.write(tableOpenTag);
	}
	if(tmpval=="|"){ //divider
		document.write("<td><div class='coolDivider'></div></td>");
	}
	if(tmpval=="spellandsave"){ //Spell Check and Save Button
		document.write("<td class=\"coolButton\" onclick=\"spellCheckAndSave('"+useIframeName+"');\"><img src=\""+xdeeditorBasePath+"images/edit/spellsave.gif\" alt=\"Spell Check and Save\" width=16 height=16></td>");
		continue;
	}
	
	if(!isNS&&tmpval=="copypaste"){ //Copy paste 
		document.write("<td class='coolButton' onclick=\"editSetFormatting('"+useIframeName+"','cut');\"><img src='"+xdeeditorBasePath+"images/edit/cut.gif' alt='Cut' width=16 height=16></td>");
		document.write("<td class='coolButton' onclick=\"editSetFormatting('"+useIframeName+"','copy');\"><img src='"+xdeeditorBasePath+"images/edit/copy.gif' alt='Copy' width=16 height=16></td>");
		document.write("<td class='coolButton' onclick=\"editSetFormatting('"+useIframeName+"','paste');\"><img src='"+xdeeditorBasePath+"images/edit/paste.gif' alt='Paste' width=16 height=16></td>");
		continue;
	}

	
	if(tmpval=="fontname"){ //Font change 
		tmp="<td class=\"coolButton\" id=\"toolFontName\" onclick=\"xdeopenMenu('dFontName','toolFontName');\"><nobr>&nbsp;<b>Font</b><img src=\""+xdeeditorBasePath+"images/ocollapse.gif\">&nbsp;</nobr></td>";
		
		document.write(tmp);
		continue;
	}
	if(tmpval=="fontsize"){ //Font size 
		document.write("<td class=\"coolButton\" id=\"toolFontSize\" onclick=\"xdeopenMenu('dFontSize','toolFontSize');\"><nobr>&nbsp;<b>Size</b><img src=\""+xdeeditorBasePath+"images/ocollapse.gif\">&nbsp;</nobr></td>");
		continue;
	}

	if(tmpval=="justifycenter"){ //JustifyLeft Line 	
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','JustifyCenter');\"><img src=\""+xdeeditorBasePath+"images/edit/center.gif\" alt=\"Center\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="justifyleft"){ //JustifyLeft Line 	
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','JustifyLeft');\"><img src=\""+xdeeditorBasePath+"images/edit/left.gif\" alt=\"Align Left\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="justifyright"){ //JustifyRight Font 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','JustifyRight');\"><img src=\""+xdeeditorBasePath+"images/edit/right.gif\" alt=\"Align Right\" width=16 height=16></td>");
		continue;
	}

	if(tmpval=="justifyfull"){ //JustifyFull Font 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','JustifyFull');\"><img src=\""+xdeeditorBasePath+"images/edit/justify.gif\" alt=\"Justify\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="insertunorderedlist"){ //InsertUnorderedList Font 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','InsertUnorderedList');\"><img src=\""+xdeeditorBasePath+"images/edit/bullets.gif\" alt=\"Bullets\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="insertorderedlist"){ //JustifyFull Font 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','InsertOrderedList');\"><img src=\""+xdeeditorBasePath+"images/edit/numbered.gif\" alt=\"Numbered\" width=16 height=16></td>");
		continue;
	}
		
	if(tmpval=="indent"){ //Indent Font 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','Indent');\"><img src=\""+xdeeditorBasePath+"images/edit/indent.gif\" alt=\"Increase Indent\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="outdent"){ //Outdent Font 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','Outdent');\"><img src=\""+xdeeditorBasePath+"images/edit/outdent.gif\" alt=\"Decrease Indent\" width=16 height=16></td>");
		continue;
	}

	if(tmpval=="forecolor"){ 
		document.write("<td class=\"coolButton\" id=\"toolFontcolor\" onclick=\"xde_openColorPicker('"+useIframeName+"','ForeColor');\"><img src=\""+xdeeditorBasePath+"images/edit/fontcolor.gif\" alt=\"Text Color\" width=16 height=16></td>");
		continue;
	}
	
	if(!isNS&&tmpval=="backcolor"){
		document.write("<td class=\"coolButton\" id=\"toolHighlightcolor\" onclick=\"xde_openColorPicker('"+useIframeName+"','BackColor');\"><img src=\""+xdeeditorBasePath+"images/edit/backcolor.gif\" alt=\"Highlight\" width=16 height=16></td>");
		continue;
	}


	if(tmpval=="emoticon"){ 
		document.write("<td class=\"coolButton\" onclick=\"xde_openPopUp('"+useIframeName+"','emoticons.htm','Emoticons',213,190);\"><img src=\""+xdeeditorBasePath+"images/edit/emoticon.gif\" alt=\"Insert Emoticon\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="specialchar"){ //Font size 
		document.write("<td class=\"coolButton\" id=\"toolSpecial\" onclick=\"xdeopenMenu('dSpecial','toolSpecial')\"><img src=\""+xdeeditorBasePath+"images/edit/specialchar.gif\" alt=\"Insert Extended Character\" width=16 height=16></td>");
		continue;
	}

	if(tmpval=="commonterms"){ //Font size 
		document.write("<td class=\"coolButton\" id=\"toolCommon\" onclick=\"xdeopenMenu('dcommonTerm','toolCommon')\"><nobr>&nbsp;<b>Common</b><img src=\""+xdeeditorBasePath+"images/ocollapse.gif\">&nbsp;</nobr></td>");
		continue;
	}
	if(tmpval=="inserttable"){ 
		document.write("<td class=\"coolButton\" onclick=\"xde_openPopUp('"+useIframeName+"','insertTable.htm','TableInsert',380,180);\"><img src=\""+xdeeditorBasePath+"images/edit/inserttable.gif\" alt=\"Insert Table\" width=16 height=16></td>");
		continue;
	}

	if(tmpval=="insertlink"){
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','insertlink');\"><img src=\""+xdeeditorBasePath+"images/edit/insertlink.gif\" alt=\"Insert URL\" width=16 height=16></td>");
		continue;
	}

	if(tmpval=="insertimage"){ 
		document.write("<td class=\"coolButton\" onclick=\"xde_openPopUp('"+useIframeName+"','imagepicker.htm','Images',313,290);\"><img src=\""+xdeeditorBasePath+"images/edit/insertimage.gif\" alt=\"Insert Image\" width=16 height=16></td>");
		continue;
	}
	if(tmpval=="horizontalrule"){ //Indent Font 
		document.write("<td class=\"coolButton\" onclick=\"editSetFormatting('"+useIframeName+"','inserthorizontalrule');\"><img src=\""+xdeeditorBasePath+"images/edit/horizontalrule.gif\" alt=\"Insert Horizontal Rule\" width=16 height=16></td>");
		continue;
	}

	if(tmpval=="spellcheck"){ 
		document.write("<td class=\"coolButton\" onclick=\"spellcheckIt('"+useIframeName+"');\"><img src=\""+xdeeditorBasePath+"images/edit/spellcheck.gif\" alt=\"Spell Check\" width=16 height=16></td>");
	}

	}
	document.write("<td class=\"coolButton\" onclick=\"alert('&copy; 2003, XDE Rich Text Editor. http://xde.net/');\"><img src=\""+xdeeditorBasePath+"images/edit/helpabout.gif\" alt=\"Copyright 2003, XDE Rich Text Editor\" width=16 height=16></td>");
	document.write("</tr></table></div>");
	if(htmlEditorMode){
		document.write("<div class=\"richEditStyle\"><iframe onfocus=\"xde_CloseMenus();\" src=\"blank.html\" frameborder=\"0\" name=\""+useIframeName+"\" id=\""+useIframeName+"\" class=\"richEdit\" style=\"width: 100%; height: 500; border: 1px inset;\"></iframe></div>");
	}else{
		document.write("<div class=\"richEditStyle\"><textarea onfocus=\"xde_CloseMenus();\" src=\"blank.html\" frameborder=\"0\" name=\""+useIframeName+"\" id=\""+useIframeName+"\" class=\"richEdit\" style=\"width: 100%; height: 500; border: 1px inset;\"></textarea></div>");
	}


}
function TrapKeyDown(){
    var e=window.event;  
	alert("here");
    if(e.keyCode==9)
         alert("you clicked on the tab key");

}

function writeRTEMenu(useIframeName){
	document.write("<div id=\"dFontName\" class=\"taskmenu\" style=\"padding: 7px; padding-top: 4px; width: 150px; position: absolute; z-index: 1; display:none;\" onmouseout=\"xde_closeDropdown(this);\">");
	var arr = fontNamesArray.split(",");
	for(var i = 0; i < arr.length; i++){
		var tmpvalUpperLower=arr[i];
		document.write("<a href=\"javascript:xde_editFontName('"+useIframeName+"','"+tmpvalUpperLower+"');\" class=\"tmenu\"><span style=\"font-family: "+tmpvalUpperLower+";\">"+tmpvalUpperLower+"</span></a><br>");
	}
	document.write("</div><div id=\"dFontSize\" class=\"taskmenu\" style=\"padding: 7px; padding-top: 4px; position: absolute; z-index: 1; display:none;\" onmouseout=\"xde_closeDropdown(this);\">");
	var arr = fontSizeArray.split(",");
	for(var i = 0; i < arr.length; i++){
		var tmpvalUpperLower=arr[i];
		document.write("<a href=\"javascript:xde_editFontSize('"+useIframeName+"',"+tmpvalUpperLower+");\" class=\"tmenu\"><nobr><font size=\""+tmpvalUpperLower+"\">ABCabc012...("+tmpvalUpperLower+")</font></nobr></a><br>");
	}
	document.write("</div>");
	document.write("<div id=\"dcommonTerm\" class=\"taskmenu\" style=\"padding: 7px; padding-top: 4px; position: absolute; z-index: 1; display:none;\" onmouseout=\"xde_closeDropdown(this);\">");
	var arr = commonTermArray.split(",");
	for(var i = 0; i < arr.length; i++){
		var tmpvalUpperLower=arr[i];
		document.write("<a href=\"javascript:insertHTML('"+useIframeName+"','"+tmpvalUpperLower+"');\" class=\"tmenu\"><nobr><font size=\"3\">"+tmpvalUpperLower+"</font></nobr></a><br>");
	}
	document.write("</div><div id=\"dSpecial\" class=\"taskmenu\" style=\"padding: 7px; padding-top: 4px; position: absolute; z-index: 1; display:none;\" onmouseout=\"xde_closeDropdown(this);\">");
	
	for(var i = 0; i < specialchars.length; i++){
		var tmpvalUpperLower=specialchars[i];
		document.write("<a href=\"javascript:insertHTML('"+useIframeName+"','"+tmpvalUpperLower+"');\" class=\"tmenu\"><nobr><font size=\"3\">"+tmpvalUpperLower+"</font></nobr></a><br>");
	}
	document.write("</div>");


}
//if (window.attachEvent)	// IE5
//	window.attachEvent("onload", initRichEdits)

