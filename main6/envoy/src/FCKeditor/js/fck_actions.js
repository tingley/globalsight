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
 * fck_actions.js: Actions called by the toolbar.
 *
 * Authors:
 *   Frederico Caldeira Knabben (fckeditor@fredck.com)
 */

function SetFocus()
{
    if (BrowserInfo.IsIE55OrMore)
        objContent.DOM.focus() ;
    else
        objContent.focus() ;
}

function decCommand(cmdId, cmdExecOpt, url)
{
    var status = objContent.QueryStatus(cmdId) ;

    if ( status != DECMDF_DISABLED && status != DECMDF_NOTSUPPORTED )
    {
        if (cmdExecOpt == null) cmdExecOpt = OLECMDEXECOPT_DODEFAULT ;
        objContent.ExecCommand(cmdId, cmdExecOpt, url) ;
    }
    SetFocus() ;
}

function docCommand(command)
{
    objContent.DOM.execCommand(command) ;
    SetFocus();
}

function doStyle(command)
{
    var oSelection = objContent.DOM.selection ;
    var oTextRange = oSelection.createRange() ;

    if (oSelection.type == "Text")
    {
        decCommand(DECMD_REMOVEFORMAT);
        if (!FCKFormatBlockNames) loadFormatBlockNames() ;
        doFormatBlock( FCKFormatBlockNames[0] );    // This value is loaded at CheckFontFormat()

        var oFont = document.createElement("FONT") ;
        oFont.innerHTML = oTextRange.htmlText ;

        var oParent = oTextRange.parentElement() ;
        var oFirstChild = oFont.firstChild ;

        if (oFirstChild.nodeType == 1 &&
            oFirstChild.outerHTML == oFont.innerHTML &&
            (oFirstChild.tagName == "SPAN" ||
             oFirstChild.tagName == "FONT" ||
             oFirstChild.tagName == "P" ||
             oFirstChild.tagName == "DIV"))
        {
            oParent.className = command.value ;
        }
        else
        {
            oFont.className = command.value ;
            oTextRange.pasteHTML( oFont.outerHTML ) ;
        }
    }
    else if (oSelection.type == "Control" && oTextRange.length == 1)
    {
        var oControl = oTextRange.item(0) ;
        oControl.className = command.value ;
    }

    command.selectedIndex = 0 ;

    SetFocus();
}

function doFormatBlock(combo)
{
    if (combo.value == null || combo.value == "")
    {
        if (!FCKFormatBlockNames) loadFormatBlockNames() ;
        {
            objContent.ExecCommand(DECMD_SETBLOCKFMT,
                OLECMDEXECOPT_DODEFAULT, FCKFormatBlockNames[0]);
        }
    }
    else
    {
        objContent.ExecCommand(DECMD_SETBLOCKFMT,
            OLECMDEXECOPT_DODEFAULT, combo.value);
    }

    SetFocus();
}

function doFontName(combo)
{
    if (combo.value == null || combo.value == "")
    {
        // TODO: Remove font name attribute.
    }
    else
    {
        objContent.ExecCommand(DECMD_SETFONTNAME,
            OLECMDEXECOPT_DODEFAULT, combo.value);
    }

    SetFocus();
}

function doFontSize(combo)
{
    if (combo.value == null || combo.value == "")
    {
        // TODO: Remove font size attribute (Now it works with size 3.
        // Will it work forever?)
        objContent.ExecCommand(DECMD_SETFONTSIZE, OLECMDEXECOPT_DODEFAULT, 3);
    }
    else
        objContent.ExecCommand(DECMD_SETFONTSIZE,
            OLECMDEXECOPT_DODEFAULT, parseInt(combo.value));

    SetFocus();
}

function dialogImage()
{
    var html = FCKShowDialog("dialog/fck_image.html", window, 400, 380);
    // The response is the IMG tag HTML
    if (html) insertHtml(html) ;
    SetFocus() ;
}

function dialogTable(searchParentTable)
{
    if (searchParentTable)
    {
        var oRange  = objContent.DOM.selection.createRange() ;
        var oParent = oRange.parentElement() ;

        while (oParent && oParent.nodeName != "TABLE")
        {
            oParent = oParent.parentNode ;
        }

        if (oParent && oParent.nodeName == "TABLE")
        {
            var oControlRange = objContent.DOM.body.createControlRange();
            oControlRange.add( oParent ) ;
            oControlRange.select() ;
        }
        else
        {
            return ;
        }
    }

    FCKShowDialog("dialog/fck_table.html", window, 350, 210);
    SetFocus() ;
}

function dialogTableCell()
{
    FCKShowDialog("dialog/fck_tablecell.html", window, 500, 220);
    SetFocus() ;
}

function dialogLink()
{
    if (checkDecCommand(DECMD_HYPERLINK) != OLE_TRISTATE_GRAY)
    {
        // CvdL: distinguish normal links from termbase links
        var oRange  = objContent.DOM.selection.createRange() ;
        var oParent = oRange.parentElement() ;

        while (oParent && oParent.nodeName != "A")
        {
            oParent = oParent.parentNode;
        }

        if (oParent && oParent.nodeName == "A" &&
            //oParent.href == 'javascript:void(0)'
            oParent.className == 'xref')
        {
            showDialogTermLink();
        }
        else
        {
            showDialogLink();
        }
    }
}

function dialogTermLink()
{
    if (checkDecCommand(DECMD_HYPERLINK) != OLE_TRISTATE_GRAY)
    {
        // CvdL: distinguish normal links from termbase links
        var oRange  = objContent.DOM.selection.createRange() ;
        var oParent = oRange.parentElement() ;

        while (oParent && oParent.nodeName != "A")
        {
            oParent = oParent.parentNode;
        }

        if (oParent && oParent.nodeName == "A" &&
            //oParent.href == 'javascript:void(0)'
            oParent.className != 'xref')
        {
            showDialogLink();
        }
        else
        {
            showDialogTermLink();
        }
    }
}

function showDialogLink()
{
    FCKShowDialog("dialog/fck_link.html", window, 400, 190);
    SetFocus();
}

function showDialogTermLink()
{
    FCKShowDialog("dialog/fck_termlink.html", window, 400, 300);
    SetFocus() ;
}

function dialogEquation(formula)
{
    var html = FCKShowDialog("dialog/fck_equation.html", window, 400, 300);
    if (html) insertHtml(html);
    SetFocus();
}

// insertHtml(): Insert HTML at the current document position.
function insertHtml(html)
{
    if (objContent.DOM.selection.type.toLowerCase() != "none")
        objContent.DOM.selection.clear() ;
    objContent.DOM.selection.createRange().pasteHTML(html) ;
    SetFocus() ;
}

function foreColor()
{
    var color = FCKShowDialog("dialog/fck_selcolor.html", window, 370, 240);
    if (color) objContent.ExecCommand(DECMD_SETFORECOLOR,OLECMDEXECOPT_DODEFAULT, color) ;
    SetFocus();
}

function backColor()
{
    var color = FCKShowDialog("dialog/fck_selcolor.html", window, 370, 240);
    if (color) objContent.ExecCommand(DECMD_SETBACKCOLOR,OLECMDEXECOPT_DODEFAULT, color) ;
    SetFocus();
}

function insertSpecialChar()
{
    var html = FCKShowDialog("dialog/fck_specialchar.html", window, 400, 250);
    if (html) insertHtml(html) ;
    SetFocus() ;
}

function insertSmiley()
{
    var html = FCKShowDialog("dialog/fck_smiley.html", window, config.SmileyWindowWidth, config.SmileyWindowHeight) ;
    if (html) insertHtml(html) ;
    SetFocus() ;
}

function FCKShowDialog(pagePath, args, width, height)
{
    return showModalDialog(pagePath, args, "dialogWidth:" + width + "px;dialogHeight:" + height + "px;help:no;scroll:no;status:no");
}

function about()
{
    FCKShowDialog("dialog/fck_about.html", window, 460, 290);
}

function pastePlainText()
{
    var sText = HTMLEncode( clipboardData.getData("Text") ) ;
    sText = sText.replace(/\n/g,'<BR>') ;
    insertHtml(sText) ;
}

function pasteFromWord()
{
    if (BrowserInfo.IsIE55OrMore)
        cleanAndPaste( GetClipboardHTML() ) ;
    else if ( confirm( lang["NotCompatiblePaste"] ) )
        decCommand(DECMD_PASTE) ;
}

function cleanAndPaste( html )
{
    // Remove all SPAN tags
    html = html.replace(/<\/?SPAN[^>]*>/gi, "" );
    // Remove Class attributes
    html = html.replace(/<(\w[^>]*) class=([^ |>]*)([^>]*)/gi, "<$1$3") ;
    // Remove Style attributes
    html = html.replace(/<(\w[^>]*) style="([^"]*)"([^>]*)/gi, "<$1$3") ; //"
    // Remove Lang attributes
    html = html.replace(/<(\w[^>]*) lang=([^ |>]*)([^>]*)/gi, "<$1$3") ;
    // Remove XML elements and declarations
    html = html.replace(/<\\?\?xml[^>]*>/gi, "") ;
    // Remove Tags with XML namespace declarations: <o:p></o:p>
    html = html.replace(/<\/?\w+:[^>]*>/gi, "") ;
    // Replace the &nbsp;
    html = html.replace(/&nbsp;/, " " );
    // Transform <P> to <DIV>
    var re = new RegExp("(<P)([^>]*>.*?)(<\/P>)","gi") ;    // Different because of a IE 5.0 error
    html = html.replace( re, "<div$2</div>" ) ;

    insertHtml( html ) ;
}

function GetClipboardHTML()
{
    var oDiv = document.getElementById("divTemp")
    oDiv.innerHTML = "" ;

    var oTextRange = document.body.createTextRange() ;
    oTextRange.moveToElementText(oDiv) ;
    oTextRange.execCommand("Paste") ;

    var sData = oDiv.innerHTML ;
    oDiv.innerHTML = "" ;

    return sData ;
}

function HTMLEncode(text)
{
    text = text.replace(/&/g, "&amp;") ;
    text = text.replace(/"/g, "&quot;") ; //"
    text = text.replace(/</g, "&lt;") ;
    text = text.replace(/>/g, "&gt;") ;
    text = text.replace(/'/g, "&#146;") ; //'

    return text ;
}

function showTableBorders()
{
    objContent.ShowBorders = !objContent.ShowBorders ;
    SetFocus() ;
}

function showDetails()
{
    objContent.ShowDetails = !objContent.ShowDetails ;
    SetFocus() ;
}

var FCKFormatBlockNames ;

function loadFormatBlockNames()
{
    var oNamesParm = new ActiveXObject("DEGetBlockFmtNamesParam.DEGetBlockFmtNamesParam") ;
    objContent.ExecCommand(DECMD_GETBLOCKFMTNAMES, OLECMDEXECOPT_DODEFAULT, oNamesParm);
    var vbNamesArray = new VBArray(oNamesParm.Names) ;

    FCKFormatBlockNames = vbNamesArray.toArray() ;
}

function doZoom( sizeCombo )
{
    if (sizeCombo.value != null || sizeCombo.value != "")
        objContent.DOM.body.runtimeStyle.zoom = sizeCombo.value + "%" ;
}

function insertList( type )
{
    var oDoc = objContent.DOM ;
    if ( !config.UseBROnCarriageReturn || oDoc.queryCommandState( 'InsertOrderedList' ) || oDoc.queryCommandState( 'InsertUnorderedList' ) )
    {
        if ( type == 'ul' )
            decCommand( DECMD_UNORDERLIST ) ;
        else
            decCommand( DECMD_ORDERLIST ) ;
    }
    else
    {
        insertHtml('<' + type + '><li id="____tempLI">.</li></' + type + '>') ;

        var oLI = oDoc.getElementById( '____tempLI' ) ;
        oLI.removeAttribute("id") ;

        var oRange = oDoc.selection.createRange() ;
        oRange.moveToElementText( oLI ) ;
        oRange.findText( '.' ) ;
        oRange.select() ;
        oDoc.selection.clear() ;
    }
}

//function to perform spell check
function SpellCheck()
{
    try
    {
        var tmpis = new ActiveXObject( "ieSpell.ieSpellExtension" ) ;
        tmpis.CheckAllLinkedDocuments( objContent );
    }
    catch (exception)
    {
        if ( exception.number == -2146827859 )
        {
            if ( confirm( "Spell checker not installed. Do you want to download it now?" ) )
                window.open( config.SpellCheckerDownloadUrl , "SpellCheckerDownload" );
        }
        else
            alert("Error Loading ieSpell: Exception " + exception.number);
    }
}
