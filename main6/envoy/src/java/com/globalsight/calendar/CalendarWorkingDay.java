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
package com.globalsight.calendar;

/**
 * CalendarWorkingDay is the sub-class of WorkingDay and is associated with a
 * Calendar. Since an object can only be mapped to one table, this sub-class is
 * required.
 */
public class CalendarWorkingDay extends WorkingDay
{
	private static final long serialVersionUID = 2136269948718983293L;

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Default constructor.
	 */
	public CalendarWorkingDay()
	{
		super();
	}

	/**
	 * Constructor used during creation of a working day.
	 * 
	 * @param p_workingDay -
	 *            The day set as a working day.
	 */
	public CalendarWorkingDay(int p_workingDay)
	{
		super(p_workingDay);
	}
	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////
}
