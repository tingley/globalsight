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
package com.globalsight.cxe.entity.segmentationrulefile;

import org.xml.sax.InputSource;

/**
 * This class is for Segmentation Rule, till now there is only one
 * type called "SRX 2.0", it store 0 in DB (m_types[0] == "SRX 2.0"). 
 * 
 * If add new type, do:
 * 1. add name "your new type name" to m_types, assume its index is 1 
 *    and name is "SRX 3.0". 
 *    private static String[] m_types = { "SRX 2.0", "SRX 3.0" };
 * 2. add new XML dtd (or other ) into SegmentationRuleFileValidator.java
 *    to validator new Rule
 *    switch(p_type)
 *		{
 *			case 0:
 *			in = new InputSource(
 *					SegmentationRuleFileValidator.class
 *							.getResourceAsStream("/properties/SRX2.0.xsd"));
 *			break;
 *			
 *			// validator SRX 3.0
 *			case 1:
 *			in = new InputSource(
 *					SegmentationRuleFileValidator.class
 *							.getResourceAsStream("/properties/SRX3.0.xsd"));
 *			break;
 *          
 *			default:
 *			in = new InputSource(
 *					SegmentationRuleFileValidator.class
 *							.getResourceAsStream("/properties/SRX2.0.xsd"));
 *			break;
 *		}
 *    
 * 
 */
public class SegmentationRuleFileType
{
	//	private static String [] m_types = {
	//		"SRX 2.0", "SRX 1.0"
	//	};
	private static String[] m_types = { "SRX 2.0" };

	public static String getTypeString(int p_index)
	{
		if (-1 < p_index && m_types.length > p_index)
		{
			return m_types[p_index];
		}
		else
		{
			return m_types[0];
		}
	}

	public static int getTypeIndex(String p_type)
	{
		for (int i = 0; i < m_types.length; i++)
		{
			if (p_type.equals(m_types[i]))
			{
				return i;
			}
		}

		return 0;
	}

	public static String[] getTypeList()
	{
		return m_types;
	}
}
