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
package com.globalsight.cxe.engine.eventflow;

import java.io.Serializable;

import org.w3c.dom.Element;

import com.globalsight.cxe.engine.util.XmlUtils;
import com.globalsight.util.edit.EditUtil;

public class BatchInfo implements Serializable
{

    private static final long serialVersionUID = -1094363677884257162L;

    private static final String BATCH_INFO = "batchInfo";
    private static final String PROCESSING_MODE = "processingMode";
    private static final String L10N_PROFILE_ID = "l10nProfileId";
    private static final String PRIORITY = "priority";
    private static final String JOB_NAME = "jobName";
    private static final String JOB_ID = "jobId";
    private static final String BASE_HREF = "baseHref";
    private static final String MASTER_TRANSLATED = "masterLayerTranslated";
    private static final String INDD_HIDDEN_TRANSLATED = "inddHiddenLayerTranslated";
    private static final String ADOBE_XMP_TRANSLATED = "adobeXmpTranslated";
    private static final String DISPLAY_NAME = "displayName";
    private static final String DOC_PAGE_NUMBER = "docPageNumber";
    private static final String DOC_PAGE_COUNT = "docPageCount";
    private static final String PAGE_NUMBER = "pageNumber";
    private static final String PAGE_COUNT = "pageCount";
    private static final String UUID = "uuid";
    private static final String BATCH_ID = "batchId";

    public static class ProcessMode implements Serializable
    {

        private static final long serialVersionUID = -4534878064644536301L;

        private static final String MANUAL_STRING = "manual";
        private static final String AUTOMATIC_STRING = "automatic";
        public static final ProcessMode MANUAL = new ProcessMode(0,
                MANUAL_STRING);
        public static final ProcessMode AUTOMATIC = new ProcessMode(1,
                AUTOMATIC_STRING);

        private int m_value;
        private String m_name;

        private ProcessMode(int p_value, String p_name)
        {
            m_value = p_value;
            m_name = p_name;
        }

        public static ProcessMode getProcessMode(String p_processMode)
        {
            if (MANUAL_STRING.equals(p_processMode))
            {
                return MANUAL;
            }
            else if (AUTOMATIC_STRING.equals(p_processMode))
            {
                return AUTOMATIC;
            }
            else
            {
                return null;
            }
        }

        public String toString()
        {
            return m_name;
        }

        public int hashCode()
        {
            return m_value;
        }

        public boolean equals(Object o)
        {
            if (o == this)
                return true;
            if (o == null)
                return false;

            if (o instanceof ProcessMode)
            {
                ProcessMode other = (ProcessMode) o;
                return other.m_value == this.m_value;
            }
            return false;
        }
    }

    private String m_batchId;
    private int m_pageCount;
    private int m_pageNumber;
    private int m_docPageCount;
    private int m_docPageNumber;
    private String m_displayName;
    private String m_baseHref;
    private String m_priority;
    private String m_jobName;
    private String m_jobId;
    private String m_masterTranslated;
    private String m_inddHiddenTranslated;
    private String m_adobeXmpTranslated;
    private String uuid;

    private String m_l10nProfileId;
    private ProcessMode m_processMode = ProcessMode.AUTOMATIC;

    public BatchInfo()
    {
    }

    protected BatchInfo(Element p_elem)
    {
        m_batchId = XmlUtils.getDirectChildElementValue(p_elem, BATCH_ID);
        m_pageCount = Integer.parseInt(XmlUtils.getDirectChildElementValue(
                p_elem, PAGE_COUNT));
        m_pageNumber = Integer.parseInt(XmlUtils.getDirectChildElementValue(
                p_elem, PAGE_NUMBER));
        m_docPageCount = Integer.parseInt(XmlUtils.getDirectChildElementValue(
                p_elem, DOC_PAGE_COUNT));
        m_docPageNumber = Integer.parseInt(XmlUtils.getDirectChildElementValue(
                p_elem, DOC_PAGE_NUMBER));
        m_displayName = XmlUtils.getDirectChildElementValue(p_elem,
                DISPLAY_NAME);
        Element baseHrefElement = XmlUtils.getDirectChildElement(p_elem,
                BASE_HREF);
        if (baseHrefElement != null)
            m_baseHref = XmlUtils.getElementValue(baseHrefElement);
        Element priorityElement = XmlUtils.getDirectChildElement(p_elem,
                PRIORITY);
        if (priorityElement != null)
            m_priority = XmlUtils.getElementValue(priorityElement);
        Element jobNameElement = XmlUtils.getDirectChildElement(p_elem,
                JOB_NAME);
        if (jobNameElement != null)
            m_jobName = XmlUtils.getElementValue(jobNameElement);
        Element jobIdElement = XmlUtils.getDirectChildElement(p_elem, JOB_ID);
        if (jobIdElement != null && jobIdElement.hasChildNodes())
        {
        	m_jobId = XmlUtils.getElementValue(jobIdElement);
        }
        else
        {
            m_jobId = "";
        }
        Element masterTranslatedElement = XmlUtils.getDirectChildElement(
                p_elem, MASTER_TRANSLATED);
        if (masterTranslatedElement != null)
            m_masterTranslated = XmlUtils
                    .getElementValue(masterTranslatedElement);
        Element inddHiddenTranslatedElement = XmlUtils.getDirectChildElement(
                p_elem, INDD_HIDDEN_TRANSLATED);
        if (inddHiddenTranslatedElement != null)
            m_inddHiddenTranslated = XmlUtils
                    .getElementValue(inddHiddenTranslatedElement);
        Element xmpTranslatedElement = XmlUtils.getDirectChildElement(p_elem,
                ADOBE_XMP_TRANSLATED);
        if (xmpTranslatedElement != null)
            m_adobeXmpTranslated = XmlUtils
                    .getElementValue(xmpTranslatedElement);

        Element uuidElement = XmlUtils.getDirectChildElement(p_elem, UUID);
        if (uuidElement != null)
            uuid = XmlUtils.getElementValue(uuidElement);

        m_l10nProfileId = XmlUtils.getAttributeValue(p_elem, L10N_PROFILE_ID);
        m_processMode = ProcessMode.getProcessMode(XmlUtils.getAttributeValue(
                p_elem, PROCESSING_MODE));
    }

    public boolean isValid()
    {
        return (m_batchId != null) && (m_pageCount > 0) && (m_pageNumber > 0)
                && (m_docPageCount > 0) && (m_docPageNumber > 0)
                && (m_displayName != null) && (m_l10nProfileId != null)
                && (m_processMode != null);
    }

    public String getBatchId()
    {
        return m_batchId;
    }

    public void setBatchId(String p_batchId)
    {
        m_batchId = p_batchId;
    }

    public int getPageCount()
    {
        return m_pageCount;
    }

    public void setPageCount(int p_pageCount)
    {
        m_pageCount = p_pageCount;
    }

    public int getPageNumber()
    {
        return m_pageNumber;
    }

    public void setPageNumber(int p_pageNumber)
    {
        m_pageNumber = p_pageNumber;
    }

    public int getDocPageCount()
    {
        return m_docPageCount;
    }

    public void setDocPageCount(int p_docPageCount)
    {
        m_docPageCount = p_docPageCount;
    }

    public int getDocPageNumber()
    {
        return m_docPageNumber;
    }

    public void setDocPageNumber(int p_docPageNumber)
    {
        m_docPageNumber = p_docPageNumber;
    }

    public String getDisplayName()
    {
        return m_displayName;
    }

    public void setDisplayName(String p_displayName)
    {
        m_displayName = p_displayName;
    }

    public String getBaseHref()
    {
        return m_baseHref;
    }

    public void setBaseHref(String p_baseHref)
    {
        m_baseHref = p_baseHref;
    }

    public String getJobName()
    {
        return m_jobName;
    }

    public void setJobName(String p_jobName)
    {
        m_jobName = p_jobName;
    }

    public String getL10nProfileId()
    {
        return m_l10nProfileId;
    }

    public void setL10nProfileId(String p_l10nProfileId)
    {
        m_l10nProfileId = p_l10nProfileId;
    }

    public ProcessMode getProcessMode()
    {
        return m_processMode;
    }

    public void setProcessMode(ProcessMode p_processMode)
    {
        m_processMode = p_processMode;
    }

    public String getMasterTranslated()
    {
        return m_masterTranslated;
    }

    public void setMasterTranslated(String p_masterTranslated)
    {
        m_masterTranslated = p_masterTranslated;
    }

    public String getInddHiddenTranslated()
    {
        return m_inddHiddenTranslated;
    }

    public void setInddHiddenTranslated(String p_inddHiddenTranslated)
    {
        m_inddHiddenTranslated = p_inddHiddenTranslated;
    }

    public String getAdobeXmpTranslated()
    {
        return m_adobeXmpTranslated;
    }

    public void setAdobeXmpTranslated(String p_adobeXmpTranslated)
    {
        m_adobeXmpTranslated = p_adobeXmpTranslated;
    }

    public String serializeToXml()
    {
        StringBuffer sb = new StringBuffer(200);
        String[][] attributes =
        {
        { L10N_PROFILE_ID, m_l10nProfileId },
        { PROCESSING_MODE, m_processMode.toString() } };
        XmlUtils.appendElementStart(sb, BATCH_INFO, attributes);
        XmlUtils.appendLine(sb);
        XmlUtils.appendElement(sb, BATCH_ID,
                EditUtil.encodeXmlEntities(m_batchId));
        XmlUtils.appendElement(sb, PAGE_COUNT, String.valueOf(m_pageCount));
        XmlUtils.appendElement(sb, PAGE_NUMBER, String.valueOf(m_pageNumber));
        XmlUtils.appendElement(sb, DOC_PAGE_COUNT,
                String.valueOf(m_docPageCount));
        XmlUtils.appendElement(sb, DOC_PAGE_NUMBER,
                String.valueOf(m_docPageNumber));
        XmlUtils.appendElement(sb, DISPLAY_NAME,
                EditUtil.encodeXmlEntities(m_displayName));
        XmlUtils.appendElement(sb, BASE_HREF,
                EditUtil.encodeXmlEntities(m_baseHref));
        XmlUtils.appendElement(sb, MASTER_TRANSLATED, m_masterTranslated);
        XmlUtils.appendElement(sb, INDD_HIDDEN_TRANSLATED,
                m_inddHiddenTranslated);
        XmlUtils.appendElement(sb, ADOBE_XMP_TRANSLATED, m_adobeXmpTranslated);
        XmlUtils.appendElement(sb, PRIORITY,
                EditUtil.encodeXmlEntities(m_priority));
        XmlUtils.appendElement(sb, JOB_NAME,
                EditUtil.encodeXmlEntities(m_jobName));
        XmlUtils.appendElement(sb, JOB_ID, EditUtil.encodeXmlEntities(m_jobId));
        XmlUtils.appendElement(sb, UUID, EditUtil.encodeXmlEntities(uuid));

        XmlUtils.appendElementEnd(sb, BATCH_INFO);
        XmlUtils.appendLine(sb);
        return sb.toString();
    }

    public static void main(String[] args)
    {
        BatchInfo batchInfo = new BatchInfo();
        batchInfo.setBatchId("batchId00000011");
        batchInfo.setPageCount(2);
        batchInfo.setPageNumber(1);
        batchInfo.setDocPageCount(3);
        batchInfo.setDocPageNumber(2);
        batchInfo.setDisplayName("task/abcdef");
        batchInfo.setJobName("silver");
        batchInfo.setL10nProfileId("1001");

        System.out.println(batchInfo.serializeToXml());
    }

}
