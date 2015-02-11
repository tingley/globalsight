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
package com.globalsight.everest.segmentationhelper;

/**
 * LanguageMap represents languagemap element in a 
 * segmentation rule file writen in xml format.
 * @author holden.cai
 *
 */
public class LanguageMap {
	/**
	 * languagepattern attribute of languagemap element.
	 */
	private String m_languagePattern;
	
	/**
	 * languagerulename attribute of languagemap element.
	 */
	private String m_languageruleName;
	
	public LanguageMap(String p_pattern, String p_name) 
	{
		m_languagePattern = p_pattern;
		m_languageruleName = p_name;
	}
	
	public LanguageMap() 
	{
		m_languagePattern = null;
		m_languageruleName = null;
	}
	
	public String getLanguagePattern() 
	{
		return m_languagePattern;
	}
	
	public void setLanguagePattern(String p_pattern) 
	{
		m_languagePattern = p_pattern;
	}
	
	public String getLanguageruleName() 
	{
		return m_languageruleName;
	}
	
	public void setLanguageruleName(String p_name) 
	{
		m_languageruleName = p_name;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("m_languagePattern: ");
		sb.append(m_languagePattern + "\r\n");
		sb.append("m_languageruleName");
		sb.append(m_languageruleName + "\r\n");
		return sb.toString();
	}

}
