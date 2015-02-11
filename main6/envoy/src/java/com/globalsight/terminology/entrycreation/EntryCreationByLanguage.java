package com.globalsight.terminology.entrycreation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ConceptHelper;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.util.SessionInfo;

public abstract class EntryCreationByLanguage extends EntryCreation
{
    private static final Logger CATEGORY = 
        Logger.getLogger(EntryCreationByLanguage.class);
    
    public EntryCreationByLanguage(String type)
    {
        super(type);
    }
    
    protected abstract List getConceptsList(long terbseId, Entry p_entry,
            SessionInfo p_session);
    
    public void batchAddEntriesAsNew(long terbseId, ArrayList p_entries,
            SessionInfo p_session) throws TermbaseException
    {
        for (int i = 0; i < p_entries.size(); i++)
        {
            Entry entry = (Entry) p_entries.get(i);
            List tcs = getConceptsList(terbseId, entry, p_session);
            if (tcs == null)
            {
                continue;
            }
            try
            {
                // must close session, because in the session we have use load()
                // or get()
                // to get a persistent object of TbConcept, when we saveorupdate
                // the new TbConcept object with the same id, will have error.
                HibernateUtil.closeSession();
                // HibernateUtil.saveOrUpdate(array);

                for (int j = 0; j < tcs.size(); j++)
                {
                    TbConcept tc = (TbConcept) tcs.get(j);
                    try
                    {
                        HibernateUtil.saveOrUpdate(tc);

                        String xml = ConceptHelper.fixConceptXml(tc.getXml(),
                                tc.getId());
                        tc.setXml(xml);
                        HibernateUtil.update(tc);
                    }
                    catch (Exception e)
                    {
                        failedBatchEntries.add(entry);
                        CATEGORY
                                .error("import terminology error, entry content:"
                                        + tc.getXml());
                    }
                }
            }
            catch (Exception e)
            {
                throw new TermbaseException(e);
            }
        }
    }
}
