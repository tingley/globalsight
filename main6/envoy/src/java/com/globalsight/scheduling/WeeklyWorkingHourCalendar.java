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

public class WeeklyWorkingHourCalendar extends BaseCalendar {
	
	private static final long serialVersionUID = 4539151542497619206L;
	
	/**
	 * Which day of week should be checked.
	 * Valid value is 1-7 as sun-sat.
	 * @see java.util.Calendar#DAY_OF_WEEK
	 */
	private int dayOfWeek = 0;
	
	public WeeklyWorkingHourCalendar(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public long getNextIncludedTime(long timeStamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeStamp);
		
		// This calendar has no excluded time, just return the result of base calendar.
		return dayOfWeek == calendar.get(Calendar.DAY_OF_WEEK) 
				? super.getNextIncludedTime(timeStamp) : timeStamp;
	}

	public boolean isTimeIncluded(long timeStamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeStamp);
		
		// If the day of week is matched, return base calendar's result.
		// Otherwise always return true.
		return dayOfWeek != calendar.get(Calendar.DAY_OF_WEEK) 
				|| super.isTimeIncluded(timeStamp);
	}
}
