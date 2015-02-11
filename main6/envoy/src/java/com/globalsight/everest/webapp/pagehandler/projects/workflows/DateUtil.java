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

package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.util.*;

/**
 * Class container for static methods to assist in dynamic population
 * of date combo boxes.
 */
public class DateUtil {

    private static final int BEGIN_YEAR_RANGE = 1990;
    private static final int END_YEAR_RANGE = 2010;

    private GregorianCalendar gregorianCalendar = new GregorianCalendar();

    private int month;
    private int year;
    private int day;

    public DateUtil() {
    }

    public int getDaysInMonth() {
	int retVal = 0;

	switch (month) {
	    case (Calendar.JANUARY):
	    	    retVal = 31;
	        break;
	    case (Calendar.FEBRUARY):
	        if (gregorianCalendar.isLeapYear(year)) {
		    retVal = 29;
		} else {
		    retVal = 28;
		}
	    	break;
	    case (Calendar.MARCH):
	    	retVal = 31;
	    	break;
	    case (Calendar.APRIL):
	    	retVal = 30;
	    	break;
	    case (Calendar.MAY):
	    	retVal = 31;
	    	break;
	    case (Calendar.JUNE):
	    	retVal = 30;
	    	break;
	    case (Calendar.JULY):
	    	retVal = 31;
	    	break;
	    case (Calendar.AUGUST):
	    	retVal = 31;
	    	break;
	    case (Calendar.SEPTEMBER):
	    	retVal = 30;
	    	break;
	    case (Calendar.OCTOBER):
	    	retVal = 31;
	    	break;
	    case (Calendar.NOVEMBER):
	    	retVal = 30;
	    	break;
	    case (Calendar.DECEMBER):
	    	retVal = 31;
	    	break;
	    default:
	    	retVal = 31;
	    	break;
	}

	return retVal;
    }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getBeginYearRange() { return BEGIN_YEAR_RANGE; }
    public int getEndYearRange() { return END_YEAR_RANGE; }
}


