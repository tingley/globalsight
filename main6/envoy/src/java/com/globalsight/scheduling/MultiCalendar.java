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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;

import org.quartz.Calendar;
import org.quartz.impl.calendar.AnnualCalendar;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.impl.calendar.DailyCalendar;
import org.quartz.impl.calendar.WeeklyCalendar;

/**
 * Class <code>MultiCalendar</code> is a sub class of <code>{@link org.quartz.impl.calendar.BaseCalendar}</code>.<p>
 * It supports multi-calendar definition. 
 * Such as a weekly calendar and a annual calendar.
 * The effective date will be the intersection of all the calendars.
 * <pre>
 * For example:
 *   If a weekly calendar excludes Wednesday, and a annual calendar excludes new Year day.
 *   The following days will be included in the schedule for each calendar:
 *       Weekly calendar:  2008.1.1 (Tuesday) 2008.1.3 (Thursday) 
 *       Annual calendar:  2008.1.2           2008.1.3
 *       
 *   If add both the two calendars into the MultiCalendar, the result will be only 2008.1.3.
 *   Both the first (New year day) and second day (Wednesday) of 2008 year will not be included in the schedule time.  
 * </pre>  
 */
public class MultiCalendar extends BaseCalendar {

	private static final long serialVersionUID = 8916562072594195043L;
	
	public static final int MILLION_SECOND_OF_ONE_DAY = 24 * 60 * 60 * 1000;
	
	/**
	 * List of Calendars.
	 */
	private List<Calendar> calendars = new ArrayList<Calendar>(); 
	
	/**
	 * If this is true, the result will be valid for all calendars;
	 * otherwise, the result will be valid for at least one calendar. 
	 */
	private boolean isIntersect = true;
	
	public MultiCalendar() {
		this(null, null);
	}
	
	public MultiCalendar(Calendar calendar) {
		this(calendar, null);
	}
	
	public MultiCalendar(TimeZone timeZone) {
		super(null, timeZone);
	}
	
	public MultiCalendar(Calendar calendar, TimeZone timeZone) {
		super(calendar, timeZone);
	}
	
	/**
	 * Add a Calendar.
	 * 
	 * @param calendar
	 */
	public void addCalendar(Calendar calendar) {
		if (calendar != null) {
			this.calendars.add(calendar);
		}
	}
	
	public void setIsIntersect(boolean isIntersect) {
		this.isIntersect = isIntersect;
	}
	
	/**
	 * Clear all Calendars.
	 */
	public void clear() {
		this.calendars.clear();
	}

	/**
	 * Return the next included time.
	 */
	public long getNextIncludedTime(long timeStamp) {
		long nextTime = getNextMayIncludedTime(timeStamp);
		long invalidTime = -1;
		
		/**
		 * The the nextTime is not included by all the Calendars,
		 * get the next may included time.
		 * 
		 * InvalidTime used to check whether the nextTime has not been changed.
		 * If a invalid time has been returned twice, that means no more valid time exists.
		 */
		while(!isTimeIncluded(nextTime) 
				&& (invalidTime == -1 || invalidTime != nextTime)) {
			invalidTime = nextTime;
			nextTime = getNextMayIncludedTime(nextTime);
			// If the next time is less than the invalid time, means no more valid time exists.
			if (invalidTime >= nextTime) {
				return nextTime;
			}
		}
		
		return nextTime;
	}
	
	/**
	 * Return the latest possible next included time.
	 * Add the middle possible time will be ignored, 
	 * because the result will be the  
	 * 
	 * @param timeStamp
	 * @return
	 */
	private long getNextMayIncludedTime(long timeStamp) {
		boolean firstCalendar = true;
		long nextTime = 0;
		long latestTime = 0;
		
		for (Calendar subCalendar : calendars) {
			nextTime = subCalendar.getNextIncludedTime(timeStamp);
			
			// If this subCalendar has no more valid time, it will return a timestamp less than current one.
			if (nextTime < timeStamp) {
				return nextTime;
			}
			
			if (firstCalendar) {
				latestTime = nextTime;
				firstCalendar = false;
			}
			
			if ((nextTime > latestTime) == isIntersect) {
				latestTime = nextTime;
			}
		}
		
		return latestTime;
	}

	/**
	 * If the parameter time is included by all the Calendars, return true.
	 * Otherwise return false.
	 */
	public boolean isTimeIncluded(long timeStamp) {
		
		for (Calendar calendar : calendars) {
			if (isIntersect) {
				if (!calendar.isTimeIncluded(timeStamp)) {
					return false;
				}
			} else {
				if (calendar.isTimeIncluded(timeStamp)) {
					return true;
				}
			}
		}
		
		return isIntersect;
	}
	
	public void makeRange(java.util.Calendar startDate, java.util.Calendar endDate, SortedSet<ExcludedTimeRange> excludes) {
		java.util.Calendar begin = (java.util.Calendar) startDate.clone();
		begin.setTimeZone(TimeZone.getDefault());
		java.util.Calendar end = (java.util.Calendar) endDate.clone();
		end.setTimeZone(TimeZone.getDefault());
		
		for (Calendar calendar : calendars) {
			makeRange(calendar, begin, end, excludes);
		}
		
	}
	
	public void makeRange(Calendar calendar, java.util.Calendar startDate, java.util.Calendar endDate, SortedSet<ExcludedTimeRange> excludes) {
		if (calendar instanceof MultiCalendar) {
			((MultiCalendar) calendar).makeRange(startDate, endDate, excludes);
			return;
		}
		
		if (calendar instanceof WeeklyCalendar) {
			WeeklyCalendar weeklyCalendar = (WeeklyCalendar) calendar;
			
			if (weeklyCalendar.areAllDaysExcluded()) {
				excludes.add(new ExcludedTimeRange(startDate.getTime(), endDate.getTime()));
			}
			
			boolean[] excludedDays = weeklyCalendar.getDaysExcluded();
			java.util.Calendar firstTimeOfStartDate = getStartOfDayJavaCalendar(startDate.getTimeInMillis());
			java.util.Calendar firstTimeOfEndDate = getStartOfDayJavaCalendar(endDate.getTimeInMillis());
			java.util.Calendar endTimeOfEndDate = getEndOfDayJavaCalendar(endDate.getTimeInMillis());
			Date begin;
			Date end;
			
			for (int i = 1; i < excludedDays.length ; i ++) {
				
				if (excludedDays[i]) {
					java.util.Calendar startTime = getStartOfDayJavaCalendar(startDate.getTimeInMillis());
					startTime.set(java.util.Calendar.DAY_OF_WEEK, i);
					java.util.Calendar endTime = getEndOfDayJavaCalendar(startTime.getTimeInMillis());
					
					// Check the first time range
					if (startTime.equals(firstTimeOfStartDate)) {
						// If start date is the same as the week day
						begin = startDate.getTime();
						end = new Date(endTime.getTimeInMillis() + 1);
						
						if (endTime.equals(endTimeOfEndDate)) {
							// This case is duration in one day
							end = endDate.getTime();
						}
						
						excludes.add(new ExcludedTimeRange(begin, end));
					} else if (startTime.after(firstTimeOfStartDate) 
							&& !startTime.after(firstTimeOfEndDate) 
							&& startTime.before(endDate)) {
						// Start time is later than start date, but before end date
						begin = startTime.getTime();
						end = new Date(endTime.getTimeInMillis() + 1);
						
						if (endTime.equals(endTimeOfEndDate)) {
							end = endDate.getTime();
						}
						
						excludes.add(new ExcludedTimeRange(begin, end));
					}
					
					startTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
					endTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
					
					// Add one week and test the invalid days
					while (!startTime.after(firstTimeOfEndDate) && startTime.before(endDate)) {
						begin = startTime.getTime();
						end = new Date(endTime.getTimeInMillis() + 1);
						
						if (endTime.equals(endTimeOfEndDate)) {
							end = endDate.getTime();
						}
						
						excludes.add(new ExcludedTimeRange(begin, end));
						
						startTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
						endTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
					}
					
				}
			}
			
			return;
		}
		
		if (calendar instanceof AnnualCalendar) {
			AnnualCalendar annualCalendar = (AnnualCalendar) calendar;
			
			List<java.util.Calendar> excludedDays = annualCalendar.getDaysExcluded();
			
			java.util.Calendar firstTimeOfStartDate = getStartOfDayJavaCalendar(startDate.getTimeInMillis());
			java.util.Calendar firstTimeOfEndDate = getStartOfDayJavaCalendar(endDate.getTimeInMillis());
			java.util.Calendar endTimeOfEndDate = getEndOfDayJavaCalendar(endDate.getTimeInMillis());
			int startYear = startDate.get(java.util.Calendar.YEAR);
			Date begin;
			Date end;
			
			for (java.util.Calendar excludedDay : excludedDays) {
				java.util.Calendar startTime = getStartOfDayJavaCalendar(excludedDay.getTimeInMillis());
				startTime.set(java.util.Calendar.YEAR, startYear);
				java.util.Calendar endTime = getEndOfDayJavaCalendar(startTime.getTimeInMillis());
				
				if (!startTime.before(firstTimeOfStartDate) && !startTime.after(firstTimeOfEndDate) && startTime.before(endDate)) {
					begin = startTime.getTime();
					end = new Date(endTime.getTimeInMillis() + 1);
					
					if (startTime.equals(firstTimeOfStartDate)) {
						begin = startDate.getTime();
					}
					
					if (endTime.equals(endTimeOfEndDate)) {
						end = endDate.getTime();
					}
					
					excludes.add(new ExcludedTimeRange(begin, end));
				}
				
				startTime.add(java.util.Calendar.YEAR, 1);
				endTime.add(java.util.Calendar.YEAR, 1);
				
				while (!startTime.before(firstTimeOfStartDate) && !startTime.after(firstTimeOfEndDate) && startTime.before(endDate)) {
					begin = startTime.getTime();
					end = new Date(endTime.getTimeInMillis() + 1);
					
					if (startTime.equals(firstTimeOfStartDate)) {
						begin = startDate.getTime();
					}
					
					if (endTime.equals(endTimeOfEndDate)) {
						end = endDate.getTime();
					}
					
					excludes.add(new ExcludedTimeRange(begin, end));
					
					startTime.add(java.util.Calendar.YEAR, 1);
					endTime.add(java.util.Calendar.YEAR, 1);
				}
			}
			
			return;
		}
		
		if (calendar instanceof RangeCalendar) {
			RangeCalendar rangeCalendar = (RangeCalendar) calendar;
			
			long startTime = rangeCalendar.getStartDate();
			long endTime = rangeCalendar.getEndDate();
			java.util.Calendar startCalendar = java.util.Calendar.getInstance();
			startCalendar.setTimeInMillis(startTime);
			java.util.Calendar endCalendar = java.util.Calendar.getInstance();
			endCalendar.setTimeInMillis(endTime);
			java.util.Calendar end;
			
			if (endTime > startDate.getTimeInMillis()) {
				if (startTime <= startDate.getTimeInMillis()) {
					end = endCalendar;
					if (endTime >= endDate.getTimeInMillis()) {
						end = endDate;
					}
					
					if (rangeCalendar.isAllForbidden()) {
						excludes.add(new ExcludedTimeRange(startDate.getTime(), end.getTime()));
					} else {
						makeRange(rangeCalendar.getBaseCalendar(), startDate, end, excludes);
					}
				} else if (startTime > startDate.getTimeInMillis() && startTime < endDate.getTimeInMillis()) {
					end = endCalendar;
					if (endTime >= endDate.getTimeInMillis()) {
						end = endDate;
					}
					
					if (rangeCalendar.isAllForbidden()) {
						excludes.add(new ExcludedTimeRange(startCalendar.getTime(), end.getTime()));
					} else {
						makeRange(rangeCalendar.getBaseCalendar(), startCalendar, endCalendar, excludes);
					}
				}
			}
			
			return;
		}
		
		if (calendar instanceof WeeklyWorkingHourCalendar) {
			WeeklyWorkingHourCalendar weeklyWorkingHourCalendar = (WeeklyWorkingHourCalendar) calendar;
			int dayOfWeek = weeklyWorkingHourCalendar.getDayOfWeek();
			
			SortedMap<java.util.Calendar, Boolean> points = new TreeMap<java.util.Calendar, Boolean>();
			
			Calendar baseCalendar = weeklyWorkingHourCalendar.getBaseCalendar();
			long refDate = System.currentTimeMillis();
			
			points.put(getStartOfDayJavaCalendar(refDate), Boolean.FALSE);
			java.util.Calendar endDayCalendar = getEndOfDayJavaCalendar(refDate);
			points.put(endDayCalendar, Boolean.FALSE);
			
			if (baseCalendar instanceof MultiCalendar) {
				MultiCalendar multiCalendar = (MultiCalendar) baseCalendar;
				
				for (Calendar dailyCalendar : multiCalendar.calendars) {
					addDailyPoints((DailyCalendar) dailyCalendar, refDate, points);
				}
				
			} else if (baseCalendar instanceof DailyCalendar) {
				addDailyPoints((DailyCalendar) baseCalendar, refDate, points);
			}
			
			SortedMap<java.util.Calendar, Boolean> finalPoints = new TreeMap<java.util.Calendar, Boolean>();
			
			Set<java.util.Calendar> keys = points.keySet();
			boolean firstPoint = true;
			boolean priousFlag = false;
			for (java.util.Calendar key : keys) {
				if (firstPoint) {
					priousFlag = points.get(key);
					if (!points.get(key)) {
						finalPoints.put(key, Boolean.FALSE);
						firstPoint = false;
					}
				} else {
					if (points.get(key) != priousFlag) {
						finalPoints.put(key, points.get(key));
						priousFlag = points.get(key);
					}
				}
			}
			
			java.util.Calendar startTime = getStartOfDayJavaCalendar(startDate.getTimeInMillis());
			startTime.set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek);
			java.util.Calendar endTime = getEndOfDayJavaCalendar(startTime.getTimeInMillis());
			java.util.Calendar firstTimeOfStartDate = getStartOfDayJavaCalendar(startDate.getTimeInMillis());
			java.util.Calendar firstTimeOfEndDate = getStartOfDayJavaCalendar(endDate.getTimeInMillis());
			java.util.Calendar endTimeOfEndDate = getEndOfDayJavaCalendar(endDate.getTimeInMillis());
			java.util.Calendar begin = null;
			java.util.Calendar end = null;
			
			while (startTime.before(firstTimeOfStartDate)) { 
				startTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
				endTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
			}
			
			while (!startTime.after(firstTimeOfEndDate)) {
				begin = startTime;
				end = endTime;
				
				if (startTime.equals(firstTimeOfStartDate)) {
					begin = startDate;
				}
				
				if (endTime.equals(endTimeOfEndDate)) {
					end = endDate;
				}
				
				addExcludesOfDailyTime(begin, end, finalPoints, excludes, refDate);
				
				startTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
				endTime.add(java.util.Calendar.WEEK_OF_MONTH, 1);
			}
			
			return;
		}
		
		throw new IllegalArgumentException("Class type " + calendar.getClass().getName() + " is invalid for Calendar");
	}
	
	private void addDailyPoints(DailyCalendar dailyCalendar, long refDate, SortedMap<java.util.Calendar, Boolean> points) {
		
		if (dailyCalendar.getInvertTimeRange()) {
			java.util.Calendar startPoint = java.util.Calendar.getInstance();
			startPoint.setTimeInMillis(dailyCalendar.getTimeRangeStartingTimeInMillis(refDate));
			points.put(startPoint, Boolean.TRUE);
			
			java.util.Calendar endPoint = java.util.Calendar.getInstance();
			endPoint.setTimeInMillis(dailyCalendar.getTimeRangeEndingTimeInMillis(refDate) + 1);
			points.put(endPoint, Boolean.FALSE);
		} else {
			points.put(getStartOfDayJavaCalendar(refDate), Boolean.TRUE);
			points.put(getEndOfDayJavaCalendar(refDate), Boolean.TRUE);
			
			java.util.Calendar startPoint = java.util.Calendar.getInstance();
			startPoint.setTimeInMillis(dailyCalendar.getTimeRangeStartingTimeInMillis(refDate));
			points.put(startPoint, Boolean.FALSE);
			
			java.util.Calendar endPoint = java.util.Calendar.getInstance();
			endPoint.setTimeInMillis(dailyCalendar.getTimeRangeEndingTimeInMillis(refDate) + 1);
			points.put(endPoint, Boolean.TRUE);
		}
	}
	
	private void addExcludesOfDailyTime(java.util.Calendar begin,
			java.util.Calendar end,
			Map<java.util.Calendar, Boolean> points,
			SortedSet<ExcludedTimeRange> excludes, long refDate) {
		int year = begin.get(java.util.Calendar.YEAR);
		int month = begin.get(java.util.Calendar.MONTH);
		int day = begin.get(java.util.Calendar.DAY_OF_MONTH);
		
		java.util.Calendar refStartTime = java.util.Calendar.getInstance();
		refStartTime.setTimeInMillis(refDate);
		refStartTime.set(java.util.Calendar.HOUR_OF_DAY, begin.get(java.util.Calendar.HOUR_OF_DAY));
		refStartTime.set(java.util.Calendar.MINUTE, begin.get(java.util.Calendar.MINUTE));
		refStartTime.set(java.util.Calendar.SECOND, begin.get(java.util.Calendar.SECOND));
		refStartTime.set(java.util.Calendar.MILLISECOND, begin.get(java.util.Calendar.MILLISECOND));
		
		java.util.Calendar refEndTime = java.util.Calendar.getInstance();
		refEndTime.setTimeInMillis(refDate);
		refEndTime.set(java.util.Calendar.HOUR_OF_DAY, end.get(java.util.Calendar.HOUR_OF_DAY));
		refEndTime.set(java.util.Calendar.MINUTE, end.get(java.util.Calendar.MINUTE));
		refEndTime.set(java.util.Calendar.SECOND, end.get(java.util.Calendar.SECOND));
		refEndTime.set(java.util.Calendar.MILLISECOND, end.get(java.util.Calendar.MILLISECOND));
		
		boolean isCurrentRangeValid = true;
		boolean firstRange = true;
		java.util.Calendar rangeBegin = null;
		java.util.Calendar rangeEnd = null;
		
		for (java.util.Calendar key : points.keySet()) {
			if (key.before(refStartTime)) {
				isCurrentRangeValid = points.get(key);
			} else {
				if (!key.before(refEndTime)) {
					if (isCurrentRangeValid) {
						excludes.add(new ExcludedTimeRange(end.getTime(), end.getTime()));
					} else {
						excludes.add(new ExcludedTimeRange(rangeBegin.getTime(), end.getTime()));
					}
					
					isCurrentRangeValid = true;
					break;
				}
				
				if (firstRange) {
					if (isCurrentRangeValid) {
						if (!points.get(key)) {
							rangeBegin = getRealCalendar(key, year, month, day);
							isCurrentRangeValid = false;
							firstRange = false;
						}
					} else {
						rangeBegin = getRealCalendar(begin, year, month, day);
						
						if (points.get(key)) {
							rangeEnd = getRealCalendar(key, year, month, day);
							excludes.add(new ExcludedTimeRange(rangeBegin.getTime(), rangeEnd.getTime()));
						}
						
						isCurrentRangeValid = points.get(key);
						firstRange = false;
					}
					
					
				} else {
					if (isCurrentRangeValid) {
						if (!points.get(key)) {
							rangeBegin = getRealCalendar(key, year, month, day);
							isCurrentRangeValid = false;
						}
					} else {
						if (points.get(key)) {
							rangeEnd = getRealCalendar(key, year, month, day);
							
							excludes.add(new ExcludedTimeRange(rangeBegin.getTime(), rangeEnd.getTime()));
							isCurrentRangeValid = true;
						}
					}
				}
			} // End of first if
		} // End of for loop
		
		if (!isCurrentRangeValid) {
			if (firstRange) {
				excludes.add(new ExcludedTimeRange(begin.getTime(), new Date(end.getTimeInMillis() + 1)));
			} else {
			    excludes.add(new ExcludedTimeRange(rangeBegin.getTime(), new Date(end.getTimeInMillis() + 1)));
			}
		}

	}
	
	private java.util.Calendar getRealCalendar(java.util.Calendar refCalendar, int year, int month, int day) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTimeInMillis(refCalendar.getTimeInMillis());
		calendar.set(java.util.Calendar.YEAR, year);
		calendar.set(java.util.Calendar.MONTH, month);
		calendar.set(java.util.Calendar.DAY_OF_MONTH, day);
		
		if (calendar.get(java.util.Calendar.MILLISECOND) == 99) {
			calendar.add(java.util.Calendar.MILLISECOND, 1);
		}
		
		return calendar;
	}
}
