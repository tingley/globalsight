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
package com.globalsight.everest.edit.offline.rtf;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.UploadIssue;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

public class IssueLoader
{
    static private final Logger c_logger = Logger.getLogger(IssueLoader.class);

    // Comment regexp for MyTitle
    static private final REProgram m_commentTitle = createProgram("\\{MyTitle=([^\\}]*)\\}");
    // Comment regexp for Status
    static private final REProgram m_commentStatus = createProgram("\\{Status=([^\\}]*)\\}");
    // Comment regexp for Category
    static private final REProgram m_commentCategory = createProgram("\\{Category=([^\\}]*)\\}");
    // Comment regexp for Priority
    static private final REProgram m_commentPriority = createProgram("\\{Priority=([^\\}]*)\\}");
    // Comment regexp for MyReply
    static private final REProgram m_commentReply = createProgram("\\{MyReply=([^\\}]*)\\}");

    static private REProgram createProgram(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException ex)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(ex.getMessage());
        }

        return pattern;
    }

    /**
     * Parses a raw annotation text into title, comment, status and priority and
     * category and populates the UploadPageData object.
     *
     * @param p_segId
     *            normalized segment ID (TUID_SUBID)
     * @param p_segName
     *            display string for the segment ID, either TUID_SUBID (RTF para
     *            view), or TUID:[XX]:SUBID (RTF list view)
     */
    static/**/void handleUploadIssue(OfflinePageData p_opd, String p_segId,
            String p_segName, String p_text)
    {
        String title = null;
        String status = null;
        String category = null;
        String priority = null;
        String reply = null;
        RE matcher;

        matcher = new RE(m_commentTitle);
        if (matcher.match(p_text))
        {
            title = matcher.getParen(1).trim();
        }

        matcher = new RE(m_commentStatus);
        if (matcher.match(p_text))
        {
            status = matcher.getParen(1).trim();
        }

        matcher = new RE(m_commentCategory);
        if (matcher.match(p_text))
        {
            category = matcher.getParen(1).trim();
        }

        matcher = new RE(m_commentPriority);
        if (matcher.match(p_text))
        {
            priority = matcher.getParen(1).trim();
        }

        matcher = new RE(m_commentReply);
        if (matcher.match(p_text))
        {
            reply = matcher.getParen(1).trim();
        }

        if (c_logger.isDebugEnabled())
        {
            System.out.println("Segment Comment " + priority + " " + status
                    + category + " '" + title + "' = '" + reply + "'.");
        }

        // Must have at least a valid reply
        if (reply == null || reply.length() == 0)
        {
            return;
        }

        // Default the title if empty
        if (title == null || title.length() == 0)
        {
            title = "(empty title)";
        }

        if (Issue.PRI_URGENT.equalsIgnoreCase(priority))
        {
            priority = Issue.PRI_URGENT;
        }
        else if (Issue.PRI_HIGH.equalsIgnoreCase(priority))
        {
            priority = Issue.PRI_HIGH;
        }
        else if (Issue.PRI_MEDIUM.equalsIgnoreCase(priority))
        {
            priority = Issue.PRI_MEDIUM;
        }
        else if (Issue.PRI_LOW.equalsIgnoreCase(priority))
        {
            priority = Issue.PRI_LOW;
        }

        if (!Issue.PRI_URGENT.equals(priority)
                && !Issue.PRI_HIGH.equals(priority)
                && !Issue.PRI_MEDIUM.equals(priority)
                && !Issue.PRI_LOW.equals(priority))
        {
            priority = Issue.PRI_MEDIUM;
        }

        if (Issue.STATUS_QUERY.equalsIgnoreCase(status))
        {
            status = Issue.STATUS_QUERY;
        }
        else if (Issue.STATUS_OPEN.equalsIgnoreCase(status))
        {
            status = Issue.STATUS_OPEN;
        }
        else if (Issue.STATUS_CLOSED.equalsIgnoreCase(status))
        {
            status = Issue.STATUS_CLOSED;
        }
        else if (Issue.STATUS_REJECTED.equalsIgnoreCase(status))
        {
            status = Issue.STATUS_REJECTED;
        }

        if (!Issue.STATUS_QUERY.equals(status)
                && !Issue.STATUS_OPEN.equals(status)
                && !Issue.STATUS_CLOSED.equals(status)
                && !Issue.STATUS_REJECTED.equals(status))
        {
            // Accept both "closed" and "close".
            if ("close".equalsIgnoreCase(status))
            {
                status = Issue.STATUS_CLOSED;
            }
            else
            {
                status = Issue.STATUS_OPEN;
            }
        }
        // if (Issue.CATEGORY_TYPE01.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE01;
        // }
        // else if (Issue.CATEGORY_TYPE02.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE02;
        // }
        // else if (Issue.CATEGORY_TYPE03.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE03;
        // }
        // else if (Issue.CATEGORY_TYPE04.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE04;
        // }
        // else if (Issue.CATEGORY_TYPE05.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE05;
        // }
        // else if (Issue.CATEGORY_TYPE06.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE06;
        // }
        // else if (Issue.CATEGORY_TYPE07.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE07;
        // }
        // else if (Issue.CATEGORY_TYPE08.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE08;
        // }
        // else if (Issue.CATEGORY_TYPE09.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE09;
        // }
        // else if (Issue.CATEGORY_TYPE10.equalsIgnoreCase(category))
        // {
        // category = Issue.CATEGORY_TYPE10;
        // }
        // else
        // {
        // category = Issue.CATEGORY_TYPE01;
        // }

        // TODO:
        String[] tmp = p_segId.split("[_:]");
        String tuvId = tmp[0];
        String subId = tmp.length == 2 ? tmp[1] : "0";

        UploadIssue issue = new UploadIssue(p_segName, Long.parseLong(tuvId),
                Long.parseLong(subId), title, status, category, priority, reply);

        if (c_logger.isDebugEnabled())
        {
            System.out.println("Fixed Comment=" + issue + ".");
        }

        p_opd.addUploadedIssue(issue);
    }
}
