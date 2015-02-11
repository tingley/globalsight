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

/**
 * ExportBatchInfo holds export batch info values.
 */
public class ExportBatchInfo
    implements java.io.Serializable
{
    static private final Integer ZERO = new Integer(0);
    static private final Integer ONE = new Integer(1);

    public String exportBatchId;

    // Number of pages in the export batch
    public Integer pageCount;
    // Number of this page starting at 0
    public Integer pageNum;

    // Number of pages of an office file in the export batch
    public Integer docPageCount;
    // Number of this page starting at 0
    public Integer docPageNum;


    public ExportBatchInfo()
    {
        exportBatchId = "N/A";
        pageCount = ONE;
        pageNum = ZERO;
        docPageCount = ONE;
        docPageNum = ZERO;
    }

    public ExportBatchInfo(String p_exportBatchId,
        Integer p_pageCount, Integer p_pageNum)
    {
        exportBatchId = p_exportBatchId;
        pageCount = p_pageCount;
        pageNum = p_pageNum;
        docPageCount = ONE;
        docPageNum = ZERO;
    }

    public ExportBatchInfo(String p_exportBatchId,
        Integer p_pageCount, Integer p_pageNum,
        Integer p_docPageCount, Integer p_docPageNum)
    {
        exportBatchId = p_exportBatchId;
        pageCount = p_pageCount;
        pageNum = p_pageNum;
        docPageCount = p_docPageCount;
        docPageNum = p_docPageNum;
    }
}

