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

public class Target implements Serializable {
	private static final String TARGET = "target";
	private static final String PREVIEW_URL = "previewUrl";
	private static final String DATABASE_MODE = "databaseMode";
	private static final String NAME = "name";
	private static final String DA = "da";
	private static final String CHARSET = "charset";
	private static final String LOCALE = "locale";

	public static class DatabaseMode {
		private static final String PREVIEW_STRING = "preview";
		private static final String FINAL_STRING = "final";
		public static final DatabaseMode FINAL = new DatabaseMode(0, FINAL_STRING);
		public static final DatabaseMode PREVIEW = new DatabaseMode(1, PREVIEW_STRING);
		
		private int m_value;
		private String m_name;
		
		private DatabaseMode(int p_value, String p_name) {
			m_value = p_value;
			m_name = p_name;
		}
		
		public static DatabaseMode getDatabaseMode(String p_name) {
			if (FINAL_STRING.equals(p_name)) {
				return DatabaseMode.FINAL;
			} else if (PREVIEW_STRING.equals(p_name)) {
				return DatabaseMode.PREVIEW;
			} else {
			    return DatabaseMode.FINAL;//default
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
			if (o instanceof DatabaseMode) {
				DatabaseMode other = (DatabaseMode) o;
				return other.m_value == this.m_value;
			}
			return false;
		}
	}
	
	private String m_locale;
	private String m_charset;
	private List m_das = new ArrayList();
	
	private String m_name;
	private DatabaseMode m_databaseMode = DatabaseMode.FINAL;
	private boolean m_previewUrl = false;
	
	public Target() {}
	
	protected Target(Element p_elem) {
		m_locale = XmlUtils.getDirectChildElementValue(p_elem, LOCALE);
		m_charset = XmlUtils.getDirectChildElementValue(p_elem, CHARSET);
		List daElements = XmlUtils.getChildElements(p_elem, DA);
		for (Iterator itor = daElements.iterator(); itor.hasNext(); ) {
			Element daElement = (Element) itor.next();
			m_das.add(new DiplomatAttribute(daElement));
		}
		
		m_name = XmlUtils.getAttributeValue(p_elem, NAME);
		m_databaseMode = DatabaseMode.getDatabaseMode(XmlUtils.getAttributeValue(p_elem, DATABASE_MODE));
		m_previewUrl = Boolean.valueOf(XmlUtils.getAttributeValue(p_elem, PREVIEW_URL)).booleanValue();
	}
	
	public boolean isValid() {
		return (m_locale != null)
			&& (m_charset != null)
			&& (m_name != null)
			&& (m_databaseMode != null);
	}
	
	public String getCharset() {
		return m_charset;
	}
	public void setCharset(String charset) {
		this.m_charset = charset;
	}
	
	public DatabaseMode getDatabaseMode() {
		return m_databaseMode;
	}
	public void setDatabaseMode(DatabaseMode databaseMode) {
		this.m_databaseMode = databaseMode;
	}
	
	public String getLocale() {
		return m_locale;
	}
	public void setLocale(String locale) {
		this.m_locale = locale;
	}
	
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		this.m_name = name;
	}
	
	public boolean isPreviewUrl() {
		return m_previewUrl;
	}
	public void setPreviewUrl(boolean previewUrl) {
		this.m_previewUrl = previewUrl;
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
				{DATABASE_MODE, m_databaseMode.toString()},
				{PREVIEW_URL, String.valueOf(m_previewUrl)}
		};
		XmlUtils.appendElementStart(sb, TARGET, attributes);
		XmlUtils.appendLine(sb);
		XmlUtils.appendElement(sb, LOCALE, m_locale);
		XmlUtils.appendElement(sb, CHARSET, m_charset);
		
		for (Iterator itor = m_das.iterator(); itor.hasNext(); ) {
			DiplomatAttribute da = (DiplomatAttribute) itor.next();
			sb.append(da.serializeToXml());
		}
		XmlUtils.appendElementEnd(sb, TARGET);
		XmlUtils.appendLine(sb);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Target target = new Target();
		target.setName("targetName");
		target.setDatabaseMode(DatabaseMode.PREVIEW);
		target.setPreviewUrl(true);
		
		target.setLocale("zh");
		target.setCharset("utf-8");
		DiplomatAttribute da = new DiplomatAttribute();
		da.setName("daName");
		da.addValue("daValue1");
		da.addValue("daValue2");
		
		target.addDiplomatAttribute(da);
		
		System.out.println(target.serializeToXml());
	}
}
