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
package com.globalsight.connector.mindtouch.form;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;

public class MindTouchConnectorFilter
{
    private String nameFilter;
    private String urlFilter;
    private String usernameFilter;
    private String companyNameFilter;
    private String descriptionFilter;

    public List<MindTouchConnector> filter(List<MindTouchConnector> conns)
    {
        List<MindTouchConnector> result = new ArrayList<MindTouchConnector>();
        
        for (MindTouchConnector conn : conns)
        {
            if (!like(nameFilter, conn.getName()))
            {
                continue;
            }

            if (!like(urlFilter, conn.getUrl()))
            {
                continue;
            }

            if (!like(usernameFilter, conn.getUsername()))
            {
                continue;
            }

            if (!like(companyNameFilter, conn.getCompanyName()))
            {
                continue;
            }
            
            if (!like(descriptionFilter, conn.getDescription()))
            {
                continue;
            }
            
            result.add(conn);
        }
        
        return result;
    }

    private boolean like(String filterValue, String candidateValue)
    {
        if (filterValue == null)
            return true;

        filterValue = filterValue.trim();
        if (filterValue.length() == 0)
            return true;

        if (candidateValue == null)
            return false;

        filterValue = filterValue.toLowerCase();
        candidateValue = candidateValue.toLowerCase();

        return candidateValue.indexOf(filterValue) > -1;
    }

    public String getNameFilter()
    {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter)
    {
        this.nameFilter = nameFilter;
    }

    public String getDescriptionFilter()
    {
        return descriptionFilter;
    }

    public void setDescriptionFilter(String descriptionFilter)
    {
        this.descriptionFilter = descriptionFilter;
    }

    public String getUrlFilter()
    {
        return urlFilter;
    }

    public void setUrlFilter(String urlFilter)
    {
        this.urlFilter = urlFilter;
    }

    public String getUsernameFilter()
    {
        return usernameFilter;
    }

    public void setUsernameFilter(String usernameFilter)
    {
        this.usernameFilter = usernameFilter;
    }

    public String getCompanyNameFilter()
    {
        return companyNameFilter;
    }

    public void setCompanyNameFilter(String companyNameFilter)
    {
        this.companyNameFilter = companyNameFilter;
    }

    
}
