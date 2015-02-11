/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003 Frederico Caldeira Knabben
 *
 * Licensed under the terms of the GNU Lesser General Public License
 * (http://www.opensource.org/licenses/lgpl-license.php)
 *
 * For further information go to http://www.fredck.com/FCKeditor/
 * or contact fckeditor@fredck.com.
 *
 * fck_editor.js: Main script that initializes the editor.
 *
 * Authors:
 *   Frederico Caldeira Knabben (fckeditor@fredck.com)
 */

var bInitialized = false;
var bDataLoaded  = false;

function initEditor()
{
    if (! bInitialized)
    {
        bInitialized = true;

        loadToolbarSet();
        loadToolbarSourceSet();

        objContent.BaseURL = config.BaseUrl;

        objContent.DocumentHTML =
            '<html xmlns:m="http://www.w3.org/1998/Math/MathML">' +
            '<head><OBJECT ID="MathPlayer" CLASSID="clsid:32F66A20-7614-11D4-BD11-00104BD3F987"></OBJECT>' +
            '<?IMPORT NAMESPACE="m" IMPLEMENTATION="#MathPlayer" ?>' +
            '</head><body></body></html>';
    }

    if (! bDataLoaded && ! objContent.Busy)
    {
        bDataLoaded = true;

        objContent.DOM.body.onpaste     = onPaste;
        objContent.DOM.body.ondrop      = onDrop;
        objContent.DOM.body.onactivate  = onActivate;

        objContent.DOM.body.onkeydown = onKeyDown;

        objContent.ShowBorders = config.StartupShowBorders;
        objContent.ShowDetails = config.StartupShowDetails;

        objContent.DOM.createStyleSheet(config.EditorAreaCSS);
        setLinkedField();
    }
}

// Method: loadToolbarSet()
// Description: Loads a toobar buttons set from an array inside the
// Toolbar holder.  Author: FredCK
function loadToolbarSet()
{
    var sToolBarSet = URLParams["Toolbar"] == null ? "Default" : URLParams["Toolbar"];

    // FredCK: Toobar holder (DIV)
    var oToolbarHolder = document.getElementById("divToolbar");

    var oToolbar = new TBToolbar();
    oToolbar.LoadButtonsSet( sToolBarSet );
    oToolbarHolder.innerHTML = oToolbar.GetHTML();
}

function loadToolbarSourceSet()
{
    // FredCK: Toobar holder (DIV)
    var oToolbarHolder = document.getElementById("divToolbarSource");

    var oToolbar = new TBToolbar();
    oToolbar.LoadButtonsSet( "Source" );
    oToolbarHolder.innerHTML = oToolbar.GetHTML();
}

function switchEditMode()
{
    var bSource = (trSource.style.display == "none");

    if (bSource)
    {
        txtSource.value = objContent.DOM.body.innerHTML;
    }
    else
    {
        objContent.DOM.body.innerHTML = "<div id=__tmpFCKRemove__>&nbsp;</div>" + txtSource.value;
        objContent.DOM.getElementById('__tmpFCKRemove__').removeNode(true);
    }

    trEditor.style.display = bSource ? "none" : "inline";
    trSource.style.display = bSource ? "inline" : "none";

    events.fireEvent('onViewMode', bSource);
}

// setValue(): called from reset() to make a select list show the
// current font or style attributes
function selValue(el, str, text)
{
    //if (!RichEditor.txtView) return;      // Disabled in View Source mode
    for (var i = 0; i < el.length; i++)
    {
        if (((text || !el[i].value) && el[i].text == str) ||
            ((!text || el[i].value) && el[i].value == str))
        {
            el.selectedIndex = i;
            return;
        }
    }
    el.selectedIndex = 0;
}

var oLinkedField = null;
function setLinkedField()
{
    if (! URLParams['FieldName']) return;

    oLinkedField = parent.document.getElementsByName(URLParams['FieldName'])[0];

    if (! oLinkedField) return;

    // __tmpFCKRemove__ added and removed to solve DHTML component
    // error when loading "<p><hr></p>"
    objContent.DOM.body.innerHTML = "<div id=__tmpFCKRemove__>&nbsp;</div>" + oLinkedField.value;
    objContent.DOM.getElementById('__tmpFCKRemove__').removeNode(true);

    var oForm = oLinkedField.form;
    if (!oForm) return;

    // Attaches the field update to the onsubmit event
    oForm.attachEvent("onsubmit", setFieldValue);

    // Attaches the field update to the submit method (IE doesn't fire
    // onsubmit on this case)
    if (! oForm.updateFCKEditor) oForm.updateFCKEditor = new Array();
    oForm.updateFCKEditor[oForm.updateFCKEditor.length] = setFieldValue;
    if (! oForm.originalSubmit)
    {
        oForm.originalSubmit = oForm.submit;
        oForm.submit = function()
        {
            if (this.updateFCKEditor)
            {
                for (var i = 0; i < this.updateFCKEditor.length; i++)
                {
                    this.updateFCKEditor[i]();
                }
            }
            this.originalSubmit();
        }
    }
}

function setFieldValue()
{
    if (trSource.style.display != "none")
    {
        switchEditMode();
    }

    if (config.EnableXHTML)
    {
        window.status = lang["ProcessingXHTML"];
        oLinkedField.value = getXhtml(objContent.DOM.body);
        window.status = 'Done';
    }
    else
        oLinkedField.value = objContent.DOM.body.innerHTML;
}

function onPaste()
{
    if (config.ForcePasteAsPlainText)
    {
        pastePlainText();
        return false;
    }
    else if (config.AutoDetectPasteFromWord && BrowserInfo.IsIE55OrMore)
    {
        var sHTML = GetClipboardHTML();
        var re = /<\w[^>]* class="?MsoNormal"?/gi;
        if (re.test(sHTML))
        {
            if (confirm(lang["PasteWordConfirm"]))
            {
                cleanAndPaste( sHTML );
                return false;
            }
        }
    }
    else
    {
        return true;
    }
}

function onDrop()
{
    if (config.ForcePasteAsPlainText)
    {
        var sText = HTMLEncode(objContent.DOM.parentWindow.event.dataTransfer.getData("Text"));
        sText = sText.replace(/\n/g,'<BR>');
        insertHtml(sText);
        return false;
    }
    else if (config.AutoDetectPasteFromWord && BrowserInfo.IsIE55OrMore)
    {
        // TODO
        // To find a way to get the HTML that is dropped,
        // clean it and insert it into the document.
        return true;
    }
    else
    {
        return true;
    }
}

function onKeyDown()
{
    var oWindow = objContent.DOM.parentWindow;

    if (oWindow.event.ctrlKey || oWindow.event.altKey || oWindow.event.shiftKey)
    {
        oWindow.event.returnValue = true;
        return;
    }

    if (oWindow.event.keyCode == 9 && config.TabSpaces > 0) // added tab support
    {
        var sSpaces = "";
        for ( i = 0; i < config.TabSpaces; i++ )
            sSpaces += "&nbsp;";
        insertHtml( sSpaces );
    }
    else if (oWindow.event.keyCode == 13 && config.UseBROnCarriageReturn)
    {
        if (objContent.DOM.queryCommandState('InsertOrderedList') ||
            objContent.DOM.queryCommandState('InsertUnorderedList'))
        {
            oWindow.event.returnValue = true;
            return;
        }

        insertHtml("<br>&nbsp;");

        var oRange = objContent.DOM.selection.createRange();
        oRange.moveStart('character',-1);
        oRange.select();
        objContent.DOM.selection.clear();

        oWindow.event.returnValue = false;
    }
}

function onActivate()
{
    var oWindow = objContent.DOM.parentWindow;
    var ctrl = oWindow.event.srcElement;

    while (ctrl && !ctrl.formula)
    {
        ctrl = ctrl.parentElement;
    }

    if (ctrl && ctrl.tagName == "SPAN" && ctrl.formula)
    {
        oWindow.event.cancelBubble = true;
        oWindow.event.returnValue = false;
        dialogEquation();
    }

    return false;
}
