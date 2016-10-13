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

import java.io.Serializable;

// Structure for record dependency
public class Dependency implements Serializable
{
	private long m_id = 0;

	private String m_profileName = "";

	private String m_table = "";

	public Dependency(long p_id, String p_profileName, String p_table)
	{
		m_id = p_id;
		m_profileName = p_profileName;
		m_table = p_table;
	}

	public long getID()
	{
		return m_id;
	}

	public String getProfileName()
	{
		return m_profileName;
	}

	public String getTable()
	{
		return m_table;
	}

	public void setID(long p_id)
	{
		m_id = p_id;
	}

	public void setProfileName(String p_profileName)
	{
		m_profileName = p_profileName;
	}

	public void setTable(String p_table)
	{
		m_table = p_table;
	}
}