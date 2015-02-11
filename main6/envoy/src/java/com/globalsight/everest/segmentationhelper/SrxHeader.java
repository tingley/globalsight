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

import java.util.*;
/**
 * The SrxHeader provides a datastructure to hold the header
 * element's information of one segmentation rule file writen
 * in xml format.
 * @author holden.cai
 *
 */

public class SrxHeader {
	
	/**
	 * segmentationsubflows attribute of header element,
	 * m_isSegmentsubflows will be ture if the value
	 * of segmentsubflows is "yes", and false if "no"
	 */
	private boolean m_isSegmentsubflows;

	/**
	 * cascade attribute of header element,
	 * m_isCascade will be true if the value of cascade
	 * is "yes", and false if "no"
	 */
	private boolean m_isCascade;

	/**
	 * To hold information of formathandle elements.
	 */
	private HashMap formatHandle = new HashMap();
	
	/**
	 * @param segmentsubflows
	 * @param cascade
	 * @param formatHandle
	 */
	public SrxHeader() 
	{
		m_isSegmentsubflows = false;
		m_isCascade = false;
		formatHandle = null;
	}
	
	public SrxHeader(boolean p_segmentsubflows, boolean p_cascade, 
			HashMap p_formatHandle) 
	{
		m_isSegmentsubflows = p_segmentsubflows;
		m_isCascade = p_cascade;
		formatHandle = p_formatHandle;
	}

	public HashMap getFormatHandle() 
	{
		return formatHandle;
	}
	
	public void setFormatHandle(HashMap formatHandle) 
	{
		this.formatHandle = formatHandle;
	}
	
	public boolean isCascade() 
	{
		return m_isCascade;
	}
	
	/**
	 * @deprecated Use {@link #isCascade(boolean)} instead
	 */
	public void setCascade(boolean cascade) 
	{
		isCascade(cascade);
	}

	public void isCascade(boolean cascade) 
	{
		m_isCascade = cascade;
	}
	
	public boolean isSegmentsubflows() 
	{
		return m_isSegmentsubflows;
	}
	
	/**
	 * @deprecated Use {@link #isSegmentsubflows(boolean)} instead
	 */
	public void setSegmentsubflows(boolean segmentsubflows) 
	{
		isSegmentsubflows(segmentsubflows);
	}

	public void isSegmentsubflows(boolean segmentsubflows) 
	{
		m_isSegmentsubflows = segmentsubflows;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("m_isSegmentsubflows: ");
		sb.append(m_isSegmentsubflows + "\n");
		sb.append("m_isCascade: ");
		sb.append(m_isCascade + "\n");
		Set format = formatHandle.keySet();
		Iterator formatIter = format.iterator();
		while (formatIter.hasNext())
		{
			String type = (String)formatIter.next();
			String include = (String)formatHandle.get(type);
			sb.append(type + " = " + include + "\n");
		}
		return sb.toString();
	}

	

}
