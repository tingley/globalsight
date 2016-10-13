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

function SelectableTableCells(oTableElement, bMultiple) {
	SelectableElements.call(this, oTableElement, bMultiple);
}
SelectableTableCells.prototype = new SelectableElements;


SelectableTableCells.prototype.isItem = function (node) {
	return node != null && node.tagName == "TD" &&
			node.parentNode.parentNode.tagName == "TBODY" &&
			node.parentNode.parentNode.parentNode == this._htmlElement;
};

/* Traversable Collection Interface */

SelectableTableCells.prototype.getNext = function (el) {
	var i = this.getItemIndex(el);
	try {
		return this.getItem(i + 1);
	}
	catch (ex) {
		return null;
	}
	
};

SelectableTableCells.prototype.getPrevious = function (el) {
	var i = this.getItemIndex(el);
	try {
		return this.getItem(i - 1);
	}
	catch (ex) {
		return null;
	}
};

/* End Traversable Collection Interface */

/* Indexable Collection Interface */

SelectableTableCells.prototype.getItems = function () {
	var rows = this._htmlElement.rows;
	var rl = rows.length;
	var tmp = [];
	var j = 0;
	var cells, cl;
	for (var y = 0; y < rl; y++) {
		cells = rows[y].cells;
		cl = cells.length;
		for (var x = 0; x < cl; x++) {
			tmp[j++] = cells[x];
		}
	}
	return tmp;
};

SelectableTableCells.prototype.getItem = function (i) {
	var rows = this._htmlElement.rows;
	var rl = rows.length;
	var cl = rows[0].cells.length;
	var ri = Math.floor(i / cl);
	var ci = i - ri * cl;
	return rows[ri].cells[ci];
};

SelectableTableCells.prototype.getItemIndex = function (el) {
	var rows = this._htmlElement.rows;
	var cl = rows[0].cells.length;
	var ri = el.parentNode.rowIndex;
	var ci = el.cellIndex;
	return ri * cl + ci;
};

/* End Indexable Collection Interface */