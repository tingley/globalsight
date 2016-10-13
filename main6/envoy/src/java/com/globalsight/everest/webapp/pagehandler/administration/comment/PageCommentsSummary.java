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

package com.globalsight.everest.webapp.pagehandler.administration.comment;

import java.io.Serializable;

import com.globalsight.everest.page.TargetPage;

/**
 * Needed to display Segment Comment Summary table in the UI.
 */
public class PageCommentsSummary implements Serializable
{
	private TargetPage targetPage;

	private int openCommentsCount = 2;
	
	private int closedCommentsCount = 0;

	private long jobId;

	public PageCommentsSummary(TargetPage targetPage)
	{
		this.targetPage = targetPage;
	}

	public TargetPage getTargetPage()
	{
		return targetPage;
	}

	public void setOpenCommentsCount(int openCommentsCount)
	{
		this.openCommentsCount = openCommentsCount;
	}

	public int getOpenCommentsCount()
	{
		return openCommentsCount;
	}

	public int getClosedCommentsCount()
    {
        return closedCommentsCount;
    }

    public void setClosedCommentsCount(int closedCommentsCount)
    {
        this.closedCommentsCount = closedCommentsCount;
    }

    public void setJobId(long jobId)
	{
		this.jobId = jobId;
	}

	public long getJobId()
	{
		return jobId;
	}
}
