/*----------------------------------------------------------------------------\
|                          Selectable Elements 1.01                           |
|-----------------------------------------------------------------------------|
|                         Created by Erik Arvidsson                           |
|                  (http://webfx.eae.net/contact.html#erik)                   |
|                      For WebFX (http://webfx.eae.net/)                      |
|-----------------------------------------------------------------------------|
|          A script that allows children of any element to be selected        |
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
| 2002-09-19 | Original Version Posted.                                       |
| 2002-09-27 | Fixed a bug in IE when mouse down and up occured on different  |
|            | rows.                                                          |
|-----------------------------------------------------------------------------|
| Created 2002-09-04 | All changes are in the log above. | Updated 2002-09-27 |
\----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------\
| This file requires that SelectableElements is first defined. This class can |
| be found in the file selectableelements.js at WebFX                         |
\----------------------------------------------------------------------------*/

function SelectableFields(oListElement, bMultiple, bHighlight)
{
    // SelectableElements Constructor
    if (oListElement == null)
        return;

    this._htmlElement = oListElement;
    this._multiple = Boolean(bMultiple);
    this._highlight = Boolean(bHighlight);

    this._selectedItems = [];
    this._fireChange = true;

    var oThis = this;
    var f = function (e) {
        if (e == null) e = oListElement.ownerDocument.parentWindow.event;
        oThis.click(e);
    };

    if (oListElement.addEventListener)
        oListElement.addEventListener('click', f, false);
    else if (oListElement.attachEvent)
        oListElement.attachEvent('onclick', f);

    // SelectableElements Constructor - end

    if (this._highlight)
    {
        var items = this.getItems();

        var f = function(e)
        {
            _addClassName(this, 'highlighted');
            _cancelEvent(e);
        };
        var g = function(e)
        {
            _removeClassName(this, 'highlighted');
            _cancelEvent(e);
        };

        if (items != null)
        {
            for (var i = 0; i < items.length; i++)
            {
                var item = items[i];

                if (item.className != 'fakeConceptGrp' &&
                    item.className != 'fakeLanguageGrp' &&
                    item.className != 'fakeTermGrp')
                {
                    item.onmouseover = f;
                    item.onmouseout = g;
                }
            }
        }
    }
}

SelectableFields.prototype.setItemSelected = function (oEl, bSelected) {
    if (!this._multiple) {
        if (bSelected) {
            var old = this._selectedItems[0]
            if (oEl == old)
                return;
            if (old != null)
                this.setItemSelectedUi(old, false);
            this.setItemSelectedUi(oEl, true);
            this._selectedItems = [oEl];
            this.fireChange();
        }
        else {
            if (this._selectedItems[0] == oEl) {
                this.setItemSelectedUi(oEl, false);
                this._selectedItems = [];
            }
        }
    }
    else {
        if (Boolean(oEl._selected) == Boolean(bSelected))
            return;

        this.setItemSelectedUi(oEl, bSelected);

        if (bSelected)
            this._selectedItems[this._selectedItems.length] = oEl;
        else {
            // remove
            var tmp = [];
            var j = 0;
            for (var i = 0; i < this._selectedItems.length; i++) {
                if (this._selectedItems[i] != oEl)
                    tmp[j++] = this._selectedItems[i];
            }
            this._selectedItems = tmp;
        }
        this.fireChange();
    }
};

// This method updates the UI of the item
SelectableFields.prototype.setItemSelectedUi = function (oEl, bSelected) {
    if (bSelected)
        _addClassName(oEl, 'selected');
    else
        _removeClassName(oEl, 'selected');

    oEl._selected = bSelected;
};

SelectableFields.prototype.getItemSelected = function (oEl) {
    return Boolean(oEl._selected);
};

SelectableFields.prototype.fireChange = function () {
    if (!this._fireChange)
        return;
    if (typeof this.onchange == 'string')
        this.onchange = new Function(this.onchange);
    if (typeof this.onchange == 'function')
        this.onchange();
};


SelectableFields.prototype.click = function (e) {
    var oldFireChange = this._fireChange;
    this._fireChange = false;

    // create a copy to compare with after changes
    var selectedBefore = this.getSelectedItems();   // is a cloned array

    // find row
    var el = e.target != null ? e.target : e.srcElement;
    while (el != null && !this.isItem(el))
        el = el.parentNode;

    if (el == null) {
        // happens in IE when down and up occur on different items
        this._fireChange = oldFireChange;
        return;
    }

    var rIndex = el;
    var aIndex = this._anchorIndex;

    // test whether the current row should be the anchor
    if (this._selectedItems.length == 0 ||
        (e.ctrlKey && !e.shiftKey && this._multiple)) {
        aIndex = this._anchorIndex = rIndex;
    }

    if (!e.ctrlKey && !e.shiftKey || !this._multiple) {
        // deselect all
        var items = this._selectedItems;
        for (var i = items.length - 1; i >= 0; i--) {
            if (items[i]._selected && items[i] != el)
                this.setItemSelectedUi(items[i], false);
        }
        this._anchorIndex = rIndex;
        if (!el._selected) {
            this.setItemSelectedUi(el, true);
        }
        this._selectedItems = [el];
    }

    // ctrl
    else if (this._multiple && e.ctrlKey && !e.shiftKey) {
        this.setItemSelected(el, !el._selected);
        this._anchorIndex = rIndex;
    }

    // ctrl + shift
    else if (this._multiple && e.ctrlKey && e.shiftKey) {
        // up or down?
        var dirUp = this.isBefore(rIndex, aIndex);

        var item = aIndex;
        while (item != null && item != rIndex) {
            if (!item._selected && item != el)
                this.setItemSelected(item, true);
            item = dirUp ? this.getPrevious(item) : this.getNext(item);
        }

        if (!el._selected)
            this.setItemSelected(el, true);
    }

    // shift
    else if (this._multiple && !e.ctrlKey && e.shiftKey) {
        // up or down?
        var dirUp = this.isBefore(rIndex, aIndex);

        // deselect all
        var items = this._selectedItems;
        for (var i = items.length - 1; i >= 0; i--)
            this.setItemSelectedUi(items[i], false);
        this._selectedItems = [];

        // select items in range
        var item = aIndex;
        while (item != null) {
            this.setItemSelected(item, true);
            if (item == rIndex)
                break;
            item = dirUp ? this.getPrevious(item) : this.getNext(item);
        }
    }

    // find change!!!
    var found;
    var changed = selectedBefore.length != this._selectedItems.length;
    if (!changed) {
        for (var i = 0; i < selectedBefore.length; i++) {
            found = false;
            for (var j = 0; j < this._selectedItems.length; j++) {
                if (selectedBefore[i] == this._selectedItems[j]) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changed = true;
                break;
            }
        }
    }

    this._fireChange = oldFireChange;
    if (changed && this._fireChange)
        this.fireChange();
}

SelectableFields.prototype.getSelectedItems = function () {
    //clone
    var items = this._selectedItems;
    var l = items.length;
    var tmp = new Array(l);
    for (var i = 0; i < l; i++)
        tmp[i] = items[i];
    return tmp;
};

//  SelectableFields.prototype.isItem = function (node) {
//      return node != null && node.nodeType == 1 &&
//          node.parentNode == this._htmlElement;
//  };


/* Traversable Collection Interface */

//  SelectableFields.prototype.getNext = function (el) {
//      var n = el.nextSibling;
//      if (n == null || this.isItem(n))
//          return n;
//      return this.getNext(n);
//  };

//  SelectableFields.prototype.getPrevious = function (el) {
//      var p = el.previousSibling;
//      if (p == null || this.isItem(p))
//          return p;
//      return this.getPrevious(p);
//  };

SelectableFields.prototype.isBefore = function (n1, n2) {
    var next = this.getNext(n1);
    while (next != null) {
        if (next == n2)
            return true;
        next = this.getNext(next);
    }
    return false;
};

/* End Traversable Collection Interface */

/* Indexable Collection Interface */

//  SelectableFields.prototype.getItems = function () {
//      var tmp = [];
//      var j = 0;
//      var cs = this._htmlElement.childNodes;
//      var l = cs.length;
//      for (var i = 0; i < l; i++) {
//          if (cs[i].nodeType == 1)
//              tmp[j++] = cs[i]
//      }
//      return tmp;
//  };

SelectableFields.prototype.getItem = function (nIndex) {
    var j = 0;
    var cs = this._htmlElement.childNodes;
    var l = cs.length;
    for (var i = 0; i < l; i++) {
        if (cs[i].nodeType == 1) {
            if (j == nIndex)
                return cs[i];
            j++;
        }
    }
    return null;
};

SelectableFields.prototype.getSelectedIndexes = function () {
    var items = this.getSelectedItems();
    var l = items.length;
    var tmp = new Array(l);
    for (var i = 0; i < l; i++)
        tmp[i] = this.getItemIndex(items[i]);
    return tmp;
};


SelectableFields.prototype.getItemIndex = function (el) {
    var j = 0;
    var cs = this._htmlElement.childNodes;
    var l = cs.length;
    for (var i = 0; i < l; i++) {
        if (cs[i] == el)
            return j;
        if (cs[i].nodeType == 1)
            j++;
    }
    return -1;
};

/* End Indexable Collection Interface */


function _addClassName(el, sClassName) {
    var s = el.className;
    var p = s.split(' ');
    var l = p.length;
    for (var i = 0; i < l; i++) {
        if (p[i] == sClassName)
            return;
    }
    p[p.length] = sClassName;
    el.className = p.join(' ');
}

function _removeClassName(el, sClassName) {
    var s = el.className;
    var p = s.split(' ');
    var np = [];
    var l = p.length;
    var j = 0;
    for (var i = 0; i < l; i++) {
        if (p[i] != sClassName)
            np[j++] = p[i];
    }
    el.className = np.join(' ');
}

function _cancelEvent(event)
{
  var evt = event ? event : (window.event ? window.event : null);
  evt.cancelBubble = true;
  evt.returnValue = false;
}

// Overwritten methods

SelectableFields.prototype.getItems = function () {
    return this._htmlElement.getElementsByTagName("DIV");
};

SelectableFields.prototype.isItem = function (node) {
    return node != null && node.tagName == "DIV";
};

/* Begin Traversable Collection Interface */

SelectableFields.prototype.getNext = function (el) {
    var next = this._getFirstDescendant(el);
    if (el != next)
        return next;
    next = this._getNextSibling(el);
    var p = el.parentNode;
    while (next == null) {
        while (p != null && !this.isItem(p))
            p = p.parentNode;
        if (p == null)
            return null;
        next = this._getNextSibling(p);
        p = p.parentNode;

    }
    return next;
};

SelectableFields.prototype._getNextSibling = function (el) {
    var n = el.nextSibling;
    while (n != null && !this.isItem(n))
        n = n.nextSibling;
    return n;
};

SelectableFields.prototype._getFirstDescendant = function (el) {
    var lis = el.getElementsByTagName("DIV");
    if (lis.length == 0)
        return el;
    return lis[0];
};

SelectableFields.prototype.getPrevious = function (el) {
    var previous = this._getPreviousSibling(el);
    var p = el.parentNode;
    if (previous == null) {
        while (p != null && !this.isItem(p))
            p = p.parentNode;
        return p;
    }
    return this._getLastDescendant(previous);
};

SelectableFields.prototype._getPreviousSibling = function (el) {
    var p = el.previousSibling;
    while (p != null && !this.isItem(p))
        p = p.previousSibling;
    return p;
};

SelectableFields.prototype._getLastDescendant = function (el) {
    var lis = el.getElementsByTagName("DIV");
    if (lis.length == 0)
        return el;
    return lis[lis.length - 1];
};


/* End Traversable Collection Interface */

/* Indexable Collection Interface not implemented */


