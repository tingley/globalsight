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
 * fck_toolbar.js: Creates and handles the toolbar.
 *
 * Authors:
 *   Frederico Caldeira Knabben (fckeditor@fredck.com)
 */

//##
//## Command Type Enum
//##
TBCMD_DEC       = 0 ;
TBCMD_DOC       = 1 ;
TBCMD_CUSTOM    = 2 ;

// -----------------------------------------------------------------
// -- TBToolbar class - The Main (upper level) toolbar object.
// -----------------------------------------------------------------
function TBToolbar()
{
    // The Toolbar Bands collection.
    this.Bands = new TBBandList();
}
TBToolbar.prototype.GetHTML         = TBToolbar_GetHTML ;
TBToolbar.prototype.LoadButtonsSet  = TBToolbar_LoadButtonsSet ;

function TBToolbar_GetHTML()
{
    var sHTML = '<TABLE width="100%" class="Toolbar" cellspacing="0" cellpadding="0" border="0" unselectable="on"><TR><TD>' ;
    var oBand ;

    for (iBand = 0 ; iBand < this.Bands.Array.length ; iBand++)
    {
        sHTML += '<TABLE class="Toolbar" cellspacing="0" cellpadding="0" border="0" unselectable="on"><TR>' ;

        oBand = this.Bands.Array[iBand]
        for (iItem = 0 ; iItem < oBand.Items.Array.length ; iItem++)
        {
            sHTML += '<TD>' + oBand.Items.Array[iItem].GetHTML() + '</TD>' ;
        }

        sHTML += '</TR></TABLE>' ;
    }

    return '</TD></TR></TABLE>' + sHTML ;
}

function TBToolbar_LoadButtonsSet(toolbarSetName)
{
    var ToolbarSet = config.ToolbarSets[toolbarSetName] ;

    if (! ToolbarSet)
    {
        alert('Toolbar set "' + toolbarSetName + '" doesn\'t exist') ;
        return ;
    }

    this.Bands = new TBBandList() ;
    var oBand ;
    var sItem ;

    for (iBand = 0 ; iBand < ToolbarSet.length ; iBand++)
    {
        oBand = this.Bands.Add() ;
        for (iItem in ToolbarSet[iBand])
        {
            sItem = ToolbarSet[iBand][iItem] ;
            if (sItem == '-')
                oBand.Items.Add(new TBSeparator()) ;
            else if (sItem == 'Equation' && !config.EnableEquations)
                continue;
            else
                oBand.Items.Add(oTB_Items[sItem]) ;
        }
    }
}

// -----------------------------------------------------------------
// -- TBBandList class - A collection of toolbar bands.
// -----------------------------------------------------------------
function TBBandList()
{
    this.Array = new Array() ;
}
TBBandList.prototype.Add = TBBandList_Add ;     // Adds a Band to the collection.

function TBBandList_Add(bandName)
{
    var i = this.Array.length ;
    this.Array[i] = new TBBand(bandName) ;
    return this.Array[i] ;
}

// -----------------------------------------------------------------
// -- TBBand class - A toolbar band. It holds a group of items (buttons, combos, etc...).
// -----------------------------------------------------------------
function TBBand(bandName)
{
    this.Name   = bandName || "" ;              // The Band name
    this.Items  = new TBItemList() ;            // The Band Items collection.
}

// -----------------------------------------------------------------
// -- TBItemList class - A collection of items (TBButton's, TBCombo's, etc...).
// -----------------------------------------------------------------
function TBItemList()
{
    this.Array = new Array() ;
}
TBItemList.prototype.Add = TBItemList_Add ;     // Adds an Item to the collection.

function TBItemList_Add(objectItem)
{
    var i = this.Array.length ;
    this.Array[i] = objectItem ;
    return this.Array[i] ;
}

// -----------------------------------------------------------------
// -- TBButton class - Represents a toolbar button.
// -----------------------------------------------------------------
function TBButton(name, toolTip, command, commandType, onEditingAction, width, height)
{
    this.Name       = name.toLowerCase() ;
    this.ToolTip    = toolTip || name ;
    this.Width      = width  || 21 ;
    this.Height     = height || 21 ;
    this.CommandType = commandType || TBCMD_DEC ;
    this.Image      = null ;
    this.Active     = false ;

    switch (this.CommandType)
    {
        case TBCMD_DEC :
            this.Command     = "decCommand(" + command + ")" ;
            this.CommandId   = command ;
            events.attachEvent('onEditing', this) ;
            break ;
        case TBCMD_DOC :
            this.Command     = "docCommand('" + command + "')" ;
            this.CommandCode = command ;
            events.attachEvent('onEditing', this) ;
            break ;
        default :
            this.Command = command || "void(0)" ;
            if (onEditingAction)
            {
                this.OnEditingAction = onEditingAction ;
                events.attachEvent('onEditing', this) ;
            }
            break ;
    }
}
TBButton.prototype.GetHTML      = TBButton_GetHTML ;
TBButton.prototype.onEditing    = TBButton_onEditing ;

function TBButton_GetHTML()
{
    this.Active = true ;
    return '<IMG id="btn' + this.Name + '" src="' + config.ToolbarImagesPath + 'button.' + this.Name + '.gif" width=' + this.Width
            + ' height=' + this.Height
            + ' onclick="' + this.Command + '"'
            + ' onload="TBButtonLoad(this,\'' + this.Name + '\');"'
            + ' onmouseover="TBButtonOver(this,\'' + this.Name + '\');"'
            + ' onmouseout="TBButtonOut(this,\'' + this.Name + '\');"'
            + ' ondrag="return false;"'
            + ' class="ButtonHidden"'
            + ' alt="' + this.ToolTip + '">' ;
}

function TBButton_onEditing()
{
    if (! this.Active) return ;

    if (this.Image == null) this.Image = document.getElementById('btn' + this.Name) ;
    if (this.Image && this.Image.Loaded)
    {
        var state ;
        switch (this.CommandType)
        {
            case TBCMD_DEC :
                state = checkDecCommand(this.CommandId) ;
                break ;
            case TBCMD_DOC :
                state = checkDocCommand(this.CommandCode) ;
                break ;
            default :
                if (this.OnEditingAction)
                    state = eval(this.OnEditingAction) ;
                break ;
        }

        if (state == OLE_TRISTATE_UNCHECKED)
        {
            if (this.Image.onmouseover == null) this.Image.onmouseover = this.Image.BackupOnMouseOver ;
            if (this.Image.onmouseout == null)  this.Image.onmouseout  = this.Image.BackupOnMouseOut ;
            TBButtonOut(this.Image, this.Name) ;
        }
        else if (state == OLE_TRISTATE_GRAY)
        {
            this.Image.onmouseover = null ;
            this.Image.onmouseout  = null ;
            if (this.Image.className != "ButtonOff") this.Image.className = "ButtonOff" ;
        }
        else
        {
            this.Image.onmouseout = null ;
            TBButtonOver(this.Image, this.Name) ;
        }
    }
}

// -----------------------------------------------------------------
// -- TBCombo class - Represents a toolbar combo.
// -----------------------------------------------------------------
function TBCombo(name, command, label, options, values, onEditingAction, separator)
{
    separator = separator ? separator : ';' ;

    this.Name    = name ;
    this.Label   = label || "" ;
    this.Command = command ;
    this.Options = options ? options.split( separator ) : new Array() ;
    this.Values  = values  ? values.split( separator )  : new Array() ;
    this.Active     = false ;

    if (onEditingAction)
    {
        this.onEditingAction = onEditingAction ;
        events.attachEvent('onEditing', this) ;
    }
}
TBCombo.prototype.GetHTML   = TBCombo_GetHTML ;
TBCombo.prototype.onEditing = TBCombo_onEditing ;

function TBCombo_GetHTML()
{
    this.Active = true ;

    var sHTML = this.Label + '&nbsp;\n<SELECT id="cmb' + this.Name + '" onchange="' + this.Command + '">\n' ;
    for (i in this.Options)
    {
        if (this.Values.length > 0)
            sHTML += '<OPTION value="' + this.Values[i] + '">' + this.Options[i] + '</OPTION>\n' ;
        else
            sHTML += '<OPTION>' + this.Options[i] + '</OPTION>\n' ;
    }
    return sHTML + '</SELECT>' ;
}

function TBCombo_onEditing()
{
    if (! this.Active) return ;

    eval(this.onEditingAction) ;
}

var iElementNum = 0 ;

// -----------------------------------------------------------------
// -- TBCheckBox class - Represents a toolbar checkbox.
// -----------------------------------------------------------------
function TBCheckBox(name, command, label, eventToListen)
{
    this.Name       = name ;
    this.Command    = command ;
    this.Label      = label || "" ;
    this.Checkboxes = null ;

    if (eventToListen)
    {
        events.attachEvent(eventToListen, this) ;
        this[eventToListen] = this.onEvent ;
    }
}
TBCheckBox.prototype.GetHTML = TBCheckBox_GetHTML ;
TBCheckBox.prototype.onEvent = TBCheckBox_onEvent ;

function TBCheckBox_GetHTML()
{
    return this.Label + '<INPUT name="chk' + this.Name + '" type=checkbox onclick="' + this.Command + '">' ;
}

function TBCheckBox_onEvent(checked)
{

    if (this.Checkboxes == null) this.Checkboxes = document.getElementsByName('chk' + this.Name) ;
    if (this.Checkboxes)
    {
        for (i = 0 ; i < this.Checkboxes.length ; i++)
        {
            this.Checkboxes[i].checked = checked ;
        }
    }
}

// -----------------------------------------------------------------
// -- TBSeparator class - Represents a toolbar items separator.
// -----------------------------------------------------------------
function TBSeparator()
{
    this.Name = '' ;
}
TBSeparator.prototype.GetHTML = TBSeparator_GetHTML ;

function TBSeparator_GetHTML()
{
    return '<IMG class="spacer" src="' + config.ToolbarImagesPath + 'separator.gif" width=5 height=22>' ;
}

// Behaviours
function TBButtonLoad(oImage, Name)
{
    oImage.onload = null ;
    oImage.className = "ButtonOut" ;
    oImage.BackupOnMouseOver = oImage.onmouseover ;
    oImage.BackupOnMouseOut  = oImage.onmouseout ;

    oImage.Loaded = true ;
}

function TBButtonOver(oImage, Name)
{
    if (oImage.className != "ButtonOver") oImage.className = "ButtonOver" ;
}

function TBButtonOut(oImage, Name)
{
    if (oImage.className != "ButtonOut") oImage.className = "ButtonOut" ;
}
