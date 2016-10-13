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

package com.globalsight.diplomat.servlet.config;

import java.util.Vector;
import com.globalsight.diplomat.servlet.config.LocalizationProfileRouting;

public class LocalizationProfileTarget
{
	private long m_id = 0;
	private int m_language = 0;
	private int m_languageTM = 0;
   private int m_TM_matchStyle = 0; //0 is exact match, 1 is exact match with text-only
	private boolean m_pageTM = false;
	private int m_characterSet = 0;
	private Vector m_routingList = null;
	
	/////////////////////////////////////////////////
	public LocalizationProfileTarget (int p_language, int p_languageTM,
		boolean p_pageTM, int p_TM_matchStyle, int p_characterSet, Vector p_routingList)
	{
		this (0, p_language, p_languageTM, p_pageTM, p_TM_matchStyle, p_characterSet, p_routingList);
	}
	
	/////////////////////////////////////////////////
	public LocalizationProfileTarget (int p_language, int p_languageTM,
		boolean p_pageTM, int p_TM_matchStyle, int p_characterSet)
	{
		this (0, p_language, p_languageTM, p_pageTM, p_TM_matchStyle, p_characterSet, new Vector());
	}
	
	/////////////////////////////////////////////////
	public LocalizationProfileTarget (long p_id, int p_language, int p_languageTM,
		boolean p_pageTM, int p_TM_matchStyle, int p_characterSet)
	{
		this (p_id, p_language, p_languageTM, p_pageTM, p_TM_matchStyle, p_characterSet, new Vector());
	}
	
	/////////////////////////////////////////////////
	public LocalizationProfileTarget (long p_id, int p_language, int p_languageTM,
		boolean p_pageTM, int p_TM_matchStyle, int p_characterSet, Vector p_routingList)
	{
		m_id = p_id;
		m_language = p_language;
		m_languageTM = p_languageTM;
		m_pageTM = p_pageTM;
                m_TM_matchStyle = p_TM_matchStyle;
		m_characterSet = p_characterSet;
		m_routingList = p_routingList;	
	}
	
	/////////////////////////////////////////////////
	public long getID() { return m_id; }
	public int getLanguage() { return m_language; }
	public int getLanguageTM() { return m_languageTM; }
	public boolean getPageTM() { return m_pageTM; }
        public int getTM_MatchStyle() { return m_TM_matchStyle; }
	public int getCharacterSet() { return m_characterSet; }
	public Vector getRoutingList() { return m_routingList; }
	public void setID(long p_id) { m_id = p_id; }
	public void setLanguage(int p_language) { m_language = p_language; }
	public void setLanguageTM(int p_languageTM) { m_languageTM = p_languageTM; }
	public void setPageTM(boolean p_pageTM) { m_pageTM = p_pageTM; }
	public void setCharacterSet(int p_characterSet) { m_characterSet = p_characterSet; }
	public void setRoutingList(Vector p_routingList) { m_routingList = p_routingList; }
	
	/////////////////////////////////////////////////
	public int getRoutingSize() { return m_routingList.size(); }
	
	/////////////////////////////////////////////////
	public void addRoutingEntry(LocalizationProfileRouting p_route)
	{
		m_routingList.addElement(p_route);		
	}
	
	/////////////////////////////////////////////////
	public boolean deleteRoutingEntry(LocalizationProfileRouting p_route)
	{
		return m_routingList.removeElement( (Object)p_route );		
	}
}
