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
 * This class is used to implement the function of schedule job on a specified day of year.
 * Such as the 165th day of year.
 */
public class DayOfYearCalendar extends BaseCalendar {

	private static final long serialVersionUID = 3132319621876358520L;
	
	/**
	 * The number of day in year.
	 * Between 1 to 366.
	 */
	private int dayOfYear;
	
	public DayOfYearCalendar(int dayOfYear) {
		if (dayOfYear < 1 || dayOfYear > 366) {
			throw new IllegalArgumentException("Day of year must between 1 and 366.");
		}
		
		this.dayOfYear = dayOfYear;
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
		if (calendar.get(Calendar.DAY_OF_YEAR) > dayOfYear) {
			calendar.add(Calendar.YEAR, 1);
		}
		calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
		
		if (isTimeIncluded(calendar.getTimeInMillis())) {
			return getStartOfDayJavaCalendar(calendar.getTimeInMillis()).getTimeInMillis();
		} else {
			/**
			 * If the number is 366, not all year contains this day.
			 * So the calendar will return the first day of the next year, which is not right.
			 * So re-get the next valid day.
			 */
			return getNextIncludedTime(calendar.getTimeInMillis());
		}
	}

	public boolean isTimeIncluded(long timeStamp) {
		Calendar calendar = createJavaCalendar(timeStamp);
		return dayOfYear == calendar.get(Calendar.DAY_OF_YEAR);
	}

}
