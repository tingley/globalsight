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
package com.globalsight.everest.webapp.javabean;
import java.io.Serializable;


/**
 * Simple bean class used to hold Name,Id information for generic objects.
 */
public class NameIdPair implements Serializable
{
    private String m_name;
    private Long m_id;

    public NameIdPair()
    {
    }

    public NameIdPair(String p_name, Long m_id)
    {
	setName(p_name);
	setId(m_id);
    }

    public void setName(String p_name)
    {
	m_name = p_name;
    }

    public void setId(Long p_id)
    {
	m_id = p_id;
    }

    public String getName()
    {
	return m_name;
    }

    public Long getId()
    {
	return m_id;
    }
}

