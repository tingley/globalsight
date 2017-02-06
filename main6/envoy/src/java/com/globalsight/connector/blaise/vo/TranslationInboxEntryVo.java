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

import com.cognitran.translation.client.workflow.TranslationInboxEntry;
import com.globalsight.connector.blaise.util.BlaiseConstants;
import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import jodd.util.StringBand;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TranslationInboxEntryVo
{
	static private final Logger logger = Logger.getLogger(TranslationInboxEntryVo.class);

	private TranslationInboxEntry entry = null;
	private List<String> usages = null;
	private int wordCount = 0;
	private boolean isUsageOfHDU = false;
	private boolean isUsageOfEDM = false;
	private boolean isOtherUsages = false;
	private boolean isNoNeedToTranslate = false;

	// GlobalSight Job ID if job created
	private List<Long> jobIds = null;

	// for junit test only
	public TranslationInboxEntryVo()
	{
	}

	public TranslationInboxEntryVo(TranslationInboxEntry entry) throws Exception
	{
		if (entry == null)
		{
			logger.error("The TranslationInboxEntry object is null.");
			throw new Exception("The TranslationInboxEntry object is null.");
		}

		if (entry.getSourceLocale() == null)
		{
			String msg = "The TranslationInboxEntry object has no source locale: " + entry.getId();
			logBadEntryInfo(entry, msg);
			throw new Exception(msg);
		}

		if (entry.getTargetLocale() == null)
		{
			String msg = "The TranslationInboxEntry object has no target locale: " + entry.getId();
			logBadEntryInfo(entry, msg);
			throw new Exception(msg);
		}

		this.entry = entry;
	}

	private void logBadEntryInfo(TranslationInboxEntry entry, String msg)
	{
		logger.info("------------------------------------------------------------");
		logger.warn(msg);
		logger.info("entry.getId(): " + entry.getId());
		logger.info("entry.getRelatedObjectId(): " + entry.getRelatedObjectId());
		logger.info("entry.getCompanyName(): " + entry.getCompanyName());
		logger.info("entry.getDescription(): " + entry.getDescription());
		logger.info("entry.getSourceLocale(): " + entry.getSourceLocale());
		logger.info("entry.getTargetLocale(): " + entry.getTargetLocale());
		logger.info("usages of entry: " + usages);
		logger.info("word count of entry: " + wordCount);
		logger.info("------------------------------------------------------------");
	}

	public List<String> getUsages()
	{
		return usages;
	}

	public String getUsages2UI()
	{
		StringBand usagesString = new StringBand();
		if (usages != null && usages.size() > 0)
		{
			for (String usage : usages)
			{
				usagesString.append(usage).append("<br>");
			}
		}
		String info = usagesString.toString();
		if (info.length() > 0)
			info.substring(0, info.length() - 4);
		return info;
	}

	public void setUsages(List<String> usages)
	{
		this.usages = usages;
		if (usages != null) {
			for (String usage : usages)
			{
				if (BlaiseConstants.USAGE_TYPE_HDU.equals(usage))
					isUsageOfHDU = true;
				else if (BlaiseConstants.USAGE_TYPE_EDM.equals(usage))
					isUsageOfEDM = true;
				else
					isOtherUsages = true;
			}
		}
	}

	public boolean isUsageOfHDU()
	{
		return isUsageOfHDU;
	}

	public boolean isUsageOfEDM()
	{
		return isUsageOfEDM;
	}

	public boolean isOtherUsages()
	{
		return isOtherUsages;
	}

	public int getWordCount()
	{
		return wordCount;
	}

	public void setWordCount(int wordCount)
	{
		this.wordCount = wordCount;
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

	public String getRelatedObjectClassName()
	{
		return this.entry.getRelatedObjectClassName();
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

	//
	// Utility methods
	//

	public String getJobIdsForDisplay()
	{
		return BlaiseManager.listToString(jobIds);
	}

	public String getJobIdLinks()
	{
		if (this.jobIds == null || this.jobIds.size() == 0)
			return "";

		StringBuffer links = new StringBuffer();
		for (int i = 0; i < jobIds.size(); i++)
		{
			long jobId = jobIds.get(i);
			links.append("<a class='standardHREF' ");
			links.append("target='_blank' ");
			links.append("href='/globalsight/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId=").append(jobId).append("'>");
			links.append(jobId);
			links.append("</a>");

			if (i < jobIds.size() - 1)
			{
				links.append(", ");
			}
		}
		return links.toString();
	}

	public String getType()
	{
		return BlaiseHelper.getTypeByRelatedObjectClassName(getRelatedObjectClassName());
	}

}
