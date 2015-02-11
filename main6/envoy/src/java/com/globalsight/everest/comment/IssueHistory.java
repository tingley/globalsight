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
package com.globalsight.everest.comment;

import java.util.Date;


public interface IssueHistory
{

    /**
     * Return a unique id for this instance.
     * An IssueHistory doesn't have its own primary key - but is made up from
     * the addition of the issue's id and the dateReported.getTime()
     */
    public long getId();

    /**
     * Return the date that this comment was added - as a formatted string.
     */
    public String dateReported();

    /**
     * Return the date that this comment was added to the issue.
     */
    public Date dateReportedAsDate();

    /**
     * Return the id of the user that added this comment to the issue.
     */
    public String reportedBy();

    /**
     * Return the comment text that was added to the issue.
     */
    public String getComment();
}
