package com.globalsight.terminology.entrycreation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Definition;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseTestConstants;
import com.globalsight.terminology.TermbaseTestHelper;
import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.terminology.importer.MockImportManager;
import com.globalsight.util.SessionInfo;

/**
 * The Parent Class for Terminology/Termbase Import JUnit Test
 */
public class EntryCreationBasic implements TermbaseTestConstants
{
    IEntryCreation m_instance;
    String m_fileType;
    SyncOptions m_options;
    static long m_tbid = -1;
    static Definition m_definition;
    static ArrayList<Entry> m_entries;
    static SessionInfo m_session;
    static String m_fileName;
    static String m_optionStr;
    
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
            m_session = new SessionInfo(m_user, m_userRole);
        }
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
        
        if (m_instance == null)
        {
            initData();
        }
        
        if (m_entries == null)
        {
            com.globalsight.terminology.java.Termbase javaTB = 
                HibernateUtil.get(com.globalsight.terminology.java.Termbase.class, m_tbid);
            Termbase tb = TermbaseTestHelper.getTermbase(javaTB);
            
            if (WebAppConstants.TERMBASE_XML.equals(m_fileType))
            {
                m_fileName = FileUtil.getResourcePath(EntryCreationBasic.class, "source/import.xml");
                m_optionStr = "<importOptions><fileOptions><fileName>" + m_fileName + "</fileName>"
                        + "<fileType>xml</fileType><fileEncoding>UTF-8</fileEncoding><separator>tab</separator>"
                        + "<ignoreHeader>false</ignoreHeader><entryCount>2</entryCount><status>analyzed</status><errorMessage/>"
                        + "</fileOptions>"
                        + "<syncOptions><syncMode>add_as_new</syncMode><syncLanguage/><syncAction/><nosyncAction/></syncOptions>"
                        + "<columnOptions/></importOptions>";
               
            }
            else if (WebAppConstants.TERMBASE_TBX.equals(m_fileType))
            {
                m_fileName = FileUtil.getResourcePath(EntryCreationBasic.class, "source/import.tbx");
                m_optionStr = "<importOptions><fileOptions><fileName>" + m_fileName + "</fileName>"
                        + "<fileType>tbx</fileType><fileEncoding>UTF-8</fileEncoding><separator>tab</separator>"
                        + "<ignoreHeader>false</ignoreHeader><entryCount>2</entryCount><status>analyzed</status><errorMessage/>"
                        + "</fileOptions>"
                        + "<syncOptions><syncMode>add_as_new</syncMode><syncLanguage/><syncAction/><nosyncAction/></syncOptions>"
                        + "<columnOptions/></importOptions>";
            }
            
            MockImportManager importer = new MockImportManager(tb, m_session, m_fileName, m_optionStr);
            m_entries = importer.getEntryList();
        }
    }

    protected void initData(){}

    @SuppressWarnings("unchecked")
    public static List getTBConceptList()
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
        IEntryCreation ic = new EntryCreation(m_fileType);
        Entry entry = new Entry();
        entry.setXml(p_entryXML);
        return ic.addEntry(m_tbid, entry, m_session);
    }
}
