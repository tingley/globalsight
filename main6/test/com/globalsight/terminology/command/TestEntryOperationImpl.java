package com.globalsight.terminology.command;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Definition;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.TermbaseTestConstants;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.java.Termbase;
import com.globalsight.util.SessionInfo;

public class TestEntryOperationImpl implements TermbaseTestConstants
{
    EntryOperationImpl m_instance = new EntryOperationImpl();
    
    static long m_tbid = -1;
    static long m_cid = -1;
    static long m_cid_TBX = -1;
    static Definition m_definition;
    static SessionInfo m_session;
    HashMap<?, ?> m_entryLocks = new HashMap<Object, Object>();
    
    @BeforeClass
    public static void init()
    {
        String hql = "from Termbase tb where tb.name = :jTBName and tb.description = :jTBDesc";
        Map<String, String> params = new HashMap<String, String>();
        params.put("jTBName", m_TBName);
        params.put("jTBDesc", m_TBDesc);
        Termbase tb = (Termbase) HibernateUtil.getFirst(hql, params);
        
        if (tb == null)
        {
            tb = new Termbase();
            tb.setName(m_TBName);
            tb.setDescription(m_TBDesc);
            Company company = HibernateUtil.get(Company.class, 1);
            tb.setCompany(company);
            tb.setDefination(m_TBDefi);

            try
            {
                HibernateUtil.save(tb);
                System.out.println("SAVE Termbase: " + m_tbid + "/" + tb.getName() + "/" + tb.getDescription() + "/");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (tb != null)
        {
            m_tbid = tb.getId();
            m_definition = new Definition(m_TBDefi);
            m_session = new SessionInfo(m_user, "Admin");
        }
    }

    @AfterClass
    public static void clear()
    {
        if (m_tbid != -1)
        {
            Termbase tb = (Termbase) HibernateUtil.get(Termbase.class, m_tbid);
            String hql = "from TbConcept tbc where tbc.termbase = :tb and tbc.creationBy = :user";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tb", tb);
            map.put("user", m_user);
            List<?> conceptList = HibernateUtil.search(hql, map);
            if (conceptList != null && conceptList.size() > 0)
            {
                System.out.println("DELETE " + conceptList.size() + " concepts, which is created by " + m_user);
                try
                {
                    HibernateUtil.delete(conceptList);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            try
            {
                 HibernateUtil.delete(tb);
                 System.out.println("DELETE Termbase: " + m_tbid + "/" + tb.getName() + "/" + tb.getDescription() + "/");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        m_definition = null;
        m_session = null;
    }
    
    @Test
    public void testAddEntry()
    {
        m_cid = m_instance.addEntry(m_tbid, m_entry_ZH, m_definition, m_session);
        System.out.println("Create Concept ID is " + m_cid);
        TbConcept tbc = HibernateUtil.get(TbConcept.class, m_cid);
        Assert.assertNotNull(tbc);
    }
    
    /**
     * @see EntryOperationImpl.getEntry(long p_entryId, String fileType, SessionInfo p_session)
     */
    @Test
    public void testGetEntry()
    {
        String entryStr = m_instance.getEntry(m_cid, WebAppConstants.TERMBASE_XML, m_session);
        Entry entry = new Entry(entryStr);
        String actual = EntryUtils.getPreferredTerm(entry, "Chinese (China)");
        assertTrue("\nThe error XML entry string is :\n" + entryStr + "\n", 
                   m_termContentZHCN.equals(actual));
        
        entryStr = m_instance.getEntry(m_cid, WebAppConstants.TERMBASE_TBX, m_session);
        entry = new Entry(entryStr);
        actual = EntryUtils.getPreferredTbxTerm(entry, "zh_CN");
        assertTrue("\nThe error TBX entry string is :\n" + entryStr + "\n", 
                   m_termContentZHCN.equals(actual));
    }
    
    @Test
    public void testUpdateEntry()
    {
        boolean condition = false;
        TbConcept tbcOld = HibernateUtil.get(TbConcept.class, m_cid);
        HibernateUtil.closeSession();
        String termContentFR = "aaa_fr";
        String entryStr = "<conceptGrp><concept></concept>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term termId=\"-1000\">"+termContentFR+"</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term termId=\"-1000\">aaa</term></termGrp></languageGrp>" 
            + "</conceptGrp>";
        Entry newEntry = new Entry(entryStr);
        m_instance.updateEntry(m_cid, newEntry, m_session);
        TbConcept tbc = HibernateUtil.get(TbConcept.class, m_cid);
        Set<?> langs = tbc.getLanguages();
        if (langs.size() == tbcOld.getLanguages().size())
        {
            Iterator<?> it = langs.iterator();
            while (it.hasNext())
            {
                TbLanguage lang = (TbLanguage) it.next();
                if ("fr".equalsIgnoreCase(lang.getLocal()))
                {
                    TbTerm term = (TbTerm) lang.getTerms().iterator().next();
                    if (termContentFR.equals(term.getTermContent()))
                    {
                        condition = true;
                        break;
                    }
                }
            }
        }
        
        assertTrue(condition);
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testLockEntry()
    {
        boolean steal = true;
        String result = m_instance.lockEntry(m_tbid, m_cid, steal, m_entryLocks, m_session);
        assertTrue("First Lock", m_entryLocks.containsKey(m_cid));
        
        result = m_instance.lockEntry(m_tbid, m_cid, steal, m_entryLocks, m_session);
        assertTrue("Second Lock", m_entryLocks.containsKey(m_cid));
    }
    
    @Test
    public void testUnlockEntry()
    {
        String cookie = m_instance.lockEntry(m_tbid, m_cid, true, m_entryLocks, m_session);;
        m_instance.unlockEntry(m_cid, cookie, m_entryLocks, m_session);
    }
    
    @Test
    public void testValidateEntry()
    {
        String regex = "<validationresult><conceptid>\\d</conceptid><validation></validation></validationresult>";
        String result = m_instance.validateEntry(m_definition, m_tbid, m_entry_FR, m_session);
        result = result.replace("\n", "").replace("\r", "");
        assertTrue("\nThe validation result is:\n"+result, result.matches(regex));
    }
    
    @Test
    public void testDeleteEntry()
    {
        m_instance.deleteEntry(m_cid, m_session);
        TbConcept tbc = HibernateUtil.get(TbConcept.class, m_cid);
        assertNull(tbc);
    }
}
