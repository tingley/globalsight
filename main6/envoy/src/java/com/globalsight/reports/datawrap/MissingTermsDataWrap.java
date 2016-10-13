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

public class MissingTermsDataWrap extends BaseDataWrap 
{
	private String pageHeader = null;
	private ArrayList missingItems = new ArrayList();
	private String pageFooter = null;
	
	public void setPageHeader(String header) 
    {
		this.pageHeader = header;
	}
    
	public String getPageHeader() 
    {
		return this.pageHeader;
	}
	
	public void setMissingItems(ArrayList missingItems) 
    {
		this.missingItems = missingItems;
	}
    
	public ArrayList getMissingItems() 
    {
		return this.missingItems;
	}
	
	public void setPageFooter(String footer) 
    {
		this.pageFooter = footer;
	}
    
	public String getPageFooter() 
    {
		return this.pageFooter;
	}
	
}
