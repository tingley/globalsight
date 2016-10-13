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
import java.util.*;

import org.w3c.dom.Element;

import com.globalsight.cxe.engine.util.XmlUtils;

public class Category implements Serializable {

    private static final long serialVersionUID = -8970737177213268452L;

    private static final String POST_MERGE_EVENT = "postMergeEvent";
	private static final String CATEGORY = "category";
	private static final String NAME = "name";
	private static final String DA = "da";

	private List m_das = new ArrayList();
	
	private String m_name;
	
	public Category() {}
	
	public Category(String p_name) {
		m_name = p_name;
	}
	
	public Category(String p_name, DiplomatAttribute[] p_das) {
		this(p_name);
		m_das = Arrays.asList(p_das);
	}
	
	protected Category(Element p_elem) {
		List daElements = XmlUtils.getChildElements(p_elem, DA);
		for (Iterator itor = daElements.iterator(); itor.hasNext(); ) {
			Element daElement = (Element) itor.next();
			m_das.add(new DiplomatAttribute(daElement));
		}
		
		m_name = XmlUtils.getAttributeValue(p_elem, NAME);
	}
	
	public String getName() {
		return m_name;
	}
	
	public void setName(String p_name) {
		m_name = p_name;
	}
	
	public void addDiplomatAttribute(DiplomatAttribute p_da) {
		m_das.add(p_da);
	}
	
	public void removeDiplomatAttribute(DiplomatAttribute p_da) {
		m_das.remove(p_da);
	}
	
	public DiplomatAttribute getDiplomatAttribute(String p_name) {
		for (Iterator itor = m_das.iterator(); itor.hasNext(); ) {
			DiplomatAttribute da = (DiplomatAttribute) itor.next();
			if (da.getName().equals(p_name))
				return da;
		}
		return null;
	}
	
	public DiplomatAttribute[] getDiplomatAttributes() {
		return (DiplomatAttribute[]) m_das.toArray(new DiplomatAttribute[m_das.size()]);
	}
	
	public String serializeToXml() {
		StringBuffer sb = new StringBuffer(200);
		XmlUtils.appendElementStart(sb, CATEGORY, new String[][]{{NAME, m_name}});
		XmlUtils.appendLine(sb);
		for (Iterator itor = m_das.iterator(); itor.hasNext();) {
			DiplomatAttribute da = (DiplomatAttribute) itor.next();
			sb.append(da.serializeToXml());
		}
		XmlUtils.appendElementEnd(sb, CATEGORY);
		XmlUtils.appendLine(sb);
		return sb.toString();
	}
	
	// convient method
	public String getPostMergeEvent() {
		return getDiplomatAttribute(POST_MERGE_EVENT).getValue();
	}
	
	public static void main(String[] args) {
		Category cat1 = new Category();
		cat1.setName("categoryName");		
		DiplomatAttribute da1 = new DiplomatAttribute();
		da1.setName("da1Name");
		da1.addValue("value1");
		da1.addValue("value2");
		cat1.addDiplomatAttribute(da1);
		DiplomatAttribute da2 = new DiplomatAttribute();
		da2.setName("da2Name");
		cat1.addDiplomatAttribute(da2);
		
		System.out.println(cat1.serializeToXml());
	}
}
