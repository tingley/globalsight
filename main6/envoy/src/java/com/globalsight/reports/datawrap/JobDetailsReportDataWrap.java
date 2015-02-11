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
import java.util.LinkedHashMap;
import java.util.List;

public class JobDetailsReportDataWrap extends BaseDataWrap 
{
    private ArrayList jobFormValue = null;
    private ArrayList jobFormLabel = null;

    public ArrayList getJobFormValue() 
    {
        if(jobFormValue == null)
        {
            jobFormValue = new ArrayList();
        }
        return jobFormValue;
    }
    
    public void setJobFormValue(ArrayList jobFormValueIn) 
    {
        this.jobFormValue = jobFormValueIn;
    }
    
    public ArrayList getJobFormLabel() 
    {
        if(jobFormLabel == null)
        {
            jobFormLabel = new ArrayList();
        }
        return jobFormLabel;
    }
    
    public void setJobFormLabel(ArrayList jobFormLabelIn) 
    {
        this.jobFormLabel = jobFormLabelIn;
    }
   
    // for pagenation
    public void addPageData(LinkedHashMap dataLinkedMap)
    {
        List tempList = this.getDataList();
        if( tempList == null)
        {
            tempList = new ArrayList();
            this.setDataList(tempList);
        }
        tempList.add(dataLinkedMap);   
    }
    
    public LinkedHashMap<String, Object> gainCurrentPageData(int currentPageNum)
    {
        List<Object> curList = this.getDataList();
        if(curList == null)
        {
            LinkedHashMap<?, ?> tmpLinkMap = new LinkedHashMap<Object,Object>();
            addPageData(tmpLinkMap);
            curList = this.getDataList();
        }
        
        if(0 < currentPageNum && (currentPageNum < curList.size() + 1) )
        {
            return (LinkedHashMap<String, Object>)(curList.get( currentPageNum -1 )); 
        }
        else
        {
            return null;
        }        
    }
}
