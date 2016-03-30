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
package com.globalsight.connector.blaise.vo;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.cognitran.translation.client.workflow.TranslationInboxEntry;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;

public class TranslationInboxEntryVo
{
	private TranslationInboxEntry entry = null;

	// GlobalSight Job ID if job created
	private List<Long> jobIds = null;

	public TranslationInboxEntryVo(TranslationInboxEntry entry)
			throws Exception
	{
		if (entry == null)
		{
			throw new Exception("The TranslationInboxEntry object is null.");
		}
		this.entry = entry;
	}

	public TranslationInboxEntry getEntry()
	{
		return entry;
	}

	public void setEntry(TranslationInboxEntry entry)
	{
		this.entry = entry;
	}

	public long getId()
	{
		return this.entry.getId();
	}

	public long getRelatedObjectId()
	{
		return this.entry.getRelatedObjectId();
	}

	public Locale getSourceLocale()
	{
		return this.entry.getSourceLocale().toLocale();
	}

	public String getDisplaySourceLocale()
	{
        Locale locale = getSourceLocale();
        return getLocaleCode(locale) + " (" + locale.getDisplayLanguage() + "_"
                + locale.getDisplayCountry() + ")";
	}

	public Locale getTargetLocale()
	{
		return this.entry.getTargetLocale().toLocale();
	}

	public String getDisplayTargetLocale()
	{
	    Locale locale = getTargetLocale();
        return getLocaleCode(getTargetLocale()) + " (" + locale.getDisplayLanguage() + "_"
                + locale.getDisplayCountry() + ")";
	}

	private String getLocaleCode(Locale locale)
	{
        return BlaiseHelper.fixLocale(locale.getLanguage() + "_" + locale.getCountry());
	}

	public String getDescription()
	{
		return this.entry.getDescription();
	}

	public int getSourceRevision()
	{
		return this.entry.getSourceRevision();
	}

	public Date getWorkflowStartDate()
	{
		return this.entry.getWorkflowStartDate();
	}

	public Date getDueDate()
	{
		return this.entry.getDueDate();
	}

	public String getWorkflowId()
	{
		return this.entry.getWorkflowId();
	}

	public long getWorkflowObjectId()
	{
		return this.entry.getWorkflowObjectId();
	}

	public String getSourceType()
	{
		return this.entry.getSourceType();
	}

	public String getCompanyName()
	{
		return this.entry.getCompanyName();
	}

	public List<Long> getJobIds()
	{
		return this.jobIds;
	}

	public void setJobIds(List<Long> jobIds)
	{
		this.jobIds = jobIds;
	}

	// Utility method
	public String getJobIdsForDisplay()
	{
		return BlaiseManager.listToString(jobIds);
	}
}
