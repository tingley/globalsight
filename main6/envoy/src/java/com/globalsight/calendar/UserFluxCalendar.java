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

/**
 * BaseFluxCalendar is an abstract class used as the base class for Calendar and
 * UserCalendar classes..
 */
public class UserFluxCalendar extends BaseFluxCalendar
{
	private static final long serialVersionUID = -8422469446062559916L;

	/**
	 * Constant used for TopLink's query. The constant value has to be exactly
	 * the same as the variable defined as the owner's user id (m_ownerUserId)
	 * and parent calendar's id of a UserFluxCalendar (for mapping purposes).
	 */
	public static final String OWNER_USER_ID = "m_ownerUserId";
	public static final String PARENT_CALENDAR_ID = "m_parentCalendarId";

	// PRIVATE MEMBER VARIABLES
	private int m_activityBuffer = 0;
	private long m_parentCalendarId = -1;
	private String m_ownerUserId = null;

	/* Holds a list of events of type personal */
	private List m_personalReservedTimes = new ArrayList();
	/* Holds a list of activities of type proposed */
	private List m_proposedActivities = new ArrayList();
	/*
	 * Holds a list of general events, and activities (of type activity and
	 * buffer)
	 */
	private List m_reservedTimes = new ArrayList();

	// Non-persistent variables
	private FluxCalendar m_parentCalendar = null;

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Constructor used by TOPLink.
	 */
	public UserFluxCalendar()
	{
		super();
	}

	/**
	 * Create a UserFluxCalendar based on a set of required attributes. This
	 * constructor is used with no activity buffer is set (i.e. 0 hours)
	 * 
	 * @param p_parentCalendarId -
	 *            The id of the system calendar which this one was derived from.
	 * @param p_ownerUserId -
	 *            The username of the calendar's owner.
	 * @param p_timeZoneId -
	 *            The time zone id of this calendar.
	 */
	public UserFluxCalendar(long p_parentCalendarId, String p_ownerUserId,
			String p_timeZone)
	{
		this(p_parentCalendarId, p_ownerUserId, 0, p_timeZone);
	}

	/**
	 * Create a UserFluxCalendar based on a set of required attributes.
	 * 
	 * @param p_parentCalendarId -
	 *            The id of the system calendar which this one was derived from.
	 * @param p_ownerUserId -
	 *            The username of the calendar's owner.
	 * @param p_activityBuffer -
	 *            The buffer in hours which is used during task assignments.
	 *            It's added to the task's end date as a buffer.
	 * @param p_timeZoneId -
	 *            The time zone id of this calendar.
	 */
	public UserFluxCalendar(long p_parentCalendarId, String p_ownerUserId,
			int p_activityBuffer, String p_timeZone)
	{
		super(null, p_timeZone);
		m_parentCalendarId = p_parentCalendarId;
		m_ownerUserId = p_ownerUserId;
		m_activityBuffer = p_activityBuffer;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Abstract Methods Implementation
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Get the hour per business day conversion factor for this user calendar.
	 * This value is obtained from the parent calendar. The conversion factor
	 * should always be greater than zero.
	 * 
	 * @return The hour per business day conversion factor.
	 */
	public int getHoursPerDay()
	{
		return m_parentCalendar == null ? 0 : m_parentCalendar.getHoursPerDay();
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Abstract Methods Implementation
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Helper Methods
	// ////////////////////////////////////////////////////////////////////

	/**
	 * Add a reserved time (general event, actual activity, and buffer) to the
	 * list of this calendar's reserved times.
	 * 
	 * @param p_reservedTime -
	 *            The reserved time to be added.
	 */
	public void addReservedTime(ReservedTime p_reservedTime)
	{
		p_reservedTime.setUserFluxCalendar(this);
		p_reservedTime.computeDurationAndDates(getTimeZone());
		if (ReservedTime.TYPE_PROPOSED.equals(p_reservedTime.getType()))
		{
			m_proposedActivities.add(p_reservedTime);
		}
		else if (ReservedTime.TYPE_PERSONAL.equals(p_reservedTime.getType()))
		{
			m_personalReservedTimes.add(p_reservedTime);
		}
		else
		{
			m_reservedTimes.add(p_reservedTime);
		}
	}

	/**
	 * Get the buffer in hours which is used during task assignment.
	 * 
	 * @return The activity buffer in hours.
	 */
	public int getActivityBuffer()
	{
		return m_activityBuffer;
	}

	/**
	 * Set the activity buffer to be the specified value
	 */
	public void setActivityBuffer(int p_activityBuffer)
	{
		m_activityBuffer = p_activityBuffer;
	}

	/**
	 * Get the list of holiday objects for this calendar.
	 * 
	 * @return A list of calendar holidays.
	 */
	public List getHolidays()
	{
		return m_parentCalendar == null ? null : m_parentCalendar.getHolidaysList();
	}

	/**
	 * Get the user id of the owner of this calendar. Note that a user calendar
	 * is associate with only one user.
	 * 
	 * @return The user id of the calendar's owner.
	 */
	public String getOwnerUserId()
	{
		return m_ownerUserId;
	}

	public void setOwnerUserId(String ownerUserId) {
		m_ownerUserId = ownerUserId;
	}

	/**
	 * Get the id of the calendar where this calendar is derived from.
	 * 
	 * @return The parent calendar id.
	 */
	public long getParentCalendarId()
	{
		return m_parentCalendarId;
	}

	/**
	 * Set the parent calendar id for this user calendar.
	 * 
	 * @param p_parentCalendarId -
	 *            The id of the calendar that this user calendar is derived
	 *            from.
	 */
	public void setParentCalendarId(long p_parentCalendarId)
	{
		m_parentCalendarId = p_parentCalendarId;
	}

	/**
	 * Get the reserved times of type personal for this user calendar.
	 * 
	 * @return A list of reserved times of type personal.
	 */
	public List getPersonalReservedTimes()
	{
		return m_personalReservedTimes;
	}

    public void setPersonalReservedTimes(List personalReservedTimes)
    {
    	for (int i = 0, j = personalReservedTimes.size(); i < j; i++) {
    		ReservedTime reservedTime = (ReservedTime) personalReservedTimes.get(i);
    		if (ReservedTime.TYPE_PERSONAL.equals(reservedTime.getType()) 
    				&& !m_personalReservedTimes.contains(reservedTime)) {
    			m_personalReservedTimes.add(reservedTime);
    		}
    	}
    }

	/**
	 * Get the proposed activities for this user calendar.
	 * 
	 * @return A list of reserved times of type proposed.
	 */
	public List getProposedActivities()
	{
		return m_proposedActivities;
	}

    public void setProposedActivities(List proposedActivities)
    {
    	for (int i = 0, j = proposedActivities.size(); i < j; i++) {
    		ReservedTime reservedTime = (ReservedTime) proposedActivities.get(i);
    		if (ReservedTime.TYPE_PROPOSED.equals(reservedTime.getType()) 
    				&& !m_proposedActivities.contains(reservedTime)) {
    			m_proposedActivities.add(reservedTime);
    		}
    	}
    }

	/**
	 * Get the reserved times of this user calendar.
	 * 
	 * @return A list of reserved times.
	 */
	public List getReservedTimes()
	{
		return m_reservedTimes;
	}

    public void setReservedTimes(List reservedTimes)
    {
    	for (int i = 0, j = reservedTimes.size(); i < j; i++) {
    		ReservedTime reservedTime = (ReservedTime) reservedTimes.get(i);
    		if (!ReservedTime.TYPE_PERSONAL.equals(reservedTime.getType()) 
    				&& !ReservedTime.TYPE_PROPOSED.equals(reservedTime.getType()) 
    				&& !m_reservedTimes.contains(reservedTime)) {
    			m_reservedTimes.add(reservedTime);
    		}
    	}
    }

	/**
	 * Remove the specified reserved time from this calendar's list.
	 * 
	 * @param p_reservedTime -
	 *            The reserved time to be removed from this calendar.
	 */
	public void removeReservedTime(ReservedTime p_reservedTime)
	{
		List list = getCollectionByType(p_reservedTime.getType());
		ReservedTime rt = (ReservedTime) list.get(list.indexOf(p_reservedTime));

		// if this is a new calendar, just remove reserved time from the list
		if (getId() == -1 && rt != null)
		{
			list.remove(rt);
		}
		else if (rt != null)
		{
			// set the state to 'deleted'. During update,
			// it'll get removed.
			rt.setCalendarAssociationState(CalendarConstants.DELETED);
		}
	}

	/**
	 * Set the calendar which this user calendar is derived from.
	 * 
	 * @param p_parentCalendar -
	 *            The parent calendar of this one.
	 */
	public void setParentCalendar(FluxCalendar p_parentCalendar)
	{
		m_parentCalendar = p_parentCalendar;
		m_parentCalendarId = p_parentCalendar == null ? -1 : p_parentCalendar
				.getId();
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Public Helper Methods
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Package-scope Methods
	// ////////////////////////////////////////////////////////////////////

	/**
	 * Make a new copy of this user calendar with the constructor level
	 * attributes only.
	 * 
	 * @return A light copy of this user calendar.
	 */
	UserFluxCalendar cloneUserFluxCalendar()
	{
		UserFluxCalendar userCal = new UserFluxCalendar(getParentCalendarId(),
				getOwnerUserId(), getActivityBuffer(), getTimeZoneId());

		return userCal;
	}

	/*
	 * Since the events/activities are queried in a way that they are stored in
	 * different collections, we need to return the valid one based on the
	 * reserved time's type.
	 */
	List getCollectionByType(String p_reservedTimeType)
	{
		if (ReservedTime.TYPE_PROPOSED.equals(p_reservedTimeType))
		{
			return m_proposedActivities;
		}
		else if (ReservedTime.TYPE_PERSONAL.equals(p_reservedTimeType))
		{
			return m_personalReservedTimes;
		}
		else
		{
			return m_reservedTimes;
		}
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Package-scope Methods
	// ////////////////////////////////////////////////////////////////////
}
