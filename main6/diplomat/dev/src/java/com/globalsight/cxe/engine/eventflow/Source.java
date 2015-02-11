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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.globalsight.cxe.engine.util.XmlUtils;
import com.globalsight.util.edit.EditUtil;

public class Source implements Serializable {
	private static final String SOURCE = "source";
	private static final String IMPORT_INITIATOR_ID = "importInitiatorId";
	private static final String IMPORT_REQUEST_TYPE = "importRequestType";
	private static final String PAGE_IS_CXE_PREVIEWABLE = "pageIsCxePreviewable";
	private static final String FORMAT_TYPE = "formatType";
	private static final String DATA_SOURCE_ID = "dataSourceId";
	private static final String DATA_SOURCE_TYPE = "dataSourceType";
	private static final String NAME = "name";
	private static final String DA = "da";
	private static final String CHARSET = "charset";
	private static final String LOCALE = "locale";

	public static class ImportRequestType {
		private static final String ALIGNER_STRING = "aligner";
		private static final String L10N_STRING = "l10n";
		
		public static final ImportRequestType L10N = new ImportRequestType(1, L10N_STRING);
		public static final ImportRequestType ALIGNER = new ImportRequestType(2, ALIGNER_STRING);
		
		private int m_value;
		private String m_name;
		
		private ImportRequestType(int p_value, String p_name) {
			m_value = p_value;
			m_name = p_name;
		}
		
		public static ImportRequestType getImportRequestType(String p_name) {
			if (L10N_STRING.equals(p_name)) {
				return L10N;
			} else if (ALIGNER_STRING.equals(p_name)) {
				return ALIGNER;
			} else {
				return L10N;
			}
		}
		
		public String toString() {
			return m_name;
		}
		
		public int hashCode() {
			return m_value;
		}
		
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (o instanceof ImportRequestType) {
				ImportRequestType other = (ImportRequestType)o;
				return other.m_value == this.m_value;
			}
			return false;
		}
	}
	
	private String m_locale;
	private String m_charset;
	private List m_das = new ArrayList();
	
	private String m_name;
	private String m_dataSourceType;
	private String m_dataSourceId;
	private String m_formatType;
	private boolean m_pageIsCxePreviewable = false;
	private ImportRequestType m_importRequestType = ImportRequestType.L10N;
	private String m_importInitiatorId = "";
	
	public Source() {}
	
	protected Source(Element p_elem) {
		m_locale = XmlUtils.getDirectChildElementValue(p_elem, LOCALE);
		m_charset = XmlUtils.getDirectChildElementValue(p_elem, CHARSET);
		
		List daElements = XmlUtils.getChildElements(p_elem, DA);
		for (Iterator itor = daElements.iterator(); itor.hasNext(); ) {
			Element daElement = (Element) itor.next();
			m_das.add(new DiplomatAttribute(daElement));
		}
		
		m_name = XmlUtils.getAttributeValue(p_elem, NAME);
		m_dataSourceType = XmlUtils.getAttributeValue(p_elem, DATA_SOURCE_TYPE);
		m_dataSourceId = XmlUtils.getAttributeValue(p_elem, DATA_SOURCE_ID);
		m_formatType = XmlUtils.getAttributeValue(p_elem, FORMAT_TYPE);
		m_pageIsCxePreviewable = Boolean.valueOf(XmlUtils.getAttributeValue(p_elem, PAGE_IS_CXE_PREVIEWABLE)).booleanValue();
		m_importRequestType = ImportRequestType.getImportRequestType(XmlUtils.getAttributeValue(p_elem, IMPORT_REQUEST_TYPE));
		m_importInitiatorId = XmlUtils.getAttributeValue(p_elem, IMPORT_INITIATOR_ID);
	}
	
	public boolean isValid() {
		return (m_locale != null)
			&& (m_charset != null)
			&& (m_name != null)
			&& (m_dataSourceType != null)
			&& (m_dataSourceId != null)
			&& (m_formatType != null)
			&& (m_importRequestType != null);
	}
	
	public String getCharset() {
		return m_charset;
	}
	public void setCharset(String m_charset) {
		this.m_charset = m_charset;
	}
	
	public String getDataSourceId() {
		return m_dataSourceId;
	}
	public void setDataSourceId(String p_sourceId) {
		m_dataSourceId = p_sourceId;
	}
	
	public String getDataSourceType() {
		return m_dataSourceType;
	}
	public void setDataSourceType(String p_sourceType) {
		m_dataSourceType = p_sourceType;
	}
	
	public String getFormatType() {
		return m_formatType;
	}
	public void setFormatType(String p_type) {
		m_formatType = p_type;
	}
	
	public String getImportInitiatorId() {
		return m_importInitiatorId;
	}
	public void setImportInitiatorId(String p_initiatorId) {
		m_importInitiatorId = p_initiatorId;
	}
	
	public ImportRequestType getImportRequestType() {
		return m_importRequestType;
	}
	public void setImportRequestType(ImportRequestType p_requestType) {
		m_importRequestType = p_requestType;
	}
	
	public String getLocale() {
		return m_locale;
	}
	public void setLocale(String p_locale) {
		m_locale = p_locale;
	}
	
	public String getName() {
		return m_name;
	}
	public void setName(String p_name) {
		m_name = p_name;
	}
	
	public boolean isPageIsCxePreviewable() {
		return m_pageIsCxePreviewable;
	}
	public void setPageIsCxePreviewable(boolean p_isCxePreviewable) {
		m_pageIsCxePreviewable = p_isCxePreviewable;
	}
	
	public void addDiplomatAttribute(DiplomatAttribute p_da) {
		m_das.add(p_da);
	}
	
	public void removeDiplomatAttribute(DiplomatAttribute p_da) {
		m_das.remove(p_da);
	}
	
	public String serializeToXml() {
		StringBuffer sb = new StringBuffer(200);
		String[][] attributes = new String[][] {
				{NAME, m_name},
				{DATA_SOURCE_TYPE, m_dataSourceType},
				{DATA_SOURCE_ID, m_dataSourceId},
				{FORMAT_TYPE, m_formatType},
				{PAGE_IS_CXE_PREVIEWABLE, String.valueOf(m_pageIsCxePreviewable)},
				{IMPORT_REQUEST_TYPE, m_importRequestType.toString()},
				{IMPORT_INITIATOR_ID, EditUtil.encodeXmlEntities(m_importInitiatorId)}
		};
		XmlUtils.appendElementStart(sb, SOURCE, attributes);
		XmlUtils.appendLine(sb);
		XmlUtils.appendElement(sb, LOCALE, m_locale);
		XmlUtils.appendElement(sb, CHARSET, m_charset);
		
		for (Iterator itor = m_das.iterator(); itor.hasNext(); ) {
			DiplomatAttribute da = (DiplomatAttribute) itor.next();
			sb.append(da.serializeToXml());
		}
		XmlUtils.appendElementEnd(sb, SOURCE);
		XmlUtils.appendLine(sb);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Source source = new Source();
		source.setCharset("utf-8");
		source.setDataSourceId("1105");
		source.setDataSourceType("fs");
		source.setImportInitiatorId("lenard");
		source.setImportRequestType(ImportRequestType.ALIGNER);
		source.setName("task/abc");
		source.addDiplomatAttribute(new DiplomatAttribute());
		
		System.out.println(source.serializeToXml());
	}
}
