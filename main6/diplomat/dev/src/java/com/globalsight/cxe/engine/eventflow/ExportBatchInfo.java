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

public class ExportBatchInfo implements Serializable 
{
	private static final String EXPORT_BATCH_INFO = "exportBatchInfo";
	private static final String EXPORT_BATCH_ID = "exportBatchId";
	private static final String EXPORT_BATCH_PAGE_COUNT = "exportBatchPageCount";
	private static final String EXPORT_BATCH_PAGE_NUM = "exportBatchPageNum";
	private static final String EXPORT_BATCH_DOC_PAGE_COUNT = "exportBatchDocPageCount";
	private static final String EXPORT_BATCH_DOC_PAGE_NUM = "exportBatchDocPageNum";

	private String m_exportBatchId;
	private int m_exportBatchPageCount;
	private int m_exportBatchPageNum;
	private int m_exportBatchDocPageCount;
	private int m_exportBatchDocPageNum;
	
	protected ExportBatchInfo(Element p_elem) 
	{
		m_exportBatchId = XmlUtils.getDirectChildElementValue(p_elem, EXPORT_BATCH_ID);
		m_exportBatchPageCount = Integer.parseInt(XmlUtils.getDirectChildElementValue(
				p_elem, EXPORT_BATCH_PAGE_COUNT));
		m_exportBatchPageNum = Integer.parseInt(XmlUtils.getDirectChildElementValue(
				p_elem, EXPORT_BATCH_PAGE_NUM));
		m_exportBatchDocPageCount = Integer.parseInt(XmlUtils.getDirectChildElementValue(
				p_elem, EXPORT_BATCH_DOC_PAGE_COUNT));
		m_exportBatchDocPageNum = Integer.parseInt(XmlUtils.getDirectChildElementValue(
				p_elem, EXPORT_BATCH_DOC_PAGE_NUM));
	}
	
	public String serializeToXml() 
	{		
		StringBuffer sb = new StringBuffer(200);
		
		XmlUtils.appendElementStart(sb, EXPORT_BATCH_INFO);
		XmlUtils.appendLine(sb);
		XmlUtils.appendElement(sb, EXPORT_BATCH_ID, EditUtil.encodeXmlEntities(m_exportBatchId));
		XmlUtils.appendElement(sb, EXPORT_BATCH_PAGE_COUNT, 
				String.valueOf(m_exportBatchPageCount));
		XmlUtils.appendElement(sb, EXPORT_BATCH_PAGE_NUM, 
				String.valueOf(m_exportBatchPageNum));
		XmlUtils.appendElement(sb, EXPORT_BATCH_DOC_PAGE_COUNT, 
				String.valueOf(m_exportBatchDocPageCount));
		XmlUtils.appendElement(sb, EXPORT_BATCH_DOC_PAGE_NUM, 
				String.valueOf(m_exportBatchDocPageNum));
		
		XmlUtils.appendElementEnd(sb, EXPORT_BATCH_INFO);
		XmlUtils.appendLine(sb);
		return sb.toString();
	}

    public String getExportBatchId()
    {
        return m_exportBatchId;
    }
	
	
}
