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

package com.globalsight.terminology.scheduler;

/**
 * Interface for a cron job specification.
 *
 * Currently implemented by
 * com.globalsight.terminology.scheduler.TermbaseReindexExpression.
 */
public interface CronExpression
{
    /**
     * The type of task this cron expression is executing,
     * e.g. "reindex a termbase.
     */
    public String getType();

    /**
     * The object identifier this task is operating on, e.g. reindex
     * termbase 1002.
     */
    public Long getObjectId();

    public String getMinutes();
    public void setMinutes(String p_arg);
    public String getHours();
    public void setHours(String p_arg);
    public String getDaysOfMonth();
    public void setDaysOfMonth(String p_arg);
    public String getMonths();
    public void setMonths(String p_arg);
    public String getDaysOfWeek();
    public void setDaysOfWeek(String p_arg);
    public String getDayOfYear();
    public void setDayOfYear(String p_arg);
    public String getWeekOfMonth();
    public void setWeekOfMonth(String p_arg);
    public String getWeekOfYear();
    public void setWeekOfYear(String p_arg);
    public String getYear();
    public void setYear(String p_arg);

    public void buildCronExpression();
    public void setCronExpression(String p_arg);
    public String getCronExpression();
}
