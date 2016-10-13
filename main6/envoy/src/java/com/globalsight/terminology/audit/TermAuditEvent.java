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
package com.globalsight.terminology.audit;
import java.util.Date;

/**
 * Simple struct to hold fields related to a Term Audit Event
     * The fields are:
     *  eventDate
     *                   date auditable event happened
     *  username user who did the action
     *  termbase affected termbase
     *  item     affected object (concept, term, etc.)
     *  langs    languages. For example: "English,French,Spanish"
     *                   
     *  action   the action, for example: "create,delete,export" etc.
     *  details  details of the action
 
 */
public class TermAuditEvent
{
    public Date date = null;
    public String username = null;
    public String termbase = null;
    public String item = null;
    public String langs = null;
    public String action = null;
    public String details = null;

    public TermAuditEvent()
    {

    }

    public TermAuditEvent(Date p_date,
                          String p_username,
                          String p_termbase,
                          String p_item,
                          String p_langs,
                          String p_action,
                          String p_details)
    {
        date=p_date;
        username=p_username;
        termbase=p_termbase;
        item=p_item;
        langs=p_langs;
        action=p_action;
        details=p_details;
    }
}
