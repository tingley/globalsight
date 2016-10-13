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

function SelectableListItems(oListElement, bMultiple) {
	SelectableElements.call(this, oListElement, bMultiple);
}
SelectableListItems.prototype = new SelectableElements;


SelectableListItems.prototype.getItems = function () {
	return this._htmlElement.getElementsByTagName("LI");
};

SelectableListItems.prototype.isItem = function (node) {
	return node != null && node.tagName == "LI";
};

/* End Traversable Collection Interface */

SelectableListItems.prototype.getNext = function (el) {
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

SelectableListItems.prototype._getNextSibling = function (el) {
	var n = el.nextSibling;
	while (n != null && !this.isItem(n))
		n = n.nextSibling;
	return n;
};

SelectableListItems.prototype._getFirstDescendant = function (el) {
	var lis = el.getElementsByTagName("LI");
	if (lis.length == 0)
		return el;
	return lis[0];
};

SelectableListItems.prototype.getPrevious = function (el) {
	var previous = this._getPreviousSibling(el);
	var p = el.parentNode;
	if (previous == null) {
		while (p != null && !this.isItem(p))
			p = p.parentNode;
		return p;
	}
	return this._getLastDescendant(previous);
};

SelectableListItems.prototype._getPreviousSibling = function (el) {
	var p = el.previousSibling;
	while (p != null && !this.isItem(p))
		p = p.previousSibling;
	return p;
};

SelectableListItems.prototype._getLastDescendant = function (el) {
	var lis = el.getElementsByTagName("LI");
	if (lis.length == 0)
		return el;
	return lis[lis.length - 1];
};


/* End Traversable Collection Interface */

/* Indexable Collection Interface not implemented */


