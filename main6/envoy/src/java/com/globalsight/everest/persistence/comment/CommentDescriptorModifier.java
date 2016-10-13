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
 * CommentDescriptorModifier extends DescriptorModifier by providing
 * amendment methods unique to the Comment descriptor.
 */
public class CommentDescriptorModifier   
{

    // public variables used for identifying the comments table and fields
    public static final String TABLE_NAME = "COMMENTS";
    public static final String OBJECT_TYPE_FIELD = "COMMENT_OBJECT_TYPE";
    public static final String OBJECT_ID_FIELD = "COMMENT_OBJECT_ID";

    // the object types that comments can be attached to.
    public static final String JOB_TYPE = "J";      // job
    public static final String TASK_TYPE = "T";     // task

    // package level
    static final String COMMENT_ID = "commentId";
    static final String DATE = "createDate";
    static final String CREATOR = "creator";
    static final String COMMENT = "comment";
    static final String TASK_NAME = "taskName";
    static final String LANG_CODE = "isoLangCode";
    static final String COUNTRY_CODE = "isoCountryCode";
    static final String JOB_ID_ARG = "jobId";
    static final String LOCALE_IDS_ARG = "localeIds";
    static final String ID_ARG = "id";
}
