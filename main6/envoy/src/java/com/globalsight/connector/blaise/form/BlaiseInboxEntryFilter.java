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
package com.globalsight.connector.blaise.form;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;

public class BlaiseInboxEntryFilter
{
    private String relatedObjectIdFilter = null;
	private String sourceLocaleFilter = null;
    private String targetLocaleFilter = null;
    private String typeFilter = null;
    private String descriptionFilter = null;
    private String jobIdFilter = null;

    // The parent Id is the Blaise ID of a Composite Publication.
    // Standalone Publications are not supported. 
    private String parentId = null;

    public List<TranslationInboxEntryVo> filter(List<TranslationInboxEntryVo> entries)
    {
        List<TranslationInboxEntryVo> result = new ArrayList<TranslationInboxEntryVo>();
        
        for (TranslationInboxEntryVo entry : entries)
        {
            if (!like(relatedObjectIdFilter, String.valueOf(entry.getRelatedObjectId())))
            {
                continue;
            }

            if (!like(sourceLocaleFilter, entry.getDisplaySourceLocale()))
            {
                continue;
            }

            if (!like(targetLocaleFilter, entry.getDisplayTargetLocale()))
            {
                continue;
            }

            if (!like(typeFilter, entry.getType()))
            {
                continue;
            }

            if (!like(descriptionFilter, entry.getDescription()))
            {
                continue;
            }

            if (!like(jobIdFilter, entry.getJobIdsForDisplay()))
            {
                continue;
            }

            result.add(entry);
        }

        return result;
    }

    private boolean like(String filterValue, String candidateValue)
    {
        if (filterValue == null)
            return true;

        filterValue = filterValue.trim();
        if (filterValue.length() == 0)
            return true;

        if (candidateValue == null)
            return false;

        filterValue = filterValue.toLowerCase();
        candidateValue = candidateValue.toLowerCase();

        return candidateValue.indexOf(filterValue) > -1;
    }

    public String getRelatedObjectIdFilter()
    {
		return relatedObjectIdFilter;
	}

	public void setRelatedObjectIdFilter(String relatedObjectIdFilter)
	{
		this.relatedObjectIdFilter = relatedObjectIdFilter;
	}

	public String getSourceLocaleFilter()
	{
		return sourceLocaleFilter;
	}

	public void setSourceLocaleFilter(String sourceLocaleFilter)
	{
		this.sourceLocaleFilter = sourceLocaleFilter;
	}

	public String getTargetLocaleFilter()
	{
		return targetLocaleFilter;
	}

	public void setTargetLocaleFilter(String targetLocaleFilter)
	{
		this.targetLocaleFilter = targetLocaleFilter;
	}

	public String getTypeFilter()
	{
	    return typeFilter;
	}

	public void setTypeFilter(String typeFilter)
	{
	    this.typeFilter = typeFilter;
	}

	public String getDescriptionFilter()
	{
		return descriptionFilter;
	}

	public void setDescriptionFilter(String descriptionFilter)
	{
		this.descriptionFilter = descriptionFilter;
	}

	public String getJobIdFilter()
	{
		return jobIdFilter;
	}

	public void setJobIdFilter(String jobIdFilter)
	{
		this.jobIdFilter = jobIdFilter;
	}

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }
}
