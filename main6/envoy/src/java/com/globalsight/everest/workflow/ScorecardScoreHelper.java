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
package com.globalsight.everest.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class ScorecardScoreHelper {
	
	static private final Logger logger = Logger.getLogger(ScorecardScoreHelper.class);
	
	public static List<ScorecardScore> getScoreByWrkflowId(long workflowId)
	{
		String hql = "from ScorecardScore as s where s.workflowId = " +
			workflowId + " and isActive = 'Y' ";
		return (List<ScorecardScore>) HibernateUtil.search(hql);
	}
	
	public static List<ScorecardScore> getScoreByJobId(long jobId)
	{
		String hql = "from ScorecardScore as s where s.jobId = " +
			jobId + " and isActive = 'Y' ";
		return (List<ScorecardScore>) HibernateUtil.search(hql);
	}
    
	public static List<Select> initSelectList(long companyId, ResourceBundle bundle)
    {
		List<String> keyList = getScorecardCategories(companyId);
        List<Select> list = new ArrayList<Select>();
        for (String key : keyList)
        {
            String valueOfSelect = "";
            try
            {
                valueOfSelect = bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                valueOfSelect = key;
            }
            // we should put value both at key and value places
            Select option = new Select(key, valueOfSelect);
            list.add(option);
        }
        return list;
    }
	
	public static List<String> getScorecardCategories(long companyId)
    {
    	List<String> scorecardCategories = CompanyWrapper
			.getCompanyScorecardCategoryList(String.valueOf(companyId));
    	return scorecardCategories;
    }
}
