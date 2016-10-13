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

import java.sql.Timestamp;
import java.util.*;

import org.dom4j.Element;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.entrycreation.EntryCreation;
import com.globalsight.terminology.entrycreation.IEntryCreation;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.util.GSEntryParse;
import com.globalsight.terminology.util.IEntryParse;
import com.globalsight.util.SessionInfo;

public class ConceptHelper
{
    private IEntryParse entryParse;
    
    public ConceptHelper() {
        entryParse = new GSEntryParse();
    }
    
    public static String fixConceptXml(String xml, long p_entryId) {
        xml = "<conceptGrp>" + xml + "</conceptGrp>";
        Entry entry = new Entry(xml);
        Element root = entry.getDom().getRootElement();
        
        String newStr = new String();
        
        boolean isHaveIdNode = false;
        
        for (Iterator it = root.elementIterator(); it.hasNext();)
        {
            Element elmt = (Element) it.next();

            if (elmt.getName().equals("concept"))
            {
                elmt.setText(Long.toString(p_entryId));
                isHaveIdNode = true;
            }
            
            newStr = newStr + elmt.asXML();
        }
        
        if(!isHaveIdNode) {
            newStr = "<concept>" + p_entryId + "</concept>" + newStr;
        }
        
        return newStr;
    }
    
    /**
     * <p>
     * Create a new entry.
     * </p>
     */
    public long addEntry(long terbseId, Entry p_entry,
            SessionInfo p_session) throws TermbaseException
    {
        IEntryCreation ic = new EntryCreation(WebAppConstants.TERMBASE_XML);
        return ic.addEntry(terbseId, p_entry, p_session);
    }
    
    /**
     * update the entry by the given xml
     */
    public TbConcept updateConcept(long p_entryId, Entry p_newEntry,
            SessionInfo p_session) throws TermbaseException
    {
        Element root = p_newEntry.getDom().getRootElement();
        EntryUtils.setConceptId(p_newEntry, p_entryId);
        // Update modification timestamp in entry (this is UPDATE)
        p_session.setTimestamp();
        EntryUtils.setModificationTimeStamp(p_newEntry, p_session);
        // Have to refresh root variable after all those changes
        root = p_newEntry.getDom().getRootElement();
        
        try
        {
            TbConcept tc = HibernateUtil.load(TbConcept.class, p_entryId);
            TbConcept new_tc = entryParse.getConceptFromXml(root, p_session);
            tc.setDomain(new_tc.getDomain());
            tc.setStatus(new_tc.getStatus());
            tc.setProject(new_tc.getProject());
            tc.setXml(new_tc.getXml());
            tc.setModifyBy(p_session.getUserName());
            Timestamp ts = new Timestamp(p_session.getTimestamp().getTime());
            tc.setModifyDate(ts);
            
            updateTbLanguageFromXML(tc, p_newEntry, p_session);
            
            return tc;
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
    }
    
    private void updateTbLanguageFromXML(TbConcept tc, Entry p_newEntry,
            SessionInfo p_session)
    {
        Element root = p_newEntry.getDom().getRootElement();
        // Count languages and terms and reserve missing ids
        List langGrps = root.selectNodes("/conceptGrp/languageGrp");
        Set languages = tc.getLanguages();
        ArrayList needRemove = new ArrayList();
        
        Iterator it = languages.iterator();

        while (it.hasNext())
        {
            TbLanguage lan = (TbLanguage) it.next();
            boolean isRemoved = true;

            for (int i = 0; i < langGrps.size(); ++i)
            {
                Element langGrp = (Element) langGrps.get(i);

                TbLanguage tlan = entryParse.getTbLanguaeFromXml(tc, langGrp,
                        p_session);
                if (lan.getName().equals(tlan.getName()))
                {
                    isRemoved = false;
                    break;
                }
            }

            if (isRemoved)
            {
                needRemove.add(lan);
            }
        }
        
        tc.getLanguages().removeAll(needRemove);

        for (int i = 0; i < langGrps.size(); ++i)
        {
            Element langGrp = (Element) langGrps.get(i);
            boolean isExist = false;
            TbLanguage tlan = 
                entryParse.getTbLanguaeFromXml(tc, langGrp, p_session);

            Iterator ite = languages.iterator();
            
            while (ite.hasNext())
            {
                TbLanguage lan = (TbLanguage) ite.next();
                if (lan.getName().equals(tlan.getName()))
                {
                    isExist = true;
                    lan.setTbid(tlan.getTbid());
                    lan.setLocal(tlan.getLocal());
                    lan.setXml(tlan.getXml());
                    tlan = lan;
                    break;
                }
            }

            if (!isExist)
            {
                tc.getLanguages().add(tlan);
            }

            updateTermsFromXML(tlan, langGrp ,p_newEntry, p_session);
        }
    }

    private void updateTermsFromXML(TbLanguage tlan, Element langGrp,Entry p_newEntry,
            SessionInfo p_session)
    {
        Element root = p_newEntry.getDom().getRootElement();
        List termGrps = langGrp.selectNodes("termGrp");
        String value = new String();

        // produce term-level statements for all terms in this language
        // termGrps = langGrp.selectNodes("termGrp");
        Set terms = tlan.getTerms();
        Set termsCopy = new HashSet(terms);
        Set existedTerms = new HashSet();
        existedTerms.addAll(terms);

        for (Iterator it1 = termGrps.iterator(); it1.hasNext();)
        {
            Element termGrp = (Element) it1.next();
            TbTerm new_tbterm = 
                entryParse.getTbTermFromXml(tlan, termGrp, p_session);
            String termId = termGrp.valueOf("term/@termId");

            if (termId == null || termId.equals("-1000"))
            {
                tlan.getTerms().add(new_tbterm);
            }
            else
            {
                Timestamp ts = new Timestamp(p_session.getTimestamp().getTime());
                long term_id = Long.parseLong(termId);
                
                TbTerm tbterm = HibernateUtil.get(TbTerm.class, term_id);
                //remvoe the existed, at last remains the need deleted term
                existedTerms.remove(tbterm);
                tbterm.setModifyBy(p_session.getUserName());
                tbterm.setModifyDate(ts);
                tbterm.setLanguage(new_tbterm.getLanguage());
                tbterm.setTermContent(new_tbterm.getTermContent());
                tbterm.setType(new_tbterm.getType());
                tbterm.setStatus(new_tbterm.getStatus());
                tbterm.setSortKey(new_tbterm.getSortKey());
                tbterm.setXml(new_tbterm.getXml());
            }
        }
        
        if (!existedTerms.isEmpty())
        {
            tlan.getTerms().removeAll(existedTerms);
        }
    }
}
