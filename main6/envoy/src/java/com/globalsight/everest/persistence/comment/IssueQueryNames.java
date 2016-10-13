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
 
package com.globalsight.everest.persistence.comment;


/**
 * Specifies the names of all the named queries for Issue.
 */
public interface IssueQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return the issue specified by the given id.
     * <p>
     * Arguments: 1. Issue id
     */
    public final static String ISSUE_BY_ID = "getIssueById";

    /**
     * Get the issues that are associated with a certain type of object and
     * has a logical key that starts with the same prefix.
     */
    public final static String ISSUES_BY_TYPE_AND_KEY = "getIssuesByTypeAndKey";
}

