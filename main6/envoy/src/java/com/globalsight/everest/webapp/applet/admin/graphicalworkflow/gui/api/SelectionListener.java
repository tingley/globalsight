/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api;

import java.util.EventListener;

/**
   This interface should be implemented by any class that is interested
   in receiving <code>SelectionEvent</code>s from a <code>SelectionSource</code>.
 */
public interface SelectionListener extends EventListener {

	/**
	   A <code>SelectionSource</code> calls this method after the selection
	   has changed.

	   @param event <code>SelectionEvent</code> containing information
	   about selection change   
	 */
	public abstract void selectionChanged(SelectionEvent event);

	/**
	   A <code>SelectionSource</code> calls this method when the selection
	   is about to change.

	   @param event <code>SelectionEvent</code> containing information
	   about selection change
	   @return false to indicate that this <code>SelectionListener</code>
	   does not allow the selection to change, true if it approves of the
	   change	   
	 */
	public abstract boolean selectionChanging(SelectionEvent event);
}
