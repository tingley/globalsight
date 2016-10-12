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
 * fck_events.js: Creates the events object that handles the editor
 * changes events for the toolbar.
 *
 * Authors:
 *   Frederico Caldeira Knabben (fckeditor@fredck.com)
 */

var events = new Object() ;

events.attachEvent = function(eventName, attachedFunction)
{
	if (! events["ev" + eventName]) 
		events["ev" + eventName] = new Array() ;
	
	events["ev" + eventName][events["ev" + eventName].length] = attachedFunction ;
}

events.fireEvent = function(eventName, params)
{
	var oEvents = events["ev" + eventName] ;
	
	if (oEvents) 
	{
		for (i in oEvents)
		{
			if (typeof(oEvents[i]) == "function")
				oEvents[i](params) ;
			else
				oEvents[i][eventName](params) ;
		}
	}
}