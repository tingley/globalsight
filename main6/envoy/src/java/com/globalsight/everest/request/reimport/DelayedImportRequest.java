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
package com.globalsight.everest.request.reimport;

// globalsight
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.request.RequestImpl;

// java
import java.util.Calendar;

/**
 * This class represents a Request whose import has been delayed. It holds
 * information necessary to perform an import at a later time.
 */
public class DelayedImportRequest extends PersistentObject
{

	private static final long serialVersionUID = 1L;

	private RequestImpl m_request; // the request that is being delayed

	// the following attributes are all part of the request object - but are
	// not stored in the database as part of the request. They are stored as
	// part of the page once the import/parsing is completed.
	// In this case the import is being delayed so the page will not be created.
	// These attributes must be stored to the database somewhere in case the
	// system is shutdown before the re-import is stared.
	// In order for TOPLink to store them to the database they must be stored as
	// separate attributes within this object.

	private String m_gxml; // the GXML that is part of the request

	private String m_externalPageId; // the unique name for this page

	private String m_dataSourceType; // the type of data source the request

	// came from

	private String m_sourceEncoding; // the encoding of the page

	private Timestamp m_timeToStartImport = null; // the time to start the

	// import at

	private SourcePage m_previousPage = null;

	/**
	 * Default constructor for TOPLink
	 */
	public DelayedImportRequest()
	{

	}

	/**
	 * Constructor - takes in request and the previous page that is part of an
	 * active job ( the reason this is a re-import)
	 */
	public DelayedImportRequest(RequestImpl p_request, SourcePage p_previousPage)
	{
		m_request = p_request;
		m_gxml = p_request.getGxml();
		m_externalPageId = p_request.getExternalPageId();
		m_sourceEncoding = p_request.getSourceEncoding();
		m_dataSourceType = p_request.getDataSourceType();
		m_previousPage = p_previousPage;
		// set the delay time
		setTime();
	}

//	public long getId()
//	{
//		return m_request.getId();
//	}

	public Long getIdAsLong()
	{
		return new Long(m_request.getId());
	}

	public RequestImpl getRequest()
	{
		return m_request;
	}

	public SourcePage getPreviousPage()
	{
		return m_previousPage;
	}

	public String getGxml()
	{
		return m_gxml;
	}

	public String getExternalPageId()
	{
		return m_externalPageId;
	}

	public String getSourceEncoding()
	{
		return m_sourceEncoding;
	}

	public String getDataSourceType()
	{
		return m_dataSourceType;
	}

	/**
	 * Set the reimport time to the specified time.
	 */
	public void setTime(Timestamp p_time)
	{
		m_timeToStartImport = p_time;
	}

	/**
	 * Calculate the import time based on the current time.
	 */
	public void setTime()
	{
		// create a time as of now
		m_timeToStartImport = new Timestamp();
		// add the necessary time to the date and set back to the timestamp
		long seconds = ActivePageReimporter.getReimportStallTime() / 1000;
		m_timeToStartImport.add(Calendar.SECOND, new Long(seconds).intValue());
	}

	public Timestamp getTime()
	{
		return m_timeToStartImport;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("Delayed Request: ");
		sb.append(getId());
		sb.append("  timeToStartImport: ");
		sb.append(getTime().toString());
		sb.append("  externalPageId: ");
		sb.append(getExternalPageId());
		sb.append("  sourceEncoding: ");
		sb.append(getSourceEncoding());
		sb.append("  dataSourceTYpe: ");
		sb.append(getDataSourceType());
		sb.append("  gxml: ");
		sb.append(getGxml());
		sb.append("  request: ");
		sb.append(getRequest().toString());
		sb.append("  previousPage id: ");
		sb.append(getPreviousPage().getId());
		return sb.toString();
	}

	public void setGxml(String m_gxml)
	{
		this.m_gxml = m_gxml;
	}

	public void setExternalPageId(String pageId)
	{
		m_externalPageId = pageId;
	}

	public void setPreviousPage(SourcePage page)
	{
		m_previousPage = page;
	}

	public void setDataSourceType(String sourceType)
	{
		m_dataSourceType = sourceType;
	}

	public void setSourceEncoding(String encoding)
	{
		m_sourceEncoding = encoding;
	}

	public void setRequest(RequestImpl m_request)
	{
		this.m_request = m_request;
	}
}
