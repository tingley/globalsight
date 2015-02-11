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
 * fck_displaychange.js: Functions fired on the editor's change event.
 *
 * Authors:
 *   Frederico Caldeira Knabben (fckeditor@fredck.com)
 */

var oFontStyle ;
var oFontFormatCombo ;
var oFontNameCombo ;
var oFontSizeCombo ;

function checkDecCommand(cmdId)
{
    if (objContent.Busy) return OLE_TRISTATE_GRAY ;
    switch (objContent.QueryStatus(cmdId))
    {
        case (DECMDF_DISABLED || DECMDF_NOTSUPPORTED) :
            return OLE_TRISTATE_GRAY ;
        case (DECMDF_ENABLED || DECMDF_NINCHED) :
            return OLE_TRISTATE_UNCHECKED ;
        default :           // DECMDF_LATCHED
            return OLE_TRISTATE_CHECKED ;
    }
}

function checkDocCommand(command)
{
    if (objContent.Busy) return OLE_TRISTATE_GRAY ;
    return (objContent.DOM && objContent.DOM.queryCommandValue(command)) ;
}

function checkShowTableBorders()
{
    return objContent.ShowBorders ;
}

function checkShowDetails()
{
    return objContent.ShowDetails ;
}

function CheckStyle(comboName)
{
    if (oFontStyle == null) oFontStyle = document.getElementById(comboName) ;
    //CheckComboValue(oFontFormatCombo,DECMD_GETBLOCKFMT) ;
}

function CheckFontFormat(comboName)
{
    if (oFontFormatCombo == null)
    {
        oFontFormatCombo = document.getElementById(comboName) ;
    }

    // Load all available Format Block options
    if (oFontFormatCombo.options.length == 0)
    {
        if (!FCKFormatBlockNames) loadFormatBlockNames() ;

        for (var i = 0 ; i < FCKFormatBlockNames.length ; i++)
        {
            oFontFormatCombo.options[oFontFormatCombo.options.length] =
                new Option(FCKFormatBlockNames[i], FCKFormatBlockNames[i]) ;
        }
    }

    CheckComboValue(oFontFormatCombo,DECMD_GETBLOCKFMT) ;
}

function CheckFontName(comboName)
{
    if (oFontNameCombo == null)
    {
        oFontNameCombo = document.getElementById(comboName) ;
    }

    CheckComboValue(oFontNameCombo,DECMD_GETFONTNAME) ;
}

function CheckFontSize(comboName)
{
    if (oFontSizeCombo == null)
    {
        oFontSizeCombo = document.getElementById(comboName) ;
    }

    CheckComboValue(oFontSizeCombo,DECMD_GETFONTSIZE) ;
}

function CheckComboValue(combo,command)
{
    if (!combo) return ;

    var sValue = "" ;
    var s = objContent.QueryStatus(command) ;

    if (s == DECMDF_DISABLED || s == DECMDF_NOTSUPPORTED)
    {
        combo.disabled = true ;
    }
    else
    {
        combo.disabled = false ;
        sValue = objContent.ExecCommand(command, OLECMDEXECOPT_DODEFAULT);
    }
    combo.value = sValue ;
}
