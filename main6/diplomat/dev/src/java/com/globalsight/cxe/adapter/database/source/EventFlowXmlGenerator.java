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
import com.globalsight.diplomat.util.database.RecordProfile;
import com.globalsight.diplomat.util.database.RecordProfileDbAccessor;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.util.CxeProxy;

/**
 * Converts the result set from an SQL query into XML format that can
 * be used by GlobalSight.
 */
public class EventFlowXmlGenerator
    extends XmlGenerator
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String EF_XML = "eventFlowXml";
    private static final String PRE_MERGE_TAG = "preMergeEvent";

    private static final String POST_MERGE_TAG = "postMergeEvent";

    private static final String BATCH_INFO = "batchInfo";
    private static final String LOC_PROF_ID = "l10nProfileId";
    private static final String PROCESSING_MODE = "processingMode";
    private static final String MANUAL = "manual";
    private static final String AUTOMATIC = "automatic";
    private static final String BATCH_ID = "batchId";
    private static final String JOB_NAME = "jobName";
    private static final String PG_COUNT = "pageCount";
    private static final String PG_NUMBER = "pageNumber";
    private static final String DOC_PG_COUNT = "docPageCount";
    private static final String DOC_PG_NUMBER = "docPageNumber";
    private static final String DISPLAY_NAME = "displayName";
    private static final String SOURCE = "source";
    private static final String DB_SOURCE_ADAPTER = "DatabaseSourceAdapter";
    private static final String DS_TYPE = "dataSourceType";
    private static final String DS_ID = "dataSourceId";
    private static final String DB_VALUE = "db";
    private static final String FORMAT_TYPE = "formatType";
    private static final String PRSXML = "prsxml";
    private static final String PREVIEWABLE = "pageIsCxePreviewable";
    private static final String TARGET = "target";
    private static final String DB_TARGET_ADAPTER = "DatabaseTargetAdapter";
    private static final String NAME = "name";
    private static final String LOCALE = "locale";
    private static final String CHARSET = "charset";
    private static final String DB_MODE_KEY = "databaseMode";
    private static final String DB_MODE_VALUE = "final";
    private static final String PREVIEW_URL_KEY = "previewUrl";
    private static final String PREVIEW_URL_VALUE = "false";
    private static final String DISPLAY_SUFFIX = ".dxp";
    private static final String IMPORT_REQUEST_TYPE="importRequestType";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private transient String m_tabString;
    private transient StringBuffer m_buffer;
    private transient int m_pageCount;
    private transient int m_page;
    private transient Task m_task;
    private transient RecordProfile m_recordProfile;
    private transient String m_batchId;

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Create an instance of the generator.
     */
    public EventFlowXmlGenerator()
    {
        super();
    }

    /**
     * Return the xml String corresponding to the current task list.
     * Select only those tasks which are part of the designated page.
     *
     * @param p_task a task containing the relevant localization profile id,
     * source language, and charset information
     * @param p_batchId a unique id for this batch
     * @param p_page the number of the page for which xml is required;
     * Must be between 1 and p_numPages
     * @param p_numPages the number of pages actually generated
     *
     * @return the current xml for the given page.
     */
    public String xml(Task p_task, long p_batchId,
        int p_page, int p_numPages)
        throws EventFlowXmlGenerationException
    {
        m_task = p_task;
        m_page = p_page;
        m_pageCount = p_numPages;
        long id = m_task.getRecordProfileId();

        try
        {
            m_recordProfile = RecordProfileDbAccessor.readRecordProfile(id);
            m_batchId = m_recordProfile.getName() + p_batchId;
        }
        catch (Exception e)
        {
            throw new EventFlowXmlGenerationException(
                "Unable to load record profile with id=" + id, e);
        }

        prepareXml();

        return getBuffer().toString();
    }

    //
    // IMPLEMENTATION OF ABSTRACT METHODS
    //

    /* Return the type of xml being generated*/
    protected String xmlType()
    {
        return EF_XML;
    }

    /* Return the text of the dtd for event flow xml */
    protected String[] dtdText()
    {
        return XmlUtil.eventFlowXmlDtd();
    }

    /* Begin the creation of xml in the text buffer. */
    private void prepareXml()
    {
        reset();
        addXmlHeader();
        addBody();
    }

    /* Write the Xml body to the buffer */
    private void addBody()
    {
        addPreamble();
        addEventFlowBody();
        addPostamble();
    }

    /* Write the event flow body to the buffer */
    private void addEventFlowBody()
    {
        CxeMessageType preMergeEvent = CxeMessageType.getCxeMessageType(
            CxeMessageType.PRSXML_LOCALIZED_EVENT);
        CxeMessageType postMergeEvent = CxeMessageType.getCxeMessageType(
            CxeMessageType.DATABASE_EXPORT_EVENT);
        addMergeElement(PRE_MERGE_TAG, preMergeEvent.getName());
        addMergeElement(POST_MERGE_TAG, postMergeEvent.getName());
        addBatchInfo();
        addSourceInfo();
        addTargetInfo();
    }

    /* Write the details of the given merge event. */
    private void addMergeElement(String p_element, String p_value)
    {
        addElement(p_element, p_value);
    }

    /* Write the contents of the given element tag. */
    private void addElement(String p_element, String p_value)
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

    /* Write the details of the batch information. */
    private void addBatchInfo()
    {
        incrementIndent();
        addIndent();
        openStartTag(BATCH_INFO);
        addSpace();
        addLocalizationProfileId();
        addSpace();
        addProcessingMode();
        closeTag();
        addBatchBody();
        addIndent();
        openEndTag(BATCH_INFO);
        closeTag();
        decrementIndent();
    }

    /* Add the localization profile id attribute. */
    private void addLocalizationProfileId()
    {
        addKeyValuePair(LOC_PROF_ID, "" + m_task.getLocalizationProfileId());
    }

    /* Add the processing mode attribute. */
    private void addProcessingMode()
    {
        addKeyValuePair(PROCESSING_MODE, m_task.isManualMode() ? MANUAL : AUTOMATIC);
    }

    /* Write out the display name for this record. */
    private String displayName()
    {
        return (m_batchId + "_" + m_page + "_of_" + m_pageCount + DISPLAY_SUFFIX);
    }

    /* Add all the elements related to the batch body.*/
    private void addBatchBody()
    {
        addElement(BATCH_ID, m_batchId);
        addElement(JOB_NAME, m_batchId);
        addElement(PG_COUNT, String.valueOf(m_pageCount));
        addElement(PG_NUMBER, String.valueOf(m_page));
        addElement(DOC_PG_COUNT, String.valueOf(m_pageCount));
        addElement(DOC_PG_NUMBER, String.valueOf(m_page));
        addElement(DISPLAY_NAME, displayName());
    }

    /* Write the information for the Source adapter. */
    private void addSourceInfo()
    {
        addSourceTargetInfo(SOURCE, DB_SOURCE_ADAPTER, true);
    }

    /* Write the information for the Target adapter. */
    private void addTargetInfo()
    {
        addSourceTargetInfo(TARGET, DB_TARGET_ADAPTER, false);
    }

    /* Write the information for the given adapter. */
    private void addSourceTargetInfo(String p_tag, String p_name, boolean p_isSource)
    {
        incrementIndent();
        addIndent();
        openStartTag(p_tag);
        addSpace();
        addKeyValuePair(NAME, p_name);
        if (p_isSource)
        {
            addSourceAttributes();
        }
        else
        {
            addTargetAttributes();
        }
        closeTag();
        addSourceTargetBody(p_isSource);
        addIndent();
        openEndTag(p_tag);
        closeTag();
        decrementIndent();
    }

    /* Write out attributes for the source element */
    private void addSourceAttributes()
    {
        addSpace();
        addKeyValuePair(DS_TYPE, DB_VALUE);
        addSpace();
        addKeyValuePair(DS_ID, Long.toString(m_recordProfile.getId()));
        addSpace();

        String previewable = "false";
        if (m_recordProfile.getPreviewUrlId() > 0)
        {
            previewable = "true";
        }
        addKeyValuePair(FORMAT_TYPE,PRSXML);
        addSpace();
        addKeyValuePair(PREVIEWABLE, previewable);
        addSpace();
        addKeyValuePair(IMPORT_REQUEST_TYPE,CxeProxy.IMPORT_TYPE_L10N);
    }

    /* Write out attributes for the target element */
    private void addTargetAttributes()
    {
        addSpace();
        addKeyValuePair(DB_MODE_KEY, DB_MODE_VALUE);
        addSpace();
        addKeyValuePair(PREVIEW_URL_KEY, PREVIEW_URL_VALUE);
    }

    /* Add element contents for the given adapter. */
    private void addSourceTargetBody(boolean p_isSource)
    {
        addElement(LOCALE, (p_isSource ? m_task.getSourceLanguage() : UNKNOWN));
        addElement(CHARSET, (p_isSource ? m_task.getCharset() : UNKNOWN));
    }

    /* Output the document definition tag and embedded DTD. */
    protected void addXmlHeader()
    {
        addString(XmlUtil.formattedEventFlowXmlDtd());
    }
}
