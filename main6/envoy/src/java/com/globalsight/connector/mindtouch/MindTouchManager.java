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
package com.globalsight.connector.mindtouch;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class MindTouchManager
{
    private static final Logger logger = Logger
            .getLogger(MindTouchManager.class);


    /**
     * Return all MindTouch connectors.
     * 
     * @return List<?>
     */
    public static List<?> getAllConnectors()
    {
        String hql = "from MindTouchConnector e where e.isActive = 'Y' ";
        HashMap<String, Long> map = new HashMap<String, Long>();

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and e.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }

    public static MindTouchConnector getMindTouchConnectorById(long mtcId)
    {
        return HibernateUtil.get(MindTouchConnector.class, mtcId);
    }
}
