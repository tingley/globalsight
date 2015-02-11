package com.globalsight.terminology.searchreplace;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseTestConstants;
import com.globalsight.terminology.TermbaseTestHelper;
import com.globalsight.terminology.entrycreation.EntryCreation;
import com.globalsight.terminology.entrycreation.IEntryCreation;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.SessionInfo;

public class TestTermMaintance implements TermbaseTestConstants
{
    static TermMaintance m_instance;
    static Termbase m_termbase;
    static long m_tbid = -1;
    static SessionInfo m_session;
    static SearchReplaceParams m_rp;
    
    @BeforeClass
    public static void beforeClass()
    {
        String hql = "from Termbase tb where tb.name = :jTBName and tb.description = :jTBDesc";
        Map<String, String> params = new HashMap<String, String>();
        params.put("jTBName", m_TBName);
        params.put("jTBDesc", m_TBDesc);
        com.globalsight.terminology.java.Termbase javaTB = 
            (com.globalsight.terminology.java.Termbase) HibernateUtil.getFirst(hql, params);
        
        if (javaTB == null)
        {
            javaTB = new com.globalsight.terminology.java.Termbase();
            javaTB.setName(m_TBName);
            javaTB.setDescription(m_TBDesc);
            Company company = HibernateUtil.get(Company.class, 1);
            javaTB.setCompany(company);
            javaTB.setDefination(m_TBDefi);

            try
            {
                HibernateUtil.save(javaTB);
                System.out.println("SAVE Termbase: " + m_tbid + "/" + 
                       javaTB.getName() + "/" + javaTB.getDescription() + "/");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (javaTB != null)
        {
            m_tbid = javaTB.getId();
            m_termbase = TermbaseTestHelper.getTermbase(javaTB);
            m_session = new SessionInfo(m_user, m_userRole);
        }
        
        String searchText = FIELD_TERM_LEVEL_STR; 
        String searchType = null; 
        String level  = null; 
        String language = "French"; 
        String field = null; 
        String fieldName = null;
        String caseInsensitive = null; 
        String smartReplace = null; 
        String wholeword = null;
        m_rp = new SearchReplaceParams(searchText, searchType, level, language, 
                   field, fieldName, caseInsensitive, smartReplace, wholeword);
        
        m_instance = new TermMaintance(m_rp, m_termbase);
    }
    
    @AfterClass
    public static void afterClass()
    {
        List<?> conceptList = getTBConceptList();
        if (conceptList != null && conceptList.size() > 0)
        {
            try
            {
                HibernateUtil.delete(conceptList);
                System.out.println("DELETE " + conceptList.size() + " concepts, which is created by " + m_user);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (m_tbid != -1)
        {
            com.globalsight.terminology.java.Termbase javaTB = 
                HibernateUtil.get(com.globalsight.terminology.java.Termbase.class, m_tbid);
            try
            {
                HibernateUtil.delete(javaTB);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Before
    public void before()
    {
        List<?> conceptList = getTBConceptList();
        if (conceptList != null && conceptList.size() > 0)
        {
            try
            {
                HibernateUtil.delete(conceptList);
                System.out.println("DELETE " + conceptList.size() + " concepts, which is created by " + m_user);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        String entryStr = "<conceptGrp>"
                + "<concept>82</concept><transacGrp>"
                + "<transac type=\"origination\">kkTest</transac><date>2011-05-16T17:39:16</date></transacGrp>"
                + "<transacGrp><transac type=\"modification\">kkTest</transac><date>2011-05-17T17:30:39</date></transacGrp>"
                + "<descripGrp><descrip type=\"text-field_text\">" + FIELD_CONCEPT_LEVEL_STR + "</descrip></descripGrp>"
                + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term termId=\"203\">aaa</term></termGrp></languageGrp>"
                + "<languageGrp>"
                + "<language name=\"French\" locale=\"fr\"/>"
                + "     <descripGrp><descrip type=\"text-field_text\">" + FIELD_LANGUAGE_LEVEL_STR + "</descrip></descripGrp>"
                + "     <termGrp>"
                + "         <term termId=\"204\">aaa_FR_XML</term>"
                + "         <descripGrp><descrip type=\"text-field_text\">" + FIELD_TERM_LEVEL_STR + "</descrip></descripGrp>"
                + "     </termGrp>"
                + "</languageGrp>"
                + "</conceptGrp>";
        
        addEntry(entryStr);
        
        setSearchText(FIELD_TERM_LEVEL_STR);
    }
    
    @Test
    public void testReplace()
    {
        ArrayList<?> arrayList = m_instance.search();
        SearchResult result = (SearchResult) arrayList.get(0);
        long levelId = result.getLevelId();
        String oldFieldText = result.getField();
        String replaceText = "replaceText";
        try
        {
            m_instance.replace(levelId, oldFieldText, replaceText);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        setSearchText(replaceText);
        arrayList = m_instance.search();
        assertNotNull(arrayList);
    }

    @Test
    public void testSearch()
    {
        ArrayList<?> arrayList = m_instance.search();
        assertNotNull(arrayList);
        SearchResult result = (SearchResult) arrayList.get(0);
        assertTrue(FIELD_TERM_LEVEL_STR.equals(result.getField()));
    }

    @SuppressWarnings("unchecked")
    protected static List getTBConceptList()
    {
        String hql = "from TbConcept tbc where tbc.termbase.id = :tbid and tbc.creationBy = :user";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tbid", m_tbid);
        map.put("user", m_user);
        
        List result = (List) HibernateUtil.search(hql, map);
        if (result == null)
        {
            result = new ArrayList();
        }
        return result;
    }
    
    public long addEntry(String p_entryXML)
    {
        IEntryCreation ic = new EntryCreation(WebAppConstants.TERMBASE_XML);
        Entry entry = new Entry();
        entry.setXml(p_entryXML);
        return ic.addEntry(m_tbid, entry, m_session);
    }
    
    /**
     * Set searchText for m_rp(SearchReplaceParams).
     */
    public void setSearchText(String p_searchText)
    {
        if (m_rp.getSearchText().equals(p_searchText))
        {
            return;
        }

        try
        {
            ClassUtil.updateField(m_rp, "m_searchText", p_searchText);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
