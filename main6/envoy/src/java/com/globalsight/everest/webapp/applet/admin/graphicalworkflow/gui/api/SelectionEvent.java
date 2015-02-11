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

import java.util.EventObject;

/**
 * SelectionEvent is an EventObject that has seletion type (node,
   category, process definition, or process instance) and selection
   category (activity list, process definition list, or process instance
   list).
 */
public class SelectionEvent extends EventObject {
	protected int _type;
	protected int _category;
	protected int _context;
	public static final int TYPE_NODE = 1;
	public static final int TYPE_ACTIVITY = 1;
	public static final int TYPE_PROCESSDEF = 2;
	public static final int TYPE_PROCESSINST = 3;
	public static final int TYPE_CATEGORY = 4;
	public static final int TYPE_FILTER = 5;
	public static final int CATEGORY_ACTIVITYLIST = 1;
	public static final int CATEGORY_PROCESSDEFLIST = 2;
	public static final int CATEGORY_PROCESSINSTLIST = 3;
	public static final int CONTEXT_FILTERCHANGE = 1;

	/**
	 * Returns the selection type
	 * @return the selection type	 
	 */
	public int getType() {
		return _type;
    }

	/**
	 * Returns the selection category
	 * @return the selection category
	 */
	public int getCategory() {
		return _category;
    }

	/**
	 * Constructs a SelectionEvent object based on a selection type
	 * @param source    the source of the selection event
	 * @param type
	   the selection type
	 */
	public SelectionEvent(SelectionSource source, int type) {
		super(source);
		_type = type;
    }

	/**
	 * Constructs a SelectionEvent object based on a selection type and
	   a selection category
	 * @param source    the source of the selection event
	 * @param type
	   the selection type
	 * @param category    the selection category
	 */
	public SelectionEvent(SelectionSource source, int type, int category) {
		super(source);
		_type = type;
		_category = category;
    }

	/**
	   @roseuid 372E553D03EA
	 */
	public synchronized void setContext(int context) {
		_context = context;
    }

	/**
	   @roseuid 372E553D03EC
	 */
	public int getContext() {
		return _context;
    }
}
