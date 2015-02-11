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
//
// Copyright (c) 2005 Welocalize Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// Welocalize CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF Welocalize CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

package com.globalsight.everest.edit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.servlet.util.ServerProxy;

public class CommentHelper
{
    static public final String SEPARATOR = "_";

    static public String makeLogicalKey(long p_trgPageId,
        long p_tuId, long p_tuvId, String p_subId)
    {
        StringBuffer result = new StringBuffer();

        result.append(p_trgPageId).append(SEPARATOR);
        result.append(p_tuId).append(SEPARATOR);
        result.append(p_tuvId).append(SEPARATOR);
        result.append(p_subId);

        return result.toString();
    }

    static public String makeLogicalKey(long p_trgPageId,
        long p_tuId, long p_tuvId, long p_subId)
    {
        StringBuffer result = new StringBuffer();

        result.append(p_trgPageId).append(SEPARATOR);
        result.append(p_tuId).append(SEPARATOR);
        result.append(p_tuvId).append(SEPARATOR);
        result.append(p_subId);

        return result.toString();
    }

    /**
     * For Offline: returns a key consisting of the TU and SUB ID for
     * use in hash maps.
     */
    static public String getTuSubKey(String p_logicalKey)
    {
        String[] tmp = p_logicalKey.split(SEPARATOR);

        return tmp[1] + SEPARATOR + tmp[3];
    }

    /**
     * Fetch segment comments for target page in map. Every target segment has
     * only one "issue" with multiple histories.
     * 
     * @param p_trgPageId
     * @return Map<Long, IssueImpl>: targetTuvId:IssueImpl
     */
    public static Map<Long, IssueImpl> getIssuesMap(long p_trgPageId)
    {
        Map<Long, IssueImpl> result = new HashMap<Long, IssueImpl>();
        CommentManager commentManager = ServerProxy.getCommentManager();
        try
        {
            List<IssueImpl> issues = commentManager.getIssues(
                    Issue.TYPE_SEGMENT, p_trgPageId);
            if (issues != null && issues.size() > 0)
            {
                for (IssueImpl issue : issues)
                {
                    result.put(issue.getLevelObjectId(), issue);
                }
            }
        }
        catch (Exception ignore)
        {

        }
        return result;
    }
}
