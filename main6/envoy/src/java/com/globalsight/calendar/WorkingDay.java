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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * WorkingDay is the super class used for Calendar and UserCalender's working
 * day.
 */
public abstract class WorkingDay extends PersistentObject
{
	//
	// PRIVATE MEMBER VARIABLES
	//

	private int m_workingDay = 0; // 1 to 7 (sun to sat)

	private List m_workingHours = new ArrayList();

	private BaseFluxCalendar m_baseFluxCalendar = null;

	// The default state of the working day. Once added to a calendar
	// the state would be changed to 'NEW'.
	private int m_calendarAssociationState = CalendarConstants.EXISTING;

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////

	/** Default constructor. */
	public WorkingDay()
	{
		super();
	}

	/**
	 * Constructor used by the sub-classes of WorkingDay.
	 * 
	 * @param p_workingDay -
	 *            The day to be set as a working day.
	 */
	public WorkingDay(int p_workingDay)
	{
		super();
		m_workingDay = p_workingDay;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Methods
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Get the calendar association state of this working day. This value is
	 * used during creation/modification of a calendar in order to determine
	 * whether this working day is an existing one, a new one, or the one that
	 * should be removed from the list of working days for a particular
	 * calendar.
	 * 
	 * @return The state for calendar association.
	 */
	public int getCalendarAssociationState()
	{
		return m_calendarAssociationState;
	}

	/**
	 * Get the working day. The day is an integer ranging from 1 to 7 (Sunday to
	 * Saturday).
	 * 
	 * @return An integer representing the day of the month.
	 */
	public int getDay()
	{
		return m_workingDay;
	}

	/**
	 * Set the working day to be the specified day (1 to 7)
	 * 
	 * @param p_day -
	 *            The working day to be set.
	 */
	public void setDay(int p_day)
	{
		m_workingDay = p_day;
	}

	/**
	 * Get the working hour for the given order. The order identifies the
	 * location of this working hour at the UI level (since we have 5 fields for
	 * working hours for a given day).
	 * 
	 * @param p_order -
	 *            The order of a working hour to be searched for.
	 * @return The working hour at the given order, or null if not found.
	 */
	public WorkingHour getWorkingHourByOrder(int p_order)
	{
		int size = m_workingHours.size();
		WorkingHour workingHour = null;
		for (int i = 0; (workingHour == null && i < size); i++)
		{
			WorkingHour wh = (WorkingHour) m_workingHours.get(i);
			if (wh.getOrder() == p_order)
			{
				workingHour = wh;
			}
		}

		return workingHour;
	}

	/**
	 * Get a list of working hours that belong to this working day. Note that
	 * the working hour type depends on the sub-class of this working day
	 * object.
	 * 
	 * @return A list of working hours.
	 */
	public List getWorkingHours()
	{
		return m_workingHours;
	}

	/**
	 * Add a working hour to the list of this working day. Note that the type of
	 * the WorkingHour object depends on the sub-class of the WorkingDay class
	 * (i.e. for CalendarWorkingDay, it'll be CalendarWorkingHour). Once a
	 * working hour is added, it's start date and duration will be calculated.
	 * 
	 * @param p_workingHour -
	 *            The working hour to be added.
	 */
	public void addWorkingHour(WorkingHour p_workingHour, TimeZone p_timeZone)
	{
		p_workingHour.setWorkingDay(this);
		p_workingHour.computeStartDateAndDuration(p_timeZone);
		m_workingHours.add(p_workingHour);
	}

	/*
	 * Set the calendar where this working day belongs to. The calendar type
	 * (i.e. FluxCalendar vs. UserFluxCalendar) depends on the sub-class of this
	 * WorkingDay.
	 * 
	 * @param p_calendar The calendar where this holiday belongs to.
	 */
	public void setBaseFluxCalendar(BaseFluxCalendar p_calendar)
	{
		m_baseFluxCalendar = p_calendar;
		// since this back pointer is set when a working day is
		// added to a calendar, we'll set the associate state here.
		m_calendarAssociationState = CalendarConstants.NEWLY_ADDED;
	}

	public BaseFluxCalendar getBaseFluxCalendar()
	{
		return m_baseFluxCalendar;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Public Methods
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Override Methods
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Indicates whether some other WorkingDay is "equal to" this one.
	 * 
	 * @param p_workingDay
	 *            The reference object with which to compare.
	 * @return true if this working day object is the same as the p_workingDay
	 *         argument; false otherwise.
	 */
	public boolean equals(Object p_workingDay)
	{
		if (p_workingDay instanceof WorkingDay)
		{
			return (getId() == ((WorkingDay) p_workingDay).getId());
		}
		return false;
	}

	/**
	 * The hashCode method is overridden to support the 'equals' method. If two
	 * WorkdingDay objects are equal according to the equals(Object) method,
	 * then calling the hashCode method on each of the two objects will produce
	 * the same integer result.
	 * 
	 * @return the working day id.
	 */
	public int hashCode()
	{
		return getIdAsLong().hashCode();
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Public Override Methods
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Package Level Methods
	// ////////////////////////////////////////////////////////////////////

	/**
	 * Remove the working hour from this working day. This is a removal of the
	 * object from the working hour list.
	 * 
	 * @param p_workingHour
	 *            The working hour to be removed.
	 */
	void removeWorkingHour(WorkingHour p_workingHour)
	{
		m_workingHours.remove(p_workingHour);
	}

	/**
	 * Set the calendar association state to be the specified value.
	 * 
	 * @param p_state -
	 *            The calendar association state to be set.
	 */
	void setCalendarAssociationState(int p_state)
	{
		m_calendarAssociationState = p_state;
	}

	public void setWorkingHours(List hours)
	{
		m_workingHours = hours;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Package Level Methods
	// ////////////////////////////////////////////////////////////////////
}
