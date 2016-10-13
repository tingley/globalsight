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
import java.util.SortedSet;

import org.quartz.impl.calendar.BaseCalendar;

public class RangeCalendar extends BaseCalendar {
	
	private static final long serialVersionUID = -433919071540073030L;
	
	/**
	 * The end time of when the base calendar is effective
	 */
	private long endDate = 0;
	
	/**
	 * The start time of when the base calendar is effective
	 */
	private long startDate = 0;
	
	/**
	 * If this is true, all the time in this range will be forbidden.
	 * Otherwise check the time by base calendar.
	 */
	private boolean allForbidden = false;
	
	public RangeCalendar(long startDate, long endDate) {
		if (startDate < 0 || endDate < 0) {
			throw new IllegalArgumentException("The time range of RangeCalendar is invalid.");
		}
		
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public RangeCalendar(long endDate) {
		if (endDate < 0) {
			throw new IllegalArgumentException("The end date time is invalid.");
		}
		
		this.endDate = endDate;
	}
	
	public long getStartDate() {
		return startDate;
	}
	
	public long getEndDate() {
		return endDate;
	}
	
	public void setAllForbidden(boolean allForbidden) {
		this.allForbidden = allForbidden;
	}
	
	public boolean isAllForbidden() {
		return this.allForbidden;
	}
	
	public long getNextIncludedTime(long timeStamp) {
		/**
		 * If time is in range, and not all forbidden, return super result.
		 * Else return end date; 
		 */
		if (timeStamp >= startDate && timeStamp < endDate) {
			if (allForbidden) {
				// If all forbidden and time in range, return the end date.
				return endDate;
			} else {
				super.getNextIncludedTime(timeStamp);
			}
		} 

		return timeStamp;
	}

	public boolean isTimeIncluded(long timeStamp) {
		if (this.startDate <= timeStamp && this.endDate > timeStamp) {
			return !allForbidden && super.isTimeIncluded(timeStamp);
		}
		
		return true;
	}
}
