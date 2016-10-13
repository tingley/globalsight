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
package com.globalsight.everest.webapp.applet.common;

import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;

/**
 * AppletDate is used for printing dates 
 * and enabling sorting of them.
 *
 * Can't use the TimeStamp object because you get this exception:
 * java.lang.RuntimePermission accessClassInPackage.sun.util.calendar
 */
public class AppletDate implements Comparable, Serializable
{

    public Date date;

    /**
     * AppletDate constructer 
     */
    public AppletDate(Date date)
    {
        this.date = date;
    }

    /*
     * compareTo: negative if a<b, zero if a==b, positive if a>b.
     */
    public int compareTo(Object anotherObject) {
        AppletDate another = (AppletDate)anotherObject;
        return date.compareTo(another.date);
    }

    public String toString()
    {
        DateFormat df = DateFormat.getDateTimeInstance(
                                DateFormat.SHORT, DateFormat.FULL,
                                GlobalEnvoy.getLocale());
        return df.format(date);
    }

}
