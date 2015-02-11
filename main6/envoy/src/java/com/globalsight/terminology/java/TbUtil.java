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

package com.globalsight.terminology.java;

import com.globalsight.persistence.hibernate.HibernateUtil;
import java.util.*;

public class TbUtil
{
    public static TbLanguage getLanguageByName(String lan_name) {
        String hql = "from TbLanguage where name='" + lan_name + "'";
        List lanList = HibernateUtil.search(hql);
        
        TbLanguage tl = null;
        
        if(lanList!= null && lanList.size() > 0 ) {
            tl = (TbLanguage)lanList.get(0);
        }
        
        return tl;
    }
    
    /**
     * Check if current concept has the specified target language.
     */
    public static boolean ConceptIfHasTrcLan(TbTerm term, String trgLan)
    {
        Set languages = term.getTbLanguage().getConcept().getLanguages();
        Iterator i = languages.iterator();

        while (i.hasNext())
        {
            TbLanguage tl = (TbLanguage) i.next();
            // Must be case insensitive here.
            if (tl.getName().equalsIgnoreCase(trgLan))
            {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Check if current concept has the specified target language.
     */
    public static boolean ConceptIfHasTrcLan(long TbConceptId, String trgLan)
    {
        TbConcept tc = HibernateUtil.get(TbConcept.class, TbConceptId);
        Set languages = tc.getLanguages();
        Iterator i = languages.iterator();

        while (i.hasNext())
        {
            TbLanguage tl = (TbLanguage) i.next();
            // Must be case insensitive here.
            if (tl.getName().equalsIgnoreCase(trgLan))
            {
                return true;
            }
        }

        return false;
    }
    
    /**
     * if the search string in HQL has the "'", when call the hibernate search method,
     * will get error. so must replace the "'" to "''", it will work.
     */
    public static String FixTermIllegalChar(String term) {
        term = term.replaceAll("'", "''");
        
        return term;
    }
    
}
