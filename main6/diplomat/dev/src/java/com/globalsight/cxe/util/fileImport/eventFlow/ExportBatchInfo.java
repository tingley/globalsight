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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "exportBatchId", "exportBatchPageCount",
        "exportBatchPageNum", "exportBatchDocPageCount",
        "exportBatchDocPageNum" })
@XmlRootElement(name = "batchInfo")
public class ExportBatchInfo implements Serializable
{
    private static final long serialVersionUID = 1421826945399697058L;
    @XmlElement(required = true)
    private String exportBatchId;
    @XmlElement(required = true)
    private int exportBatchPageCount;
    @XmlElement(required = true)
    private int exportBatchPageNum;
    @XmlElement(required = true)
    private int exportBatchDocPageCount;
    @XmlElement(required = true)
    private int exportBatchDocPageNum;

    /**
     * @return the exportBatchId
     */
    public String getExportBatchId()
    {
        return exportBatchId;
    }

    /**
     * @param exportBatchId
     *            the exportBatchId to set
     */
    public void setExportBatchId(String exportBatchId)
    {
        this.exportBatchId = exportBatchId;
    }

    /**
     * @return the exportBatchPageCount
     */
    public int getExportBatchPageCount()
    {
        return exportBatchPageCount;
    }

    /**
     * @param exportBatchPageCount
     *            the exportBatchPageCount to set
     */
    public void setExportBatchPageCount(int exportBatchPageCount)
    {
        this.exportBatchPageCount = exportBatchPageCount;
    }

    /**
     * @return the exportBatchPageNum
     */
    public int getExportBatchPageNum()
    {
        return exportBatchPageNum;
    }

    /**
     * @param exportBatchPageNum
     *            the exportBatchPageNum to set
     */
    public void setExportBatchPageNum(int exportBatchPageNum)
    {
        this.exportBatchPageNum = exportBatchPageNum;
    }

    /**
     * @return the exportBatchDocPageCount
     */
    public int getExportBatchDocPageCount()
    {
        return exportBatchDocPageCount;
    }

    /**
     * @param exportBatchDocPageCount
     *            the exportBatchDocPageCount to set
     */
    public void setExportBatchDocPageCount(int exportBatchDocPageCount)
    {
        this.exportBatchDocPageCount = exportBatchDocPageCount;
    }

    /**
     * @return the exportBatchDocPageNum
     */
    public int getExportBatchDocPageNum()
    {
        return exportBatchDocPageNum;
    }

    /**
     * @param exportBatchDocPageNum
     *            the exportBatchDocPageNum to set
     */
    public void setExportBatchDocPageNum(int exportBatchDocPageNum)
    {
        this.exportBatchDocPageNum = exportBatchDocPageNum;
    }

}
