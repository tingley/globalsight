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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.date.DateHelper;

/**
 * WorkingHour is the super class used for Calendar and UserCalender's working
 * day.
 */
public abstract class WorkingHour extends PersistentObject
{
	//
	// PRIVATE MEMBER VARIABLES
	//
	private int m_endHour = -1;
	private int m_endMinute = -1;
	private int m_order = 1; // 1 to n (depending on UI's fields)
	private int m_startHour = -1;
	private int m_startMinute = -1;

	private String m_durationExpression = null;
	private Date m_startDate = null;

	// used for TOPLink's back pointer
	private WorkingDay m_workingDay = null;
	// The default state of a working hour. Once added to a working
	// day the state will change to 'NEW'.
	private int m_wkDayAssociationState = CalendarConstants.EXISTING;

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Create an initialized WorkingHour (used by TOPLink).
	 */
	public WorkingHour()
	{
		super();
	}

	/**
	 * Constructor used by the sub-classes of WorkingHour.
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
	public WorkingHour(int p_order, int p_startHour, int p_startMinute,
			int p_endHour, int p_endMinute)
	{
		super();
		m_order = p_order;
		m_startHour = p_startHour;
		m_startMinute = p_startMinute;
		m_endHour = p_endHour;
		m_endMinute = p_endMinute;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Methods
	// ////////////////////////////////////////////////////////////////////

	public WorkingDay getWorkingDay()
	{
		return m_workingDay;
	}
	
	/*
	 * Set the calendar where this working day belongs to. The calendar type
	 * (i.e. FluxCalendar vs. UserFluxCalendar) depends on the sub-class of this
	 * WorkingHour.
	 * 
	 * @param p_calendar The calendar where this holiday belongs to.
	 */
	public void setWorkingDay(WorkingDay p_workingDay)
	{
		m_workingDay = p_workingDay;
		// since this back pointer is set when a working hour is
		// added to a working day, we'll set the associate state here.
		m_wkDayAssociationState = CalendarConstants.NEWLY_ADDED;
	}
	
	public Calendar getStartCalendar() {
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.HOUR_OF_DAY, m_startHour);
    	calendar.set(Calendar.MINUTE, m_startMinute);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	
    	return calendar;
    }
    
    public Calendar getEndCalendar() {
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.HOUR_OF_DAY, m_endHour);
    	calendar.set(Calendar.MINUTE, m_endMinute);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	
    	return calendar;
    }

	/**
	 * Get the duration expression which is used by Quartz for applying time
	 * expression. This expression is in the form of "+<hour>H+<minute>m"
	 * which is a combination of hour and minute (i.e. 2 hours and 30 minutes is
	 * set as "+2H+30m" time expression).
	 * 
	 * @return The duration expression in terms of hours and minutes.
	 */
	public String getDurationExpression()
	{
		return m_durationExpression;
	}

	public void setDurationExpression(String durationExpression)
	{
		m_durationExpression = durationExpression;
	}

	/**
	 * Get the end hour for this working hour. Note that a valid value is
	 * between 0 to 23 for Hour.
	 * 
	 * @return The ending hour.
	 */
	public int getEndHour()
	{
		return m_endHour;
	}

	/**
	 * Set the end hour to be the specified value (0 to 23).
	 * 
	 * @param p_endHour -
	 *            The end hour to be set.
	 */
	public void setEndHour(int p_endHour)
	{
		m_endHour = p_endHour;
	}

	/**
	 * Get the ending minute for this working hour. Note that a valid value is
	 * between 0 to 50.
	 * 
	 * @return The ending minute.
	 */
	public int getEndMinute()
	{
		return m_endMinute;
	}

	/**
	 * Set the end minute to be the specified value (0 to 59).
	 * 
	 * @param p_endMinute -
	 *            The end minute to be set.
	 */
	public void setEndMinute(int p_endMinute)
	{
		m_endMinute = p_endMinute;
	}

	/**
	 * Get the order of this working hour. This order is used for displaying the
	 * working hour in the appropriate field (since many options are available
	 * to the user). There for the working hour object would be sorted by order.
	 * 
	 * @return An integer representing the order of the working hour.
	 */
	public int getOrder()
	{
		return m_order;
	}

	/**
	 * Set the order of this working hour to be the specified value. This value
	 * is used for sorting purpose before the working hours are displayed at the
	 * UI level.
	 * 
	 * @param p_order -
	 *            The order to be set.
	 */
	public void setOrder(int p_order)
	{
		m_order = p_order;
	}

	/**
	 * Get the start date generated based on the working day, and the start time
	 * (as hour:min). This is used for Flux methods.
	 * 
	 * @return The start date based on the create/modify date, the working day,
	 *         and the start time.
	 */
	public Date getStartDate()
	{
		return m_startDate;
	}
	
	public void setStartDate(Date startDate)
	{
		m_startDate = startDate;
	}

	/**
	 * Get the start hour for this working hour. Note that a valid value is
	 * between 0 to 23.
	 * 
	 * @return The starting hour.
	 */
	public int getStartHour()
	{
		return m_startHour;
	}

	/**
	 * Set the start hour to be the specified value (0 to 23).
	 * 
	 * @param p_startHour -
	 *            The start hour to be set.
	 */
	public void setStartHour(int p_startHour)
	{
		m_startHour = p_startHour;
	}

	/**
	 * Get the start minute for this working hour. Note that a valid value is
	 * between 0 to 59.
	 * 
	 * @return The starting minute (will be appended to the starting hour).
	 */
	public int getStartMinute()
	{
		return m_startMinute;
	}

	/**
	 * Set the start minute to be the specified value (0 to 59).
	 * 
	 * @param p_startMinute -
	 *            The start minute to be set.
	 */
	public void setStartMinute(int p_startMinute)
	{
		m_startMinute = p_startMinute;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Public Methods
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Override Methods
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Indicates whether some other WorkingHour is "equal to" this one.
	 * 
	 * @param p_workingHour
	 *            The reference object with which to compare.
	 * @return true if this working hour object is the same as the p_workingHour
	 *         argument; false otherwise.
	 */
	public boolean equals(Object p_workingHour)
	{
		if (p_workingHour instanceof WorkingHour)
		{
			return (getId() == ((WorkingHour) p_workingHour).getId());
		}
		return false;
	}

	/**
	 * The hashCode method is overridden to support the 'equals' method. If two
	 * WorkdingHour objects are equal according to the equals(Object) method,
	 * then calling the hashCode method on each of the two objects will produce
	 * the same integer result.
	 * 
	 * @return the working hour id.
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
	 * Compute the duration and start date which will be used by Flux when a
	 * date calculation is performed. This method should be called anytime a
	 * working hour is created or modified. The duration is calculated as a
	 * combination of hour and minute (i.e. 2 hours and 30 minutes) and set as
	 * "+2H+30m" time expression.
	 */
	void computeStartDateAndDuration(TimeZone p_timeZone)
	{
		Timestamp startTime = new Timestamp(p_timeZone);
		startTime.resetTimeOfDay();
		startTime.setHour(m_startHour);
		startTime.setMinute(m_startMinute);

		Timestamp endTime = new Timestamp(p_timeZone);
		// if ending hour is less than start hour (i.e. 0 for 12AM)
		// then add one day to the current day. This can also
		// happen if the user has 10PM to 2AM working hours.
		if (m_endHour < m_startHour)
		{
			endTime.add(Timestamp.DAY, 1);
		}
		endTime.resetTimeOfDay();
		endTime.setHour(m_endHour);
		endTime.setMinute(m_endMinute);

		// now convert the difference in terms of "h m"
		long[] hourMin = DateHelper.hoursMinutes(endTime.getTimeInMillisec()
				- startTime.getTimeInMillisec());

		// set the duration expression
		StringBuffer sb = new StringBuffer();
		sb.append("+");
		sb.append(hourMin[0]);
		sb.append("H");
		sb.append("+");
		sb.append(hourMin[1]);
		sb.append("m");
		m_durationExpression = sb.toString();

		// Calendar's set() method does not update the
		// time (also indicated in the Javadoc). So we
		// need to refresh the time based on the changes
		// above before setting the day of the week
		startTime.getDayOfWeek();
		// now set the start date based on the working day
		startTime.setDayOfWeek(m_workingDay.getDay());
		m_startDate = startTime.getDate();
	}

	/**
	 * Get the working day association state of this working hour. This value is
	 * used during creation/modification of a calendar in order to determine
	 * whether this working hour is an existing one, a new one, or the one that
	 * should be removed from the list of working hours for a particular working
	 * day.
	 * 
	 * @return The state for working day association.
	 */
	int getWorkingDayAssociationState()
	{
		return m_wkDayAssociationState;
	}

	/**
	 * Set the working day association state to be the specified value.
	 * 
	 * @param p_state -
	 *            The working day association state to be set.
	 */
	void setWorkingDayAssociationState(int p_state)
	{
		m_wkDayAssociationState = p_state;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Package Level Methods
	// ////////////////////////////////////////////////////////////////////
}
