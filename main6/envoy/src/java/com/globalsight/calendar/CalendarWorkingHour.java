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
 * CalendarWorkingHour is the sub-class of WorkingHour used by the
 * CalendarWorkingDay object.
 */
public class CalendarWorkingHour extends WorkingHour
{
	private static final long serialVersionUID = 4567094042972437775L;

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Create an initialized CalendarWorkingHour (used by TOPLink).
	 */
	public CalendarWorkingHour()
	{
		super();
	}

	/**
	 * Constructor used during creation of a working hour within a calendar
	 * working day.
	 * 
	 * @param p_order -
	 *            The order of this working hour used for UI.
	 * @param p_startHour -
	 *            The starting hour.
	 * @param p_startHour -
	 *            The starting minute.
	 * @param p_endHour -
	 *            The ending hour.
	 * @param p_startHour -
	 *            The ending minute.
	 */
	public CalendarWorkingHour(int p_order, int p_startHour, int p_startMinute,
			int p_endHour, int p_endMinute)
	{
		super(p_order, p_startHour, p_startMinute, p_endHour, p_endMinute);
	}
	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////
}
