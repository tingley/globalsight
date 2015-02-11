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
package com.globalsight.everest.page.pageexport;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.page.PageException;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * This class represents an export request whose export has been delayed.
 * It holds information necessary to perform the export at a later time.
 */
public class DelayedExportRequest
    extends PersistentObject
{
    private static final String PIPE = "|";

    private String m_codeSet;
    private String m_exportLocation;
    private String m_localeSubDir;
    private int m_bomType;
    private int m_xlfSrcAsTrg;

    // boolean to know the content of m_pageIds
    private boolean m_isTargetPage = false;
    private String m_listOfPageIds;
    private long m_jobId;

    // the time to start the export
    private transient Timestamp m_timeToStartExport = null;

    // the user who initiated the export
    private String m_exportingUserId = null;

    /**
     *  Default constructor for TOPLink
     */
    public DelayedExportRequest()
    {}


    /**
     * Creates a DelayedExportRequest object using the given
     * ExportParameters and export delay time
     *
     * @param p_exportParams the export parameters
     * @param p_pageIds list of source or target page Ids
     * @param p_isTargetPage true is the page IDs are target pages
     * @param p_delayMinutes the amount of time to delay the export in
     * @param p_exportingUser the user who initiated the export
     * minutes
     */
    public DelayedExportRequest(ExportParameters p_exportParams,
        List p_pageIds, boolean p_isTargetPage, int p_delayMinutes,
        long p_jobId, User p_exportingUser)
    {
        m_codeSet = p_exportParams.getExportCodeset();
        m_exportLocation = p_exportParams.getExportLocation();
        m_localeSubDir = p_exportParams.getLocaleSubDir();
        m_bomType = p_exportParams.getBOMType();
        m_xlfSrcAsTrg = p_exportParams.getXlfSrcAsTrg();
        m_jobId = p_jobId;
        m_isTargetPage = p_isTargetPage;
        m_exportingUserId = p_exportingUser.getUserId();

        StringBuffer sb = new StringBuffer();

        for (Iterator it = p_pageIds.iterator(); it.hasNext(); )
        {
            Long id = (Long)it.next();

            sb.append(id.toString());
            sb.append(PIPE);
        }

        m_listOfPageIds = sb.toString();

        // set the delay time
        setTime(p_delayMinutes);
    }


    /**
     * Creates new export parameters and returns them.
     *
     * @return ExportParameters
     */
    public ExportParameters getExportParameters() throws PageException
    {
        ExportParameters ep = new ExportParameters(null, m_codeSet,
                m_exportLocation, m_localeSubDir, m_bomType,
                ExportConstants.EXPORT_FOR_UPDATE);
        ep.setXlfSrcAsTrg(this.m_xlfSrcAsTrg);
        return ep;
    }

    /**
     * Returns whether the pages are target pages
     *
     * @return boolean
     */
    public boolean getIsTargetPage()
    {
        return m_isTargetPage;
    }


    /**
     * Calculates the list of pageIds from
     * a string containing ids in the form:
     * 1|2|3 because that is how the list is persisted
     * in the DB.
     *
     * @return a List of page ids (Long)
     */
    public List getPageIds()
    {
        StringTokenizer st = new StringTokenizer(m_listOfPageIds,PIPE);
        ArrayList pageIds = new ArrayList();
        while (st.hasMoreTokens())
        {
            String tok = st.nextToken();
            pageIds.add(new Long(tok));
        }

        return pageIds;
    }


    /**
     * Returns the job id
     *
     * @return job id
     */
    public long getJobId()
    {
        return m_jobId;
    }

    /**
     * Set the export time to the specified time.
     */
    public void setTime(Timestamp p_time)
    {
        m_timeToStartExport = p_time;
    }

    /**
     * Calculate the export time based on the current time and the
     * number of minutes to delay export.
     */
    public void setTime(int p_minutes)
    {
        // create a time as of now
        m_timeToStartExport = new Timestamp();

        // add the necessary time to the date and set back to the timestamp
	int delay;
	if (p_minutes < 1)
	{
	    delay = 10;
	}
	else
	{
	    delay = p_minutes * 60;
	}
        
        m_timeToStartExport.add(Calendar.SECOND, delay);
    }


    /**
     * Gets the timestamp
     *
     * @return Timetamp
     */
    public Timestamp getTime()
    {
        return m_timeToStartExport;
    }

    /**
    * Returns the user responsible for clicking 'Export Source'
    */
    public String getExportingUserId()
    {
	return m_exportingUserId;
    }
}

