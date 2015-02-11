/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003 Frederico Caldeira Knabben
 *
 * Licensed under the terms of the GNU Lesser General Public License
 * (http://www.opensource.org/licenses/lgpl-license.php)
 *
 * For further information go to http://www.fredck.com/FCKeditor/
 * or contact fckeditor@fredck.com.
 */

/*
 * fck_xhtml.js - Part 1: String Builder 1.02
 * A class that allows more efficient building of strings than concatenation.
 * For further information: http://webfx.eae.net
 * License: GPL - The GNU General Public License
 *
 * Authors:
 *   Erik Arvidsson (http://webfx.eae.net/contact.html#erik)
 */

function StringBuilder(sString)
{
    // public
    this.length = 0;

    this.append = function (sString)
    {
        // append argument
        this.length += (this._parts[this._current++] = String(sString)).length;

        // reset cache
        this._string = null;
        return this;
    };

    this.toString = function ()
    {
        if (this._string != null)
        {
            return this._string;
        }

        var s = this._parts.join("");
        this._parts = [s];
        this._current = 1;
        this.length = s.length;

        return this._string = s;
    };

    // private
    this._current   = 0;
    this._parts     = [];
    this._string    = null; // used to cache the string

    // init
    if (sString != null)
    {
        this.append(sString);
    }
}

/*
 * fck_xhtml.js - Part 2: Get XHTML for IE 1.03
 * Serializes an IE HTML DOM tree to a well formed XHTML string.
 * For further information: http://webfx.eae.net/dhtml/richedit/js/getxhtml.js
 * License: GPL - The GNU General Public License
 *
 * Authors:
 *   Erik Arvidsson (http://webfx.eae.net/contact.html#erik)
 */

// Changed by FredCK
function getXhtml(oNode)
{
    AMunzoomMathPlayer(oNode);

    var sb = new StringBuilder;
    var cs = oNode.childNodes;
    var l = cs.length;
    for (var i = 0; i < l; i++)
    {
        _appendNodeXHTML(cs[i], sb);
    }

    return sb.toString();
}

function _fixAttribute(s)
{
    return String(s).replace(/\&/g, "&amp;").replace(/</g, "&lt;").replace(/\"/g, "&quot;");
}

function _fixText(s)
{
    return String(s).replace(/\&/g, "&amp;").replace(/</g, "&lt;");
}

function _getAttributeValue(oAttrNode, oElementNode, sb)
{
    if (!oAttrNode.specified)
    {
        return;
    }

    var name = oAttrNode.expando ? oAttrNode.nodeName :
        oAttrNode.nodeName.toLowerCase() ;      // FredCK: changed
    var value = oAttrNode.nodeValue;

    if (name != "style" )
    {
        if (!isNaN(value) || name == "src" || name == "href")
        {
            // IE5.x bugs for number values
            // FredCK: force to get the correct href or source
            // FredCK: gets the attributes values as is (2 option)
            value = oElementNode.getAttribute(name, 2);
        }
        sb.append(" " + name + "=\"" + _fixAttribute(value) + "\"");
    }
    else
    {
        sb.append(" style=\"" + _fixAttribute(oElementNode.style.cssText) + "\"");
    }
}

function _appendNodeXHTML(node, sb)
{
    switch (node.nodeType) {
        case 1: // ELEMENT

            if (node.nodeName == "!")
            {
                // IE5.0 and IE5.5 are weird
                sb.append(node.text);
                break;
            }

            var name = node.nodeName;
            if (node.scopeName == "HTML" || node.scopeName == "m")
            {
                name = name.toLowerCase();
            }

            sb.append("<");
            if (node.scopeName != "HTML")
            {
                sb.append(node.scopeName + ":");
            }
            sb.append(name);

            // attributes
            var attrs = node.attributes;
            var l = attrs.length;
            for (var i = 0; i < l; i++)
            {
                _getAttributeValue(attrs[i], node, sb);
            }

            if (name == "input" && node.value)
            {
                sb.append(" value=\"" + _fixAttribute(node.value) + "\"");
            }

            if (node.canHaveChildren || node.hasChildNodes())
            {
                sb.append(">");

                // childNodes
                var cs = node.childNodes;
                l = cs.length;
                for (var i = 0; i < l; i++)
                    _appendNodeXHTML(cs[i], sb);

                sb.append("</");
                if (node.scopeName != "HTML")
                {
                    sb.append(node.scopeName + ":");
                }
                sb.append(name + ">");
            }
            else if (name == "script")
            {
                sb.append(">" + node.text + "</" + name + ">");
            }
            else if (name == "title" || name == "style" || name == "comment")
            {
                sb.append(">" + node.innerHTML + "</" + name + ">");
            }
            else
            {
                sb.append(" />");
            }

            break;

        case 3: // TEXT
            sb.append( _fixText(node.nodeValue) );
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
            {
                _appendNodeXHTML(cs[i], sb);
            }
            break;

        default:
            sb.append("<!--\nNot Supported:\n\n" + "nodeType: " +
                node.nodeType + "\nnodeName: " + node.nodeName + "\n-->");
    }
}
