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

import java.util.List;

public class CostsByLocaleReportDataWrap extends BaseDataWrap 
{
    private List tableHeadList = null;
    private String description = null;
    private double[][] data;
    private String[] rowKeys; 
    private String[] columnKeys;
    public double[][] getData() {
		return data;
	}

	public void setData(double[][] data) {
		this.data = data;
	}

	public String[] getRowKeys() {
		return rowKeys;
	}

	public void setRowKeys(String[] rowKeys) {
		this.rowKeys = rowKeys;
	}

	public String[] getColumnKeys() {
		return columnKeys;
	}

	public void setColumnKeys(String[] columnKeys) {
		this.columnKeys = columnKeys;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List getTableHeadList() 
    {
        return tableHeadList;
    }

    public void setTableHeadList(List tableHeadListIn) 
    {
        this.tableHeadList = tableHeadListIn;
    }
    

}
