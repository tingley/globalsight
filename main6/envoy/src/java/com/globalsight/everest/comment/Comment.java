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

import com.globalsight.everest.foundation.WorkObject;

import java.util.Date;

public interface Comment
{
    // work object that the comment can be attached to
    public static int JOB= 1;
    public static int WORKFLOW= 2;
    public static int TASK= 3;
    
    /**
     * Get the id of this task comment.
     * @return The task's comment id.
     */
    long getId();

    /**
     * Get the date that the comment was created.
     * @return The creation date.
     */
    String getCreatedDate();

    /**
     * Get the date that the comment was created.
     * @return The creation date.
     */
    Date getCreatedDateAsDate();

    /**
     * Get the id (user name) of the creator of this comment.
     * @return The comment creator's id.
     */
    String getCreatorId();

    /**
     * Get the comment.
     * @return The comment.
     */
    String getComment();

    /**
     * Set the comment text to be the specified value.
     * @param p_comment The comment to be set.
     */
    void setComment(String p_comment);

    /**
     * Returns the work object - Job, Task, Workflow - that the comment
     * is associated with.
     */
    WorkObject getWorkObject();
}
