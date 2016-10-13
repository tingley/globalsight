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

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.date.DateHelper;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReservedTime is an object representing a time blocked for a user calendar.
 */
public class ReservedTime extends PersistentObject
{
	private static final long serialVersionUID = -8008780873112642063L;

	// PUBLIC CONSTANTS
	/* The valid repeat expressions */
	public static final String EVERY_DAY = "+d";
	public static final String EVERY_WEEK = "+w";
	public static final String EVERY_MONTH = "+M";
	public static final String EVERY_OTHER_DAY = "+2d";
	public static final String EVERY__OTHER_WEEK = "+2w";
	public static final String EVERY_OTHER_MONTH = "+2M";

	/* The valid types for a reserved time */
	public static final String TYPE_ACTIVITY = "ACCEPTED";
	public static final String TYPE_BUFFER = "BUFFER";
	public static final String TYPE_EVENT = "EVENT";
	public static final String TYPE_PERSONAL = "PERSONAL";
	public static final String TYPE_PROPOSED = "ASSIGNED";
	//
	// PRIVATE MEMBER VARIABLES
	//
	private int m_endHour = -1;
	private int m_endMinute = -1;
	private int m_startHour = -1;
	private int m_startMinute = -1;
	private Date m_endDate = null;
	private Date m_startDate = null;
	private Long m_taskId = null;//Long is used to avoid inserting 0 or -1 in db
	private String m_durationExpression = null;
	private String m_repeatExpression = null;
	private String m_subject = null;
	private String m_type = null;

	// Timestamp is used as it can provide support for UI
	// and other update features
	private Timestamp m_startTimestamp = null;
	private Timestamp m_endTimestamp = null;

	// used for TOPLink's back pointer
	private UserFluxCalendar m_userFluxCalendar = null;

	// The default state of this reserved time. For a new one, once
	// added to a calendar, it'll change to 'NEW'.
	private int m_calendarAssociationState = CalendarConstants.EXISTING;

	// A time expression indication 24 hours (a whole day)
	private static String s_allDayTimeExpression = "+24H+0m";

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Create an initialized ReservedTime (used ONLY by TOPLink).
	 */
	public ReservedTime()
	{
		super();
	}

	/**
	 * Constructor used during creation of a reserved time.
	 */
	public ReservedTime(String p_subject, String p_type,
			Timestamp p_startTimestamp, int p_startHour, int p_startMinute,
			Timestamp p_endTimestamp, int p_endHour, int p_endMinute,
			String p_repeatExpression)
	{
		this(p_subject, p_type, p_startTimestamp, p_startHour, p_startMinute,
				p_endTimestamp, p_endHour, p_endMinute, p_repeatExpression,
				null);
	}

	/**
	 * Constructor used during creation of a reserved time for an accepted task.
	 */
	public ReservedTime(String p_subject, String p_type,
			Timestamp p_startTimestamp, int p_startHour, int p_startMinute,
			Timestamp p_endTimestamp, int p_endHour, int p_endMinute,
			String p_repeatExpression, Long p_taskId)
	{
		super();

		m_endTimestamp = p_endTimestamp;
		m_endHour = p_endHour;
		m_endMinute = p_endMinute;
		m_startTimestamp = p_startTimestamp;
		m_startHour = p_startHour;
		m_startMinute = p_startMinute;
		m_subject = p_subject;
		m_taskId = p_taskId;
		m_type = p_type;
		computeDates(null);

		m_repeatExpression = p_repeatExpression == null ? s_allDayTimeExpression
				: p_repeatExpression;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Methods
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Get the user calendar association state of this reserved time. This value
	 * is used during creation/modification of a calendar in order to determine
	 * whether this reserved time is an existing one, a new one, or the one that
	 * should be removed from the list of reserved times for a particular user
	 * calendar.
	 * 
	 * @return The state for calendar association.
	 */
	public int getCalendarAssociationState()
	{
		return m_calendarAssociationState;
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
	 * Get the end date for this reserved time.
	 * 
	 * @return The reserved time's end date.
	 */
	public Date getEndDate()
	{
		return m_endDate;
	}

	public void setEndDate(Date endDate)
	{
		m_endDate = endDate;
	}

	/**
	 * Get the end hour for this reserved time. Note that a valid value is
	 * between 0 to 23 for Hour.
	 * 
	 * @return The ending hour of the reserved time.
	 */
	public int getEndHour()
	{
		return m_endHour;
	}

	/**
	 * Get the ending minute for this reserved time. Note that a valid value is
	 * between 0 to 50.
	 * 
	 * @return The ending minute.
	 */
	public int getEndMinute()
	{
		return m_endMinute;
	}

	/**
	 * Get the end date for this reserved time in the form of a Timestamp.
	 * 
	 * @return The reserved time's end date as Timestamp object.
	 */
	public Timestamp getEndTimestamp()
	{
		if (m_endTimestamp == null && m_endDate != null)
		{
			m_endTimestamp = new Timestamp(getTimeZone());
			m_endTimestamp.setDate(m_endDate);
		}
		return m_endTimestamp;
	}

	/**
	 * Get the repeat expression for this reserved time. Note that a reserved
	 * expression is one of the valid types defined as constants in this class.
	 * A null value means that the reserved time is non-repeatable.
	 * 
	 * @return The reserved time's repeat expression (if any).
	 */
	public String getRepeatExpression()
	{
		return m_repeatExpression == null ? s_allDayTimeExpression
				: m_repeatExpression;
	}

	/**
	 * Set the repeat expression for this reserved time.
	 * 
	 * @param p_repeatExpression -
	 *            The value to be set.
	 */
	public void setRepeatExpression(String p_repeatExpression)
	{
		m_repeatExpression = p_repeatExpression;
	}

	/**
	 * Get the start date for this reserved time.
	 * 
	 * @return The reserved time's start date.
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
	 * Get the start hour for this reserved time. Note that a valid value is
	 * between 0 to 23.
	 * 
	 * @return The starting hour.
	 */
	public int getStartHour()
	{
		return m_startHour;
	}

	/**
	 * Get the start minute for this reserved time. Note that a valid value is
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
		if (m_startTimestamp != null)
		{
			m_startTimestamp.setMinute(p_startMinute);
		}
	}

	/**
	 * Get the start date for this reserved time in the form of Timestamp.
	 * 
	 * @return The reserved time's start date as a Timestamp object.
	 */
	public Timestamp getStartTimestamp()
	{
		if (m_startTimestamp == null && m_startDate != null)
		{
			m_startTimestamp = new Timestamp(getTimeZone());
			m_startTimestamp.setDate(m_startDate);
		}
		return m_startTimestamp;
	}

	/**
	 * Get the subject of this reserved time.
	 * 
	 * @return This reserved time's subject.
	 */
	public String getSubject()
	{
		return m_subject;
	}

	/**
	 * Set the subject of this reserved time to be the specified value.
	 * 
	 * @param p_subject
	 *            The subject to be set.
	 */
	public void setSubject(String p_subject)
	{
		m_subject = p_subject;
	}
	
	public String getDisplaySubject()
	{
		if (m_subject == null)
			return "";
		
		Pattern p = Pattern.compile("(\\[[^\\]]*\\]\\[[^\\]]*\\])\\[([^\\]]*)\\]");
		Matcher m = p.matcher(m_subject);
		if (m.find())
		{
			String s1 = m.group(1);
			String s2 = m.group(2);
			String name = UserUtil.getUserNameById(s2);
			return s1 + "[" + name + "]";
		}
		
		return m_subject;
	}

	/**
	 * Get the task id from which this reserved time was created. Note that a
	 * valid id will only be returned if the reserved time was created based on
	 * a specific activity (i.e. accepting the activity by the calendar owner).
	 * 
	 * @return The task id.
	 */
	public Long getTaskId()
	{
		return m_taskId;
	}

	/*
	 * Set the task id for this reserved time to be the specified value.
	 * 
	 * @param p_taskId The task id to be set (indicated that this time is
	 * reserved for the given task id).
	 */
	public void setTaskId(Long p_taskId)
	{
		m_taskId = p_taskId;
	}

	/**
	 * Get the type of this reserved time.
	 * 
	 * @return This reserved time's type.
	 */
	public String getType()
	{
		return m_type;
	}

	/**
	 * Set the type of this reserved time to be the specified value.
	 * 
	 * @param p_type
	 *            The type to be set.
	 */
	public void setType(String p_type)
	{
		m_type = p_type;
	}

	/**
	 * Get the user calendar which this reserved time belongs to.
	 * 
	 * @return The reserved time's user calendar.
	 */
	public UserFluxCalendar getUserFluxCalendar()
	{
		return m_userFluxCalendar;
	}

	/*
	 * Set the user calendar where this reserved time belongs to.
	 * 
	 * @param p_userFluxCalendar The user calendar where this reserved time
	 * belongs to.
	 */
	public void setUserFluxCalendar(UserFluxCalendar p_userFluxCalendar)
	{
		m_userFluxCalendar = p_userFluxCalendar;
		// since this back pointer is set when a reserved time is
		// added to a calendar, we'll set the associate state here.
		m_calendarAssociationState = CalendarConstants.NEWLY_ADDED;
	}

	/**
	 * Determines whether this reserved time is for the whole day. The whole day
	 * means from midnight of a particular day to the midnight of the next day.
	 * 
	 * @return True if the reserved time is set for the whole day (the start
	 *         hour, start minute, end hour, and end minute are all zero).
	 */
	public boolean isAllDay()
	{
		return m_startHour == 0 && m_startMinute == 0 && m_endHour == 0
				&& m_endMinute == 0;
	}

	/**
	 * Determines whether this reserved time is for one day only.
	 * 
	 * @return True if the reserved time is set for one day only. Therefore, the
	 *         end time should be only 1 day greater than the start time.
	 */
	public boolean isOneDayOnly()
	{
		return m_startTimestamp != null
				&& m_endTimestamp != null
				&& (m_endTimestamp.getTimeInMillisec()
						- m_startTimestamp.getTimeInMillisec() == 86400000);
	}

	/**
	 * Determines whether this reserved time falls within the given date.
	 * 
	 * @return True if the reserved time falls within the given date.
	 */
	public boolean isReservedForGivenDate(Date p_date, TimeZone p_timeZone)
	{
		if (getStartTimestamp() == null)
		{
			return false;
		}

		boolean isReservedForGivenDate = false;
		Timestamp begin = new Timestamp(p_timeZone);
		begin.setDate(p_date);
		begin.resetTimeOfDay();

		Timestamp end = new Timestamp(p_timeZone);
		end.setDate(begin.getDate());
		end.add(Timestamp.DAY, 1);

		isReservedForGivenDate = m_startTimestamp.isGreaterThanOrEqualTo(begin)
				&& m_startTimestamp.isLessThan(end);

		// if there's no time stamp or it's already determined that the
		// reserved time falls on the given date, don't go here...
		if (getEndTimestamp() != null && !isReservedForGivenDate)
		{
			isReservedForGivenDate = begin
					.isGreaterThanOrEqualTo(m_startTimestamp)
					&& begin.isLessThan(m_endTimestamp);
		}

		return isReservedForGivenDate;
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
		if (m_endTimestamp != null)
		{
			m_endTimestamp.setHour(p_endHour);
		}
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
		if (m_endTimestamp != null)
		{
			m_endTimestamp.setMinute(p_endMinute);
		}
	}

	/**
	 * Set the ending date of this reserved time to be the specified value.
	 * 
	 * @param p_endDate
	 *            The end Timestamp to be set.
	 */
	public void setEndTimestamp(Timestamp p_endTimestamp)
	{
		m_endTimestamp = p_endTimestamp;
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
		if (m_startTimestamp != null)
		{
			m_startTimestamp.setHour(p_startHour);
		}
	}

	/**
	 * Set the start Timestamp of this reserved time to be the specified value.
	 * 
	 * @param p_startDate
	 *            the start Timestamp to be set.
	 */
	public void setStartTimestamp(Timestamp p_startTimestamp)
	{
		m_startTimestamp = p_startTimestamp;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Public Methods
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Override Methods
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Indicates whether some other ReservedTime is "equal to" this one.
	 * 
	 * @param p_reservedTime
	 *            The reference object with which to compare.
	 * @return true if this reserved time object is the same as the
	 *         p_reservedTime argument; false otherwise.
	 */
	public boolean equals(Object p_reservedTime)
	{
		if (p_reservedTime instanceof ReservedTime)
		{
			return (getId() == ((ReservedTime) p_reservedTime).getId());
		}
		return false;
	}

	/**
	 * The hashCode method is overridden to support the 'equals' method. If two
	 * ReservedTime objects are equal according to the equals(Object) method,
	 * then calling the hashCode method on each of the two objects will produce
	 * the same integer result.
	 * 
	 * @return the reserved time id.
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
	 * Compute the duration and start/end dates which will be used by Flux when
	 * a date calculation is performed. This method should be called anytime a
	 * reserved time is created or modified. The duration is calculated as a
	 * combination of hour and minute (i.e. 2 hours and 30 minutes) and set as
	 * "+2H+30m" time expression.
	 */
	void computeDurationAndDates(TimeZone p_timeZone)
	{
		computeDuration();
		computeDates(p_timeZone);
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

	// ////////////////////////////////////////////////////////////////////
	// End: Package Level Methods
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Private Methods
	// ////////////////////////////////////////////////////////////////////
	/*
	 * Compute the duration expression (difference between end time and start
	 * time).
	 */
	private void computeDuration()
	{
		// for a task there's always one buffer day. So book the whole day.
		if (m_taskId != null)
		{
			m_durationExpression = s_allDayTimeExpression;
		}
		else
		{
			Timestamp startTime = new Timestamp();
			startTime.resetTimeOfDay();
			startTime.setHour(m_startHour);
			startTime.setMinute(m_startMinute);

			Timestamp endTime = new Timestamp();
			// if ending hour is less than start hour (i.e. 0 for 12AM)
			// then add one day to the current day. This can also
			// happen if the user has 10PM to 2AM working hours. Also
			// for a whole day we'll use 0 for start and end time
			if (m_endHour <= m_startHour
					|| (m_endHour == 0 && m_startHour == 0))
			{
				endTime.add(Timestamp.DAY, 1);
			}
			endTime.resetTimeOfDay();
			endTime.setHour(m_endHour);
			endTime.setMinute(m_endMinute);

			// now convert the difference in terms of "h m"
			long[] hourMin = DateHelper.hoursMinutes(endTime
					.getTimeInMillisec()
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
		}
	}

	/*
	 * Update the start date's time to be based on the start hour and start
	 * minute. Do the same for the end date.
	 */
	private void computeDates(TimeZone p_timeZone)
	{
		// no setting required during instantiation of ReservedTime
		if (p_timeZone != null)
		{
			getStartTimestamp().setTimeZone(p_timeZone);
		}
		m_startTimestamp.resetTimeOfDay();
		m_startTimestamp.setHour(m_startHour);
		m_startTimestamp.setMinute(m_startMinute);
		m_startDate = m_startTimestamp.getDate();

		// end date can be null
		if (m_endTimestamp != null)
		{
			// no setting required during instantiation of ReservedTime
			if (p_timeZone != null)
			{
				m_endTimestamp.setTimeZone(p_timeZone);
			}
			m_endTimestamp.resetTimeOfDay();
			m_endTimestamp.setHour(m_endHour);
			m_endTimestamp.setMinute(m_endMinute);
			m_endDate = m_endTimestamp.getDate();
		}
	}

	/*
	 * Return the time zone of the calendar.
	 */
	private TimeZone getTimeZone()
	{
		return m_userFluxCalendar == null ? null : m_userFluxCalendar
				.getTimeZone();
	}
	// ////////////////////////////////////////////////////////////////////
	// End: Private Methods
	// ////////////////////////////////////////////////////////////////////
}
