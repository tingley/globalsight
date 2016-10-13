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
package com.globalsight.cxe.util.fileImport.eventFlow;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "batchId", "fileProfileId", "pageCount",
        "pageNumber", "docPageCount", "docPageNumber", "displayName",
        "adobeXmpTranslated", "inddHiddenTranslated", "masterTranslated",
        "baseHref", "priority", "jobName", "jobId", "uuid" })
@XmlRootElement(name = "batchInfo")
public class BatchInfo implements Serializable
{

    private static final long serialVersionUID = 4984274277017393564L;
    @XmlAttribute(name = "l10nProfileId", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String l10NProfileId;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String processingMode;
    @XmlElement(required = true)
    protected String batchId;
    @XmlElement(required = true)
    protected String fileProfileId;
    @XmlElement(required = true)
    protected int pageCount;
    @XmlElement(required = true)
    protected int pageNumber;
    @XmlElement(required = true)
    protected int docPageCount;
    @XmlElement(required = true)
    protected int docPageNumber;
    @XmlElement(required = true)
    protected String displayName;
    @XmlElement(required = false)
    protected String adobeXmpTranslated;
    @XmlElement(required = false)
    protected String inddHiddenTranslated;
    @XmlElement(required = false)
    protected String masterTranslated;

    protected String baseHref;
    protected String jobName;
    protected String jobId;
    protected String uuid;
    protected String priority;

    /**
     * Gets the value of the l10NProfileId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getL10NProfileId()
    {
        return l10NProfileId;
    }

    /**
     * Sets the value of the l10NProfileId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setL10NProfileId(String value)
    {
        this.l10NProfileId = value;
    }

    /**
     * Gets the value of the processingMode property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getProcessingMode()
    {
        if (processingMode == null)
        {
            return "automatic";
        } else
        {
            return processingMode;
        }
    }

    /**
     * Sets the value of the processingMode property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setProcessingMode(String value)
    {
        this.processingMode = value;
    }

    /**
     * Gets the value of the batchId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getBatchId()
    {
        return batchId;
    }

    /**
     * Sets the value of the batchId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setBatchId(String value)
    {
        this.batchId = value;
    }

    /**
     * Gets the value of the fileProfileId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getFileProfileId()
    {
        return fileProfileId;
    }

    /**
     * Sets the value of the fileProfileId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setFileProfileId(String value)
    {
        this.fileProfileId = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDisplayName(String value)
    {
        this.displayName = value;
    }

    /**
     * Gets the value of the baseHref property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getBaseHref()
    {
        return baseHref;
    }

    /**
     * Sets the value of the baseHref property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setBaseHref(String value)
    {
        this.baseHref = value;
    }

    /**
     * Gets the value of the jobName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getJobName()
    {
        return jobName;
    }

    /**
     * Sets the value of the jobName property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setJobName(String value)
    {
        this.jobName = value;
    }

    /**
     * Gets the value of the jobId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getJobId()
    {
        return jobId;
    }

    /**
     * Sets the value of the jobId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setJobId(String value)
    {
        this.jobId = value;
    }

    /**
     * Gets the value of the uuid property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getUuid()
    {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setUuid(String value)
    {
        this.uuid = value;
    }

    /**
     * @return the priority
     */
    public String getPriority()
    {
        return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    /**
     * @return the pageCount
     */
    public int getPageCount()
    {
        return pageCount;
    }

    /**
     * @param pageCount
     *            the pageCount to set
     */
    public void setPageCount(int pageCount)
    {
        this.pageCount = pageCount;
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber()
    {
        return pageNumber;
    }

    /**
     * @param pageNumber
     *            the pageNumber to set
     */
    public void setPageNumber(int pageNumber)
    {
        this.pageNumber = pageNumber;
    }

    /**
     * @return the docPageCount
     */
    public int getDocPageCount()
    {
        return docPageCount;
    }

    /**
     * @param docPageCount
     *            the docPageCount to set
     */
    public void setDocPageCount(int docPageCount)
    {
        this.docPageCount = docPageCount;
    }

    /**
     * @return the docPageNumber
     */
    public int getDocPageNumber()
    {
        return docPageNumber;
    }

    /**
     * @param docPageNumber
     *            the docPageNumber to set
     */
    public void setDocPageNumber(int docPageNumber)
    {
        this.docPageNumber = docPageNumber;
    }

    /**
     * @return the adobeXmpTranslated
     */
    public String getAdobeXmpTranslated()
    {
        return adobeXmpTranslated;
    }

    /**
     * @param adobeXmpTranslated
     *            the adobeXmpTranslated to set
     */
    public void setAdobeXmpTranslated(String adobeXmpTranslated)
    {
        this.adobeXmpTranslated = adobeXmpTranslated;
    }

    /**
     * @return the inddHiddenTranslated
     */
    public String getInddHiddenTranslated()
    {
        return inddHiddenTranslated;
    }

    /**
     * @param inddHiddenTranslated
     *            the inddHiddenTranslated to set
     */
    public void setInddHiddenTranslated(String inddHiddenTranslated)
    {
        this.inddHiddenTranslated = inddHiddenTranslated;
    }

    /**
     * @return the masterTranslated
     */
    public String getMasterTranslated()
    {
        return masterTranslated;
    }

    /**
     * @param masterTranslated
     *            the masterTranslated to set
     */
    public void setMasterTranslated(String masterTranslated)
    {
        this.masterTranslated = masterTranslated;
    }

}
