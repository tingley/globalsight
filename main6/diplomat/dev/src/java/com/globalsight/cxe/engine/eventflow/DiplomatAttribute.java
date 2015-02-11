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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.globalsight.cxe.engine.util.XmlUtils;
import com.globalsight.util.edit.EditUtil;

public class DiplomatAttribute implements Serializable {
	private static final String DA = "da";
	private static final String NAME = "name";
	private static final String DV = "dv";

	private List m_dvs = new ArrayList();
	
	private String m_name;
	
	public DiplomatAttribute() {}
	
	public DiplomatAttribute(String p_name) {
		m_name = p_name;
	}
	
	public DiplomatAttribute(String p_name, String p_value) {
		this(p_name, new String[]{p_value});	
	}
	
	public DiplomatAttribute(String p_name, String[] p_values) {
		this(p_name);
		m_dvs = Arrays.asList(p_values);
	}
	
	protected DiplomatAttribute(Element p_elem) {
		m_dvs = XmlUtils.getDirectChildElementValues(p_elem, DV);
		
		m_name = XmlUtils.getAttributeValue(p_elem, NAME);
	}
	
	public boolean isValid() {
		return m_name != null;
	}
	
	public String getName() {
		return m_name;
	}
	
	public void setName(String p_name) {
		m_name = p_name;
	}
	
	public void addValue(String p_dv) {
		m_dvs.add(p_dv);
	}
	
	public void removeValue(String p_dv) {
		m_dvs.remove(p_dv);
	}
	
	public String getValue() {
		if (m_dvs.size() >= 0)
			return ((String) m_dvs.get(0));
		return null;
	}
	
	public String[] getValues() {
		return (String[]) m_dvs.toArray(new String[m_dvs.size()]);
	}
	
	public String serializeToXml() {
		StringBuffer sb = new StringBuffer(200);
		XmlUtils.appendElementStart(sb, DA, new String[][]{{NAME, m_name}});
		XmlUtils.appendLine(sb);
		for (Iterator itor = m_dvs.iterator(); itor.hasNext();) {
			String value = (String) itor.next();
			XmlUtils.appendElement(sb, DV, EditUtil.encodeXmlEntities(value));
		}
		XmlUtils.appendElementEnd(sb, DA);
		XmlUtils.appendLine(sb);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		DiplomatAttribute da1 = new DiplomatAttribute();
		da1.setName("da1Name");
		System.out.println("============da1:");
		System.out.println(da1.serializeToXml());
		
		DiplomatAttribute da2 = new DiplomatAttribute();
		da2.setName("da2Name");
		da2.addValue("value1");
		da2.addValue("value2");
		System.out.println("============da2:");
		System.out.println(da2.serializeToXml());
	}
}
