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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.TaskCommentInfo;

/**
 * CommentQueryResultHandler provides functionality to convert a Collection of
 * ReportQueryResults into a Collection containing a TaskCommentInfo objects.
 */
public class CommentQueryResultHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(CommentQueryResultHandler.class.getName());

    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing TaskCommentInfo objects
     * 
     * @param p_collection
     *            the collection of ReportQueryResults that is created when the
     *            original query is executed
     * 
     * @return a collection containing TaskCommentInfo object
     */
    public static Collection handleResult(Collection p_collection)
    {
        Collection issues = new ArrayList();
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Object[] result = (Object[]) it.next();

            String taskName = (String) result[0];
            long commentId = ((Number) result[1]).longValue();
            Date createDate = (Date) result[2];
            String creator = (String) result[3];
            String comment = (String) result[4];
            String langCode = (String) result[5];
            String countryCode = (String) result[6];
            Locale loc = new Locale(langCode, countryCode);

            TaskCommentInfo tci = new TaskCommentInfo(commentId, createDate,
                    creator, comment, taskName, loc);
            issues.add(tci);
        }
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("handleResult returns " + issues.toString());
        }
        return issues;
    }
}
