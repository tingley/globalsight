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

package com.globalsight.terminology;

import java.util.*;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.java.TbUtil;
import com.globalsight.terminology.util.SqlUtil;

public class ExactSearch extends AbstractTermSearch
{
    public Hitlist getHitListResults(String srcLan, String trgLan,
            String p_query, int maxHits, int begin) throws TermbaseException
    {
        Hitlist result = new Hitlist();
        String hql = new String();

        HashMap map = new HashMap<String, String>();
        map.put("tbid", m_id);
        
        // Add "distinct" to ignore duplicated records when target language is
        // empty or null.This will reduce performance,so if it will bring
        // performance problem,target language should not be empty or null.
     // Allow empty or null target language.
        if (trgLan != null && !"".equals(trgLan.trim()))
        {
            hql = "select src from TbTerm src, TbTerm trg";
            hql = hql + " where src.tbLanguage.concept.termbase.id=:tbid";
            hql = hql + " and trg.tbLanguage.concept.termbase.id=:tbid";
            hql = hql + " and src.tbLanguage.name='" + SqlUtil.quote(srcLan)
                    + "'";
            hql = hql + " and trg.tbLanguage.name='" + SqlUtil.quote(trgLan)
                    + "'";
            hql = hql + " and src.tbLanguage.concept=trg.tbLanguage.concept";

            if (p_query != null && !p_query.trim().equals(""))
            {
                hql = hql + " and src.termContent=:termContent ";
                map.put("termContent", p_query.trim());
            }
        }
        else
        {
           hql = "select tt from TbTerm tt where tt.tbLanguage.name='" 
               + SqlUtil.quote(srcLan) + "'";
           hql = hql + " and tt.tbLanguage.concept.termbase.id=:tbid";
           
           if (p_query != null && !p_query.trim().equals(""))
           {
               hql = hql + " and tt.termContent=:termContent ";
               map.put("termContent", p_query.trim());
           }
        }

        Collection terms = HibernateUtil.search(hql, map, begin, maxHits);
        Iterator iter = terms.iterator();
        
        if (terms.size() == 0)
        {
            if(begin > 0) {
                flag = "isLast";
            }
            else {
                flag = "noResults";
            }
        }
        else
        {
            flag = "";
        }
        
        while (iter.hasNext())
        {
            TbTerm term = (TbTerm) iter.next();
            result.add(term.getTermContent(), term.getTbLanguage()
                    .getConcept().getId(), term.getId(), 100, term.getXml());
        }

        return result;
    }
}
