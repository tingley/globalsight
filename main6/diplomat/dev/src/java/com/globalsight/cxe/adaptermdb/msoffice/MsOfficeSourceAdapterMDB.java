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
package com.globalsight.cxe.adaptermdb.msoffice;

import javax.ejb.CreateException;
import com.globalsight.cxe.adaptermdb.BaseAdapterMDB;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.msoffice.MsOfficeAdapter;

/**
 * MsOfficeSourceAdapterMDB uses the MsOfficeAdapter
 */
public class MsOfficeSourceAdapterMDB extends BaseAdapterMDB
{
    private static String ADAPTER_NAME = "MsOfficeSourceAdapter";
    
    /*
     * Returns the Adapter Name
     */
    protected String getAdapterName()
    {
        return ADAPTER_NAME;
    }

    /**
     * Creates and loads the LingAdapter
     * 
     * @return BaseAdapter
     * @exception Exception
     */
    protected BaseAdapter loadAdapter() throws Exception
    {
        return new MsOfficeAdapter(ADAPTER_NAME);
    }
}

