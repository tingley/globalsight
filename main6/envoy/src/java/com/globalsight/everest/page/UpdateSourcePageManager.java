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

package com.globalsight.everest.page;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class UpdateSourcePageManager
{
    static private final Logger logger = Logger
            .getLogger(UpdateSourcePageManager.class);

    public static List<UpdatedSourcePage> getAllUpdatedSourcePage(Job job)
    {
        String hql = "from UpdatedSourcePage p where p.jobId = :jobId";
        Map map = new HashMap();
        map.put("jobId", job.getId());
        List result = HibernateUtil.search(hql, map);

        if (result.size() > 0)
        {
            List<SourcePage> sourcePages = (List) job.getSourcePages();
            for (SourcePage page : sourcePages)
            {
                for (int i = result.size() - 1; i >= 0; i--)
                {
                    UpdatedSourcePage uPage = (UpdatedSourcePage) result.get(i);
                    if (uPage.equal(page))
                    {
                        try
                        {
                            HibernateUtil.delete(uPage);
                        }
                        catch (Exception e)
                        {
                            logger.error(e.getMessage(), e);
                        }

                        result.remove(i);
                        break;
                    }
                }

                if (result.size() == 0)
                {
                    break;
                }
            }
        }

        return result;
    }

    public static void removeAllUpdatedFiles(long jobId)
    {
        String hql = "from UpdatedSourcePage p where p.jobId = :jobId";
        Map map = new HashMap();
        map.put("jobId", jobId);
        List result = HibernateUtil.search(hql, map);
        try
        {
            HibernateUtil.delete(result);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public static void sort(List<UpdatedSourcePage> pages)
    {
        SortUtil.sort(pages, new Comparator<UpdatedSourcePage>()
        {
            @Override
            public int compare(UpdatedSourcePage o1, UpdatedSourcePage o2)
            {
                return o1.getExternalPageId().compareTo(o2.getExternalPageId());
            }
        });
    }
}
