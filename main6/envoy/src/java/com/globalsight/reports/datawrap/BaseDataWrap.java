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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public abstract class BaseDataWrap implements Serializable
{
    private String reportTitle = null;
    private Integer totalJobNum = null;
    
    private ArrayList criteriaFormLabel = null;
    private ArrayList criteriaFormValue = null;
    
    private String txtFooter = null;
    private String txtPageNumber = null;
    private String txtDate = null;
    
    private int totalPageNum = -1;
    private int currentPageNum = 0;
    
    private List<Object> dataList = null;
    
	public List<Object> getDataList() 
    {
		return dataList;
	}

	public void setDataList(List dataList) 
    {
		this.dataList = dataList;
	}

	public int getCurrentPageNum() 
    {
		return currentPageNum;
	}

	public void setCurrentPageNum(int currentPageNum) 
    {
		this.currentPageNum = currentPageNum;
	}

	public int getTotalPageNum() 
    {
		return totalPageNum;
	}

	public void setTotalPageNum(int totalPageNum) 
    {
		this.totalPageNum = totalPageNum;
	}

	public String getTxtDate() 
    {
		return txtDate;
	}
    
	public void setTxtDate(String txtDate) 
    {
		this.txtDate = txtDate;
	}
    
	public String getTxtFooter() 
    {
		return txtFooter;
	}
    
	public void setTxtFooter(String txtFooter) 
    {
		this.txtFooter = txtFooter;
	}

	public String getTxtPageNumber() 
    {
		String tmp1 = txtPageNumber.replaceAll("\\{P\\}", currentPageNum + "");
		return tmp1.replaceAll("\\{N\\}", totalPageNum + "");
	}
    
	public void setTxtPageNumber(String txtPageNumber) 
    {
		this.txtPageNumber = txtPageNumber;
	}
  
    public Integer getTotalJobNum() 
    {
        return totalJobNum;
    }

    public void setTotalJobNum(Integer totalJobNum) 
    {
        this.totalJobNum = totalJobNum;
    }
    
    public String getReportTitle() 
    {
        return reportTitle;
    }
    
    public void setReportTitle(String reportTitle) 
    {
        this.reportTitle = reportTitle;
    }
    
    public ArrayList getCriteriaFormLabel() 
    {
        return criteriaFormLabel;
    }
    
    public void setCriteriaFormLabel(ArrayList criteriaFormLabel) 
    {
        this.criteriaFormLabel = criteriaFormLabel;
    }
    
    public ArrayList getCriteriaFormValue() 
    {
        return criteriaFormValue;
    }
    
    public void setCriteriaFormValue(ArrayList criteriaFormvalue) 
    {
        this.criteriaFormValue = criteriaFormvalue;
    }
	
}
