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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import org.quartz.Calendar;

import com.globalsight.util.StringUtil;

/**
 * Class <code>TimeExpressionConverter</code> is used to convert time expression 
 * from Flux format to Quartz format.
 * 
 * This class does not implemented to parse all valid time expression for Flux,
 * it just allows cron time expression and simple interval time expression with special character "+" and "-".
 * 
 * <pre>
 * For Flux cron time expression, the sequence of valid value is as following:
 *     Parameter name               Value                                                  Note
 *      Millisecond                  *                                        This parameter will be ignored in Quartz
 *        Second                    0-59
 *        Minute                    0-59
 *         Hour                     0-23
 *      Day of month              1-31 or ([1,2,3,4,$][MO, TU, WE, TH, FR, SA, SU])   If this parameter's value is not pure digit, the value of day of week must be "*".
 *        Month                   0-11 or jan-dec
 *      Day of week                1-7 or sun-sat
 *      Day of year                 1-366
 *     Week of month                  *
 *     Week of year                   *
 *        Year                     1970-3000
 * </pre>
 */
public class TimeExpressionConverter {
	
    private static final Logger s_logger = 
    	Logger.getLogger(TimeExpressionConverter.class);
	
	/**
	 * Default value of "All" both in Flux and Quartz.
	 */
	private static final String DEFAULT_ALL = "*";
	
	/**
	 * Special value of "All" for week day or day of month in Quartz
	 */
	private static final String SPECIAL_ALL = "?";
	
	/**
	 * Define the parameter sequence of cron time expression in FLux.
	 * The sequence is:
	 *  millisecond second minute hour dayOfMonth month dayOfWeek dayOfYear weekOfMonth weekOfYear year
	 */
	private static final List<String> fluxCron = new ArrayList<String>();
	
	/**
	 * Define the parameter sequence of cron time expression in Quartz.
	 * The sequence is:
	 *   second minute hour dayOfMonth month dayOfWeek year
	 */
	private static final List<String> quartzCron = new ArrayList<String>();
	
	/**
	 * Define the special name of weekdays in day of month expression.
	 */
	private static final Map<String, String> weekDayInDayOfMonth = new HashMap<String, String>();
	
	/**
	 * Names of time expression parameter.
	 */
	private static final String MILLISECOND = "millisecond";
	private static final String SECOND = "second";
	private static final String MINUTE = "minute";
	private static final String HOUR = "hour";
	private static final String DAYOFMONTH = "dayofmonth";
	private static final String MONTH = "month";
	private static final String DAYOFWEEK = "dayofweek";
	private static final String DAYOFYEAR = "dayofyear";
	private static final String WEEKOFMONTH = "weekofmonth";
	private static final String WEEKOFYEAR = "weekofyear";
	private static final String YEAR = "year";
	
	/**
	 * Names of week days in day of month parameter.
	 */
	private static final String SHORT_SUNDAY = "SU";
	private static final String SHORT_MONDAY = "MO";
	private static final String SHORT_TUESDAY = "TU";
	private static final String SHORT_WEDNESDAY = "WE";
	private static final String SHORT_THURSDAY = "TH";
	private static final String SHORT_FRIDAY = "FR";
	private static final String SHORT_SATURDAY = "SA";
	
	/**
	 * Names of week days in day of week parameter.
	 */
	private static final String SUNDAY = "SUN";
	private static final String MONDAY = "MON";
	private static final String TUESDAY = "TUE";
	private static final String WEDNESDAY = "WED";
	private static final String THURSDAY = "THU";
	private static final String FRIDAY = "FRI";
	private static final String SATURDAY = "SAT";
	
	/**
	 * Special char for setting the last week. 
	 */
	private static final char LAST_WEEK_IN_DAY = '$';
	private static final String LAST_WEEK_IN_MONTH = "L";
	
	/**
	 * The flag used to union the week number and week day.
	 */
	private static final String UNION_FLAG_OF_WEEK = "#";
	
	/**
	 * Special characters used in interval expression.
	 */
	private static final char INTERVAL_HOUR = 'H';
	private static final char INTERVAL_MINUTE = 'M';
	private static final char INTERVAL_SECOND = 'S';
	private static final char INTERVAL_WEEK = 'W';
	
	private static final String[] months = {
		"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
		"JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
	
	/**
	 * Original flux time expression.
	 */
	private String fluxExpression;

	/**
	 * Flag of whether this convert is a time interval.
	 */
	private boolean isInterval = false;
	
	/**
	 * Interval value of each time parameter.
	 */
	private int intervalSecond = 0;
	private int intervalMinute = 0;
	private int intervalHour = 0;
	private boolean isWeekly = false;
	
	/**
	 * Special calendar, used with the convert to implement the same function as Flux time expression. 
	 */
	private Calendar calendar = null;
	
	/**
	 * Contains all the values of each time parameter.
	 */
	private Map<String, String> timeGroup = null;
	
	static {
		fluxCron.add(MILLISECOND);
		fluxCron.add(SECOND);
		fluxCron.add(MINUTE);
		fluxCron.add(HOUR);
		fluxCron.add(DAYOFMONTH);
		fluxCron.add(MONTH);
		fluxCron.add(DAYOFWEEK);
		fluxCron.add(DAYOFYEAR);
		fluxCron.add(WEEKOFMONTH);
		fluxCron.add(WEEKOFYEAR);
		fluxCron.add(YEAR);
		
		quartzCron.add(SECOND);
		quartzCron.add(MINUTE);
		quartzCron.add(HOUR);
		quartzCron.add(DAYOFMONTH);
		quartzCron.add(MONTH);
		quartzCron.add(DAYOFWEEK);
		quartzCron.add(YEAR);
		
		weekDayInDayOfMonth.put(SHORT_SUNDAY, SUNDAY);
		weekDayInDayOfMonth.put(SHORT_MONDAY, MONDAY);
		weekDayInDayOfMonth.put(SHORT_TUESDAY, TUESDAY);
		weekDayInDayOfMonth.put(SHORT_WEDNESDAY, WEDNESDAY);
		weekDayInDayOfMonth.put(SHORT_THURSDAY, THURSDAY);
		weekDayInDayOfMonth.put(SHORT_FRIDAY, FRIDAY);
		weekDayInDayOfMonth.put(SHORT_SATURDAY, SATURDAY);
	}
	
	/**
	 * Create a instance of class <code>TimeExpressionConverter</code>.
	 * 
	 * The parameter is time expression for Flux.
	 * There are two kind of time expression.
	 * One is cron time expression whose format is :
	 * millisecond second minute hour dayOfMonth month dayOfWeek dayOfYear weekOfMonth weekOfYear year
	 * another is interval time expression which is start with "+" or "-", such as "+3600s".
	 * 
	 * @param fluxExpression
	 * @throws ParseException 
	 */
	public TimeExpressionConverter(String fluxExpression) throws ParseException {
		initialTimeGroup();
		this.fluxExpression = fluxExpression;
		
		s_logger.debug("Original flux time expression is " + fluxExpression);
		parseExpression(fluxExpression);
	}
	
	/**
	 * Get original Flux time expression.
	 * 
	 * @return
	 */
	public String getFluxExprssion() {
		return this.fluxExpression;
	}
	
	public boolean isInterval() {
		return isInterval;
	}
	
	public Calendar getCalendar() {
		return calendar;
	}
	
	/**
	 * Return cron time expression for Quartz.
	 * 
	 * @return
	 */
	public String getCronExpression() {
		if (!isInterval) {
			StringBuffer cron = new StringBuffer();
			for (String parameter : quartzCron) { 
				cron.append(" " + timeGroup.get(parameter));
			}
			
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Quartz cron expression is " + cron.toString().trim());
			}
			
			return cron.toString().trim();
		} else {
			return String.valueOf(calculateInterval());
		}
	}
	
	/**
	 * Get the millisecond of interval.
	 * 
	 * @return
	 */
	public long getIntervalMilli() {
		return calculateInterval();
	}
	
	/**
	 * Parse Flux time expression.
	 * 
	 * @param fluxExpression
	 * @throws ParseException 
	 */
	private void parseExpression(String fluxExpression) throws ParseException {
		// If time expression is null or blank, throw exception.
		if (StringUtil.isEmpty(fluxExpression)) {
			throw new IllegalArgumentException(
					"The Flux time expression is illegal.");
		}
		
		if (Character.isDigit(fluxExpression.charAt(0))) {
			// If the first letter is digit, parse this expression as cron time expression
			s_logger.debug("Determine flux time expression " + fluxExpression + " as cron time expression.");
			parseCronExpression(fluxExpression);
		} else {
			// Else parse this expression as interval time expression
			s_logger.debug("Determine flux time expression " + fluxExpression + " as interval time expression.");
			parseIntervalExpression(fluxExpression);
		}
	}
	
	/**
	 * Initialize timeGroup.
	 */
	private void initialTimeGroup() {
		timeGroup = new HashMap<String, String>();
		cleanTimeGroup();
	}
	
	/**
	 * Clear timeGroup and then set default values.
	 */
	private void cleanTimeGroup() {
		timeGroup.clear();
		for (String parameter : fluxCron) {
			timeGroup.put(parameter, DEFAULT_ALL);
		}
	}
	
	/**
	 * Parse Flux cron time expression.
	 * 
	 * @param fluxExpression
	 * @throws ParseException 
	 */
	private void parseCronExpression(String fluxExpression) throws ParseException {
		if (timeGroup == null) {
			initialTimeGroup();
		}
		
		isInterval = false;
		
		StringTokenizer stringTokenizer = new StringTokenizer(fluxExpression, " \t", false);
		for (Iterator<String> iterator = fluxCron.iterator(); 
				stringTokenizer.hasMoreTokens() && iterator.hasNext();) {
			parse(iterator.next(), stringTokenizer.nextToken().trim().toUpperCase());
		}
		
		specialDefaultValue();
		convertMonth();
		buildCalendar();
		
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Parse Flux expression " + fluxExpression + " to Quartz expression " + getCronExpression());
		}
	}
	
	/**
	 * Parse the value of special parameter type.
	 * 
	 * @param type
	 * @param value
	 * @throws ParseException 
	 */
	private void parse(String type, String value) throws ParseException {
		if (DAYOFMONTH.equals(type)) {
			if (!value.matches("\\d+") && !DEFAULT_ALL.equals(value)) {
				/*
				 * In this situation, the day of month must define the special weekday of specified week of month.
				 * Such as "3WE", means the third Wednesday.  
				 */
				s_logger.debug("Determine day of month expression :" + value + " as week of month. Set day of month to \"*\".");
				timeGroup.put(type, DEFAULT_ALL);
				
				String sub = value.substring(1);
		    	String weekday = weekDayInDayOfMonth.get(sub);
		    	if (weekday == null) {
		    		throw new ParseException("The day of month parameter: " + value + " in cron expression is an invalid.", 1);
		    	}
		    	
		    	String weekNumSuffix;
		    	if (LAST_WEEK_IN_DAY == value.charAt(0)) {
		    		weekNumSuffix = LAST_WEEK_IN_MONTH;
		    	} else {
		    		weekNumSuffix = UNION_FLAG_OF_WEEK + value.charAt(0);
		    	}
		    	
		    	timeGroup.put(DAYOFWEEK, weekday + weekNumSuffix);
		    	s_logger.debug("Determine day of week :" + weekday + weekNumSuffix);
		    	
		    	return;
			}
		}
		
		String oldvalue = timeGroup.get(type);
		if (oldvalue == null) {
			throw new ParseException(type + " is an invalid type of time parameter.", 0);
		}
		
		if (!oldvalue.equals(value) && !DEFAULT_ALL.equals(oldvalue) && !DEFAULT_ALL.equals(value)) {
			throw new ParseException("Parameter " + type + " is set multiple value of " + oldvalue + " and " + value, 0);
		}
		
		if (!DEFAULT_ALL.equals(value)) {
			timeGroup.put(type, value);
		}
	}
	
	/**
	 * Parse Flux interval time expression.
	 * 
	 * @param fluxExpression
	 * @throws ParseException 
	 */
	private void parseIntervalExpression(String fluxExpression) throws ParseException {
		if (timeGroup == null) {
			initialTimeGroup();
		}
		
		isInterval = true;
		
		StringTokenizer stringTokenizer = new StringTokenizer(fluxExpression, " \t", false);
		while (stringTokenizer.hasMoreTokens()) {
			parse(stringTokenizer.nextToken().trim().toUpperCase()); 
		}
	}
	
	/**
	 * Parse a part of interval time expression.
	 * 
	 * @param interval
	 * @throws ParseException 
	 */
	private void parse(String interval) throws ParseException {
		char flag = interval.charAt(0);
		if ('+' != flag && '-' != flag) {
			throwIntervalParseException(interval, 0);
		}
		
		char type = interval.charAt(interval.length() - 1);
		switch (type) {
		    case INTERVAL_HOUR :
		    	intervalHour = Integer.parseInt(interval.substring(1, interval.length() - 1));
		    	s_logger.debug("Parse " + interval + " to " + intervalHour + " hour.");
		    	break;
		    	
		    case INTERVAL_MINUTE :
		    	intervalMinute = Integer.parseInt(interval.substring(1, interval.length() - 1));
		    	s_logger.debug("Parse " + interval + " to " + intervalMinute + " minute.");
		    	break;
		    	
		    case INTERVAL_SECOND :
		    	intervalSecond = Integer.parseInt(interval.substring(1, interval.length() - 1));
		    	s_logger.debug("Parse " + interval + " to " + intervalSecond + " second.");
		    	break;
		    	
		    case INTERVAL_WEEK :
		    	isWeekly = true;
		    	s_logger.debug("Parse " + interval + " to per-week.");
		    	break;
		    	
		    default :
		    	throwIntervalParseException(interval, interval.length() - 1);
		        break;
		}
	}
	
	/**
	 * Throw out new ParseException.
	 * 
	 * @param intervalExpression
	 * @param offset
	 * @throws ParseException
	 */
	private void throwIntervalParseException(String intervalExpression,
			int offset) throws ParseException {
		throw new ParseException(intervalExpression
				+ " is invalid interval time expression.", offset);
	}
	
	/**
	 * Calculate the millisecond of interval.
	 * 
	 * @return
	 */
	private long calculateInterval() {
		long interval = 0;
		if (isWeekly) {
			interval = 7 * 24 * 3600 * 1000;
		} else {
			interval = intervalHour * 60 * 60 * 1000l;
			interval += intervalMinute * 60 * 1000l;
			interval += intervalSecond * 1000l;
		}
		
		return interval;
	}
	
	/**
	 * Support for specifying both a day-of-week AND a day-of-month parameter is not implemented by Quartz.
	 * So we need to change the default value of day of week or day of month,
	 * if the other one is not default value.
	 * @throws ParseException 
	 */
	private void specialDefaultValue() throws ParseException {
		String dayOfWeek = timeGroup.get(DAYOFWEEK);
		String dayOfMonth = timeGroup.get(DAYOFMONTH);
		
		if (!DEFAULT_ALL.equals(dayOfWeek)) {
			// If both day of week and day of month are specified, throw exception.
			if (!DEFAULT_ALL.equals(dayOfMonth)) {
				throw new ParseException(
						"Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.", 0);
			}
			
			timeGroup.put(DAYOFMONTH, SPECIAL_ALL);
		} else {
			timeGroup.put(DAYOFWEEK, SPECIAL_ALL);
		}
	}
	
	/**
	 * Build calendar for day of year and week of year if necessary.
	 * 
	 */
	private void buildCalendar() {
		// The two parameter cannot be specified both at the same time.
		if (!DEFAULT_ALL.equals(timeGroup.get(DAYOFYEAR))) {
			buildDayOfYearCalendar();
		} else if (!DEFAULT_ALL.equals(timeGroup.get(WEEKOFYEAR))) {
			buildWeekOfYearCalendar();
		}
	}
	
	/**
	 * Build day of year calendar.
	 */
	private void buildDayOfYearCalendar() {
		String day = (String) timeGroup.get(DAYOFYEAR);
		addCalendar(new DayOfYearCalendar(Integer.parseInt(day.substring(1))));
	}
	
	/**
	 * Build week of year calendar.
	 */
	private void buildWeekOfYearCalendar() {
		String week = (String) timeGroup.get(WEEKOFYEAR);
		addCalendar(new WeekOfYearCalendar(Integer.parseInt(week.substring(1))));
	}
	
	private void addCalendar(Calendar calendar) {
		if (this.calendar == null) {
			this.calendar = calendar;
		} else if (this.calendar instanceof MultiCalendar) {
			((MultiCalendar) this.calendar).addCalendar(calendar);
		} else {
			MultiCalendar multiCalendar = new MultiCalendar();
			
			multiCalendar.addCalendar(this.calendar);
			multiCalendar.addCalendar(calendar);
			
			this.calendar = multiCalendar;
		}
	}
	
	private void convertMonth() {
		String month = timeGroup.get(MONTH);
		if (DEFAULT_ALL.equals(month)) {
			return;
		}
		
		StringBuffer monthBuffer = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(month, ",");
		
		if (tokenizer.hasMoreTokens()) {
			String oneMonth = tokenizer.nextToken().trim();
			if (oneMonth.length() == 1 && Character.isDigit(oneMonth.charAt(0))) {
				monthBuffer.append(months[Integer.parseInt(oneMonth)]);
			} else {
				monthBuffer.append(oneMonth);
			}
		}
		
		while (tokenizer.hasMoreTokens()) {
			String oneMonth = tokenizer.nextToken().trim();
			if (oneMonth.length() == 1 && Character.isDigit(oneMonth.charAt(0))) {
				monthBuffer.append("," + months[Integer.parseInt(oneMonth)]);
			} else {
				monthBuffer.append("," + oneMonth);
			}
		}
		
		timeGroup.put(MONTH, monthBuffer.toString());
		
	}

}
