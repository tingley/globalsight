// Scripts for handling modal dialog boxes
// global variable settings
{
var modalFlag = false;
var dialog;
var Nav4 = ((navigator.appName == "Netscape") && (parseInt(navigator.appVersion) >= 4))
var IELinkClicks;
}
// Event handler to inhibit Navigator form element and Internet Explorer
// link activity when dialog window is active.
function deadend() {
	if (modalFlag) setTimeout('dialog.focus()',1);
	return false;
}
// Disable form elements and links in all frames for IE.
function disableForms() {
   IELinkClicks = new Array()
   for (var h = 0; h < frames.length; h++) {
      for (var i = 0; i < frames[h].document.forms.length; i++) {
         for (var j = 0; j < frames[h].document.forms[i].elements.length; j++) {
            frames[h].document.forms[i].elements[j].disabled = true
         }
      }
      IELinkClicks[h] = new Array()
      for (i = 0; i < frames[h].document.links.length; i++) {
         IELinkClicks[h][i] = frames[h].document.links[i].onclick
         frames[h].document.links[i].onclick = deadend
      }
   }
}
// Restore IE form elements and links to normal behavior.
function enableForms() {
   for (var h = 0; h < frames.length; h++) {
      for (var i = 0; i < frames[h].document.forms.length; i++) {
         for (var j = 0; j < frames[h].document.forms[i].elements.length; j++) {
            frames[h].document.forms[i].elements[j].disabled = false
         }
      }
      for (i = 0; i < frames[h].document.links.length; i++) {
         frames[h].document.links[i].onclick = IELinkClicks[h][i]
      }
   }
}
// Capture Events sent to background window prior to returning focus to dialog
function blockEvents() {
   if (Nav4) {
      window.captureEvents(Event.CLICK | Event.MOUSEDOWN | Event.MOUSEUP | Event.FOCUS)
      window.onclick = deadend
   } else {
      disableForms()
   }
   window.onfocus = keepModalOnTop();
}
// As dialog closes, restore the main window's original event mechanisms.
function unblockEvents() {
   if (Nav4) {
      window.releaseEvents(Event.CLICK | Event.MOUSEDOWN | Event.MOUSEUP | Event.FOCUS)
      window.onclick = null
      window.onfocus = null
   } else {
      enableForms()
   }
}
// Opens the dialog window
function createModalDialog(pageToOpen,w,h,xpos,ypos) {
	modalFlag = true;
	dialog = open(pageToOpen,'dialog','width=' + w + ',height=' + h + ',screenX=' + xpos + ',screenY=' + ypos + ',top=' + ypos + ',left=' + ypos);
	if (dialog.opener == null) dialog.opener = self;
}
// Maintains focus on dialog window
function keepModalOnTop() {
	if (modalFlag) if (!dialog.closed) setTimeout('dialog.focus()',1);
}
// End Dialog Window Scripts
