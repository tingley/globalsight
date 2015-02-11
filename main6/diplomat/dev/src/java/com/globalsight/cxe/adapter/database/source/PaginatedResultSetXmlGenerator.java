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
package com.globalsight.cxe.adapter.database.source;

import com.globalsight.diplomat.util.XmlUtil;

import com.globalsight.cxe.adapter.database.source.ResultSetProxy;
import com.globalsight.cxe.adapter.database.source.ResultSetProxyDbAccessor;

import com.globalsight.diplomat.util.database.ColumnProfile;
import com.globalsight.diplomat.util.database.RecordProfile;
import com.globalsight.diplomat.util.database.RecordProfileDbAccessor;

import java.util.Date;
import java.util.Vector;
import java.util.HashMap;

/**
 * Converts the result set from an SQL query into XML format that can
 * be used by GlobalSight.
 */
public class PaginatedResultSetXmlGenerator
    extends XmlGenerator
{
    //
    // PRIVATE CONSTANTS
    //
    private static final int DEFAULT_RPP = 10;
    private static final int DEFAULT_PPB = 1;
    private static final String PRS_XML = "paginatedResultSetXml";
    private static final String VERSION = "version";
    private static final String PRS_ID = "id";
    private static final String LOCALE = "locale";
    private static final String RECORD_PROFILE_ID = "recordProfileId";
    private static final String SEQ_NUM = "sequenceNumber";
    private static final String RECORD = "record";
    private static final String ACQ_PARM = "acqSqlParm";
    private static final String COLUMN = "column";
    private static final String DEFAULT_LOCALE = "en_US";
    private static final String NAME = "name";
    private static final String TABLE_NAME = "tableName";
    private static final String DATA_TYPE = "dataType";
    private static final String MAX_LENGTH = "maxLength";
    private static final String RULE_ID = "ruleId";
    private static final String CONTENT_MODE = "contentMode";
    private static final String LABEL = "label";
    private static final String CONTENT = "content";
    private static final String EMPTY_STRING = "";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private transient HashMap m_profiles;
    private transient Vector m_taskVector;
    private transient int m_recsPerPage;
    private transient int m_pagesPerBatch;
    private transient int m_pageNumber;
    private transient int m_seqNum;
    private transient RecordProfile m_recProf;
    private transient ResultSetProxy m_proxy;

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Create an initialized instance of the generator.
     */
    public PaginatedResultSetXmlGenerator()
    {
        super();
        m_recsPerPage = DEFAULT_RPP;
        m_pagesPerBatch = DEFAULT_PPB;
        m_pageNumber = 0;
        m_taskVector = new Vector();
        m_profiles = new HashMap();
    }

    //
    // IMPLEMENTATION OF ABSTRACT METHODS
    //
    /* Return the type of xml being generated*/
    protected String xmlType()
    {
        return PRS_XML;
    }

    /* Return the text of the dtd for paginated result set xml */
    protected String[] dtdText()
    {
        return XmlUtil.paginatedResultSetXmlDtd();
    }

    //
    // SUPERCLASS OVERRIDES
    //
    /* Reset important variables before proceeding. */
    protected void reset()
    {
        super.reset();
        m_seqNum = 1;
    }

    //
    // PUBLIC ACCESSORS
    //
    /**
     * Return the task list being used by the generator.
     *
     * @return the current task list.
     */
    public Vector getTaskVector()
    {
        return m_taskVector;
    }

    /**
     * Set the value of the task list being used by the generator.
     *
     * @param p_taskList the new value.
     */
    public void setTaskVector(Vector p_taskVector)
    {
        m_taskVector = p_taskVector;
    }

    /**
     * Return the number of records per page.
     *
     * @return the recs per page.
     */
    public int getRecordsPerPage()
    {
        return m_recsPerPage;
    }

    /**
     * Set the number of records per page.
     *
     * @param p_int the new value.
     */
    public void setRecordsPerPage(int p_int)
    {
        m_recsPerPage = p_int < 1 ? DEFAULT_RPP : p_int;
    }

    /**
     * Return the number of pages per batch.
     *
     * @return the pages per batch.
     */
    public int getPagesPerBatch()
    {
        return m_pagesPerBatch;
    }

    /**
     * Set the number of pages per batch.
     *
     * @param p_int the new value.
     */
    public void setPagesPerBatch(int p_int)
    {
        m_pagesPerBatch = p_int < 1 ? DEFAULT_PPB : p_int;
    }

    /**
     * Return the locale for which this generator is producing xml.
     *
     * @return the current locale.
     */
    public String getLocale()
    {
        return (m_taskVector == null || m_taskVector.size() == 0) ?
            DEFAULT_LOCALE :
            taskAt(0).getSourceLanguage();
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Return the number of pages of xml that will be generated.  The result
     * depends on the size of the current task list and the current value of
     * the number of records per page.
     *
     * @return then number of pages to be generated.
     */
    public int pageCount()
    {
        return (int)Math.ceil((double)getTaskVector().size() /
            (double)getRecordsPerPage());
    }

    /**
     * Return the xml String corresponding to the current task list.
     * Select only those tasks which are part of the designated page.
     *
     * @param p_pageNumber the number of the page for which xml is required.
     * Must be between 1 and the pageCount().
     *
     * @return the current xml for the given page.
     */
    public String xml(int p_pageNumber)
        throws PaginatedResultSetXmlGenerationException
    {
        m_pageNumber =
            (p_pageNumber < 1 ? 0 : (p_pageNumber > pageCount() ? 0 : p_pageNumber)) - 1;
        prepareXml();
        return getBuffer().toString();
    }

    //
    // PRIVATE SUPPORT METHODS
    //

    /* Return the task at the given index. */
    private Task taskAt(int p_index)
    {
        return (Task)m_taskVector.elementAt(p_index);
    }

    /* Return the record profile with the given id. */
    private RecordProfile recordProfile(long p_id)
        throws PaginatedResultSetXmlGenerationException
    {
        Long id = new Long(p_id);
        RecordProfile rp = (RecordProfile)m_profiles.get(id);
        if (rp == null)
        {
            try
            {
                rp = RecordProfileDbAccessor.readRecordProfile(p_id);
                // The following line is commented out as a temporary fix for bug
                // #GSDEF00001925.  This ensures that the record profile must be
                // loaded from the database every time, guaranteeing that changes
                // are immediately visible.
                // m_profiles.put(id, rp);
            }
            catch (Exception e)
            {
                throw new PaginatedResultSetXmlGenerationException(
                    "Unable to load record profile with id=" + p_id, e);
            }
        }
        return rp;
    }

    /* Execute the record profile's acquisition sql with the given parameters, */
    /* and return a proxy representing the result set of the query. */
    private ResultSetProxy resultSetProxy(RecordProfile p_rp, String p_params)
        throws PaginatedResultSetXmlGenerationException
    {
        ResultSetProxy rsp = null;
        try
        {
            rsp = ResultSetProxyDbAccessor.readResultSetProxy(p_rp, p_params);
        }
        catch (Exception e)
        {
            throw new PaginatedResultSetXmlGenerationException(
                "Unable to obtain result set for " + p_rp, e);
        }
        return rsp;
    }

    /* Get the result set proxy for the given task. */
    private ResultSetProxy resultSetProxy(Task p_t)
        throws PaginatedResultSetXmlGenerationException
    {
        return resultSetProxy(recordProfile(p_t.getRecordProfileId()),
            p_t.getParameterString());
    }

    /* Begin the creation of xml in the text buffer. */
    private void prepareXml()
        throws PaginatedResultSetXmlGenerationException
    {
        reset();
        if (m_taskVector.size() > 0 && m_pageNumber > -1)
        {
            addXmlHeader();
            addBody();
        }
    }

    /* Write the Xml body to the buffer */
    private void addBody()
        throws PaginatedResultSetXmlGenerationException
    {
        addPreamble();
        addRecords();
        addPostamble();
    }

    /* OVERRIDE: Start the body with the root tag. */
    protected void addPreamble()
    {
        openStartTag(xmlType());
        addSpace();
        addKeyValuePair(VERSION, "1.0");
        addSpace();
        addKeyValuePair(PRS_ID, "PRS" + System.currentTimeMillis());
        addSpace();
        addKeyValuePair(LOCALE, getLocale());
        closeTag();
    }

    /* Write the details for each record in the task list. */
    private void addRecords()
        throws PaginatedResultSetXmlGenerationException
    {
        int start = m_pageNumber * m_recsPerPage;
        int end = (m_pageNumber + 1) * m_recsPerPage - 1;
        if (end >= m_taskVector.size())
        {
            end = m_taskVector.size() - 1;
        }

        for (int i = start ; i <= end ; i++)
        {
            addRecordDetails(i);
        }
    }

    /* Write the details for the given record into the buffer. */
    private void addRecordDetails(int p_recNum)
        throws PaginatedResultSetXmlGenerationException
    {
        Task t = taskAt(p_recNum);
        m_recProf = recordProfile(t.getRecordProfileId());
        m_proxy = resultSetProxy(m_recProf, t.getParameterString());
        if (m_proxy != null && m_proxy.size() > 0)
        {
            incrementIndent();
            addRecordHeader();
            addParameter(t.getParameterString());
            addColumnDetails();
            addRecordFooter();
            decrementIndent();
        }
    }

    /* Write the tag and value for the parameter string. */
    private void addParameter(String p_str)
    {
        if (p_str == null || p_str.length() > 0)
        {
            incrementIndent();
            addIndent();
            openStartTag(ACQ_PARM);
            closeTag(false);
            addString(p_str);
            openEndTag(ACQ_PARM);
            closeTag();
            decrementIndent();
        }
    }

    /* Write out the opening tag for the record. */
    private void addRecordHeader()
    {
        addIndent();
        openStartTag(RECORD);
        addSpace();
        addKeyValuePair(RECORD_PROFILE_ID, "" + m_recProf.getId());
        addSpace();
        addKeyValuePair(SEQ_NUM, "SN" + m_seqNum++);
        closeTag();
    }

    /* Write out the closing tag for the record. */
    private void addRecordFooter()
    {
        addIndent();
        openEndTag(RECORD);
        closeTag();
    }

    /* Write the details for each column in the record. */
    private void addColumnDetails()
    {
        Vector cols = m_recProf.getColumnProfiles();
        for (int i = 0 ; i < cols.size() ; i++)
        {
            ColumnProfile c = (ColumnProfile)cols.elementAt(i);
            addColumnDetail(c, i);
        }
    }

    /* Write the details for the given column, using the given column */
    /* profile as a template. */
    private void addColumnDetail(ColumnProfile p_cp, int p_index)
    {
        incrementIndent();
        addColumnHeader(p_cp, p_index);
        addColumnBody(p_cp, p_index);
        addColumnFooter();
        decrementIndent();
    }

    /* Write out the opening tag for the column. */
    private void addColumnHeader(ColumnProfile p_cp, int p_index)
    {
        String colName = m_proxy.keyAt(p_index);
        if (colName == null)
        {
            colName = UNKNOWN;
        }
        addIndent();
        openStartTag(COLUMN);
        addSpace();
        addKeyValuePair(NAME, colName);
        addSpace();
        addKeyValuePair(TABLE_NAME, p_cp.getTableName());
        addSpace();
        addKeyValuePair(DATA_TYPE, p_cp.getDataType());
        addSpace();
        addKeyValuePair(MAX_LENGTH, "" + m_proxy.widthAt(p_index));
        addSpace();
        addKeyValuePair(RULE_ID, "" + p_cp.getRuleId());
        addSpace();
        addKeyValuePair(CONTENT_MODE, p_cp.CONTENT_MODES[p_cp.getContentMode() - 1]);
        closeTag();
    }

    /* Write out the label & content tags for the column. */
    private void addColumnBody(ColumnProfile p_cp, int p_index)
    {
        String content = m_proxy.valueAt(p_index);
        addColumnElement(LABEL, p_cp.getLabel());
        addColumnElement(CONTENT, (content == null ? EMPTY_STRING : content));
    }

    /* Write out a single element for the column. */
    private void addColumnElement(String p_element, String p_value)
    {
        incrementIndent();
        addIndent();
        openStartTag(p_element);
        closeTag(false);
        addString(escapeString(p_value));
        openEndTag(p_element);
        closeTag();
        decrementIndent();
    }

    /* Write out the closing tag for the column. */
    private void addColumnFooter()
    {
        addIndent();
        openEndTag(COLUMN);
        closeTag();
    }

    /* Output the document definition tag and embedded DTD. */
    protected void addXmlHeader()
    {
        addString(XmlUtil.formattedPaginatedResultSetXmlDtd());
    }
}

