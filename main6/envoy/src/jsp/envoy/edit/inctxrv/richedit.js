/*----------------------------------------------------------------------------\
|                           Rich Text Editor 1.14                             |
|-----------------------------------------------------------------------------|
|                         Created by Erik Arvidsson                           |
|                  (http://webfx.eae.net/contact.html#erik)                   |
|                      For WebFX (http://webfx.eae.net/)                      |
|-----------------------------------------------------------------------------|
| A rich text editor (WYSIWYG) for Internet Explorer 4.0 (Win32/Unix) and up  |
|-----------------------------------------------------------------------------|
|                  Copyright (c) 1999 - 2002 Erik Arvidsson                   |
|-----------------------------------------------------------------------------|
| This software is provided "as is", without warranty of any kind, express or |
| implied, including  but not limited  to the warranties of  merchantability, |
| fitness for a particular purpose and noninfringement. In no event shall the |
| authors or  copyright  holders be  liable for any claim,  damages or  other |
| liability, whether  in an  action of  contract, tort  or otherwise, arising |
| from,  out of  or in  connection with  the software or  the  use  or  other |
| dealings in the software.                                                   |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| This  software is  available under the  three different licenses  mentioned |
| below.  To use this software you must chose, and qualify, for one of those. |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Non-Commercial License          http://webfx.eae.net/license.html |
| Permits  anyone the right to use the  software in a  non-commercial context |
| free of charge.                                                             |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Commercial license           http://webfx.eae.net/commercial.html |
| Permits the  license holder the right to use  the software in a  commercial |
| context. Such license must be specifically obtained, however it's valid for |
| any number of  implementations of the licensed software.                    |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| GPL - The GNU General Public License    http://www.gnu.org/licenses/gpl.txt |
| Permits anyone the right to use and modify the software without limitations |
| as long as proper  credits are given  and the original  and modified source |
| code are included. Requires  that the final product, software derivate from |
| the original  source or any  software  utilizing a GPL  component, such  as |
| this, is also licensed under the GPL license.                               |
|-----------------------------------------------------------------------------|
| 2000-??-?? |
| 2001-12-02 | (1.1) Added getXHTML, supportsXHTML, usebr, oneditinit, fix    |
|            | for no selection, bUI flag to execCommand method               |
| 2001-12-03 | Fixed case of HTML node names to lowercase                     |
| 2002-07-13 | Fixed security error when reloading page in IE. Also updated   |
|            | empty doc template to use IE CSS1 rendering mode.              |
| 2002-09-03 | Added getRange and surroundSelection                           |
| 2002-10-03 | Separated Get XHTML functionality into separate file. XHTML    |
|            | changes can be found in the getxhtml.js file.                  |
|-----------------------------------------------------------------------------|
| Created 2000-??-?? | All changes are in the log above. | Updated 2002-10-03 |
\----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------\
| To use getXHTML you need to include the files stringbuilder as well as the  |
| file getxhtml.js before this file. These can be found at WebFX.             |
\----------------------------------------------------------------------------*/
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
function initRichEdit(el)
{
    // needs an id to be accessible in the frames collection
	var status = "false";
	if(el)
	{
		if(el.id)
		{
			status = "true";
		}
	}
    if (status=="true")
    {
        if(window.frames[el.id])
		{
			el.frameWindow = window.frames[el.id];
		}
		else
		{
			el.frameWindow = document.getElementById(el.id);
		}

        if (el.formattingKeys == null)
            el.formattingKeys = true;

        if (el.verbosePTags == null)
            el.verbosePTags = false;

        if (el.value == null)
            el.value = el.innerHTML;

        if (el.dir == null)
            el.dir = "ltr";

        if (el.ptagColor == null)
            el.ptagColor = "#3366FF";

        // if (el.value.replace(/\s/g, "") == "" )
        //var tempval = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
        //    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
        var tempval = "<html dir='" + el.dir + "'>\n" +
            "<head><style>\n" +
            /* .ptag must be first style for brighten/dim */
            ".ptag { color: " + el.ptagColor + "; }\n" +
            "#idBody { font-family:Arial, Helvetica, sans-serif; " +
            "  font-size: 10pt; word-wrap: break-word; margin: 1px; }\n" +
            "</style></head>" +
            "<body id=idBody>" + el.value + "</body></html>";

        //el.src = "about:blank";
        if(el.frameWindow.document)
		{
			var d = el.frameWindow.document;
		}
		else
		{
			var d = document.getElementById(el.id).contentWindow.document;
		}
        d.open();
        d.write(tempval);
        d.close();
        d.designMode = "On";

       el.supportsXHTML = (document.getElementById(el.id).contentWindow.document.documentElement) &&
            (document.getElementById(el.id).contentWindow.document.childNodes != null);

        // set up the expandomethods

        // first some basic
        el.setHTML = function (sHTML) {
            el.value = sHTML;
            initRichEdit(el);
        }

        el.getHTML = function () {
            // notice that IE4 cannot get the document.documentElement
            // so we'll use the body
            //return el.frameWindow.document.body.innerHTML;
            // for IE5 the following is much better. If you don't want
            // IE4 compatibilty modify this
            if(el.frameWindow.document)
			{
				return el.frameWindow.document.body.innerHTML;
			}
			else
			{
				return document.getElementById(el.id).contentWindow.document.body.innerHTML;
			}
        }

        el.getXHTML = function () {
            if (!el.supportsXHTML) {
                alert("Document root node cannot be accessed in IE4.x");
                return;
            }
            else if (typeof window.StringBuilder != "function") {
                alert("StringBuilder is not defined. Make sure to include stringbuilder.js");
                return;
            }

            var sb = new StringBuilder;

            // IE5 and IE55 has trouble with the document node

           if(el.frameWindow)
			{
				 var cs = el.frameWindow.document.body.childNodes;
			}
			else
			{
				 var cs = document.getElementById(el.id).contentWindow.document.body.childNodes;
			}

            var l = cs.length;

            for (var i = 0; i < l; i++)
                _appendNodeXHTML(cs[i], sb);

            return sb.toString();
        };

        el.setText = function (sText) {
            el.value = sText.replace(/\&/g, "&amp;").replace(/\</g, "&lt;").replace(/\>/g, "&gt;").replace(/\n/g, "<br>");
            initRichEdit(el);
        }

        el.getText = function () {
            // notice that IE4 cannot get the document.documentElement
            // so we'll use the body
            // not that it matters when it comes to innerText.
            if(document.recalc)
			{
				return el.frameWindow.document.body.innerText;
			}
			else
			{
				return document.getElementById(el.id).contentWindow.document.body.textContent;
			}
        }

        // and now some text manipulations

        el.execCommand = function (execProp, execVal, bUI) {
            return execCommand(this, execProp, execVal, bUI);
        }

        el.surroundSelection = function(sBefore, sAfter) {
            var r = this.getRange();
            if (isIE)
            {
				r.pasteHTML(sBefore + r.htmlText + sAfter);
            }
			else
			{
				var oFragment = r.createContextualFragment(sBefore + r + sAfter); 
				r.deleteContents();
				r.insertNode(oFragment); 
			}
        };

        el.getRange = function () {
            el.focus();
            if(isIE)
			{
				var doc = this.frameWindow.document;
			}
            else
			{
				var doc = document.getElementById(el.id).contentWindow.document;
			}
            if(doc.selection)
			{
				var r = doc.selection.createRange();
			}
			else
			{
				var selection = document.getElementById(el.id).contentWindow.getSelection().getRangeAt(0);
			}
            if (isIE){
            	return r;
            }else{
            	return selection;
            }
            // can happen in IE55+
            return null;
        };

        el.insertText = function (text) {
            el.focus();
           	if(document.recalc)
			{
				var sel = el.frameWindow.document.selection;
				if (sel.type == "Control")
                	return;
            	var r = sel.createRange();
            	r.pasteHTML(text);
            	r.select();
            	r.moveEnd("character", 1);
           	 	r.moveStart("character", 1);
            	r.collapse(false);
            	return false;
			}
			else
			{
				var sel = document.getElementById(el.id).contentWindow.getSelection();
				var oNewRange = sel.getRangeAt(sel.rangeCount - 1).cloneRange();
				var oFragment = oNewRange.createContextualFragment(text); 
				oNewRange.deleteContents();
				oNewRange.insertNode(oFragment); 
				return false;
			}
        }

        el.insertHTML = function (html) {
            el.focus();
            if(document.recalc)
			{
				var sel = el.frameWindow.document.selection;
				if (sel.type == "Control")
                	return;

           		var r = sel.createRange();
            	r.pasteHTML(html);
            	r.select();
            	r.moveEnd("character", 1);
            	r.moveStart("character", 1);
            	r.collapse(false);
				return false;
			}
            else
			{
				var sel = document.getElementById(el.id).contentWindow.getSelection();
				var oNewRange = sel.getRangeAt(sel.rangeCount - 1);
				var oFragment = oNewRange.createContextualFragment(html); 
				oNewRange.deleteContents();
				oNewRange.insertNode(oFragment); 
				return false;
			}
           
        }

        el.setPtagColor = function (sArg) {
            el.ptagColor = sArg;

           	if(document.recalc)
			{
				 var css = el.frameWindow.document.styleSheets.item(0);
			}
            else
			{
				var css = document.getElementById(el.id).contentWindow.document.styleSheets.item(0);
			}
            if (!document.recalc) 
			{
				try {var rule =  css.cssRules[0]; } catch (e) {};
            }
            else 
			{
				var rule = css.rules[0];
				rule.style.color = sArg;
            }
        }

        el.insertPTag = function (ptag) {
            return el.insertHTML(getPTag(ptag));
        }

        el.addBold = function () {
            return this.insertPTag(this.verbosePTags ? "[bold]" : "[b]");
        }

        el.addEndBold = function () {
            return this.insertPTag(this.verbosePTags ? "[/bold]" : "[/b]");
        }

        el.addItalic = function () {
            return this.insertPTag(this.verbosePTags ? "[italic]" : "[i]");
        }

        el.addEndItalic = function () {
            return this.insertPTag(this.verbosePTags ? "[/italic]" : "[/i]");
        }

        el.addUnderline = function () {
            return this.insertPTag(this.verbosePTags ? "[underline]" : "[u]");
        }

        el.addEndUnderline = function () {
            return this.insertPTag(this.verbosePTags ? "[/underline]" : "[/u]");
        }

        el.addNbsp = function () {
            return this.insertPTag("[nbsp]");
        }

        el.addBr = function () {
            return this.insertPTag(this.verbosePTags ? "[break]" : "[br]");
        }

        el.addLre = function () {
            return this.insertText("&lre;");
        }

        el.addPdf = function () {
            return this.insertText("&pdf;");
        }

        el.makeBold = function () {
            this.surroundSelection(
                getPTag(this.verbosePTags ? "[bold]"  : "[b]"),
                getPTag(this.verbosePTags ? "[/bold]" : "[/b]"));
        }
        
        el.makeSub = function () {
            this.surroundSelection(
                getPTag(this.verbosePTags ? "[subscript]"  : "[sub]"),
                getPTag(this.verbosePTags ? "[/subscript]" : "[/sub]"));
        }
        
        el.makeSup = function () {
            this.surroundSelection(
                getPTag(this.verbosePTags ? "[superscript]"  : "[sup]"),
                getPTag(this.verbosePTags ? "[/superscript]" : "[/sup]"));
        }

        el.makeItalic = function () {
            this.surroundSelection(
                getPTag(this.verbosePTags ? "[italic]"  : "[i]"),
                getPTag(this.verbosePTags ? "[/italic]" : "[/i]"));
        }

        el.makeUnderline = function () {
            this.surroundSelection(
                getPTag(this.verbosePTags ? "[underline]"  : "[u]"),
                getPTag(this.verbosePTags ? "[/underline]" : "[/u]"));
        }

        el.addCR = function () {
            return this.insertText("\\\<br>");
        }

        el.addBrackets = function () {
            this.surroundSelection(getPTag("[["),getPTag("]"));
        }
/* -------------------------------------
        el.setBold = function () {
            return this.execCommand("bold");
        }

        el.setItalic = function () {
            return this.execCommand("italic");
        }

        el.setUnderline = function () {
            return this.execCommand("underline");
        }

        el.setBackgroundColor = function(sColor) {
            return this.execCommand("backcolor", sColor);
        }

        el.setColor = function(sColor) {
            return this.execCommand("forecolor", sColor);
        }
------------------------------------- */

        el.enableFormattingKeys = function(flag) {
            el.formattingKeys = flag;
        };

        el.setVerbosePTags = function(flag) {
            el.verbosePTags = flag;
        };
        if (window.attachEvent) // IE5
        {
          document.getElementById(el.id).contentWindow.document.onkeydown = function ()
          {
            if(el.frameWindow.event)
			{
				var event = el.frameWindow.event;
			}
			else
			{
				var event = getEvent();
			}
            var doc = document.getElementById(el.id).contentWindow.document;
            var key = event.keyCode;
            //alert(key);

            if (event.ctrlKey && !event.altKey)
            {
              if (key == 66) // "B"
              {
                event.cancelBubble = true;
                event.returnValue = false;
                if (el.formattingKeys) el.makeBold();
                return;
              }
              else if (key == 73) // "I"
              {
                event.cancelBubble = true;
                event.returnValue = false;
                if (el.formattingKeys) el.makeItalic();
                return;
              }
              else if (key == 85) // "U"
              {
                event.cancelBubble = true;
                event.returnValue = false;
                if (el.formattingKeys) el.makeUnderline();
                return;
              }
              else if (key == 56) // "8"
              {
            	  event.cancelBubble = true;
                  event.returnValue = false;
                  el.makeSub();
                  return;
              }
              else if (key == 57) // "9"
              {
            	  event.cancelBubble = true;
                  event.returnValue = false;
                  el.makeSup();            	
              	  return;
              }
            }

            // keys with ctrl modifier
            if (event.ctrlKey && !event.altKey)
            {
              if (key == 81) // "Q"
              {
                event.cancelBubble = true;
                event.returnValue = false;
                // doc.designMode = "Inherit";
                parent.parent.doClose();
                return;
              }
              else if (key == 83) // "S"
              {
                event.cancelBubble = true;
                event.returnValue = false;
                // doc.designMode = "Inherit";
                parent.parent.doRefresh(0, true);
                return;
              }
              else if (key == 33) // PAGE UP
              {
                event.cancelBubble = true;
                event.returnValue = false;
                // doc.designMode = "Inherit";
                parent.parent.doRefresh(-1, false);
                return;
              }
              else if (key == 34) // PAGE DOWN
              {
                event.cancelBubble = true;
                event.returnValue = false;
                // doc.designMode = "Inherit";
                parent.parent.doRefresh(1, false);
                return;
              }
            }

            // keys with any modifier (ctrl, alt)
            if (key == 13) {    // ENTER
                event.cancelBubble = true;
                event.returnValue = false;

                el.insertHTML("<BR>");

                return false;
            }
        };
       }
       else 
       {
        document.getElementById(el.id).contentWindow.document.addEventListener("keydown", function () {
            if(el.frameWindow.event)
			{
				var event = el.frameWindow.event;
			}
			else
			{
				var event = getEvent();
			}
            var doc = document.getElementById(el.id).contentWindow.document;
            var key = event.keyCode;
            //alert(key);

            if (event.ctrlKey && !event.altKey)
            {
              if (key == 66) // "B"
              {
                event.stopPropagation();
                event.preventDefault();
                if (el.formattingKeys) el.makeBold();
                return;
              }
              else if (key == 73) // "I"
              {
                event.stopPropagation();
                event.preventDefault();
                if (el.formattingKeys) el.makeItalic();
                return;
              }
              else if (key == 85) // "U"
              {
                event.stopPropagation();
                event.preventDefault();
                if (el.formattingKeys) el.makeUnderline();
                return;
              }
              else if (key == 56) // "8"
              {
            	  event.stopPropagation();
                  event.preventDefault();
                  el.makeSub();
                  return;
              }
              else if (key == 57) // "9"
              {
            	  event.stopPropagation();
                  event.preventDefault();
                  el.makeSup();            	
              	  return;
              }
            }

            // keys with ctrl modifier
            if (event.ctrlKey && !event.altKey)
            {
              if (key == 81) // "Q"
              {
                event.stopPropagation();
                event.preventDefault();
                // doc.designMode = "Inherit";
                parent.parent.doClose();
                return;
              }
              else if (key == 83) // "S"
              {
                event.stopPropagation();
                event.preventDefault();
                // doc.designMode = "Inherit";
                parent.parent.doRefresh(0, true);
                return;
              }
              else if (key == 33) // PAGE UP
              {
                event.stopPropagation();
                event.preventDefault();
                // doc.designMode = "Inherit";
                parent.parent.doRefresh(-1, false);
                return;
              }
              else if (key == 34) // PAGE DOWN
              {
                event.stopPropagation();
                event.preventDefault();
                // doc.designMode = "Inherit";
                parent.parent.doRefresh(1, false);
                return;
              }
            }

            // keys with any modifier (ctrl, alt)
            if (key == 13) {    // ENTER
                event.stopPropagation();
                event.preventDefault();

                el.insertHTML("<BR>");

                return false;
            }
        },false);
        }
        document.getElementById(el.id).contentWindow.document.onkeypress =
        document.getElementById(el.id).contentWindow.document.onkeyup = function () {
           if(el.frameWindow.event)
			{
				var event = el.frameWindow.event;
			}
			else
			{
				var event = getEvent();
			}
            if (event.keyCode == 13)    // ENTER
            {
                event.cancelBubble = true;
                event.returnValue = false;
                return false;
            }
        };

        // Add your own or use the execCommand method.
        // See msdn.microsoft.com for commands.

        // Can't overwrite focus in IE 5.00 or 5.01
        try
        {
            el.focus = function () {
              document.getElementById(el.id).contentWindow.focus();
            }
        }
        catch (e) {}

        // call oneditinit if defined
        if (typeof(el.oneditinit) == "string")
            el.oneditinit = new Function(el.oneditinit);
        if (typeof(el.oneditinit) == "function")
            el.oneditinit();
    }

    function execCommand(el, execProp, execVal, bUI)
    {
        if(document.recalc)
		{
			var doc = el.frameWindow.document;
			var type = doc.selection.type;
        	var oTarget = type == "None" ? doc : doc.selection.createRange();
			var r = oTarget.execCommand(execProp, bUI, execVal);
        	if (type == "Text") oTarget.select();
        	return r;
		}
		else
		{
			var doc = document.getElementById(el.id).contentWindow.document;
			var r = doc.execCommand(execProp, bUI, execVal);
			return r;
		}
    }
}
function getEvent(){     //ͬʱ����ie��ff��д��
         if(document.all)    return window.event;        
         func=getEvent.caller;            
         while(func!=null){    
             var arg0=func.arguments[0];
             if(arg0){
                 if((arg0.constructor==Event || arg0.constructor ==MouseEvent)
                     || (typeof(arg0)=="object" && arg0.preventDefault && arg0.stopPropagation)){    
                     return arg0;
                 }
             }
             func=func.caller;
         }
         return null;
}
function getPTag(ptag)
{
    return "<SPAN DIR=ltr class=ptag UNSELECTABLE=on CONTENTEDITABLE=true>" +
        ptag + "</SPAN>";
}

function _appendNodeXHTML(node, sb)
{

    function fixAttribute(s) {
        return String(s).replace(/\&/g, "&amp;").replace(/>/g, "&gt;").replace(/</g, "&lt;").replace(/\"/g, "&quot;");
    }

    function fixText(s) {
        return String(s).replace(/\&/g, "&amp;").replace(/>/g, "&gt;").replace(/</g, "&lt;");
    }

    switch (node.nodeType) {

        case 1: // ELEMENT

            if (node.nodeName == "!") { // IE5.0 and IE5.5 are weird
                sb.append(node.text);
                break;
            }

            var name = node.nodeName;
            if (node.scopeName == "HTML")
                name = name.toLowerCase();

            sb.append("<" + name);

            // attributes
            var attrs = node.attributes;
            var l = attrs.length;
            for (var i = 0; i < l; i++) {
                if (attrs[i].specified) {
                    if (attrs[i].nodeName != "style")
                        sb.append(" " + attrs[i].nodeName + "=\"" + fixAttribute(attrs[i].nodeValue) + "\"");
                    else
                        sb.append(" style=\"" + fixAttribute(node.style.cssText) + "\"");
                }
            }

            if (node.canHaveChildren || node.hasChildNodes()) {

                sb.append(">");

                // childNodes
                var cs = node.childNodes;
                l = cs.length;
                for (var i = 0; i < l; i++)
                    _appendNodeXHTML(cs[i], sb);

                sb.append("</" + name + ">");
            }
            else if (name == "script")
                sb.append(">" + node.text + "</" + name + ">");
            else if (name == "title" || name == "style" || name == "comment")
                sb.append(">" + node.innerHTML + "</" + name + ">");
            else
                sb.append(" />");

            break;

        case 3: // TEXT
            sb.append(fixText(node.nodeValue));
            break;

        case 4:
            sb.append("<![CDA" + "TA[\n" + node.nodeValue + "\n]" + "]>");
            break;

        case 8:
            //sb.append("<!--" + node.nodeValue + "-->");
            sb.append(node.text);
            if (/(^<\?xml)|(^<\!DOCTYPE)/.test(node.text) )
                sb.append("\n");
            break;

        case 9: // DOCUMENT
            // childNodes
            var cs = node.childNodes;
            l = cs.length;
            for (var i = 0; i < l; i++)
                _appendNodeXHTML(cs[i], sb);
            break;

        default:
            sb.append("<!--\nNot Supported:\n\n" +
                "nodeType: " + node.nodeType +
                "\nnodeName: " + node.nodeName + "\n-->");
    }
}
      
function initAllRichEdits()
{
   var iframes = document.getElementsByTagName("IFRAME");
	//var iframes = document.all.tags("IFRAME");

    for (var i=0; i<iframes.length; i++)
    {
        if (iframes[i].className == "richEdit")
            initRichEdit(iframes[i]);
    }
}


if (window.attachEvent) // IE5
{
    window.attachEvent("onload", initAllRichEdits)
}
else if (document.all)  // IE4
{
    window.onload = initAllRichEdits;
}
else 
{
	window.addEventListener("load", initAllRichEdits, false);
}
