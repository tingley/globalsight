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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.engine.util.XmlUtils;
import com.globalsight.diplomat.util.XmlUtil;

public class EventFlow implements Serializable {

    private static final String EVENT_FLOW_XML = "eventFlowXml";

    private static final String CXE_REQUEST_TYPE = "cxeRequestType";

    private static final String CAP_MESSAGE_ID = "capMessageId";

    private static final String CATEGORY = "category";

    private static final String DA = "da";

    private static final String TARGET = "target";

    private static final String SOURCE = "source";

    private static final String BATCH_INFO = "batchInfo";

    private static final String POST_MERGE_EVENT = "postMergeEvent";

    private static final String PRE_MERGE_EVENT = "preMergeEvent";
    
    private static final String EXPORT_BATCH_INFO = "exportBatchInfo";

    private String m_preMergeEvent;

    private String m_postMergeEvent;

    private BatchInfo m_batchInfo;

    private Source m_source;

    private Target m_target;

    private List m_das = new ArrayList();

    private List m_categories = new ArrayList();

    private String m_capMessageId;
    
    private ExportBatchInfo m_exportBatchInfo;

    private String m_cxeRequestType;
    /**
     * build an empty eventflow
     */
    public EventFlow() {
    }
    

    /**
     * @deprecated
     */
    public EventFlow(String p_xml) {
        init(XmlUtils.findRootElement(p_xml));
    }

    public EventFlow(Element p_elem) {
        init(p_elem);
    }

    private void init(Element p_elem) {
        m_preMergeEvent = XmlUtils.getDirectChildElementValue(p_elem, PRE_MERGE_EVENT);
        m_postMergeEvent = XmlUtils.getDirectChildElementValue(p_elem, POST_MERGE_EVENT);
        m_batchInfo = new BatchInfo(XmlUtils.getDirectChildElement(p_elem, BATCH_INFO));
        m_source = new Source(XmlUtils.getDirectChildElement(p_elem, SOURCE));

        Element targetElement = XmlUtils.getDirectChildElement(p_elem, TARGET);
        if (targetElement != null)
            m_target = new Target(targetElement);

        List daElements = XmlUtils.getChildElements(p_elem, DA);
        for (Iterator itor = daElements.iterator(); itor.hasNext();) {
            Element daElement = (Element) itor.next();
            m_das.add(new DiplomatAttribute(daElement));
        }
        List categoryElements = XmlUtils.getChildElements(p_elem, CATEGORY);
        for (Iterator itor = categoryElements.iterator(); itor.hasNext();) {
            Element categoryElement = (Element) itor.next();
            m_categories.add(new Category(categoryElement));
        }

        Element capMessageIdElement = XmlUtils.getDirectChildElement(p_elem, CAP_MESSAGE_ID);
        if (capMessageIdElement != null)
            m_capMessageId = XmlUtils.getElementValue(capMessageIdElement);
        
        Element exportBatchInfoElement = XmlUtils.getDirectChildElement(p_elem, EXPORT_BATCH_INFO);
        if(exportBatchInfoElement != null)
        	m_exportBatchInfo = new ExportBatchInfo(exportBatchInfoElement);
        
        Element cxeRequestTypeElement = XmlUtils.getDirectChildElement(p_elem, CXE_REQUEST_TYPE);
        if (cxeRequestTypeElement != null)
            m_cxeRequestType = XmlUtils.getElementValue(cxeRequestTypeElement);
    }

    public boolean isValid() {
        return (m_preMergeEvent != null) && (m_postMergeEvent != null)
                && m_batchInfo.isValid() && m_source.isValid()
                && (m_batchInfo != null) && (m_source != null);
    }

    public String getCapMessageId() {
        return m_capMessageId;
    }

    public void setCapMessageId(String capMessageId) {
        this.m_capMessageId = capMessageId;
    }

    public BatchInfo getBatchInfo() {
        return m_batchInfo;
    }

    public void setBatchInfo(BatchInfo info) {
        m_batchInfo = info;
    }

    public String getPostMergeEvent() {
        return m_postMergeEvent;
    }

    public void setPostMergeEvent(String mergeEvent) {
        m_postMergeEvent = mergeEvent;
    }

    public String getPreMergeEvent() {
        return m_preMergeEvent;
    }

    public void setPreMergeEvent(String mergeEvent) {
        m_preMergeEvent = mergeEvent;
    }

    public Source getSource() {
        return m_source;
    }

    public void setSource(Source m_source) {
        this.m_source = m_source;
    }

    public Target getTarget() {
        return m_target;
    }

    public void setTarget(Target m_target) {
        this.m_target = m_target;
    }

    public String getCxeRequestType() {
        return m_cxeRequestType;
    }

    public void setType(String cxeRequestType) {
        this.m_cxeRequestType = cxeRequestType;
    }

    public void addDiplomatAttribute(DiplomatAttribute da) {
        m_das.add(da);
    }

    public void removeDiplomatAttribute(DiplomatAttribute da) {
        m_das.remove(da);
    }

    public void clearDiplomatAttribute() {
        m_das.clear();
    }
    
    public List getDiplomatAttributes(){
        return m_das;
    }
    public DiplomatAttribute getDiplomatAttribute(String p_categoryName){
        for (Iterator itor = m_das.iterator(); itor.hasNext();) {
            DiplomatAttribute da = (DiplomatAttribute) itor.next();
            if (da.getName().equals(p_categoryName)) {
                return da;
            }
        }
        return null;
    }

    public void addCategory(Category p_category) {
        m_categories.add(p_category);
    }

    public void removeCategory(Category p_category) {
        m_categories.remove(p_category);
    }

    public Category getCategory(String p_categoryName) {
        for (Iterator itor = m_categories.iterator(); itor.hasNext();) {
            Category category = (Category) itor.next();
            if (category.getName().equals(p_categoryName)) {
                return category;
            }
        }
        return null;
    }

    public Category[] getCategories() {
        return (Category[]) m_categories.toArray(new Category[m_categories.size()]);
    }

    public void clearCategories() {
        m_categories.clear();
    }

    // Dispatch to BatchInfo
    public int getPageCount() {
        return m_batchInfo.getPageCount();
    }

    public void setPageCount(int p_pageCount) {
        m_batchInfo.setPageCount(p_pageCount);
    }

    public int getPageNumber() {
        return m_batchInfo.getPageNumber();
    }

    public void setPageNumber(int p_pageNumber) {
        m_batchInfo.setPageNumber(p_pageNumber);
    }

    public void setDocPageCount(int p_docPageCount) {
        m_batchInfo.setDocPageCount(p_docPageCount);
    }

    public int getDocPageNumber() {
        return m_batchInfo.getDocPageNumber();
    }

    public void setDocPageNumber(int p_docPageNumber) {
        m_batchInfo.setDocPageNumber(p_docPageNumber);
    }

    public String getDisplayName() {
        return m_batchInfo.getDisplayName();
    }

    public void setDisplayName(String p_displayName) {
        m_batchInfo.setDisplayName(p_displayName);
    }
    
    public String getMasterTranslated() {
        return m_batchInfo.getMasterTranslated();
    }

    public void setMasterTranslated(String p_masterTranslated) {
        m_batchInfo.setMasterTranslated(p_masterTranslated);
    }
    
    public String getInddHiddenTranslated() {
        return m_batchInfo.getInddHiddenTranslated();
    }
    
    public void setInddHiddenTranslated(String p_inddHiddenTranslated) {
        m_batchInfo.setInddHiddenTranslated(p_inddHiddenTranslated);
    }
    
    public String getAdobeXmpTranslated() {
        return m_batchInfo.getAdobeXmpTranslated();
    }
    
    public void setAdobeXmpTranslated(String p_adobeXmpTranslated) {
        m_batchInfo.setAdobeXmpTranslated(p_adobeXmpTranslated);
    }

    // disptach to Source
    public String getSourceLocale() {
        return m_source.getLocale();
    }

    public void setSourceLocale(String p_locale) {
        m_source.setLocale(p_locale);
    }

    public String getSourceFormatType() {
        return m_source.getFormatType();
    }

    public void setSourceFormatType(String p_sourceFormatType) {
        m_source.setFormatType(p_sourceFormatType);
    }

    // dispatch to Target
    public String getTargetLocale() {
        return m_target.getLocale();
    }

    public void setTargetLocale(String p_locale) {
        m_target.setLocale(p_locale);
    }

    private void checkIntegrity() {
        if (!isValid()) {
            throw new ParserException("Some data is required, please make it complete");
        }
    }

    public String toXml() {
        //checkIntegrity();
        StringBuffer sb = new StringBuffer(1024 * 4);
        XmlUtils.appendElementStart(sb, EVENT_FLOW_XML);
        XmlUtils.appendLine(sb);
        XmlUtils.appendElement(sb, PRE_MERGE_EVENT, m_preMergeEvent);
        XmlUtils.appendElement(sb, POST_MERGE_EVENT, m_postMergeEvent);
        sb.append(m_batchInfo.serializeToXml());
        sb.append(m_source.serializeToXml());
        if (m_target != null){
            sb.append(m_target.serializeToXml());
        }
        for (Iterator itor = m_das.iterator(); itor.hasNext();) {
            DiplomatAttribute da = (DiplomatAttribute) itor.next();
            sb.append(da.serializeToXml());
        }
        for (Iterator itor = m_categories.iterator(); itor.hasNext();) {
            Category category = (Category) itor.next();
            sb.append(category.serializeToXml());
        }
        XmlUtils.appendElement(sb, CAP_MESSAGE_ID, m_capMessageId);
        if (m_exportBatchInfo != null)
        	sb.append(m_exportBatchInfo.serializeToXml());
        XmlUtils.appendElement(sb, CXE_REQUEST_TYPE, m_cxeRequestType);
        XmlUtils.appendElementEnd(sb, EVENT_FLOW_XML);
        //XmlUtils.appendLine(sb);
        return sb.toString();
    }

    /**
     * @deprecated use toXml, do not add head and DTD
     */
    public String serializeToXml() {
        // Check if all required data is filled.
        checkIntegrity();

        StringBuffer sb = new StringBuffer(1024 * 4);
        // Write the DTD first, I don't think it's necessary.
        sb.append(XmlUtil.formattedEventFlowXmlDtd());

        XmlUtils.appendElementStart(sb, EVENT_FLOW_XML);

        XmlUtils.appendElement(sb, PRE_MERGE_EVENT, m_preMergeEvent);
        XmlUtils.appendElement(sb, POST_MERGE_EVENT, m_postMergeEvent);
        sb.append(m_batchInfo.serializeToXml());
        sb.append(m_source.serializeToXml());
        if (m_target != null)
            sb.append(m_target.serializeToXml());
        for (Iterator itor = m_das.iterator(); itor.hasNext();) {
            DiplomatAttribute da = (DiplomatAttribute) itor.next();
            sb.append(da.serializeToXml());
        }
        for (Iterator itor = m_categories.iterator(); itor.hasNext();) {
            Category category = (Category) itor.next();
            sb.append(category.serializeToXml());
        }
        XmlUtils.appendElement(sb, CAP_MESSAGE_ID, m_capMessageId);
        if (m_exportBatchInfo != null)
        	sb.append(m_exportBatchInfo.serializeToXml());
        XmlUtils.appendElement(sb, CXE_REQUEST_TYPE, m_cxeRequestType);

        XmlUtils.appendElementEnd(sb, EVENT_FLOW_XML);

        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        String xml = FileUtils.read("eventFlow.xml");

        System.out.println(xml);

        EventFlow eventFlow = new EventFlow(xml);
        System.out.println("preMergeEvent = " + eventFlow.getPreMergeEvent());
        System.out.println("postMergeEvent = " + eventFlow.getPostMergeEvent());
        System.out.println("cxeRequestType = " + eventFlow.getCxeRequestType());
        System.out.println("capMessageId = " + eventFlow.getCapMessageId());

        BatchInfo batchInfo = eventFlow.getBatchInfo();
        System.out.println("displayName = " + batchInfo.getDisplayName());
        System.out.println("batchId = " + batchInfo.getBatchId());
        System.out.println("pageCount = " + batchInfo.getPageCount());
        System.out.println("pageNumber = " + batchInfo.getPageNumber());
        System.out.println("docPageCount = " + batchInfo.getDocPageCount());
        System.out.println("docPageNumber = " + batchInfo.getDocPageNumber());
        System.out.println("baseHref = " + batchInfo.getBaseHref());
        System.out.println("jobName = " + batchInfo.getJobName());
        System.out.println("l10nProfileId = " + batchInfo.getL10nProfileId());
        System.out.println("processMode = " + batchInfo.getProcessMode());

        System.out.println(eventFlow.serializeToXml());
    }

	public ExportBatchInfo getExportBatchInfo() {
		return m_exportBatchInfo;
	}

	public void setExportBatchInfo(ExportBatchInfo p_exportBatchInfo) {
		m_exportBatchInfo = p_exportBatchInfo;
	}
}