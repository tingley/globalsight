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
package com.globalsight.scheduling;

import java.util.Calendar;

import org.quartz.impl.calendar.BaseCalendar;

/**
 * This class used to implement the function of schedule a job on a specified week of year.
 * Such as only run job at the 5th week of year.
 */
public class WeekOfYearCalendar extends BaseCalendar {

	private static final long serialVersionUID = 5892159265828710158L;
	
	/**
	 * The number of valid week in year.
	 * Between 1 to 53.
	 */
	private int weekOfYear;
	
	public WeekOfYearCalendar(int weekOfYear) {
		if (weekOfYear < 1 || weekOfYear > 53) {
			throw new IllegalArgumentException("Week of year must between 1 and 53.");
		}
		
		this.weekOfYear = weekOfYear;
	}

	public long getNextIncludedTime(long timeStamp) {
		long baseTime = super.getNextIncludedTime(timeStamp);
		if (baseTime > 0 && baseTime > timeStamp) {
			timeStamp = baseTime;
		}
		
		if (isTimeIncluded(timeStamp)) {
			return timeStamp;
		}
		
		Calendar calendar = createJavaCalendar(timeStamp);
		int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
		
		if (weekNumber > weekOfYear) {
			// Get the first valid day of next year
			calendar = getFirstDayOfValidWeek(calendar, true);
		} else if (weekNumber < weekOfYear) {
			// Get the first valid day of this year.
			calendar = getFirstDayOfValidWeek(calendar, false);
		} else {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}

		/**
		 * Sometimes there is no valid day in this year.
		 * Such as the 53th week.
		 * If set the week of year to 53, the calendar will return the first week of next year.
		 * In order to avoid this situation, check the calendar again and re-get it if necessary.
		 */
		if (!isTimeIncluded(calendar.getTimeInMillis())) {
			return getNextIncludedTime(calendar.getTimeInMillis());
		}
		
		return getStartOfDayJavaCalendar(calendar.getTimeInMillis()).getTimeInMillis();
	}
	
	/**
	 * Get the first valid day of specified week.
	 * 
	 * @param calendar
	 * @param nextYear If true, get the valid day of next year; otherwise get the valid day of this year.
	 * @return
	 */
	private Calendar getFirstDayOfValidWeek(Calendar calendar, boolean nextYear) {
		if (nextYear) {
			calendar.add(Calendar.YEAR, 1);
		}
		
		calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		
		return calendar;
	}

	public boolean isTimeIncluded(long timeStamp) {
		Calendar calendar = createJavaCalendar(timeStamp);
		return this.weekOfYear == calendar.get(Calendar.WEEK_OF_YEAR);
	}

}
