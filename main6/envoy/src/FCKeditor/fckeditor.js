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
 * fckeditor.js: Inserts a FCKeditor instance in a HTML page.
 *
 * Authors:
 *   Frederico Caldeira Knabben (fckeditor@fredck.com)
 */

var FCKeditorBasePath = '/globalsight/FCKeditor/' ;

var isOpera = (navigator.userAgent.indexOf("Opera") > 0) ;

// Check if the browser is compatible with the Editor:
//      - Internet Explorer 5 or above
var isCompatible = (!isOpera && navigator.appName == 'Microsoft Internet Explorer') ;
if (isCompatible)
{
    var browserVersion = navigator.appVersion.match(/MSIE (.\..)/)[1] ;
    isCompatible = (browserVersion >= 5) ;
}

// FCKeditor class
function FCKeditor(instanceName, width, height, toolbarSet, value)
{
    this.InstanceName   = instanceName ;
    this.Width          = width         || '100%' ;
    this.Height         = height        || '200' ;
    this.ToolbarSet     = toolbarSet ;
    this.Value          = value         || '' ;

    this.Config         = new Object() ;
    this.CanUpload      = null ;    // true / false
    this.CanBrowse      = null ;    // true / false
}

FCKeditor.prototype.Create = function()
{
    if (isCompatible)
    {
        var sLink = FCKeditorBasePath + 'fckeditor.html?FieldName=' + this.InstanceName ;
        if (this.ToolbarSet) sLink += '&Toolbar=' + this.ToolbarSet ;
        if (this.CanUpload != null) sLink += '&Upload=' + (this.CanUpload ? "true" : "false") ;
        if (this.CanBrowse != null) sLink += '&Browse=' + (this.CanBrowse ? "true" : "false") ;

        for ( o in this.Config )
            sLink += '&' + o + '=' + this.Config[o] ;

        document.write('<IFRAME src="' + sLink + '" width="' + this.Width + '" height="' + this.Height + '" frameborder="no" scrolling="no"></IFRAME>') ;
        document.write('<INPUT type="hidden" name="' + this.InstanceName + '" value="' +  HTMLEncode( this.Value ) + '">') ;
    }
    else
    {
        var sWidth  = this.Width.toString().indexOf('%')  > 0 ? this.Width  : this.Width  + 'px' ;
        var sHeight = this.Height.toString().indexOf('%') > 0 ? this.Height : this.Height + 'px' ;
        document.write('<TEXTAREA name="' + this.InstanceName + '" rows="4" cols="40" style="WIDTH: ' + sWidth + '; HEIGHT: ' + sHeight + '" wrap="virtual">' + HTMLEncode( this.Value ) + '<\/TEXTAREA>') ;
    }
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
