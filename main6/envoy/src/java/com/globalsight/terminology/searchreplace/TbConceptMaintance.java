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

package com.globalsight.terminology.searchreplace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.util.SqlUtil;

public class TbConceptMaintance extends TbMaintance
{
    static private final Logger CATEGORY = Logger
            .getLogger(TbConceptMaintance.class);

    public TbConceptMaintance(SearchReplaceParams rp, Termbase m_termbase)
    {
        super(rp, m_termbase);
    }

    @Override
    public void replace(long levelId, String oldFieldText, String replaceText)
    {
        try
        {
            TbConcept tt = HibernateUtil.get(TbConcept.class, levelId);
            String xml = replaceField(tt.getXml(), oldFieldText, replaceText);
            tt.setXml(xml);
            HibernateUtil.save(tt);
        }
        catch (Exception e)
        {
            CATEGORY.error(oldFieldText + " replace error.");
        }
        
    }

    @Override
    public ArrayList search()
    {
        CATEGORY.info("Begin language-level search in termbase "
                + m_termbase.getName() + " for " + rp.getSearchText());

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + rp.toString());
        }

        ArrayList array = new ArrayList();
        String language = rp.getLanguage();

        m_termbase.addReader();

        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append("from TbConcept t where t.termbase.id=");
            hql.append(m_termbase.getId());
            List list = HibernateUtil.search(hql.toString());

            Iterator<TbConcept> ite = list.iterator();

            while (ite.hasNext())
            {
                TbConcept tl = ite.next();

                if (entryIsLocked(tl.getId()))
                {
                    String message = "entry "
                            + tl.getId()
                            + " is locked, ignoring...";
                    CATEGORY.info(message);
                }
                else
                {
                    searchField(array, tl.getXml(), tl.getId(), tl
                            .getId());
                }

            }
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
        finally
        {
            m_termbase.releaseReader();
        }

        CATEGORY.info("End language-level search in termbase "
                + m_termbase.getName());

        return array;
    }
}
