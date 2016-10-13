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
package com.globalsight.terminology.entrycreation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dom4j.Element;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ConceptHelper;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.java.TbUtil;
import com.globalsight.terminology.util.GSEntryParse;
import com.globalsight.terminology.util.IEntryParse;
import com.globalsight.terminology.util.TBXEntryParse;
import com.globalsight.util.SessionInfo;

public class EntryCreation implements IEntryCreation
{
    private static final Logger CATEGORY = Logger
            .getLogger(EntryCreation.class);
    protected IEntryParse entryParse;
    protected SyncOptions options;
    protected String fileType;
    protected INosyncAction nosync;
    protected ArrayList failedBatchEntries = new ArrayList();

    public EntryCreation(String p_fileType)
    {
        fileType = p_fileType;
        doInit();
    }
    
    private void doInit()
    {
        if (fileType != null
                && fileType.equalsIgnoreCase(WebAppConstants.TERMBASE_TBX))
        {
            entryParse = new TBXEntryParse();
        }
        else
        {
            entryParse = new GSEntryParse();
        }
    }

    @Override
    public long addEntry(long terbseId, Entry p_entry, SessionInfo p_session)
            throws TermbaseException
    {
        try
        {
            TbConcept tc = getTbConceptByEntry(terbseId, p_entry, p_session);
            HibernateUtil.saveOrUpdate(tc);

            String xml = ConceptHelper.fixConceptXml(tc.getXml(), tc.getId());
            tc.setXml(xml);
            HibernateUtil.update(tc);

            return tc.getId();
        }
        catch (Exception e)
        {
            CATEGORY.error("Create Entry error!");
            throw new TermbaseException(e);
        }
    }

    @Override
    public void batchAddEntriesAsNew(long terbseId, ArrayList p_entries,
            SessionInfo p_session) throws TermbaseException
    {
        HashMap<TbConcept, Entry> map = new HashMap<TbConcept, Entry>();

        for (int i = 0; i < p_entries.size(); i++)
        {
            Entry entry = (Entry) p_entries.get(i);
            try
            {
                TbConcept tc = getTbConceptByEntry(terbseId, entry, p_session);
                if (tc != null)
                    map.put(tc, entry);
            }
            catch (Exception e)
            {
                failedBatchEntries.add(entry);
                CATEGORY.error("Entry parse error, Entry content:"
                        + entry.getDom().asXML());
            }
        }

        // must close session, because in the session we have use load() or
        // get()
        // to get a persistent object of TbConcept to check if the concept
        // has been existed in the database, when we saveorupdate
        // the new TbConcept object with the same id, will have error.
        // and when saveOrUpdate, if there are no session, will create a new
        // session.
        HibernateUtil.closeSession();
        Iterator ite = map.entrySet().iterator();
        
        while(ite.hasNext()) {
            Map.Entry mapentry = (Map.Entry) ite.next();
            TbConcept tc = (TbConcept)mapentry.getKey();
            Entry entry = (Entry)mapentry.getValue();
            
            try
            {
                HibernateUtil.saveOrUpdate(tc);

                String xml = ConceptHelper.fixConceptXml(tc.getXml(), tc
                        .getId());
                tc.setXml(xml);
                HibernateUtil.update(tc);
            }
            catch (Exception e)
            {
                failedBatchEntries.add(entry);
                CATEGORY.error("import terminology error, entry content:"
                        + tc.getXml());
            }
        }
    }

    @Override
    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    @Override
    public void setSynchronizeOption(SyncOptions p_options)
    {
        this.options = p_options;
        nosync = new NosyncAction(p_options);
    }

    protected TbConcept getTbConceptByEntry(long terbseId, Entry p_entry,
            SessionInfo p_session) throws TermbaseException
    {
        Element root = p_entry.getDom().getRootElement();

        // Count languages and terms and reserve ids
        List langGrps = root.selectNodes(entryParse.getLanNodeName());

        // Update creation timestamp in entry (this is ADD)
        p_session.setTimestamp();
        EntryUtils.setCreationTimeStamp(p_entry, p_session);

        // Have to refresh root variable after all those changes
        root = p_entry.getDom().getRootElement();

        try
        {
            TbConcept tc = entryParse.getConceptFromXml(root, p_session);
            com.globalsight.terminology.java.Termbase tb = HibernateUtil.load(
                    com.globalsight.terminology.java.Termbase.class, terbseId);
            

            tc.setTermbase(tb);

            // produce language-level statements
            for (int i = 0, max = langGrps.size(); i < max; ++i)
            {
                Element langGrp = (Element) langGrps.get(i);
                TbLanguage tlan = entryParse.getTbLanguaeFromXml(tc, langGrp,
                        p_session);
                tc.getLanguages().add(tlan);

                // produce term-level statements for all terms in this language
                List termGrps = new ArrayList();
                String[] termNodeNames = entryParse.getTermNodeNames();
                // Loop term node names
                for (int j=0; j<termNodeNames.length; j++)
                {
                    termGrps = langGrp.selectNodes(termNodeNames[j]);
                    if (termGrps != null && termGrps.size() > 0) {
                        break;
                    }
                }
                for (Iterator it1 = termGrps.iterator(); it1.hasNext();)
                {
                    Element termGrp = (Element) it1.next();
                    TbTerm tbterm = entryParse.getTbTermFromXml(tlan, termGrp,
                            p_session);
                    tlan.getTerms().add(tbterm);
                }
            }

            return tc;
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
    }

    protected List getConceptByTermsAndLan(long terbseId, String p_term,
            String p_lan)
    {
        StringBuffer hql = new StringBuffer();
        p_term = TbUtil.FixTermIllegalChar(p_term);
        hql.append("select tm.tbLanguage.concept from TbTerm tm ");
        hql.append("where tm.tbLanguage.concept.termbase.id=").append(terbseId);
        hql.append(" and tm.tbLanguage.name='").append(p_lan).append("'");
        hql.append(" and tm.termContent='").append(p_term).append("'");

        List list = HibernateUtil.search(hql.toString());

        return list;

    }
    
    protected TbConcept getOverwriteConcept(TbConcept tcNew, TbConcept tcOld)
    {
        Iterator iteNew = tcNew.getLanguages().iterator();
        ArrayList needRemove = new ArrayList();

        while (iteNew.hasNext())
        {
            TbLanguage tlNew = (TbLanguage) iteNew.next();
            Iterator iteOld = tcOld.getLanguages().iterator();

            while (iteOld.hasNext())
            {
                TbLanguage tlOld = (TbLanguage) iteOld.next();

                if (tlNew.getName().equals(tlOld.getName()))
                {
                    needRemove.add(tlOld);
                }
            }

            tlNew.setConcept(tcOld);
            tcOld.getLanguages().add(tlNew);
        }

        tcOld.getLanguages().removeAll(needRemove);

        return tcOld;
    }
    
    protected TbConcept getMergeConcept(TbConcept tcNew, TbConcept tcOld)
    {
        Iterator iteNew = tcNew.getLanguages().iterator();
        ArrayList array = new ArrayList();

        while (iteNew.hasNext())
        {
            TbLanguage tlNew = (TbLanguage) iteNew.next();
            Iterator iteOld = tcOld.getLanguages().iterator();
            boolean isExist = false;

            while (iteOld.hasNext())
            {
                TbLanguage tlOld = (TbLanguage) iteOld.next();

                if (tlNew.getName().equals(tlOld.getName()))
                {
                    isExist = true;
                    mergeLange(tlNew, tlOld);
                }
            }
            
            if(!isExist) {
                tlNew.setConcept(tcOld);
                array.add(tlNew);
            }
        }
        
        tcOld.getLanguages().addAll(array);

        return tcOld;
    }
    
    private void mergeLange(TbLanguage tlNew, TbLanguage tlOld) {
        Iterator iteNew = tlNew.getTerms().iterator();
        ArrayList array = new ArrayList();
        
        while (iteNew.hasNext())
        {
            TbTerm ttNew = (TbTerm)iteNew.next();
            Iterator iteOld = tlOld.getTerms().iterator();
            boolean isExist = false;
            
            while(iteOld.hasNext())
            {
                TbTerm ttOld = (TbTerm)iteOld.next();
                if(ttNew.getTermContent().trim().equals(ttOld.getTermContent().trim()))
                {
                    isExist = true;
                }
            }
            
            if(!isExist)
            {
                ttNew.setTbLanguage(tlOld);
                array.add(ttNew);
            }
        }
        
        tlOld.getTerms().addAll(array);
    }
    
    public ArrayList getFailedEntries() {
        return this.failedBatchEntries;
    }
}
