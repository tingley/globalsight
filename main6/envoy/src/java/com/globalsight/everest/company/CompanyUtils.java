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
package com.globalsight.everest.company;

import com.globalsight.everest.servlet.util.ServerProxy;

/**
 * @deprecated
 */
public class CompanyUtils {
    /**
     * I think we should put Company to ThreadLocal but not companyId,
     * so we can get its name easily, as we will use the company name frequently.
     * But its a great job. so I add these util methods.
     * (it's a poor way, for we should access from database each time).
     * 
     * @deprecated
     */
	public static String getCompanyNameById(String id){
	    Company company=null;
	    try{
	        company=ServerProxy.getJobHandler().getCompanyById(Long.parseLong(id));
	    }catch(Exception e){
	        return "Null Company";
	    }
	    if(company==null){
	        return "Null Company";
	    }
	    return company.getName();
	}
    
    /**
     * @deprecated
     */
	public static String getCurrentCompanyId() {
        return CompanyThreadLocal.getInstance().getValue();
    }

    /**
     * @deprecated
     */
    public static long getCurrentCompanyIdAsLong() {
        return Long.parseLong(getCurrentCompanyId());
    }

}
