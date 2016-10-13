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
package com.globalsight.reports.datawrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.reports.datawrap.BaseDataWrap;

/**
 * @author Jerome.He
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CostingReportDataWrap extends BaseDataWrap 
{
    private ArrayList jobFormValue = null;
    private ArrayList jobFormLabel = null;
    
    private String totalCostNote = null;
    
    private String lineInGreen = null;
    
    private Map<String,String> lineInGreenMap = new HashMap<String,String>();
    
    // for job form !
    public ArrayList getJobFormValue() 
    {
        return jobFormValue;
    }
    
    public void setJobFormValue(ArrayList jobFormValueIn) 
    {
        this.jobFormValue = jobFormValueIn;
    }
    
    public ArrayList getJobFormLabel() 
    {
        return jobFormLabel;
    }
    
    public void setJobFormLabel(ArrayList jobFormLabelIn) 
    {
        this.jobFormLabel = jobFormLabelIn;
    }
    
    // for TotalCostNote
    public void setTotalCostNote(String totalCostNoteIn)
    {
        if(totalCostNote != null)
        {
            StringBuffer tempStrBuf = new StringBuffer(this.totalCostNote);
            tempStrBuf.append("<br>");
            tempStrBuf.append(totalCostNoteIn);
            totalCostNote = tempStrBuf.toString();
        }
        else
        {
            this.totalCostNote = totalCostNoteIn;
        }        
    }
    
    public String getTotalCostNote()
    {
        return this.totalCostNote;
    }
  
    // for pagenation
    public void addPageData(Map dataLinkedMap)
    {
        List tempList = this.getDataList();
        if( tempList == null)
        {
            tempList = new ArrayList();
            this.setDataList(tempList);
        }
        tempList.add(dataLinkedMap);   
    }
    
    public LinkedHashMap gainCurrentPageData(int currentPageNum)
    {
        List curList = this.getDataList();
        if(curList == null)
        {
            LinkedHashMap tmpLinkMap = new LinkedHashMap();
            addPageData(tmpLinkMap);
            curList = this.getDataList();
        }
        if(0 < currentPageNum && (currentPageNum < curList.size() + 1) )
        {
            return (LinkedHashMap)(curList.get( currentPageNum -1 )); 
        }
        else
        {
            return null;
        }        
    }

	public String getLineInGreen()
	{
		return lineInGreen;
	}

	public void setLineInGreen(String lineInGreen)
	{
		this.lineInGreen = lineInGreen;
	}

	public Map<String, String> getLineInGreenMap()
    {
    	return lineInGreenMap;
    }

	public void setLineInGreenMap(Map<String, String> lineInGreenMap)
    {
    	this.lineInGreenMap = lineInGreenMap;
    }
}
