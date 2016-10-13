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

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.ValidationInfo;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.util.SqlUtil;

import org.dom4j.Node;
import java.util.*;

/**
 * Entry validation: examines all terms for duplicates or near
 * duplicates in the database and returns a list of the terms and
 * entries in which they occur.
 *
 * @see ValidationInfo
 */
public class Validator
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            Validator.class);

    /**
     * Validates an entry. Caller must lock Termbase object with
     * Termbase.addReader().
     */
    static public ValidationInfo validate(Entry p_entry, Definition m_definition, long tb_id)
        throws TermbaseException
    {
        long cid = EntryUtils.getConceptId(p_entry);
        ValidationInfo result = new ValidationInfo(cid);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Validating entry " + cid + "...");
        }

        List langGrps = EntryUtils.getLanguageGrps(p_entry);

        for (int i = 0, max = langGrps.size(); i < max; i++)
        {
            Node langGrp = (Node)langGrps.get(i);

            String langName = langGrp.valueOf("language/@name");
            Definition.Language language =
                m_definition.getLanguage(langName);

            List terms = EntryUtils.getTerms(langGrp);

            for (int j = 0, max2 = terms.size(); j < max2; j++)
            {
                Node termNode = (Node)terms.get(j);
                Node termAttribute = termNode.selectSingleNode("//term/@termId");
                String termId = null;
                if(termAttribute != null) {
                    termId = termAttribute.getText();
                }
                String term = termNode.getText();

                // Collect exact matches from the same language.
                Hitlist hits = getDuplicateTerm(tb_id, langName,term, termId);

                addHits(result, langName, term, hits);

                // Should look for fuzzy matches too.
            }
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Validating entry " + cid + "... done.");
        }

        return result;
    }
    
    static private Hitlist getDuplicateTerm(long tbId, String langName, String term, String termId) {
        Hitlist result = new Hitlist();
        String hql = new String();

        com.globalsight.terminology.java.Termbase tbase = 
            HibernateUtil.get(com.globalsight.terminology.java.Termbase.class, tbId);
        HashMap map = new HashMap<String, String>();
        map.put("tbase", tbase);
        map.put("planguage", SqlUtil.quote(langName));
        hql = "select tm from TbTerm tm where tm.tbLanguage.concept.termbase=:tbase" 
            + " and tm.tbLanguage.name=:planguage and " +
            "tm.termContent=:termContent";
        map.put("termContent", term.trim());
        Collection terms = HibernateUtil.search(hql, map);
        Iterator iter = terms.iterator();
        while(iter.hasNext()) {
            TbTerm tbterm = (TbTerm) iter.next();
            if (termId != null && tbterm.getId() != Long.parseLong(termId))
            {
                result.add(tbterm.getTermContent(), tbterm.getTbLanguage()
                        .getConcept().getId(), tbterm.getId(), 100, tbterm
                        .getXml());
            }
        }
        
        return result;
    }

    static private void addHits(ValidationInfo p_info, String p_language,
        String p_term, Hitlist p_hits)
    {
        ArrayList hits = p_hits.getHits();

        for (int i = 0, max = hits.size(); i < max; i++)
        {
            Hitlist.Hit hit = (Hitlist.Hit)hits.get(i);

            p_info.addDuplicate(p_language, p_term, p_language,
                hit.getTerm(), hit.getConceptId(), hit.getTermId());
        }
    }
}
